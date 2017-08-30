package webbDownload.southboundData;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

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
		String copy = stockCode;
		for(int i = 0; i < 4 - stockCode.length(); i++){
			copy = "0" + copy;
		}
		stockCode = copy;
		
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
	
	public enum SOUTHBOUND_FIELD{
		NAME,SHARES,VALUE
	}
}
