package strategy.db_southboundFlowPortfolio;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import backtesting.analysis.DrawDownAnalysis;
import backtesting.backtesting.Backtesting;
import backtesting.backtesting.Trade;
import backtesting.portfolio.Portfolio;
import backtesting.portfolio.PortfolioOneDaySnapshot;
import backtesting.portfolio.Underlying;
import math.MyMath;
import utils.Utils;
import utils.XMLUtil;
import webDownLoadHKEX.NorthboundHolding;
import webbDownload.southboundData.DataCombiner;
import webbDownload.southboundData.DataDownloader;

public class Main {
	private static Logger logger = LogManager.getLogger(Main.class.getName());
	
	//System.out.println(southboundData.DataGetter.getStockData("700", "20170801", "yyyyMMdd").get(2));
	public static SimpleDateFormat sdf_yyyyMMdd = new SimpleDateFormat("yyyyMMdd");
	public static SimpleDateFormat sdf_yyyy_MM_dd = new SimpleDateFormat("yyyy-MM-dd");
	//ArrayList<Calendar> allTradingDate = utils.Utils.getAllTradingDate("D:\\stock data\\all trading date - hk.csv");
	public static String allTradingDatePath = "Z:\\Mubing\\stock data\\all trading date - hk.csv";
	public static String allMMATradingDatePath = "Z:\\Mubing\\stock data\\all MMA trading date - hk.csv";
	public static ArrayList<Calendar> allTradingCal = new ArrayList<Calendar>();
	public static ArrayList<Date> allTradingDate = new ArrayList<Date>();
	public static ArrayList<Calendar> allMMATradingCal = new ArrayList<Calendar>();
	public static ArrayList<Date> allMMATradingDate = new ArrayList<Date>();
	//ArrayList<Calendar> allTradingDate = utils.Utils.getAllTradingDate("T:\\Mubing\\stock data\\all trading date - hk.csv");
	//String MAIN_ROOT_PATH = "D:\\stock data\\southbound flow strategy - db";
	public static String MAIN_ROOT_PATH = "Z:\\Mubing\\stock data\\southbound flow strategy - db";
	public static String ALL_STOCK_LIST_PATH = "Z:\\Mubing\\stock data\\all stock list.csv";
	public static String STOCK_PRICE_PATH = "Z:\\Mubing\\stock data\\stock hist data - webb"; //"Z:\\Mubing\\stock data\\stock hist data - webb"
	public static String SOUTHBOUND_DATA_PATH = "Z:\\Mubing\\stock data\\HK CCASS - WEBB SITE\\southbound\\combined";
	public static String notionalChgDataRootPath = "Z:\\Mubing\\stock data\\southbound flow strategy - db\\";
	public static String percentileDataRootPath = MAIN_ROOT_PATH + "\\southbound chg percentile - by stock\\";  //outputPath_byStock
	public static String southboundDataRootPath = "Z:\\Mubing\\stock data\\HK CCASS - WEBB SITE\\southbound";
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			if(false) {
				MAIN_ROOT_PATH = "T:\\Mubing\\stock data\\southbound flow strategy - db";
				ALL_STOCK_LIST_PATH = "T:\\Mubing\\stock data\\all stock list.csv";
				STOCK_PRICE_PATH = "T:\\Mubing\\stock data\\stock hist data - webb";
				SOUTHBOUND_DATA_PATH = "T:\\Mubing\\stock data\\HK CCASS - WEBB SITE\\southbound\\combined";
				notionalChgDataRootPath = "T:\\Mubing\\stock data\\southbound flow strategy - db\\";
				southboundDataRootPath = "T:\\Mubing\\stock data\\HK CCASS - WEBB SITE\\southbound";
				allTradingDatePath = "T:\\Mubing\\stock data\\all trading date - hk.csv";
				allMMATradingDatePath = "T:\\Mubing\\stock data\\all MMA trading date - hk.csv";
			}
			
			allTradingCal = utils.Utils.getAllTradingCal(allTradingDatePath);
			allTradingDate = utils.Utils.getAllTradingDate(allTradingDatePath);
			allMMATradingCal = utils.Utils.getAllTradingCal(allMMATradingDatePath);
			allMMATradingDate = utils.Utils.getAllTradingDate(allMMATradingDatePath);
			
//			Please run this program in the morning 
//			and DO NOT shut down the program after running
			
			//downloadSBData();
			
