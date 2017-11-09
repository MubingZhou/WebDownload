package cgi.ib.a_share;

import com.ib.client.Contract;
import com.ib.controller.ApiConnection;

import cgi.ib.avat.MyAPIController;
import cgi.ib.avat.MyIConnectionHandler;
import cgi.ib.avat.MyLogger;

public class Main {

	public static void main(String[] args) {
		try {
			
			/*------------------ Connect to IB -----------------------*/
			String host = "127.0.0.1";   //  "127.0.0.1" the local host
			int port = 7496;   	// 7497 - paper account
								// 7496 - real account
			//int clientId = (int) (Math.random() * 100) + 1;  // a self-specified unique client ID
			int clientId = 1;
			
			//[start] 
			MyLogger inLogger = new MyLogger();
			MyLogger outLogger = new MyLogger();
			
			
			MyAShareConnectionHandler myConnectionHandler = new MyAShareConnectionHandler();
			//****** the main controller **********
			MyAShareAPIController myController = new MyAShareAPIController(myConnectionHandler, inLogger, outLogger	);
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
			
			/*----------------------Request realtime data---------------------------*/
			Contract con1 = new Contract();
			con1.symbol("300001");
			con1.exchange("CHINEXT");
			con1.secType("STK");
			con1.currency("CNH");
			
			MyAShareTopMktDataHandler h = new MyAShareTopMktDataHandler(con1);
			myController.reqTopMktData(con1, null, false, false, h);
			
			
			/*----------------------END---------------------------*/
			Thread.sleep(1000 * 10000000);
		}catch(Exception e) {
			e.printStackTrace();
		}

	}

}
