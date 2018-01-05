package webbDownload.southboundData;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

public class GetSouthboundChg {
	public static Logger logger = Logger.getLogger(GetSouthboundChg.class);
	public static String ALL_TRADING_DATE_PATH = "Z:\\Mubing\\stock data\\all trading date - hk.csv";
	public static String ALL_TRADING_DATE_PATH_ASHARE = "Z:\\Mubing\\stock data\\A share data\\all trading date a share.csv";
	public static String SOUTHBOUND_DATA_PATH = "Z:\\Mubing\\stock data\\HK CCASS - WEBB SITE\\southbound\\combined\\";
	public static String NORTHBOUND_DATA_PATH = "Z:\\Mubing\\stock data\\A share data\\northbound holding\\combined\\";
	public static String STOCK_DATA_ROOT_PATH = "Z:\\Mubing\\stock data\\stock hist data - webb\\";
	public static String SOUTHBOUND_DATA_DATEFORMAT = "yyyy-MM-dd";
	public static String NORTHBOUND_DATA_DATEFORMAT = "yyyy-MM-dd";
	
	public static void main(String[] args) {
		try {
			String stockListPath = "D:\\stocklist.csv";
			String startDateStr = "20171201";
			String endDateStr = "20180104";
			String dateFormat = "yyyyMMdd";
			String outputFileName = "D:\\notionalChg.csv";
				// "Z:\\Mubing\\stock data\\southbound flow strategy - db\\results\\notionalChg.csv"
			
			ALL_TRADING_DATE_PATH = ALL_TRADING_DATE_PATH_ASHARE;
			SOUTHBOUND_DATA_PATH = NORTHBOUND_DATA_PATH;
			SOUTHBOUND_DATA_DATEFORMAT = NORTHBOUND_DATA_DATEFORMAT;
			
			getSouthboundHolding(stockListPath, startDateStr, endDateStr, dateFormat,true, outputFileName);
			//getSouthboundNotionalChg(stockListPath, startDateStr, endDateStr, dateFormat,true, outputFileName);
			
		}catch(Exception e)	{
			e.printStackTrace();
		}
	}
	
