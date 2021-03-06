package strategy.db_southboundFlowPortfolio;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import stockPrice.DataGetter.StockDataField;

public class PortfolioScreening {
	public static String outputPath = "";
	public static int topNStocks = 20;
	public static ArrayList<StockSingleDate> stockList = new ArrayList<StockSingleDate>();
	public static double avgDailyValueThreshHold_USD = 7000000.0;
	public static int oneMonthBeforeDays = 20; // 正常而言这应该是 ind - 20，表示考虑20天前的数据\
	public static int stockUniverse = 1;
		/*
		 * 1 - all southbound stocks
		 * 2 - all HSI stocks
		 * 3 - all HSCEI stocks
		 */
	public static ArrayList<String>  blackList = new ArrayList<String>() ;   //股票黑名单，在这上面的股票都不会被买入
	public static boolean isToCalNotional = true; 
	/*
	 * 是否计算notional的value的chg
	 * 计算方法：	因为T日得到的southbound的CCASS change其实只是T-2日的数据，而且只有股数，所以要计算T日得到的数据和T-1日的数据的chg，其实
	 * 			计算方式为：(T-2的股数 - T-3日的股数) * T-2的股价
	 */
	
	public static Logger logger = LogManager.getLogger(PortfolioScreening.class.getName());
	public static Map<String, String> ffPctMap = new HashMap();
	public static Map<String, Map<Date,ArrayList<Double>>> sbDataMap = new HashMap<String, Map<Date,ArrayList<Double>>>();  // String - stock code, Date - date, ArrayList<Double> - {holding shares, holding value}
	public static Map<String, Map<Date,ArrayList<Double>>> osDataMap = new HashMap<String, Map<Date,ArrayList<Double>>>();  // String - stock code, Date - date, ArrayList<Double> - {outstanding shares, outstanding value}
	public static Map<String, Map<Date,ArrayList<Double>>> SbChgPercentileDataMap = new HashMap<String, Map<Date,ArrayList<Double>>>();  // String - stock code, Date - date, ArrayList<Double> - {percentile wrt all hist data, percentile wrt to 250-day data, percentile wrt to 60-day data}
	public static Map<String, ArrayList<Object>> osDataMap2 = new HashMap<String, ArrayList<Object>>();  // String - stock, ArrayList<Object> - 三个元素，一个是ArrayList<Date>，一个是ArrayList<Double>，一个是ArrayList<Double>，第一个是股本有变化的日期，第二个是当日的股本，第三个当日的市值
	public static String outstandingFilePath = 
			utils.Utils.addBackSlashToPath(
					utils.PathConifiguration.HK_STOCK_OUTSTANDING_DATA);
			//"D:\\stock data\\HK CCASS - WEBB SITE\\outstanding\\";
	//public static Map<String, Map<Date,ArrayList<Double>>> priceDataMap = new HashMap<String, Map<Date,ArrayList<Double>>>();  // String - stock code, Date - date, ArrayList<Double> - {unadjusted price, adjusted price}
	//public static String priceFilePath = "Z:\\Mubing\\stock data\\stock hist data - webb\\";
	public static Map<Date, Map<String, Double>> notionalChgData = new HashMap<Date, Map<String, Double>>();	// 每一天，所有港股通股票相对于前一天的flow的notional chg（
											// CCASS是T+2的，举个例子，比如某只股票15号得到的CCASS holding是1000股，14号的CCASS holding是900股，则从数据上看是增加了100股
											// 但其实15号得到的数据是13号的实际holding，14号是12号的实际holding，所以计算notional的时候，应该用 （15号的数据 - 14号的数据）*13号的股价
	public static String notionalChgDataRootPath = "Z:\\Mubing\\stock data\\southbound flow strategy - db\\southbound notional chg\\";
	public static String avgVolMainPath = "Z:\\Mubing\\stock data\\southbound flow strategy - db\\stock avg trd vol - 1M\\";
	
	public static String allTradingDatePath = utils.PathConifiguration.ALL_TRADING_DATE_PATH_HK;
	public static ArrayList<Date> allTradingDate = new ArrayList<Date>(); 
	
	public static boolean isNormalSorting = true; //normal sorting - rank with higher rank in the front

