package ashare.t_2limitup;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;

import org.apache.log4j.Logger;

public class PerformanceMetrics {
	/*
	 * 为了测试涨停板T-2策略的表现，输入的文件是存储所有trade的csv文件，trade是从AmiBroker的report文件中copy下来的
	 * 
	 */
	
	
	public static Logger logger = Logger.getLogger(PerformanceMetrics.class);
	
	public static ArrayList<TradeRecord> tradeRecords = new ArrayList<TradeRecord>();
	public static int rollingPeriod = 20;   // trading days
	public static int rollingNumTrades = 20; // num of trades
	
	public static ArrayList<Date> tradingDate = new ArrayList<Date> (); 
	public static LinkedHashMap<Date, Double> rollingTotalTrades = new LinkedHashMap<Date, Double>(); 
	public static LinkedHashMap<Date, Double> rollingHitRate = new LinkedHashMap<Date, Double>(); 
	public static LinkedHashMap<Date, Double> rollingAvgWinProfit = new LinkedHashMap<Date, Double>(); 
	public static LinkedHashMap<Date, Double> rollingAvgLoseProfit= new LinkedHashMap<Date, Double>(); 
	public static LinkedHashMap<Date, Double> rollingTotalEquity = new LinkedHashMap<Date, Double>(); 
	public static SimpleDateFormat sdf = new SimpleDateFormat ("dd/MM/yyyy");
	
	public static Double initialEquity = 10000.0;
	
	public static void main(String[] args) {
		String rootPath = "T:\\A share strategy\\T-2 backtest\\";
		readTrades(rootPath + "trades.csv");
		int mode = 2;
		getRollingData(mode);
		output(rootPath + "analysis.csv");
	}
	
