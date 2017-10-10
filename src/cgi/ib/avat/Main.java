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
	
	public static void main(String[] args) {
		try {
			String dateFormat = "yyyyMMdd HH:mm:ss";
			SimpleDateFormat sdf = new SimpleDateFormat (dateFormat); 
			String todayDate = new SimpleDateFormat ("yyyyMMdd").format(new Date());//todayDate="20171006";
			ArrayList<Calendar> allTradingDate = utils.Utils.getAllTradingDate("D:\\stock data\\all trading date - hk.csv");
			SimpleDateFormat sdf_100 = new SimpleDateFormat ("yyyyMMdd HH_mm_ss"); 
			
			AVAT.todayDate = todayDate;
			AvatUtils.todayDate = todayDate;
			
			// ------------ MODE -----------
			int mode = 1;
			/*
			 * 0 - download historical data
			 * 1 - avat: real time running
			 * 
			 * 100 - testing
			 */
			
			String host = "127.0.0.1";   //  "127.0.0.1" the local host
			int port = 7496;
			int clientId = (int) (Math.random() * 100) + 1;  // a self-specified unique client ID
			
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
			
			if(mode == 0) {
				//AvatUtils.downloadHistorical1MinData_20D(myController, conArr, "20170908", "yyyyMMdd");
				AvatUtils.downloadHistorical1MinData(myController, conArr, "20171009", "yyyyMMdd");
				//AvatUtils.preparePrevCrossSectionalAvat2(conArr,"20170929", "yyyyMMdd");
				logger.trace("prepare ends...");
				return;
			}
			
			if(mode == 1) {
				AVAT.setting(myController, conArr, AVAT_ROOT_PATH);
				
				AVAT.start();
			}
			if(mode == 100) {
				Contract con = conArr.get(0);
				
				String stockCode = con.symbol();
				
				
				Order order = new Order();
				order.action("BUY");
				order.orderType(OrderType.LMT);
				
				Double buyPrice =300.0;
				order.lmtPrice(buyPrice);  // 以best bid作为买入价
				
				Double lotSize = 100.0;
				order.totalQuantity(100 * (int)(500000 / 100 / buyPrice) );
				order.transmit(true);  // false - 只在api平台有这个order
				
				MyIOrderHandler myOrderH = new MyIOrderHandler (con, order); 
				myController.placeOrModifyOrder(con, order, myOrderH);
				
				int isSubmitted = -1;
				Double newLotSize = -1.0;
				while(true) {
					if(myOrderH.isSubmitted == 1) {
						logger.info("Order submitted!");
						isSubmitted = 1;
						break;
					}
					
					// 处理error
					if(myOrderH.errorCode == 461) {
						newLotSize = myOrderH.newLostSize;  // 修改然后resubmit
						order.totalQuantity(newLotSize * (int)(500000 / newLotSize / buyPrice) );
						myController.placeOrModifyOrder(con, order, myOrderH);
						logger.info("need new lot size = " + newLotSize + " resubmit order!");
					}
					
					Thread.sleep(500);
				}
				
				Thread.sleep(1000 * 5);
				// monitor order
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