	/**
	 * 
	 * @param stockListPath
	 * @param startDateStr
	 * @param endDateStr
	 * @param dateFormat
	 * @param outputFileName
	 * @return Map<String, LinkedHashMap<Date, Double>>; String - stock code, Date - date (smaller date in the front, e.g. 2017/1/1 is in the front of 2017/12/31), Double - southbound holding
	 */
	public static Map<String, LinkedHashMap<Date, Double>> getSouthboundHolding(String stockListPath, String startDateStr, String endDateStr, String dateFormat, boolean isOutput, String outputFileName) {
		Map<String, LinkedHashMap<Date, Double>> sbDataMap = new HashMap<String, LinkedHashMap<Date, Double>>();
		
		try {
			// get stock list and set date
			BufferedReader bf = utils.Utils.readFile_returnBufferedReader(stockListPath);
			String line = bf.readLine();
			ArrayList<String> stockList = new ArrayList<String>(Arrays.asList(line.split(",")));
			
			ArrayList<Calendar> allTradingCal = utils.Utils.getAllTradingDate(ALL_TRADING_DATE_PATH);
			ArrayList<Date> allTradingDate = new ArrayList<Date>();
			for(int i = 0; i < allTradingCal.size(); i++) {
				Calendar c = allTradingCal.get(i);
				allTradingDate.add(c.getTime());
			}
			
			SimpleDateFormat sdf_1 = new SimpleDateFormat(dateFormat); 
			Date startDate =sdf_1.parse(startDateStr);
			Date endDate =sdf_1.parse(endDateStr);
			startDate = utils.Utils.getMostRecentDate( startDate, allTradingDate);
			int startDateInd = allTradingDate.indexOf(startDate);
			endDate = utils.Utils.getMostRecentDate( endDate, allTradingDate);
			int endDateInd = allTradingDate.indexOf(endDate);
			
			
			SimpleDateFormat sdf = new SimpleDateFormat(SOUTHBOUND_DATA_DATEFORMAT); 
			ArrayList<Date> dataDate = new ArrayList<Date>();  // 真正有data的date
			for(int i = startDateInd; i <= endDateInd; i++) {
				Date date = allTradingDate.get(i);
				String fileName = SOUTHBOUND_DATA_PATH + sdf.format(date) + ".csv";
				
				File file = new File(fileName);
				if(!file.exists()) {
					continue;
				}
				dataDate.add(date);
				
				BufferedReader bf2 = utils.Utils.readFile_returnBufferedReader(fileName);
				line = "";
				int count = 0;
				while((line = bf2.readLine()) != null) {
					if(count == 0) {
						count++;
						continue;
					}
					String[]  lineArr =line.split(",");
					String code = lineArr[0];
					if(code.equals("071696")) {
						logger.info("071696  date=" + sdf.format(date));
						Thread.sleep(100000000);
					}
					//String holdingStr = lineArr[2];
					String holdingStr = lineArr[3];
					Double holding = Double.parseDouble(holdingStr);
					
					LinkedHashMap<Date, Double> thisStockData = sbDataMap.get(code);
					if(thisStockData == null)
						thisStockData = new LinkedHashMap<Date, Double>();
					
					thisStockData.put(date, holding);
					sbDataMap.put(code, thisStockData);
				}
			} // end of reading files
			
			// output sbDataMap
			if(isOutput) {
				FileWriter fw = new FileWriter(outputFileName);
				for(int i = 0; i < dataDate.size(); i++) {
					Date date = dataDate.get(i);
					fw.write("," + sdf.format(date));
				}
				fw.write("\n");
				for(int i = 0; i< stockList.size(); i++) {
					String code = stockList.get(i);
					fw.write(code); 
					logger.info("output stock=" + code);
					
					LinkedHashMap<Date, Double> thisStockData = sbDataMap.get(code);
					Set<Date> allDates = thisStockData.keySet();
					for(Date d : allDates) {
						fw.write("," + thisStockData.get(d));
					}
					fw.write("\n");
				}
				fw.close();
			}
			
			
		}catch(Exception e)	{
			e.printStackTrace();
		}
		return sbDataMap;
	}
	
