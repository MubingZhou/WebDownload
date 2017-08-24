package sfcData;

import java.io.BufferedReader;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

public class ShortPosition_DataGetter extends myAbstract.DataFetch{
	public static String SI_DATAPATH = "D:\\stock data\\SFC Short Interest\\";
	
	/**
	 * return the data line for a stock at specified date
	 * @param stockCode
	 * @param date
	 * @param dateFormat, "date"'s format
	 * @return
	 * @throws Exception
	 */
	public static ArrayList<String> getStockData(String stockCode, String date, String dateFormat) throws Exception{
		String errMsgHead = "SFC short interest[" + stockCode + "-" + date + "]:";
		
		// ====== deal with date =====
		date = utils.Utils.formatDate(date, dateFormat, "yyyyMMdd");
				
		// ======= deal with read path ===========
		if(!SI_DATAPATH.substring(SI_DATAPATH.length() - 1).equals("\\")){
			SI_DATAPATH = SI_DATAPATH + "\\";
		}
		String filePath = SI_DATAPATH + date + ".csv";
		//System.out.println("sfc si filepath = " + filePath);
		
		// ======= deal with stock code ========== (e.g. stock code doesn't contain zero in the front
		String sc2 = stockCode;
		for(int i = 0; i < stockCode.length(); i++){
			if(stockCode.substring(i, i+1).equals("0"))
				sc2 = sc2.substring(1);
			else
				break;
		}
		stockCode = sc2;
		
		String separator = ",";
		int ind = 1;
		
		return dataFetch(filePath, stockCode, ind, separator, errMsgHead);
	}
	
	/*
	public static ArrayList<String> dataGetter(String dataPath, String date, String stockCode, ArrayList<SFC_SHORTINTEREST_FIELD> fields) throws Exception{
		// dataPath = "D:\\stock data\\SFC Short Interest"
		String errMsgHead = "SFC_ShortPositionReport_DataGetter - dataGetter - ";
		
		//
		ArrayList<String> toReturn = new ArrayList<String>();
		boolean isGetData = false;
		
		//====== get data ==========
		if(!dataPath.substring(dataPath.length()-2).equals("\\"))
			dataPath = dataPath + "\\";
		
		File file = new File(dataPath + date + ".csv");
		if(!file.exists()) {
			System.out.println(errMsgHead + "File doesn't exist: file = " + dataPath + date + ".csv");
			return null;
		}
		
		BufferedReader bw = utils.Utils.readFile_returnBufferedReader(dataPath + date + ".csv");
		String line = "";
		int counter = 0;
		while((line = bw.readLine()) != null) {
			if(counter == 0) {
				counter ++;
				continue;
			}
			
			ArrayList<String> dataArr = new ArrayList<String>()	;
			dataArr.addAll(Arrays.asList(line.split(",")));
			
			String thisCode = dataArr.get(1);
			if(thisCode.equals(stockCode)) { // find the stock
				for(int i = 0; i < fields.size(); i++) {
					switch(fields.get(i)) {
					case NAME:
						toReturn.add(dataArr.get(2));
						break;
					case SHARES:
						toReturn.add(dataArr.get(3));
						break;
					case VALUE:
						toReturn.add(dataArr.get(4));
						break;
					default:
						break;
					}
					isGetData = true;
				}
				break;
			}
			counter++;
		}// end of while..
		
		if(!isGetData) {
			System.out.println(errMsgHead + "Getting data failed");
			return null;
		}else {
			return toReturn;
		}
		
	}
	
	public enum SFC_SHORTINTEREST_FIELD{
		NAME,SHARES,VALUE
	}
	*/
}
