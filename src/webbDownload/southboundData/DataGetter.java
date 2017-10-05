package webbDownload.southboundData;

import java.io.BufferedReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class DataGetter extends myAbstract.DataFetch{

public static String SOUTHBOUND_DATAPATH = "D:\\stock data\\HK CCASS - WEBB SITE\\southbound\\combined";
	
	/**
	 * Return a line of data for specified stock at specified date
	 * @param stockCode
	 * @param date 
	 * @param dateFormat - format of "date"
	 * @return
	 * @throws Exception
	 */
	public static ArrayList<String> getStockData(String stockCode, String date, String dateFormat) throws Exception{
		// ====== deal with date =====
		date = utils.Utils.formatDate(date, dateFormat, "yyyy-MM-dd");
				
		// ======= deal with read path ===========
		if(!SOUTHBOUND_DATAPATH.substring(SOUTHBOUND_DATAPATH.length() - 1).equals("\\")){
			SOUTHBOUND_DATAPATH = SOUTHBOUND_DATAPATH + "\\";
		}
		String filePath = SOUTHBOUND_DATAPATH + date + ".csv";
		//System.out.println("SOUTHBOUND_DATAPATH = " + filePath);
		
		// ======= deal with stock code ==========
		if(false) {
			String copy = stockCode;
			for(int i = 0; i < 4 - stockCode.length(); i++){
				copy = "0" + copy;
			}
			stockCode = copy;
		}
		
		
		if(stockCode.length() > 4)
			stockCode = stockCode.substring(stockCode.length() - 4);
		
		String errMsgHead = "Southbound - get stock data:[stock=" + stockCode + "]";
		
		String separator = ",";
		int ind = 0;
		
		return dataFetch(filePath, stockCode, ind, separator, errMsgHead);
	}
	
	public static ArrayList<String> getStockData(String stockCode, Calendar cal) throws Exception{
		return getStockData(stockCode, new SimpleDateFormat("yyyy-MM-dd").format(cal.getTime()), "yyyy-MM-dd");
	}
	
	/**
	 * return the whole day's data in the form of Map<String, ArrayList<String>> where the 1st String is stock code
	 * @param date
	 * @param dateFormat
	 * @return
	 * @throws Exception
	 */
	public static Map<String, ArrayList<String>> getStockData_map(String date, String dateFormat) throws Exception{
		Map<String, ArrayList<String>> sbData = new HashMap();
		
		// ====== deal with date =====
		date = utils.Utils.formatDate(date, dateFormat, "yyyy-MM-dd");
				
		// ======= deal with read path ===========
		if(!SOUTHBOUND_DATAPATH.substring(SOUTHBOUND_DATAPATH.length() - 1).equals("\\")){
			SOUTHBOUND_DATAPATH = SOUTHBOUND_DATAPATH + "\\";
		}
		String filePath = SOUTHBOUND_DATAPATH + date + ".csv";
		
		BufferedReader bf = utils.Utils.readFile_returnBufferedReader(filePath);
		String line = "";
		int ind1 = 0;
		while((line = bf.readLine()) != null) {
			if(ind1 == 0) {
				ind1 ++;
				continue;
			}
			String[] dataArr0 = line.split(",");
			dataArr0[0] = formatStockCode(dataArr0 [0]);
			ArrayList<String> dataArr = new ArrayList<String>(Arrays.asList(dataArr0));
			sbData.put(dataArr.get(0), dataArr);
		} 
		
		return sbData;
	}
	
	public static Map<String, ArrayList<String>> getStockData_map(Calendar cal) throws Exception{
		return getStockData_map(new SimpleDateFormat("yyyy-MM-dd").format(cal.getTime()), "yyyy-MM-dd");
	}
	
	public enum SOUTHBOUND_FIELD{
		NAME,SHARES,VALUE
	}
	
	/**
	 * All stock stock should in the form of "290","1", etc. i.e. no zeros in the front. "0290" will be formatted into "290"
	 * @param code
	 * @return
	 */
	private static String formatStockCode(String code) {
		char[] c = code.toCharArray();
		int ind = 0;
		for(int i = 0; i < c.length; i++) {
			if(c[i] != '0') {
				ind = i;
				break;
			}
				
		}
		return code.substring(ind);
	}
}
