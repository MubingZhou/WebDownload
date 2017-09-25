package backtesting.analysis;

import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import backtesting.portfolio.Portfolio;
import backtesting.portfolio.PortfolioOneDaySnapshot;
import backtesting.portfolio.Underlying;
import math.MyMath;

public class DrawDownAnalysis {
	public static String analysisBetweenDates_outputPath = ""; // should include the file name, i.e. "D:\\test.csv"
	
	
	private static Logger logger = Logger.getLogger(DrawDownAnalysis.class.getName());
	
	/**
	 * calculate the pnl contribution for each stock within the time period. The output file is stored in DrawDownAnalysis.analysisBetweenDates_outputPath
	 * @param p
	 * @param date1
	 * @param date2
	 * @param dateFormat
	 */
	public static void pnlAnalysisBetweenDates(Portfolio p, String date1, String date2, String dateFormat) {
		try {
			logger.info("========= Start PnL Drawdown Analysis ===========");
			//analysisBetweenDates_outputPath = utils.Utils.checkPath(analysisBetweenDates_outputPath);
			String errMsgHead = "[DrawDownAnalysis - pnl Analysis Between Dates] ";
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
				logger.error(errMsgHead + "No such period existed in the Portfolio!");
				//System.out.println(sdf.format(date1Cal.getTime()) + " " + sdf.format(date2Cal.getTime()));
				//System.out.println(allDaysArr.get(0).equals(date1Cal));
				//logger.error(date1Ind + " " + date2Ind);
				return;
			}
			
			FileWriter fw = new FileWriter(analysisBetweenDates_outputPath);
			logger.info(errMsgHead + " output path=" + analysisBetweenDates_outputPath);
			logger.info(sdf.format(date1Cal.getTime()) + " " + sdf.format(date2Cal.getTime()));
			
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
		
		logger.info("========= End PnL Drawdown Analysis ===========");
	}

	/**
	 * To calculate the maximum drawdown
	 * @param p
	 * @param date1
	 * @param date2
	 * @param dateFormat
	 * @return ArrayList<Object>: 0th: max DD (Double); 1st: max DD start date (Calendar); 2nd: max DD end Date (Calendar); 3rd: max DD start portfolio value (Double); 4th: max DD end portfolio value (Double)
	 */
	public static ArrayList<Object> maxDrawdown(Portfolio p, String date1, String date2, String dateFormat){
		ArrayList<Object> maxDD = new ArrayList<Object>();
		
		try {
			logger.info("========= Start Caculating Max Drawdown ===========");
			//analysisBetweenDates_outputPath = utils.Utils.checkPath(analysisBetweenDates_outputPath);
			String errMsgHead = "[DrawDownAnalysis - calculating max DD] ";
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
				logger.error(errMsgHead + "No such period existed in the Portfolio!");
				//System.out.println(sdf.format(date1Cal.getTime()) + " " + sdf.format(date2Cal.getTime()));
				//System.out.println(allDaysArr.get(0).equals(date1Cal));
				//logger.error(date1Ind + " " + date2Ind);
				return null;
			}
			
			// ========= get the market value ===========
			ArrayList<Double> marketValue = new ArrayList<Double> ();
			for(int i = date1Ind; i < date2Ind; i++) {
				PortfolioOneDaySnapshot todayP = p.histSnap.get(allDaysArr.get(i));
				Double todayMarketValue = todayP.marketValue;
				marketValue.add(todayMarketValue);
			}
			
			// ========== calculate DD ===========
			ArrayList<Double> maxDD_temp = maxDrawdown(marketValue, errMsgHead);
			if(maxDD_temp == null || maxDD_temp.size() == 0) {
				logger.error(errMsgHead + " maxDD null!");
				return null;
			}
			maxDD.add(maxDD_temp.get(0));
			maxDD.add(allDaysArr.get(maxDD_temp.get(1).intValue() + date1Ind));
			maxDD.add(allDaysArr.get(maxDD_temp.get(2).intValue() + date1Ind));
			maxDD.add(maxDD_temp.get(3));
			maxDD.add(maxDD_temp.get(4));
			
			logger.debug("Finally, maxDD=" + maxDD.get(0) + " start=" + sdf.format(((Calendar) maxDD.get(1)).getTime())
					+ " end=" + sdf.format(((Calendar) maxDD.get(2)).getTime()) + " startV=" + maxDD.get(3) + " endV=" + maxDD.get(4)); 
			logger.info("========= End Caculating Max Drawdown ===========");
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		return maxDD;
	}
	
