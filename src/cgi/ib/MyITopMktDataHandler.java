package cgi.ib;

import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.apache.log4j.Logger;

import com.ib.client.Contract;
import com.ib.client.TickAttr;
import com.ib.client.TickType;
import com.ib.controller.ApiController.ITopMktDataHandler;

public class MyITopMktDataHandler implements ITopMktDataHandler{
	private static Logger logger = Logger.getLogger(MyITopMktDataHandler.class.getName());
	private static String dateFormat = "yyyyMMdd HH:mm:ss";
	private static SimpleDateFormat sdf = new SimpleDateFormat (dateFormat); 
	private String tradeInfo = "";
	private String orderBookInfo = "";
	private String rtInfo = "";
	
	private Date lastTimeStamp = new Date();
	
	public String stockCode;
	public Contract contract;
	public String fileWriterMainPath = "D:\\stock data\\IB\\realtime data test\\";
	public FileWriter fileWriter_raw;
	public FileWriter fileWriter_trade;  // records trade data
	public FileWriter fileWriter_orderbook;  // records orderbook data (level 1)
	public FileWriter fileWriter_rt;  // records real time day OHLCV
	
	/*
	 * avat - this arrayList contains 3 elements, each is also a list which contains Time/Volume/Price information
	 * ArrayList<Object> -> {ArrayList<Date>, ArrayList<Double>, ArrayList<Double>}
	 * Everyday time range: 9:31:00 - 12:00:00 & 13:00:01 - 16:10:00   time interval: 1 min
	 */
	public ArrayList<Object> avat = new ArrayList<Object> (); 
	
