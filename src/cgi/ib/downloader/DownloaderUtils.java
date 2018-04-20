package cgi.ib.downloader;

import com.ib.controller.ApiConnection;

import cgi.ib.MyAPIController;
import cgi.ib.MyIConnectionHandler;
import cgi.ib.MyLogger;

public class DownloaderUtils {
	
	/**
	 * Connet to IB and return MyAPIController. If connection not successful, return null
	 * @param host
	 * @param port
	 * @param clientId
	 * @return
	 */
	public static MyAPIController connect(String host, int port, int clientId) {
		boolean getConnected = false;
		MyAPIController myController = null;
		
		try {
			MyLogger inLogger = new MyLogger();
			MyLogger outLogger = new MyLogger();
			
			MyIConnectionHandler myConnectionHandler = new MyIConnectionHandler();
			myController = new MyAPIController(myConnectionHandler, inLogger, outLogger	);
			myController.connect(host, port, clientId, null);
			
			// create EClient
			ApiConnection myClient = myController.client();  
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
				return null;
			}
		}catch(Exception e	) {
			e.printStackTrace();
		}
		
		
		return myController;
	}
}
