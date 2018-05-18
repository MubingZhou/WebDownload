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
import com.ib.controller.ApiController;
import com.ib.controller.ApiController.IConnectionHandler;
import com.ib.controller.ApiController.ITopMktDataHandler;

import cgi.ib.MyLogger;

public class Main {
	public static Logger logger = Logger.getLogger(Main.class.getName());
	public static String AVAT_ROOT_PATH = "D:\\AVAT\\";
	//public static String AVAT_ROOT_PATH = "T:\\AVAT\\";
	public static double bilateralTrdCost = 0.003;
	public static String STOCKLIST_PATH_HISTORICAL_DATA = AVAT_ROOT_PATH + "stocklist - historical data.csv";
	//public static String STOCKLIST_PATH_HISTORICAL_DATA = AVAT_ROOT_PATH + "stocklist.csv";
	
	// IB controller  
	public static MyLogger inLogger = new MyLogger();
	public static MyLogger outLogger = new MyLogger();
	public static AvatIConnectionHandler myConnectionHandler = new AvatIConnectionHandler();
	public static AvatAPIController myController;
	public static ApiConnection myClient; 
	public static ArrayList<Contract> conArr = new ArrayList<Contract> ();
	public static boolean isDownloadEnds = false;   // if downloading historical data ends
	
