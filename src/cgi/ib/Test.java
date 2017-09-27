package cgi.ib;

import java.io.BufferedReader;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import org.apache.log4j.Logger;

import com.ib.client.Contract;
import com.ib.client.EReaderSignal;
import com.ib.client.Types.BarSize;
import com.ib.client.Types.DurationUnit;
import com.ib.client.Types.WhatToShow;
import com.ib.controller.ApiConnection;


public class Test {
	public static Logger logger = Logger.getLogger(Test.class.getName());
	
	public static void main(String[] args) {
		try {
			String dateFormat = "yyyyMMdd HH:mm:ss";
			SimpleDateFormat sdf = new SimpleDateFormat (dateFormat); 
			String todayDate = new SimpleDateFormat ("yyyyMMdd").format(new Date());
			
			String host = "127.0.0.1";   //  "127.0.0.1" the local host
			int port = 7496;
			int clientId = 1;  // a self-specified unique client ID
			
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
			ArrayList<String> industryList = new ArrayList<String>();
			
			File alreadyExited = new File("D:\\stock data\\IB\\historical data");
			String[] aListTemp = alreadyExited.list();
			ArrayList<String> aList = new ArrayList<String>(Arrays.asList(aListTemp));  // excluding those already existed in historical data
			aList = new ArrayList<String>();  
			
			BufferedReader bf = utils.Utils.readFile_returnBufferedReader("D:\\stock data\\IB\\stocklist.csv");
			stockList.addAll(Arrays.asList(bf.readLine().split(",")));
			for(int i = 0; i < stockList.size() - 396; i ++) {
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
			industryList.addAll(Arrays.asList(bf.readLine().split(",")));
			bf.close();
			/*
			Contract con1 = new Contract();
			con1.symbol("CNH");
			con1.exchange("IDEALPRO");
			con1.currency("HKD");
			con1.secType("CASH");
			conArr.add(con1);
			*/
			
			// ========== requesting top mkt data =========
			ArrayList<MyITopMktDataHandler> topMktDataHandlerArr = new ArrayList<MyITopMktDataHandler>();
			if(false) {
				for(int i = 0; i < conArr.size(); i++) {
					Contract con = conArr.get(i);
					MyITopMktDataHandler myTop = new MyITopMktDataHandler(con.symbol());
					topMktDataHandlerArr.add(myTop);
					myController.reqTopMktData(con, "233,375", false, false, myTop);
					/*
					 * Generic tick type:
					 * 233 - RT volume
					 * 375 - RT trade volume
					 */
					
				}
			}
			
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
					
					MyIHistoricalDataHandler myHist = new MyIHistoricalDataHandler(conArr.get(i).symbol());
					histHandlerArr.add(myHist);
					myController.reqHistoricalData(conArr.get(i), "20170927 14:30:00", 30, DurationUnit.DAY, BarSize._1_day, WhatToShow.TRADES, rthOnly, false, myHist);
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
				int numOfRead = 2;
				int counter  = 1;
				Date endTime = sdf.parse(todayDate + " 16:10:00");
				
				for(int i = 0; i < conArr.size(); i++) {
					logger.debug("i=" + i + " Downloading " + conArr.get(i).symbol());

					int numOfData = 1000;
					MyIHistoricalTickHandler myHistTick = new MyIHistoricalTickHandler(conArr.get(i).symbol());
					histTickHandlerArr.add(myHistTick);
					
					
					myController.reqHistoricalTicks(conArr.get(i), null,  "20170826 09:31:00", numOfData, "TRADES", 1, true, myHistTick);
					
					// 每次只读取numOfRead的整数倍只股票的信息
					int cum = 0;
					if(i == (numOfRead * counter - 1)) {  // i 是numOfRead的整数倍
						int startInd = numOfRead * (counter - 1);
						
						while(cum != numOfRead) { 
							cum = 0;
							for(int j = startInd; j <= i; j++) {
								MyIHistoricalTickHandler thisHistTick = histTickHandlerArr.get(j);
								int numOfDataReceived = thisHistTick.getNumOfData_tickLast();
								cum += numOfDataReceived == numOfData? 1 : 0;  // 判断是否收到了足够的data
							}
							
							Thread.sleep(1000 * 3);
							logger.debug("Ping Pong!");
						}
						logger.info("i = " + i + " Download END!");
						counter ++;
					}
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
			for(int i = 0; i < topMktDataHandlerArr.size(); i++) {
				MyITopMktDataHandler myTop = topMktDataHandlerArr.get(i);
				myTop.fileWriter_raw.close();
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
}