	public static Map<String, LinkedHashMap<Date, Double>> getSouthboundNotionalChg(String stockListPath, String startDateStr, String endDateStr, String dateFormat, boolean isOutput, String outputFileName) {
		Map<String, LinkedHashMap<Date, Double>> sbNotionalChgDataMap = new HashMap<String, LinkedHashMap<Date, Double>>();
		Map<String, LinkedHashMap<Date, Double>> stockDataMap = new HashMap<String, LinkedHashMap<Date, Double>>();
		try {
			// get stock list and set date
			BufferedReader bf = utils.Utils.readFile_returnBufferedReader(stockListPath);
			String line = bf.readLine();
			ArrayList<String> stockList = new ArrayList<String>(Arrays.asList(line.split(",")));
			
			ArrayList<Calendar> allTradingCal = utils.Utils.getAllTradingDate(ALL_TRADING_DATE_PATH);
			ArrayList<Date> allTradingDate = new ArrayList<Date>();
			for(int i = 0; i < allTradingCal.size(); i++) {
				Calendar c = allTradingCal.get(i);
				allTradingDate.add(c.getTime());
			}
			
			SimpleDateFormat sdf_1 = new SimpleDateFormat(dateFormat); 
			Date startDate =sdf_1.parse(startDateStr);
			Date endDate =sdf_1.parse(endDateStr);
			startDate = utils.Utils.getMostRecentDate( startDate, allTradingDate);
			int startDateInd = allTradingDate.indexOf(startDate);
			String startDate_1dayBefore = sdf_1.format(allTradingDate.get(startDateInd - 1));
			endDate = utils.Utils.getMostRecentDate( endDate, allTradingDate);
			int endDateInd = allTradingDate.indexOf(endDate);
			
			
			// get southbound holding data
			Map<String, LinkedHashMap<Date, Double>> sbHoldingDataMap = getSouthboundHolding(stockListPath, startDate_1dayBefore, endDateStr, dateFormat, false, "");	
						
			// get stock price data
			SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd");
			for(String stock : stockList) {
				LinkedHashMap<Date, Double> thisStockData = stockDataMap.get(stock);
				if(thisStockData == null)
					thisStockData = new LinkedHashMap<Date, Double>();
				
				String stockDataPath = STOCK_DATA_ROOT_PATH + stock + ".csv";
				BufferedReader bfs = utils.Utils.readFile_returnBufferedReader(stockDataPath);
				line = "";
				int c = 0;
				while((line = bfs.readLine()) != null) {
					if(c == 0) {
						c++;
						continue;
					}
					String[] lineArr = line.split(",");
					String vwapStr = lineArr[10];
					String todayDateStr = lineArr[0];
					
					Date todayDate = sdf2.parse(todayDateStr);
					if(!todayDate.before(startDate) || !todayDate.after(endDate)) {
						Double vwap = Double.parseDouble(vwapStr);
						thisStockData.put(todayDate, vwap);
					}
					if(todayDate.before(startDate))
						break;
					
				}
				bfs.close();
				stockDataMap.put(stock, thisStockData);
			}
			
			
			// get notional chg data
			FileWriter fw = new FileWriter("D:\\test23333.csv");
			if(isOutput) {
				fw.close();
				fw = new FileWriter(outputFileName);
				for(int i = startDateInd; i <= endDateInd; i++) {
					fw.write("," + sdf_1.format(allTradingDate.get(i)));
				}
				fw.write("\n");
			}
			for(String stock : stockList) {
				LinkedHashMap<Date, Double> thisStockData = stockDataMap.get(stock);
				LinkedHashMap<Date, Double> thisStockHoldingData = sbHoldingDataMap.get(stock);
				LinkedHashMap<Date, Double> thisStockNotinoalChg = sbNotionalChgDataMap.get(stock);
				logger.info("Stock=" + stock);
				if(isOutput) {
					fw.write(stock);
				}
				if(thisStockNotinoalChg == null)
					thisStockNotinoalChg = new LinkedHashMap<Date, Double>();
				for(int i = startDateInd; i <= endDateInd; i++) {
					Date date = allTradingDate.get(i);
					logger.info("  date=" + sdf_1.format(date));
					Double thisStockVwap = thisStockData.get(date);
					Double thisStockHolding = thisStockHoldingData.get(date);
					if(thisStockHolding == null)
						thisStockHolding = 0.0;
					Double thisStockHolding_1dayBefore = thisStockHoldingData.get(allTradingDate.get(i-1));
					if(thisStockHolding_1dayBefore == null)
						thisStockHolding_1dayBefore = 0.0;
					
					Double notionalChg = null;
					if(thisStockVwap != null) {
						notionalChg = thisStockVwap 
								* (thisStockHolding 
										- thisStockHolding_1dayBefore);
						thisStockNotinoalChg.put(date, notionalChg);
					}
					if(sdf_1.format(date).equals("20171214"))
						logger.info("       thisStockHolding=" + thisStockHolding + " thisStockHolding_1dayBefore="+thisStockHolding_1dayBefore);
					
					if(isOutput) {
						if(notionalChg != null)
							fw.write("," + String.valueOf(notionalChg));
						else
							fw.write(",-");
					}
				}
				if(isOutput) {
					fw.write("\n");
				}
				sbNotionalChgDataMap.put(stock, thisStockNotinoalChg);
			}
			if(isOutput) {
				fw.close();
			}
			
			
		}catch(Exception e)	{
			e.printStackTrace();
		}
		
		return sbNotionalChgDataMap;
	
	}
}
