package cgi.ib.avat;

import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;

import com.ib.client.Contract;
import com.ib.controller.ApiController.IRealTimeBarHandler;
import com.ib.controller.Bar;

public class AvatIRealTimeBarHandler implements IRealTimeBarHandler{
	public String stockCode;
	public Contract contract;
	public FileWriter fileWriter;
	
	private static Logger logger = Logger.getLogger(AvatITopMktDataHandler.class.getName());
	
	public AvatIRealTimeBarHandler(String stockCode) {
		super();
		this.stockCode = stockCode;
		try {
			fileWriter = new FileWriter("D:\\stock data\\IB\\intraday data\\" + stockCode + ".csv", true); // append
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public void realtimeBar(Bar bar) {
		// TODO Auto-generated method stub
		Date date =  new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
		
		String info = bar.formattedTime() + "," + String.valueOf(bar.open()) + "," + String.valueOf(bar.high())+ "," 
				+ String.valueOf(bar.low())+ "," + String.valueOf(bar.close()) + "," + String.valueOf(bar.volume());
		
		try {
			fileWriter.write(info + "\n");
			
			logger.info("stock=" + stockCode + " " + info);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
