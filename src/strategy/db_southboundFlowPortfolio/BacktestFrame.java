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
	public static double tradingCost = 0.0;
	
	public static String portFilePath="";    // 最终输出的root path
	public static String allSbDataPath = "Z:\\Mubing\\stock data\\HK CCASS - WEBB SITE\\southbound\\combined";  // 存储所有southbound data的文件夹
	public static String allPriceDataPath = "Z:\\Mubing\\stock data\\stock hist data - webb";  //存储所有stock price的data的文件夹
	public static String notionalChgDataRootPath = "Z:\\Mubing\\stock data\\southbound flow strategy - db\\";
	
	public static ArrayList<Calendar> allTradingDate = new ArrayList<Calendar> ();  
	public static String allTradingDatePath = utils.PathConifiguration.ALL_TRADING_DATE_PATH;
	public static boolean isNormalSorting = true; //normal sorting - rank with higher rank in the front
	
	// ---------------- factors --------------
	public static int rankingStrategy = 1;
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
	
	// ----------- 运行中需要用到的variables --------------
	public static boolean isOutputDailyCCASSChg = true; // 是否输出每日southbound的CCASS的change
														// 必须设置为true，不然会影响earlyUnwindStrategy
	
	public static ArrayList<String> rebalDateArr = new ArrayList<String>();
	/*public static int rebalStartInd =-1;
	public static int rebalEndInd =-1;
	public static ArrayList<ArrayList<StockSingleDate>> allPortfolioScreeningData = new ArrayList<ArrayList<StockSingleDate>> (); // 存储每次运行portfolio screening选出的股票
	public static ArrayList<ArrayList<String>> data = new ArrayList<ArrayList<String>> ();   // 存储每个rebalancing date选出的股票
	*/
	
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
			if(PortfolioScreening.sbDataMap == null || PortfolioScreening.sbDataMap.size() == 0)
				PortfolioScreening.getAllSbData(allSbDataPath);
			
			/*
			Calendar rebalStart = Calendar.getInstance();
			rebalStart.setTime(sdf_yyyyMMdd.parse(rebalDateArr.get(0)));
			int rebalStartInd = allTradingDate .indexOf(rebalStart);
			if(PortfolioScreening.priceDataMap == null || PortfolioScreening.priceDataMap.size() == 0)
				PortfolioScreening.getAllPriceData(allPriceDataPath, allTradingDate.get(rebalStartInd - 30).getTime());
			*/
			
			PortfolioScreening.oneMonthBeforeDays = 20;
			PortfolioScreening.isToCalNotional = isToCalNotional;
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
	 * 	}
	 * 	,
	 * 	{
	 * 		{stock1, stock2, ...},
	 * 		{direction1, direction2, ...},
	 * 		{weighting1, weighting2, ...},
	 * 		{price1, price2, ...}
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
			ArrayList<String> stocksToBuy_str = new ArrayList<String>();  //记录每个rebalancing date需要选出的股票
			ArrayList<StockSingleDate> stocksToBuy = new ArrayList<StockSingleDate> ();  // //记录每个rebalancing date需要选出的股票
			for(int i = rebalStartInd-dayCalStart; i <= rebalEndInd; i++) {  
				long startTime = System.currentTimeMillis();
				
				daysBetweenRelancingDate++;
				String todayDate = sdf_yyyyMMdd.format(allTradingDate.get(i).getTime());
				//logger.info("\tScreening date = " + todayDate);
				logger.info("======= Stocks Screening - " + todayDate + " ===============");
				
				PortfolioScreening.oneMonthBeforeDays = 1;
				ArrayList<StockSingleDate> todaySel = PortfolioScreening.assignValue_singleDate(todayDate, "yyyyMMdd"); //对每只股票进行赋值
				logger.debug("\t\ttodaySel .size() = " + todaySel .size());
				if(allPortfolioScreeningData.size() >= max_allPS_Data) {   // to keep allPortfolioScreeningData not so short
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
						
						final int stocksToBuySize = stocksToBuy_str.size();
						Date DayT = allTradingDate.get(i).getTime();  // day T
						Date DayT_1 = allTradingDate.get(i-1).getTime();  // day T-1
						Date DayT_2 = allTradingDate.get(i-2).getTime();  // day T-2
						Date DayT_3 = allTradingDate.get(i-3).getTime();  // day T-3
						Date DayT_4 = allTradingDate.get(i-4).getTime();  // day T-4
						
						for(int j = 0; j < stocksToBuySize; j++) {
							String stock = stocksToBuy_str.get(j);
							Map<Date, Double> stockData = dailyCCASSChg_map.get(stock);
							if(stockData != null) {
								Double chgT = stockData.get(DayT);
								Double chgT_1 = stockData.get(DayT_1);
								Double chgT_2 = stockData.get(DayT_2);
								Double chgT_3 = stockData.get(DayT_3);
								Double chgT_4 = stockData.get(DayT_4);
								
								if(chgT != null && chgT_2 != null & chgT_1 != null && chgT_3 != null && chgT_4 != null 
										&& chgT < 0 && chgT_1 < 0 && chgT_2 < 0 && chgT_3 < 0 && chgT_4 < 0) {  // 连续多天净卖出，满足early unwind的条件
									logger.info("stocksToBuy_str=" + stocksToBuy_str);
									
									stocksToSell_str.add(stock);
									direction_sell.add(-1);
									if(earlyUnwindStrategy == 2) {
										weighting_sell.add(-100.0);
									}
									if(earlyUnwindStrategy == 3) {
										weighting_sell.add(-50.0);
									}
									price_sell.add(
											Double.parseDouble(stockPrice.DataGetter.getStockDataField(stock,stockPrice.DataGetter.StockDataField.adjclose, todayDate, "yyyyMMdd"))
											);
									logger.info("Early unwind (strategy " + earlyUnwindStrategy + ")! stock=" + stock);
								}
							}
						}
						
						if(stocksToSell_str.size() > 0) {
							logger.info("       todaydate=" + todayDate);
							ArrayList<ArrayList<Object>> thisRebalData = new ArrayList<ArrayList<Object>>(); 
							thisRebalData.add(stocksToSell_str);
							thisRebalData.add(direction_sell);
							thisRebalData.add(weighting_sell);
							thisRebalData.add(price_sell);
							
							data.add(thisRebalData);
							rebalDates.add(sdf_yyyyMMdd.parse(todayDate));
						}
						
					}

					// *********** rebalancing date *************
					if(todayDate.equals(rebalDateArr.get(rebalCount))) {
						daysBetweenRelancingDate = 15; // rolling 的概念(if don't want rolling, just comment this line),
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
									if(false && findStock.stockCode.equals("700") && testDate.equals(sdf_yyyyMMdd.parseObject(todayDate))) {
										logger.info("    date=" + sdf_yyyyMMdd.format(thisStock.cal.getTime()) + " cum notional =" + findStock.SB_cum_notional_chg);
									}
								}
							}
						}
						
						// 上面是做daily的ranking，这里只做month end和month begin的ranking
						PortfolioScreening.oneMonthBeforeDays = daysBetweenRelancingDate;
						ArrayList<StockSingleDate> todaySel_20 = PortfolioScreening.assignValue_singleDate(todayDate, "yyyyMMdd");
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
							
							if(rankingStrategy == 1)
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
							thisStock.rank7 = thisStock.sorting_indicator;
						}
						Collections.sort(todaySel2, StockSingleDate.getComparator(-1));  // 降序排列
						
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
								thisStock.rank7 = thisStock.sorting_indicator;
							}
							Collections.sort(todaySel2, StockSingleDate.getComparator(-1));  // 降序排列
						}
						if(!isNormalSorting)
							Collections.sort(todaySel2, StockSingleDate.getComparator(1));  // 降序排列
						
						
						// 还要进行filter
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
						for(int k = 0; k < todaySel2.size(); k++	) {
							StockSingleDate thisStock = todaySel2.get(k);
							
							if(thisStock.filter2 / (lookbackDays1 * 100) >= ((double) minDays1 / lookbackDays1)
									&& thisStock.filter3 / (lookbackDays2 * 100) >= ((double) minDays2 / lookbackDays2)) {
								thisStock.filter4 = 2.0;
							}else {
								thisStock.filter4 = -2.0;
							}
						}
						
						if(topNStocks_mode == 1) {
							stocksToBuy = PortfolioScreening.pickStocks_singleDate(todaySel2, false);
						}
						//ArrayList<StockSingleDate> pickedStocks_topNStocks_mode2 = new ArrayList<StockSingleDate> (); 
						if(topNStocks_mode == 2) {  // buffer mode
							int temp = PortfolioScreening.topNStocks;
							PortfolioScreening.topNStocks = topNStocks_bufferZone_out;
							stocksToBuy = PortfolioScreening.pickStocks_singleDate(todaySel2, false);
							PortfolioScreening.topNStocks = temp;
						}
					
						// stocks to sell
						ArrayList<String> stocksToSell_str = new ArrayList<String>(stocksToBuy_str); //此时的stocksToBuy_str还存储着上次rebalance要买的股票，这恰好是本次要卖的股票
						ArrayList<Integer> direction_sell = new ArrayList<Integer>();
						ArrayList<Double> weighting_sell = new ArrayList<Double>();
						ArrayList<Double> price_sell = new ArrayList<Double>();
						final int size1 = stocksToSell_str.size();   
						for(int j = 0; j < size1; j++) {
							String thisStockToSell = stocksToBuy_str.get(j);
							direction_sell.add(-1);
							weighting_sell.add(-100.0);
							
							String priceToSell = stockPrice.DataGetter.getStockDataField(
									thisStockToSell,
									stockPrice.DataGetter.StockDataField.adjclose, 
									todayDate, "yyyyMMdd");
							if(priceToSell == null)
								priceToSell ="0";
							price_sell.add(Double.parseDouble(priceToSell));
						}
						
						// stocks to buy，此时stocksToBuy已经update了，但是stocksToBuy_str还没update。。。
						ArrayList<String> lastHoldStocks_str = new ArrayList<String>(stocksToBuy_str);  //上一期hold的股票
						stocksToBuy_str = new ArrayList<String>();  //每个rebalancing date需要选出的股票
						ArrayList<Integer> direction_buy = new ArrayList<Integer>();
						ArrayList<Double> weighting_buy = new ArrayList<Double>();
						ArrayList<Double> price_buy = new ArrayList<Double>();
						int size2 = stocksToBuy.size();
						for(int j = 0; j < size2; j++) {   //size2其实就是topNStocks_bufferZone_out
							String thisStockToBuy = stocksToBuy.get(j).stockCode;
							if(topNStocks_mode == 2 && j >= topNStocks_bufferZone_in) {  
								if(lastHoldStocks_str.indexOf(thisStockToBuy)  == -1) {
									continue;   //如果这只股票跌出了topNStocks_bufferZone_in的范围，就不买入
								}
							}
							
							stocksToBuy_str.add(thisStockToBuy);
							direction_buy.add(1);
							Double thisWeighting = 0.0;
							if(weightingStrategy == 1) {
								thisWeighting  = -98.0 /  size2;
								weighting_buy.add(-98.0 /  size2);
							}
							if(weightingStrategy == 2) {
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
							
							price_buy.add(Double.parseDouble(stockPrice.DataGetter.getStockDataField(thisStockToBuy,stockPrice.DataGetter.StockDataField.adjclose, todayDate, "yyyyMMdd")));
						}
						
						logger.info("todayDate = " + todayDate + " stocksToBuy_str=" + stocksToBuy_str);
						
						
						//合并2个list
						ArrayList<Object> thisRebalStocks = new ArrayList<Object> (stocksToSell_str); 
						thisRebalStocks.addAll(stocksToBuy_str);
						ArrayList<Object> thisRebalDirections = new ArrayList<Object> (direction_sell);
						thisRebalDirections.addAll(direction_buy);
						ArrayList<Object> thisRebalWeightings = new ArrayList<Object>(weighting_sell);
						thisRebalWeightings.addAll(weighting_buy);
						ArrayList<Object> thisRebalPrices = new ArrayList<Object>(price_sell);
						thisRebalPrices.addAll(price_buy);
								
						ArrayList<ArrayList<Object>> thisRebalData = new ArrayList<ArrayList<Object>>(); 
						thisRebalData.add(thisRebalStocks);
						thisRebalData.add(thisRebalDirections);
						thisRebalData.add(thisRebalWeightings);
						thisRebalData.add(thisRebalPrices);
						
						/*
						// debug
						String stocksTemp = "";
						//String
						for(int k = 0; k < thisRebalStocks.size(); k++) {
							stocksTemp += thisRebalStocks.get(k) + " " + thisRebalDirections.get(k) + " " + thisRebalWeightings.get(k) + " " + thisRebalPrices.get(k) + "\n";
						}
						logger.info("stocksTemp=" + stocksTemp);
						*/
						
						data.add(thisRebalData);
						rebalDates.add(sdf_yyyyMMdd.parse(todayDate));
						
						//----------------- 记录每次调仓时所有股票的排名 --------------
						fw_stock_ranking = new FileWriter(portFilePath + "\\stock ranking " + todayDate + ".csv");
						fw_stock_ranking.write("stock,"
								+ "1M ADV (USD mm),SB/FF,rank1,SB/3M ADV,rank2,"
								+ "rank3(avg daily SB/FF ranking),rank4(avg daily SB/3M ADV ranking),"
								+ "cum notional chg (HKD mm),rank5,"
								+ "Total Ranking,"
								+ "filter2,filter3,filter4\n");
						for(int j = 0; j < todaySel2 .size(); j++) {
							StockSingleDate thisStock = todaySel2.get(j);
							Double _3MADV = thisStock.Turnover_3M_avg / 7.8 / 1000000;
							
							StockSingleDate thisStock_20 = todaySel_20_map.get(thisStock.stockCode);
							Double monthlySBChg_FF = thisStock_20.SB_over_os_shares;
							Double monthlySBChg_vol = thisStock_20.SB_over_vol;
							
							fw_stock_ranking.write(thisStock.stockCode + "," + _3MADV + "," 
									+ monthlySBChg_FF + "," + thisStock.rank1 + ","
									+ monthlySBChg_vol + "," + thisStock.rank2 + ","
									+ thisStock.rank3 + "," + thisStock.rank4 + ","
									+ thisStock.SB_cum_notional_chg / 1000000 + "," + thisStock.rank5 + ","
									+ thisStock.rank7 + ","
									+ thisStock.filter2 + "," + thisStock.filter3 + "," + thisStock.filter4 + "\n");
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
				logger.info("======= Stocks Screening END - " + todayDate + " time=" + (endTime - startTime)/1000.0 + "s ===============");
				
			} // end of the outermost for
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
			
			ArrayList<ArrayList<ArrayList<Object>>> allRebalData = (ArrayList<ArrayList<ArrayList<Object>>>) data.get(1);
			ArrayList<Date> rebalDates = (ArrayList<Date>) data.get(0);
			ArrayList<String> rebalDatesStr = new ArrayList<String>();
			final int size = rebalDates.size();
			for(int i = 0;i < size; i ++) {
				rebalDatesStr.add(sdf.format(rebalDates.get(i)));
			}
			bt.rotationalTrading(rebalDatesStr, dateFormat, allRebalData);
			
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
				fw.write(",Stock holding\n");
				
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
			FileWriter fw_stockPicks1 = new FileWriter(portFilePath + "\\stock picks1.csv");
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
				
				
				Date startDate2 = sdf_yyyyMMdd.parse("20171201");
				startDate2 = utils.Utils.getMostRecentDate(startDate2, allTradingDate_date);
				int startInd2 = allTradingDate_date.indexOf(startDate2);
				
				Date endDate2 = sdf_yyyyMMdd.parse("20180104");
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
