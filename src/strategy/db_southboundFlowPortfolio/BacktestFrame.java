package strategy.db_southboundFlowPortfolio;

import java.io.File;
import java.io.FileWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import backtesting.backtesting.Backtesting;
import backtesting.portfolio.Portfolio;
import backtesting.portfolio.PortfolioOneDaySnapshot;
import backtesting.portfolio.Underlying;
import utils.XMLUtil;

public class BacktestFrame {
	public static Logger logger = Logger.getLogger(BacktestFrame.class);
	
	// ---------- controlling variables -----------
	public static String dateFormat = "";
	public static SimpleDateFormat sdf;
	public static SimpleDateFormat sdf2 ;
	public static SimpleDateFormat sdf_yyyyMMdd ; 
	public static Date startDate ;
	public static String startDateStr = "";
	public static Date endDate ;
	public static String endDateStr ="";
	public static Double initialFunding = 1000000.0;
	
	public static boolean isFixedAmount = false;    	// 是否每只股票买入固定金额, let's make it true all the time
	public static Double eachStockValue = 1000000.0;    // （只在isFixedAmount为true的时候有用）每只股票买多少钱，只适用于每只股票按照固定金额买的情况
	public static boolean isRebalanceEachTime = false;  // 是否在每次rebalancing date的时候进行rebalance，
															//如果是在isFixedAmount=true时，则每次rebal回到eachStockValue
															//如果是在isFixedAmount=false时，则每次rebal回到整个portfolio的percentage
	public static Double eachStockValueRebalanceThreshold = 0.01;  // 每只股票只有在超出了eachStockValue * 这个threshold的时候才进行rebal
	
	public static double tradingCost = 0.0;
	
	public static String portFilePath="";    // 最终输出的root path
	public static String allSbDataPath = "Z:\\Mubing\\stock data\\HK CCASS - WEBB SITE\\southbound\\combined";  // 存储所有southbound data的文件夹
	public static String allPriceDataPath = "Z:\\Mubing\\stock data\\stock hist data - webb";  //存储所有stock price的data的文件夹
	public static String notionalChgDataRootPath = "Z:\\Mubing\\stock data\\southbound flow strategy - db\\";
	public static String percentileDataRootPath = "Z:\\Mubing\\stock data\\southbound flow strategy - db\\southbound chg percentile - by stock\\";
	public static String executionOutputFilePath = "";
	
	public static ArrayList<Calendar> allTradingDate = new ArrayList<Calendar> ();  
	public static String allTradingDatePath = utils.PathConifiguration.ALL_TRADING_DATE_PATH_HK;
	public static boolean isNormalSorting = true; //normal sorting - rank with higher rank in the front
	
	// ---------------- factors --------------
	public static double rankingStrategy = 1;
	public static double avgDailyValueThreshHold_USD = 7000000.0;
	public static int topNStocks = 20;
	public static double minInflowPct = 0.7;   // factor 4  在两次调仓之间，至少有这个比例的日子的flow是流入的
	
	public static int stockUniverse = 1;
	/*
	 * 1 - south bound 
	 * 2 - HSI
	 * 3 - HSCEI
	 * 4 - HSCEI + HSI
	 */
	
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
	public static int earlyUnwindStrategy  = 1;
	/*
	 * 1 - 正常，不会提前卖出某只股票
	 * 2 - 如果一只股票的southbound holding连续5天减少，则提前全部卖掉，并不再补充其他股票
	 * 3 - 如果一只股票的southbound holding连续三天减少，则提前卖掉一半，并不再补充其他股票
	 */
	public static boolean isToCalNotional = true;
	
	public static int topNStocks_mode = 1;    	// 1 - 正常
												// 2 - buffer模式，即需要买入股票的排名进入“入选股票线”才买入，但是已经买入的股票只有跌出“剔除股票线”才卖出。在这种情况下，每只股票的持仓相当于1/topNStocks_bufferZone_out的比例，即有可能不是满仓
	public static int topNStocks_bufferZone_in = 15;   // 入选股票线
	public static int topNStocks_bufferZone_out = 20;  // 剔除股票线
	
	public static double rankingStrategy6_1_threshold = 1.0;   // 当inflow的percentile超过历史数据的某个threshold之后，才买入
	public static int rankingStrategy6_1_holdDay = 10;
	
	//public static 
	
	// ----------- 运行中需要用到的variables --------------
	public static boolean isOutputDailyCCASSChg = true; // 是否输出每日southbound的CCASS的change
														// 必须设置为true，不然会影响earlyUnwindStrategy
	public static int rank6Mode = 0;
	
	public static ArrayList<String> rebalDateArr = new ArrayList<String>();
	/*public static int rebalStartInd =-1;
	public static int rebalEndInd =-1;
	public static ArrayList<ArrayList<StockSingleDate>> allPortfolioScreeningData = new ArrayList<ArrayList<StockSingleDate>> (); // 存储每次运行portfolio screening选出的股票
	public static ArrayList<ArrayList<String>> data = new ArrayList<ArrayList<String>> ();   // 存储每个rebalancing date选出的股票
	*/
	
	/*
	 * this variable stores External signals for selling
	 * Date - date, 
	 * String - stock code, 
	 * ArrayList<Double> - {Type: 1.0 - cash, 2.0 - quantity
	 * 						Value: 	
	 * 									type 1.0: sell certain amt of cash value stocks (e.g. if stock price = 10, Value = 5000, then we will sell 500 shares, subject to further checks)
	 * 									type 2.0: buy certain amt of shares (e.g. Value = 10000, will sell 10000 shares)
	 * 								
	 * 						}
	 */
	public static Map<Date, Map<String, ArrayList<Double>>> externalSignals_sell = new HashMap<Date, Map<String, ArrayList<Double>>>();
	/*
	 * this variable stores External signals for buying
	 * Date - date, 
	 * String - stock code, 
	 * ArrayList<Double> - {Type: 1.0 - cash, 2.0 - quantity
	 * 						Value: 	
	 * 									type 1.0: buy certain amt of cash value stocks (e.g. if stock price = 10, Value = 5000, then we will buy 500 shares)
	 * 									type 2.0: buy certain amt of shares (e.g. Value = 10000, will buy 10000 shares)
	 * 								
	 * 						}
	 */
	public static Map<Date, Map<String, ArrayList<Double>>> externalSignals_buy = new HashMap<Date, Map<String, ArrayList<Double>>>();
	public static LinkedHashMap<Date, ArrayList<String>> stockPicks = new LinkedHashMap<Date, ArrayList<String>>();  // 记录每个rebal date的stock picks
	
