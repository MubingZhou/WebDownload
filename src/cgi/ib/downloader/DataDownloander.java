package cgi.ib.downloader;

import java.io.BufferedReader;
import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.apache.log4j.Logger;

import com.ib.client.Contract;
import com.ib.client.Types.BarSize;
import com.ib.client.Types.DurationUnit;
import com.ib.client.Types.WhatToShow;
import com.ib.controller.ApiController;

import cgi.ib.MyAPIController;
import cgi.ib.avat.AvatIHistoricalDataHandler;
import webbDownload.outstanding.DataDownloader;

public class DataDownloander {
	private static Logger logger  =Logger.getLogger(DataDownloader.class);
	public static ArrayList<Date> allTrdDate = new ArrayList<Date>(); 
	
	public static void main(String[] args) {
		try {
			String rootPath = "D:\\stock data\\HK_Futures\\Data";
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd HH:mm:ss"); 
			
			String host = "127.0.0.1";   //  "127.0.0.1" the local host
			int port = 7497;   	// 7497 - paper account
								// 7496 - real account
			int clientId = 23;
			MyAPIController apiController = DownloaderUtils.connect(host, port, clientId);
			
			ArrayList<Contract> conArr = genFutContract("HSI", "201701", "201804", "HKFE","FUT", "HKD");
			String requestType = "TRADES";  //TRADES, BID_ASK, MIDPOINT
			int numOfData = 1000;  // max 1000
			int noDataThresh = 1000 * 1; // 
			
			for(int i = 1000000; i < conArr.size(); i++) {
				Contract con  = conArr.get(i);
				String firstTrdDateStr = getFirstTrdDate_HKF(con.lastTradeDateOrContractMonth());
				
				
				DlIHistoricalTickHandler handler = new DlIHistoricalTickHandler(con.symbol()+con.lastTradeDateOrContractMonth(), 
						rootPath, requestType, numOfData);
				
				boolean isGetAllData = false;
				long lastUpdTime_long = 0;
				while(!isGetAllData) {
					String startTime = "";
					if(lastUpdTime_long == 0)
						startTime = firstTrdDateStr + " 09:00:00";
					else
						startTime = sdf.format(new Date(lastUpdTime_long + 1000)); 
					apiController.reqHistoricalTicks(con, startTime, null, numOfData, "TRADES", 1, true, handler);
					
					
					
				}
				
				//System.out.println(firstTrdDateStr);
				
			}
			
			
			Contract con1 = new Contract();
			//con1.symbol("HSI");
			con1.localSymbol("HSIJ8");
			con1.exchange("HKFE");
			con1.secType("FUT");
			con1.currency("HKD");
			//con1.lastTradeDateOrContractMonth("201803");
			DlIHistoricalTickHandler handler = new DlIHistoricalTickHandler("HIH8 Index", 
					rootPath, requestType, numOfData);
			handler.writeOrder = false;
			System.out.println("Requesting...");
			apiController.reqHistoricalTicks(con1, null, "20180328 09:00:00", 10, "TRADES", 1, true, handler);
			//while (handler.isNoData())
//			Thread.sleep(1000 * 3);
//			apiController.reqHistoricalTicks(con1, "20180409 09:16:01", null, 950, "TRADES", 1, true, handler);
//			
			
			
		}catch(Exception e){
			e.printStackTrace();
		}

	}

