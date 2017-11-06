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
import utils.XMLUtil;

public class Main {
	private static Logger logger = LogManager.getLogger(Main.class.getName());
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			//System.out.println(southboundData.DataGetter.getStockData("700", "20170801", "yyyyMMdd").get(2));
			SimpleDateFormat sdf_yyyyMMdd = new SimpleDateFormat("yyyyMMdd");
			SimpleDateFormat sdf_yyyy_MM_dd = new SimpleDateFormat("yyyy-MM-dd");
			ArrayList<Calendar> allTradingDate = utils.Utils.getAllTradingDate("D:\\stock data\\all trading date - hk.csv");
			
			
			int mode = 1;
			/*
			 * 0 - downloading data
			 * 1 - full backtesting
			 * 2 - drawdown analysis
			 * 3 - reshape & calculate avg volume & turnover
			 * 4 - reshape stock outstanding shares info - undone
			 * 先download数据（包括outstanding shares的数据和southbound的数据），再run mode 3，然后再run mode 1
			 */
			
			if(mode == 0) {
				//需要的数据： southbound + outstanding shares数据
				BufferedReader bf = utils.Utils.readFile_returnBufferedReader("D:\\stock data\\all stock list.csv");
				String[] stockList = {};
				for(int i = 0; i< stockList.length; i++) {
					System.out.println("Stock = " + stockList[i]);
					webDownload.GetPrice.getHistoricalData(stockList[i], stockList[i]+".csv", "D:\\stock data\\stock hist data - webb\\");
					webbDownload.outstanding.DataDownloader.dataDownloader(stockList[i]);
				}
			}
			