	public static void init() {
		try {
			allTradingDate = utils.Utils.getAllTradingDate(allTradingDatePath);
			
			if(dateFormat.equals(""))
				dateFormat = "yyyyMMdd";
			sdf = new SimpleDateFormat (dateFormat);
			sdf2 = new SimpleDateFormat("yyyyMMdd HHmmss"); 
			sdf_yyyyMMdd = new SimpleDateFormat("yyyyMMdd"); 
			
			if(startDateStr.equals("") || endDateStr.equalsIgnoreCase("")) {
				startDate = sdf.parse("20160704");
				endDate = sdf.parse("20171027");
			}else {
				startDate = sdf.parse(startDateStr);
				endDate = sdf.parse(endDateStr);
			}
			
			if(portFilePath.equals(""))
				portFilePath = "D:\\stock data\\southbound flow strategy - db\\" 
						+ sdf2.format(new Date()) + " - idea3 - bactesting四 - 15stocks";    // 最终输出的root path
			
			File f = new File(portFilePath);
			if(!f.exists())
				f.mkdir();
			
			PortfolioScreening.avgDailyValueThreshHold_USD =  avgDailyValueThreshHold_USD;  // 每天的平均成交额需要超过这个数才能入选
			PortfolioScreening.topNStocks = topNStocks;   // 每次选多少只股票进行买入卖出
			PortfolioScreening.stockUniverse = stockUniverse;
			
			rebalDateArr = getRebalDate(startDate, endDate, dateFormat, rebalancingStrategy,allTradingDate);
			
			PortfolioScreening.outputPath = portFilePath;
			if(PortfolioScreening.allTradingDate == null || PortfolioScreening.allTradingDate.size() == 0)
				PortfolioScreening.getAllTradingDate();
			if(PortfolioScreening.sbDataMap == null || PortfolioScreening.sbDataMap.size() == 0) {
				
				PortfolioScreening.getAllSbData(allSbDataPath);
			}
			if(PortfolioScreening.SbChgPercentileDataMap == null || PortfolioScreening.SbChgPercentileDataMap.size() == 0)
				PortfolioScreening.getSbChgPercentileData(percentileDataRootPath);
			
			/*
			Calendar rebalStart = Calendar.getInstance();
			rebalStart.setTime(sdf_yyyyMMdd.parse(rebalDateArr.get(0)));
			int rebalStartInd = allTradingDate .indexOf(rebalStart);
			if(PortfolioScreening.priceDataMap == null || PortfolioScreening.priceDataMap.size() == 0)
				PortfolioScreening.getAllPriceData(allPriceDataPath, allTradingDate.get(rebalStartInd - 30).getTime());
			*/
			
			PortfolioScreening.oneMonthBeforeDays = 20;
			PortfolioScreening.isToCalNotional = isToCalNotional;
			
			executionOutputFilePath = utils.Utils.addBackSlashToPath(portFilePath) + "executedOrders.csv";
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * 得到一个ArrayList<Object>
	 * 其中第一个数据是ArrayList<Date>形式，存储了每次需要rebal的日期
	 * 其中第二个数据是ArrayList<ArrayList<ArrayList<Object>>>形式，存储了一次backtesting需要的在每个rebalancing date的股票数据，返回的数据形式如下：
	 * {
	 * 	{
	 * 		{stock1, stock2, ...},
	 * 		{direction1, direction2, ...},   // 1 - buy, -1 - sell
	 * 		{weighting1, weighting2, ...},	 // positive #: num of shares to buy/sell; negative #: percentage to buy/sell (e.g. if buy, -10 represents buying 10% of total portfolio value; if sell, -10 represents selling 10% of total holding value, that is, if holding 10000 shares, -10 means selling 1000 shares
	 * 		{price1, price2, ...}
	 * 		{comment1, comment2, ...}
	 * 	}
	 * 	,
	 * 	{
	 * 		{stock1, stock2, ...},
	 * 		{direction1, direction2, ...},
	 * 		{weighting1, weighting2, ...},
	 * 		{price1, price2, ...}
	 * 		{comment1, comment2, ...}
	 * 	}
	 * 	...
	 * }
	 * 是一个3层的ArrayList。。。
	 * @return
	 */
	public static ArrayList<Object> getRebalancingSelection() {
		ArrayList<ArrayList<ArrayList<Object>>> data = new ArrayList<ArrayList<ArrayList<Object>>> ();   //用来存储每次rebal选出来的股票
		ArrayList<Date> rebalDates =  new ArrayList<Date> ();   //所有可能的rebalDates，不只是rebalDateArr，有可能有提前退出的股票
		ArrayList<Object> toReturn = new ArrayList<Object>(); 
		FileWriter fw_stock_ranking;
		
		try {
			Calendar rebalStart = Calendar.getInstance();
			rebalStart.setTime(sdf_yyyyMMdd.parse(rebalDateArr.get(0)));
			int rebalStartInd = allTradingDate .indexOf(rebalStart);
			
			Calendar rebalEnd = Calendar.getInstance();
			rebalEnd.setTime(sdf_yyyyMMdd.parse(rebalDateArr.get(rebalDateArr.size() - 1)));
			int rebalEndInd = allTradingDate.indexOf(rebalEnd);
			
			if(rebalEndInd  == -1 || rebalStartInd == -1) {
				logger.error("Rebalancing start date or end date is not a trading date! Cease running");
				return null;
			}
			
			int rebalCount = 0;
			ArrayList<ArrayList<StockSingleDate>> allPortfolioScreeningData = new ArrayList<ArrayList<StockSingleDate>>(); 
			int max_allPS_Data = 30; // max size for allPortfolioScreeningData
			
			// -------------- now start... -------------------
			//FileWriter fw_dailyCCASSChg ; // 仅仅是为了初始化
			Map<String, Map<Date, Double>> dailyCCASSChg_map = new HashMap<String, Map<Date, Double>>();
			Set<Date> dailyCCASSChg_allDates = new  HashSet<>();;
			
			int dayCalStart = 20;
			int daysBetweenRelancingDate = 0;
			PortfolioScreening.getAllOsData(PortfolioScreening.outstandingFilePath, allTradingDate.get(rebalStartInd-dayCalStart).getTime());
			ArrayList<StockSingleDate> stocksSelected = new ArrayList<StockSingleDate> ();  // //记录每个rebalancing date需要选出的股票
			
			//ArrayList<String> stocksToBuy_str = new ArrayList<String>();  //记录每个rebalancing date需要选出的股票
			LinkedHashMap<String, Double> holdingSharesLastTime = new LinkedHashMap<String, Double>();   // 记录上一次的选出的股票和股数
			
			for(int i = rebalStartInd-dayCalStart; i <= rebalEndInd; i++) {  
				long startTime = System.currentTimeMillis();
				
				ArrayList<String> selectedLastTime_stock = new ArrayList<String> (holdingSharesLastTime.keySet());  // 储存上一次的股票
				
				
				daysBetweenRelancingDate++;
				String todayDateStr = sdf_yyyyMMdd.format(allTradingDate.get(i).getTime());
				Date todayDate = allTradingDate.get(i).getTime();
				//logger.info("\tScreening date = " + todayDate);
				logger.info("======= Stocks Screening - " + todayDateStr + " ===============");
				
				PortfolioScreening.oneMonthBeforeDays = 1;
				ArrayList<StockSingleDate> todaySel = PortfolioScreening.assignValue_singleDate(todayDateStr, "yyyyMMdd"); //对每只股票进行赋值
				logger.debug("\t\ttodaySel .size() = " + todaySel .size());
				
				// 每天按照rank6进行排序，存储在dummyRank6
				if(rankingStrategy >= 6 && rankingStrategy < 7) {
					for(int j = 0; j < todaySel.size(); j++) {
						StockSingleDate s = todaySel.get(j);
						if(rank6Mode == 0)
							s.dummyRank6 = todaySel .size() * s.SB_chg_hist_percentile;
						if(rank6Mode == 1)
							s.dummyRank6 = todaySel .size() * s.SB_chg_250D_percentile;
						if(rank6Mode == 2)
							s.dummyRank6 = todaySel .size() * s.SB_chg_60D_percentile;
						todaySel.set(j, s);
					}
				}
				
				
				if(allPortfolioScreeningData.size() >= max_allPS_Data) {   // to keep allPortfolioScreeningData not so long
					allPortfolioScreeningData.remove(0);
				}
				allPortfolioScreeningData.add(todaySel );
				
				int todayInd = allPortfolioScreeningData.size() - 1;
				
				if(todayInd > 0) {  //比较昨天south bound的数据，update filter
					
					ArrayList<StockSingleDate> lastDateSel = allPortfolioScreeningData.get(todayInd - 1);
					// 将昨天的数据转换为map
					Map<String, StockSingleDate> lastDateSel_map = new HashMap()	;
					for(int k = 0; k < lastDateSel .size(); k++) {
						StockSingleDate thisStock = lastDateSel.get(k);
						lastDateSel_map.put(thisStock.stockCode, thisStock);
					}
					
					//计算今天每只股票的southbound的增加值
					for(int k = 0; k < todaySel.size(); k++) {
						StockSingleDate thisStock = todaySel.get(k);
						StockSingleDate thisStock_yest = lastDateSel_map.get(thisStock.stockCode);
						
						// 记录当天southbound的share的变动
						if(thisStock_yest == null) { // 找不到昨天的数据
							thisStock.dummy6 = 1.0;  // 用dummy6来存储当天southbound的share的变动
						}else {
							Double sb_yesterday = thisStock_yest.SB_today_holding;
							Double sb_today = thisStock.SB_today_holding;
							
							if(sb_today >= sb_yesterday) {
								thisStock.dummy6 = 1.0;
							}else {
								thisStock.dummy6 = -1.0;
							}
						}
						todaySel.set(k, thisStock);
					
						
						// 输出每日的chg
						if(isOutputDailyCCASSChg) {   
							Double chg = 0.0;
							if(thisStock_yest != null && thisStock != null) {
								Double sb_yesterday =  0.0;
								if(thisStock_yest != null && thisStock_yest.SB_today_holding != null)
									sb_yesterday = thisStock_yest.SB_today_holding;
								Double sb_today = 0.0 ;
								if(thisStock != null && thisStock.SB_today_holding != null)
									sb_today = thisStock.SB_today_holding;
								chg = sb_today / thisStock.osShares_freefloat_today 
										- sb_yesterday/ thisStock_yest.osShares_freefloat_today;
							
							
								Map<Date, Double> thisStock_chg_date = dailyCCASSChg_map.get(thisStock_yest.stockCode);
								if(thisStock_chg_date == null)
									thisStock_chg_date = new HashMap<Date, Double>();
								
								Date thisDate = allTradingDate.get(i).getTime();
								thisStock_chg_date.put(thisDate, chg);
								dailyCCASSChg_map.put(thisStock_yest.stockCode, thisStock_chg_date);
								
								dailyCCASSChg_allDates.add(thisDate);
							}
						}
						
					}
					
					allPortfolioScreeningData.set(todayInd, todaySel);
					
					// ---------- 执行early Unwind Strategy ----------
					if(earlyUnwindStrategy == 2 || earlyUnwindStrategy == 3) {
						ArrayList<Object> stocksToSell_str = new ArrayList<Object>(); //此时的stocksToBuy_str还存储着上次rebalance要买的股票，这恰好是本次要卖的股票
						ArrayList<Object> direction_sell = new ArrayList<Object>();
						ArrayList<Object> weighting_sell = new ArrayList<Object>();
						ArrayList<Object> price_sell = new ArrayList<Object>();
						
						final int stocksToBuySize = holdingSharesLastTime.size();
						Date DayT = allTradingDate.get(i).getTime();  // day T
						Date DayT_1 = allTradingDate.get(i-1).getTime();  // day T-1
						Date DayT_2 = allTradingDate.get(i-2).getTime();  // day T-2
						Date DayT_3 = allTradingDate.get(i-3).getTime();  // day T-3
						Date DayT_4 = allTradingDate.get(i-4).getTime();  // day T-4
						
						for(int j = 0; j < stocksToBuySize; j++) {
							String stock = selectedLastTime_stock.get(j);
							Map<Date, Double> stockData = dailyCCASSChg_map.get(stock);
							if(stockData != null) {
								Double chgT = stockData.get(DayT);
								Double chgT_1 = stockData.get(DayT_1);
								Double chgT_2 = stockData.get(DayT_2);
								Double chgT_3 = stockData.get(DayT_3);
								Double chgT_4 = stockData.get(DayT_4);
								
								if(chgT != null && chgT_2 != null & chgT_1 != null && chgT_3 != null && chgT_4 != null 
										&& chgT < 0 && chgT_1 < 0 && chgT_2 < 0 && chgT_3 < 0 && chgT_4 < 0) {  // 连续多天净卖出，满足early unwind的条件
									logger.info("selectedLastTime=" + holdingSharesLastTime);
									
									stocksToSell_str.add(stock);
									direction_sell.add(-1);
									if(earlyUnwindStrategy == 2) {
										weighting_sell.add(-100.0);
									}
									if(earlyUnwindStrategy == 3) {
										weighting_sell.add(-50.0);
									}
									price_sell.add(
											Double.parseDouble(stockPrice.DataGetter.getStockDataField(stock,stockPrice.DataGetter.StockDataField.adjclose, todayDateStr, "yyyyMMdd"))
											);
									logger.info("Early unwind (strategy " + earlyUnwindStrategy + ")! stock=" + stock);
								}
							}
						}
						
						if(stocksToSell_str.size() > 0) {
							logger.info("       todaydate=" + todayDateStr);
							ArrayList<ArrayList<Object>> thisRebalData = new ArrayList<ArrayList<Object>>(); 
							thisRebalData.add(stocksToSell_str);
							thisRebalData.add(direction_sell);
							thisRebalData.add(weighting_sell);
							thisRebalData.add(price_sell);
							
							data.add(thisRebalData);
							rebalDates.add(sdf_yyyyMMdd.parse(todayDateStr));
						}
						
					}
					// ---------- 执行early Unwind Strategy结束----------

					// *********** rebalancing date *************
					if(todayDateStr.equals(rebalDateArr.get(rebalCount))) {
						daysBetweenRelancingDate = 5; // rolling 的概念(if don't want rolling, just comment this line),
														// daysBetweenRelancingDate应该小于max_allPS_Data
						logger.info("\t\tToday is a rebalancing date! daysBetweenRelancingDate=" + daysBetweenRelancingDate);
						
						Map<String, StockSingleDate> rebalStock_map = new HashMap<String, StockSingleDate>();  // 想要在rebal那天将每只股票的rank都列出来
						for(int j = 0; j < todaySel .size(); j++) { // 先将rebalStock的框架搭出来
							StockSingleDate stock = todaySel.get(j);
							//stock.dummy3 = stock.dummy1;  // dummy1是按照freefloat的排序排出来的
							//stock.dummy4 = stock.dummy2;  // dummy1是按照3M ADV的排序排出来的
							//stock.dummy5 = 1.0;    // 有效天数
							rebalStock_map.put(stock.stockCode, stock);
						}
						
						for(int j = 0; j < daysBetweenRelancingDate; j++) { // 寻找最近20天的数据，计算这段时间内的平均ranking
							ArrayList<StockSingleDate> rebal_thisDateSel = allPortfolioScreeningData.get(allPortfolioScreeningData.size() - 1 - j) ;
							//String rebal_thisDate = sdf_yyMMdd.format(allTradingDate.get(i-j).getTime());
							for(int k = 0; k < rebal_thisDateSel.size(); k++) {
								StockSingleDate thisStock = rebal_thisDateSel.get(k);
								StockSingleDate findStock =rebalStock_map.get(thisStock.stockCode);
								if(findStock != null) { // 只考虑能找到的情况
									findStock.dummy3 = findStock.dummy3 + thisStock.dummy1; // dummy1是每只股票在当天的按照southbound的变化除以freefloat的排名，dummy3来存储20天内这个排名的总值  
									findStock.dummy4 = findStock.dummy4 + thisStock.dummy2; // dummy2是每只股票在当天的按照southbound的变化除以3M ADV的排名，dummy4来存储20天内这个排名的总值  
									findStock.dummy5 = findStock.dummy5 + 1;  // dummy5来存储有多少天是有效的
									findStock.dummyRank6Total = findStock.dummyRank6Total + findStock.dummyRank6;
									
									
									if(findStock.SB_notional_chg == null) {
										//logger.info("   notional chg null! stock=" + findStock.stockCode + " ");
										findStock.SB_notional_chg = 0.0;
									}
									if(findStock.SB_cum_notional_chg == null) {
										findStock.SB_cum_notional_chg = 0.0;
									}
									if(thisStock.SB_notional_chg == null)
										thisStock.SB_notional_chg = 0.0;
									
									findStock.SB_cum_notional_chg = findStock.SB_cum_notional_chg 
											+ thisStock.SB_notional_chg;
									rebalStock_map.put(thisStock.stockCode, findStock);
									
									Date testDate = sdf_yyyyMMdd.parse("20170801");
									if(false && findStock.stockCode.equals("700") && testDate.equals(sdf_yyyyMMdd.parseObject(todayDateStr))) {
										logger.info("    date=" + sdf_yyyyMMdd.format(thisStock.cal.getTime()) + " cum notional =" + findStock.SB_cum_notional_chg);
									}
								}
							}
						}
						
						// 上面是做daily的ranking，这里只做month end和month begin的ranking
						PortfolioScreening.oneMonthBeforeDays = daysBetweenRelancingDate;
						ArrayList<StockSingleDate> todaySel_20 = PortfolioScreening.assignValue_singleDate(todayDateStr, "yyyyMMdd");
						Map<String, StockSingleDate> todaySel_20_map = new HashMap<String, StockSingleDate>();
						for(int k = 0; k < todaySel_20.size(); k++) {
							StockSingleDate thisStock = todaySel_20.get(k);
							StockSingleDate findStock =rebalStock_map.get(thisStock.stockCode);
							if(findStock != null) { // 只考虑能找到的情况
								findStock.rank1 = thisStock.dummy1;
								findStock.rank2 = thisStock.dummy2;
								rebalStock_map.put(thisStock.stockCode, findStock);
								
								todaySel_20_map.put(findStock.stockCode, thisStock);
							}
						}
						
						// score assigning
						ArrayList<StockSingleDate> todaySel2 = new ArrayList<StockSingleDate>();
						todaySel2.addAll(rebalStock_map.values());
						logger.debug("\t\trebalStock_map.values().size() = " + rebalStock_map.values().size());
						logger.debug("\t\ttodaySel2.size() = " + todaySel2 .size());
						
						// 按照SB_cum_notional_chg进行排序，然后对rank5进行赋值
						for(StockSingleDate singleStock : todaySel2) {
							singleStock.sorting_indicator = singleStock.SB_cum_notional_chg;
						}
						Collections.sort(todaySel2, StockSingleDate.getComparator(1));   // 升序排列，即cum notional chg越低的越靠前，这样cum notional chg越高的得分就越高
						for(int kk = 0; kk < todaySel2.size(); kk++) {
							StockSingleDate singleStock =  todaySel2.get(kk);
							singleStock.rank5 = (double) kk;
						}
						
						// 将每只股票最近20天的ranking加总，然后求平均值，再排序
						for(int j = 0; j < todaySel2 .size(); j++) {
							StockSingleDate thisStock = todaySel2.get(j);
							thisStock.rank3 = thisStock.dummy3 / thisStock.dummy5;
							thisStock.rank4 = thisStock.dummy4 / thisStock.dummy5;
							thisStock.rank6 = thisStock.dummyRank6Total / thisStock.dummy5;
							
							if(rankingStrategy == 1 || rankingStrategy == 7)  // ranking 7依赖 ranking 1
								thisStock.sorting_indicator = (0
										+ thisStock.rank1 
										+ thisStock.rank2 
										+ thisStock.rank3 
										+ thisStock.rank4
										)/4;   // 这个句子决定了最后的排序
							if(rankingStrategy == 2)
								thisStock.sorting_indicator = (0
										+ thisStock.rank1 
										+ thisStock.rank2 
										)/2;   // 这个句子决定了最后的排序
							if(rankingStrategy == 3)
								thisStock.sorting_indicator = (0
										+ thisStock.rank1 
										+ thisStock.rank4
										)/2;   // 这个句子决定了最后的排序
							if(rankingStrategy == 4)
								thisStock.sorting_indicator = (0
										+ thisStock.rank2 
										+ thisStock.rank3 
										)/2;   // 这个句子决定了最后的排序
							if(rankingStrategy == 5) {
								thisStock.sorting_indicator = thisStock.rank5;
							}
							if(rankingStrategy >= 6.0 && rankingStrategy < 7.0) {
								thisStock.sorting_indicator = thisStock.rank6;
							}
							thisStock.rankFinal = thisStock.sorting_indicator;
						}
						Collections.sort(todaySel2, StockSingleDate.getComparator(-1));  // 降序排列
						for(int k = 0; k < todaySel2.size(); k++) {
							StockSingleDate singleStock = todaySel2.get(k);
							singleStock.dummyRankingStrategy1Rank = (double) k;
							todaySel2.set(k, singleStock);
						}
						
						// update rank7
						if(rankingStrategy == 7) {
							//取出上一个rebal date的每只股票的rank7
							ArrayList<StockSingleDate> rebal_lastDateSel = allPortfolioScreeningData.get(allPortfolioScreeningData.size() - 1 - daysBetweenRelancingDate) ;
							Map<String, Double> lastDateRank7_map = new HashMap<String, Double>();
							for(StockSingleDate singleStock : rebal_lastDateSel) {
								String stockCode = singleStock.stockCode;
								Double rankingStrategy1Rank = singleStock.dummyRankingStrategy1Rank;
								lastDateRank7_map.put(stockCode, rankingStrategy1Rank);
							}
							
							//计算这个rebal date的rank7
							final int  tempSize = todaySel2 .size();
							for(int j = 0; j < tempSize; j++) {  // 此时的todaySel2是已经按照ranking strategy 1排过序的，排名越靠前说明越是ranking strategy 1 的picks
								StockSingleDate thisStock = todaySel2.get(j);
								thisStock.dummyRankingStrategy1Rank = (double) j;
								
								Double lastDateRank7 = lastDateRank7_map.get(thisStock.stockCode);
								if(lastDateRank7 == null) {
									thisStock.sorting_indicator = -1000.0;
								}else {
									thisStock.sorting_indicator = lastDateRank7 - thisStock.dummyRankingStrategy1Rank;
								}
								thisStock.dummyRank7RankDiff = thisStock.sorting_indicator;
								todaySel2.set(j, thisStock);
							}
							
													
							Collections.sort(todaySel2, StockSingleDate.getComparator(-1));  // 降序排列
							
							for(int j = 0; j < todaySel2 .size(); j++) {
								StockSingleDate thisStock = todaySel2.get(j);
								thisStock.rank7 = (double) j;    //这个似乎是用不到的
							}
							allPortfolioScreeningData.set(allPortfolioScreeningData.size()-1, todaySel2);
							
						}
						
						
						if(rankingStrategy == 2) {
							for(int j = 0; j < todaySel2 .size(); j++) {
								StockSingleDate thisStock = todaySel2.get(j);
								thisStock.dummy1 = (double) j;
								thisStock.sorting_indicator = (
										 thisStock.rank3 
										+ thisStock.rank4
										)/2;   // 这个句子决定了最后的排序
							}
							Collections.sort(todaySel2, StockSingleDate.getComparator(-1));  // 降序排列
							
							for(int j = 0; j < todaySel2 .size(); j++) {
								StockSingleDate thisStock = todaySel2.get(j);
								thisStock.dummy2 = (double) j;
								thisStock.sorting_indicator = ( thisStock.dummy1 + thisStock.dummy2) /  2;
								thisStock.rankFinal = thisStock.sorting_indicator;
							}
							Collections.sort(todaySel2, StockSingleDate.getComparator(-1));  // 降序排列
						}
						if(!isNormalSorting)
							Collections.sort(todaySel2, StockSingleDate.getComparator(1));  // 升序排列
						
						
						// ------------ 还要进行filter ----------
						int lookbackDays1 = daysBetweenRelancingDate;
						int minDays1 = (int) Math.floor((double) lookbackDays1 * minInflowPct);
						
						int lookbackDays2 = 5;
						int minDays2 = (int) Math.floor((double) lookbackDays2 * minInflowPct);
						for(int j = 0; j < Math.max(lookbackDays1, lookbackDays2); j++) { // 回看过去20天
							ArrayList<StockSingleDate> thisDateSel = allPortfolioScreeningData.get(todayInd - j);
							
							Map<String, StockSingleDate> thisDateSel_map = new HashMap()	;
							for(int k = 0; k < thisDateSel.size(); k++) {
								StockSingleDate thisStock = thisDateSel.get(k);
								thisDateSel_map.put(thisStock.stockCode, thisStock);
							}
							
							for(int k = 0; k < todaySel2.size(); k++	) {   // 对今天的每只股票，查看过去某一天其southbound是否增加，如果增加，则filter2增加100
								StockSingleDate thisStock = todaySel2.get(k);
								StockSingleDate findStock = thisDateSel_map.get(thisStock.stockCode);
								
								if(findStock == null) {
									thisStock.filter2 = thisStock.filter2 + 100;
									if(j < lookbackDays2)  // 最近5天的情况
										thisStock.filter3 = thisStock.filter3 + 100;
								}else {
									if(findStock.dummy6 == 1.0) {
										thisStock.filter2 = thisStock.filter2 + 100; 
										if(j < lookbackDays2)  // 最近5天的情况
											thisStock.filter3 = thisStock.filter3 + 100;
									}
								}
								
								todaySel2.set(k, thisStock);
							}
						}// end of filter
						
						// 判断每只股票的符合条件的天数是否满足最小天数要求
						for(int k = 0; k < todaySel2.size(); k++) {
							StockSingleDate thisStock = todaySel2.get(k);
							
							if(thisStock.filter2 / (lookbackDays1 * 100) >= ((double) minDays1 / lookbackDays1)
									&& thisStock.filter3 / (lookbackDays2 * 100) >= ((double) minDays2 / lookbackDays2)) {
								thisStock.filter4 = 2.0;
							}else {
								thisStock.filter4 = -2.0;
							}
							
							// ranking strategy 6.1 (filter out those un-qualified)
							if(rankingStrategy == 6.1 && thisStock.rank6 < todaySel2.size() * rankingStrategy6_1_threshold)
								thisStock.filterRankingStrategy6_1 = -1.0;
							
						}
						// ---------------- filter 结束 ---------------
						
						
						// ------------ 选出要买的股票 ---------------
						if(topNStocks_mode == 1) {
							stocksSelected = PortfolioScreening.pickStocks_singleDate(todaySel2, false);
						}
						//ArrayList<StockSingleDate> pickedStocks_topNStocks_mode2 = new ArrayList<StockSingleDate> (); 
						if(topNStocks_mode == 2) {  // buffer mode
							int temp = PortfolioScreening.topNStocks;
							PortfolioScreening.topNStocks = topNStocks_bufferZone_out;
							stocksSelected = PortfolioScreening.pickStocks_singleDate(todaySel2, false);
							PortfolioScreening.topNStocks = temp;
						}
						
						if(rankingStrategy == 6.1) {  // rankingStrategy 6.1只使用 external signal进行买卖
							//externalSignals_buy;
							Date sellDate = allTradingDate.get(i+rankingStrategy6_1_holdDay).getTime();
							Map<String, ArrayList<Double>> nextDateMapSell = externalSignals_sell.get(sellDate);
							if(nextDateMapSell == null) {
								nextDateMapSell = new HashMap<String, ArrayList<Double>>();
							}
							
							Double totalCash = initialFunding * 0.001;
							
							Map<String, ArrayList<Double>> todayMapBuy = externalSignals_buy.get(todayDate);
							if(todayMapBuy == null)
								todayMapBuy = new HashMap<String, ArrayList<Double>>();
							
							for(StockSingleDate stock : stocksSelected) {
								String stockC = stock.stockCode;
								String priceToBuy = stockPrice.DataGetter.getStockDataField(
										stockC,
										stockPrice.DataGetter.StockDataField.adjclose, 
										todayDateStr, "yyyyMMdd");
								Double amt = totalCash / Double.parseDouble(priceToBuy)	;
								amt = 10*Math.floor(amt/10);
								
								ArrayList<Double> d = new ArrayList<Double>();
								d.add(2.0);
								d.add(amt);
								
								todayMapBuy.put(stockC, d);
								nextDateMapSell.put(stockC, d);
							}
							//还差sell的signal
							
							externalSignals_buy.put(todayDate, todayMapBuy);
							externalSignals_sell.put(sellDate, nextDateMapSell);
						}
					
						// --------------- 比较本次和上次的选股，找出需要卖掉或者买入的股票 ----------
						ArrayList<String> stockToSellThisTime = new ArrayList<String>(selectedLastTime_stock);
						ArrayList<String> stockToBuyThisTime = new ArrayList<String>();
						LinkedHashMap<String, Double> holdingSharesThisTime = new LinkedHashMap<String, Double>(holdingSharesLastTime);
						ArrayList<String> selectedThisTime_stock = new ArrayList<String>();
						//ArrayList<String> selectedLastTimeCopy = new ArrayList<String>(selectedLastTime_stock);
						for(StockSingleDate s : stocksSelected) {
							String sc = s.stockCode;
							selectedThisTime_stock.add(sc);
							
							if(selectedLastTime_stock.indexOf(sc) == -1) { // 本次选出的股票不在上次的股票池中，所以要买入
								stockToBuyThisTime.add(sc);
							}else {   // 如果本次选出的股票也在上次的股票池中，要保留这只股票，selectedLastTimeCopy最终剩下的股票是这次要卖的股票
								stockToSellThisTime.remove(sc);
							}
							
						}
						//stockToSellThisTime.addAll(selectedLastTimeCopy);
						//System.out.println("stockToSellThisTime=" + stockToSellThisTime);
						//System.out.println("stockToBuyThisTime=" + stockToBuyThisTime);
						
						// --------------- 生成买卖信号 ---------------
						// stocks to sell (原始信号)
						ArrayList<String> stocksToSell_str = new ArrayList<String>(stockToSellThisTime); //此时的stocksToBuy_str还存储着上次rebalance要买的股票，这恰好是本次要卖的股票
						ArrayList<Integer> direction_sell = new ArrayList<Integer>();
						ArrayList<Double> weighting_sell = new ArrayList<Double>();
						ArrayList<Double> price_sell = new ArrayList<Double>();
						ArrayList<String> comment_sell = new ArrayList<String>();
						final int size1 = stocksToSell_str.size();   
						for(int j = 0; j < size1; j++) {
							if(rankingStrategy == 6.1) {
								break;   //如果是6.1，不用按照结果的排序进行rebalance，而是固定hold一段时间
							}
							
							String thisStockToSell = stocksToSell_str.get(j);
							direction_sell.add(-1);
							weighting_sell.add(-100.0);
							
							String priceToSell = stockPrice.DataGetter.getStockDataField(
									thisStockToSell,
									stockPrice.DataGetter.StockDataField.adjclose, 
									todayDateStr, "yyyyMMdd");
							if(priceToSell == null || priceToSell.equals("") || priceToSell.length() == 0)
								priceToSell ="0";
							price_sell.add(Double.parseDouble(priceToSell));
							comment_sell.add("Kicked out");
							
							holdingSharesThisTime.remove(thisStockToSell);
						}
						
						// stocks to sell (external signals)
						Map<String, ArrayList<Double>> today_externalSignalsSell = externalSignals_sell.get(todayDate);
						ArrayList<String> stocksToSell_external_str = new ArrayList<String>();
						ArrayList<Integer> direction_external_sell = new ArrayList<Integer>();
						ArrayList<Double> weighting_external_sell = new ArrayList<Double>();
						ArrayList<Double> price_external_sell = new ArrayList<Double>();
						ArrayList<String> comment_external_sell = new ArrayList<String>();
						if(today_externalSignalsSell != null) {
							for(String tempStock : today_externalSignalsSell.keySet()) {
								ArrayList<Double> stock_signal =  today_externalSignalsSell.get(tempStock);
								Double type = stock_signal.get(0);
								Double value = stock_signal.get(1);
								
								String priceToSell = stockPrice.DataGetter.getStockDataField(
										tempStock,
										stockPrice.DataGetter.StockDataField.adjclose, 
										todayDateStr, "yyyyMMdd");
								Double price = 0.0;
								if(priceToSell == null || priceToSell.equals("")) {
									price = 1.0;
								}else {
									price =Double.parseDouble(priceToSell);
								}
								
								
								Double weighting  = 0.0;   //这里的weighting是卖的股数
								boolean isTypeCorrect = true;
								if(type == 1.0) { // cash
									weighting = value / price;  // get quantity to sell
								}else
									if(type == 2.0) {// quantity
										weighting = value;
									}else
										isTypeCorrect = false;
									
								if(isTypeCorrect) {
									direction_external_sell.add(-1);
									weighting_external_sell.add(weighting);
									price_external_sell.add(price);
									stocksToSell_external_str.add(tempStock);
									comment_external_sell.add("External sell");
									
									Double currentHolding = holdingSharesThisTime.get(tempStock);
									currentHolding -= weighting;
									if(currentHolding <= 0)
										holdingSharesThisTime.remove(tempStock);
									else	
										holdingSharesThisTime.put(tempStock, currentHolding);
								}
							}
						}
						logger.info("todayDate = " + todayDateStr + " stocksToSell_str=" + stocksToSell_str 
								+ " external sell=" + stocksToSell_external_str);
						
						// stocks to buy （买new position）
						 //ArrayList<String> lastHoldStocks_str = new ArrayList<String>(stockToSBuyThisTime);  
						ArrayList<String> stocksToBuy_str = new ArrayList<String>(stockToBuyThisTime);  //每个rebalancing date需要选出的股票
						ArrayList<Integer> direction_buy = new ArrayList<Integer>();
						ArrayList<Double> weighting_buy = new ArrayList<Double>();
						ArrayList<Double> price_buy = new ArrayList<Double>();
						ArrayList<String> comment_buy = new ArrayList<String>();
						int size2 = stocksToBuy_str.size();
						for(int j = 0; j < size2; j++) {   //size2其实就是topNStocks_bufferZone_out
							if(rankingStrategy == 6.1) {
								break;   //如果是6.1，不用按照结果的排序进行rebalance，而是固定hold一段时间
							}
							
							String thisStockToBuy = stocksToBuy_str.get(j);
							if(topNStocks_mode == 2 && j >= topNStocks_bufferZone_in) {  
								if(selectedLastTime_stock.indexOf(thisStockToBuy)  == -1) {
									continue;   //如果这只股票跌出了topNStocks_bufferZone_in的范围，就不买入
								}
							}
							
							direction_buy.add(1);
							
							Double priceToBuy = Double.parseDouble(stockPrice.DataGetter.getStockDataField(thisStockToBuy,stockPrice.DataGetter.StockDataField.adjclose, todayDateStr, "yyyyMMdd"));
							
							Double thisWeighting = 0.0;
							if(weightingStrategy == 1) {  // 每只股票都买固定的金额
								if(isFixedAmount) {
									thisWeighting = Math.floor(eachStockValue / priceToBuy);    // 每次买的股票股数
									holdingSharesThisTime.put(thisStockToBuy, thisWeighting);
									
								}else {
									thisWeighting  = -98.0 /  topNStocks;
								}
								weighting_buy.add(thisWeighting);
							}
							if(weightingStrategy == 2) {   // weightingStrategy 2似乎不太给力，可以先不考虑这里的code 
								//分四组，每组加起来权重分别是40%,30%,20%,10%
								final int g1 = (int) Math.floor(size2 * 0.25);
								final int g2 = (int) Math.floor(size2 * 0.5);
								final int g3 = (int) Math.floor(size2 * 0.75);
								if(j < g1) {
									thisWeighting = -39.75 / g1;
								}else if(j < g2) {
									thisWeighting = -29.75 / (g2 - g1);
								}
								else if(j < g3) {
									thisWeighting = -19.75 / (g3 - g2);
								}
								else {
									thisWeighting = -9.75 / (size2 - g3);
								}
								weighting_buy.add(thisWeighting);
							}
							
							price_buy.add(priceToBuy);
							comment_buy.add("New Buy");
						}
						
						// stocks to buy (external signals)
						Map<String, ArrayList<Double>> today_externalSignals_buy = externalSignals_buy.get(todayDate);
						ArrayList<String> stocksToBuy_external_str = new ArrayList<String>();
						ArrayList<Integer> direction_external_buy = new ArrayList<Integer>();
						ArrayList<Double> weighting_external_buy = new ArrayList<Double>();
						ArrayList<Double> price_external_buy = new ArrayList<Double>();
						ArrayList<String> comment_external_buy = new ArrayList<String>();
						if(today_externalSignals_buy != null) {
							for(String tempStock : today_externalSignals_buy.keySet()) {
								ArrayList<Double> stock_signal =  today_externalSignals_buy.get(tempStock);
								Double type = stock_signal.get(0);
								Double value = stock_signal.get(1);
								
								String priceToBuy = stockPrice.DataGetter.getStockDataField(
										tempStock,
										stockPrice.DataGetter.StockDataField.adjclose, 
										todayDateStr, "yyyyMMdd");
								Double price = Double.parseDouble(priceToBuy);
								
								Double weighting  = 0.0;   // num of shares
								boolean isTypeCorrect = true;
								if(type == 1.0) { // cash
									weighting = value / price;  // get quantity to sell
								}else
									if(type == 2.0) {// quantity
										weighting = value;
									}else
										isTypeCorrect = false;
									
								if(isTypeCorrect) {
									direction_external_buy.add(1);
									weighting_external_buy.add(weighting);
									price_external_buy.add(price);
									stocksToBuy_external_str.add(tempStock);
									comment_external_buy.add("External buy");
									
									Double currentHolding = holdingSharesThisTime.get(tempStock);
									currentHolding += weighting;
									if(currentHolding <= 0)
										holdingSharesThisTime.remove(tempStock);
									else	
										holdingSharesThisTime.put(tempStock, currentHolding);
								}
							}
						}
						
						logger.info("todayDate = " + todayDateStr + " stocksToBuy_str=" + stocksToBuy_str 
								+ " external buy=" + stocksToBuy_external_str);
						
						// 买卖正股完毕，进行rebalance
						if(isRebalanceEachTime) {
							for(String stock : selectedThisTime_stock) {
								if(stockToBuyThisTime.indexOf(stock) == -1) {  // 这次刚买的股票可以不rebal
									String priceStr = stockPrice.DataGetter.getStockDataField(
											stock,
											stockPrice.DataGetter.StockDataField.adjclose, 
											todayDateStr, "yyyyMMdd");
									
									Double price = 0.0;
									if(utils.Utils.isDouble(priceStr))
											price = Double.parseDouble(priceStr);
									if(price > 0.0) {  //进行rebalance
										Double diff = price * holdingSharesThisTime.get(stock) - eachStockValue;
										if(diff > eachStockValue * eachStockValueRebalanceThreshold) { //当前的市值大于fixed amt，要卖掉
											Double qty = Math.floor(diff / price);
											
											direction_external_sell.add(-1);
											weighting_external_sell.add(qty);
											price_external_sell.add(price);
											stocksToSell_external_str.add(stock);
											comment_external_sell.add("Rebalance Sell");
											
											Double currentHolding = holdingSharesThisTime.get(stock);
											currentHolding -= qty;
											if(currentHolding <= 0)
												holdingSharesThisTime.remove(stock);
											else	
												holdingSharesThisTime.put(stock, currentHolding);
										} 
										if(diff < -eachStockValue * eachStockValueRebalanceThreshold){  
											Double qty = Math.floor(-diff / price);
											
											direction_external_buy.add(1);
											weighting_external_buy.add(qty);
											price_external_buy.add(price);
											stocksToBuy_external_str.add(stock);
											comment_external_buy.add("Rebalance Buy");
											
											Double currentHolding = holdingSharesThisTime.get(stock);
											currentHolding += qty;
											if(currentHolding <= 0)
												holdingSharesThisTime.remove(stock);
											else	
												holdingSharesThisTime.put(stock, currentHolding);
										}
										
									}
									
								}
							}
						}
						
						//合并2个list
						ArrayList<Object> thisRebalStocks = new ArrayList<Object> (stocksToSell_str); 
						thisRebalStocks.addAll(stocksToSell_external_str);  // external sell
						thisRebalStocks.addAll(stocksToBuy_str);
						thisRebalStocks.addAll(stocksToBuy_external_str);  // external buy
						
						if(thisRebalStocks != null && thisRebalStocks.size() > 0) {   // 只有当今天有数据的时候才
							ArrayList<Object> thisRebalDirections = new ArrayList<Object> (direction_sell);
							thisRebalDirections.addAll(direction_external_sell);  // external sell
							thisRebalDirections.addAll(direction_buy);
							thisRebalDirections.addAll(direction_external_buy);  // external buy
							ArrayList<Object> thisRebalWeightings = new ArrayList<Object>(weighting_sell);
							thisRebalWeightings.addAll(weighting_external_sell); // external sell
							thisRebalWeightings.addAll(weighting_buy);
							thisRebalWeightings.addAll(weighting_external_buy); // external buy
							ArrayList<Object> thisRebalPrices = new ArrayList<Object>(price_sell);
							thisRebalPrices.addAll(price_external_sell); // external sell
							thisRebalPrices.addAll(price_buy);
							thisRebalPrices.addAll(price_external_buy); // external buy
							ArrayList<Object> thisRebalComment = new ArrayList<Object>(comment_sell);
							thisRebalComment.addAll(comment_external_sell); // external sell
							thisRebalComment.addAll(comment_buy);
							thisRebalComment.addAll(comment_external_buy); // external buy
									
							ArrayList<ArrayList<Object>> thisRebalData = new ArrayList<ArrayList<Object>>(); 
							thisRebalData.add(thisRebalStocks);
							thisRebalData.add(thisRebalDirections);
							thisRebalData.add(thisRebalWeightings);
							thisRebalData.add(thisRebalPrices);
							thisRebalData.add(thisRebalComment);
							
							data.add(thisRebalData);
							rebalDates.add(sdf_yyyyMMdd.parse(todayDateStr));
							
//							logger.info("Date = " + todayDateStr + " [Stocks] stocksToSell_str = " + stocksToSell_str.size() 
//								+ " [Stocks] stocksToSell_external_str=" + stocksToSell_external_str.size() 
//								+ " [Stocks] stocksToBuy_str=" + stocksToBuy_str.size() 
//								+ " [Stocks] stocksToBuy_external_str=" + stocksToBuy_external_str.size());
//							logger.info("[Direction] direction_sell = " + direction_sell.size() 
//								+ " [Direction] direction_external_sell=" + direction_external_sell.size() 
//								+ " [Direction] direction_buy=" + direction_buy.size() 
//								+ " [Direction] direction_external_buy=" + direction_external_buy.size());
//							logger.info("[Weighting] weighting_sell = " + weighting_sell.size() 
//								+ " [Weighting] weighting_external_sell=" + weighting_external_sell.size() 
//								+ " [Weighting] weighting_buy=" + weighting_buy.size() 
//								+ " [Weighting] weighting_external_buy=" + weighting_external_buy.size());
//							logger.info("[Price] price_sell = " + price_sell.size() 
//								+ " [Price] price_external_sell=" + price_external_sell.size() 
//								+ " [Price] price_buy=" + price_buy.size() 
//								+ " [Price] price_external_buy=" + price_external_buy.size());
//							logger.info("[Comment] comment_sell = " + comment_sell.size() 
//								+ " [Comment] comment_external_sell=" + comment_external_sell.size() 
//								+ " [Comment] comment_buy=" + comment_buy.size() 
//								+ " [Comment] comment_external_buy=" + comment_external_buy.size()
//									);
//							logger.info("thisRebalStocks="+thisRebalStocks.size() 
//								+ " thisRebalDirections=" + thisRebalDirections.size()
//								+ " thisRebalWeightings=" + thisRebalWeightings.size()
//								+ " thisRebalPrices=" + thisRebalPrices.size() 
//								+ " thisRebalComment=" + thisRebalComment.size()
//									);
							//Thread.sleep(1000 * 1000000);
						}
						
						// ----------------- 生成买卖信号结束 -----------
						
						// ------------ 更新并记录 -------------
						selectedLastTime_stock.clear();
						selectedLastTime_stock.addAll(selectedThisTime_stock);
						holdingSharesLastTime.clear();
						holdingSharesLastTime.putAll(holdingSharesThisTime);
						
						stockPicks.put(todayDate, selectedThisTime_stock);
						// ------------ 更新结束 -------------
						
						/*
						// debug
						String stocksTemp = "";
						//String
						for(int k = 0; k < thisRebalStocks.size(); k++) {
							stocksTemp += thisRebalStocks.get(k) + " " + thisRebalDirections.get(k) + " " + thisRebalWeightings.get(k) + " " + thisRebalPrices.get(k) + "\n";
						}
						logger.info("stocksTemp=" + stocksTemp);
						*/
						
						//----------------- 记录每次调仓时所有股票的排名 --------------
						fw_stock_ranking = new FileWriter(portFilePath + "\\stock ranking " + todayDateStr + ".csv");
						fw_stock_ranking.write("stock,"
								+ "1M ADV (USD mm),SB/FF,rank1,SB/3M ADV,rank2,"
								+ "rank3(avg daily SB/FF ranking),rank4(avg daily SB/3M ADV ranking),"
								+ "ranking strategy 1 rank,"
								+ "cum notional chg (HKD mm),rank5,"
								+ "percentile,rank6,"
								+ "rank diff,rank7,"
								//+ "Total Ranking,"
								+ "filter2,filter3,filter4,filterRankingStrategy6_1\n");
						for(int j = 0; j < todaySel2 .size(); j++) {
							StockSingleDate thisStock = todaySel2.get(j);
							Double _3MADV = thisStock.Turnover_3M_avg / 7.8 / 1000000;
							
							StockSingleDate thisStock_20 = todaySel_20_map.get(thisStock.stockCode);
							Double monthlySBChg_FF = thisStock_20.SB_over_os_shares;
							Double monthlySBChg_vol = thisStock_20.SB_over_vol;
							
							Double percentileData = 0.0;
							if(rank6Mode == 0) {
								percentileData = thisStock.SB_chg_hist_percentile;
							}
							if(rank6Mode == 1) {
								percentileData = thisStock.SB_chg_250D_percentile;
							}
							if(rank6Mode == 2) {
								percentileData = thisStock.SB_chg_60D_percentile;
							}
							
							fw_stock_ranking.write(thisStock.stockCode + "," + _3MADV + "," 
									+ monthlySBChg_FF + "," + thisStock.rank1 + ","
									+ monthlySBChg_vol + "," + thisStock.rank2 + ","
									+ thisStock.rank3 + "," + thisStock.rank4 + ","
									+ thisStock.dummyRankingStrategy1Rank + ","
									+ thisStock.SB_cum_notional_chg / 1000000 + "," + thisStock.rank5 + ","
									+ percentileData + "," + thisStock.rank6 + ","
									+ thisStock.dummyRank7RankDiff + "," + thisStock.rank7 + ","
									//+ thisStock.rankFinal + ","
									+ thisStock.filter2 + "," + thisStock.filter3 + "," + thisStock.filter4 + "," + thisStock.filterRankingStrategy6_1 + "\n");
						}
						fw_stock_ranking.close();
						
						// ========== 写入每日的southbound change ==========
						if(isOutputDailyCCASSChg) {
							SimpleDateFormat sdf111 = new SimpleDateFormat ("dd/MM/yyyy"); 
							//dailyCCASSChg_map;
							//fw_dailyCCASSChg = new FileWriter(portFilePath + "\\daily southbound chg (over free float) " + todayDate + ".csv");
							Set<String> allStocks = dailyCCASSChg_map.keySet();
							
							// 先对date list 进行排序
							ArrayList<Date> allDateArr = new ArrayList<Date>();
							allDateArr.addAll(dailyCCASSChg_allDates);
							Collections.sort(allDateArr);
							for(int j = 0; j < allDateArr.size(); j++) {
								//fw_dailyCCASSChg.write("," + sdf111.format(allDateArr.get(j)));
							}
							//fw_dailyCCASSChg.write("\n");
							
							// 读取每只股票数据
							for(String stock : allStocks) {
								Map<Date, Double> thisStockData = dailyCCASSChg_map.get(stock);
								ArrayList<String> thisStockToWrite = new ArrayList<String>();
								thisStockToWrite.add(stock);
								for(int k = 0; k < allDateArr.size(); k++)
									thisStockToWrite.add("");
								
								for(Date date : thisStockData.keySet()) {
									int ind = allDateArr.indexOf(date);
									if(ind >= 0)
										thisStockToWrite.set(ind + 1, String.valueOf(thisStockData.get(date) * 100));
								}
								
								for(int k = 0; k < thisStockToWrite.size(); k++) {
									if(k > 0) {
										//fw_dailyCCASSChg.write(",");
									}
									//fw_dailyCCASSChg.write(thisStockToWrite.get(k));
								}
								//fw_dailyCCASSChg.write("\n");
							}
							//fw_dailyCCASSChg.close();
							dailyCCASSChg_allDates = new HashSet<Date>();
							dailyCCASSChg_map = new HashMap<String, Map<Date, Double>>();
						}
						
						rebalCount ++;
						daysBetweenRelancingDate = 0;
					} 
					// *********** rebalancing date END *************
				}   // end of "if(todayInd > 0) {"
				
				long endTime = System.currentTimeMillis(); 
				logger.info("======= Stocks Screening END - " + todayDateStr + " time=" + (endTime - startTime)/1000.0 + "s ===============");
				
			} // end of the outermost for
			
			// --------------- output stock picks -----------
			FileWriter fw_stockPicks = new FileWriter(portFilePath + "\\stock picks.csv");
			ArrayList<Date> allDates = new ArrayList<Date>(stockPicks.keySet());
			int numOfStocks = stockPicks.get(allDates.get(0)).size();
			for(int k = 0; k < numOfStocks; k++) {
				if(k == 0) {  // 写表头
					for(int m = 0; m < allDates.size(); m++) {
						fw_stockPicks.write(sdf_yyyyMMdd.format(allDates.get(m)) + ",");
					}
					fw_stockPicks.write("\n");
				}
				
				for(int l = 0; l < allDates.size(); l++) {
					Date thisDate = allDates.get(l);
					ArrayList<String> thisDateStock = stockPicks.get(thisDate);
					String stock = thisDateStock.get(k);
					
					fw_stockPicks.write(stock + ",");
				}
				fw_stockPicks.write("\n");
			}
			fw_stockPicks.close();
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		toReturn.add(rebalDates);
		toReturn.add(data);
		return toReturn;
	}
	
	/**
	 * backtesting & output 
	 * @param data
	 */
	public static Portfolio backtesting(ArrayList<Object> data) {
		Portfolio pf = null;
		try {
			Backtesting bt = new Backtesting();
			bt.initialFunding = initialFunding; 
			//bt.startDate = "20160630";
			bt.startDate = rebalDateArr.get(0);
			bt.endDate = rebalDateArr.get(rebalDateArr.size() - 1);
			//bt.endDate = "20171213";
			bt.tradingCost = tradingCost;
			bt.executionOutputFilePath = executionOutputFilePath;
			
			ArrayList<ArrayList<ArrayList<Object>>> allRebalData = (ArrayList<ArrayList<ArrayList<Object>>>) data.get(1);
			ArrayList<Date> rebalDates = (ArrayList<Date>) data.get(0);
			ArrayList<String> rebalDatesStr = new ArrayList<String>();
			final int size = rebalDates.size();
			for(int i = 0;i < size; i ++) {
				rebalDatesStr.add(sdf.format(rebalDates.get(i)));
			}
			bt.rotationalTrading(rebalDatesStr, dateFormat, allRebalData);
			bt.portfolio.fw.close();
			
			
			pf = bt.portfolio;
			Map<Calendar, PortfolioOneDaySnapshot> histSnap = pf.histSnap;
			Set<Calendar> keys = histSnap.keySet();
			List<Calendar> keysArr = new ArrayList<Calendar>(keys);
			Collections.sort(keysArr);
			

			// ==== output market value & portfolio ======
			FileWriter fw = new FileWriter(portFilePath + "\\portfolio.csv");
			FileWriter fw2 = new FileWriter(portFilePath + "\\market value.csv");
			for(Calendar date : keysArr) {
				String dateStr = sdf.format(date.getTime());
				PortfolioOneDaySnapshot snapData = histSnap.get(date);
				
				Double marketValue = snapData.marketValue;
				Double cash = snapData.cashRemained;
				Map<String, Underlying> stockHeld = snapData.stockHeld;
				
				//System.out.println("======== " + dateStr + " =============");
				fw.write("======== " + dateStr + " =============\n");
				
				//System.out.println("  MV = " + String.valueOf(marketValue)); 
				fw.write(",MV," + String.valueOf(marketValue)+"\n");
				
				//System.out.println("  Cash = " + String.valueOf(cash));
				fw.write(",Cash,"+ String.valueOf(cash) + "\n");
				
				//System.out.println("  Stock holdings:");
				fw.write(",Stock holding (shares)\n");
				
				Set<String> stockKeys = stockHeld.keySet()	;
				for(String stock : stockKeys) {
					Underlying holding = stockHeld.get(stock);
					
					//System.out.println("    stock = " + stock + " amt = " + holding);
					if(holding.amount > 0.0)
						fw.write(",," + stock + "," + holding.amount + "\n");
				}
				
				fw2.write(dateStr + "," + String.valueOf(marketValue) + "\n");
			}
			fw.close();
			fw2.close();
			
			// ======= output stock picks =========
			Map<String, ArrayList<String>> data_trans = new HashMap();
			FileWriter fw_stockPicks1 = new FileWriter(portFilePath + "\\stock picks1 (buy only).csv");
			for(int i = 0; i < rebalDatesStr.size(); i++) {
				if(i > 0)
					fw_stockPicks1.write(",");
				fw_stockPicks1.write(rebalDatesStr.get(i));
			}
			fw_stockPicks1.write("\n");
			int size_stocks = -100;
			if(topNStocks_mode == 1) {
				size_stocks = PortfolioScreening.topNStocks;
			}
			if(topNStocks_mode == 2) {
				size_stocks = topNStocks_bufferZone_out;
			}
			for(int i = 0; i < size_stocks; i++) { //第几只股票
				for(int j = 0; j < rebalDatesStr.size(); j++) {
					if(j > 0)
						fw_stockPicks1.write(",");
					
					ArrayList<Object> todayStocks = allRebalData.get(j).get(0);
					ArrayList<Object> todayDirections = allRebalData.get(j).get(1);
					
					String stock = "";
					if(todayStocks == null || todayStocks.size() == 0) {
						continue;
					}
					if( i > (todayStocks.size() - 1))
						continue;
					
					int numOfBuysFound = 0;
					for(int k = 0; k < todayDirections.size(); k++) {   //因为allRebalData既有buy的股票也有sell的，所以每次都要找到buy的股票才行
						Integer direction = (Integer) todayDirections.get(k);
						if(direction == null || direction < 0)
							continue;  //  these are sell orders
						if(numOfBuysFound == i) {
							stock = (String) todayStocks.get(k);
							break;
						}
						numOfBuysFound ++;
					}
					
					
					
					fw_stockPicks1.write(stock);
					
					//======= transform "data" ====
					ArrayList<String> thisStockData = data_trans.get(stock);
					if(thisStockData == null || thisStockData.size() == 0) {
						thisStockData = new ArrayList<String>();
						for(int k = 0; k < rebalDatesStr.size(); k ++) {
							thisStockData.add("");
						}
					}
					thisStockData.set(j, "1");
					data_trans.put(stock, thisStockData);
				}
				fw_stockPicks1.write("\n");
			}
			fw_stockPicks1.close();
			
			// === write "stock picks2.csv" ====
			FileWriter fw_stockPicks2 = new FileWriter(portFilePath + "\\stock picks2.csv");
			for(int i = 0; i < rebalDatesStr.size(); i++) {
				fw_stockPicks2.write("," + rebalDatesStr.get(i));
			}
			fw_stockPicks2.write(",Total\n");
			
			Set<String> stockSet = data_trans.keySet();
			for(String stock : stockSet) {
				fw_stockPicks2.write(stock);
				
				ArrayList<String> thisStockData = data_trans.get(stock);
				int count = 0;
				for(int i = 0; i < thisStockData .size(); i++) {
					String thisData = thisStockData.get(i);
					fw_stockPicks2.write("," + thisData);
					if(thisData.equals("1"))
						count ++;
				}
				fw_stockPicks2.write("," + String.valueOf(count) + "\n");
			}
			fw_stockPicks2.close();
			
			// save the portfolio
			//XMLUtil.convertToXml(pf, portFilePath + "\\portfolio.xml");
			// save the every data
			//XMLUtil.convertToXml(allPortfolioScreeningData, portFilePath + "\\allPortfolioScreeningData.xml");
		
			
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		return pf;
	}
	
	// --------------------- Utility functions ------------
	
	/**
	 * 返回rebalancing的date
	 * 	rebalancingStrategy
	 * 	 * 1 - monthly, rebal at month beginning
		 * 2 - monthly, rebal at month end
		 * 3 - bi-weekly
		 * 4 - weekly
		 * 5 - every 40 trading days
	 * @param startDate
	 * @param endDate
	 * @param dateFormat
	 * @param rebalancingStrategy
	 * @return
	 */
	public static ArrayList<String> getRebalDate(Date startDate, Date endDate, String dateFormat, int rebalancingStrategy, ArrayList<Calendar> allTradingDate){
		ArrayList<String> rebalArr = new ArrayList<String>();
		SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
		
		/*
		 * rebalancingStrategy
		 * 1 - monthly, rebal at month beginning
		 * 2 - monthly, rebal at month end
		 * 3 - bi-weekly
		 * 4 - weekly
		 * 5 - every 40 trading days
		 * 100 - self defined
		 */
	
		try {	
			Calendar endCal = Calendar.getInstance();
			endCal.setTime(endDate);
			endCal = utils.Utils.getMostRecentDate(endCal, allTradingDate);
			
			if(rebalancingStrategy == 1) {   // 1 - monthly, rebal at month beginning
				//只需要得到每个月的month beginning
				Calendar cal = Calendar.getInstance();
				cal.setTime(startDate);
				cal.set(Calendar.DATE, 1);  // 月初
				
				while(!cal.after(endCal)) {
					Calendar firstTrdDate = utils.Utils.getMostRecentDate(cal, allTradingDate);
					
					String rebalStr = "";
					if(firstTrdDate.before(cal)) {
						int ind = allTradingDate.indexOf(firstTrdDate);
						Calendar rebalDate = allTradingDate.get(ind + 1);
						rebalStr = sdf.format(rebalDate.getTime());
					}else {
						rebalStr = sdf.format(firstTrdDate.getTime());
					}
					
					rebalArr.add(rebalStr);
					//logger.info("rebalancingStrategy 1 - rebalDate=" + rebalStr);
					
					cal.add(Calendar.MONTH, 1);
					cal.set(Calendar.DATE, 1);
					
				}
				
			}
			
			if(rebalancingStrategy == 2) {   // 2 - monthly, rebal at month end
				//只需要得到每个月的month beginning
				Calendar cal = Calendar.getInstance();
				cal.setTime(startDate);
				cal.set(Calendar.DATE, 1);  
				cal.add(Calendar.MONTH, 1);
				cal.add(Calendar.DATE, -1); // 月底
				
				while(!cal.after(endCal)) {
					Calendar firstTrdDate = utils.Utils.getMostRecentDate(cal, allTradingDate);
					
					
					String rebalStr = sdf.format(firstTrdDate.getTime());
					rebalArr.add(rebalStr);
					//logger.info("rebalancingStrategy 1 - rebalDate=" + rebalStr);
					
					cal.add(Calendar.MONTH, 2);
					cal.set(Calendar.DATE, 1);
					cal.add(Calendar.DATE, -1);  //下个月月底
					
				}
				
			}
			
			if(rebalancingStrategy == 3 || rebalancingStrategy == 4 || rebalancingStrategy == 5) {   //3 - bi-weekly
				Calendar cal = Calendar.getInstance();
				cal.setTime(startDate);
				
				while(!cal.after(endCal)) {
					Calendar firstTrdDate = utils.Utils.getMostRecentDate(cal, allTradingDate);
					
					String rebalStr = sdf.format(firstTrdDate.getTime());
					rebalArr.add(rebalStr);
					//logger.info("rebalancingStrategy 1 - rebalDate=" + rebalStr);
					
					if(rebalancingStrategy == 3)
						cal.add(Calendar.WEEK_OF_MONTH, 2);
					if(rebalancingStrategy == 4)
						cal.add(Calendar.WEEK_OF_MONTH, 1);
					if(rebalancingStrategy == 5)
						cal.add(Calendar.DATE, 40);
				}
				
			}
			
			if(rebalancingStrategy == 100) {
				/*
				rebalArr.add("20171031");
				rebalArr.add("20171124");
				*/
				
				
				//rebalArr.add("20171027");
				//rebalArr.add("20171101");
				//rebalArr.add("20171129");
				
				ArrayList<Date> allTradingDate_date = new ArrayList<Date> ();
				for(int i = 0; i < allTradingDate.size(); i++) {
					Calendar cal = allTradingDate.get(i);
					Date d = cal.getTime();
					allTradingDate_date.add(d);
				}
				
				
				Date startDate2 = sdf_yyyyMMdd.parse("20170101");
				startDate2 = utils.Utils.getMostRecentDate(startDate2, allTradingDate_date);
				int startInd2 = allTradingDate_date.indexOf(startDate2);
				
				Date endDate2 = sdf_yyyyMMdd.parse("20180126");
				endDate2 = utils.Utils.getMostRecentDate(endDate2, allTradingDate_date);
				int endInd2 = allTradingDate_date.indexOf(endDate2);
				
				for(int i = startInd2; i <= endInd2; i++) {
					rebalArr.add(sdf_yyyyMMdd.format(allTradingDate_date.get(i)));
				}
			}
			if(rebalancingStrategy == 101) {
				rebalArr.add("20170731");
				rebalArr.add("20170831");
				rebalArr.add("20170929");
				rebalArr.add("20171030");
				rebalArr.add("20171130");
			}
			
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		logger.info("Rebalancing date=" + rebalArr);
		return rebalArr;
	}

}
