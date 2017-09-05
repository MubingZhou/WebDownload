package backtesting.analysis;

import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Set;

import backtesting.backtesting.Portfolio;
import backtesting.backtesting.PortfolioOneDaySnapshot;

public class DrawDownAnalysis {
	public static String analysisBetweenDates_outputPath = ""; // should include the file name, i.e. "D:\\test.csv"
	
	public static void analysisBetweenDates(Portfolio p, String date1, String date2, String dateFormat) {
		try {
			//analysisBetweenDates_outputPath = utils.Utils.checkPath(analysisBetweenDates_outputPath);
			String errMsgHead = "[DrawDownAnalysis - analysisBetweenDates] ";
			
			// ======= some pre dealings =======
			Set<Calendar> allDaysSet = p.histSnap.keySet();
			ArrayList<Calendar> allDaysArr = new ArrayList<Calendar>(allDaysSet);
			Collections.sort(allDaysArr ); // ascending
			
			ArrayList<Calendar> allTradingDate = utils.Utils.getAllTradingDate("D:\\stock data\\all trading date - hk.csv");
			
			Calendar date1Cal = Calendar.getInstance();
			date1Cal .setTime(new SimpleDateFormat(dateFormat).parse(date1));
			date1Cal = utils.Utils.getMostRecentDate(date1Cal, allTradingDate);

			Calendar date2Cal = Calendar.getInstance();
			date2Cal .setTime(new SimpleDateFormat(dateFormat).parse(date2));
			date2Cal = utils.Utils.getMostRecentDate(date2Cal, allTradingDate);
			
			int date1Ind = allDaysArr.indexOf(date1Cal);
			int date2Ind = allDaysArr.indexOf(date2Cal);
			
			if(date1Ind == -1 || date2Ind == -1) {
				System.out.println(errMsgHead + "No such period existed in the Portfolio!");
				return;
			}
			
			FileWriter fw = new FileWriter(analysisBetweenDates_outputPath);
			
			//======== overview ========
			PortfolioOneDaySnapshot startPortfolio = p.histSnap.get(date1Cal);
			PortfolioOneDaySnapshot endPortfolio = p.histSnap.get(date2Cal);
			Double startMV = startPortfolio.marketValue;
			Double endMV = endPortfolio.marketValue;
			
			fw.write("Start=," + date1 + ",End=," + date2 + "\n");
			fw.write("Start market value=" + String.valueOf(startMV) + ",End market value=," + String.valueOf(endMV) + "\n");
			fw.write("Return=," + String.valueOf((endMV - startMV)/startMV) + "\n");
			
			// ======== calculate the cumulative contribution for each stock =======
			
			
			fw.close();
		}catch(Exception e) {
			e.printStackTrace();
		}
		
	}
}
