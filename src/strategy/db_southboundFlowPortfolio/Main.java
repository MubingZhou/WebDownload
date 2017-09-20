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
			 * 3 - calculate avg volume & turnover
			 */
			
			if(mode == 0) {
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
				int topNStocks = 20;  // # of stocks to pick for every screening
				PortfolioScreening.topNStocks = topNStocks;
				
				String dateFormat = "yyyyMMdd";
				SimpleDateFormat sdf = new SimpleDateFormat (dateFormat);
				
				SimpleDateFormat sdf2 = new SimpleDateFormat("yyyyMMdd HHmmss"); 
				String portFilePath = "D:\\stock data\\southbound flow strategy - db\\" + sdf2.format(new Date());
				String mvFilePath = "D:\\stock data\\southbound flow strategy - db\\" + sdf2.format(new Date());
				File f = new File(mvFilePath);
				f.mkdir();
				PortfolioScreening.outputPath = mvFilePath;
				
				ArrayList<String> rebalDateArr = new ArrayList<String>();
				
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
					rebalDateArr .add("20170901");
					rebalDateArr .add("20170912");
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
				}
				
				
				ArrayList<ArrayList<String>> data = new ArrayList<ArrayList<String>> ();
				
				/*
				 * idea1：在每个rebalancing date，计算每只股票前lastNdays天每一天的ranking，加总之后求均值，再根据这个均值的ranking来选股票
				 */
				int lastNdays = 20;
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
				for(int i = rebalStartInd-lastNdays; i <= rebalEndInd; i++) {
					String todayDate = sdf_yyyyMMdd.format(allTradingDate.get(i).getTime());
					logger.info("\tScreening date = " + todayDate);
					
					ArrayList<StockSingleDate> todaySel = PortfolioScreening.assignValue_singleDate(todayDate, "yyyyMMdd"); //对每只股票进行赋值
					logger.debug("\t\ttodaySel .size() = " + todaySel .size());
					allPortfolioScreeningData.add(todaySel );
					
					if(todayDate.equals(rebalDateArr.get(rebalCount))) { // rebalancing date
						logger.info("\t\tToday is a rebalancing date!");
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
						
						ArrayList<StockSingleDate> stocksToBuy = PortfolioScreening.pickStocks_singleDate(todaySel2, true);
						ArrayList<String> stocksToBuy_str = new ArrayList<String>();
						for(int j = 0; j < stocksToBuy.size(); j++) {
							stocksToBuy_str.add(stocksToBuy.get(j).stockCode);
						}
						data.add(stocksToBuy_str);
						
						rebalCount ++;
					} // rebalancing 结束
				}
				
				/*
				 * idea1结束
				 */
				
				if(false) /// 不用考虑rebalancing date之间的screening结果
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
				bt.endDate = "20170913";
				bt.tradingCost = 0.000;
				
				bt.rotationalTrading(dateArr, "yyyyMMdd", data);
				
				Portfolio pf = bt.portfolio;
				Map<Calendar, PortfolioOneDaySnapshot> histSnap = pf.histSnap;
				Set<Calendar> keys = histSnap.keySet();
				List<Calendar> keysArr = new ArrayList<Calendar>(keys);
				Collections.sort(keysArr);
				
				// ==== output market value & portfolio ======
				FileWriter fw = new FileWriter(portFilePath + "\\portfolio.csv");
				FileWriter fw2 = new FileWriter(mvFilePath + "\\market value.csv");
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
						
						String stock = data.get(j).get(i);
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
			}
			
			if(mode == 2) {
				String portFilePathRoot =  "D:\\stock data\\southbound flow strategy - db\\20170915 131922\\";
				String portFilePath = portFilePathRoot + "portfolio.xml";
				Portfolio pf = (Portfolio) XMLUtil.convertXmlFileToObject(Portfolio.class,portFilePath);
				
				String startDate = "20170420";
				String endDate = "20170720";
				String dateFormat = "yyyyMMdd";
				DrawDownAnalysis.analysisBetweenDates_outputPath = portFilePathRoot + "drawdown_analysis " + startDate + " - " + endDate + ".csv";
				DrawDownAnalysis.pnlAnalysisBetweenDates(pf,startDate ,endDate ,dateFormat );
			}
			
			if(mode == 3) {  // calculating stock volume and save
				logger.info("============== calculating stock volume and save ===============");
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
						
						String path = "D:\\stock data\\southbound flow strategy - db\\stock avg trd vol\\";
						FileWriter fw = new FileWriter(path + thisCal_str + ".csv");
						fw.write("stock,3M avg vol(shares),3M avg turnover(value)\n");
						
						fwMap.put(thisCal_str, fw);
					}
				}
				
				// write data
				String stockDirPath = "D:\\stock data\\stock hist data - webb\\";
				File f = new File(stockDirPath );
				String[] fileList = f.list();
				for(int i = 0; i < fileList.length; i++) {
					logger.debug("File = " + fileList[i]);
					String stock = fileList[i].substring(0, fileList[i].length() - 4);
					
					BufferedReader bf = utils.Utils.readFile_returnBufferedReader(stockDirPath + fileList[i]);
					
					int ind0 = 0;
					int ind1 = 0;
					ArrayList<ArrayList<String>> data = new ArrayList<ArrayList<String>> ();
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
			
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
