package webbDownload.outstanding;

import java.io.BufferedReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;

import stockPrice.DataGetter.StockDataField;

public class DataGetter extends myAbstract.DataFetch{
	public static String OUTSTANDING_DATAPATH = "D:\\stock data\\HK CCASS - WEBB SITE\\outstanding\\";
	
	public static ArrayList<String> getStockDataLine(String stockCode, String date, String dateFormat) throws Exception{
		// ====== deal with date =====
		date = utils.Utils.formatDate(date, dateFormat, "yyyy-MM-dd");
				
		// ======= deal with read path ===========
		if(!OUTSTANDING_DATAPATH.substring(OUTSTANDING_DATAPATH.length() - 1).equals("\\")){
			OUTSTANDING_DATAPATH = OUTSTANDING_DATAPATH + "\\";
		}
		
		// ======== deal with stock code =========
		stockCode = removeFrontZero(stockCode);  // stock code should in the form 6881,700,1...
		
		String filePath = OUTSTANDING_DATAPATH + stockCode + ".csv";
		//System.out.println("OUTSTANDING_DATAPATH = " + filePath);
		
		
		String errMsgHead = "Outstanding - get stock data:[stock=" + stockCode + " date=" + date + "]";
		
		String separator = ",";
		int ind = 0;
		
		ArrayList<String> dataLine = new ArrayList<String>();
		// ========== read data and fetch data ==========
		BufferedReader bf = utils.Utils.readFile_returnBufferedReader( filePath );
		if(bf==null){
			System.out.println(errMsgHead + "no such file.");
		}else{
			String line = "";
			int counter = 0;
			String[] prevDataLine= {};
			while((line = bf.readLine()) != null){
				String[] lineArr = line.split(separator);
				String thisDateStr = lineArr[0];  
				//System.out.println("thisIdentifier = " + thisIdentifier);
				
				if(counter == 0) { // the 1st line is the header 
					
				}else { 
					Calendar thisCal = Calendar.getInstance();
					thisCal.setTime(new SimpleDateFormat("yyyy-MM-dd").parse(thisDateStr));
					
					Calendar benchCal = Calendar.getInstance();
					benchCal.setTime(new SimpleDateFormat("yyyy-MM-dd").parse(date));
				
					if(counter == 1){ // the 1st data line
						if(!benchCal.before(thisCal))
							dataLine.addAll(Arrays.asList(lineArr));
					
					}else {
						String prevDateStr = prevDataLine[0];
						Calendar prevCal = Calendar.getInstance();
						prevCal.setTime(new SimpleDateFormat("yyyy-MM-dd").parse(prevDateStr));
						
						if(!benchCal.before(thisCal) && benchCal.before(prevCal)) {
							dataLine.addAll(Arrays.asList(lineArr));
						}
					}
					prevDataLine = lineArr;
				}
				counter++;
			}// end of while
		} 
		bf.close();
		
		if(dataLine == null || dataLine.size() == 0){
			//System.out.println("Southbound - get stock data:no data for " + stockCode + " at " + date);
			System.out.println(errMsgHead + "no data.");
		}
			
		return dataLine;
		
	}
	
	public static String getStockDataField(String stockCode, OutstandingDataField field, String date, String dateFormat) throws Exception{
		ArrayList<String> stockDataLine = getStockDataLine(stockCode, date, dateFormat);
		
		// get field
		int ind = -1;
		switch(field) {
		case DATE:
			ind = 0;
			break;
		case OUTSTANDING_SHARES:
			ind = 1;
			break;
		case CHANGE:
			ind = 2;
			break;
		case PRICE:
			ind = 3;
			break;
		case PRICE_DATE:
			ind = 4;
			break;
		case MKT_CAP:
			ind = 5;
			break;
		case PENDING_SHARES:
			ind = 6;
			break;
		case PENDING_MKT_CAP:
			ind = 7;
			break;
		}
		
		if(stockDataLine == null || stockDataLine.size() == 0) {
			return "";
		}else
			return stockDataLine.get(ind);
	}
	public enum OutstandingDataField{
		DATE,OUTSTANDING_SHARES,CHANGE,PRICE,PRICE_DATE,MKT_CAP,PENDING_SHARES,PENDING_MKT_CAP;
		
	}
	
	private static String removeFrontZero(String s) {
		String newS = "";
		
		boolean isStarted = false;
		for(int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if(c != '0' || isStarted) {
				newS = newS + String.valueOf(c);
				isStarted = true;
			}
		}
		
		int ind = -1;
		for(int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if(c != '0' ) {
				ind = i;
				break;
			}
		}
		return s.substring(ind);
	}
}