	public MyITopMktDataHandler(String stockCode) {
		super();
		this.stockCode = stockCode;
		try {
			fileWriter_raw = new FileWriter(fileWriterMainPath + stockCode + ".csv", true); // append
			fileWriter_trade = new FileWriter(fileWriterMainPath + stockCode + " trade.csv", true); // append
			fileWriter_orderbook = new FileWriter(fileWriterMainPath + stockCode + " orderbook.csv", true); // append
			fileWriter_rt = new FileWriter(fileWriterMainPath + stockCode + " rt.csv", true); // append
			
			// initializing avat
			String dateFormat = "yyyyMMdd HH:mm:ss";
			SimpleDateFormat sdf = new SimpleDateFormat (dateFormat); 
			ArrayList<Date> timeArr = new ArrayList<Date>();
			ArrayList<Double> volArr = new ArrayList<Double>();
			ArrayList<Double> priceArr = new ArrayList<Double>();
			
			SimpleDateFormat sdf2 = new SimpleDateFormat("yyyyMMdd");
			String todayDate = sdf2.format(new Date());
			
			//FileWriter fw_tmp = new FileWriter("D:\\test.csv");
			
			Long _1min = (long) (1000 * 60);
			// first one
			Date start1 = sdf.parse(todayDate + " 09:31:00"	); 
			//fw_tmp.write(todayDate + " 09:31:00\n" );
			timeArr.add(start1);
			volArr.add(0.0);
			priceArr.add(0.0);
			
			Date break1 = sdf.parse(todayDate + " 12:00:01");
			Date start2 = sdf.parse(todayDate + " 13:00:59");
			Date break2 = sdf.parse(todayDate + " 16:10:01");
			
			Date nextTime = new Date(start1.getTime() + _1min);
			while(nextTime.before(break2)) {
				if(nextTime.before(break1) || nextTime.after(start2)) { // trading hours
					timeArr.add(nextTime);
					volArr.add(0.0);
					priceArr.add(0.0);
					//fw_tmp.write(sdf.format(nextTime) + "\n");
				}
				nextTime = new Date(nextTime.getTime() + _1min);
				
			}
			//fw_tmp.close();
			avat.add(timeArr);
			avat.add(volArr);
			avat.add(priceArr);
				
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void tickPrice(TickType tickType, double price, TickAttr attribs) {
		// TODO Auto-generated method stub
		String info = "";
		clearInfo();
		
		String sysTime = sdf.format(new Date());
		info = sysTime + "," + tickType.name() + "," + String.valueOf(price);
		switch(tickType) {
		case BID:  // bid price
			orderBookInfo = info;
			break;
		case ASK:  // ask price
			orderBookInfo = info;
			break;
		case LAST: // last price
			tradeInfo =  info;
			break;
		case OPEN:
			rtInfo = info;
			break;
		case HIGH:
			rtInfo = info;
			break;
		case LOW:
			rtInfo = info;
			break;
		case CLOSE:
			rtInfo = info;
			break;
		default:
			logger.info("Unknown tick price!");
			break;
		}
		logger.info(info);
		
		try {
			fileWriter_raw.write(info + "\n");
			fileWriter_raw.flush();
			
			if(!orderBookInfo.equals("")) {
				fileWriter_orderbook.write(orderBookInfo + "\n"); 
				fileWriter_orderbook.flush();
			}
			if(!tradeInfo.equals("")) {
				fileWriter_trade.write(tradeInfo + "\n");
				fileWriter_trade.flush();
			}
			if(!rtInfo.equals("")) {
				fileWriter_rt.write(rtInfo + "\n");
				fileWriter_rt.flush();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	@Override
	public void tickSize(TickType tickType, int size) {
		// TODO Auto-generated method stub
		String info = "";
		clearInfo();
		
		String sysTime = sdf.format(new Date());
		info = sysTime + "," + tickType.name() + "," + String.valueOf(size);
		switch(tickType) {
		case BID_SIZE:
			orderBookInfo = info;
			break;
		case ASK_SIZE:
			orderBookInfo = info;
			break;
		case LAST_SIZE:
			tradeInfo =  info;
			break;
		case VOLUME:
			rtInfo = info;
			break;
		default:
			logger.info("Unknown tick size!");
			break;
		}
		
		logger.info(info);
		
		try {
			fileWriter_raw.write(info + "\n");
			fileWriter_raw.flush();
			
			if(!orderBookInfo.equals("")) {
				fileWriter_orderbook.write(orderBookInfo + "\n"); 
				fileWriter_orderbook.flush();
			}
			if(!tradeInfo.equals("")) {
				fileWriter_trade.write(tradeInfo + "\n");
				fileWriter_trade.flush();
			}
			if(!rtInfo.equals("")) {
				fileWriter_rt.write(rtInfo + "\n");
				fileWriter_rt.flush();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void tickString(TickType tickType, String value) {
		String info = "";
		clearInfo();
		
		String sysTime = sdf.format(new Date());
		info = sysTime + "," + tickType.name() + "," + timeStamp2Date(value, dateFormat);
		switch(tickType) {
		case LAST_TIMESTAMP:
			tradeInfo =  info; 
			break;
		case RT_VOLUME:
			tradeInfo =  info;
			break;
		case RT_TRD_VOLUME:
			tradeInfo =  info;
			break;
		default:
			logger.info("Unknown tick string!");
			break;
		}
		
		logger.info(info);
		
		try {
			fileWriter_raw.write(info + "\n");
			fileWriter_raw.flush();
			
			if(!orderBookInfo.equals("")) {
				fileWriter_orderbook.write(orderBookInfo + "\n"); 
				fileWriter_orderbook.flush();
			}
			if(!tradeInfo.equals("")) {
				fileWriter_trade.write(tradeInfo + "\n");
				fileWriter_trade.flush();
			}
			if(!rtInfo.equals("")) {
				fileWriter_rt.write(rtInfo + "\n");
				fileWriter_rt.flush();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	@Override
	public void tickSnapshotEnd() {
		String sysTime = sdf.format(new Date());
		String info = sysTime+ ",tickSnapshotEnd" ;
		//info +=  " " + sdf.format(new Date());
		logger.info(info);
		
		try {
			fileWriter_raw.write(info + "\n");
			fileWriter_raw.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	@Override
	public void marketDataType(int marketDataType) {
		String sysTime = sdf.format(new Date());
		String info = sysTime+ ",marketDataType," + String.valueOf(marketDataType);
		//info +=  " " + sdf.format(new Date());
		logger.info(info);
		
		try {
			fileWriter_raw.write(info + "\n");
			fileWriter_raw.flush();
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
		info +=  " " + sdf.format(new Date());
		logger.info(info);
		
		try {
			fileWriter_raw.write(info + "\n");
			fileWriter_raw.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	//----------------- my functions ----------------------
	private void clearInfo() {
		tradeInfo = "";
		orderBookInfo = "";
		rtInfo = "";
	}
	
	private String timeStamp2Date(String timeStamp, String format) {
		Long timestampL = Long.parseLong(timeStamp) * 1000;
        String date = new SimpleDateFormat(format).format(new Date(timestampL));
        return date;
	}

}