	/**
	 * Not finished. Need further coding
	 * @param myController
	 * @param conArr
	 * @param endDateTime
	 * @param duration
	 * @param durationUnit
	 * @param barSize
	 * @param whatToShow
	 * @param outputRootPath
	 * @return
	 */
	public static boolean downloadHistoricalBarData(ApiController myController, ArrayList<Contract> conArr, 
			String endDateTime /*yyyyMMdd HH:mm:ss*/, int duration, DurationUnit durationUnit, BarSize barSize, WhatToShow whatToShow,
			String outputRootPath) {
		
		boolean isOK = true;
		try {
			//String outputPath = ibRootPath + "historical ";
			ArrayList<AvatIHistoricalDataHandler> histHandlerArr = myController.histHandlerArr;
			int numOfRead = 20;
			boolean rthOnly = true;
			int counter  = 1;
			int counter2 = 0;
			int available_i = 0;
			
			int inQueueStockNum = 0;   //排队下载的股票数
			ArrayList<Integer> inQueueStockNum_i = new ArrayList<Integer>();
			for(int i = 0; i < conArr.size(); i++) {
				String stockCode = conArr.get(i).symbol();
				String fileName = outputRootPath + stockCode + ".csv";
				File f = new File(fileName);
				if(f.exists()) {
					logger.debug("    [historical bar] i=" + i + " stock=" + stockCode + " Already existed!" );
					continue;
				}else
					logger.debug("[historical bar] i=" + i + " Downloading " + conArr.get(i).symbol());
				
				AvatIHistoricalDataHandler myHist = new AvatIHistoricalDataHandler(stockCode, outputRootPath);
				histHandlerArr.add(myHist);
				myController.reqHistoricalData(conArr.get(i), endDateTime, duration, durationUnit, barSize, whatToShow, rthOnly, false, myHist);
				inQueueStockNum ++;
				inQueueStockNum_i.add(available_i);
				
				if(inQueueStockNum == numOfRead || i == conArr.size() - 1) {
					logger.info("========== waiting this part to be finished... ==============");
					int cum = 0;
					int cumFailedNum = 0;
					while(cum < inQueueStockNum) {
						cum = 0;
						for(int j = 0; j < inQueueStockNum; j++) {
							AvatIHistoricalDataHandler thisHist = histHandlerArr.get(inQueueStockNum_i.get(j));
							int isEnd = thisHist.isEnd;
							cum += isEnd;
						}
						Thread.sleep(1000 * 2);
						logger.debug("Ping Pong! cum=" + cum);
						cumFailedNum ++;
						if(cumFailedNum == 20) {  //把没有end的全部取消掉
							for(int j = 0; j < inQueueStockNum; j++) {
								AvatIHistoricalDataHandler thisHist = histHandlerArr.get(inQueueStockNum_i.get(j));
								int isEnd = thisHist.isEnd;
								int isActive = thisHist.isActive;
								if(isEnd != 1 ) {
									myController.cancelHistoricalData(thisHist); logger.info("   -->cancel stock=" + thisHist.stockCode + " j=" + j);
									thisHist.isActive = 0;
									histHandlerArr.set(j, thisHist);
								}
							}
							break;
						}
							
					}
					
					inQueueStockNum =  0;
					inQueueStockNum_i.clear();
					logger.info("========== finished... ==============");
				}
				/*
				// 每次只读取numOfRead的整数倍只股票的信息
				int cum = 0;
				if(available_i == (numOfRead * counter - 1)) {  // i 是numOfRead的整数倍
					int startInd = numOfRead * (counter - 1);
					
					while(cum != numOfRead) { 
						cum = 0;
						for(int j = startInd; j <= available_i; j++) {
							MyIHistoricalDataHandler thisHist = histHandlerArr.get(j);
							int isEnd = thisHist.isEnd;
							int isActive = thisHist.isActive;
							if(isEnd == 1 && isActive == 1) {
								myController.cancelHistoricalData(thisHist); logger.info("   -->stock=" + thisHist.stockCode + " i=" + j);
								thisHist.isActive = 0;
								histHandlerArr.set(j, thisHist);
							}
							cum += isEnd;
						}
						
						Thread.sleep(1000 * 2);
						logger.debug("Ping Pong!");
						
						counter2 ++;
						if(counter2 >= 30) { // if wait for too long, 强制断开
							break;
						}
					}
					
					counter2 = 0;
					logger.debug("i = " + i + " Download END!");
					counter ++;
				}
				*/
				available_i ++;
				
			}
		}catch(Exception e) {
			e.printStackTrace();
			isOK = false;
		}
		
		return isOK;
	}
	
