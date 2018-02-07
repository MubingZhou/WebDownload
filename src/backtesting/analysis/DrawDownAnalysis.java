package backtesting.analysis;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
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
	public static Double riskFreeRate = 0.0;
	
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
			
			ArrayList<Calendar> allTradingDate = utils.Utils.getAllTradingCal("D:\\stock data\\all trading date - hk.csv");
			
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
			
			ArrayList<Calendar> allTradingDate = utils.Utils.getAllTradingCal("D:\\stock data\\all trading date - hk.csv");
			
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
			for(int i = date1Ind; i <= date2Ind; i++) {
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
	 * @return returns ArrayList<Double>. 0th value: max DD pct (Double); 1st value: max DD period starts (Integer); 2nd value: max DD period ends (Integer); 3rd value: max DD start value; 4th value: max DD ends value
	 */
	public static ArrayList<Double> maxDrawdown(ArrayList<Double> portfolioValue){
		return maxDrawdown(portfolioValue, "[Calculate Max DD]");
	}

	/**
	 * Return the rolling Max DD. For example, if we input a series of market value like this
	 * 100
	 * 100.5
	 * 100.6
	 * 101
	 * ...
	 * and the rolling days is 10, then it will return two columns
	 * The first column: rolling max DD with absolute value (the first 10 data are blank)
	 * null
	 * null
	 * ...
	 * null
	 * 1
	 * 0.5
	 * 0.3
	 * The 2nd column: rolling max DD with percentage (the first 10 data are blank)
	 * null
	 * null
	 * ...
	 * null
	 * 0.01
	 * 0.02
	 * 0.03
	 * @param portfolioValue
	 * @param rollingDays
	 * @return
	 */
	public static ArrayList<ArrayList<Double>> maxDrawdown_Rolling(ArrayList<Double> portfolioValue, int rollingDays){
		if(rollingDays >= portfolioValue.size()) {
			logger.error("portfolioValue too short!");
			return null;
		}
		ArrayList<ArrayList<Double>> toReturn = new ArrayList<ArrayList<Double>>();
		ArrayList<Double> rollingV = new ArrayList<Double>();  // rolling maxDD - value
		ArrayList<Double> rollingP = new ArrayList<Double>();  // rolling maxDD - percentage
		
		for(int i = 0; i < rollingDays-1; i++) {
			rollingV.add(null);
			rollingP.add(null);
		}
		
		for(int i = rollingDays-1; i <= portfolioValue.size(); i++) {
			ArrayList<Double> p = new ArrayList<Double>(portfolioValue.subList(i + 1 - rollingDays, i));
			ArrayList<Double> maxDD = maxDrawdown(p);
			Double maxDD_value = Math.abs(maxDD.get(3) - maxDD.get(4));
			Double maxDD_pct = Math.abs(maxDD.get(0));
			
			rollingV.add(maxDD_value);
			rollingP.add(maxDD_pct);
		}
		
		toReturn.add(rollingV);
		toReturn.add(rollingP);
		
		return toReturn;
	}
	/**
	 * get the Sharpe Ratio. rf  - risk free rate
	 * @param dailyPortfolioValue
	 * @param rf
	 * @param errMsgHead
	 * @return
	 */
	private static Double sharpeRatio(ArrayList<Double> dailyPortfolioValue, Double rf, String errMsgHead) {
		Double s = -100.0;
		
		if(dailyPortfolioValue == null || dailyPortfolioValue.size() == 0) {
			logger.error(errMsgHead + " portfolioValue has no values!");
			return null;
		}
		
		// get the average excessive return 
		ArrayList<Double> er = new ArrayList<Double>(); 
		Double avgEr = 0.0;  // avg excessive return
		Double cumEr = 0.0; // cumulative excessive return
		for(int i = 1; i < dailyPortfolioValue.size(); i++) {
			Double todayValue = dailyPortfolioValue.get(i);
			Double lastValue = dailyPortfolioValue.get(i - 1);
			
			Double todayEr = todayValue / lastValue  - 1 - rf;
			er.add(todayEr);
			cumEr = cumEr + todayEr	;
			
			
		}
		avgEr = cumEr / (dailyPortfolioValue.size() - 1);
		logger.trace("avg excessive return = " + avgEr);
		
		// calculate std
		//Double cumSQ = 0.0;
		Double std = MyMath.std(er);
		
		logger.trace("std = " + std);
		
		s = avgEr / std * Math.sqrt(er.size());
		
		return s;
	}
	
	/**
	 * get the Sharpe Ratio. rf  - risk free rate
	 * assuming portfolioValue contains DAILY portfolio value
	 * @param dailyPortfolioValue
	 * @param rf
	 * @return
	 */
	public static Double sharpeRatio(ArrayList<Double> dailyPortfolioValue, Double rf) {
		return sharpeRatio(dailyPortfolioValue, rf, "[Calculating Sharpe Ratio]");
	}
	
	public static void comprehensiveAnalysis(Portfolio p, String filePath) {
		try {
			logger.info("========= Start Caculating Max Drawdown ===========");
			//analysisBetweenDates_outputPath = utils.Utils.checkPath(analysisBetweenDates_outputPath);
			String errMsgHead = "[DrawDownAnalysis - calculating max DD] ";
			//SimpleDateFormat sdf = new SimpleDateFormat(dateFormat); 
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd"); 
			
			// ======= some pre dealings =======
			Set<Calendar> allDaysSet = p.histSnap.keySet();
			ArrayList<Calendar> allDaysArr = new ArrayList<Calendar>(allDaysSet);
			//ArrayList<Calendar> allDaysArr = new ArrayList<Calendar>();
			Collections.sort(allDaysArr ); // ascending
			
			ArrayList<Calendar> allTradingDate = utils.Utils.getAllTradingCal("D:\\stock data\\all trading date - hk.csv");
			
			/*
			Calendar date1Cal = (Calendar) allDaysArr.get(0).clone();
			date1Cal .setTime(sdf.parse(startDate));
			Calendar date1Cal_temp = utils.Utils.getMostRecentDate(date1Cal, allTradingDate);
			date1Cal.setTime(sdf.parse(sdf.format(date1Cal_temp.getTime())));  // seems that the calendar system extracted from the file is different than that of our system

			Calendar date2Cal = (Calendar) allDaysArr.get(0).clone();
			date2Cal .setTime(sdf.parse(endDate));
			Calendar date2Cal_temp = utils.Utils.getMostRecentDate(date2Cal, allTradingDate);
			date2Cal.setTime(sdf.parse(sdf.format(date2Cal_temp.getTime())));
			
			int date1Ind = allDaysArr.indexOf(date1Cal);
			int date2Ind = allDaysArr.indexOf(date2Cal);
			
			if(date1Ind == -1 || date2Ind == -1) {
				logger.error(errMsgHead + "No such period existed in the Portfolio!");
				return ;
			}*/
			
			int date1Ind = 0;
			int date2Ind = allDaysArr.size() - 1;
			Calendar date1Cal = (Calendar) allDaysArr.get(0).clone();
			Calendar date2Cal  = (Calendar) allDaysArr.get(allDaysArr.size() - 1).clone();
			logger.debug("date1Cal = " + sdf.format(date1Cal.getTime()));
			logger.debug("date2Cal = " + sdf.format(date2Cal.getTime()));
			// ========= get the market value ===========
			ArrayList<Double> marketValue = new ArrayList<Double> ();
			for(int i = date1Ind; i <= date2Ind; i++) {
				PortfolioOneDaySnapshot todayP = p.histSnap.get(allDaysArr.get(i));
				Double todayMarketValue = todayP.marketValue;
				marketValue.add(todayMarketValue);
			}
			
			// =========== get return & volatility & Sharpe ratio ========
			int numOfTradingDays = date2Ind - date1Ind + 1;
			Double beginMV = marketValue.get(0);
			Double endMV = marketValue.get(marketValue.size() - 1);
			
			Double simpleReturn = (endMV - beginMV) / beginMV;
			Double annualizedReturn = Math.log(endMV / beginMV) / (numOfTradingDays  / 252);
			
			ArrayList<Double> returnArr = new ArrayList<Double>();
			Double cumR = 0.0;
			for(int i = 1; i < marketValue.size(); i++) {
				Double todayMV = marketValue.get(i);
				Double lastMV = marketValue.get(i - 1);
				
				Double r = (todayMV - lastMV) / lastMV;
				returnArr.add(r);
				
				cumR += r;
				
				logger.debug("date=" + sdf.format(allDaysArr.get(i).getTime()) +  " i=" + i);
			}
			Double avgReturn = cumR / returnArr.size();
			
			Double std = MyMath.std(returnArr);
			
			Double sharpe = (avgReturn - riskFreeRate) / std * Math.sqrt(returnArr.size());
			
			//========== get beta with respect to HSI or HSCEI ===========
			String HSI_path = "D:\\stock data\\HSI.csv";
			String HSCEI_path = "D:\\stock data\\HSCEI.csv";
			
			String dateFormat2 = "dd/MM/yyyy";
			SimpleDateFormat sdf2 = new SimpleDateFormat(dateFormat2);
			
			BufferedReader bf1 = utils.Utils.readFile_returnBufferedReader(HSI_path);
			ArrayList<String> hsiDateStr = new ArrayList<String> (Arrays.asList(bf1.readLine().split(",")));
			ArrayList<String> hsiPriceStr = new ArrayList<String> (Arrays.asList(bf1.readLine().split(",")));
			bf1.close();
			
			BufferedReader bf2 = utils.Utils.readFile_returnBufferedReader(HSCEI_path);
			ArrayList<String> hsceiDateStr = new ArrayList<String> (Arrays.asList(bf2.readLine().split(",")));
			ArrayList<String> hsceiPriceStr = new ArrayList<String> (Arrays.asList(bf2.readLine().split(",")));
			bf2.close();
			
			ArrayList<Double> hsiPrice = new ArrayList<Double>();
			ArrayList<Double> hsceiPrice = new ArrayList<Double>();
			ArrayList<Double> hsiReturn = new ArrayList<Double>();
			ArrayList<Double> hsceiReturn = new ArrayList<Double>();
			Double cumHSI_return = 0.0;
			Double cumHSCEI_return = 0.0;
			
			int priceCount = -1;
			for(int i = 0; i < hsiPriceStr.size(); i++) {
				//Calendar c = (Calendar) date1Cal.clone();
				Calendar c = (Calendar) allDaysArr.get(0).clone();
				c.setTime(sdf2.parse(hsiDateStr.get(i)));
				
				if((!c.before(date1Cal)) && (!c.after(date2Cal))) {
					Double hsiTodayPrice = Double.parseDouble(hsiPriceStr.get(i));
					Double hsceiTodayPrice = Double.parseDouble(hsceiPriceStr.get(i));
					
					hsiPrice.add(hsiTodayPrice);
					hsceiPrice.add(hsceiTodayPrice);
					
					priceCount++;
					
					if(priceCount >= 1) {
						logger.trace("todaydate = " + hsiDateStr.get(i) + " " + sdf.format(c.getTime()) + " priceCount=" + priceCount);
						Double hsiLastPrice = hsiPrice.get(priceCount - 1);
						Double hsceiLastPrice = hsceiPrice.get(priceCount - 1);
						Double hsiTodayReturn = (hsiTodayPrice - hsiLastPrice) / hsiLastPrice;
						Double hsceiTodayReturn = (hsceiTodayPrice - hsceiLastPrice) / hsceiLastPrice;
						
						hsiReturn.add(hsiTodayReturn);
						hsceiReturn.add(hsceiTodayReturn);
						
						cumHSI_return += hsiTodayReturn;
						cumHSCEI_return += hsceiTodayReturn;
					}
					
				}
			}
			Double avgHSIReturn = cumHSI_return / hsiReturn.size();
			Double avgHSCEIReturn = cumHSCEI_return / hsceiReturn.size();
			
			// == beta with HSI & HSCEI ==
			Double beta_hsi = MyMath.corr(hsiReturn, returnArr);
			Double beta_hscei = MyMath.corr(hsceiReturn, returnArr);
			
			// ========= write files =======
			FileWriter fw = new FileWriter(filePath);
			fw.write("Total Return=," + simpleReturn + "\n");
			fw.write("Annualized Return=," + annualizedReturn + "\n");
			fw.write("Sharpe Ratio=," + sharpe + "\n");
			fw.write("Beta (HSI) =," + beta_hsi + "\n");
			fw.write("Beta (HSCEI) =," + beta_hscei + "\n");
			fw.close();
			
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
}
