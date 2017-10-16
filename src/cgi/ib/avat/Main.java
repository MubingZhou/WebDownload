package cgi.ib.avat;

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
import com.ib.client.ExecutionFilter;
import com.ib.client.Order;
import com.ib.client.OrderType;
import com.ib.client.Types.BarSize;
import com.ib.client.Types.DurationUnit;
import com.ib.client.Types.WhatToShow;
import com.ib.controller.ApiConnection;
import com.ib.controller.ApiController.IConnectionHandler;

public class Main {
	public static Logger logger = Logger.getLogger(Main.class.getName());
	public static String AVAT_ROOT_PATH = "Z:\\AVAT\\";
	//public static String AVAT_ROOT_PATH = "T:\\AVAT\\";
	public static double bilateralTrdCost = 0.003;
	
	public static void main(String[] args) {
		try {
			String dateFormat = "yyyyMMdd HH:mm:ss";
			SimpleDateFormat sdf = new SimpleDateFormat (dateFormat); 
			String todayDate = new SimpleDateFormat ("yyyyMMdd").format(new Date()); todayDate="20171017";
			ArrayList<Calendar> allTradingDate = utils.Utils.getAllTradingDate("D:\\stock data\\all trading date - hk.csv");
			SimpleDateFormat sdf_100 = new SimpleDateFormat ("yyyyMMdd HH_mm_ss"); 
			
			AVAT.todayDate = todayDate;
			AvatUtils.todayDate = todayDate;
			AVAT.bilateralTrdCost = bilateralTrdCost;
			AvatUtils.AVAT_ROOT_PATH = AVAT_ROOT_PATH;
			AVAT.AVAT_ROOT_PATH = AVAT_ROOT_PATH;
			
			// ------------ MODE -----------
			int mode = 1;
			/*
			 * 0 - download historical data
			 * 1 - avat: real time running
			 * 
			 * 100 or larger - testing
			 */
			
			logger.info("today date=" + todayDate);
			
			String host = "127.0.0.1";   //  "127.0.0.1" the local host
			int port = 7497;   	// 7497 - paper account
								// 7496 - real account
			//int clientId = (int) (Math.random() * 100) + 1;  // a self-specified unique client ID
			int clientId = 1;
			
			//[start] 
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
			
			BufferedReader bf = utils.Utils.readFile_returnBufferedReader(AVAT_ROOT_PATH + "stocklist.csv");
			stockList.addAll(Arrays.asList(bf.readLine().split(",")));
			for(int i = 0; i < stockList.size(); i ++) {
				String symbol = stockList.get(i);
				
				Contract con1 = new Contract();
				con1.symbol(stockList.get(i));
				con1.exchange("SEHK");
				con1.secType("STK");
				con1.currency("HKD");
				
				conArr.add(con1);
			}
			//industryList.addAll(Arrays.asList(bf.readLine().split(",")));
			bf.close();
			// [end] 
			
			if(mode == 0) {
				//AvatUtils.downloadHistorical1MinData_20D(myController, conArr, "20170908", "yyyyMMdd");
				AvatUtils.downloadHistorical1MinData(myController, conArr, "20171016", "yyyyMMdd");
				//AvatUtils.preparePrevCrossSectionalAvat2(conArr,"20170929", "yyyyMMdd");
				logger.trace("prepare ends...");
				return;
			}
			
			if(mode == 1) {
				AVAT.setting(myController, conArr, AVAT_ROOT_PATH);
				
				AVAT.start();
			}
			
			if(mode == 100) {
				Contract con = new Contract();
				con.localSymbol("HSIV7");
				con.exchange("HKFE");
				con.secType("FUT");
				con.currency("HKD");
				//con.lastTradeDateOrContractMonth("20171030");
				//con.multiplier("50");
				
				String stockCode = con.symbol();
				
				
				Order order = new Order();
				order.action("BUY");
				order.orderType(OrderType.LMT);
				
				Double buyPrice =28200.0;
				order.lmtPrice(buyPrice);  // 以best bid作为买入价
				
				Double lotSize = 1.0;
				Double totalQuatity = 1.0;
				order.totalQuantity(totalQuatity);
				//order.totalQuantity(100 * (int)(100000 / 100 / buyPrice) );
				boolean transmitToIB = true;
				order.transmit(transmitToIB);  // false - 只在api平台有这个order
				
				MyIOrderHandler myOrderH = new MyIOrderHandler (con, order); 
				myController.placeOrModifyOrder(con, order, myOrderH);
				
				int isSubmitted = -1;
				Double newLotSize = -1.0;
				boolean c = false;
				while(c) {
					if(myOrderH.isSubmitted == 1) {
						logger.info("Order submitted!");
						isSubmitted = 1;
						break;
					}
					
					// 处理error
					if(myOrderH.errorCode == 4610) {
						newLotSize = myOrderH.newLostSize;  // 修改然后resubmit
						order.totalQuantity(newLotSize * (int)(100000 / newLotSize / buyPrice) );
						myController.placeOrModifyOrder(con, order, myOrderH);
						myOrderH.errorCode = -1;
						logger.info("need new lot size = " + newLotSize + " resubmit order!");
					}
					
					if(!transmitToIB)
						myOrderH.isSubmitted = 1;
					
					Thread.sleep(1000);
					c = false;
					System.out.println("orderId=" + myOrderH.getOrderId());
				}
				
				Thread.sleep(100);
				
				//myController.cancelOrder(myOrderH.orderId);
				
				//Thread.sleep(1000 * 20);
				
				//myController.cancelOrder(myOrderH.orderId);
				
				Thread orderMonitorThd = new Thread(new Runnable(){
					   public void run(){
						   //ordersMonitor();
						   AVAT.executionMonitor();
					   }
				});
				orderMonitorThd.start();
				
				if(false) {
					// monitor order
					String liveOrderRecPath = AVAT_ROOT_PATH + "orders\\" + todayDate + "\\live order records.csv";
					MyILiveOrderHandler myLiveOrder = new MyILiveOrderHandler(liveOrderRecPath);
					myController.takeTwsOrders(myLiveOrder);
					
					// monitor executions
					String exeRecPath = AVAT_ROOT_PATH + "orders\\" + todayDate + "\\execution records.csv";
					MyITradeReportHandler myTradeReport = new MyITradeReportHandler(exeRecPath);
					ExecutionFilter filter = new ExecutionFilter();
					filter.secType("FUT");
					myController.reqExecutions(filter, myTradeReport);
					
					while(true) {
						if(myLiveOrder.isEnd) {  // order收集完全
							break;
						}
						Thread.sleep(200);
					} // end of while
					logger.info(myLiveOrder.toString());
				}
			}
			if(mode == 101) {
				MyILiveOrderHandler myLiveOrder = new MyILiveOrderHandler();
				myController.takeTwsOrders(myLiveOrder);
				
				
			}
			
			//============== requesting historical tick data ===============
			ArrayList<MyIHistoricalTickHandler> histTickHandlerArr = new ArrayList<MyIHistoricalTickHandler>();
			if(false) {
				int numOfRead = 1;
				int counter  = 1;
				String startTimeS = "20170831 09:00:00";
				String endTimeS = "20170926 17:00:30";
				Date endTime = sdf.parse(endTimeS);
				Date startTime = sdf.parse(startTimeS);
				Long endTimeL = endTime.getTime();
				Long startTimeL = startTime.getTime();
				
				String dataType = "TRADES";   // 
				for(int i = 1; i < conArr.size(); i++) {
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
				Thread.sleep(1000 * 10000000);
			} catch (InterruptedException e) {
				e.printStackTrace();
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
			e.printStackTrace();
		}
	}
	
	public void getRecentNDayTickData(Contract con, String endDate, int NDays, String filePath) {
		
	}
	
	//public String getNextTradeDate(String date, ArrayList<Calendar> allTradingDate)

}