	/**
	 * Download historical tick data and store them.
	 * @param myController
	 * @param conArr
	 * @param startDateTime - String, form: "yyyyMMdd HH:mm:ss"; Exactly one of start time and end time has to be defined. If start time is defined, end time will be ignored
	 * @param endDateTime - String, form: "yyyyMMdd HH:mm:ss"; Exactly one of start time and end time has to be defined. If start time is defined, end time will be ignored
	 * @param duration
	 * @param durationUnit
	 * @param barSize
	 * @param whatToShow
	 * @param outputRootPath
	 * @return
	 */
	public static void downloadHistoricalTickData(ApiController myController, ArrayList<Contract> conArr, 
			String startDateTime /*yyyyMMdd HH:mm:ss*/, String endDateTime /*yyyyMMdd HH:mm:ss*/, 
			int numOfTicks, WhatToShow whatToShow, int useRth, boolean ignoreSize,
			String outputRootPath) {
		boolean isOK = false;
		try {
			
			
			
		}catch(Exception e) {
			e.printStackTrace();
			isOK = false;
		}
	
	}
	
	public static ArrayList<Contract> genFutContract(String uly, String fromDate /*yyyyMM*/, String endDate/*yyyyMM*/, String exchange, String type, String currency){
		ArrayList<Contract> conArr = new ArrayList<Contract>();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMM");  
		ArrayList<String> monthArr = new ArrayList<String>();
		
		try {
			Integer fromDate_int = Integer.parseInt(fromDate);
			Integer endDate_int = Integer.parseInt(endDate);
			while(fromDate_int <= endDate_int) {
				//System.out.println(fromDate_int);
				Contract con1 = new Contract();
				con1.symbol(uly);
				con1.exchange(exchange);
				con1.secType(type);
				con1.currency(currency);
				con1.lastTradeDateOrContractMonth(fromDate);
				conArr.add(con1);
				
				// get next month
				Integer y = fromDate_int / 100;
				Integer m = fromDate_int - 100 * y;
				Integer next_m = m + 1;
				if(next_m > 12) {
					y ++;
					next_m = next_m - 12; 
				}
				
				fromDate_int = 100 * y + next_m;	// next month
				fromDate = String.valueOf(fromDate_int);
			}
			
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		return conArr;
	}
	
	public static String getFirstTrdDate_HKF(String conMonth /*yyyyMM*/) {
		String ftd = "";
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		if(allTrdDate == null || allTrdDate.size() == 0) {
			allTrdDate = utils.Utils.getAllTradingDate();
		}
		
		try {
			Date d = sdf.parse(conMonth + "01");
			int i  = 0;
			for(; i < allTrdDate.size(); i++) {
				Date date = allTrdDate.get(i);
				if(!date.before(d))
					break;
			}
			
			ftd = sdf.format(allTrdDate.get(i - 5));
			
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return ftd;
	}
	
	public static String getLastTrdDate_HKF(String conMonth /*yyyyMM*/) {
		String ltd = "";
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		if(allTrdDate == null || allTrdDate.size() == 0) {
			allTrdDate = utils.Utils.getAllTradingDate();
		}
		
		try {
			Date d = sdf.parse(getNextMon(conMonth) + "01");
			int i  = 0;
			for(; i < allTrdDate.size(); i++) {
				Date date = allTrdDate.get(i);
				if(!date.before(d)) {
					//System.out.println("last date=" );
					break;
				}
			}
			
			ltd = sdf.format(allTrdDate.get(i - 2));  // get the date before the last date of a month
			
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return ltd;
	}
	
	public static String getNextMon(String conMonth /*yyyyMM*/) {
		Integer mon_int = Integer.parseInt(conMonth);
		
		Integer y = mon_int / 100;
		Integer m = mon_int - 100 * y;
		Integer next_m = m + 1;
		if(next_m > 12) {
			y ++;
			next_m = next_m - 12; 
		}
		
		mon_int = 100 * y + next_m;	// next month
		String nextMon = String.valueOf(mon_int);
		
		return nextMon;
	}
}