			if(mode == 1) {
				logger.info("============ Backtesting ============");
				int topNStocks = 15;  // # of stocks to pick for every screening
				PortfolioScreening.topNStocks = topNStocks;
				
				String dateFormat = "yyyyMMdd";
				SimpleDateFormat sdf = new SimpleDateFormat (dateFormat);
				
				SimpleDateFormat sdf2 = new SimpleDateFormat("yyyyMMdd HHmmss"); 
				String portFilePath = "D:\\stock data\\southbound flow strategy - db\\" 
						+ sdf2.format(new Date()) + " - idea3 - bactesting四 - 15stocks";
				//String mvFilePath = "D:\\stock data\\southbound flow strategy - db\\" + sdf2.format(new Date());
				File f = new File(portFilePath);
				f.mkdir();
				PortfolioScreening.outputPath = portFilePath;
				
				ArrayList<String> rebalDateArr = new ArrayList<String>();
				
				int idea =3;
				/*
				 * 0 - normal
				 * 1 - avg rank, if choose idea = 1, need to consider oneMonthBeforeDays in "PortfolioScreening"
				 * 2 - filter 
				 */
				PortfolioScreening.oneMonthBeforeDays = 20;
						
				int rebalDate = 0;  
				if(rebalDate == 0) { // please make sure that all rebalancing dates are trading date
					rebalDateArr .add("20160704");
					rebalDateArr .add("20160801");
					rebalDateArr .add("20160901");
					rebalDateArr .add("20161003");
					rebalDateArr .add("20161101");
					rebalDateArr .add("20161201");
					rebalDateArr .add("20170104");
					rebalDateArr .add("20170206");
					rebalDateArr .add("20170301");
					rebalDateArr .add("20170403");
					rebalDateArr .add("20170502");
					rebalDateArr .add("20170601");
					rebalDateArr .add("20170703");
					rebalDateArr .add("20170801");
					rebalDateArr .add("20170929");
					rebalDateArr .add("20171027");
				}else if(rebalDate == 1) {
					rebalDateArr .add("20160715");
					rebalDateArr .add("20160815");
					rebalDateArr .add("20160915");
					rebalDateArr .add("20161014");
					rebalDateArr .add("20161115");
					rebalDateArr .add("20161215");
					rebalDateArr .add("20170116");
					rebalDateArr .add("20170215");
					rebalDateArr .add("20170315");
					rebalDateArr .add("20170413"); //4.14 is a holiday
					rebalDateArr .add("20170515");
					rebalDateArr .add("20170615");
					rebalDateArr .add("20170714");
					rebalDateArr .add("20170815");
				}else if(rebalDate == 2) {
					rebalDateArr .add("20160708");
					rebalDateArr .add("20160810");
					rebalDateArr .add("20160908");  // 9.9 is a holiday
					rebalDateArr .add("20161010");
					rebalDateArr .add("20161110");
					rebalDateArr .add("20161209");
					rebalDateArr .add("20170110");
					rebalDateArr .add("20170210");
					rebalDateArr .add("20170310");
					rebalDateArr .add("20170410");
					rebalDateArr .add("20170510");
					rebalDateArr .add("20170609");
					rebalDateArr .add("20170710");
					rebalDateArr .add("20170810");
					rebalDateArr .add("20170911");
				}else if(rebalDate == 3) {
					rebalDateArr .add("20160720");
					rebalDateArr .add("20160819");
					rebalDateArr .add("20160920");
					rebalDateArr .add("20161020");
					rebalDateArr .add("20161118");
					rebalDateArr .add("20161220");
					rebalDateArr .add("20170120");
					rebalDateArr .add("20170220");
					rebalDateArr .add("20170320");
					rebalDateArr .add("20170420");
					rebalDateArr .add("20170519");
					rebalDateArr .add("20170620");
					rebalDateArr .add("20170720");
					rebalDateArr .add("20170818");
					rebalDateArr .add("20170920");
					rebalDateArr .add("20171020");
				}
				
				
				ArrayList<ArrayList<String>> data = new ArrayList<ArrayList<String>> ();
				
				
				int lastNdays = 13;     // for idea1 only，对多少天之内的数据进行average ranking
				
				int lookbackDays2 = 13; // for idea2 only  最近lookbackDays2天中至少要有minDays2 （inclusive) 满足条件
				int minDays2 = 9;		// for idea2 only
				int lookbackDays3 = 5; // for idea2 only	同时，最近lookbackDays3天中至少要有minDays3（inclusive) 满足条件
				int minDays3 = 4;		// for idea2 only
				
				boolean isOutputDailyCCASSChg = true; // 是否输出每日southbound的CCASS的change，只在idea1和idea2下有效
				FileWriter fw_dailyCCASSChg ; // 仅仅是为了初始化
				Map<String, Map<Date, Double>> dailyCCASSChg_map = new HashMap<String, Map<Date, Double>>();
				Set<Date> dailyCCASSChg_allDates = new  HashSet<>();;

				
				if(idea == 1 || idea == 2 || idea == 3) {
					/*
					 * =================================================================
					 * idea1 (average)：在每个rebalancing date，计算每只股票前lastNdays天每一天的ranking，加总之后求均值，再根据这个均值的ranking来选股票
					 */
					/*
					 * ===============================================================
					 * idea2 (filter): 给每只股票每天设一个filter1，如果当天的southbound的share是增加的，则这个filter1设为1；如果过去30天中的filter1大于0的天数大于15，则另一个filter2设为1
					 * 在rebalancing时，只考虑filter2大于0的情况
					 */
					
					Calendar rebalStart = Calendar.getInstance();
					rebalStart.setTime(sdf_yyyyMMdd.parse(rebalDateArr.get(0)));
					int rebalStartInd = allTradingDate .indexOf(rebalStart);
					
					Calendar rebalEnd = Calendar.getInstance();
					rebalEnd.setTime(sdf_yyyyMMdd.parse(rebalDateArr.get(rebalDateArr.size() - 1)));
					int rebalEndInd = allTradingDate.indexOf(rebalEnd);
					
					if(rebalEndInd  == -1 || rebalStartInd == -1) {
						logger.error("Rebalancing start date or end date is not a trading date! Cease running");
						return;
					}
					
					int rebalCount = 0;
					ArrayList<ArrayList<StockSingleDate>> allPortfolioScreeningData = new ArrayList<ArrayList<StockSingleDate>>(); 
					
					// *************** 每天都进行screening ******************
					int dayCalStart = 0;
					switch(idea) {
					case 1:
						dayCalStart = lastNdays + 1;
						break;
					case 2:
						dayCalStart = lookbackDays2 + 1;
						break;
					case 3:
						dayCalStart = 20;
						break;
					default:
						break;
					}
					int daysBetweenRelancingDate = 0;
					for(int i = rebalStartInd-dayCalStart; i <= rebalEndInd; i++) {   
						daysBetweenRelancingDate++;
						String todayDate = sdf_yyyyMMdd.format(allTradingDate.get(i).getTime());
						logger.info("\tScreening date = " + todayDate);
						
						PortfolioScreening.oneMonthBeforeDays = 1;
						ArrayList<StockSingleDate> todaySel = PortfolioScreening.assignValue_singleDate(todayDate, "yyyyMMdd"); //对每只股票进行赋值
						logger.debug("\t\ttodaySel .size() = " + todaySel .size());
						allPortfolioScreeningData.add(todaySel );
						
						int todayInd = allPortfolioScreeningData.size() - 1;
						// ===================== idea == 2 || idea == 3 ===================
						if((isOutputDailyCCASSChg || idea == 2 || idea == 3) && todayInd > 0) {  //比较昨天south bound的数据，update filter
							
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
								
								if(idea == 2 || idea == 3) {
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
								}
								
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
							
						}
						// ======= idea2 END ==========
						
						// *********** rebalancing date *************
						if(todayDate.equals(rebalDateArr.get(rebalCount))) { 
							logger.info("\t\tToday is a rebalancing date! daysBetweenRelancingDate=" + daysBetweenRelancingDate);
							ArrayList<StockSingleDate> stocksToBuy = new ArrayList<StockSingleDate> ();
							
							// ================= idea == 3 =================
							if(idea == 3) {
								Map<String, StockSingleDate> rebalStock_map = new HashMap();  // 想要在rebal那天将每只股票的rank都列出来
								for(int j = 0; j < todaySel .size(); j++) { // 先将rebalStock的框架搭出来
									StockSingleDate stock = todaySel.get(j);
									stock.dummy3 = stock.dummy1;  // dummy1是按照freefloat的排序排出来的
									stock.dummy4 = stock.dummy2;  // dummy1是按照3M ADV的排序排出来的
									stock.dummy5 = 1.0;    // 有效天数
									rebalStock_map.put(stock.stockCode, stock);
								}
								
								for(int j = 1; j < lastNdays; j++) { // 寻找最近20天的数据，不用循环j=0的情况了
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
									
									thisStock.sorting_indicator = (0
											//+ thisStock.rank1 
											+ thisStock.rank2 
											+ thisStock.rank3 
											//+ thisStock.rank4
											)/4;   // 这个句子决定了最后的排序
									thisStock.rank5 = thisStock.sorting_indicator;
								}
								Collections.sort(todaySel2, StockSingleDate.getComparator(-1));  // 降序排列
								
								/*
								for(int j = 0; j < todaySel2 .size(); j++) {
									StockSingleDate thisStock = todaySel2.get(j);
									thisStock.dummy1 = (double) j;
									thisStock.sorting_indicator = (
											//thisStock.rank1 
											//+ thisStock.rank2 
											 thisStock.rank3 
											+ thisStock.rank4
											)/4;   // 这个句子决定了最后的排序
								}
								Collections.sort(todaySel2, StockSingleDate.getComparator(-1));  // 降序排列
								
								for(int j = 0; j < todaySel2 .size(); j++) {
									StockSingleDate thisStock = todaySel2.get(j);
									thisStock.dummy2 = (double) j;
									thisStock.sorting_indicator = ( thisStock.dummy1 + thisStock.dummy2) /  4;
								}
								Collections.sort(todaySel2, StockSingleDate.getComparator(-1));  // 降序排列
								*/
								
								// 还要进行filter
								lookbackDays2 = daysBetweenRelancingDate;
								minDays2 = (int) Math.floor((double) lookbackDays2 * 0.7);
								for(int j = 0; j < lookbackDays2; j++) { // 回看过去20天
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
											if(j < lookbackDays3)  // 最近5天的情况
												thisStock.filter3 = thisStock.filter3 + 100;
										}else {
											if(findStock.dummy6 == 1.0) {
												thisStock.filter2 = thisStock.filter2 + 100; 
												if(j < lookbackDays3)  // 最近5天的情况
													thisStock.filter3 = thisStock.filter3 + 100;
											}
										}
										
										todaySel2.set(k, thisStock);
									}
								}// end of filter
								
								// 判断每只股票的符合条件的天数是否满足最小天数要求
								for(int k = 0; k < todaySel2.size(); k++	) {
									StockSingleDate thisStock = todaySel2.get(k);
									
									if(thisStock.filter2 / (lookbackDays2 * 100) >= ((double) minDays2 / lookbackDays2)
											&& thisStock.filter3 / (lookbackDays3 * 100) >= ((double) minDays3 / lookbackDays3)) {
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
							}
							// ============= idea == 3 ---- END ===============
							
							// ================= idea == 1 =================
							if(idea  == 1) {
								Map<String, StockSingleDate> rebalStock_map = new HashMap();  // 想要在rebal那天将每只股票的rank都列出来
								for(int j = 0; j < todaySel .size(); j++) { // 先将rebalStock的框架搭出来
									StockSingleDate stock = todaySel.get(j);
									stock.dummy3 = stock.sorting_indicator;
									stock.dummy4 = 1.0;
									rebalStock_map.put(stock.stockCode, stock);
								}
								
								for(int j = 1; j < lastNdays; j++) { // 寻找最近20天的数据，不用循环j=0的情况了
									ArrayList<StockSingleDate> rebal_thisDateSel = allPortfolioScreeningData.get(allPortfolioScreeningData.size() - 1 - j) ;
									//String rebal_thisDate = sdf_yyMMdd.format(allTradingDate.get(i-j).getTime());
									for(int k = 0; k < rebal_thisDateSel.size(); k++) {
										StockSingleDate thisStock = rebal_thisDateSel.get(k);
										StockSingleDate findStock =rebalStock_map.get(thisStock.stockCode);
										if(findStock != null) { // 只考虑能找到的情况
											findStock.dummy3 = findStock.dummy3 + thisStock.sorting_indicator; // sorting_indicator是每只股票在当天的综合排名，dummy3来存储20天内这个排名的总值  
											findStock.dummy4 = findStock.dummy4 + 1;  // dummy4 来存储有多少天是有效的
											rebalStock_map.put(thisStock.stockCode, findStock);
										}
									}
								}
								
								// 将每只股票最近20天的ranking加总，然后求平均值，再排序
								ArrayList<StockSingleDate> todaySel2 = new ArrayList<StockSingleDate>();
								todaySel2.addAll(rebalStock_map.values());
								logger.debug("\t\trebalStock_map.values().size() = " + rebalStock_map.values().size());
								logger.debug("\t\ttodaySel2.size() = " + todaySel2 .size());
								for(int j = 0; j < todaySel2 .size(); j++) {
									StockSingleDate thisStock = todaySel2.get(j);
									thisStock .sorting_indicator = thisStock.dummy3 / thisStock.dummy4;
								}
								Collections.sort(todaySel2, StockSingleDate.getComparator(-1));  // 降序排列
								
								stocksToBuy = PortfolioScreening.pickStocks_singleDate(todaySel2, true);
							}  // ============= idea == 1 ---- END ===============
							
							// ============== idea2 =================
							if(idea == 2) {
								//int todayInd = allPortfolioScreeningData.size() - 1;
								//ArrayList<StockSingleDate> todaySel = allPortfolioScreeningData.get(todayInd);
								
								for(int j = 0; j < lookbackDays2; j++) { // 回看过去20天
									ArrayList<StockSingleDate> thisDateSel = allPortfolioScreeningData.get(todayInd - j);
									
									Map<String, StockSingleDate> thisDateSel_map = new HashMap()	;
									for(int k = 0; k < thisDateSel.size(); k++) {
										StockSingleDate thisStock = thisDateSel.get(k);
										thisDateSel_map.put(thisStock.stockCode, thisStock);
									}
									
									for(int k = 0; k < todaySel.size(); k++	) {   // 对今天的每只股票，查看过去某一天其southbound是否增加，如果增加，则filter2增加100
										StockSingleDate thisStock = todaySel.get(k);
										StockSingleDate findStock = thisDateSel_map.get(thisStock.stockCode);
										
										if(findStock == null) {
											thisStock.filter2 = thisStock.filter2 + 100;
											if(j < lookbackDays3)  // 最近5天的情况
												thisStock.filter3 = thisStock.filter3 + 100;
										}else {
											if(findStock.dummy6 == 1.0) {
												thisStock.filter2 = thisStock.filter2 + 100; 
												if(j < lookbackDays3)  // 最近5天的情况
													thisStock.filter3 = thisStock.filter3 + 100;
											}
										}
										
										todaySel.set(k, thisStock);
									}
								} // 至此，todaySel中的filter2应该已经记录了每只股票在过去某个时间段内，southbound是增加的天数。
								
								// 判断每只股票的符合条件的天数是否满足最小天数要求
								for(int k = 0; k < todaySel.size(); k++	) {
									StockSingleDate thisStock = todaySel.get(k);
									
									if(thisStock.filter2 / (lookbackDays2 * 100) >= ((double) minDays2 / lookbackDays2)
											&& thisStock.filter3 / (lookbackDays3 * 100) >= ((double) minDays3 / lookbackDays3)) {
										thisStock.filter4 = 2.0;
									}else {
										thisStock.filter4 = -2.0;
									}
								}
								allPortfolioScreeningData.set(todayInd, todaySel);
								
								stocksToBuy = PortfolioScreening.pickStocks_singleDate(todaySel, true); // todaySel早就已经是排序好的
							}
							// ============== idea2 END =================
							
							ArrayList<String> stocksToBuy_str = new ArrayList<String>();
							for(int j = 0; j < stocksToBuy.size(); j++) {
								stocksToBuy_str.add(stocksToBuy.get(j).stockCode);
							}
							data.add(stocksToBuy_str);
							
							
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
						} // rebalancing 结束
					}
					
					/*
					 * idea1/2/3结束
					 * ===============================================================
					 */
				
				}
				
				
				if(idea == 0) /// 不用考虑rebalancing date之间的screening结果
					for(int i = 0; i < rebalDateArr.size(); i++) {
						String date = rebalDateArr.get(i);
						SimpleDateFormat sdf3 = new SimpleDateFormat("yyyyMMdd");
						Calendar benchCal = Calendar.getInstance();
						benchCal.setTime(sdf3.parse(date));
						benchCal = utils.Utils.getMostRecentDate(benchCal, allTradingDate);
						date = sdf3.format(benchCal.getTime());
						
						ArrayList<StockSingleDate> stockList = PortfolioScreening.assignValue_singleDate(date, "yyyyMMdd");
						ArrayList<StockSingleDate> stocksToBuy = PortfolioScreening.pickStocks_singleDate(stockList, true);
						ArrayList<String> stocksToBuy_str = new ArrayList<String> ();
						for(int j = 0; j < stocksToBuy .size(); j++) {
							stocksToBuy_str .add(stocksToBuy.get(j).stockCode);
						}
						data.add(stocksToBuy_str);
						
						rebalDateArr.set(i, date);
				}
				
				ArrayList<String> dateArr = new ArrayList<String>();
				dateArr.addAll(rebalDateArr);
				
				Backtesting bt = new Backtesting();
				//bt.startDate = "20160630";
				bt.startDate = rebalDateArr.get(0);
				bt.endDate = rebalDateArr.get(rebalDateArr.size() - 1);
				bt.tradingCost = 0.000;
				
				bt.rotationalTrading(dateArr, "yyyyMMdd", data);
				
				Portfolio pf = bt.portfolio;
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
				for(int i = 0; i < rebalDateArr.size(); i++) {
					if(i > 0)
						fw_stockPicks1.write(",");
					fw_stockPicks1.write(rebalDateArr.get(i));
				}
				fw_stockPicks1.write("\n");
				for(int i = 0; i < topNStocks; i++) { //第几只股票
					for(int j = 0; j < rebalDateArr.size(); j++) {
						if(j > 0)
							fw_stockPicks1.write(",");
						
						ArrayList<String> todayStocks = data.get(j);
						String stock = "";
						if(todayStocks == null || todayStocks.size() == 0) {
							continue;
						}
						if( i > (todayStocks.size() - 1))
							continue;
						stock = data.get(j).get(i);
						fw_stockPicks1.write(stock);
						
						//======= transform "data" ====
						ArrayList<String> thisStockData = data_trans.get(stock);
						if(thisStockData == null || thisStockData.size() == 0) {
							thisStockData = new ArrayList<String>();
							for(int k = 0; k < rebalDateArr.size(); k ++) {
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
				for(int i = 0; i < rebalDateArr.size(); i++) {
					fw_stockPicks2.write("," + rebalDateArr.get(i));
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
				XMLUtil.convertToXml(pf, portFilePath + "\\portfolio.xml");
				// save the every data
				//XMLUtil.convertToXml(allPortfolioScreeningData, portFilePath + "\\allPortfolioScreeningData.xml");
			}
			
			if(mode == 2) {
				String portFilePathRoot =  "D:\\stock data\\southbound flow strategy - db\\20170925 075008 - filter\\";
				String portFilePath = portFilePathRoot + "portfolio.xml";
				Portfolio pf = (Portfolio) XMLUtil.convertXmlFileToObject(Portfolio.class,portFilePath);
				
				String startDate = "20160704";
				String endDate = "20160810";
				String dateFormat = "yyyyMMdd";
				
				//DrawDownAnalysis.analysisBetweenDates_outputPath = portFilePathRoot + "drawdown_analysis " + startDate + " - " + endDate + ".csv";
				//DrawDownAnalysis.pnlAnalysisBetweenDates(pf,startDate ,endDate ,dateFormat );
				
				//DrawDownAnalysis.maxDrawdown(pf, startDate, endDate, dateFormat);
				ArrayList<Object> mvArr = pf.getMarketValue("20160101", "20171231", "yyyyMMdd");
				ArrayList<Double> mv = (ArrayList<Double>) mvArr.get(0);
				Double s = DrawDownAnalysis.sharpeRatio(mv, 0.0);
				System.out.println("Sharpe = " + s);
				
				DrawDownAnalysis.comprehensiveAnalysis(pf, portFilePathRoot + "test.csv");
			}
			
			if(mode == 3) {  // calculating stock volume and save
				logger.info("============== calculating stock volume and save ===============");
				Map<String, FileWriter> fwMap = new HashMap();  // map的key是yyyyMMdd形式的日期
				Calendar startCal = Calendar.getInstance();
				startCal.setTime(sdf_yyyyMMdd.parse("20171027"));
				Calendar startCal2 = utils.Utils.getMostRecentDate(startCal, allTradingDate);
				
				int start_ind = allTradingDate.indexOf(startCal2);
				Calendar dataStartCal = allTradingDate.get(start_ind - 60);
				startCal = (Calendar) startCal2.clone();
				
				// create these filewriters
				for(int i = 0; i < allTradingDate.size(); i++) {
					Calendar thisCal = allTradingDate.get(i);
					if(!thisCal.before(startCal)) {
						String thisCal_str = sdf_yyyyMMdd.format(thisCal.getTime());
						
						String path = "D:\\stock data\\southbound flow strategy - db\\stock avg trd vol\\";
						FileWriter fw = new FileWriter(path + thisCal_str + ".csv");
						fw.write("stock,3M avg vol(shares),3M avg turnover(value)\n");
						
						fwMap.put(thisCal_str, fw);
					}
				}
				
				// write data
				String stockDirPath = stockPrice.DataGetter.STOCK_DATA_PATH;
				File f = new File(stockDirPath );
				String[] fileList = f.list();
				for(int i = 0; i < fileList.length; i++) {
					logger.debug("File = " + fileList[i]);
					String stock = fileList[i].substring(0, fileList[i].length() - 4);
					
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
						thisCal.setTime(sdf_yyyy_MM_dd.parse(lineDataArr .get(0)));
						
						// 只提取dataStartCal往后日期的数据
						if(!thisCal.before(dataStartCal)) {
							data.add(lineDataArr);
						}
					}
					
					// 从后面往前开始读取
					for(int j = data.size() - 1 - 60; j >= 0; j--) {
						ArrayList<String> dataArr = data.get(j);
						
						String thisDate = dataArr.get(0);
						thisDate = sdf_yyyyMMdd.format(sdf_yyyy_MM_dd.parse(thisDate)); // change the format
						
						Double accVol = 0.0;
						Double accTur = 0.0;
						int numTrdDate = 0;
						for(int k = 0; k < 60; k++) {
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
						
						FileWriter fw = fwMap.get(thisDate);
						fw.write(stock+","+String.valueOf(avgVol) + "," + String.valueOf(avgTur) + "\n");
					} // end of for(j)
				}
				
				
				// close all filewriters
				Set<String> allDates = fwMap.keySet();
				for(String thisDate : allDates) {
					FileWriter fw = fwMap.get(thisDate);
					fw.close();
				}
			
			}
			
			if(mode == 4) {  // reshaping outstanding shares info - undone
				logger.info("============== reshaping outstanding shares info ===============");
				
				Map<String, FileWriter> fwMap = new HashMap();  // map的key是yyyyMMdd形式的日期
				Calendar startCal = Calendar.getInstance();
				startCal.setTime(sdf_yyyyMMdd.parse("20141201"));
				Calendar startCal2 = utils.Utils.getMostRecentDate(startCal, allTradingDate);
				
				int start_ind = allTradingDate.indexOf(startCal2);
				Calendar dataStartCal = allTradingDate.get(start_ind - 60);
				startCal = (Calendar) startCal2.clone();
				
				// create these filewriters
				for(int i = 0; i < allTradingDate.size(); i++) {
					Calendar thisCal = allTradingDate.get(i);
					if(!thisCal.before(startCal)) {
						String thisCal_str = sdf_yyyyMMdd.format(thisCal.getTime());
						
						String path = "D:\\stock data\\southbound flow strategy - db\\stock outstanding shares\\";
						FileWriter fw = new FileWriter(path + thisCal_str + ".csv");
						fw.write("stock,outstanding shares\n");
						
						fwMap.put(thisCal_str, fw);
					}
				}
				
				// write data
				String stockDirPath = "D:\\stock data\\HK CCASS - WEBB SITE\\outstanding\\";
				File f = new File(stockDirPath );
				String[] fileList = f.list();
				for(int i = 0; i < fileList.length; i++) {
					logger.debug("File = " + fileList[i]);
					String stock = fileList[i].substring(0, fileList[i].length() - 4);
					
					BufferedReader bf = utils.Utils.readFile_returnBufferedReader(stockDirPath + fileList[i]);
					String line = "";
					int count1 = 0;
					int trdDateCount = 0;
					int trdDateInd = allTradingDate.size() - 1;
					while((line = bf.readLine()) != null) {
						if(count1 == 0) {
							count1 ++;
							continue;
						}
						ArrayList<String> dataArr = new ArrayList<String>(Arrays.asList(line.split(",")));
						String thisOsShares = dataArr.get(1);
						
						Calendar thisCal = Calendar.getInstance();
						thisCal.setTime(sdf_yyyy_MM_dd.parse(dataArr.get(0)));
						
						if(thisCal.after(allTradingDate.get(trdDateInd)))
							continue;
						
						while(!allTradingDate.get(trdDateInd).before(thisCal)) {
							
						}
					}
					
				}
				
			}
			
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
