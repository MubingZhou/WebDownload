package test_no_use;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

public class CountAShareLimitUp {
	public static void main(String[] args) {
		Logger logger = Logger.getLogger(CountAShareLimitUp.class.getName());
		try {
			String rootPath = "Z:\\Mubing\\stock data\\A share data\\";
			
			SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
			Date date2016Bgn = sdf.parse("01/01/2016");
			Date date2017Bgn = sdf.parse("01/01/2017");
			Date date2016End = sdf.parse("31/12/2016");
			
			//String stockListPath = "Z:\\Mubing\\stock data\\A share data\\stock list.csv";
			String stockListPath = "Z:\\Mubing\\stock data\\A share data\\northbound stock.csv";
			BufferedReader bf1 = utils.Utils.readFile_returnBufferedReader(stockListPath);
			String line1 = bf1.readLine();
			ArrayList<String> stockList = new ArrayList<String>(Arrays.asList(line1.split(",")));
			//stockList = new ArrayList<String>();
			//stockList .add("000639");
			
			String stockDataRootPath = "Z:\\Mubing\\stock data\\A share data\\historical data\\";
			
			// ----- get listing date first -----
			Map<String, Date> listingMap = new HashMap<String, Date>();
			String listingPath = "Z:\\Mubing\\stock data\\A share data\\listing date.csv";
			BufferedReader bfff = utils.Utils.readFile_returnBufferedReader(listingPath);
			line1 = "";
			int c = 0;
			while((line1 = bfff.readLine()) != null) {
				if(c == 0) {
					c++;
					continue;
				}
				String[] arr = line1.split(",");
				String stock = arr[0];
				String dateStr = arr[1];
				Date date = new SimpleDateFormat("dd/MM/yyyy").parse(dateStr);
				listingMap.put(stock, date);
			}
			bfff.close();
			
			// ------- get trading date -------
			ArrayList<Date> trdDate = new ArrayList<Date>();
			String trdDatePath = "Z:\\Mubing\\stock data\\A share data\\trading date.csv";
			BufferedReader bffff = utils.Utils.readFile_returnBufferedReader(trdDatePath);
			line1 = bffff.readLine();
			bffff.close();
			String[] line1Arr = line1.split(",");
			for(int i = 0; i < line1Arr.length; i++) {
				trdDate.add(sdf.parse(line1Arr[i]));
			}
			
			
			// ---------------------
			FileWriter fw1Limit = new FileWriter("Z:\\Mubing\\stock data\\A share data\\consecutive 1 limit ups.csv");
			FileWriter fw2Limit = new FileWriter("Z:\\Mubing\\stock data\\A share data\\consecutive 2 limit ups.csv");
			FileWriter fw3Limit = new FileWriter("Z:\\Mubing\\stock data\\A share data\\consecutive 3 limit ups.csv");
			FileWriter fw4Limit = new FileWriter("Z:\\Mubing\\stock data\\A share data\\consecutive 4 limit ups.csv");
			int num1LimitAll = 0;
			int num2LimitAll = 0;
			int num3LimitAll = 0;
			int num4LimitAll = 0;
			
			int num1Limit2016 = 0;
			int num2Limit2016 = 0;
			int num3Limit2016 = 0;
			int num4Limit2016 = 0;
			
			int num1Limit2017 = 0;
			int num2Limit2017 = 0;
			int num3Limit2017 = 0;
			int num4Limit2017 = 0;
			
			ArrayList<Double> returnDist1stLimitUp = new ArrayList<Double>();   // 第一次limit up之后的return分布，只记录return
			ArrayList<Double> returnDist2ndLimitUp = new ArrayList<Double>();   // 第2次limit up之后的return分布，只记录return
			ArrayList<Double> returnDist3rdLimitUp = new ArrayList<Double>();
			ArrayList<Double> returnDist4thLimitUp = new ArrayList<Double>();
			
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
				ArrayList<Integer> consecutiveIsLimitUpArr = new ArrayList<Integer>(); 
				
				Date firstDate = listingMap.get(stock + " CH Equity");
				logger.trace("first date" + sdf.format(firstDate));
				
				BufferedReader bf2 = utils.Utils.readFile_returnBufferedReader(stockDataPath);
				String line = "";
				int count = 0;
				boolean isLimitUpFromSuspend = false; // 是不是因为复牌导致的limit up
				boolean isLimitUpChain = false; // 是否处于一个涨停的chain中
				while((line = bf2.readLine()) != null) {
					String[] lineArr = line.split(",");
					
					String date = lineArr[0];
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
					
					Date thisDate = new SimpleDateFormat("dd/MM/yyyy").parse(date);
					long _90Days = 90;
					boolean isNew = false;
					long diff = (thisDate.getTime() - firstDate.getTime()) / 1000 / 60 / 60 / 24;
					if( diff <= _90Days) {
						isNew = true;
					}
					logger.trace("   diff = " + diff + " isNew=" + isNew + " _90Days=" + _90Days);
					//System.out.println("count = " + count); 
					
					if(count == 0 || isNew) {
						isLimitUpArr.add(0);
						consecutiveIsLimitUpArr.add(0);
					}else {
						Double lastClose = closeArr.get(count-1);
						
						Double lastVolume = volArr.get(count - 1);
						
						String lastDateStr = dateArr.get(count - 1);
						Date lastDate = new SimpleDateFormat("dd/MM/yyyy").parse(lastDateStr);
						Date lastMktDate = trdDate.get(trdDate.indexOf(thisDate) - 1);
						boolean isRecoverFromSuspend = false;   // 是不是刚刚从停牌中复牌
						if(!lastMktDate.equals(lastDate) || lastVolume.equals(0.0))
							isRecoverFromSuspend = true;
						
						
						//if(count > 420)
						logger.trace(date + " " + open + " " + high + " " + low + " " + close + " " + lastClose); 
						
						int lastLimitUp = isLimitUpArr.get(count  - 1);
						if(lastLimitUp == 1) {
							isLimitUpChain  =true;  // 正处于一个涨停chain中
						}else {
							isLimitUpChain  = false;
						}
						
						boolean isLimitUp = false;
						if(close >= 1.096 * lastClose && high.equals(close))
							isLimitUp = true;
						
						if(!isLimitUpFromSuspend) {
							if(isLimitUp && isRecoverFromSuspend)
								isLimitUpFromSuspend = true;
						}
						if(!isLimitUp) {
							isLimitUpFromSuspend=false;  // 如果没哟limit up，则将这个标志设为false
						}
							
						// --------- 是不是雄安时间段 -------
						Date xiongAnStart = sdf.parse("01/04/2017");
						Date xiongAnEnd = sdf.parse("18/04/2017");
						boolean isXiongAnPeriod = false;
						if(thisDate.after(xiongAnStart) && thisDate.before(xiongAnEnd))
							isXiongAnPeriod = true;
						
						// ----------涨停 ------------
						if(isLimitUp && !isLimitUpFromSuspend && !isXiongAnPeriod) {  // limit up
							logger.trace("  is limit up");
							
							isLimitUpArr.add(1);
							int lastCLimitUp = consecutiveIsLimitUpArr.get(count-1);
							
							consecutiveIsLimitUpArr.add(lastCLimitUp + 1);
							
							if(lastCLimitUp + 1 == 1 ) {
								fw1Limit.write(stock + "," + date + "\n");
								
								num1LimitAll++;
								if(thisDate.before(date2016End))
									num1Limit2016++;
								if(thisDate.after(date2017Bgn))
									num1Limit2017++;
							}
							if(lastCLimitUp + 1 == 2 && isLimitUpChain) {
								fw2Limit.write(stock + "," + date + "\n");
								
								num2LimitAll++;
								if(thisDate.before(date2016End))
									num2Limit2016++;
								if(thisDate.after(date2017Bgn))
									num2Limit2017++;
							}
							if(lastCLimitUp + 1 == 3 && isLimitUpChain) {
								fw3Limit.write(stock + "," + date + "\n");
								
								num3LimitAll++;
								if(thisDate.before(date2016End))
									num3Limit2016++;
								if(thisDate.after(date2017Bgn))
									num3Limit2017++;
							}
							if(lastCLimitUp + 1 == 4 && isLimitUpChain) {
								fw4Limit.write(stock + "," + date + "\n");
								
								num4LimitAll++;
								if(thisDate.before(date2016End))
									num4Limit2016++;
								if(thisDate.after(date2017Bgn))
									num4Limit2017++;
							}
							
						}else {
							isLimitUpArr.add(0);
							consecutiveIsLimitUpArr.add(0);
						}
						
						// ---- 计算涨停之后的return分布---------
						int lastConsecutiveLimitUp = consecutiveIsLimitUpArr.get(count -1);
						Double thisReturn = close / lastClose  - 1;
						switch(lastConsecutiveLimitUp) {
						case 1:
							returnDist1stLimitUp.add(thisReturn);
							break;
						case 2:
							returnDist2ndLimitUp.add(thisReturn);
							break;
						case 3:
							returnDist3rdLimitUp.add(thisReturn);
							break;
						case 4:
							returnDist4thLimitUp.add(thisReturn);
							break;
						default :
							break;
						}
						
					}
					
					count++;
				}
				bf2.close();
				fw2Limit.flush();
				fw3Limit.flush();
				fw4Limit.flush();
				fw1Limit.flush();
			}
			
			fw2Limit.close();
			fw3Limit.close();
			fw4Limit.close();
			fw1Limit.close();
			logger.info("num1Limit=" + num1LimitAll + " num2Limit=" + num2LimitAll + " num3Limit=" + num3LimitAll + " num4Limit=" + num4LimitAll);
			logger.info("num1Limit2016=" + num1Limit2016 + " num2Limit2016=" + num2Limit2016 + " num3Limit2016=" + num3Limit2016 + " num4Limit2016=" + num4Limit2016);
			logger.info("num1Limit2017=" + num1Limit2017 + " num2Limit2017=" + num2Limit2017 + " num3Limit2017=" + num3Limit2017 + " num4Limit2017=" + num4Limit2017);
			
			// ---------- write distribution -----
			FileWriter returnDistFW = new FileWriter(rootPath + "returnDist.csv");
			for(int i = 0; i < returnDist1stLimitUp.size(); i++) {
				returnDistFW.write(returnDist1stLimitUp.get(i).toString());
				if(i < returnDist2ndLimitUp.size())
					returnDistFW.write("," + returnDist2ndLimitUp.get(i));
				if(i < returnDist3rdLimitUp.size())
					returnDistFW.write("," + returnDist3rdLimitUp.get(i));
				if(i < returnDist4thLimitUp.size())
					returnDistFW.write("," + returnDist4thLimitUp.get(i));
			}
			returnDistFW.close();
			
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
}
