package test_no_use;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class Test {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			SimpleDateFormat sdf = new SimpleDateFormat ("yyyyMMdd");
			Calendar c1 = Calendar.getInstance();
			c1.setTime(sdf.parse("20170807"));
			
			ArrayList<Calendar> allT = utils.Utils.getAllTradingDate("D:\\stock data\\all trading date - hk.csv");
			
			Calendar c2 = utils.Utils.getMostRecentDate(c1, allT);
			
			System.out.println(sdf.format(new Date()));
			
			System.out.println(webbDownload.outstanding.DataGetter.getStockDataField("680", 
					webbDownload.outstanding.DataGetter.OutstandingDataField.OUTSTANDING_SHARES, "20170401", "yyyyMMdd"));
		}catch(Exception e) {
			e.printStackTrace();
		}
		
	}

}
