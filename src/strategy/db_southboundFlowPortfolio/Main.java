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
import java.util.List;
import java.util.Map;
import java.util.Set;

import backtesting.analysis.DrawDownAnalysis;
import backtesting.backtesting.Backtesting;
import backtesting.backtesting.Trade;
import backtesting.portfolio.Portfolio;
import backtesting.portfolio.PortfolioOneDaySnapshot;
import backtesting.portfolio.Underlying;
import utils.XMLUtil;

public class Main {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			//System.out.println(southboundData.DataGetter.getStockData("700", "20170801", "yyyyMMdd").get(2));
			
			int mode = 1;
			/*
			 * 0 - downloading data
			 * 1 - full backtesting
			 * 2 - drawdown analysis
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
				if(rebalDate == 0) {
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
					/*
					rebalDateArr .add("20170403");
					rebalDateArr .add("20170510");
					rebalDateArr .add("20170615");
					rebalDateArr .add("20170717");
					rebalDateArr .add("20170801");
					 */
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
				ArrayList<Calendar> allTradingDate = utils.Utils.getAllTradingDate("D:\\stock data\\all trading date - hk.csv");
				
				for(int i = 0; i < rebalDateArr.size(); i++) {
					String date = rebalDateArr.get(i);
					SimpleDateFormat sdf3 = new SimpleDateFormat("yyyyMMdd");
					Calendar benchCal = Calendar.getInstance();
					benchCal.setTime(sdf3.parse(date));
					benchCal = utils.Utils.getMostRecentDate(benchCal, allTradingDate);
					date = sdf3.format(benchCal.getTime());
					
					ArrayList<String> stockToBuy = PortfolioScreening.portfolioScreening_singleDate(date, "yyyyMMdd");
					data.add(stockToBuy);
					
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
			
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
