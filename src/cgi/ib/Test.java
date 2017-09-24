package cgi.ib;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.Arrays;

import com.ib.client.Contract;
import com.ib.client.EReaderSignal;
import com.ib.client.Types.WhatToShow;
import com.ib.controller.ApiConnection;


public class Test {
	public static void main(String[] args) {
		try {
			String host = "127.0.0.1";   //  "127.0.0.1" the local host
			int port = 7496;
			int clientId = 0;  // a self-specified unique client ID
			
			MyLogger inLogger = new MyLogger();
			MyLogger outLogger = new MyLogger();
			
			MyIConnectionHandler myConnectionHandler = new MyIConnectionHandler();
			//****** the main controller **********
			MyIBAPIController myController = new MyIBAPIController(myConnectionHandler, inLogger, outLogger	);
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
			BufferedReader bf = utils.Utils.readFile_returnBufferedReader("D:\\stock data\\all stock list.csv");
			stockList.addAll(Arrays.asList(bf.readLine().split(",")));
			for(int i = 0; i < -1; i ++) {
				Contract con1 = new Contract();
				con1.symbol(stockList.get(i));
				con1.exchange("SEHK");
				con1.secType("STK");
				con1.currency("HKD");
				
				conArr.add(con1);
			}
			bf.close();
			Contract con1 = new Contract();
			con1.symbol("CNH");
			con1.exchange("IDEALPRO");
			con1.currency("HKD");
			con1.secType("CASH");
			conArr.add(con1);
			
			// ========== requesting top mkt data =========
			ArrayList<MyITopMktDataHandler> topMktDataHandlerArr = new ArrayList<MyITopMktDataHandler>();
			if(true) {
				for(int i = 0; i < conArr.size(); i++) {
					Contract con = conArr.get(i);
					MyITopMktDataHandler myTop = new MyITopMktDataHandler(con.symbol());
					topMktDataHandlerArr.add(myTop);
					myController.reqTopMktData(con, "", false, false, myTop);
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
			
			
			System.out.println("here11234");
			// pause and disconnect
			try {   
				Thread.sleep(1000 * 5);
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
				myTop.fileWriter.close();
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
}