	private static ArrayList<Double> maxDrawdown(ArrayList<Double> portfolioValue, String errMsgHead){
		if(portfolioValue == null || portfolioValue.size() == 0) {
			logger.error(errMsgHead + " portfolioValue has no values!");
			return null;
		}
		
		ArrayList<Double> maxDDArr = new ArrayList<Double>();
		maxDDArr.add(-1.0);
		maxDDArr.add(-1.0);
		maxDDArr.add(-1.0);
		maxDDArr.add(-1.0);
		maxDDArr.add(-1.0);
		
		if(portfolioValue.size() == 1	) {
			maxDDArr.set(0, 0.0);
			maxDDArr.set(1, 0.0);
			maxDDArr.set(2, 0.0);
			maxDDArr.set(3,portfolioValue.get(0));
			maxDDArr.set(4,portfolioValue.get(0));
			
			logger.info(errMsgHead + " portfolioValue has only one value!");
			return maxDDArr;
		}
		
		Double maxDD = 0.0;
		int maxDDStartInd = 0;
		int maxDDEndInd = 0;
		try {
			Double maxPortfolioValue = portfolioValue.get(0);
			int lastMaxPValueInd = 0;
			for(int i = 1; i < portfolioValue.size(); i++) {
				Double thisPValue = portfolioValue.get(i);
				logger.trace("i = " + i + " thisPValue = " + thisPValue + " lastMaxPValueInd=" + lastMaxPValueInd + " maxDDStartInd=" + maxDDStartInd + " maxDDEndInd=" + maxDDEndInd);
				
				if(thisPValue > maxPortfolioValue ) {
					maxPortfolioValue = thisPValue;
					lastMaxPValueInd = i;
				}else {
					Double thisDD = (thisPValue - maxPortfolioValue) / maxPortfolioValue;
					if(thisDD < maxDD) {  // current drawdown exceeds historical high; note that maxDD is a negative number
						maxDD = thisDD;
						maxDDEndInd = i;
						maxDDStartInd = lastMaxPValueInd;
					}
					
				}
				
				logger.trace("\t" + " lastMaxPValueInd=" + lastMaxPValueInd + " maxDDStartInd=" + maxDDStartInd + " maxDDEndInd=" + maxDDEndInd);
			}
			
			logger.trace("maxDD=" + maxDD + " lastMaxPValueInd=" + lastMaxPValueInd + " maxDDStartInd=" + maxDDStartInd + " maxDDEndInd=" + maxDDEndInd);
		
			maxDDArr.set(0, maxDD);
			maxDDArr.set(1, (double) maxDDStartInd); 
			maxDDArr.set(2, (double) maxDDEndInd); 
			maxDDArr.set(3, portfolioValue.get(maxDDStartInd));
			maxDDArr.set(4, portfolioValue.get(maxDDEndInd));
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		return maxDDArr;
	}
	
	/**
	 * to calculate the max DD given a series of portfolio value.
	 * @param ArrayList<Double> portfolioValue
	 * @return returns ArrayList<Double>. 0th value: max DD value (Double); 1st value: max DD period starts (Integer); 2nd value: max DD period ends (Integer);
	 */
	public static ArrayList<Double> maxDrawdown(ArrayList<Double> portfolioValue){
		return maxDrawdown(portfolioValue, "[Calculate Max DD]");
	}

	/**
	 * get the Sharpe Ratio. rf  - risk free rate
	 * @param portfolioValue
	 * @param rf
	 * @param errMsgHead
	 * @return
	 */
	private static Double sharpeRatio(ArrayList<Double> portfolioValue, Double rf, String errMsgHead) {
		Double s = -100.0;
		
		if(portfolioValue == null || portfolioValue.size() == 0) {
			logger.error(errMsgHead + " portfolioValue has no values!");
			return null;
		}
		
		// get the average excessive return 
		ArrayList<Double> er = new ArrayList<Double>(); 
		Double avgEr = 0.0;  // avg excessive return
		Double cumEr = 0.0; // cumulative excessive return
		for(int i = 1; i < portfolioValue.size(); i++) {
			Double todayValue = portfolioValue.get(i);
			Double lastValue = portfolioValue.get(i - 1);
			
			Double todayEr = todayValue / lastValue  - 1 - rf;
			er.add(todayEr);
			cumEr = cumEr + todayEr	;
			
			
		}
		avgEr = cumEr / (portfolioValue.size() - 1);
		
		// calculate std
		Double cumSQ = 0.0;
		Double std = MyMath.std(er);
		
		s = avgEr / std * Math.sqrt(252);
		
		return s;
	}
	
	/**
	 * get the Sharpe Ratio. rf  - risk free rate
	 * @param portfolioValue
	 * @param rf
	 * @param errMsgHead
	 * @return
	 */
	public static Double sharpeRatio(ArrayList<Double> portfolioValue, Double rf) {
		return sharpeRatio(portfolioValue, rf, "[Calculating Sharpe Ratio]");
	}
}
