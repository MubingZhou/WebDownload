package strategy.db_southboundFlowPortfolio;

import java.io.BufferedReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;

public class StockSingleDate {
	public Calendar cal = Calendar.getInstance();
	
	public Double SB_1M_trailingFlow = 0.0;
	public Double SB_today_holding = 0.0;
	public Double SB_today_holdingValue = 0.0;
	public Double SB_1MBefore_holding = 0.0;
	public Double SB_1MBefore_holdingValue = 0.0;
	
	public Double Vol_1M_avg = 0.0;
	public Double Vol_3M_avg = 0.0;
	public Double SB_over_vol = 0.0;
	
	public Double Turnover_3M_avg = 0.0;
	public Double SB_over_turnover = 0.0;
	
	public String stockCode;
	
	public Double osShares_today = 0.0;
	public Double osShares_1MBefore = 0.0;
	public Double osShares_freefloat_today = 0.0;
	public Double osShares_freefloat_1MBefore = 0.0;
	
	public Double osValue_today = 0.0;
	public Double osValue_1MBefore = 0.0;
	public Double osValue_freefloat_today = 0.0;
	public Double osValue_freefloat_1MBefore = 0.0;
	
	public Double SB_over_os_shares = 0.0;
	public Double SB_over_os_shares_freefloat = 0.0;
	public Double SB_over_os_value_freefloat  = 0.0;
	
	public Double sorting_indicator = 0.0;
	
	public boolean suspended = false;
	
	public Double dummy1 = 0.0;
	public Double dummy2 = 0.0;
	public Double dummy3 = 0.0;
	public Double dummy4 = 0.0;
	
	public Double db_SB_over_ff = 0.0;
	public Double db_SB_over_turnover = 0.0;
	
	public StockSingleDate(String stockCode, String date, String dateFormat) {
		this.stockCode = stockCode;
		try {
			this.cal.setTime(new SimpleDateFormat(dateFormat).parse(date));
			
			// ======= get all trading date =========
			if(allTradingDate.size() == 0)
				fetchAllTradingDate();
		} catch (ParseException e) {
			e.printStackTrace();
			System.out.println("[Initializing StockSingleDate failed] " + stockCode + " " + date + " " + dateFormat + ". Set date to be 20170101" );
			try {
				this.cal.setTime(new SimpleDateFormat("yyyyMMdd").parse("20170101"));
			} catch (ParseException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}
	
	// sort ascendingly
	public static java.util.Comparator<StockSingleDate> getComparator(int dir) {
		// dir = 1 - sort ascendingly
		// dir = -1 - sort descendingly
		return new  java.util.Comparator<StockSingleDate>() {
			public int compare(StockSingleDate arg0, StockSingleDate arg1) {
				return dir*arg0.sorting_indicator.compareTo(arg1.sorting_indicator);
			}
			
		};
	}
	
	public static ArrayList<Calendar> allTradingDate = new ArrayList<Calendar>();
	public static boolean fetchAllTradingDate() {
		boolean isOK = true;
		
		try {
			String filePath = "D:\\stock data\\all trading date - hk.csv";
			BufferedReader bf  = utils.Utils.readFile_returnBufferedReader(filePath);
			String line = bf.readLine();
			String[] lineArr = line.split(",");
			
			String dateFormat = "dd/MM/yyyy";
			SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
			
			for(int i = 0; i < lineArr.length; i++) {
				Calendar cal = Calendar.getInstance();
				cal.setTime(sdf.parse(lineArr[i]));
				allTradingDate.add(cal);
			}
			
			// sorting - ascending, i.e. older date at the front
			Collections.sort(allTradingDate);
			
		}catch(Exception e) {
			e.printStackTrace();
			isOK = false;
		}
		
		return isOK;
	}
	
}
