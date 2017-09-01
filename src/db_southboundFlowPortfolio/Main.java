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

import backtesting.Backtesting;
import backtesting.Portfolio;

public class Main {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			//System.out.println(southboundData.DataGetter.getStockData("700", "20170801", "yyyyMMdd").get(2));
			
			
			BufferedReader bf = utils.Utils.readFile_returnBufferedReader("D:\\stock data\\all stock list.csv");
			String[] stockList = {};
			for(int i = 0; i< stockList.length; i++) {
				System.out.println("Stock = " + stockList[i]);
				//webDownload.GetPrice.getHistoricalData(stockList[i], stockList[i]+".csv", "D:\\stock data\\stock hist data - webb\\");
				webbDownload.outstanding.DataDownloader.dataDownloader(stockList[i]);
			}
			
			
			/*
			StockSingleDate s1 = new StockSingleDate("1", "20170101", "yyyyMMdd"); 
			s1.SB_over_vol = 0.0;
			
			StockSingleDate s2 = new StockSingleDate("2", "20170101", "yyyyMMdd"); 
			s2.SB_over_vol = 10.0;
			
			StockSingleDate s3 = new StockSingleDate("3", "20170101", "yyyyMMdd"); 
			s3.SB_over_vol = 5.0;
			
			ArrayList<StockSingleDate> arr = new ArrayList<StockSingleDate>();
			arr.add(s1);
			arr.add(s2);
			arr.add(s3);
			StockSingleDate[] arr2 = {s1,s2,s3};
			
			Arrays.sort(arr2, StockSingleDate.getComparator());
			Collections.sort(arr, StockSingleDate.getComparator());
			
			if(true)
			for(int i = 0; i < arr.size(); i++) {
				StockSingleDate s = arr.get(i);
				System.out.println(s.stockCode + " " + String.valueOf(s.SB_over_vol));
			}
			
			if(false)
				for(int i = 0; i < arr2.length; i++) {
					StockSingleDate s = arr2[i];
					System.out.println(s.stockCode + " " + String.valueOf(s.SB_over_vol));
				}
			*/
			String dateFormat = "yyyyMMdd";
			SimpleDateFormat sdf = new SimpleDateFormat (dateFormat);
			
			SimpleDateFormat sdf2 = new SimpleDateFormat("yyyyMMdd HHmmss"); 
			String portFilePath = "D:\\stock data\\southbound flow strategy - db\\" + sdf2.format(new Date());
			String mvFilePath = "D:\\stock data\\southbound flow strategy - db\\" + sdf2.format(new Date());
			File f = new File(mvFilePath);
			f.mkdir();
			PortfolioScreening.outputPath = mvFilePath;
			
			ArrayList<String> rebalDateArr = new ArrayList<String>();
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
			bt.startDate = "20170331";
			bt.endDate = "20170824";
			bt.tradingCost = 0.001;
			
			bt.rotationalTrading(dateArr, "yyyyMMdd", data);
			
			Portfolio pf = bt.portfolio;
			Map<Calendar, ArrayList<Object>> histSnap = pf.histSnap;
			Set<Calendar> keys = histSnap.keySet();
			List<Calendar> keysArr = new ArrayList<Calendar>(keys);
			Collections.sort(keysArr);
			
			
			FileWriter fw = new FileWriter(portFilePath + "\\portfolio.csv");
			FileWriter fw2 = new FileWriter(mvFilePath + "\\market value.csv");
			for(Calendar date : keysArr) {
				String dateStr = sdf.format(date.getTime());
				ArrayList<Object> snapData = histSnap.get(date);
				
				Double marketValue = (Double) snapData.get(0);
				Double cash = (Double) snapData.get(1);
				Map<String, Double> stockHeld = (Map<String, Double>) snapData.get(2);
				
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
					Double holding = stockHeld.get(stock);
					
					//System.out.println("    stock = " + stock + " amt = " + holding);
					fw.write(",," + stock + "," + holding + "\n");
				}
				
				fw2.write(dateStr + "," + String.valueOf(marketValue) + "\n");
			}
			fw.close();
			fw2.close();
			
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