	public static void readTrades(String fileName) {
		try {
			BufferedReader bf = utils.Utils.readFile_returnBufferedReader(fileName);
			String line = "";
			int count = 0;
			
			while((line = bf.readLine()) != null) {
				if(count == 0) {
					count ++;
					continue;
				}
				
				String[] lineArr = line.split(",");
				
				int ind = Math.floorMod(count, 2);
				if(ind == 1) {  //奇数行
					String stock = lineArr[0];
					String buyDateStr = lineArr[2];
					String sellDateStr = lineArr[3];
					String profitStr = lineArr[5];
					String sharesStr = lineArr[6];
					String posValStr = lineArr[7];
					String cumProfitStr = lineArr[8];
					
					TradeRecord tr = new TradeRecord ();
					tr.stock = stock;
					tr.buyDate = sdf.parse(buyDateStr);
					tr.sellDate = sdf.parse(sellDateStr);
					tr.profit = Double.parseDouble(profitStr);
					tr.numShares = Double.parseDouble(sharesStr);
					tr.posValue = Double.parseDouble(posValStr);
					tr.cumProfit = Double.parseDouble(cumProfitStr);
					tradeRecords.add(tr);
					
					if(tradingDate.indexOf(tr.sellDate) == -1)
						tradingDate.add(tr.sellDate);
				}else { // 偶数行
					String buyPrice = lineArr[2];
					String sellPrice = lineArr[3];
					String profitPctStr = lineArr[5];
					
					TradeRecord tr = tradeRecords.get(tradeRecords.size() - 1);
					tr.buyPrice = Double.parseDouble(buyPrice);
					tr.sellPrice = Double.parseDouble(sellPrice);
					tr.profitPercent = Double.parseDouble(profitPctStr.substring(0, profitPctStr.length()-1)) / 100;
					
					tradeRecords.set(tradeRecords.size()-1, tr);
				}
				count++;
				
			} // end of WHILE
			bf.close();
		}catch(Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 以固定天数为为一个period，进行roll
	 * mode
	 * 1 - get rolling data by data, needs to define "rollingPeriod"
	 * 2 - get rolling data by trades, needs to define "rollingNumTrades"
	 * @param mode
	 */
	public static void getRollingData(int mode) {
		try {
			//mode = 1;
			
			if(tradeRecords == null || tradeRecords.size() == 0)
				return;
			
			ArrayList<TradeRecord> rollingRecords = new ArrayList<TradeRecord>(); 
			
			Date rollingEndDate = tradingDate.get(rollingPeriod-1);
			int rollingEndInd = 0;
			if(mode == 1)
				rollingEndInd = rollingPeriod-1;   // for mode 1
			if(mode == 2)
				rollingEndInd = rollingNumTrades - 1; // for mode 2
				
			Double currentEquity = initialEquity;
			for(int i = 0; i < tradeRecords.size(); i++) {
				TradeRecord thisTr = tradeRecords.get(i);
				currentEquity = initialEquity +  thisTr.cumProfit;
				
				Date thisDate = thisTr.sellDate;
				logger.info("stock=" + thisTr.stock + "   date=" + sdf.format(thisDate)  );
				
				// -------- 填充rolling records -----------
				if(mode  == 1 && !thisDate.after(rollingEndDate)) {  // this date 在rollingEndDate前面
					rollingRecords.add(thisTr);
				}
				if(mode == 2 && i <= rollingEndInd) {
					rollingRecords.add(thisTr);
				}
				
				// ----------- rolling records 填充完毕，开始分析，并将rollingInd推到下一个 -------
				boolean isFilled  = false;
				if(mode == 1 && (thisDate.after(rollingEndDate) || i == tradeRecords.size() - 1 ))
					isFilled = true;
				if(mode == 2 && i == rollingEndInd)
					isFilled = true;
				
				if(isFilled ){  //分析完之后再向前roll trding date
					Date lastDate = rollingRecords.get(rollingRecords.size() - 1).sellDate;
					
					ArrayList<Double> data = analyzeTrades(rollingRecords);
					double totalTrades = data.get(0);
					double hitRate = data.get(2);
					double avgWinProfit = data.get(3);
					double avgLoseProfit = data.get(4);
					
					rollingTotalTrades.put(lastDate, totalTrades);
					rollingHitRate.put(lastDate, hitRate);
					rollingAvgWinProfit.put(lastDate, avgWinProfit);
					rollingAvgLoseProfit.put(lastDate, avgLoseProfit);
					rollingTotalEquity.put(lastDate, currentEquity);
					
					if(mode == 1) {
						// 然后去除最前一天的
						Date firstDate = rollingRecords.get(0).sellDate;
						//ArrayList<TradeRecord> rollingRecords_copy = (ArrayList<TradeRecord>) rollingRecords.clone(); 
						Set<TradeRecord> toDelete = new HashSet();
						for(int j = 0; j < rollingRecords.size();j++) {
							TradeRecord tr = rollingRecords.get(0);
							if(tr.sellDate.equals(firstDate)) {
								toDelete.add(tr);
								rollingRecords.remove(0);
								logger.trace("       to delete: stock=" + tr.stock + " date=" + sdf.format(tr.sellDate));
							}
							else
								break;
						}
						
						if(i != tradeRecords.size() - 1) {
							rollingEndInd++;
							rollingEndDate = tradingDate.get(rollingEndInd);
						}
						
						rollingRecords.add(thisTr);
					}
					if(mode == 2) { // 除去第一个record，然后加上最后一个record
						rollingRecords.remove(0);
						//rollingRecords.add(thisTr);
						rollingEndInd++;
					}
					
					
				}
				
				if(i > 500)
					Thread.sleep(0);
			} // end of FOR
		}catch(Exception e) {
			e.printStackTrace();
		}
		
	}
	
	public static ArrayList<Double> analyzeTrades(ArrayList<TradeRecord> rollingRecords) {
		ArrayList<Double> data = new ArrayList<Double> (); 
		
		double totalTrades = 0.0;  totalTrades = rollingRecords.size();
		double winTrades = 0.0;
		double winRate = 0.0;
		double avgWinProfit = 0.0;
		double avgLoseProfit = 0.0;
		
		ArrayList<Double> equity = new ArrayList<Double> ();
		
		for(TradeRecord tr : rollingRecords) {
			if(tr.profit >= 0) {
				winTrades += 1;
				avgWinProfit += tr.profitPercent;
			}else {
				avgLoseProfit += tr.profitPercent;
			}
		}
		
		winRate = winTrades / totalTrades;
		avgWinProfit = avgWinProfit / winTrades;
		avgLoseProfit = avgLoseProfit / (totalTrades - winTrades);
				
		data.add(totalTrades);
		data.add(winTrades);
		data.add(winRate);
		data.add(avgWinProfit);
		data.add(avgLoseProfit);
		
		return data;
	}

	public static void output(String fileName) {
		try {
			FileWriter fw = new FileWriter(fileName);
			fw.write("date,total trades,hitRate,avg win profit,avg lose loss,cum equity\n");
			Set<Date> dateSet = rollingTotalTrades.keySet();
			for(Date date : dateSet) {
				String dateStr = sdf.format(date);
				fw.write(dateStr + "," + rollingTotalTrades.get(date) + "," + rollingHitRate.get(date) + "," + rollingAvgWinProfit.get(date) + "," + rollingAvgLoseProfit.get(date) + "," + rollingTotalEquity.get(date) +  "\n");
			}
			fw.close();
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
}
