package strategy.db_southboundFlowPortfolio;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class PortfolioScreening {
	public static String outputPath = "";

	public static ArrayList<String> portfolioScreening_singleDate(String date, String dateFormat){
		System.out.println("======= Stocks Screening - " + date + " ===============");
		ArrayList<StockSingleDate> stockList = new ArrayList<StockSingleDate>();
		ArrayList<String> stockListStr = new ArrayList<String> ();
		
		try {
			// ==== convert the date to the most recent trading date before it =========
			SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
			Calendar benchCal = Calendar.getInstance();
			benchCal.setTime(sdf.parse(date));
			ArrayList<Calendar> allTradingDate = utils.Utils.getAllTradingDate("D:\\stock data\\all trading date - hk.csv");
			benchCal = utils.Utils.getMostRecentDate(benchCal, allTradingDate);
			date = sdf.format(benchCal.getTime());
			
			int ind = allTradingDate.indexOf(benchCal);
			Calendar oneMonthBefore = allTradingDate.get(ind - 20);
			Calendar threeMonthBefore = allTradingDate.get(ind - 60);
			
			// ========= get stock free float pct ========
			BufferedReader ff_reader = utils.Utils.readFile_returnBufferedReader("D:\\stock data\\freefloat pct - hk.csv");
			String ff_line = "";
			Map<String, String> ffPctMap = new HashMap();
			while((ff_line = ff_reader.readLine()) != null) {
				String[] ff_lineArr = ff_line.split(",");
				ffPctMap.put(ff_lineArr[0], ff_lineArr[1]);
			}
			
			// get southbound stock list at the specified date
			ArrayList<String> stockListStrArr = utils.Utils.getSouthboundStocks(date, dateFormat, true, true);
			for(int i = 0; i < stockListStrArr.size(); i++) {
				String stockCode = stockListStrArr.get(i);  // stock code in the form of "1","6881" etc...
				//System.out.println("======== stock = " + stockCode + " ============");
				
				StockSingleDate stock = new StockSingleDate(stockCode, date, dateFormat);
				
				// ========== get southbound flow information ==============
				ArrayList<String> today_SBData = webbDownload.southboundData.DataGetter.getStockData(stockCode, date, dateFormat);
				//Double today_holding = 0.0;
				if(today_SBData.size() > 0) {
					stock.SB_today_holding = Double.parseDouble(today_SBData.get(2));
					stock.SB_today_holdingValue = Double.parseDouble(today_SBData.get(3));
				}
				
				//ArrayList<Calendar> allTradingDate = stock.allTradingDate;
				ArrayList<String> oneMontBeofore_SBData = webbDownload.southboundData.DataGetter.getStockData(stockCode, oneMonthBefore);
				//Double oneMontBeofore_holding = 0.0;
				if(oneMontBeofore_SBData.size() > 0) {
					stock.SB_1MBefore_holding = Double.parseDouble(oneMontBeofore_SBData.get(2));
					stock.SB_1MBefore_holdingValue = Double.parseDouble(oneMontBeofore_SBData.get(3));
				}
				
				stock.SB_1M_trailingFlow = stock.SB_today_holding - stock.SB_1MBefore_holding;
				//System.out.println(new SimpleDateFormat("yyyyMMdd").format(oneMonthBefore.getTime()) + " Southbound trailing = " + stock.SB_1M_trailingFlow);
				
				// ======== get stock volume & turnover information ============
				Double cumVol = 0.0;
				Double avgVol = 0.0;
				Double cumTurnover = 0.0;
				Double avgTurnover = 0.0;
				int numValidData = 0;
				
				BufferedReader bf = utils.Utils.readFile_returnBufferedReader(stockPrice.DataGetter.STOCK_DATA_PATH + stockCode + ".csv");
				String dataLine = "";
				int rowC = 0;
				int dayCount = 0;
				int numDays = 60;
				while((dataLine = bf.readLine()) != null) {
					if(rowC > 0) {
						ArrayList<String> dataLineArr = new ArrayList<String>(Arrays.asList(dataLine.split(",")));
						String thisDateStr = dataLineArr.get(0);
						Calendar thisDate = utils.Utils.dateStr2Cal(thisDateStr, "yyyy-MM-dd");
						if(!thisDate.before(oneMonthBefore) && !thisDate.after(benchCal)) {
							// if thisDate is between benchCal & oneMonthBefore
							if(dayCount < numDays) {
								String volStr = dataLineArr.get(8);
								String turnoverStr = dataLineArr.get(9);
								
								numValidData++;
								try {
									Double todayVol = Double.parseDouble(volStr);
									if(todayVol.compareTo(0.1) < 0)
										throw new Exception();
									else
										cumVol = cumVol + todayVol;
									
									Double todayTurnover = Double.parseDouble(turnoverStr);
									if(todayTurnover.compareTo(0.1) < 0)
										throw new Exception();
									else
										cumTurnover = cumTurnover + todayTurnover;
								}catch(Exception e) {
									numValidData --;
								}
							}else
								break;
							
							dayCount++;
						}
					}
					rowC++;
				}
				bf.close();
				
				if(numValidData != 0) {
					avgVol = cumVol / numValidData;
					avgTurnover = cumTurnover / numValidData;
				}
				stock.Vol_1M_avg = avgVol;
				stock.Vol_3M_avg = avgVol;
				stock.Turnover_3M_avg = avgTurnover;
				//System.out.println("avg vol = " + stock.Vol_1M_avg);
				//Thread.sleep(1000 * 10000000);
				
				// ========== get outstanding shares / value ==========
				String todayOsSharesStr = webbDownload.outstanding.DataGetter.getStockDataField(stockCode, 
						webbDownload.outstanding.DataGetter.OutstandingDataField.OUTSTANDING_SHARES, date, dateFormat);
				String todayOsValueStr = webbDownload.outstanding.DataGetter.getStockDataField(stockCode, 
						webbDownload.outstanding.DataGetter.OutstandingDataField.MKT_CAP, date, dateFormat);
				//if(stockCode.equals("680"))
				//	System.out.println("680 today os shares" + todayOsSharesStr);
				//Double todayOsShares = 0.0;
				if(todayOsSharesStr  != null && !todayOsSharesStr.equals("")) {
					try {
						stock.osShares_today = Double.parseDouble(todayOsSharesStr);
					}catch(Exception e) {
						
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
						
					}
					
					try {
						stock.osValue_freefloat_today = stock.osValue_today * Double.parseDouble(ffPctMap.get(stockCode));
					}catch(Exception e) {
						stock.osValue_freefloat_today = stock.osValue_today * 1.0;
					}
				}
				
				String oneMonthBefore_osShareStr = webbDownload.outstanding.DataGetter.getStockDataField(stockCode, 
						webbDownload.outstanding.DataGetter.OutstandingDataField.OUTSTANDING_SHARES, new SimpleDateFormat(dateFormat).format(oneMonthBefore.getTime()), dateFormat);
				String oneMonthBefore_osValueStr = webbDownload.outstanding.DataGetter.getStockDataField(stockCode, 
						webbDownload.outstanding.DataGetter.OutstandingDataField.MKT_CAP, new SimpleDateFormat(dateFormat).format(oneMonthBefore.getTime()), dateFormat);
				if(oneMonthBefore_osShareStr != null && !oneMonthBefore_osShareStr.equals("")) {
					try {
						stock.osShares_1MBefore = Double.parseDouble(oneMonthBefore_osShareStr); 
					}catch(Exception e) {
						
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
						
					}
					try {
						stock.osValue_freefloat_1MBefore = stock.osValue_1MBefore * Double.parseDouble(ffPctMap.get(stockCode)); 
					}catch(Exception e) {
						stock.osValue_freefloat_1MBefore = stock.osValue_1MBefore * 1.0;
					}
				}
				
				// ========== set the ratio ==========
				// southbound flow change vs. daily turnover
				if(!stock.Turnover_3M_avg.equals(0.0))
					stock.SB_over_turnover = (stock.SB_today_holdingValue - stock.SB_1MBefore_holdingValue) / stock.Turnover_3M_avg;
				else
					stock.SB_over_turnover = 0.0;
				if(!stock.Vol_3M_avg.equals(0.0))
					stock.SB_over_vol = (stock.SB_today_holding - stock.SB_1MBefore_holding) / stock.Vol_3M_avg;
				else
					stock.SB_over_vol = 0.0;
				
				// southbound change (in shares)
				int totalOrFreeFloat = 0;
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
				if(!stock.Turnover_3M_avg.equals(0.0))
					stock.db_SB_over_turnover = (stock.SB_today_holdingValue - stock.SB_1MBefore_holdingValue) / 20 / stock.Turnover_3M_avg;
				
				// ========== calculate the indicator ==========
				//stock.sorting_indicator = stock.SB_over_os_shares;
				//stock.sorting_indicator = stock.SB_over_os_value_freefloat;
				stock.sorting_indicator  = stock.db_SB_over_ff; // DB's method
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
					//stock.sorting_indicator = stock.SB_over_vol;
					//stock.sorting_indicator = stock.SB_over_turnover;
					stock.sorting_indicator = stock.db_SB_over_turnover; // DB's method
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
			for(int i = 1000000; i < stockList.size(); i++) {
				StockSingleDate stock = stockList.get(i);
				stock.dummy1 = (double) i;
			}
			
			// pick out the eligible stocks
			int stockCounter = 0;
			for(int i = 0; i < stockList.size(); i++) {
				StockSingleDate s = stockList.get(i);
				String stockCode = s.stockCode;
				
				// ========= test if suspended && volume is OK ==========
				String isSuspended = stockPrice.DataGetter.getStockDataField(stockCode, stockPrice.DataGetter.StockDataField.suspend, date, dateFormat);
				try {
					if(isSuspended.equals("1") ) {
						s.suspended = true;
					}
				}catch(Exception e) {
					
				}
				
				if(!s.suspended && s.Turnover_3M_avg > (7500000 * 7.8)) {
					stockListStr.add(s.stockCode);
					stockCounter ++;
				}
				if(stockCounter >= 20)
					break;
				
			}
			
			// ========== display =========
			if(true) {
				FileWriter fw = new FileWriter(outputPath + "\\screening " + date + ".csv");
				//fw.write("stock,1M avg vol,rank1,SB today holding,SB 1M before holding,os share today,os shares 1M before,SB today/os today,SB 1M/os 1M,diff,rank2,rank1+2\n");
				fw.write("stock,mkt cap (US mm),3M ADV (US mm),Score,"
						+ "H-share discount,SB Holding (US mm),SB Holding to FF,"
						+ "1M Change in SB holding (US mm),1M Change in SB holding/FF,1m SB Flow to Turnover,"
						+ "suspend,turnover > 7.5m US?\n");
				
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
						ffValue = s.osValue_today* Double.parseDouble(ffPctMap.get(s.stockCode));
						ffShare = s.osShares_today* Double.parseDouble(ffPctMap.get(s.stockCode));
						ffValue_1MBefore = s.osValue_1MBefore * Double.parseDouble(ffPctMap.get(s.stockCode));
						ffShare_1MBefore =s.osShares_1MBefore * Double.parseDouble(ffPctMap.get(s.stockCode));
					}catch(Exception e) {
						
					}
					
					Double SB_over_ff_today_shares = s.SB_today_holding / ffShare;
					Double SB_over_ff_1MBefore_shares = s.SB_1MBefore_holding / ffShare_1MBefore;
					Double SB_over_ff_today_value = s.SB_today_holdingValue / ffValue;
					Double SB_over_ff_1MBefore_value = s.SB_1MBefore_holdingValue / ffValue_1MBefore;
	
					Double SB_change_share = s.SB_today_holding - s.SB_1MBefore_holding;
					Double SB_change_value = s.SB_today_holdingValue - s.SB_1MBefore_holdingValue; 
					
					Double isVolLarge = s.Turnover_3M_avg / 7.8 / 1000000 > 7.5?1.0:0.0;
							
					fw.write(s.stockCode + "," + String.valueOf(s.osValue_today / 7.8 / 1000000) + "," + String.valueOf(s.Turnover_3M_avg / 7.8 / 1000000) + "," + String.valueOf(s.dummy1 + s.dummy2) + ","
							+ "" + "," + String.valueOf(s.SB_today_holdingValue / 7.8 / 1000000) + "," + String.valueOf(s.SB_today_holdingValue / ffValue) + "," 
							+ String.valueOf((s.SB_today_holdingValue - s.SB_1MBefore_holdingValue) / 7.8 / 1000000) + "," + String.valueOf((s.SB_today_holdingValue - s.SB_1MBefore_holdingValue) / ffValue) + "," + String.valueOf((s.SB_today_holdingValue - s.SB_1MBefore_holdingValue) / s.Turnover_3M_avg / 20.0) + ","
							+ String.valueOf(s.suspended) + "," + String.valueOf(isVolLarge)
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
		
		System.out.println("======= Stocks Screening END - " + date + " ===============");
		return stockListStr;
	}
}