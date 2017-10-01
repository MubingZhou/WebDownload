package cgi.ib;

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
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.ib.client.Contract;
import com.ib.client.EReaderSignal;
import com.ib.client.Types.BarSize;
import com.ib.client.Types.DurationUnit;
import com.ib.client.Types.WhatToShow;
import com.ib.controller.ApiConnection;

import cgi.ib.avat.AvatRecordSingleStock;
import cgi.ib.avat.MyAPIController;
import cgi.ib.avat.MyIConnectionHandler;
import cgi.ib.avat.MyIHistoricalDataHandler;
import cgi.ib.avat.MyIHistoricalTickHandler;
import cgi.ib.avat.MyIRealTimeBarHandler;
import cgi.ib.avat.MyITopMktDataHandler;
import cgi.ib.avat.MyLogger;


public class Test {
	public static Logger logger = Logger.getLogger(Test.class.getName());
	
	public static void main(String[] args) {
		try {
			String dateFormat = "yyyyMMdd HH:mm:ss";
			SimpleDateFormat sdf = new SimpleDateFormat (dateFormat); 
			String todayDate = new SimpleDateFormat ("yyyyMMdd").format(new Date());
			ArrayList<Calendar> allTradingDate = utils.Utils.getAllTradingDate("D:\\stock data\\all trading date - hk.csv");
			
			String host = "127.0.0.1";   //  "127.0.0.1" the local host
			int port = 7496;
			int clientId = 12;  // a self-specified unique client ID
			
			MyLogger inLogger = new MyLogger();
			MyLogger outLogger = new MyLogger();
			
			MyIConnectionHandler myConnectionHandler = new MyIConnectionHandler();
			//****** the main controller **********
			MyAPIController myController = new MyAPIController(myConnectionHandler, inLogger, outLogger	);
			myController.connect(host, port, clientId, null);
			
			// create EClient
			//MyEReaderSignal signal = new MyEReaderSignal();
			//ApiConnection myConnection = new ApiConnection(myController, inLogger, outLogger);
			ApiConnection myClient = myController.client();  
			//myClient.eConnect(host, port, clientId, true);
			if(myClient.isConnected()){
				System.out.println("Is connected!");
				try {
					Thread.sleep(1000 * 3);   
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			else{
				System.out.println("Not connected!");
				return;
			}
			
			//======== constructing contracts ===========
			ArrayList<Contract> conArr = new ArrayList<Contract> ();
			ArrayList<String> stockList = new ArrayList<String>();
			//ArrayList<String> industryList = new ArrayList<String>();
			
			File alreadyExited = new File("D:\\stock data\\IB\\historical data");
			String[] aListTemp = alreadyExited.list();
			ArrayList<String> aList = new ArrayList<String>(Arrays.asList(aListTemp));  // excluding those already existed in historical data
			aList = new ArrayList<String>();  
			
			BufferedReader bf = utils.Utils.readFile_returnBufferedReader("D:\\stock data\\IB\\stocklist.csv");
			stockList.addAll(Arrays.asList(bf.readLine().split(",")));
			for(int i = 0; i < stockList.size(); i ++) {
				String symbol = stockList.get(i);
				if(aList.indexOf(symbol + ".csv") != -1)
					continue;
				
				Contract con1 = new Contract();
				con1.symbol(stockList.get(i));
				con1.exchange("SEHK");
				con1.secType("STK");
				con1.currency("HKD");
				
				conArr.add(con1);
			}
			//industryList.addAll(Arrays.asList(bf.readLine().split(",")));
			bf.close();
			/*
			Contract con1 = new Contract();
			con1.symbol("CNH");
			con1.exchange("IDEALPRO");
			con1.currency("HKD");
			con1.secType("CASH");
			conArr.add(con1);
			*/
			
			
			
			
			// ==== requesting real time bars ========
			ArrayList<MyIRealTimeBarHandler> rtBarHandlerArr = new ArrayList<MyIRealTimeBarHandler>();
			if(false) {
				boolean rthOnly_realtime = true;
				for(int i = 0; i < conArr.size(); i++) {
					MyIRealTimeBarHandler myRt = new MyIRealTimeBarHandler(conArr.get(i).symbol());
					rtBarHandlerArr.add(myRt);
					myController.reqRealTimeBars(conArr.get(i), WhatToShow.TRADES, rthOnly_realtime, myRt);
				}
				
			}
			
			// ========== requesting historical bar data ========
			//System.out.println("here68423");
			ArrayList<MyIHistoricalDataHandler> histHandlerArr = new ArrayList<MyIHistoricalDataHandler>();
			if(false) {
				int numOfRead = 2;
				boolean rthOnly = true;
				int counter  = 1;
				for(int i = 0; i < conArr.size(); i++) {
					logger.debug("i=" + i + " Downloading " + conArr.get(i).symbol());
					
					MyIHistoricalDataHandler myHist = new MyIHistoricalDataHandler(conArr.get(i).symbol(),"D:\\stock data\\IB\\historical data\\");
					histHandlerArr.add(myHist);
					myController.reqHistoricalData(conArr.get(i), "20170928 16:30:00", 20, DurationUnit.DAY, BarSize._1_min, WhatToShow.TRADES, rthOnly, false, myHist);
					//myController.reqHistoricalData(contract, endDateTime, duration, durationUnit, barSize, whatToShow, rthOnly, keepUpToDate, handler);
					
					//MyIHistoricalTickHandler myHistTick = new MyIHistoricalTickHandler(conArr.get(i).symbol());
					//myController.reqHistoricalTicks(conArr.get(i), null, "20170927 16:10:00", 1000, WhatToShow.TRADES.toString(), 1, true, myHistTick);
					
					// 每次只读取numOfRead的整数倍只股票的信息
					int cum = 0;
					if(i == (numOfRead * counter - 1)) {  // i 是45的整数倍
						int startInd = numOfRead * (counter - 1);
						
						while(cum != numOfRead) { 
							cum = 0;
							for(int j = startInd; j <= i; j++) {
								MyIHistoricalDataHandler thisHist = histHandlerArr.get(j);
								int isEnd = thisHist.isEnd;
								cum += isEnd;
							}
							
							Thread.sleep(1000 * 3);
							logger.debug("Ping Pong!");
						}
						logger.info("i = " + i + " Download END!");
						counter ++;
					}
					
				}
			}
			
			//============== requesting historical tick data ===============
			ArrayList<MyIHistoricalTickHandler> histTickHandlerArr = new ArrayList<MyIHistoricalTickHandler>();
			if(true) {
				int numOfRead = 1;
				int counter  = 1;
				String startTimeS = "20170831 09:00:00";
				String endTimeS = "20170926 17:00:30";
				Date endTime = sdf.parse(endTimeS);
				Date startTime = sdf.parse(startTimeS);
				Long endTimeL = endTime.getTime();
				Long startTimeL = startTime.getTime();
				
				String dataType = "TRADES";   // 
				for(int i = 338; i < conArr.size(); i++) {
					logger.debug("i=" + i + " Downloading " + conArr.get(i).symbol());

					int numOfData = 1000;
					long nextTimeL = startTimeL;
					String lastTimeS = startTimeS;
					MyIHistoricalTickHandler myHistTick = new MyIHistoricalTickHandler(conArr.get(i).symbol());
					histTickHandlerArr.add(myHistTick);
					
					//myController.reqHistoricalTicks(conArr.get(i), startTimeS, null, numOfData, dataType, 1, true, myHistTick);
					
					int isNo_trades = 0;
					while(nextTimeL <= endTimeL ) {  // 这样只可以读一天的数据
						logger.trace("Downloading ... " + sdf.format(new Date(nextTimeL)));
						// ========= step 1: flush data and initializing ==========
						ArrayList<Object> data_trades = myHistTick.getData_trades();
						
						myHistTick.flushData_trades();
						myHistTick.initialize();
						
						// ============ step 2: downloading data ============				
						String nextTimeS =  sdf.format(new Date(nextTimeL));
						logger.info("Downloading... nextTimeS = " + nextTimeS + " stock=" + conArr.get(i).symbol());
						myController.reqHistoricalTicks(conArr.get(i), nextTimeS, null, numOfData, dataType, 1, true, myHistTick);
						
						// =========== step 3: update next time & indicator ===========
						while(myHistTick.getIsEnd_trades() != 1) {  // 判断是否将这次的数据都读完了
							Thread.sleep(500);
						}
						isNo_trades = myHistTick.getIsNo_trades();
						//nextTimeL = myHistTick.getLastTime_trades(); // 更新next time
						
						if(isNo_trades == 1) { // day end
							// get next trading date
							Calendar lastTimeCal = (Calendar) allTradingDate.get(0).clone();
							SimpleDateFormat sdf_temp = new SimpleDateFormat("yyyyMMdd");
							String lastTimeS2 =  sdf_temp.format(new Date(nextTimeL));
							logger.trace("----- last time date = " + lastTimeS2);
							
							lastTimeCal.setTime(sdf_temp.parse(lastTimeS2));  // change to yyyyMMdd
							lastTimeCal.add(Calendar.DATE, 1);
							
							//int ind = allTradingDate.indexOf(lastTimeCal);
							//Calendar nextDate = allTradingDate.get(ind + 1);
							String nextDateS = sdf_temp.format(lastTimeCal.getTime().getTime());
							logger.trace("--- next day = " + nextDateS);
							
							nextTimeL = sdf.parse(nextDateS + " 09:00:00").getTime();
							
						}else {
							nextTimeL = myHistTick.getLastTime_trades() + 1000;   // lastTime的下一秒
							//lastTimeNextS =  sdf.format(new Date(lastTimeNextL));
						}
					}
					myHistTick.close();
				}
			}
			
			System.out.println("here11234");
			// pause and disconnect
			try {   
				Thread.sleep(1000 * 1000000000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			//========= pre-close actions ============
			for(int i = 0; i < rtBarHandlerArr.size(); i++) {
				MyIRealTimeBarHandler myRt = rtBarHandlerArr.get(i);
				myRt.fileWriter.close();
			}
			
			
			// ======== close =========
			myController.disconnect();
			if(myClient.isConnected()){
				System.out.println("Is connected!");
			}
			else{
				System.out.println("Not connected!");
			}
			System.out.println("========================== END ==========================");
			
		}catch(Exception e) {
			
		}
	}
	
	public void getRecentNDayTickData(Contract con, String endDate, int NDays, String filePath) {
		
	}
	
	//public String getNextTradeDate(String date, ArrayList<Calendar> allTradingDate)
	
}
