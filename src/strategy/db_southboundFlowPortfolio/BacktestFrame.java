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

public class BacktestFrame {
	public static Logger logger = Logger.getLogger(BacktestFrame.class);
	
	// ---------- controlling variables -----------
	public static String dateFormat = "";
	public static SimpleDateFormat sdf;
	public static SimpleDateFormat sdf2 ;
	public static SimpleDateFormat sdf_yyyyMMdd ; 
	public static Date startDate ;
	public static String startDateStr ;
	public static Date endDate ;
	public static String endDateStr ;
	
	public static String portFilePath = "D:\\stock data\\southbound flow strategy - db\\" 
			+ sdf2.format(new Date()) + " - idea3 - bactesting四 - 15stocks";    // 最终输出的root path
	public static String allSbDataPath = "D:\\stock data\\HK CCASS - WEBB SITE\\southbound\\combined";  // 
	
	public static ArrayList<Calendar> allTradingDate = new ArrayList<Calendar> ();  
	public static String allTradingDatePath = "D:\\stock data\\all trading date - hk.csv";
	
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
	public static int earlyUnwindStrategy  = 1;
	/*
	 * 1 - 正常，不会提前卖出某只股票
	 * 2 - 如果一只股票的southbound holding连续三天减少，则提前全部卖掉，并不再补充其他股票
	 * 3 - 如果一只股票的southbound holding连续三天减少，则提前卖掉一半，并不再补充其他股票
	 */
	
	// ----------- 运行中需要用到的variables --------------
	public static boolean isOutputDailyCCASSChg = true; // 是否输出每日southbound的CCASS的change
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
			
			
			File f = new File(portFilePath);
			f.mkdir();
			
			PortfolioScreening.avgDailyValueThreshHold_USD =  7000000.0;  // 每天的平均成交额需要超过这个数才能入选
			PortfolioScreening.topNStocks = 15;   // 每次选多少只股票进行买入卖出
			
			rebalDateArr = getRebalDate(startDate, endDate, dateFormat, rebalancingStrategy,allTradingDate);
			
			PortfolioScreening.outputPath = portFilePath;
			PortfolioScreening.getAllSbData(allSbDataPath);
			PortfolioScreening.getAllTradingDate();
			
			PortfolioScreening.oneMonthBeforeDays = 20;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * 得到一个
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
			
			// -------------- now start... -------------------
			FileWriter fw_dailyCCASSChg ; // 仅仅是为了初始化
			Map<String, Map<Date, Double>> dailyCCASSChg_map = new HashMap<String, Map<Date, Double>>();
			Set<Date> dailyCCASSChg_allDates = new  HashSet<>();;
			
