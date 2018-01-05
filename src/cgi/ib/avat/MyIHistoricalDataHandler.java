package cgi.ib.avat;

import com.ib.controller.ApiController.IHistoricalDataHandler;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.log4j.Logger;

import com.ib.client.Contract;
import com.ib.controller.Bar;

public class MyIHistoricalDataHandler implements IHistoricalDataHandler{
	private static Logger logger = Logger.getLogger(MyITopMktDataHandler.class.getName());
	
	public String stockCode;
	public Contract contract;
	public FileWriter fileWriter;
	public int isEnd = 0;
	public int isActive = 1;  // 如果在收到所有历史数据后，没有取消request的话，这个isActive仍然是1，
	
	public int reqId = -1;
	
	//public String AVAT_ROOT_PATH = "Z:\\AVAT\\";
	
	public MyIHistoricalDataHandler(String stockCode, String downloadRooPath) {
		super();
		this.stockCode = stockCode;
		
		try {
			if(!downloadRooPath.substring(downloadRooPath.length()-1, downloadRooPath.length()).equals("\\"))
				downloadRooPath += "\\";
			//fileWriter = new FileWriter("D:\\stock data\\IB\\historical data\\" + stockCode + ".csv", true); // append
			//fileWriter = new FileWriter("D:\\stock data\\IB\\historical data 20170928\\" + stockCode + ".csv", false); // not append
			
			File f = new File(downloadRooPath);
			if(!f.exists())
				f.mkdirs();
			
			String fileName = downloadRooPath + stockCode + ".csv";
			fileWriter = new FileWriter(fileName, false); // not append
			
			//AVAT_ROOT_PATH = downloadRooPath;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	

	@Override
	public void historicalData(Bar bar) {
		try {
			String toWrite = bar.formattedTime() + "," + bar.open() + "," + bar.high() + "," + bar.low() + "," + bar.close() + "," + bar.volume() + "\n";
			logger.trace(stockCode + " - " + toWrite);
			fileWriter.write(toWrite);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	@Override
	public void historicalDataEnd() {
		logger.trace("stock code = " + stockCode + " historical bar END!");
		isEnd = 1;
		isActive = 0;
		try {
			fileWriter.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
