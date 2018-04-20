package cgi.ib.downloader;

import java.io.File;
import java.util.ArrayList;

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
	
	public static void main(String[] args) {
		try {
			String rootPath = "Z:\\Mubing\\HK_Future\\Data";
			
			String host = "127.0.0.1";   //  "127.0.0.1" the local host
			int port = 7497;   	// 7497 - paper account
								// 7496 - real account
			int clientId = 23;
			
			Contract con1 = new Contract();
			con1.symbol("HSI");
			con1.exchange("HKFE");
			con1.secType("FUT");
			con1.currency("HKD");
			con1.lastTradeDateOrContractMonth("201804");
			
			
			MyAPIController apiController = DownloaderUtils.connect(host, port, clientId);
			DlIHistoricalTickHandler handler = new DlIHistoricalTickHandler("HIJ8 Index", rootPath);
			apiController.reqHistoricalTicks(con1, "20180409 09:00:00", null, 1000, "TRADES", 1, true, handler);
			Thread.sleep(1000 * 10);
			apiController.reqHistoricalTicks(con1, "20180410 09:00:00", null, 1000, "TRADES", 1, true, handler);
			
			
			
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
}