	@SuppressWarnings("unused")
	public static void main(String[] args) {
		try {
			String dateFormat = "yyyyMMdd HH:mm:ss";
			SimpleDateFormat sdf = new SimpleDateFormat (dateFormat); 
			SimpleDateFormat sdf_HHmmss = new SimpleDateFormat ("HH:mm:ss"); 
			String dateFormat_yyyyMMdd = "yyyyMMdd";
			SimpleDateFormat sdf_yyyyMMdd = new SimpleDateFormat (dateFormat_yyyyMMdd); 
			
			
			String todayDate = sdf_yyyyMMdd.format(new Date()); //todayDate="20171228";
			SimpleDateFormat sdf_100 = new SimpleDateFormat ("yyyyMMdd HH_mm_ss"); 
			
			AVAT.todayDate = todayDate;
			AvatUtils.todayDate = todayDate;
			AVAT.bilateralTrdCost = bilateralTrdCost;
			AvatUtils.AVAT_ROOT_PATH = AVAT_ROOT_PATH;
			AVAT.AVAT_ROOT_PATH = AVAT_ROOT_PATH;
		
			boolean readyToExit = false;
			ArrayList<Calendar> allTradingCal = utils.Utils.getAllTradingCal(utils.PathConifiguration.ALL_TRADING_DATE_PATH_HK);
			ArrayList<Date> allTradingDate = new ArrayList<Date> ();
			for(Calendar cal : allTradingCal) {
				allTradingDate .add(cal.getTime());
			}			
			
			// ------------ MODE -----------
			int mode = 123456;
			/*
			 * 0 - download historical data
			 * 1 - avat: real time running
			 * 		每次update股票list的时候，需要update之前的历史1min数据，然后需要update每日的auction和prevclose的股票list，还需要update那个industry的table（注意如果有一些N/A的数据，需要替换掉）
			 * 
			 * 100 or larger - testing
			 */
			int isTestRun = 0;
			AVAT.isTestRun = isTestRun;
			
			logger.info("today date=" + todayDate);
			
			String host = "127.0.0.1";   //  "127.0.0.1" the local host
			int port = 7497;   	// 7497 - paper account
								// 7496 - real account
			//int clientId = (int) (Math.random() * 100) + 1;  // a self-specified unique client ID
			int clientId = 0;
			if(isTestRun == 1)
				clientId = 53;
			
			//****** the main controller **********
			connect(host, port, clientId);
			
			// test codes
			// myClient.reqIds(-1);
			
			/*
			myController = new MyAPIController(myConnectionHandler, inLogger, outLogger	);
			myController.connect(host, port, clientId, null);
			
			// create EClient
			//MyEReaderSignal signal = new MyEReaderSignal();
			//ApiConnection myConnection = new ApiConnection(myController, inLogger, outLogger);
			myClient = myController.client();  
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
			*/
			
			//---------- temp: download historical 1 min data - multiple days ------------
//			String[] temp_dateArrStr = {"20180427"}; //"20180207","20180206","20180205"
//			for(int j = 0; j < temp_dateArrStr.length; j++) {
//				//connect(host, port, clientId);
//				
//				String dateStr = temp_dateArrStr[j];
//				logger.info("=========== downloading " + dateStr + " ============");
//				//Date date = sdf_yyyyMMdd.parse(dateStr);
//				
//				boolean temp_isEnd = getHistorical1MinData_OneDay(dateStr, dateFormat_yyyyMMdd);
//				while(!temp_isEnd) {
//					Thread.sleep(1000 * 5);
//				}
//				logger.info("=========== downloading " + dateStr + " end ============");
//				
//				//disconnect();
//			}
			//Thread.sleep(1000 * 5000000);

			
			//======== constructing contracts ===========
			ArrayList<String> stockList = new ArrayList<String>();
			//ArrayList<String> industryList = new ArrayList<String>();
			
			String stockListPath = AVAT_ROOT_PATH + "stocklist.csv";
			if(isTestRun==1)
				stockListPath = AVAT_ROOT_PATH + "stocklist-test.csv";
			BufferedReader bf = utils.Utils.readFile_returnBufferedReader(stockListPath);
			//BufferedReader bf = utils.Utils.readFile_returnBufferedReader(AVAT_ROOT_PATH + "additional-stocklist.csv");
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
			
			
			// --------------- downloading historical data --------------- 
			Date nowDate = sdf.parse(todayDate + " " + sdf_HHmmss.format(new Date()));
			logger.info("nowDate = " + sdf.format(nowDate));
			if(nowDate.after(sdf.parse(todayDate + " 16:30:00")) ) {  // 16:30之后，下载当天数据
				System.out.println("after " + todayDate + " 16:30:00");
				isDownloadEnds = getHistorical1MinData_OneDay(todayDate, dateFormat_yyyyMMdd);
			}else {
				Date lastTrdDate = allTradingDate.get(allTradingDate.indexOf(sdf_yyyyMMdd.parse(todayDate)) - 1);
				System.out.println("last trd date = " + sdf_yyyyMMdd.format(lastTrdDate));
				isDownloadEnds = getHistorical1MinData_OneDay(sdf_yyyyMMdd.format(lastTrdDate), dateFormat_yyyyMMdd);
			}
			
			while(!isDownloadEnds) {
				Thread.sleep(1000 * 5);
			}
			logger.info("dowloading ends...");
			
			// --------------- avat--------------- 
			if(!nowDate.after(sdf.parse(todayDate + " 16:10:00")) ) {   //在当日16：10之前，才run
				AVAT.setting(myController, conArr, AVAT_ROOT_PATH);
				logger.info("AVAT.setting ends...");
				
				AVAT.start();
			}
			// --------------- avat done --------------- 
			logger.info("-------------------- All ends ----------------------");
			//Thread.sleep(1000 * 500000);
			
			
			
			//-------------- dead codes below ----------------
			
			if(mode == 0) {
				//AvatUtils.downloadHistorical1MinData_20D(myController, conArr, "20170908", "yyyyMMdd");
				ArrayList<String> dateArr = new ArrayList<String>();
				dateArr.add("20171213");
				dateArr.add("20171212");
				dateArr.add("20171211");
				dateArr.add("20171208");
				dateArr.add("20171207");
				dateArr.add("20171206");
				dateArr.add("20171205");
				dateArr.add("20171204");
				dateArr.add("20171201");
				dateArr.add("20171130");
				dateArr.add("20171129");
				dateArr.add("20171128");
				dateArr.add("20171127");
				for(int i = 1000; i < dateArr.size(); i ++) {
					String dateStr  = dateArr.get(i);
					AvatUtils.downloadHistorical1MinData(myController, conArr, dateStr, "yyyyMMdd");
				}
				readyToExit = AvatUtils.downloadHistorical1MinData(myController, conArr, "20180124", "yyyyMMdd");
				myController.disconnect();
				//AvatUtils.preparePrevCrossSectionalAvat2(conArr,"20170929", "yyyyMMdd");
				logger.info("dowloading ends...");
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
				
				AvatIOrderHandler myOrderH = new AvatIOrderHandler (con, order); 
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
					AvatILiveOrderHandler myLiveOrder = new AvatILiveOrderHandler(liveOrderRecPath);
					myController.takeTwsOrders(myLiveOrder);
					
					// monitor executions
					String exeRecPath = AVAT_ROOT_PATH + "orders\\" + todayDate + "\\execution records.csv";
					AvatITradeReportHandler myTradeReport = new AvatITradeReportHandler(exeRecPath);
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
				Contract con = new Contract();
				con.symbol("75");
				con.exchange("SEHK");
				con.secType("STK");
				con.currency("HKD");
				
				System.out.println("DONE");
				
				AvatIHistoricalDataHandler myHist = new AvatIHistoricalDataHandler("75", "D:\\no use\\");
				myController.reqHistoricalData(con, "2017-12-27 16:00:00", 1, DurationUnit.DAY, BarSize._1_day, WhatToShow.ADJUSTED_LAST, true, false, myHist);
				
			}
			if(mode == 103) {
				System.out.println("");
				for(int i = 0; i < 405; i++) {
					Contract con = conArr.get(i);
					AvatITopMktDataHandler myTop = new AvatITopMktDataHandler(con, AVAT_ROOT_PATH, "20171020");
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
				AvatITradeReportHandler myTradeReportHandler = new AvatITradeReportHandler(executionsRecPath);
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
//			try {   
//				while(!readyToExit) {
//					Thread.sleep(1000 * 5);
//				}
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
			
			
			// ======== close =========
			
			myController.disconnect();
			/*
			if(myClient.isConnected()){
				System.out.println("Is connected!");
			}
			else{
				System.out.println("Not connected!");
			}
			*/
			
			System.out.println("========================== END ==========================");
			System.exit(0);
			
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * connected to IB API
	 * @param host
	 * @param port
	 * @param clientId
	 * @return
	 */
	public static boolean connect(String host, int port, int clientId) {
		boolean getConnected = false;
		try {
			myController = new AvatAPIController(myConnectionHandler, inLogger, outLogger	);
			myController.connect(host, port, clientId, null);
			
			// create EClient
			//MyEReaderSignal signal = new MyEReaderSignal();
			//ApiConnection myConnection = new ApiConnection(myController, inLogger, outLogger);
			myClient = myController.client();  
			//myClient.eConnect(host, port, clientId, true);
			if(myClient.isConnected()){
				System.out.println("Is connected!");
				try {
					Thread.sleep(1000*3);   
					getConnected = true;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			else{
				System.out.println("Not connected!");
				return getConnected;
			}
		}catch(Exception e	) {
			e.printStackTrace();
		}
		
		
		return getConnected;
	}
	
	/**
	 * get disconnected from IB API
	 * @return
	 */
	public static boolean disconnect() {
		boolean getDisconnect = false;
		
		try {
			if(!myClient.isConnected()) {
				myController.disconnect();
				getDisconnect = true;
			}else {
				getDisconnect = true;
			}
			
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		
		return getDisconnect ;
	}
	
	public void getRecentNDayTickData(Contract con, String endDate, int NDays, String filePath) {
		
	}
	
	public static boolean getHistorical1MinData_OneDay(String dateStr, String dateFormat) {
		boolean readyToExit = false;
		try {
//			ArrayList<String> dateArr = new ArrayList<String>();
//			dateArr.add("20171213");
//			dateArr.add("20171212");
//			dateArr.add("20171211");
//			dateArr.add("20171208");
//			dateArr.add("20171207");
//			dateArr.add("20171206");
//			dateArr.add("20171205");
//			dateArr.add("20171204");
//			dateArr.add("20171201");
//			dateArr.add("20171130");
//			dateArr.add("20171129");
//			dateArr.add("20171128");
//			dateArr.add("20171127");
//			for(int i = 1000; i < dateArr.size(); i ++) {
//				String dateStr1  = dateArr.get(i);
//				AvatUtils.downloadHistorical1MinData(myController, conArr, dateStr1, dateFormat);
//			}
//			
			BufferedReader bf = utils.Utils.readFile_returnBufferedReader(STOCKLIST_PATH_HISTORICAL_DATA);
			//BufferedReader bf = utils.Utils.readFile_returnBufferedReader(AVAT_ROOT_PATH + "additional-stocklist.csv");
			
			ArrayList<Contract> conArr_histData = new ArrayList<Contract>();
			ArrayList<String> stockList = new ArrayList<String>(); 
			stockList.addAll(Arrays.asList(bf.readLine().split(",")));
			
			for(int i = 0; i < stockList.size(); i ++) {
				String symbol = stockList.get(i);
				
				Contract con1 = new Contract();
				con1.symbol(symbol);
				con1.exchange("SEHK");
				con1.secType("STK");
				con1.currency("HKD");
				
				conArr_histData.add(con1);
			}
			readyToExit = AvatUtils.downloadHistorical1MinData(myController, conArr_histData, dateStr, dateFormat);
			//myController.disconnect();
			//AvatUtils.preparePrevCrossSectionalAvat2(conArr,"20170929", "yyyyMMdd");
		}catch(Exception e) {
			e.printStackTrace();
		}

		return readyToExit;
	}

}
