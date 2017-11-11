package strategy.db_southboundFlowPortfolio;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class BacktestFrame {
	public static String dateFormat = "";
	public static SimpleDateFormat sdf;
	public static SimpleDateFormat sdf2 = new SimpleDateFormat("yyyyMMdd HHmmss"); 
	public static Date startDate ;
	public static Date endDate ;
	
	public static String portFilePath = "D:\\stock data\\southbound flow strategy - db\\" 
			+ sdf2.format(new Date()) + " - idea3 - bactesting四 - 15stocks";    // 最终输出的root path
	String allSbDataPath = "D:\\stock data\\HK CCASS - WEBB SITE\\southbound\\combined";  // 
	
	// ---------------- factors --------------
	public static int rankingStrategy = 1;
	public static double avgDailyValueThreshHold_USD = 7000000.0;
	public static int topNStocks = 20;
	public static double minInflowPct = 0.7;   // factor 4  在两次调仓之间，至少有这个比例的日子的flow是流入的
	
	public static int weightingStrategy = 1;
		/*
		 * 1 - Equally weighted
		 * 2 - 按照排名分成四组，每组所有股票的加起来的weights分别是40%，30%，20%，10%
		 * 
		 */
	public static int rebalancingStrategy = 1;
	/*
	 * rebalancingStrategy
	 * 1 - monthly, rebal at month beginning
	 * 2 - monthly, rebal at month end
	 * 3 - bi-weekly
	 * 4 - weekly
	 * 5 - every 40 trading days
	 */
	
	public static void init() {
		try {
			if(dateFormat.equals(""))
				dateFormat = "yyyyMMdd";
			sdf = new SimpleDateFormat (dateFormat);
			
			startDate = sdf.parse("20160704");
			endDate = sdf.parse("20171027");
			
			File f = new File(portFilePath);
			f.mkdir();
			
			PortfolioScreening.avgDailyValueThreshHold_USD =  7000000.0;  // 每天的平均成交额需要超过这个数才能入选
			PortfolioScreening.topNStocks = 15;   // 每次选多少只股票进行买入卖出
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
