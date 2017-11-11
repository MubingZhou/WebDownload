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
import com.ib.client.MarketDataType;
import com.ib.client.Order;
import com.ib.client.OrderType;
import com.ib.client.Types.Action;
import com.ib.client.Types.BarSize;
import com.ib.client.Types.DurationUnit;
import com.ib.client.Types.WhatToShow;
import com.ib.controller.ApiConnection;
import com.ib.controller.ApiController.IConnectionHandler;
import com.ib.controller.ApiController.ITopMktDataHandler;

public class Main {
	public static Logger logger = Logger.getLogger(Main.class.getName());
	public static String AVAT_ROOT_PATH = "Z:\\AVAT\\";
	//public static String AVAT_ROOT_PATH = "T:\\AVAT\\";
	public static double bilateralTrdCost = 0.003;
	
	@SuppressWarnings("unused")
	public static void main(String[] args) {
		try {
			String dateFormat = "yyyyMMdd HH:mm:ss";
			SimpleDateFormat sdf = new SimpleDateFormat (dateFormat); 
			String todayDate = new SimpleDateFormat ("yyyyMMdd").format(new Date()); //todayDate="20171102";
			ArrayList<Calendar> allTradingDate = utils.Utils.getAllTradingDate("D:\\stock data\\all trading date - hk.csv");
			SimpleDateFormat sdf_100 = new SimpleDateFormat ("yyyyMMdd HH_mm_ss"); 
			
			AVAT.todayDate = todayDate;
			AvatUtils.todayDate = todayDate;
			AVAT.bilateralTrdCost = bilateralTrdCost;
			AvatUtils.AVAT_ROOT_PATH = AVAT_ROOT_PATH;
			AVAT.AVAT_ROOT_PATH = AVAT_ROOT_PATH;
			
			// ------------ MODE -----------
			int mode = 0;
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
				AvatUtils.downloadHistorical1MinData(myController, conArr, "20171110", "yyyyMMdd");
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
				AVAT.setting(myController, conArr, AVAT_ROOT_PATH);
				
				Thread orderMonitorThd = new Thread(new Runnable(){
					   public void run(){
						   //ordersMonitor();
						   AVAT.executionMonitor();
					   }
				});
				orderMonitorThd.start();
				
			}
			if(mode == 102) {
				//String path  = "Z:\\AVAT\\orders\\20171017\\holdingRecords.javaObj";
				String path  = "D:\\test.javaObj";
				
				Map<String, Map<Integer, HoldingRecord>> holdingRecords = new HashMap<String, Map<Integer, HoldingRecord>>();
				//holdingRecords = (Map<String, Map<Integer, HoldingRecord>> ) utils.Utils.readObject(path)  ;
				System.out.println(holdingRecords.get("493"));
				System.out.println(holdingRecords.get("701"));
				
				Thread.sleep(1000 * 1000);
				
				Contract con = new Contract();
				con.symbol("700");
				con.exchange("SEHK");
				con.secType("STK");
				con.currency("HKD");
				
				Order order = new Order();
				order.action(Action.BUY);
				order.totalQuantity(100.0);;
				order.orderType(OrderType.LMT);
				order.lmtPrice(350.0);
				
				MyIOrderHandler myOrderH1 = new MyIOrderHandler (con, order); 
				myOrderH1.isTransmit = true;
				myController.placeOrModifyOrder(con, order, myOrderH1);
				
				while(myOrderH1.getOrderId() == -1) {Thread.sleep(5);}
				
				HoldingRecord hld1 = new HoldingRecord(myOrderH1, new Date().getTime());
				
				Map<Integer, HoldingRecord> thisHoldingMap = new HashMap();
				
				thisHoldingMap.put(myOrderH1.getOrderId(), hld1);
				holdingRecords.put("700", thisHoldingMap);
				holdingRecords.put("701", thisHoldingMap);
				
				//utils.Utils.saveObject(holdingRecords, path);
				System.out.println("DONE");
			}
			if(mode == 103) {
				System.out.println("");
				for(int i = 0; i < 405; i++) {
					Contract con = conArr.get(i);
					MyITopMktDataHandler myTop = new MyITopMktDataHandler(con.symbol(), AVAT_ROOT_PATH, "20171020");
					//myTop.fileWriterMainPath = AVAT_ROOT_PATH + "real time data\\";
					//topMktDataHandlerArr.add(myTop);
					//myController.reqMktDataType(MarketDataType.REALTIME);
					myController.reqTopMktData(con, "233,375", false, false, myTop);
					//Thread.sleep(20);
					/*
					 * Generic tick type:
					 * 233 - RT volume
					 * 375 - RT trade volumes
					 */
					logger.info("Subscribe top market data. Stock=" + con.symbol() + " i=" + i);
				}
				/*
				//orderMonitorThd.start();
				   Contract con1 = new Contract();
					con1.localSymbol("EUR.USD");
					con1.exchange("IDEALPRO");
					con1.secType("CASH");
					con1.currency("USD");
					Contract con2 = new Contract();
					con2.localSymbol("GBP.USD");
					con2.exchange("IDEALPRO");
					con2.secType("CASH");
					con2.currency("USD");
					Contract con3 = new Contract();
					con3.localSymbol("USD.JPY");
					con3.exchange("IDEALPRO");
					con3.secType("CASH");
					con3.currency("JPY");
					
					MyITopMktDataHandler myTop1 = new MyITopMktDataHandler(con1.localSymbol(), AVAT_ROOT_PATH, "20171019");
					myController.reqTopMktData(con1, "233,375", false, false, myTop1);
					MyITopMktDataHandler myTop2 = new MyITopMktDataHandler(con2.localSymbol(), AVAT_ROOT_PATH, "20171019");
					myController.reqTopMktData(con2, "233,375", false, false, myTop2);
					MyITopMktDataHandler myTop3 = new MyITopMktDataHandler(con3.localSymbol(), AVAT_ROOT_PATH, "20171019");
					myController.reqTopMktData(con3, "233,375", false, false, myTop3);
					try {
						//Thread.sleep(1000 * 10000);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					*/
			}
			if(mode == 104) {
				AVAT.setting(myController, conArr, AVAT_ROOT_PATH);
				
				AVAT.executionMonitor();
				
				Thread.sleep(1000 * 10000);
				
				ExecutionFilter filter = new ExecutionFilter();
				filter.secType("STK");
				
				String executionsRecPath = AVAT_ROOT_PATH + "orders\\" + todayDate + "\\executions records.csv";
				MyITradeReportHandler myTradeReportHandler = new MyITradeReportHandler(executionsRecPath);
				myTradeReportHandler.isCalledByMonitor = 1;
				myController.reqExecutions(filter, myTradeReportHandler);
				//System.out.println("here11234--------");
				//Thread.sleep(1000 * 10000);
			}
			if(mode == 105) {
				// A share
				Contract con1 = new Contract();
				con1.symbol("1");
				con1.exchange("SEHK");
				con1.secType("STK");
				con1.currency("HKD");
				
				SimpleDateFormat sdf_105 = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
				Date now = sdf_105.parse("2017-11-07 10:00:00");
				
				AvatRecordSingleStock a = new AvatRecordSingleStock(now.getTime(), con1.symbol(), con1, 100.0, 0.01, 10.0,10.0,"1");
				AvatRecordSingleStock b = new AvatRecordSingleStock(now.getTime(), con1.symbol(), con1, 100.0, 0.01, 10.0,10.0,"1");
				
				ArrayList<AvatRecordSingleStock > arr = new ArrayList<AvatRecordSingleStock >();
				arr.add(a);
				arr.add(b);
				AVAT.scanForOrders(arr, now);
				
			}
			
			System.out.println("here11234");
			// pause and disconnect
			try {   
				Thread.sleep(1000 * 10000000);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			
			// ======== close =========
			/*
			myController.disconnect();
			if(myClient.isConnected()){
				System.out.println("Is connected!");
			}
			else{
				System.out.println("Not connected!");
			}
			*/
			System.out.println("========================== END ==========================");
			
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public void getRecentNDayTickData(Contract con, String endDate, int NDays, String filePath) {
		
	}
	
	//public String getNextTradeDate(String date, ArrayList<Calendar> allTradingDate)

}