			int dayCalStart = 20;
			int daysBetweenRelancingDate = 0;
			PortfolioScreening.getAllOsData(PortfolioScreening.outstandingFilePath, allTradingDate.get(rebalStartInd-dayCalStart).getTime());
			ArrayList<String> stocksToBuy_str = new ArrayList<String>();  //记录每个rebalancing date需要选出的股票
			for(int i = rebalStartInd-dayCalStart; i <= rebalEndInd; i++) {  
				daysBetweenRelancingDate++;
				String todayDate = sdf_yyyyMMdd.format(allTradingDate.get(i).getTime());
				logger.info("\tScreening date = " + todayDate);
				
				PortfolioScreening.oneMonthBeforeDays = 1;
				ArrayList<StockSingleDate> todaySel = PortfolioScreening.assignValue_singleDate(todayDate, "yyyyMMdd"); //对每只股票进行赋值
				logger.debug("\t\ttodaySel .size() = " + todaySel .size());
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
					

					// *********** rebalancing date *************
					if(todayDate.equals(rebalDateArr.get(rebalCount))) { 
						logger.info("\t\tToday is a rebalancing date! daysBetweenRelancingDate=" + daysBetweenRelancingDate);
						ArrayList<StockSingleDate> stocksToBuy = new ArrayList<StockSingleDate> ();
						
					
						Map<String, StockSingleDate> rebalStock_map = new HashMap();  // 想要在rebal那天将每只股票的rank都列出来
						for(int j = 0; j < todaySel .size(); j++) { // 先将rebalStock的框架搭出来
							StockSingleDate stock = todaySel.get(j);
							stock.dummy3 = stock.dummy1;  // dummy1是按照freefloat的排序排出来的
							stock.dummy4 = stock.dummy2;  // dummy1是按照3M ADV的排序排出来的
							stock.dummy5 = 1.0;    // 有效天数
							rebalStock_map.put(stock.stockCode, stock);
						}
						
						for(int j = 1; j < daysBetweenRelancingDate; j++) { // 寻找最近20天的数据，不用循环j=0的情况了
							ArrayList<StockSingleDate> rebal_thisDateSel = allPortfolioScreeningData.get(allPortfolioScreeningData.size() - 1 - j) ;
							//String rebal_thisDate = sdf_yyMMdd.format(allTradingDate.get(i-j).getTime());
							for(int k = 0; k < rebal_thisDateSel.size(); k++) {
								StockSingleDate thisStock = rebal_thisDateSel.get(k);
								StockSingleDate findStock =rebalStock_map.get(thisStock.stockCode);
								if(findStock != null) { // 只考虑能找到的情况
									findStock.dummy3 = findStock.dummy3 + thisStock.dummy1; // dummy1是每只股票在当天的按照southbound的变化除以freefloat的排名，dummy3来存储20天内这个排名的总值  
									findStock.dummy4 = findStock.dummy4 + thisStock.dummy2;
									findStock.dummy5 = findStock.dummy5 + 1;  // dummy4 来存储有多少天是有效的
									rebalStock_map.put(thisStock.stockCode, findStock);
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
						// 将每只股票最近20天的ranking加总，然后求平均值，再排序
						ArrayList<StockSingleDate> todaySel2 = new ArrayList<StockSingleDate>();
						todaySel2.addAll(rebalStock_map.values());
						logger.debug("\t\trebalStock_map.values().size() = " + rebalStock_map.values().size());
						logger.debug("\t\ttodaySel2.size() = " + todaySel2 .size());
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
							thisStock.rank5 = thisStock.sorting_indicator;
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
							}
							Collections.sort(todaySel2, StockSingleDate.getComparator(-1));  // 降序排列
						}
						
						// 还要进行filter
						int lookbackDays1 = daysBetweenRelancingDate;
						int minDays1 = (int) Math.floor((double) lookbackDays1 * minInflowPct);
						
						int lookbackDays2 = 5;
						int minDays2 = (int) Math.floor((double) lookbackDays2 * minInflowPct);
						for(int j = 0; j < lookbackDays1; j++) { // 回看过去20天
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
						
						stocksToBuy = PortfolioScreening.pickStocks_singleDate(todaySel2, false);
						
						//进行output
						FileWriter fw = new FileWriter(portFilePath + "\\stock ranking " + todayDate + ".csv");
						fw.write("stock,"
								+ "3M ADV (USD mm),SB/FF,rank1,SB/3M ADV,rank2,"
								+ "rank3(avg daily SB/FF ranking),rank4(avg daily SB/3M ADV ranking),Total Ranking,"
								+ "filter2,filter3,filter4\n");
						for(int j = 0; j < todaySel2 .size(); j++) {
							StockSingleDate thisStock = todaySel2.get(j);
							Double _3MADV = thisStock.Turnover_3M_avg / 7.8 / 1000000;
							
							StockSingleDate thisStock_20 = todaySel_20_map.get(thisStock.stockCode);
							Double monthlySBChg_FF = thisStock_20.SB_over_os_shares;
							Double monthlySBChg_vol = thisStock_20.SB_over_vol;
							
							fw.write(thisStock.stockCode + "," + _3MADV + "," 
									+ monthlySBChg_FF + "," + thisStock.rank1 + ","
									+ monthlySBChg_vol + "," + thisStock.rank2 + ","
									+ thisStock.rank3 + "," + thisStock.rank4 + "," + thisStock.rank5 + ","
									+ thisStock.filter2 + "," + thisStock.filter3 + "," + thisStock.filter4 + "\n");
						}
						fw.close();
					
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
							price_sell.add(Double.parseDouble(stockPrice.DataGetter.getStockDataField(thisStockToSell,stockPrice.DataGetter.StockDataField.adjclose, todayDate, "yyyyMMdd")));
						}
						
						// stocks to buy
						stocksToBuy_str = new ArrayList<String>();  //每个rebalancing date需要选出的股票
						ArrayList<Integer> direction_buy = new ArrayList<Integer>();
						ArrayList<Double> weighting_buy = new ArrayList<Double>();
						ArrayList<Double> price_buy = new ArrayList<Double>();
						final int size2 = stocksToBuy.size();
						for(int j = 0; j < size2; j++) {
							String thisStockToBuy = stocksToBuy.get(j).stockCode;
							stocksToBuy_str.add(thisStockToBuy);
							direction_buy.add(1);
							weighting_buy.add(-98.0 /  size2);
							price_buy.add(Double.parseDouble(stockPrice.DataGetter.getStockDataField(thisStockToBuy,stockPrice.DataGetter.StockDataField.adjclose, todayDate, "yyyyMMdd")));
						}
						
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
						
						data.add(thisRebalData);
						rebalDates.add(sdf_yyyyMMdd.parse(todayDate));
						
						// ========== 写入每日的southbound change ==========
						if(isOutputDailyCCASSChg) {
							SimpleDateFormat sdf111 = new SimpleDateFormat ("dd/MM/yyyy"); 
							//dailyCCASSChg_map;
							fw_dailyCCASSChg = new FileWriter(portFilePath + "\\daily southbound chg (over free float) " + todayDate + ".csv");
							Set<String> allStocks = dailyCCASSChg_map.keySet();
							
							// 先对date list 进行排序
							ArrayList<Date> allDateArr = new ArrayList<Date>();
							allDateArr.addAll(dailyCCASSChg_allDates);
							Collections.sort(allDateArr);
							for(int j = 0; j < allDateArr.size(); j++) {
								fw_dailyCCASSChg.write("," + sdf111.format(allDateArr.get(j)));
							}
							fw_dailyCCASSChg.write("\n");
							
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
									if(k > 0)
										fw_dailyCCASSChg.write(",");
									fw_dailyCCASSChg.write(thisStockToWrite.get(k));
								}
								fw_dailyCCASSChg.write("\n");
							}
							fw_dailyCCASSChg.close();
							dailyCCASSChg_allDates = new HashSet<Date>();
							dailyCCASSChg_map = new HashMap<String, Map<Date, Double>>();
						}
						
						rebalCount ++;
						daysBetweenRelancingDate = 0;
					} 
					// *********** rebalancing date END *************
				}   // end of "if(todayInd > 0) {"
				
			} // end of the outermost for
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		toReturn.add(rebalDates);
		toReturn.add(data);
		return toReturn;
	}
	
	
	public static void backtesting(ArrayList<Object> data) {
		try {
			Backtesting bt = new Backtesting();
			//bt.startDate = "20160630";
			bt.startDate = rebalDateArr.get(0);
			bt.endDate = rebalDateArr.get(rebalDateArr.size() - 1);
			bt.tradingCost = 0.0015;
			
			ArrayList<ArrayList<ArrayList<Object>>> allRebalData = (ArrayList<ArrayList<ArrayList<Object>>>) data.get(1);
			ArrayList<Date> rebalDates = (ArrayList<Date>) data.get(0);
			ArrayList<String> rebalDatesStr = new ArrayList<String>();
			final int size = rebalDates.size();
			for(int i = 0;i < size; i ++) {
				rebalDatesStr.add(sdf.format(rebalDates.get(i)));
			}
			bt.rotationalTrading(rebalDatesStr, dateFormat, allRebalData);
			
			Portfolio pf = bt.portfolio;
			Map<Calendar, PortfolioOneDaySnapshot> histSnap = pf.histSnap;
			Set<Calendar> keys = histSnap.keySet();
			List<Calendar> keysArr = new ArrayList<Calendar>(keys);
			Collections.sort(keysArr);
			
			
		}catch(Exception e) {
			e.printStackTrace();
		}
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
			
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		return rebalArr;
	}

}