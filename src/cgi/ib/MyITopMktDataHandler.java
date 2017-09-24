package cgi.ib;

import java.io.FileWriter;
import java.io.IOException;

import org.apache.log4j.Logger;

import com.ib.client.Contract;
import com.ib.client.TickAttr;
import com.ib.client.TickType;
import com.ib.controller.ApiController.ITopMktDataHandler;

public class MyITopMktDataHandler implements ITopMktDataHandler{
	private Logger logger = Logger.getLogger(this.getClass().getName());
	public String stockCode;
	public Contract contract;
	public FileWriter fileWriter;
	
	public MyITopMktDataHandler(String stockCode) {
		super();
		this.stockCode = stockCode;
		
		try {
			fileWriter = new FileWriter("D:\\stock data\\IB\\realtime data\\" + stockCode + ".csv", true); // append
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public void tickPrice(TickType tickType, double price, TickAttr attribs) {
		// TODO Auto-generated method stub
		String info = "";
		switch(tickType) {
		case BID:
			info = tickType.name() + " " + String.valueOf(price) + " " + attribs.toString();
			break;
		case ASK:
			info = tickType.name() + " " + String.valueOf(price) + " " + attribs.toString();
			break;
		default:
			break;
		}
		
		info = "[" + stockCode + " TickPrice] " + tickType.name() + " " + String.valueOf(price) + " " + attribs.toString();
		logger.info(info);
		
		try {
			fileWriter.write(info + "\n");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	@Override
	public void tickSize(TickType tickType, int size) {
		// TODO Auto-generated method stub
		String info = "[" + stockCode + " TickSize] " + tickType.name() + " " + String.valueOf(size);
		logger.info(info);
		
		try {
			fileWriter.write(info + "\n");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void tickString(TickType tickType, String value) {
		String info = "[" + stockCode + " TickString] " + tickType.name() + " " + String.valueOf(value);
		logger.info(info);
		
		try {
			fileWriter.write(info + "\n");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	@Override
	public void tickSnapshotEnd() {
		String info = "[" + stockCode + " tickSnapshotEnd] " ;
		logger.info(info);
		
		try {
			fileWriter.write(info + "\n");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	@Override
	public void marketDataType(int marketDataType) {
		String info = "[" + stockCode + " marketDataType] " + String.valueOf(marketDataType);
		logger.info(info);
		
		try {
			fileWriter.write(info + "\n");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void tickReqParams(int tickerId, double minTick, String bboExchange, int snapshotPermissions) {
		String info = "[" + stockCode + " tickReqParams] " 
					+ " tickerId=" + String.valueOf(tickerId)
					+ " minTick=" + String.valueOf(minTick)
					+ " bboExchange=" + String.valueOf(bboExchange)
					+ " snapshotPermissions=" + String.valueOf(snapshotPermissions)
					;
		logger.info(info);
		
		try {
			fileWriter.write(info + "\n");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
