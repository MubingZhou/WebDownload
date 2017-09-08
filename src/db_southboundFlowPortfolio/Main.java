package db_southboundFlowPortfolio;

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
			
			int mode = 2;
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
				rebalDateArr .add("20170510");
				rebalDateArr .add("20170615");
				rebalDateArr .add("20170717");
				rebalDateArr .add("20170821");
				
				ArrayList<ArrayList<String>> data = new ArrayList<ArrayList<String>> ();
				for(int i = 0; i < rebalDateArr.size(); i++) {
					ArrayList<String> stockToBuy = PortfolioScreening.portfolioScreening_singleDate(rebalDateArr.get(i), "yyyyMMdd");
					data.add(stockToBuy);
				}
				
				ArrayList<String> dateArr = new ArrayList<String>();
				dateArr.addAll(rebalDateArr);
				
				Backtesting bt = new Backtesting();
				//bt.startDate = "20160630";
				bt.startDate = rebalDateArr.get(0);
				bt.endDate = "20170824";
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
				String portFilePathRoot =  "D:\\stock data\\southbound flow strategy - db\\20170907 150710\\";
				String portFilePath = portFilePathRoot + "portfolio.xml";
				Portfolio pf = (Portfolio) XMLUtil.convertXmlFileToObject(Portfolio.class,portFilePath);
				
				String startDate = "20161201";
				String endDate = "20170101";
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
