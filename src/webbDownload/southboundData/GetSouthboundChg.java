package webbDownload.southboundData;

import java.io.BufferedReader;
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

public class GetSouthboundChg {
	public static String ALL_TRADING_DATE_PATH = "Z:\\Mubing\\stock data\\all trading date - hk.csv";
	public static String SOUTHBOUND_DATA_PATH = "Z:\\Mubing\\stock data\\HK CCASS - WEBB SITE\\southbound\\combined\\";
	public static String SOUTHBOUND_DATA_DATEFORMAT = "yyyy-MM-dd";
	
	public static void main(String[] args) {
		try {
			String stockListPath = "D:\\stocklist.csv";
			String startDateStr = "20171127";
			String endDateStr = "20171205";
			String dateFormat = "yyyyMMdd";
			String outputFileName = "D:\\holding.csv";
			
			getSouthboundHolding(stockListPath, startDateStr, endDateStr, dateFormat, outputFileName);
			
		}catch(Exception e)	{
			e.printStackTrace();
		}
	}
	
	public static void getSouthboundHolding(String stockListPath, String startDateStr, String endDateStr, String dateFormat, String outputFileName) {
		try {
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
			
			Map<String, LinkedHashMap<Date, Double>> sbDataMap = new HashMap<String, LinkedHashMap<Date, Double>>();
			SimpleDateFormat sdf = new SimpleDateFormat(SOUTHBOUND_DATA_DATEFORMAT); 
			for(int i = startDateInd; i <= endDateInd; i++) {
				Date date = allTradingDate.get(i);
				String fileName = SOUTHBOUND_DATA_PATH + sdf.format(date) + ".csv";
				
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
					String holdingStr = lineArr[2];
					Double holding = Double.parseDouble(holdingStr);
					
					LinkedHashMap<Date, Double> thisStockData = sbDataMap.get(code);
					if(thisStockData == null)
						thisStockData = new LinkedHashMap<Date, Double>();
					
					thisStockData.put(date, holding);
					sbDataMap.put(code, thisStockData);
				}
			} // end of reading files
			
			
			FileWriter fw = new FileWriter(outputFileName);
			for(int i = startDateInd; i <= endDateInd; i++) {
				Date date = allTradingDate.get(i);
				fw.write("," + sdf.format(date));
			}
			fw.write("\n");
			for(int i = 0; i< stockList.size(); i++) {
				String code = stockList.get(i);
				fw.write(code);
				
				LinkedHashMap<Date, Double> thisStockData = sbDataMap.get(code);
				Set<Date> allDates = thisStockData.keySet();
				for(Date d : allDates) {
					fw.write("," + thisStockData.get(d));
				}
				fw.write("\n");
			}
			fw.close();
			
		}catch(Exception e)	{
			e.printStackTrace();
		}
	}
}
