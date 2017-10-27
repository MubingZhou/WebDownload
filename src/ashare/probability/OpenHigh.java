package ashare.probability;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

public class OpenHigh {

	public static void main(String[] args) {
		Logger logger = Logger.getLogger(CountAShareLimitUp.class.getName());
		SimpleDateFormat sdf0 = new SimpleDateFormat("yyyyMMdd hh_mm_ss"); 
		
		try {
			String rootPath = "T:\\Mubing\\stock data\\A share data\\";
			
			SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
			Date date2016Bgn = sdf.parse("01/01/2016");
			Date date2017Bgn = sdf.parse("01/01/2017");
			Date date2016End = sdf.parse("31/12/2016");
			
			//String stockListPath = "Z:\\Mubing\\stock data\\A share data\\stock list.csv";
			String stockListPath = rootPath + "northbound stock.csv";
			BufferedReader bf1 = utils.Utils.readFile_returnBufferedReader(stockListPath);
			String line1 = bf1.readLine();
			ArrayList<String> stockList = new ArrayList<String>(Arrays.asList(line1.split(",")));
			//stockList = new ArrayList<String>();
			//stockList .add("000639");
			
			String stockDataRootPath = rootPath + "historical data\\";
		
			
			// ---------- start ----------
			String[] groups = {"below 5%","-5% ~ -4%","-4% ~ -3%", "-3% ~ -2%", "-2% ~ -1.5%", "-1.5 ~ -1%", "-1% ~ -0.5%", "-0.5 ~ 0%"
					, "0% ~ 0.5%", "0.5% ~ 1%", "1% ~ 1.5%", "1.5% ~ 2%", "2% ~ 3%", "3% ~ 4%", "4% ~ 5%", "above 5%"	
					};
			Date now = new Date();
			FileWriter fw = new FileWriter(rootPath + "\\open high research\\yesterday up " + sdf0.format(now) + ".csv");
			FileWriter fw_stock = new FileWriter(rootPath + "\\open high research\\stock records " + sdf0.format(now) + ".csv");
			
			fw_stock.write("stock,date,openChg,highChg,lowChg,closeChg,return,cum return\n");
			fw.write("If T-1 close is 6% higher than T-2 close and not limit up at T-1, check the open distribution of T day\n");
			
			int totalFreq = 0;
			int[] groupFreq_openChg = {0,0,0,0,0,0,0,0,  0,0,0,0,0,0,0,0};
			int[] groupFreq_highChg = {0,0,0,0,0,0,0,0,  0,0,0,0,0,0,0,0};
			int[] groupFreq_lowChg = {0,0,0,0,0,0,0,0,  0,0,0,0,0,0,0,0};
			int[] groupFreq_closeChg = {0,0,0,0,0,0,0,0,  0,0,0,0,0,0,0,0};
			
			int numSimTrades = 0;
			double cumRetSimTrades = 0.0;
			
			for(int i = 0; i < stockList.size(); i++) {
				String stock = stockList.get(i);
				String stockDataPath = stockDataRootPath + NoUse_BBG_AMB.tdxStockName(stock) + ".csv";
				logger.info("i=" + i + " stock=" + stock);
				
				ArrayList<String> dateArr = new ArrayList<String> ();
				ArrayList<Double> openArr = new ArrayList<Double> ();
				ArrayList<Double> highArr = new ArrayList<Double> ();
				ArrayList<Double> lowArr = new ArrayList<Double> ();
				ArrayList<Double> closeArr = new ArrayList<Double> ();
				ArrayList<Double> volArr =  new ArrayList<Double> ();
				ArrayList<Double> shArr =  new ArrayList<Double> ();  // share outstanding
				
				ArrayList<Integer> isLimitUpArr = new ArrayList<Integer> ();
				ArrayList<Double> rocArr = new ArrayList<Double> ();
			
				BufferedReader bf2 = utils.Utils.readFile_returnBufferedReader(stockDataPath);
				String line = "";
				int count = 0;
				while((line = bf2.readLine()) != null) {
					String[] lineArr = line.split(",");
					
					String date = lineArr[0];
					Date thisDate = new SimpleDateFormat("dd/MM/yyyy").parse(date);
					
					// ---------- 只考虑2017年的情况 -----------
					if(thisDate.before(date2017Bgn)) {
						//count++;
						//continue;
					}
					
					Double open = Double.parseDouble(lineArr[1]);
					Double high = Double.parseDouble(lineArr[2]);
					Double low = Double.parseDouble(lineArr[3]);
					Double close = Double.parseDouble(lineArr[4]);
					Double volume = Double.parseDouble(lineArr[5]);
					
					dateArr.add(date);
					openArr.add(open);
					highArr.add(high);
					lowArr.add(low);
					closeArr.add(close);
					volArr.add(volume);
					
					if(count == 0 ) {
						count ++;
						isLimitUpArr.add(0);
						rocArr.add(0.0);
						continue;
					}
					
					// ---------- update the statistics ----------
					Double lastClose = closeArr.get(count - 1);
					boolean isLimitUp = false;
					boolean closeHigh = false;
					
					if(close >= 1.096 * lastClose && high.equals(close)) {
						isLimitUp = true;
						isLimitUpArr.add(1);
					}else {
						isLimitUpArr.add(0);
					}
						
					Double priceChg = close / lastClose - 1;
					if(priceChg >= 0.06)
						closeHigh = true;
					rocArr.add(priceChg);
					
					// -------- 判断昨天是否符合要求 ----------
					int recentHighUp = 0;
					for(int j = count - 2; j >= count - 22 && j > 0; j--) {
						//System.out.println("       j=" + j);
						if(rocArr.get(j) > 0.04 ) {
							recentHighUp  ++ ;
						}
							
					}
					if(count >= 23
							&& isLimitUpArr.get(count - 2) != 1 
							//&& highArr.get(count - 2) / closeArr.get(count - 2) - 1 > 0.02
							//&& openArr.get(count - 2) / openArr.get(count - 3) - 1 < 0.01
							&& recentHighUp <= 1
							&& rocArr.get(count - 2) > 0.04 
							//&& rocArr.get(count - 2) < 0.07
							&& rocArr.get(count - 1) > -0.02
							&& rocArr.get(count - 1) < 0.02
							) {
						double openChg = open / lastClose - 1;
						double highChg = high / lastClose - 1;
						double lowChg = low / lastClose - 1;
						double closeChg = close / lastClose - 1;
						
						totalFreq++;
						
						allocateGroup(openChg, groupFreq_openChg);
						allocateGroup(highChg, groupFreq_highChg);
						allocateGroup(lowChg, groupFreq_lowChg);
						allocateGroup(closeChg, groupFreq_closeChg);
						
						// 模拟交易
						numSimTrades ++;
						double ret = 0.0;
						double openStopProfit = 0.01;
						double stopProfit1 = 0.02;
						double stopProfit2 = 0.05;
						
						if(openChg >= openStopProfit) {
							ret = openChg;
						}else {
							if(highChg >= stopProfit2)
								ret = (stopProfit1 + stopProfit2) / 2;
							if(highChg >= stopProfit1 && highChg < stopProfit2)
								ret = (stopProfit1 + closeChg ) / 2;
							if(highChg < stopProfit1)
								ret = closeChg;
						}
						ret -= 0.002;  // transaction cost
						
						cumRetSimTrades += ret;
						
						fw_stock.write(stock  + "," + date + "," + openChg  + "," + highChg + "," + lowChg + "," + closeChg + "," + ret + "," + cumRetSimTrades + "\n" );
						
					}
					count++;
					
				} // end of WHILE
				bf2.close();
				
			} // end of FOR
			
			fw.write("Total freq=," + totalFreq + "\n");
			fw.write(",open,high,low,close\n");
			for(int i = 0; i < groupFreq_openChg.length; i++) {
				fw.write(groups[i] + "," 
						+ groupFreq_openChg[i] / (double) totalFreq + "," 
						+ groupFreq_highChg[i] / (double) totalFreq + "," 
						+ groupFreq_lowChg[i] / (double) totalFreq + "," 	
						+ groupFreq_closeChg[i] / (double) totalFreq + "," 	
						+ "\n");
			}
			//fw.write(headLine + "\n");
			//fw.write(data1 + "\n");
			//fw.write(data2 + "\n");
			
			fw.write("# of trades=," + numSimTrades + ",avg return=," + cumRetSimTrades / numSimTrades + ",cum return=," + cumRetSimTrades);
			fw.close();
			fw_stock.close();
			
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		

	}
	
	private static void allocateGroup(double indicator, int[] groupFreq) {
		if(indicator < -0.05) {
			groupFreq[0] += 1;
		}
		if(indicator < -0.04 && indicator >= -0.05) {
			groupFreq[1] += 1;
		}
		if(indicator < -0.03 && indicator >= -0.04) {
			groupFreq[2] += 1;
		}
		if(indicator < -0.02 && indicator >= -0.03) {
			groupFreq[3] += 1;
		}
		if(indicator < -0.015 && indicator >= -0.02) {
			groupFreq[4] += 1;
		}
		if(indicator < -0.01 && indicator >= -0.015) {
			groupFreq[5] += 1;
		}
		if(indicator < -0.005 && indicator >= -0.01) {
			groupFreq[6] += 1;
		}
		if(indicator < 0.0 && indicator >= -0.005) {
			groupFreq[7] += 1;
		}
		if(indicator < 0.005 && indicator >= 0.0) {
			groupFreq[8] += 1;
		}
		if(indicator < 0.01 && indicator >= 0.005) {
			groupFreq[9] += 1;
		}
		if(indicator < 0.015 && indicator >= 0.01) {
			groupFreq[10] += 1;
		}
		if(indicator < 0.02 && indicator >= 0.015) {
			groupFreq[11] += 1;
		}
		if(indicator < 0.03 && indicator >= 0.02) {
			groupFreq[12] += 1;
		}
		if(indicator < 0.04 && indicator >= 0.03) {
			groupFreq[13] += 1;
		}
		if(indicator < 0.05 && indicator >= 0.04) {
			groupFreq[14] += 1;
		}
		if(indicator >= 0.05) {
			groupFreq[15] += 1;
		}
	}

}
