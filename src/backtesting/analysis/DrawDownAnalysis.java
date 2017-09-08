package backtesting.analysis;

import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import backtesting.portfolio.Portfolio;
import backtesting.portfolio.PortfolioOneDaySnapshot;
import backtesting.portfolio.Underlying;

public class DrawDownAnalysis {
	public static String analysisBetweenDates_outputPath = ""; // should include the file name, i.e. "D:\\test.csv"
	
	public static void pnlAnalysisBetweenDates(Portfolio p, String date1, String date2, String dateFormat) {
		try {
			System.out.println("========= Start Drawdown Analysis ===========");
			//analysisBetweenDates_outputPath = utils.Utils.checkPath(analysisBetweenDates_outputPath);
			String errMsgHead = "[DrawDownAnalysis - analysisBetweenDates] ";
			SimpleDateFormat sdf = new SimpleDateFormat(dateFormat); 
			
			// ======= some pre dealings =======
			Set<Calendar> allDaysSet = p.histSnap.keySet();
			ArrayList<Calendar> allDaysArr = new ArrayList<Calendar>(allDaysSet);
			//ArrayList<Calendar> allDaysArr = new ArrayList<Calendar>();
			Collections.sort(allDaysArr ); // ascending
			
			ArrayList<Calendar> allTradingDate = utils.Utils.getAllTradingDate("D:\\stock data\\all trading date - hk.csv");
			
			Calendar date1Cal = (Calendar) allDaysArr.get(0).clone();
			date1Cal .setTime(sdf.parse(date1));
			Calendar date1Cal_temp = utils.Utils.getMostRecentDate(date1Cal, allTradingDate);
			date1Cal.setTime(sdf.parse(sdf.format(date1Cal_temp.getTime())));  // seems that the calendar system extracted from the file is different than that of our system

			Calendar date2Cal = (Calendar) allDaysArr.get(0).clone();
			date2Cal .setTime(sdf.parse(date2));
			Calendar date2Cal_temp = utils.Utils.getMostRecentDate(date2Cal, allTradingDate);
			date2Cal.setTime(sdf.parse(sdf.format(date2Cal_temp.getTime())));
			
			int date1Ind = allDaysArr.indexOf(date1Cal);
			int date2Ind = allDaysArr.indexOf(date2Cal);
			
			if(date1Ind == -1 || date2Ind == -1) {
				System.out.println(errMsgHead + "No such period existed in the Portfolio!");
				//System.out.println(sdf.format(date1Cal.getTime()) + " " + sdf.format(date2Cal.getTime()));
				//System.out.println(allDaysArr.get(0).equals(date1Cal));
				System.out.println(date1Ind + " " + date2Ind);
				return;
			}
			
			FileWriter fw = new FileWriter(analysisBetweenDates_outputPath);
			System.out.println(analysisBetweenDates_outputPath);
			System.out.println(sdf.format(date1Cal.getTime()) + " " + sdf.format(date2Cal.getTime()));
			
			//======== overview ========
			PortfolioOneDaySnapshot startPortfolio = p.histSnap.get(date1Cal);
			PortfolioOneDaySnapshot endPortfolio = p.histSnap.get(date2Cal);
			Double startMV = startPortfolio.marketValue;
			Double endMV = endPortfolio.marketValue;
			
			fw.write("Start=," + date1 + ",End=," + date2 + "\n");
			fw.write("Start market value=," + String.valueOf(startMV) + ",End market value=," + String.valueOf(endMV) + "\n");
			fw.write("Return=," + String.valueOf((endMV - startMV)/startMV) + "\n");
			
			// ======== calculate the cumulative contribution for each stock =======
			fw.write("Stock,Period P&L,% of start portfolio,start stock value,start stock price (adj),end stock value,end stock price(adj)\n");
			Map<String, Underlying> startHoldings = startPortfolio.stockHeld;
			Map<String, Underlying> endHoldings = endPortfolio.stockHeld;
			
			// to remove duplicated elements
			Set<String> startHoldingsStock = startHoldings.keySet();
			Set<String> endHoldingsStock = endHoldings.keySet();
			HashSet<String>  allHoldingsStock = new HashSet<String>(startHoldingsStock);
			allHoldingsStock.addAll(endHoldingsStock);
			
			for(String stock : allHoldingsStock) {
				// var to be output
				Double periodPnL = 0.0;
				Double percent = 0.0;
				Double startStockValue = 0.0;
				Double startStockPrice = 0.0;
				Double endStockValue = 0.0;
				Double endStockPrice = 0.0;
				
				// var for internal use
				Double startStockPnL = 0.0;
				Double endStockPnL = 0.0;
				
				Underlying startStock = startHoldings.get(stock);
				if(startStock != null) {
					startStockPnL = startStock.realized_PnL + startStock.unrealized_PnL;
					
					Double startStockAmt = startStock.amount;
					String startStockPriceStr = stockPrice.DataGetter.getStockDataField(stock, 
							stockPrice.DataGetter.StockDataField.adjclose, sdf.format(startPortfolio.todayCal.getTime()), dateFormat);
					try {
						startStockPrice = Double.parseDouble(startStockPriceStr);
						startStockValue = startStockPrice * startStockAmt;
					}catch(Exception e) {
						
					}
				}
				
				Underlying endStock = endHoldings.get(stock);
				if(endStock != null) {
					endStockPnL = endStock.realized_PnL + endStock.unrealized_PnL;
					
					Double endStockAmt = endStock.amount;
					String endStockPriceStr = stockPrice.DataGetter.getStockDataField(stock, 
							stockPrice.DataGetter.StockDataField.adjclose, sdf.format(endPortfolio.todayCal.getTime()), dateFormat);
					try {
						endStockPrice = Double.parseDouble(endStockPriceStr);
						endStockValue = endStockPrice * endStockAmt;
					}catch(Exception e) {
						
					}
				}
				
				periodPnL = endStockPnL - startStockPnL;
				if(periodPnL == 0.0)
					continue;
				percent = periodPnL / startPortfolio.marketValue;
				
				
				
				// change to string
				String periodPnLStr = periodPnL == 0.0?"-":String.valueOf(periodPnL);
				String percentStr = percent == 0.0?"-":String.valueOf(percent);
				String startStockValueStr = startStockValue == 0.0?"-":String.valueOf(startStockValue);
				String startStockPriceStr = startStockPrice == 0.0?"-":String.valueOf(startStockPrice);
				String endStockValueStr = endStockValue == 0.0?"-":String.valueOf(endStockValue);
				String endStockPriceStr = endStockPrice == 0.0?"-":String.valueOf(endStockPrice);
				
				fw.write(stock + "," + periodPnLStr + "," + percentStr + ","
						+ startStockValueStr + "," + startStockPriceStr + "," 
						+ endStockValueStr + "," + endStockPriceStr + "," 
						+ "\n");
			} // end of for
			
			
			fw.close();
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		System.out.println("========= End Drawdown Analysis ===========");
	}
}