	/**
	 * 在某一天进行portfolio screening. 先不选出合适的股票，只是把每只股票的相关数据以及ranking全部 计算出来，然后排序
	 * @param date
	 * @param dateFormat
	 * @param isOutput
	 * @return
	 */
	public static ArrayList<StockSingleDate> assignValue_singleDate(String date, String dateFormat){
		logger.trace("======= Stocks Screening - " + date + " ===============");
		long startTime = System.currentTimeMillis(); 
		
		ArrayList<StockSingleDate> stockList = new ArrayList<StockSingleDate>();
		//ArrayList<StockSingleDate> stockPickedList = new ArrayList<StockSingleDate>();
		ArrayList<String> stockListStr = new ArrayList<String> ();
		SimpleDateFormat sdf_yyyyMMdd = new SimpleDateFormat("yyyyMMdd"); 
	
		try {
			// ==== convert the date to the most recent trading date before it =========
			SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
			//Date todayDate = utils.Utils.getMostRecentDate(sdf.parse(date), allTradingDate);
			Date todayDate = sdf.parse(date);
			//logger.info("today date = " + sdf.format(todayDate));
			
			int ind = allTradingDate.indexOf(todayDate);
			Date oneMonthBeforeDate = allTradingDate.get(ind - oneMonthBeforeDays); // 正常而言这应该是 ind - 20，表示考虑20天前的数据
			
			// ========= get stock free float pct ========
			if(ffPctMap == null || ffPctMap.isEmpty() || ffPctMap.size() == 0){
				BufferedReader ff_reader = utils.Utils.readFile_returnBufferedReader("D:\\stock data\\freefloat pct - hk.csv");
				String ff_line = "";
				while((ff_line = ff_reader.readLine()) != null) {
					String[] ff_lineArr = ff_line.split(",");
					ffPctMap.put(ff_lineArr[0], ff_lineArr[1]);
				}
				ff_reader.close();
			}
			
			// ========= get southbound info ============
			// southbound的数据是每天的数据存一个文件，我们可以先读出今天的文件的内容
			//Map<String, ArrayList<String>> sbToday_map = webbDownload.southboundData.DataGetter.getStockData_map(date, "yyyyMMdd");
			//Map<String, ArrayList<String>> sbOneMonthBefore_map = webbDownload.southboundData.DataGetter.getStockData_map(oneMonthBefore);
			
			
			// ======== get avg vol info ==========
			// 经过处理后的average volume：每天一个file，存储所有股票前三个月的avg vol
			Map<String, Double> avgVol_map = new HashMap<String, Double>();
			Map<String, Double> avgTur_map = new HashMap<String, Double>();
			String avgVolPath = avgVolMainPath  + date + ".csv";
			BufferedReader bf_avgVol = utils.Utils.readFile_returnBufferedReader(avgVolPath);
			String avgVolLine = "";
			int counter1 = 0;
			while((avgVolLine = bf_avgVol.readLine()) != null) {
				if(counter1 == 0) {
					counter1++;
					continue;
				}
				String[] arr = avgVolLine.split(",");
				String stock = arr[0];
				String avgVol = arr[1];
				String avgTur = arr[2];
				
				avgVol_map.put(stock, Double.parseDouble(avgVol));
				avgTur_map.put(stock, Double.parseDouble(avgTur));
			}
			bf_avgVol.close();
			
			// ----------- get notional chg data ------------
			if(false && (notionalChgData == null || notionalChgData.size() == 0)) {
				getNotionalChgData(notionalChgDataRootPath);
			}
			
			
			// get southbound stock list at the specified date
			ArrayList<String> stockListStrArr = new ArrayList<String>();
			if(stockUniverse == 1) {
				stockListStrArr= utils.Utils.getSouthboundStocks(date, dateFormat, true, true);
				stockListStrArr.remove("607");
				stockListStrArr.remove("1250");
			}
			if(stockUniverse == 2) {
				stockListStrArr= utils.Utils.getHSCEI_HSIStocks(date, dateFormat, false, true);
				stockListStrArr.remove("823");
			}
			if(stockUniverse == 3)
				stockListStrArr= utils.Utils.getHSCEI_HSIStocks(date, dateFormat, true, false);
			if(stockUniverse == 4) {
				stockListStrArr= utils.Utils.getHSCEI_HSIStocks(date, dateFormat, true, true);
				//stockListStrArr.remove("823");
			}
			for(String blStock : blackList) {
				stockListStrArr.remove(blStock);
			}
			
			final int size = stockListStrArr.size();
			StockSingleDate stock;
			for(int i = 0; i < size; i++) {
				String stockCode = stockListStrArr.get(i);  // stock code in the form of "1","6881" etc...
				//System.out.println("======== stock = " + stockCode + " ============");
				
				 stock = new StockSingleDate(stockCode, date, dateFormat);
				
				// ========== get southbound flow information ==============
				//ArrayList<String> today_SBData = webbDownload.southboundData.DataGetter.getStockData(stockCode, date, dateFormat);
				ArrayList<Double> today_SBData = new ArrayList<Double> ();
				Map<Date,ArrayList<Double>> sbDataMap_oneStock = sbDataMap.get(stockCode);
				if(sbDataMap_oneStock != null)
					today_SBData = sbDataMap_oneStock.get(todayDate);
				//Double today_holding = 0.0;
				if(today_SBData != null && today_SBData.size() > 0) {
					stock.SB_today_holding = today_SBData.get(0);
					stock.SB_today_holdingValue = today_SBData.get(1);
				}else {
					logger.debug("[today SB data not exists] code=" + stockCode +  " " + date);
					stock.SB_today_holding = 0.0;
					stock.SB_today_holdingValue = 0.0;
				}
				
				ArrayList<Double> oneMontBeofore_SBData = new ArrayList<Double> ();
				if(sbDataMap_oneStock != null)
					oneMontBeofore_SBData = sbDataMap_oneStock.get(oneMonthBeforeDate);
				if(oneMontBeofore_SBData != null && oneMontBeofore_SBData.size() > 0) {
					stock.SB_1MBefore_holding = oneMontBeofore_SBData.get(0);
					stock.SB_1MBefore_holdingValue = oneMontBeofore_SBData.get(1);
				}else {
					logger.debug("[1 month ago SB data not exists] code=" + stockCode +  " " + date);
					stock.SB_1MBefore_holding = 0.0;
					stock.SB_1MBefore_holdingValue = 0.0;
				}
				
				// ============= get percentile information =======
				Map<Date,ArrayList<Double>> sbPercentileDataMap_oneStock = SbChgPercentileDataMap.get(stockCode);
				ArrayList<Double> today_SBPercentileData = new ArrayList<Double> ();
				if(sbPercentileDataMap_oneStock != null) {
					today_SBPercentileData = sbPercentileDataMap_oneStock.get(todayDate);
				}
				if(today_SBPercentileData != null && today_SBPercentileData.size() > 0) {
					stock.SB_chg_hist_percentile = today_SBPercentileData.get(0);
					stock.SB_chg_250D_percentile = today_SBPercentileData.get(1);
					stock.SB_chg_60D_percentile = today_SBPercentileData.get(2);
				}else {
					logger.debug("[today SB Percentile data not exists] code=" + stockCode +  " " + date);
					stock.SB_chg_hist_percentile = -1.0;
					stock.SB_chg_250D_percentile = -1.0;
					stock.SB_chg_60D_percentile = -1.0;
				}
				
				
				stock.SB_1M_trailingFlow = stock.SB_today_holding - stock.SB_1MBefore_holding;
				//System.out.println(new SimpleDateFormat("yyyyMMdd").format(oneMonthBefore.getTime()) + " Southbound trailing = " + stock.SB_1M_trailingFlow);
				
				// ======== get stock volume & turnover information ============
				//stock.Vol_1M_avg = avgVol;
				if(avgVol_map.get(stockCode) != null)
					stock.Vol_3M_avg = avgVol_map.get(stockCode);
				if(avgTur_map.get(stockCode) != null)
					stock.Turnover_3M_avg = avgTur_map.get(stockCode);
				//System.out.println("avg vol = " + stock.Vol_1M_avg);
				//Thread.sleep(1000 * 10000000);
				
				
				// ========== get outstanding shares / value ==========
				/*
				String todayOsSharesStr = webbDownload.outstanding.DataGetter.getStockDataField(stockCode, 
						webbDownload.outstanding.DataGetter.OutstandingDataField.OUTSTANDING_SHARES, date, dateFormat);
				String todayOsValueStr = webbDownload.outstanding.DataGetter.getStockDataField(stockCode, 
						webbDownload.outstanding.DataGetter.OutstandingDataField.MKT_CAP, date, dateFormat);
						
				String todayOsSharesStr =  "";
				String todayOsValueStr = "";
				ArrayList<String> todayOsLine = webbDownload.outstanding.DataGetter.getStockDataLine(stockCode, date, dateFormat);  //这个语句显著拖慢整体速度
				
				if(todayOsLine != null && todayOsLine.size() > 0) {
					todayOsSharesStr = todayOsLine.get(1);
					todayOsValueStr = todayOsLine.get(5);
				}
				//if(stockCode.equals("680"))
				//	System.out.println("680 today os shares" + todayOsSharesStr);
				//Double todayOsShares = 0.0;
				if(todayOsSharesStr  != null && !todayOsSharesStr.equals("")) {
					try {
						stock.osShares_today = Double.parseDouble(todayOsSharesStr);
					}catch(Exception e) {
						e.printStackTrace();
					}
					
					try {
						stock.osShares_freefloat_today = stock.osShares_today * Double.parseDouble(ffPctMap.get(stockCode));
					}catch(Exception e) {
						stock.osShares_freefloat_today = stock.osShares_today * 1;
					}
				}
				
				if(todayOsValueStr  != null && !todayOsValueStr.equals("")) {
					try {
						stock.osValue_today = Double.parseDouble(todayOsValueStr);
					}catch(Exception e) {
						e.printStackTrace();
					}
					
					try {
						stock.osValue_freefloat_today = stock.osValue_today * Double.parseDouble(ffPctMap.get(stockCode));
					}catch(Exception e) {
						stock.osValue_freefloat_today = stock.osValue_today * 1.0;
					}
				}
				*/
				int method = 2;
				if(method == 1) {  //使用osDataMap
					Map<Date,ArrayList<Double>> osDataMap_oneStock = osDataMap.get(stockCode);
					if(osDataMap_oneStock != null) {
						ArrayList<Double> osData_today = osDataMap_oneStock.get(todayDate);
						if(osData_today != null) {
							stock.osShares_today = osData_today.get(0);
							stock.osValue_today = osData_today.get(1);
							
							try {
								stock.osShares_freefloat_today = stock.osShares_today * Double.parseDouble(ffPctMap.get(stockCode));
							}catch(Exception e) {
								stock.osShares_freefloat_today = stock.osShares_today * 1;
							}
							try {
								stock.osValue_freefloat_today = stock.osValue_today * Double.parseDouble(ffPctMap.get(stockCode));
							}catch(Exception e) {
								stock.osValue_freefloat_today = stock.osValue_today * 1.0;
							}
						}else {
							logger.error("[Today outstanding data not exist!!! date not exist!] stock=" + stockCode + " date=" + date);
						}
						
						ArrayList<Double> osData_oneMonthBefore = osDataMap_oneStock.get(oneMonthBeforeDate);
						if(osData_oneMonthBefore != null) {
							stock.osShares_1MBefore = osData_oneMonthBefore.get(0);
							stock.osValue_1MBefore = osData_oneMonthBefore.get(1);
							
							try {
								stock.osShares_freefloat_1MBefore = stock.osShares_1MBefore * Double.parseDouble(ffPctMap.get(stockCode)); 
							}catch(Exception e) {
								stock.osShares_freefloat_1MBefore = stock.osShares_1MBefore * 1.0;
							}
							try {
								stock.osValue_freefloat_1MBefore = stock.osValue_1MBefore * Double.parseDouble(ffPctMap.get(stockCode)); 
							}catch(Exception e) {
								stock.osValue_freefloat_1MBefore = stock.osValue_1MBefore * 1.0;
							}
						}else {
							logger.error("[Today outstanding data not exist!!! oneMonthBeforeDate not exist!] stock=" + stockCode + " date=" + sdf.format(oneMonthBeforeDate));
						}
					}else {
						logger.error("[Today outstanding data not exist!!! Stock not exist!] stock=" + stockCode + " date=" + date);
					}
				}
				if(method == 2) {//使用osDataMap2
					ArrayList<Double> osData_today = getSingleStockOsData(stockCode, todayDate);
					if(osData_today != null && osData_today.size() > 0) {
						stock.osShares_today = osData_today.get(0);
						stock.osValue_today = osData_today.get(1);
						
						try {
							stock.osShares_freefloat_today = stock.osShares_today * Double.parseDouble(ffPctMap.get(stockCode));
						}catch(Exception e) {
							stock.osShares_freefloat_today = stock.osShares_today * 1;
						}
						try {
							stock.osValue_freefloat_today = stock.osValue_today * Double.parseDouble(ffPctMap.get(stockCode));
						}catch(Exception e) {
							stock.osValue_freefloat_today = stock.osValue_today * 1.0;
						}
					}else {
						logger.error("[Today outstanding data not exist!!! date not exist!] stock=" + stockCode + " date=" + date);
					}
					
					ArrayList<Double> osData_oneMonthBefore = getSingleStockOsData(stockCode, oneMonthBeforeDate);
					if(osData_oneMonthBefore != null && osData_oneMonthBefore.size() > 0) {
						stock.osShares_1MBefore = osData_oneMonthBefore.get(0);
						stock.osValue_1MBefore = osData_oneMonthBefore.get(1);
						
						try {
							stock.osShares_freefloat_1MBefore = stock.osShares_1MBefore * Double.parseDouble(ffPctMap.get(stockCode)); 
						}catch(Exception e) {
							stock.osShares_freefloat_1MBefore = stock.osShares_1MBefore * 1.0;
						}
						try {
							stock.osValue_freefloat_1MBefore = stock.osValue_1MBefore * Double.parseDouble(ffPctMap.get(stockCode)); 
						}catch(Exception e) {
							stock.osValue_freefloat_1MBefore = stock.osValue_1MBefore * 1.0;
						}
					}else {
						logger.error("[Today outstanding data not exist!!! oneMonthBeforeDate not exist!] stock=" + stockCode + " date=" + sdf.format(oneMonthBeforeDate));
					}
				}
				
				/*
				String oneMonthBefore_osShareStr = webbDownload.outstanding.DataGetter.getStockDataField(stockCode, 
						webbDownload.outstanding.DataGetter.OutstandingDataField.OUTSTANDING_SHARES, new SimpleDateFormat(dateFormat).format(oneMonthBefore.getTime()), dateFormat);
				String oneMonthBefore_osValueStr = webbDownload.outstanding.DataGetter.getStockDataField(stockCode, 
						webbDownload.outstanding.DataGetter.OutstandingDataField.MKT_CAP, new SimpleDateFormat(dateFormat).format(oneMonthBefore.getTime()), dateFormat);
				
				String oneMonthBefore_osShareStr = "";
				String oneMonthBefore_osValueStr = "";
				ArrayList<String> oneMonthBefore_osLine = webbDownload.outstanding.DataGetter.getStockDataLine(stockCode, 
						new SimpleDateFormat(dateFormat).format(oneMonthBeforeDate), dateFormat);
				
				if(oneMonthBefore_osLine != null && oneMonthBefore_osLine.size() > 0) {
					oneMonthBefore_osShareStr = oneMonthBefore_osLine.get(1);
					oneMonthBefore_osValueStr = oneMonthBefore_osLine.get(5);
				}
				
				if(oneMonthBefore_osShareStr != null && !oneMonthBefore_osShareStr.equals("")) {
					try {
						stock.osShares_1MBefore = Double.parseDouble(oneMonthBefore_osShareStr); 
					}catch(Exception e) {
						e.printStackTrace();
					}
					try {
						stock.osShares_freefloat_1MBefore = stock.osShares_1MBefore * Double.parseDouble(ffPctMap.get(stockCode)); 
					}catch(Exception e) {
						stock.osShares_freefloat_1MBefore = stock.osShares_1MBefore * 1.0;
					}
				}
				if(oneMonthBefore_osValueStr != null && !oneMonthBefore_osValueStr.equals("")) {
					try {
						stock.osValue_1MBefore = Double.parseDouble(oneMonthBefore_osValueStr); 
					}catch(Exception e) {
						e.printStackTrace();
					}
					try {
						stock.osValue_freefloat_1MBefore = stock.osValue_1MBefore * Double.parseDouble(ffPctMap.get(stockCode)); 
					}catch(Exception e) {
						stock.osValue_freefloat_1MBefore = stock.osValue_1MBefore * 1.0;
					}
				}
				*/
				
				// ============= get the notional change ==============
				if(isToCalNotional) {
					try {
						stock.SB_notional_chg = notionalChgData.get(todayDate)
								.get(stockCode);
					}
					catch(Exception ee) {
						ee.printStackTrace();
						logger.error("  notional data null! stock=" + stock.stockCode + " date=" + date);
					}
					
				}
				
				
				// ========== set the ratio ==========
				// southbound flow change vs. daily turnover
				if(stock.Turnover_3M_avg != null && !stock.Turnover_3M_avg.equals(0.0))
					stock.SB_over_turnover = (stock.SB_today_holdingValue - stock.SB_1MBefore_holdingValue) / stock.Turnover_3M_avg;
				else
					stock.SB_over_turnover = 0.0;
				if(stock.Vol_3M_avg != null && !stock.Vol_3M_avg.equals(0.0))
					stock.SB_over_vol = (stock.SB_today_holding - stock.SB_1MBefore_holding) / stock.Vol_3M_avg;
				else
					stock.SB_over_vol = 0.0;
				
				// southbound change (in shares)
				int totalOrFreeFloat = 0;  // 1 - total; 0 - freefloat
				if(totalOrFreeFloat == 1) {
					// southbound flow change vs. total outstanding shares
					if(!stock.osShares_today.equals(0.0) && !stock.osShares_1MBefore.equals(0.0))
						stock.SB_over_os_shares = stock.SB_today_holding/stock.osShares_today - stock.SB_1MBefore_holding/stock.osShares_1MBefore;
					else
						stock.SB_over_os_shares = 0.0;
				}else {
					// southbound flow change vs. free float shares
					if(!stock.osShares_freefloat_today.equals(0.0) && !stock.osShares_freefloat_1MBefore.equals(0.0))
						stock.SB_over_os_shares = stock.SB_today_holding/stock.osShares_freefloat_today - stock.SB_1MBefore_holding/stock.osShares_freefloat_1MBefore;
					else
						stock.SB_over_os_shares = 0.0;
				}
				
				// southbound flow change vs. free float value
				if(!stock.osValue_freefloat_today.equals(0.0) && !stock.osValue_freefloat_1MBefore.equals(0.0))
					stock.SB_over_os_value_freefloat = stock.SB_today_holdingValue/stock.osValue_freefloat_today - stock.SB_today_holdingValue/stock.osValue_freefloat_1MBefore;
				else
					stock.SB_over_os_value_freefloat = 0.0;
				
				// DB's method to define Southbound over Freefloat
				if(!stock.osValue_freefloat_today.equals(0.0))
					stock.db_SB_over_ff = (stock.SB_today_holdingValue - stock.SB_1MBefore_holdingValue) / stock.osValue_freefloat_today;
				// DB's method to define Southbound over turnover
				if(stock.Turnover_3M_avg != null && !stock.Turnover_3M_avg.equals(0.0))
					stock.db_SB_over_turnover = (stock.SB_today_holdingValue - stock.SB_1MBefore_holdingValue) / 20.0 / stock.Turnover_3M_avg;
				
				// ========== calculate the indicator ==========
				stock.sorting_indicator = stock.SB_over_os_shares;
				//stock.sorting_indicator = stock.SB_over_os_value_freefloat;
				//stock.sorting_indicator  = stock.db_SB_over_ff; // DB's method
				//stock.sorting_indicator = Math.random();
					
				// ========== add to the list ===========
				stockList.add(stock);
				stock=null;
			} // end of for
			
			// =========== sorting stock list ============
			if(true) {
				Collections.sort(stockList, StockSingleDate.getComparator(1));  // first rank by SB_over_os_shares, then rank by SB_over_vol, then add the two ranks up and rank again
				for(int i = 0; i < stockList.size(); i++) {
					StockSingleDate stock1 = stockList.get(i);
					stock1.dummy1 = (double) i;
					stock1.sorting_indicator = stock1.SB_over_vol;
					//stock.sorting_indicator = stock.SB_over_turnover;
					//stock.sorting_indicator = stock.db_SB_over_turnover; // DB's method
				}
				Collections.sort(stockList, StockSingleDate.getComparator(1));  
				for(int i = 0; i < stockList.size(); i++) {
					StockSingleDate stock2 = stockList.get(i);
					stock2.dummy2 = (double) i;
					stock2.sorting_indicator = stock2.dummy1 + stock2.dummy2;
					//stock2.sorting_indicator = stock2.SB_chg_250D_percentile;
				}
			}
			
			if(isNormalSorting)
				Collections.sort(stockList, StockSingleDate.getComparator(-1));  // rank with higher score are in the front
			else
				Collections.sort(stockList, StockSingleDate.getComparator(1));  // rank with lower score are in the front
				
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		long endTime = System.currentTimeMillis(); 
		logger.trace("======= Stocks Screening END - " + date + " time=" + (endTime - startTime)/1000.0 + "s ===============");
		
		
		return stockList;
	}


	/**
	 * 在给定了包含完整数据并且排序好的stockList之后，通过一些filter选出符合条件的股票并进行输出，一般来说要先run完assignValue_singleDate()，再run这个
	 * @param stockList
	 * @param isOutput
	 * @return
	 */
	public static ArrayList<StockSingleDate> pickStocks_singleDate(ArrayList<StockSingleDate> stockList, boolean isOutput){
		ArrayList<StockSingleDate> stockPickedList = new ArrayList<StockSingleDate>(); 
		String date = new SimpleDateFormat("yyyyMMdd").format(stockList.get(0).cal.getTime());
		
		try {
			// ========= get stock free float pct ========
			if(ffPctMap.isEmpty()){
				BufferedReader ff_reader = utils.Utils.readFile_returnBufferedReader("D:\\stock data\\freefloat pct - hk.csv");
				String ff_line = "";
				while((ff_line = ff_reader.readLine()) != null) {
					String[] ff_lineArr = ff_line.split(",");
					ffPctMap.put(ff_lineArr[0], ff_lineArr[1]);
				}
				ff_reader.close();
			}
			
			// ============ apply filters ===========
			int stockCounter = 0;
			for(int i = 0; i < stockList.size(); i++) {
				StockSingleDate s = stockList.get(i);
				String stockCode = s.stockCode;
				
				// ========= test if suspended && volume is OK ==========
				if(s.filter1 > 0 && s.filter2 > 0 && s.filter3 > 0 && s.filter4 > 0 && s.filterRankingStrategy6_1 > 0) {
					if(!s.suspended && s.Turnover_3M_avg > (avgDailyValueThreshHold_USD * 7.8)) {
						//stockListStr.add(s.stockCode);
						stockPickedList.add(s);
						stockCounter ++;
					}
				}
				
				if(stockCounter >= topNStocks)
					break;
				
			}
			
			// ========== display =========
			if(isOutput) {
				FileWriter fw = new FileWriter(outputPath + "\\screening " + date + ".csv");
				//fw.write("stock,1M avg vol,rank1,SB today holding,SB 1M before holding,os share today,os shares 1M before,SB today/os today,SB 1M/os 1M,diff,rank2,rank1+2\n");
				Double avgDailyValueThreshHold_USD_mln = avgDailyValueThreshHold_USD / 1000000;
				DecimalFormat  df = new DecimalFormat("#.0");
				fw.write("stock,mkt cap (US mm),3M ADV (US mm),Score,"
						+ "H-share discount,SB Holding (US mm),SB Holding to FF,"
						+ "1M Change in SB holding (US mm),1M Change in SB holding/FF,rank1,1m SB Flow to Turnover,rank2,"
						+ "suspend,turnover > " + String.valueOf(df.format(avgDailyValueThreshHold_USD_mln)) +"m US?,total rank(idea1),filter1,filter2(idea2),filter3(idea2)\n");
				
				FileWriter fw2 = new FileWriter(outputPath + "\\screening " + date + " shares.csv");
				fw2.write("stock,SB today (shares),SB 1M Before (shares),change,"
						+ "3M ADV (shares),"
						+ "FF today (shares),FF 1M Before (shares),"
						+ "SB today / FF, SB 1M Before / FF,change,"
						+ "SB change / 3M ADV,rank,"
						+ "SB change / FF,rank,"
						+ "suspend,turnover > 7.5m US,"
						+ "\n");
				FileWriter fw3 = new FileWriter(outputPath + "\\screening " + date + " value.csv");
				fw3.write("stock,SB today (value),SB 1M Before (value),change,"
						+ "3M ADV (value USD mm),"
						+ "FF today (value),FF 1M Before (value),"
						+ "SB today / FF, SB 1M Before / FF,change,"
						+ "SB change / 3M ADV,rank,"
						+ "SB change / FF,rank,"
						+ "suspend,turnover > 7.5m US,"
						+ "\n");
				
				for(int i = 0; i < stockList.size(); i++) {
					StockSingleDate s = stockList.get(i);
					//System.out.println(s.stockCode + " " + String.valueOf(s.SB_over_vol));
					//System.out.println("s.stock = " + s.stockCode);
					Double ffValue = s.osValue_today;
					Double ffShare =s.osShares_today;
					Double ffValue_1MBefore = s.osValue_1MBefore;
					Double ffShare_1MBefore =s.osShares_1MBefore;
					try {
						String ffPct_single = ffPctMap.get(s.stockCode);
						if(ffPct_single == null)
							ffPct_single = "1";
						
						ffValue = s.osValue_today* Double.parseDouble(ffPct_single);
						ffShare = s.osShares_today* Double.parseDouble(ffPct_single);
						ffValue_1MBefore = s.osValue_1MBefore * Double.parseDouble(ffPct_single);
						ffShare_1MBefore =s.osShares_1MBefore * Double.parseDouble(ffPct_single);
					}catch(Exception e) {
						e.printStackTrace();
						//System.out.println("   no FF stock=" + s.stockCode);
					}
					
					Double SB_over_ff_today_shares = s.SB_today_holding / ffShare;
					Double SB_over_ff_1MBefore_shares = s.SB_1MBefore_holding / ffShare_1MBefore;
					Double SB_over_ff_today_value = s.SB_today_holdingValue / ffValue;
					Double SB_over_ff_1MBefore_value = s.SB_1MBefore_holdingValue / ffValue_1MBefore;
	
					Double SB_change_share = s.SB_today_holding - s.SB_1MBefore_holding;
					Double SB_change_value = s.SB_today_holdingValue - s.SB_1MBefore_holdingValue; 
					
					Double isVolLarge = s.Turnover_3M_avg / 7.8 > avgDailyValueThreshHold_USD?1.0:0.0;
							
					fw.write(s.stockCode + "," + String.valueOf(s.osValue_today / 7.8 / 1000000) + "," + String.valueOf(s.Turnover_3M_avg / 7.8 / 1000000) + "," + String.valueOf(s.dummy1 + s.dummy2) + ","
							+ "" + "," + String.valueOf(s.SB_today_holdingValue / 7.8 / 1000000) + "," + String.valueOf(s.SB_today_holdingValue / ffValue) + "," 
							+ String.valueOf((s.SB_today_holdingValue - s.SB_1MBefore_holdingValue) / 7.8 / 1000000) + "," + String.valueOf((s.SB_today_holdingValue - s.SB_1MBefore_holdingValue) / ffValue) + "," + String.valueOf(s.dummy1) + "," + String.valueOf((s.SB_today_holdingValue - s.SB_1MBefore_holdingValue) / s.Turnover_3M_avg / 20.0) + "," + s.dummy2 + ","
							+ String.valueOf(s.suspended) + "," + String.valueOf(isVolLarge) + "," 
							+ String.valueOf(s.sorting_indicator) + ","
							+ String.valueOf(s.filter1) + "," + String.valueOf(s.filter2) + "," + String.valueOf(s.filter3) + ","
							+ "\n");
				
		
					//fw.write(s.stockCode + "," + String.valueOf(s.SB_over_os_shares_freefloat) + "," + String.valueOf(s.dummy1) + "\n");
				
					fw2.write(s.stockCode + "," + String.valueOf(s.SB_today_holding) + "," + String.valueOf(s.SB_1MBefore_holding) + "," + String.valueOf(SB_change_share) + ","
							+ String.valueOf(s.Vol_3M_avg ) + ","
							+ String.valueOf(ffShare) + "," + String.valueOf(ffShare_1MBefore) + "," 
							+ String.valueOf(SB_over_ff_today_shares) + "," + String.valueOf(SB_over_ff_1MBefore_shares) + "," + String.valueOf(SB_over_ff_today_shares - SB_over_ff_1MBefore_shares) + ","
							+ String.valueOf(SB_change_share / s.Vol_3M_avg) + "," + String.valueOf(s.dummy2) + "," 
							+ String.valueOf(SB_over_ff_today_shares - SB_over_ff_1MBefore_shares) + "," + String.valueOf(s.dummy1) + "," 
							+ String.valueOf(s.suspended) + "," + String.valueOf(isVolLarge) + ","
							+ "\n"
							);
					fw3.write(s.stockCode + "," + String.valueOf(s.SB_today_holdingValue) + "," + String.valueOf(s.SB_1MBefore_holdingValue) + "," + String.valueOf(SB_change_value) + ","
							+ String.valueOf(s.Turnover_3M_avg / 7.8 / 1000000 ) + ","
							+ String.valueOf(ffValue) + "," + String.valueOf(ffValue_1MBefore) + "," 
							+ String.valueOf(SB_over_ff_today_value) + "," + String.valueOf(SB_over_ff_1MBefore_value) + "," + String.valueOf(SB_over_ff_today_value - SB_over_ff_1MBefore_value) + ","
							+ String.valueOf(SB_change_value / s.Turnover_3M_avg) + "," + String.valueOf(s.dummy2) + "," 
							+ String.valueOf(SB_over_ff_today_value - SB_over_ff_1MBefore_value) + "," + String.valueOf(s.dummy1) + "," 
							+ String.valueOf(s.suspended) + "," + String.valueOf(isVolLarge) + ","
							+ "\n"
							);
				}
				fw.close();
				fw2.close();
				fw3.close();
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		return stockPickedList;
	}
	
	/**
	 * update all southbound data
	 * @param rootPath
	 */
	public static void getAllSbData(String rootPath) {
		getAllSbData(rootPath, "19000101","30000101", "yyyyMMdd");
	}
	
	/**
	 * 只获得某个时间段内的
	 * @param rootPath
	 * @param startDate
	 * @param endDate
	 * @param dateFormat
	 */
	public static void getAllSbData(String rootPath, String startDateStr, String endDateStr, String dateFormat) {
		try {
			SimpleDateFormat sdf_readDate = new SimpleDateFormat (dateFormat);
			Date startDate = sdf_readDate.parse(startDateStr);
			Date endDate = sdf_readDate.parse(endDateStr);
			
			getAllSbData(rootPath, startDate, endDate);
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void getAllSbData(String rootPath, Date startDate, Date endDate) {
		try {
			//Map<String, Map<Date,Double>> sbDataMap = new HashMap<String, Map<Date,Double>>();
			//rootPath  ="D:\\stock data\\HK CCASS - WEBB SITE\\southbound\\combined";
			
			SimpleDateFormat sdf = new SimpleDateFormat ("yyyy-MM-dd");
			
			
            File allFile = new File(rootPath);
            for(File f : allFile.listFiles()) {
            	String fileName = f.getName();  // "2017-07-21.csv"
            	String filePath = rootPath + "\\" + fileName	;
            	String dateStr = fileName.substring(0, fileName.length() - 4); // "2017-07-21"   
            	Date date = sdf.parse(dateStr);
            	if(date.before(startDate) || date.after(endDate))
            		continue;
            	
            	logger.trace("get all SB data - " + dateStr);
            	
            	BufferedReader bf = utils.Utils.readFile_returnBufferedReader(filePath);
            	String line ="";
            	int count  = 0;
            	while((line = bf.readLine()) != null) {
            		if(count  == 0) {
            			count ++;
            			continue;
            		}
            		String[] lineArr = line.split(",");
            		String stock = lineArr[0];
            		String holding = lineArr[2];
            		Double holdingD = Double.parseDouble(holding);
            		String value = lineArr[3];
            		Double valueD = 0.0;
            		if(utils.Utils.isDouble(value))
            			valueD = Double.parseDouble(value);
            		
            		Map<Date,ArrayList<Double>> stockData = sbDataMap.get(stock);
            		if(stockData == null)
            			stockData = new HashMap<Date,ArrayList<Double>>(); 
            		ArrayList<Double> data = new ArrayList<Double>();
            		data.add(holdingD);
            		data.add(valueD);
            		
            		stockData.put(date, data);
            		
            		sbDataMap.put(stock, stockData);
            		count++;
            	} // end of while
            	bf.close();
            } // end of file for
           
            logger.info("Get All Sb Data - Done!");
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * return sbDataMap
	 */
	public static Map<String, Map<Date,ArrayList<Double>>> getAllSbData_return(String sbDataPath) {
		if(sbDataMap == null || sbDataMap.size() == 0) {
			getAllSbData(sbDataPath);
		}
		
		return sbDataMap;
	}
	
	public static void getSbChgPercentileData(String rootPath) {
		// e.g rootPath -  Z:\Mubing\stock data\southbound flow strategy - db\southbound chg percentile - by stock
		try {
			SimpleDateFormat sdf = new SimpleDateFormat ("yyyy-MM-dd");
			
			File rootFile = new File(rootPath);
			File[] files = rootFile.listFiles();
			
			String line = "";
			for(File f : files) {
				String fileName = f.getName();
				String stockCode = fileName.substring(0, fileName.length()-4);
				
				if(!utils.Utils.isDouble(stockCode))
					continue;
				
				Map<Date,ArrayList<Double>> singleStockMap = SbChgPercentileDataMap.get(stockCode);
				if(singleStockMap == null) {
					singleStockMap = new HashMap<Date,ArrayList<Double>>();
				}
				
				BufferedReader bf = utils.Utils.readFile_returnBufferedReader(utils.Utils.addBackSlashToPath(rootPath) + fileName);
				line = "";
				int c = 0;
				while((line = bf.readLine()) != null) {
					if(c == 0) {
						c++;
						continue;
					}
					
					String[] lineArr = line.split(",");
					String dateS = lineArr[0];
					Date date = sdf.parse(dateS);
					
					String pctHistS = lineArr[1];
					Double pctHist = -1.0;
					if(utils.Utils.isDouble(pctHistS)) {
						pctHist = Double.parseDouble(pctHistS);
					}
					
					String pctHist250DS = lineArr[2];
					Double pctHist250D = -1.0;
					if(utils.Utils.isDouble(pctHist250DS)) {
						pctHist250D = Double.parseDouble(pctHist250DS);
					}
					
					String pctHist60DS = lineArr[3];
					Double pctHist60D = -1.0;
					if(utils.Utils.isDouble(pctHist60DS)) {
						pctHist60D = Double.parseDouble(pctHist60DS);
					}
					
					ArrayList<Double> data = new ArrayList<Double>();
					data.add(pctHist);
					data.add(pctHist250D);
					data.add(pctHist60D);
					
					singleStockMap.put(date, data);
				}
				SbChgPercentileDataMap.put(stockCode, singleStockMap);
				
			}
			
			
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * get all trading dates
	 * @param filePath
	 */
	public static ArrayList<Date> getAllTradingDate(String filePath) {
		ArrayList<Date> dateArr = new ArrayList<Date>();
		try {
			BufferedReader bf = utils.Utils.readFile_returnBufferedReader(filePath);
			ArrayList<String> dateStrArr = new ArrayList<String>();
			dateStrArr.addAll(Arrays.asList(bf.readLine().split(",")));
			 
			SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
			for(int i = 0; i < dateStrArr.size(); i++) {
				String dateStr = dateStrArr.get(i);
				Date date = sdf.parse(dateStr);
				dateArr.add(date);
			}
			
			Collections.sort(dateArr);   // 从旧到新排序
			
			bf.close();
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		return dateArr;
	}
	
	public static void getAllTradingDate() {
		allTradingDate = getAllTradingDate(allTradingDatePath);
	}
	
	/**
	 * get the data of outstanding shares
	 * @param outstandingFilePath
	 * @param cutOffDateStr
	 * @param dateFormat
	 */
	public static void getAllOsData_old(String outstandingFilePath,  Date cutOffDate) {
		try {
			if(allTradingDate == null || allTradingDate.size() == 0 )
				getAllTradingDate();
			
			SimpleDateFormat sdf = new SimpleDateFormat ("yyyy-MM-dd");
			//SimpleDateFormat sdf_temp = new SimpleDateFormat (dateFormat);
			//Date cutOffDate = sdf_temp.parse(cutOffDateStr);
			
	        File allFile = new File(outstandingFilePath);
	        for(File f : allFile.listFiles()) {
	        	String fileName = f.getName();
	        	if(fileName.substring(fileName.length() - 4, fileName.length()).equals(".csv")) {
	        		logger.info("file=" + fileName);
	        		String stock = fileName.substring(0, fileName.length() - 4);
	        		
	        		Map<Date,ArrayList<Double>> osData_oneStock = new HashMap<Date,ArrayList<Double>>();
	        		
	        		BufferedReader bf = utils.Utils.readFile_returnBufferedReader(outstandingFilePath + fileName);
	        		int count = 0;
	        		int indTradingDate = allTradingDate.size() - 1;   // 将会沿着allTradingDate从后往前进
	        		//boolean hasMetCutOffDate = false;
	        		String line = "";	
	        		while((line = bf.readLine()) != null) {
	        			if(count == 0) { // skip first line
	        				count++;
	        				continue;
	        			}
	        			
	        			String[] lineArr = line.split(",");
	        			String dataDateStr = lineArr[0];
	        			String osShareStr= lineArr[1];
	        			String osValueStr = lineArr[5];
	        			Date dataDate = sdf.parse(dataDateStr);
	        			Double osShare = Double.parseDouble(osShareStr);
	        			Double osValue = Double.parseDouble(osValueStr);
	        			
	        			Date currentTradingDate = allTradingDate.get(indTradingDate);
	        			while(!currentTradingDate.before(dataDate) && !currentTradingDate.before(cutOffDate)) {  //目前的考虑的trading date还在数据日期的后面
	        				ArrayList<Double> data = new ArrayList<Double>();
	        				data.add(osShare);
	        				data.add(osValue);
	        				osData_oneStock.put(currentTradingDate, data);
	        				indTradingDate -- ;
	        				currentTradingDate = allTradingDate.get(indTradingDate);
	        				
	        			}
	        			
	        			if(currentTradingDate.before(cutOffDate))
	        				break;
	        		}
	        		bf.close();
	        		
	        		osDataMap.put(stock, osData_oneStock);
	        	}
	        }// end of for
		}catch(Exception e) {
			e.printStackTrace();
		}
		
	}
	
	/**
	 * 将outstanding shares的数据存到osDataMap2中
	 * @param outstandingFilePath
	 * @param cutOffDate
	 */
	public static void getAllOsData(String outstandingFilePath,  Date cutOffDate) {
		try {
			if(allTradingDate == null || allTradingDate.size() == 0 )
				getAllTradingDate();
			
			SimpleDateFormat sdf = new SimpleDateFormat ("yyyy-MM-dd");
			//SimpleDateFormat sdf_temp = new SimpleDateFormat (dateFormat);
			//Date cutOffDate = sdf_temp.parse(cutOffDateStr);
			
	        File allFile = new File(outstandingFilePath);
	        for(File f : allFile.listFiles()) {
	        	String fileName = f.getName();
	        	if(fileName.substring(fileName.length() - 4, fileName.length()).equals(".csv")) {
	        		//logger.info("file=" + fileName);
	        		String stock = fileName.substring(0, fileName.length() - 4);
	        		
	        		ArrayList<Object> data = new ArrayList<Object>();
	        		ArrayList<Date> dateArr = new ArrayList<Date>();
	        		ArrayList<Double> shareArr = new ArrayList<Double>();
	        		ArrayList<Double> valueArr = new ArrayList<Double>();
	        		
	        		BufferedReader bf = utils.Utils.readFile_returnBufferedReader(outstandingFilePath + fileName);
	        		int count = 0;
	        		
	        		String line = "";	
	        		while((line = bf.readLine()) != null) {
	        			if(count == 0) { // skip first line
	        				count++;
	        				continue;
	        			}
	        			
	        			String[] lineArr = line.split(",");
	        			String dataDateStr = lineArr[0];
	        			String osShareStr= lineArr[1];
	        			String osValueStr = lineArr[5];
	        			
	        			Date dataDate = sdf.parse(dataDateStr);
	        			Double osShare = Double.parseDouble(osShareStr);
	        			Double osValue = Double.parseDouble(osValueStr);
	        			
	        			dateArr.add(dataDate);
	        			shareArr.add(osShare);
	        			valueArr.add(osValue);
	        			
	        			if(cutOffDate.after(dataDate))
	        				break;
	        			
	        		} // end of while
	        		bf.close();
	        		
	        		data.add(dateArr);
	        		data.add(shareArr);
	        		data.add(valueArr);
	        		
	        		osDataMap2.put(stock, data);
	        		
	        		data = null;
	        		dateArr = null;
	        		shareArr = null;
	        		valueArr = null;
	        	}
	        }
	        
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		logger.info("Get All Outstanding Data - Done!");
	}
	
	public static void getAllOsData(String outstandingFilePath,  String curOffDateStr, String dateFormat) {
		try {
			getAllOsData(outstandingFilePath, new SimpleDateFormat(dateFormat).parse(curOffDateStr));
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * 从osDataMap2获取某只股票某个日期的outstanding的数据
	 * @param stock
	 * @param date
	 * @return
	 */
	public static ArrayList<Double> getSingleStockOsData(String stock, Date date) {
		ArrayList<Double> data_return = new ArrayList<Double>();
		Double shares = 0.0;
		Double value = 0.0;
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd"); 
		try {
			ArrayList<Object> allData = osDataMap2.get(stock);
			if(allData == null || allData.size() == 0)
				return data_return;
			
			ArrayList<Date> dateArr = (ArrayList<Date>) allData.get(0);
    		ArrayList<Double> shareArr = (ArrayList<Double>) allData.get(1);
    		ArrayList<Double> valueArr = (ArrayList<Double>) allData.get(2);
    		
    		final int size = dateArr.size();
    		for(int i = 0; i < size; i++) {
    			Date thisDate = dateArr.get(i);
    			//logger.info("date=" + sdf.format(thisDate));
    			
    			if(!thisDate.after(date)) {  //thisDate在date之前
    				shares = shareArr.get(i);
    				value = valueArr.get(i);
    				break;
    			}
    		}
    		
    		if(shareArr.size() == 0) {
    			shares = 10000 * 10000 * 10000.0; //如果出现这种情况，就假设是10000亿的股本
    			value = 100000 * 10000 * 10000.0; //如果出现这种情况，就假设是100000亿的市值
    		}
    		if(shares == 0.0 || value == 0.0) {  //处理一些特殊情况，这个是说date要比数据的最早一个日期还早
    			shares = shareArr.get(shareArr.size() - 1);
    			value = valueArr.get(valueArr.size() - 1);
    		}
    		
    		data_return.add(shares);
    		data_return.add(value);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return data_return;
	}

/*
	public static void getAllPriceData(String priceFilePath,  Date cutOffDate) {
		try {
			if(allTradingDate == null || allTradingDate.size() == 0 )
				getAllTradingDate();
			
			SimpleDateFormat sdf = new SimpleDateFormat ("yyyy-MM-dd");
			//SimpleDateFormat sdf_temp = new SimpleDateFormat (dateFormat);
			//Date cutOffDate = sdf_temp.parse(cutOffDateStr);
			
	        File allFile = new File(priceFilePath);
	        for(File f : allFile.listFiles()) {
	        	String fileName = f.getName();
	        	if(fileName.substring(fileName.length() - 4, fileName.length()).equals(".csv")) {
	        		//logger.info("file=" + fileName);
	        		String stock = fileName.substring(0, fileName.length() - 4);
	        		Double isStockFile = 0.0;
	        		try {
	        			isStockFile = Double.parseDouble(stock);
	        		}catch(Exception e) {
	        			isStockFile = -1.0;
	        		}
	        		if(isStockFile < 0)
	        			continue;
	        		
	        		
	        		BufferedReader bf = utils.Utils.readFile_returnBufferedReader(priceFilePath + "\\" + fileName);
	        		int count = 0;
	        		
	        		Map<Date, ArrayList<Double>> thisStockMap = priceDataMap.get(stock);
	        		String line = "";	
	        		while((line = bf.readLine()) != null) {
	        			if(count == 0) { // skip first line
	        				count++;
	        				continue;
	        			}
	        			
	        			String[] lineArr = line.split(",");
	        			String dateStr = lineArr[0];
	        			String closeStr = lineArr[3];
	        			String adjCloseStr= lineArr[11];
	        			
	        			Date date = sdf.parse(dateStr);
	        			if(date.before(cutOffDate))
	        				continue;
	        			
	        			Double close = Double.parseDouble(closeStr);
	        			Double adjClose = Double.parseDouble(adjCloseStr);
	        			
	        			ArrayList<Double> closeArr = new ArrayList<Double>();
	        			closeArr.add(close);
	        			closeArr.add(adjClose);
	        			
	        			
	        			if(thisStockMap == null	) {
	        				thisStockMap = new HashMap<Date, ArrayList<Double>>(); 
	        			}
	        			thisStockMap.put(date, closeArr);
	        			
	        			
	        			
	        		} // end of while
	        		bf.close();
	        		
	        		priceDataMap.put(stock, thisStockMap);
	        		
	        	}
	        }
	        
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		logger.info("Get All Price Data - Done!");
	}
	
	public static void getPriceData(String stock, String priceFilePath,  Date cutOffDate) {
		try {
			if(allTradingDate == null || allTradingDate.size() == 0 )
				getAllTradingDate();
			
			SimpleDateFormat sdf = new SimpleDateFormat ("yyyy-MM-dd");
			//SimpleDateFormat sdf_temp = new SimpleDateFormat (dateFormat);
			//Date cutOffDate = sdf_temp.parse(cutOffDateStr);
			
    		BufferedReader bf = utils.Utils.readFile_returnBufferedReader(priceFilePath + "\\" + stock + ".csv");
    		int count = 0;
    		
    		Map<Date, ArrayList<Double>> thisStockMap = priceDataMap.get(stock);
    		String line = "";	
    		while((line = bf.readLine()) != null) {
    			if(count == 0) { // skip first line
    				count++;
    				continue;
    			}
    			
    			String[] lineArr = line.split(",");
    			String dateStr = lineArr[0];
    			String closeStr = lineArr[3];
    			String adjCloseStr= lineArr[11];
    			
    			Date date = sdf.parse(dateStr);
    			if(date.before(cutOffDate))
    				continue;
    			
    			Double close = Double.parseDouble(closeStr);
    			Double adjClose = Double.parseDouble(adjCloseStr);
    			
    			ArrayList<Double> closeArr = new ArrayList<Double>();
    			closeArr.add(close);
    			closeArr.add(adjClose);
    			
    			
    			if(thisStockMap == null	) {
    				thisStockMap = new HashMap<Date, ArrayList<Double>>(); 
    			}
    			thisStockMap.put(date, closeArr);
    			
    			
    			
    		} // end of while
    		bf.close();
    		
    		priceDataMap.put(stock, thisStockMap);
    		
    	
	        
	        
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		logger.info("Get All Price Data - Done!");
	}
	*/
	
	/**
	 * 得到每个日期的所有股票的notional chg
	 * @param rootFilePath
	 */
	public static void getNotionalChgData(String rootFilePath) {
		try {
			SimpleDateFormat sdf_yyyyMMdd = new SimpleDateFormat("yyyyMMdd");
			
			//得到合格的file list
			File f = new File(rootFilePath);
			ArrayList<String> fileNameList = new ArrayList<String>();
			for(File file : f.listFiles()) {
				String fileName = file.getName();
				String dateStr = fileName.substring(0, fileName.length() - 4);
				try {
					Date d = sdf_yyyyMMdd.parse(dateStr);
					fileNameList.add(fileName);
				}catch(Exception ee) {
					
				}
			}
			
			// read data
			for(String fileName : fileNameList) {
				String filePath = rootFilePath + fileName;
				String dateStr = fileName.substring(0, fileName.length() - 4);
				
				BufferedReader bf = utils.Utils.readFile_returnBufferedReader(filePath);
				int count = 0;
				String line = "";
				Map<String, Double> todayDataMap = new HashMap<String, Double>(); 
				while((line = bf.readLine()) != null) {
					if(count == 0) {
						count ++;
						continue;
					}
					
					String[] lineArr = line.split(",");
					String stock = lineArr[0];
					Double notionalChg = utils.Utils.safeParseDouble(lineArr[1],0.0);
					
					todayDataMap.put(stock, notionalChg);
				}
				bf.close();
				
				Date date = sdf_yyyyMMdd.parse(dateStr);
				notionalChgData.put(date, todayDataMap);
			}
			
		}catch(Exception e) {
			e.printStackTrace();
		}
		
	}


}