			if(true) {
				boolean isDownloadSBData = true;
				
				boolean isBacktest = true;
					boolean isOutputAVATStockPicks = false;
					
				boolean isDownloadPriceData = true;
					boolean isShutDown_StockPrice = false;
				
				
				
				//downloadSBData();
				if(isDownloadSBData) {
					downloadSBData();
					logger.info("Download Southbound Data - Done!");
					
					calAvgVolume();
					logger.info("Calculate Average Volume - Done!");
					
					//calDailyNotionalChg();
					//logger.info("Calculate Daily Notional Change - Done!");
					
					//calSBPercentile();
					//logger.info("Download Southbound Percentile - Done!");
					
				}
				
				if(isBacktest) {
					BacktestFrame.isOutputAVATStockPicks = isOutputAVATStockPicks;
					fullBacktesting();
					logger.info("Full Back-testing - Done!");
				}
				
				if(isDownloadPriceData) {
					SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd hh:mm:ss");
					String todayDateStr = sdf_yyyyMMdd.format(new Date());
					Date downloadingDate = sdf.parse(todayDateStr + " 23:30:00");
					
					Date nowDate = new Date();
					while(nowDate.before(downloadingDate)) {
						logger.info("nowDate=" + sdf.format(nowDate));
						Thread.sleep(1000 * 60 * 30);  // half an hour
						nowDate = new Date();
					}
					webDownload.GetPrice.downloadData_2();
					logger.info("Downloading Data - Done!");
					
					if(isShutDown_StockPrice)
						Runtime.getRuntime().exec( "shutdown -s -t 1");
				}
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void fullBacktesting() {
		logger.info("============ Full Backtesting ============");
		try {
			/*
			 * Factors to consider:
			 * 1. ranking methodology (√)
			 * 2. avg turnover threshold (√)
			 * 3. # of stocks to be selected on each rebalancing date (√)
			 * 4. filter: 在两次调仓之间，至少有70%的日子的flow是流入的 
			 * 5. 提前结束某只股票position的条件：连续X天出现净outflow  (√)
			 * 6. 所有的股票是equally weighted，还是根据排名，赋予的权重不同 (√)
			 * 7. 是short HSI还是HSCEI，还是只从HSI或者HSCEI的成分股中选择
			 * 8. rebalancing的frequency，1周、2周、1个月rebalance一次？
			 * 9. 是否使用inflow的金额，而不是inflow占freefloat的百分比做ranking
			 * 10. 1 month or 3 month adv
			 * 11. rolling
			 * 12. For single stock, SB change compare to past changes.
			 * 
			 * Notes:
			 * 关于factor 1，目前有四种ranking：
			 * 		1) 计算两次调仓日期之间的southbound flow的change，然后除以freefloat，按这个排序  	(rank1)
			 * 		2) 计算两次调仓日期之间的southbound flow的change，然后除以1 month ADV，按这个排序	(rank2)
			 * 		3) 计算两次调仓日期之间每日的southbound /flow的change，然后除以当日的freefloat，排序，再将所有天的排序取均值		(rank3)
			 * 		4) 计算两次调仓日期之间每日的southbound flow的change，然后除以当日的1 month ADV，排序，再将所有天的排序取均值	(rank4)
			 * 		5) 计算两次调仓日期之间的southbound flow的notional change，即有多少资金买入了		(rank5)
			 * 		6) 计算两次调仓日期之间的southbound flow占过去250天的flow的percentile，算法为(current flow - min flow) / (max flow - min flow)，然后rank6 = 430 * percentile  （假设有430只股票）  (rank6)  效果好像不是很好
			 * 		7) 对于所有股票而言，利用rank1计算其每日的ranking，然后计算过去一段时间某只股票ranking的升幅，升幅越大的排名越靠前（比如对过去5天的所有index member计算其ranking的升幅，买入升幅靠前的5名） （rank7） 效果好像不是很好
			 *    
			 * 然后有5种ranking strategy：
			 * 		1) 最终的ranking是 (rank1 + rank2 + rank3 + rank4) / 4
			 * 		2) 最终的ranking是：先按照rank1 + rank2进行ranking，得到rank5，再按照rank3 + rank4进行ranking，得到rank6，最后按照rank5 + rank6进行ranking
			 * 		3) 最终的ranking是 (rank1 + rank4) / 2
			 * 		4) 最终的ranking是 (rank2 + rank3) / 2
			 * 		5) 最终的ranking是 rank5
			 * 		6) 最终的ranking是 rank6
			 * 			6.1) 查看过去一段时间（比如一周）的数据，如果过去1周之内
			 * 		7) 最终的ranking是 rank7
			 * 		8) 最终的ranking是 ((rank1 + rank2 + rank3 + rank4) / 4 + rank5)/2，也就是说，notional的影响占一半，freefloat的影响占1/4，volume的影响占1/4
			 */
			String dateFormat = "yyyyMMdd";
			SimpleDateFormat sdf = new SimpleDateFormat (dateFormat);
			SimpleDateFormat sdf2 = new SimpleDateFormat("yyyyMMdd HHmmss"); 
			String startDateStr = "20170101";  // 20160729
			String endDateStr = "20180202";		// "20171109"
			Double initialFunding = 12500000.0;  // 
			Double tradingCost = 0.002;
			ArrayList<String>  blackList = new ArrayList<String>() ;
			boolean isFixedAmount = true;	//每次每只股票买fixed amount
			double eachStockValue = 800000;
			boolean isRebalanceEachTime = false;   
			double eachStockValueRebalanceThreshold = 0.01;
			int daysBetweenRelancingDate_Rolling = 15;   // 在rolling的情况下，rolling的observation period
			blackList.add("607");
			blackList.add("1250");
			
			
			// -------------------- path settings -------------------
			BacktestFrame.allSbDataPath =SOUTHBOUND_DATA_PATH ;
			BacktestFrame.allPriceDataPath = STOCK_PRICE_PATH;
			BacktestFrame.allTradingDatePath = allMMATradingDatePath;
			PortfolioScreening.avgVolMainPath = MAIN_ROOT_PATH + "\\stock avg trd vol - 1M\\";
			BacktestFrame.notionalChgDataRootPath = notionalChgDataRootPath;
			BacktestFrame.percentileDataRootPath = percentileDataRootPath;
			
			// -------------------- Configurations -----------------------
			String portFilePath = MAIN_ROOT_PATH + "\\" 
					+ sdf2.format(new Date()) 
					//+ " 11M USD - roll 15days"
					+ " 12.5M USD rolling " + sdf.format(utils.Utils.getRefDate(new Date(), allMMATradingDate, -1)) + ""
					//rolling stock picks 20171219   buffer - 15-20   index 5days FULLLIST
					;
			/*
			 * Rolling configurations:
			 * rankingStrategy = 1;
			 * stockUniverse = 1;
			 * rebalancingStrategy: self defined daily
			 * daysBetweenRelancingDate = 15
			 * topNStocks = 25;
			 * minInflowPct = 0.0;
			 */
			
			File f = new File(portFilePath);
			f.mkdir();
			
			double avgDailyValueThreshHold_USD =  12500000.0;  // 每天的平均成交额需要超过这个数才能入选
			int topNStocks = 100;   // 每次选多少只股票进行买入卖出
			int topNStocksMode = 1;
			/*
			 * 1 - 正常
			 * 2 - buffer模式，即需要买入股票的排名进入“入选股票线”才买入，但是已经买入的股票只有跌出“剔除股票线”才卖出。在这种情况下，每只股票的持仓相当于1/topNStocks_bufferZone_out的比例，即有可能不是满仓
			 * 		这种模式下需要对BacktestFrame.topNStocks_bufferZone_in和BacktestFrame.topNStocks_bufferZone_out进行赋值，后者要大于前者
			 */
			int topNStocks_bufferZone_in =  15;
			int topNStocks_bufferZone_out = 20;
			
			double minInflowPct = 0.0;   // factor 4  在两次调仓之间，至少有这个比例的日子的flow是流入的
			
			// 现在rebalancing时使用的数据是固定5天的   daysBetweenRelancingDate
			double rankingStrategy = 1;
			/*
			 * 1 - (rank1 + rank2 + rank3 + rank4) / 4
			 * 2 - 
			 * 3 - (rank1 + rank4) / 2
			 * 4 - (rank2 + rank3) / 2
			 * 5 - rank5
			 * 6 - rank6 (	mode 0: use historical percentile
			 * 				mode 1: use past 250D percentile
			 * 				mode 2: use past 60D percentile
			 * 			)
			 *		6.1 - 只有historical的值超过一定的阈值（比如95%）的时候才买入
			 * 7 - rank7
			 * 8 - ((rank1 + rank2 + rank3 + rank4) / 4 + rank5)/2
			 */
			int rank6Mode = 2;
			double rankingStrategy6_1_threshold = 1.0;
			int rankingStrategy6_1_holdDay = 5;
			
			int stockUniverse = 1;
			/*
			 * 1 - south bound 
			 * 2 - HSI
			 * 3 - HSCEI
			 * 4 - HSCEI + HSI
			 */
			int weightingStrategy = 1;
				/*
				 * 1 - Equally weighted
				 * 2 - 按照排名分成四组，每组所有股票的加起来的weights分别是40%，30%，20%，10%
				 * 
				 */
			int rebalancingStrategy = 100;
			/*
			 * rebalancingStrategy
			 * 1 - monthly, rebal at month beginning
			 * 2 - monthly, rebal at month end
			 * 3 - bi-weekly
			 * 4 - weekly
			 * 5 - every 40 trading days
			 * 100+ - self defined
			 */
			
			int earlyUnwindStrategy  = 1;
			/*
			 * 1 - 正常，不会提前卖出某只股票
			 * 2 - 如果一只股票的southbound holding连续5天减少，则提前全部卖掉，并不再补充其他股票
			 * 3 - 如果一只股票的southbound holding连续三天减少，则提前卖掉一半，并不再补充其他股票
			 */
			
			boolean isOutputDailyCCASSChg = false; // 是否输出每日southbound的CCASS的change  (必须设置为true，不然会影响earlyUnwindStrategy)
			
			
			// ----------- performance profile pre-settings -----------
			ArrayList<String> performanceItems = new ArrayList<String>();
			performanceItems.add("Start Market Value");
			performanceItems.add("End Market Value");
			//performanceItems.add("Total Return ($)");
			performanceItems.add("Total Return");
			performanceItems.add("Annualized Return");
			performanceItems.add("# of years");
			performanceItems.add("Max DD Pct");
			performanceItems.add("Annualized Return / Max DD");
			performanceItems.add("Sharpe");
			performanceItems.add("Std");
			performanceItems.add("Annualized Volatility");
			
			ArrayList<Map<String, Double>> allPerformanceData = new ArrayList<Map<String, Double>>();
			ArrayList<String> allPerformanceDataTitle = new ArrayList<String>();
			
			if(rankingStrategy == 5)
				BacktestFrame.isToCalNotional = true;
			else
				BacktestFrame.isToCalNotional = false;
			
			//-----------------------------------------
			int[] topNStocksArr = {35}; //{3,5,7,9,10,11,13,15,17,19,20,21,23,25,27,29,30,31};
			int[] weightingStrategyArr = {1,2};
			int[] earlyUnwindStrategyArr = {1,2};
			double[] avgDailyValueThreshHold_USDArr = {
				5000000 /*5 mil USD*/,  7000000 /*7 mil USD*/, 10000000 /*10 mil USD*/, 15000000 /*15 mil USD*/ 	
			};
			int[] stockUniverseArr = {1,2,3,4};
			int[] rebalancingStrategyArr = {1,2,3,4};
			
			double[] rankingStrategy6_1_threshold_Arr = {0.8,0.85,0.9,0.95,1.0};
			int [] rankingStrategy6_1_holdDay_Arr = {3,5,10,15};
//			double[] rankingStrategy6_1_threshold_Arr = {0.9};
//			int [] rankingStrategy6_1_holdDay_Arr = {3};
			boolean[] isRebalanceEachTimeArr = {false, true};
			int[] daysBetweenRelancingDate_Rolling_Arr = {15};  //{5,10,15,20,25,30,35,40}
			
			int size1 = 1;
			//size1 = topNStocksArr.length;
			//size1 = rankingStrategy6_1_threshold_Arr.length;
			size1 = topNStocksArr.length;
			
			int size2 = 1;
			//size2 = weightingStrategyArr.length;
			//size2 = earlyUnwindStrategyArr.length;
			//size2 = avgDailyValueThreshHold_USDArr.length;
			//size2 = stockUniverseArr.length;
			//size2 = rebalancingStrategyArr.length;
			//size2 = rankingStrategy6_1_holdDay_Arr.length;
			//size2 = isRebalanceEachTimeArr.length;
			size2 = daysBetweenRelancingDate_Rolling_Arr.length;

			for(int i = 0; i < size1; i++) {
				topNStocks = topNStocksArr[i];
				//rankingStrategy6_1_threshold = rankingStrategy6_1_threshold_Arr[i];
				for(int j = 0; j < size2; j++) {
					//rankingStrategy6_1_holdDay = rankingStrategy6_1_holdDay_Arr[j];
					//earlyUnwindStrategy = earlyUnwindStrategyArr[j];
					//avgDailyValueThreshHold_USD = avgDailyValueThreshHold_USDArr[j];
					//stockUniverse = stockUniverseArr[j];
					//rebalancingStrategy = rebalancingStrategyArr[j];
					//isRebalanceEachTime = isRebalanceEachTimeArr[j];
					daysBetweenRelancingDate_Rolling = daysBetweenRelancingDate_Rolling_Arr[j];
					
					// -------------- file sub name ----------
					String fileSubName = "";
					switch(rebalancingStrategy) {
					case 1:
						fileSubName += "montly begin";
						break;
					case 2:
						fileSubName += "monthly end";
						break;
					case 3:
						fileSubName += "biweekly";
						break;
					case 4:
						fileSubName += "weekly";
						break;
					case 5:
						fileSubName += "40 trd days";
						break;
					default:
						fileSubName += "self defined";
						break;
					}
					
					fileSubName += " - ";
					
					switch(stockUniverse) {
					case 1:
						fileSubName += "sb";
						break;
					case 2:
						fileSubName += "HSI";   // excluding LINK REIT (823 HK Equity)
						break;
					case 3:
						fileSubName += "HSCEI";
						break;
					case 4:
						fileSubName += "HSI HSCEI";  // excluding LINK REIT (823 HK Equity)
						break;
					}
					String title = "";  // 每个case的文件夹名称
					if(topNStocksMode == 1) {
						title += topNStocks + "stocks - ";
					}
					if(topNStocksMode == 2) {
						title += "bufferMode2_" + topNStocks_bufferZone_in + "-" + topNStocks_bufferZone_out + " - ";
					}
					title +=  fileSubName;
					//title = " th " + rankingStrategy6_1_threshold + " hl " + rankingStrategy6_1_holdDay;
					if(isRebalanceEachTime)
						title += " - rebal";
					else
						title += " - not rebal";
					
					title += " - roll" + daysBetweenRelancingDate_Rolling + " days";
					allPerformanceDataTitle.add(title);
					
					// ------------------- main settings -------------
					BacktestFrame.portFilePath = portFilePath + "\\" + title;
					BacktestFrame.rankingStrategy = rankingStrategy;
					BacktestFrame.rank6Mode = rank6Mode;
					BacktestFrame.rankingStrategy6_1_threshold = rankingStrategy6_1_threshold;
					BacktestFrame.rankingStrategy6_1_holdDay = rankingStrategy6_1_holdDay;
					BacktestFrame.avgDailyValueThreshHold_USD = avgDailyValueThreshHold_USD; 
					BacktestFrame.topNStocks = topNStocks;
					BacktestFrame.minInflowPct =  minInflowPct;
					BacktestFrame.stockUniverse = stockUniverse; 
					BacktestFrame.weightingStrategy = weightingStrategy;
					BacktestFrame.rebalancingStrategy = rebalancingStrategy;
					BacktestFrame.isOutputDailyCCASSChg = isOutputDailyCCASSChg;  
					BacktestFrame.earlyUnwindStrategy = earlyUnwindStrategy;
					BacktestFrame.topNStocks_mode = topNStocksMode; 
					BacktestFrame.topNStocks_bufferZone_in = topNStocks_bufferZone_in;
					BacktestFrame.topNStocks_bufferZone_out = topNStocks_bufferZone_out;
					BacktestFrame.daysBetweenRelancingDate_Rolling = daysBetweenRelancingDate_Rolling;
					
					BacktestFrame.isFixedAmount = isFixedAmount;   
					BacktestFrame.eachStockValue = eachStockValue;
					BacktestFrame.isRebalanceEachTime = isRebalanceEachTime;
					BacktestFrame.eachStockValueRebalanceThreshold = eachStockValueRebalanceThreshold;
					
					if(isFixedAmount)
						initialFunding = eachStockValue * topNStocks * 2;
					
					BacktestFrame.initialFunding = initialFunding;
					BacktestFrame.tradingCost = tradingCost;
					
					BacktestFrame.startDateStr = startDateStr;
					BacktestFrame.endDateStr = endDateStr;
					
					// -------------- Main body -----------
					BacktestFrame.init();
					ArrayList<Object> data = BacktestFrame.getRebalancingSelection();
					//Thread.sleep(1000  * 100000);
					Portfolio pf = BacktestFrame.backtesting( data);
					ArrayList<Object> marketValueData = pf.getMarketValue("20000101","20200101","yyyyMMdd");
					ArrayList<Double> marketValue = (ArrayList<Double>) marketValueData.get(0);
					
					// ---------- test --------------
					ArrayList<ArrayList<ArrayList<Object>>> allRebalData = (ArrayList<ArrayList<ArrayList<Object>>>) data.get(1);
					ArrayList<Date> rebalDates = (ArrayList<Date>) data.get(0);
					ArrayList<String> rebalDatesStr = new ArrayList<String>();
					final int size = rebalDates.size();
					for(int k = 0;k < size; k ++) {
						rebalDatesStr.add(sdf.format(rebalDates.get(k)));
					}
					FileWriter fw_t = new FileWriter("D:\\test.csv");
					for(int k = 0; k < size; k++) {
						fw_t.write(rebalDatesStr.get(k));
						ArrayList<Object> todayStocks = allRebalData.get(k).get(0);
						ArrayList<Object> todayDirections = allRebalData.get(k).get(1);
						for(int kk = 0; kk < todayStocks.size(); kk++) {
							if((Integer) todayDirections.get(kk) > 0)
								fw_t.write("," + todayStocks.get(kk));
						}
						fw_t.write("\n");
					}
					fw_t.close();
					
					// ------------- performance profile -----------
					double startMarketValue  = initialFunding;
					double endMarketValue = marketValue.get(marketValue.size() - 1);
					double totalReturn = endMarketValue / startMarketValue - 1;  
					
					ArrayList<Double> maxDrawdownData = backtesting.analysis.DrawDownAnalysis.maxDrawdown(marketValue);
					/*
					double maxDrawdownValue = maxDrawdownData.get(0);
					int maxDD_beginInd = maxDrawdownData.get(1).intValue();
					double maxDD_beginValue = marketValue.get(maxDD_beginInd);
					int maxDD_endInd = maxDrawdownData.get(2).intValue();
					double maxDD_endValue = marketValue.get(maxDD_endInd);
					double maxDDPct = (maxDD_beginValue -  maxDD_endValue) / maxDD_beginValue;
					*/
					double maxDDPct = maxDrawdownData.get(0);
					
					// calculate annuliazed return
					Date startDate = sdf_yyyyMMdd.parse(startDateStr);
					Date endDate = sdf_yyyyMMdd.parse(endDateStr);
					double days = (endDate.getTime() - startDate.getTime()) / (1000 * 3600 * 24);
					double numYears = days/365.0;
					double annualizedReturn = Math.log(endMarketValue/startMarketValue) / numYears;
					
					
					double sharpe = backtesting.analysis.DrawDownAnalysis.sharpeRatio(marketValue, 0.0);
					double std = MyMath.std(marketValue);
					
					double volatility = math.MyMath.volatility(marketValue);
					
					Map<String, Double> thisPerformanceData = new HashMap<String, Double>();
					thisPerformanceData.put("Start Market Value", startMarketValue);
					thisPerformanceData.put("End Market Value", endMarketValue);
					thisPerformanceData.put("Total Return", totalReturn);
					thisPerformanceData.put("Annualized Return",annualizedReturn);
					thisPerformanceData.put("# of years",numYears);
					thisPerformanceData.put("Max DD Pct", maxDDPct);  // negative number
					thisPerformanceData.put("Annualized Return / Max DD", annualizedReturn / -maxDDPct);
					thisPerformanceData.put("Sharpe", sharpe);
					thisPerformanceData.put("Std", std);
					thisPerformanceData.put("Annualized Volatility",volatility);
					allPerformanceData.add(thisPerformanceData);
					
				}
			}
			
			FileWriter fw = new FileWriter(portFilePath+"\\performance overview.csv");
			for(int i = 0; i < allPerformanceDataTitle.size(); i++)
				fw.write("," + allPerformanceDataTitle.get(i));
			fw.write("\n");
			for(int i = 0; i < performanceItems.size(); i++) {
				//Map<String, Double> thisPerformanceData = allPerformanceData.get(i);
				String thisPerformanceItem = performanceItems.get(i); 
				fw.write(thisPerformanceItem);
				for(int j = 0; j < allPerformanceDataTitle.size(); j++) {
					Double d = allPerformanceData.get(j).get(thisPerformanceItem);
					fw.write("," + d.toString());
				}
				fw.write("\n");
			}
			fw.close();
			
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 计算并输出每只股票一段时间内的平均成交量，每天存一个file
	 * Z:\Mubing\stock data\southbound flow strategy - db\stock avg trd vol - 1M
	 */
	public static void calAvgVolume() {
		try {
			logger.info("============== calculating stock volume and save ===============");
			Map<Date, FileWriter> fwMap = new HashMap();  // map的key是yyyyMMdd形式的日期
			Calendar startCal = Calendar.getInstance();
			startCal.setTime(sdf_yyyyMMdd.parse("20180101"));
			Calendar startCal2 = utils.Utils.getMostRecentDate(startCal, allTradingCal);
			
			int daysShift = 20;
			int start_ind = allTradingCal.indexOf(startCal2);
			Calendar dataStartCal = allTradingCal.get(start_ind - daysShift);
			startCal = (Calendar) startCal2.clone();
			
			// create these filewriters
			for(int i = 0; i < allTradingCal.size(); i++) {
				Calendar thisCal = allTradingCal.get(i);
				if(!thisCal.before(startCal)) {
					String thisCal_str = sdf_yyyyMMdd.format(thisCal.getTime());
					Date thisCal_date = thisCal.getTime();
					
					String path = MAIN_ROOT_PATH + "\\stock avg trd vol - 1M\\";
					//path = "D:\\stock data\\southbound flow strategy - db\\stock avg trd vol\\";
					FileWriter fw = new FileWriter(path + thisCal_str + ".csv");
					fw.write("stock,1M avg vol(shares),1M avg turnover(value)\n");
					
					fwMap.put(thisCal_date, fw);
				}
			}
			
			// write data
			String stockDirPath = stockPrice.DataGetter.STOCK_DATA_PATH;
			File f = new File(stockDirPath );
			String[] fileList = f.list();
			for(int i = 0; i < fileList.length; i++) {
				String fileName = fileList[i];
				logger.debug("File = " + fileName);
				String stock = fileName.substring(0, fileName.length() - 4);
				
				try {
					Double d = Double.parseDouble(stock);
				}catch(Exception e) {
					logger.debug("Non stock data file! - " + fileList[i]);
					continue;
				}
				
				BufferedReader bf = utils.Utils.readFile_returnBufferedReader(stockDirPath + fileList[i]);
				
				int ind0 = 0;
				int ind1 = 0;
				ArrayList<ArrayList<String>> data = new ArrayList<ArrayList<String>> ();  //先把这只股票所有天的数据存到这里
				String line = "";
				while((line = bf.readLine()) != null) {
					if(ind0 == 0) {
						ind0++;
						continue;
					}
					ArrayList<String> lineDataArr = new ArrayList<String>(Arrays.asList(line.split(",")));
					Calendar thisCal = Calendar.getInstance();
					
					String dStr = lineDataArr .get(0);
					Date d = new Date();
					if(utils.Utils.isDate(dStr, "dd/MM/yyyy"))
						d = new SimpleDateFormat("dd/MM/yyyy").parse(dStr);
					if(utils.Utils.isDate(dStr, "yyyy-MM-dd")) {
						d = new SimpleDateFormat("yyyy-MM-dd").parse(dStr);
					}
					thisCal.setTime(d);
					
					// 只提取dataStartCal往后日期的数据
					if(thisCal.before(dataStartCal)) {
						break;
					}
					data.add(lineDataArr);
				}
				bf.close();
				
				// 从后面往前开始读取
				for(int j = data.size() - 1 - daysShift; j >= 0; j--) {
					ArrayList<String> dataArr = data.get(j);
					
					String thisDate = dataArr.get(0);
					//System.out.println("thisDate=" + thisDate);
					
					Date temp  = new Date();
					
					if(utils.Utils.isDate(thisDate, "dd/MM/yyyy")) {
						temp = new SimpleDateFormat("dd/MM/yyyy").parse(thisDate);
					}
					if(utils.Utils.isDate(thisDate, "yyyy-MM-dd")) {
						temp = new SimpleDateFormat("yyyy-MM-dd").parse(thisDate);
					}
									
					thisDate = sdf_yyyyMMdd.format(temp); // change the format
					
					
					Double accVol = 0.0;
					Double accTur = 0.0;
					int numTrdDate = 0;
					for(int k = 0; k < daysShift; k++) {
						ArrayList<String> pastDataArr = data.get(j + k);
						Double thisVol = Double.parseDouble(pastDataArr.get(8));
						Double thisTur = Double.parseDouble(pastDataArr.get(9));
						
						if(thisVol > 0)
							numTrdDate++;
						accVol = accVol + thisVol;
						accTur = accTur + thisTur;
					}
					
					Double avgVol = -1.0;
					Double avgTur = -1.0;
					if(numTrdDate > 0) {
						avgVol = accVol / numTrdDate;
						avgTur = accTur / numTrdDate;
					}
					
					FileWriter fw = fwMap.get(temp);
					if(fw==null) {
						System.out.println("fw null! date=" + new SimpleDateFormat("yyyyMMdd").format(temp));
					}
					fw.write(
							stock+","
							+String.valueOf(avgVol) + "," 
							+ String.valueOf(avgTur) + "\n");
				} // end of for(j)
			}
			
			
			// close all filewriters
			Set<Date> allDates = fwMap.keySet();
			for(Date thisDate : allDates) {
				FileWriter fw = fwMap.get(thisDate);
				fw.close();
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 计算southbound holding每天的notional chg，对于一只股票的计算方式如下：
	 * 假设1.22，1.23，1.24，1.25为连续的4个交易日
	 * 假设我们southbound holding的settlement date为1.25，则其trading date为1.23（也就是说1.25才能拿到1.23的数据）
	 * 我们计算1.25的notional chg是 （trading date为1.23的holding - trading date为1.22的holding）×1.22的vwap price  
	 * 
	 * Z:\Mubing\stock data\southbound flow strategy - db\southbound notional chg
	 */
	public static void calDailyNotionalChg() {
		try {
			logger.info("============== notional chg ===============");
			String startDateStr = "20180101";
			String endDateStr = sdf_yyyyMMdd.format(utils.Utils.getRefDate(new Date(), allTradingDate, -1));
			String filePath = MAIN_ROOT_PATH + "\\southbound notional chg\\";
			ArrayList<String> allStockList = new ArrayList<String>();
			BufferedReader bf_0 = utils.Utils.readFile_returnBufferedReader(ALL_STOCK_LIST_PATH);
			String line  = bf_0.readLine();
			allStockList.addAll(Arrays.asList(line.split(",")));
			
			
			Map<String, FileWriter> fwMap = new HashMap();  // map的key是yyyyMMdd形式的日期
			Calendar startCal = Calendar.getInstance();
			startCal.setTime(sdf_yyyyMMdd.parse(startDateStr));
			Calendar startCal2 = utils.Utils.getMostRecentDate(startCal, allTradingCal);
			logger.info("start date = " + sdf_yyyy_MM_dd.format(startCal2.getTime()));
			
			int start_ind = allTradingCal.indexOf(startCal2);
			Calendar dataStartCal = allTradingCal.get(start_ind);
			startCal = (Calendar) startCal2.clone();
			Date startDate = startCal.getTime();
			
			Date endDate = sdf_yyyyMMdd.parse(endDateStr);
			
			// create these filewriters
			for(int i = 0; i < allTradingCal.size(); i++) {
				Calendar thisCal = allTradingCal.get(i);
				if(thisCal.getTime().after(endDate)) {
					break;
				}
				
				if(!thisCal.before(startCal)) {
					String thisCal_str = sdf_yyyyMMdd.format(thisCal.getTime());

					FileWriter fw = new FileWriter(filePath + thisCal_str + ".csv");
					fw.write("stock,1D notional chg\n");
					
					fwMap.put(thisCal_str, fw);
				}
			}
			
			Map<String, Map<Date,ArrayList<Double>>> priceDataMap = new HashMap<String, Map<Date,ArrayList<Double>>>();
			Map<String, Double> oneDaySbDataMap = new HashMap<String, Double>();	// 存储一天的sb data，String是stock code
			Map<String, Double> lastDaySbDataMap = new HashMap<String, Double>();
			
			//先读取lastDaySbDataMap, 初始化
			BufferedReader bf = Utils.readFile_returnBufferedReader(
					SOUTHBOUND_DATA_PATH + "\\"  +
					sdf_yyyy_MM_dd.format(allTradingCal.get(start_ind).getTime()) + ".csv" );
			line = "";
			int count = 0;
			while((line = bf.readLine()) != null) {
				if(count == 0) {
					count ++;
					continue;
				}
				String[] lineArr = line.split(",");
				String hldStr = lineArr[2];
				Double hld = 0.0;
				try {
					hld = Double.parseDouble(hldStr);
				}catch(Exception e) {
					
				}
				lastDaySbDataMap.put(lineArr[0], hld);
			} // end of while
			bf.close();
			
			
			for(int i = start_ind + 1; i < allTradingCal.size(); i++) {
				String todayDateStr = sdf_yyyy_MM_dd.format(allTradingCal.get(i).getTime());
				Date todayDate = allTradingCal.get(i).getTime();
				if(todayDate.after(endDate))
					break;
				
				logger.debug("-- todayDateStr = " + todayDateStr );
				
				// ------- 获取当天 southbound的数据 ----------
				String sbDataFilePath = SOUTHBOUND_DATA_PATH + "\\" + todayDateStr + ".csv";
				BufferedReader bfi = Utils.readFile_returnBufferedReader(sbDataFilePath);
				line = "";
				count = 0;
				while((line = bfi.readLine()) != null) {
					if(count == 0) {
						count ++;
						continue;
					}
					String[] lineArr = line.split(",");
					String hldStr = lineArr[2];
					Double hld = 0.0;
					try {
						hld = Double.parseDouble(hldStr);
					}catch(Exception e) {
						
					}
					String stock = lineArr[0];
					oneDaySbDataMap.put(stock, hld);
					
					// ---------- 找price的数据 ----------
					Map<Date,ArrayList<Double>> thisStockPriceMap = priceDataMap.get(stock);
					if(thisStockPriceMap == null || thisStockPriceMap.size() == 0) {
						logger.debug("   -- find price, stock=" + stock);
						thisStockPriceMap = new HashMap<Date,ArrayList<Double>>();
						String priceDataFile = STOCK_PRICE_PATH + "\\" + stock + ".csv";
						BufferedReader bf_price = Utils.readFile_returnBufferedReader(priceDataFile);
						int c = 0;
						String lineP = "";
						while((lineP = bf_price.readLine()) != null) {
							if(c == 0) {
								c++;
								continue;
							}
							String[] linePArr = lineP.split(",");
							String tempDateStr = linePArr[0];
							String tempPriceStr = linePArr[3];
							//logger.debug("    line=" + c);
							String tempPriceStrAdj = linePArr[11];
							String tempVWAPPriceStr = linePArr[10];
							
							
							Date tempDate = sdf_yyyy_MM_dd.parse(tempDateStr);
							if(tempDate.before(allTradingCal.get(start_ind).getTime()))
								break;
							
							Double tempPrice = Double.parseDouble(tempPriceStr);
							Double tempPriceAdj = Double.parseDouble(tempPriceStrAdj);
							Double tempVWAPPrice = Double.parseDouble(tempVWAPPriceStr);
							
							ArrayList<Double> tempData = new ArrayList<Double>();
							tempData.add(tempPrice);
							tempData.add(tempPriceAdj);
							tempData.add(tempVWAPPrice);
							
							thisStockPriceMap.put(tempDate, tempData);
							c++;
							
							if(stock.equals("590")) {
								//logger.info("  --- --- date=" + tempDateStr + " price=" + tempPrice + " tempPriceAdj=" + tempPriceAdj);
								//Thread.sleep(1000 * 10000000);
							}
						}
						bf_price.close();
						priceDataMap.put(stock, thisStockPriceMap);
					}
					
				} // end of while
				bfi.close();
				
				// --------- 计算两天holding之间的diff ----------
				Set<String> todayAllStock = oneDaySbDataMap.keySet();
				for(String stock : todayAllStock) {
					Double todayHld = oneDaySbDataMap.get(stock);
					Double yestHld = lastDaySbDataMap.get(stock);
					if(yestHld == null)
						yestHld = 0.0;
					
					Date T_2Date = allTradingCal.get(i - 2).getTime();
					logger.trace("   -- cal diff, stock=" + stock + " date=" + sdf_yyyy_MM_dd.format(T_2Date));
					Map<Date,ArrayList<Double>> thisStockPriceMap = priceDataMap.get(stock);
					if(thisStockPriceMap == null)
						logger.info("      -- thisStockPriceMap == null");
					logger.trace("        -- thisStockPriceMap.size=" + thisStockPriceMap.size());
					ArrayList<Double> thisStockPriceData = thisStockPriceMap.get(T_2Date);
					if(thisStockPriceData == null) {
						logger.info("      -- thisStockPriceData (T-2) == null");
						continue;
					}
					
					Double T_2Price = thisStockPriceData.get(2); //用 VWAP price
					Double notionalChg = (todayHld - yestHld) * T_2Price;
					
					FileWriter fw = fwMap.get(sdf_yyyyMMdd.format(todayDate));
					fw.write(stock + "," + notionalChg + "\n");
				}
				
				// ------- updating -------
				lastDaySbDataMap.clear();;
				lastDaySbDataMap.putAll(oneDaySbDataMap);
				oneDaySbDataMap.clear();
			}
			
			//-------- close all ------------
			Set<String> fwSet = fwMap.keySet();
			for(String stock : fwSet) {
				fwMap.get(stock).close();
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 计算每只股票southbound holding相对于历史的percentile
	 * 
	 * Z:\Mubing\stock data\southbound flow strategy - db\southbound chg percentile - by stock
	 */
	public static void calSBPercentile() {
		try {
			String outputPath_byStock = percentileDataRootPath;
			//String outputPath_byDate = MAIN_ROOT_PATH + "\\southbound chg percentile - by date\\";
			
			// first to use the PortfolioScreening's function to get southbound holding data for the past
			PortfolioScreening.getAllSbData(SOUTHBOUND_DATA_PATH);
			final int lookBackPeriod1 = 250;  // LBP1
			final int lookBackPeriod2 = 60;		// // LBP2
			
			// by stock
			for(String stock : PortfolioScreening.sbDataMap.keySet()) {
				logger.info("stock = " + stock);
				Map<Date,ArrayList<Double>> singleStock_DataMap = PortfolioScreening.sbDataMap.get(stock);
				ArrayList<Date> singleStock_AllDate = new ArrayList<Date>(singleStock_DataMap.keySet());
				if(singleStock_AllDate == null || singleStock_AllDate.size() <= 1 ) {
					continue;
				}
				Collections.sort(singleStock_AllDate);   // 从小到大排序
				ArrayList<Double> singleStock_HistPercentile = new ArrayList<Double>(); 
				ArrayList<Double> singleStock_LBP1Percentile = new ArrayList<Double>(); 
				ArrayList<Double> singleStock_LBP2Percentile = new ArrayList<Double>(); 
				
				String writePath = utils.Utils.addBackSlashToPath(outputPath_byStock) + stock + ".csv";
				FileWriter fw = new FileWriter(writePath);
				fw.write("Date,Percentile all hist,Percentile 250D,Percenetile 60D,today Chg,hist high,high low," + lookBackPeriod1 + "D high,"+ lookBackPeriod1 + "D low," + lookBackPeriod2 +"D high," + lookBackPeriod2 + "D low\n");
				
				final int dateSize = singleStock_AllDate.size();
				ArrayList<Double> hist_Chg = new ArrayList<Double>(); //存储过去的chg 
				ArrayList<Double> LBP1_Chg = new ArrayList<Double>(); //存储过去250天的chg 
				ArrayList<Double> LBP2_Chg = new ArrayList<Double>(); //存储过去60天的chg 
				
				ArrayList<Double> hist_high = new ArrayList<Double>(); //存储过去的high
				ArrayList<Double> hist_low = new ArrayList<Double>(); //存储过去的low
				ArrayList<Double> LBP1_high = new ArrayList<Double>(); //存储过去250天的high 
				ArrayList<Double> LBP1_low = new ArrayList<Double>(); //存储过去250天的low
				ArrayList<Double> LBP2_high = new ArrayList<Double>(); //存储过去60天的high 
				ArrayList<Double> LBP2_low = new ArrayList<Double>(); //存储过去60天的low
				
				Double histHigh = 0.0;
				Double histLow = 0.0;
				Double histHighLBP1 = 0.0;
				Double histLowLBP1 = 0.0;
				Double histHighLBP2 = 0.0;
				Double histLowLBP2 = 0.0;
				Double lastHolding = 0.0;
				Double tempChg0 = 0.0;  //存储第一个chg值
				Double tempChg1 = 0.0;
				for(int i = 0; i < dateSize; i++) {
					Date date = singleStock_AllDate.get(i);
					ArrayList<Double> data = singleStock_DataMap.get(date);
					Double holding = data.get(0);   // shares
					
					Double holdingChg = holding - lastHolding;
					hist_Chg.add(holdingChg);
					
					if(i == 0) {
						tempChg0 = holdingChg;
						singleStock_HistPercentile.add(-1.0);
						singleStock_LBP1Percentile.add(-1.0);
						singleStock_LBP2Percentile.add(-1.0);
						hist_high.add(-1.0);
						hist_low.add(-1.0);
						LBP1_high.add(-1.0);
						LBP1_low.add(-1.0);
						LBP2_high.add(-1.0);
						LBP2_low.add(-1.0);
					}
					
					if(i == 1) {
						tempChg1 = holdingChg;
						histHigh = Math.max(tempChg0, tempChg1);
						histLow = Math.min(tempChg0, tempChg1);
						singleStock_HistPercentile.add(-1.0);
						singleStock_LBP1Percentile.add(-1.0);
						singleStock_LBP2Percentile.add(-1.0);
						hist_high.add(-1.0);
						hist_low.add(-1.0);
						LBP1_high.add(-1.0);
						LBP1_low.add(-1.0);
						LBP2_high.add(-1.0);
						LBP2_low.add(-1.0);
					}
					if(i >= 2) {
						// hist
						Double percentile_hist = (holdingChg - histLow) / (histHigh - histLow);
						singleStock_HistPercentile.add(percentile_hist);
						if(holdingChg > histHigh)
							histHigh = holdingChg;
						if(holdingChg < histLow)
							histLow = holdingChg;
						hist_high.add(histHigh);
						hist_low.add(histLow);
						
						
						// 250D..
						Double percentile_LBP1 = 0.0;
						if(LBP1_Chg.size() == lookBackPeriod1) {
							histHighLBP1 = MyMath.max(LBP1_Chg);   //这里算出来的high和low还是没有包括今天的数据
							histLowLBP1 = MyMath.min(LBP1_Chg);
							
							percentile_LBP1 = (holdingChg - histLowLBP1) / (histHighLBP1 - histLowLBP1);
							singleStock_LBP1Percentile.add(percentile_LBP1);
							
							// updating...
							LBP1_Chg.remove(0);
							LBP1_Chg.add(holdingChg);
							LBP1_high.add(MyMath.max(LBP1_Chg));
							LBP1_low.add(MyMath.min(LBP1_Chg));
							
							
						}else {
							LBP1_Chg.add(holdingChg);
							singleStock_LBP1Percentile.add(-1.0);
							LBP1_high.add(-1.0);
							LBP1_low.add(-1.0);
						}
						
						// 60D....
						Double percentile_LBP2 = 0.0;
						if(LBP2_Chg.size() == lookBackPeriod2) {
							histHighLBP2 = MyMath.max(LBP2_Chg);
							histLowLBP2 = MyMath.min(LBP2_Chg);
							
							percentile_LBP2 = (holdingChg - histLowLBP2) / (histHighLBP2 - histLowLBP2);
							singleStock_LBP2Percentile.add(percentile_LBP2);
							
							// updating...
							LBP2_Chg.remove(0);
							LBP2_Chg.add(holdingChg);
							LBP2_high.add(MyMath.max(LBP2_Chg));
							LBP2_low.add(MyMath.min(LBP2_Chg));
							
						}else {
							LBP2_Chg.add(holdingChg);
							singleStock_LBP2Percentile.add(-1.0);
							LBP2_high.add(-1.0);
							LBP2_low.add(-1.0);
						}
						
						
						
						
						
					} // end of if(i >= 2)
					
					lastHolding = holding;
					
				}  // end of for(int i = 0; i < dateSize; i++) {
				
				// write files 从最近的往前开始写
				for(int i = dateSize-1; i >= 0; i--) {
					Date date = singleStock_AllDate.get(i);
					fw.write(sdf_yyyy_MM_dd.format(date) 
							+ "," + singleStock_HistPercentile.get(i) 
							+ "," + singleStock_LBP1Percentile.get(i) 
							+ "," + singleStock_LBP2Percentile.get(i) 
							+ "," +  hist_Chg.get(i) 
							+ "," +  hist_high.get(i) 
							+ "," +  hist_low.get(i) 
							+ "," +  LBP1_high.get(i) 
							+ "," +  LBP1_low.get(i) 
							+ "," +  LBP2_high.get(i) 
							+ "," +  LBP2_low.get(i) 
							+ "\n");
				}
				fw.close();
				
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 下载southbound和northbound的数据
	 */
	public static void downloadSBData() {
		try {
			Date lastTrdDate = utils.Utils.getRefDate(new Date(), allMMATradingDate, -1);
			String dateStr_yyyyMMdd = sdf_yyyyMMdd.format(lastTrdDate);
			//dateStr_yyyyMMdd = "20180404";
			
			String southboundDateFormat = "yyyy-MM-dd";
			SimpleDateFormat sdf_2 = new SimpleDateFormat (southboundDateFormat); 
			String dateStr_2 = sdf_2.format(lastTrdDate);
			//dateStr_2 = "2018-04-04";
			
			// ---------- southbound ------------
			String shPath = southboundDataRootPath + "\\sh";
			String szPath = southboundDataRootPath + "\\sz";
			String outputPath = southboundDataRootPath + "\\combined";
			DataDownloader.FILE_OUTPUT_PATH = southboundDataRootPath;
			
			logger.info(" -Southbound data download date=" + dateStr_2);
			DataDownloader.dataDownloader(dateStr_2, dateStr_2, southboundDateFormat, true, true);
			DataCombiner.dataCombiner(shPath, szPath, outputPath);
			
			// ---------- northbound ------------
			logger.info(" -Northbound dateStr_yyyyMMdd=" + dateStr_yyyyMMdd);
			webDownLoadHKEX.NorthboundHolding.downloader(dateStr_yyyyMMdd,dateStr_yyyyMMdd,"yyyyMMdd");
			webDownLoadHKEX.NorthboundHolding.combiner(dateStr_yyyyMMdd,dateStr_yyyyMMdd,"yyyyMMdd");
		}catch(Exception e) {
			e.printStackTrace();
		}
	}

}
