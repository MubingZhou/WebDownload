package strategy.db_southboundFlowPortfolio;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.text.DecimalFormat;
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

public class PortfolioScreening {
	public static String outputPath = "";
	public static int topNStocks = 20;
	public static ArrayList<StockSingleDate> stockList = new ArrayList<StockSingleDate>();
	public static double avgDailyValueThreshHold_USD = 7000000.0;
	public static int oneMonthBeforeDays = 20; // 正常而言这应该是 ind - 20，表示考虑20天前的数据
	
	private static Logger logger = LogManager.getLogger(PortfolioScreening.class.getName());
	private static Map<String, String> ffPctMap = new HashMap();
	private static Map<String, Map<Date,ArrayList<Double>>> sbDataMap = new HashMap<String, Map<Date,ArrayList<Double>>>();

	/**
	 * 在某一天进行portfolio screening. 先不选出合适的股票，只是把每只股票的相关数据以及ranking全部 计算出来，然后排序
	 * @param date
	 * @param dateFormat
	 * @param isOutput
	 * @return
	 */
	public static ArrayList<StockSingleDate> assignValue_singleDate(String date, String dateFormat){
		logger.info("======= Stocks Screening - " + date + " ===============");
		ArrayList<StockSingleDate> stockList = new ArrayList<StockSingleDate>();
		//ArrayList<StockSingleDate> stockPickedList = new ArrayList<StockSingleDate>();
		ArrayList<String> stockListStr = new ArrayList<String> ();
		SimpleDateFormat sdf_yyyyMMdd = new SimpleDateFormat("yyyyMMdd"); 
	
		try {
			// ==== convert the date to the most recent trading date before it =========
			SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
			Calendar benchCal = Calendar.getInstance();
			benchCal.setTime(sdf.parse(date));
			ArrayList<Calendar> allTradingDate = utils.Utils.getAllTradingDate("D:\\stock data\\all trading date - hk.csv");
			benchCal = utils.Utils.getMostRecentDate(benchCal, allTradingDate);
			date = sdf.format(benchCal.getTime());
			Date todayDate = benchCal.getTime();
			
			int ind = allTradingDate.indexOf(benchCal);
			Calendar oneMonthBefore = allTradingDate.get(ind - oneMonthBeforeDays);  // 正常而言这应该是 ind - 20，表示考虑20天前的数据
			Calendar threeMonthBefore = allTradingDate.get(ind - 60);
			Date oneMonthBeforeDate = oneMonthBefore.getTime();
			
			// ========= get stock free float pct ========
			if(ffPctMap.isEmpty()){
				BufferedReader ff_reader = utils.Utils.readFile_returnBufferedReader("D:\\stock data\\freefloat pct - hk.csv");
				String ff_line = "";
				while((ff_line = ff_reader.readLine()) != null) {
					String[] ff_lineArr = ff_line.split(",");
					ffPctMap.put(ff_lineArr[0], ff_lineArr[1]);
				}
			}
			
			// ========= get southbound info ============
			// southbound的数据是每天的数据存一个文件，我们可以先读出今天的文件的内容
			Map<String, ArrayList<String>> sbToday_map = webbDownload.southboundData.DataGetter.getStockData_map(date, "yyyyMMdd");
			Map<String, ArrayList<String>> sbOneMonthBefore_map = webbDownload.southboundData.DataGetter.getStockData_map(oneMonthBefore);
			
			
			// ======== get avg vol info ==========
			// 经过处理后的average volume：每天一个file，存储所有股票前三个月的avg vol
			Map<String, Double> avgVol_map = new HashMap();
			Map<String, Double> avgTur_map = new HashMap();
			String avgVolPath = "D:\\stock data\\southbound flow strategy - db\\stock avg trd vol\\" + date + ".csv";
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
			
			// get southbound stock list at the specified date
			ArrayList<String> stockListStrArr = utils.Utils.getSouthboundStocks(date, dateFormat, true, true);
			for(int i = 0; i < stockListStrArr.size(); i++) {
				String stockCode = stockListStrArr.get(i);  // stock code in the form of "1","6881" etc...
				//System.out.println("======== stock = " + stockCode + " ============");
				
				StockSingleDate stock = new StockSingleDate(stockCode, date, dateFormat);
				
				// ========== get southbound flow information ==============
				//ArrayList<String> today_SBData = webbDownload.southboundData.DataGetter.getStockData(stockCode, date, dateFormat);
				ArrayList<Double> today_SBData = sbDataMap.get(stockCode).get(todayDate);
				//Double today_holding = 0.0;
				if(today_SBData != null && today_SBData.size() > 0) {
					stock.SB_today_holding = today_SBData.get(0);
					stock.SB_today_holdingValue = today_SBData.get(1);
				}else {
					logger.debug("[today SB data not exists] code=" + stockCode +  " " + date);
					stock.SB_today_holding = 0.0;
					stock.SB_today_holdingValue = 0.0;
				}
				
				ArrayList<Double> oneMontBeofore_SBData = sbDataMap.get(stockCode).get(oneMonthBeforeDate);
				if(oneMontBeofore_SBData != null && oneMontBeofore_SBData.size() > 0) {
					stock.SB_1MBefore_holding = oneMontBeofore_SBData.get(0);
					stock.SB_1MBefore_holdingValue = oneMontBeofore_SBData.get(1);
				}else {
					logger.debug("[1 month ago SB data not exists] code=" + stockCode +  " " + date);
					stock.SB_1MBefore_holding = 0.0;
					stock.SB_1MBefore_holdingValue = 0.0;
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
						*/
				String todayOsSharesStr =  "";
				String todayOsValueStr = "";
				ArrayList<String> todayOsLine = webbDownload.outstanding.DataGetter.getStockDataLine(stockCode, date, dateFormat);
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
				/*
				String oneMonthBefore_osShareStr = webbDownload.outstanding.DataGetter.getStockDataField(stockCode, 
						webbDownload.outstanding.DataGetter.OutstandingDataField.OUTSTANDING_SHARES, new SimpleDateFormat(dateFormat).format(oneMonthBefore.getTime()), dateFormat);
				String oneMonthBefore_osValueStr = webbDownload.outstanding.DataGetter.getStockDataField(stockCode, 
						webbDownload.outstanding.DataGetter.OutstandingDataField.MKT_CAP, new SimpleDateFormat(dateFormat).format(oneMonthBefore.getTime()), dateFormat);
				*/
				String oneMonthBefore_osShareStr = "";
				String oneMonthBefore_osValueStr = "";
				ArrayList<String> oneMonthBefore_osLine = webbDownload.outstanding.DataGetter.getStockDataLine(stockCode, 
						new SimpleDateFormat(dateFormat).format(oneMonthBefore.getTime()), dateFormat);
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
			} // end of for
			
			// =========== sorting stock list ============
			if(true) {
				Collections.sort(stockList, StockSingleDate.getComparator(1));  // first rank by SB_over_os_shares, then rank by SB_over_vol, then add the two ranks up and rank again
				for(int i = 0; i < stockList.size(); i++) {
					StockSingleDate stock = stockList.get(i);
					stock.dummy1 = (double) i;
					stock.sorting_indicator = stock.SB_over_vol;
					//stock.sorting_indicator = stock.SB_over_turnover;
					//stock.sorting_indicator = stock.db_SB_over_turnover; // DB's method
				}
				Collections.sort(stockList, StockSingleDate.getComparator(1));  
				for(int i = 0; i < stockList.size(); i++) {
					StockSingleDate stock = stockList.get(i);
					stock.dummy2 = (double) i;
					stock.sorting_indicator = stock.dummy1 + stock.dummy2;
					
					//stock.sorting_indicator = (stock.SB_today_holding - stock.SB_1MBefore_holding) / stock.osShares_today;
					//stock.sorting_indicator = stock.SB_over_os_shares;
				}
			}
			
			Collections.sort(stockList, StockSingleDate.getComparator(-1)); 			
				
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		logger.info("======= Stocks Screening END - " + date + " ===============");
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
			}
			
			// ============ apply filters ===========
			int stockCounter = 0;
			for(int i = 0; i < stockList.size(); i++) {
				StockSingleDate s = stockList.get(i);
				String stockCode = s.stockCode;
				
				// ========= test if suspended && volume is OK ==========
				if(s.filter1 > 0 && s.filter2 > 0 && s.filter3 > 0 && s.filter4 > 0) {
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
	
	public static void getAllSbData(String rootPath) {
		try {
			//Map<String, Map<Date,Double>> sbDataMap = new HashMap<String, Map<Date,Double>>();
			
			SimpleDateFormat sdf = new SimpleDateFormat ("yyyy-MM-dd");
            File allFile = new File(rootPath);
            for(File f : allFile.listFiles()) {
            	String fileName = f.getName();  // "2017-07-21.csv"
            	String filePath = rootPath + "\\" + fileName	;
            	String dateStr = fileName.substring(0, fileName.length() - 4); // "2017-07-21"   
            	Date date = sdf.parse(dateStr);
            	System.out.println(dateStr);
            	
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
            		Double valueD = Double.parseDouble(value);
            		
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
            } // end of file for
           
		}catch(Exception e) {
			e.printStackTrace();
		}
	}

}
