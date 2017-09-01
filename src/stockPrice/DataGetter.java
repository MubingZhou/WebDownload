package stockPrice;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class DataGetter extends myAbstract.DataFetch {
	public static String STOCK_DATA_PATH = "D:\\stock data\\stock hist data - webb\\";
	
	public static ArrayList<String> getStockDataLine(String stockCode, String date, String dateFormat) throws Exception{
		// ======= deal with read path ===========
		if(!STOCK_DATA_PATH.substring(STOCK_DATA_PATH.length() - 1).equals("\\")){
			STOCK_DATA_PATH = STOCK_DATA_PATH + "\\";
		}
		String filePath = STOCK_DATA_PATH + stockCode + ".csv";
		
		date = new SimpleDateFormat("yyyy-MM-dd").format(new SimpleDateFormat(dateFormat).parse(date));  // change date to yyyy-MM-dd
		
		String errMsgHead = "[Get stock data]" + stockCode + " " + date + " - ";
		return dataFetch(filePath, date, 0, ",", errMsgHead); 
	}
	
	public static String getStockDataField(String stockCode, StockDataField field, String date, String dateFormat) throws Exception{
		ArrayList<String> stockDataLine = getStockDataLine(stockCode, date, dateFormat);
		
		// get field
		int ind = -1;
		switch(field) {
		case high:
			ind = 7;
			break;
		case low:
			ind = 6;
			break;
		case close:
			ind = 3;
			break;
		case volume:
			ind = 8;
			break;
		case turnover:
			ind = 9;
			break;
		case vwap:
			ind = 10;
			break;
		case adjclose:
			ind = 11;
			break;
		case suspend:
			ind = 2;
			break;
			
		}
		
		if(stockDataLine == null || stockDataLine.size() == 0) {
			return "";
		}else
			return stockDataLine.get(ind);
	}
	
	public static ArrayList<String> getStockDataField(ArrayList<String> stockCodeArr, StockDataField field, String date, String dateFormat) throws Exception{
		ArrayList<String> toReturn = new ArrayList<String>();
		
		for(int i = 0; i < stockCodeArr.size(); i++) {
			String stockCode = stockCodeArr.get(i);
			toReturn.add(getStockDataField(stockCode, field, date, dateFormat));
		}
		
		return toReturn;
	}
	
	public enum StockDataField{
		high,low,close,volume,turnover,vwap,adjclose,suspend;
		// volume - # of shares traded
		// turnover - value of shares traded
		
	}
}
