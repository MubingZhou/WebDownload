package cgi.ib;

import com.ib.controller.ApiController.IHistoricalDataHandler;

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
	
	public MyIHistoricalDataHandler(String stockCode) {
		super();
		this.stockCode = stockCode;
		
		try {
			//fileWriter = new FileWriter("D:\\stock data\\IB\\historical data\\" + stockCode + ".csv", true); // append
			fileWriter = new FileWriter("D:\\stock data\\IB\\historical data\\" + stockCode + ".csv", false); // not append
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public void historicalData(Bar bar) {
		try {
			String toWrite = bar.formattedTime() + "," + bar.open() + "," + bar.high() + "," + bar.low() + "," + bar.close() + "," + bar.volume() + "\n";
			logger.debug(stockCode + " - " + toWrite);
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
		try {
			fileWriter.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
