package cgi.ib.avat;

import java.io.File;
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

public class AvatITopMktDataHandler implements ITopMktDataHandler{
	private static Logger logger = Logger.getLogger(AvatITopMktDataHandler.class.getName());
	private static String dateFormat = "yyyyMMdd HH:mm:ss";
	private static SimpleDateFormat sdf = new SimpleDateFormat (dateFormat); 
	private String tradeInfo = "";
	private String orderBookInfo = "";
	private String rtInfo = "";
	
	public Double latestVolume = 0.0;
	public Double latestPrice = 0.0;
	public Double latestVWAP = 0.0;
	public Double latestRTVolume = 0.0;   // should similar to latestVolume
	public Double latestRTTurnover = 0.0; // = latestRTVolume * latestVWAP
	public Double latestTrdRTVolume = 0.0; // will not include unreportable volume (this num will be used to calculate avat because historical 1min bar data also contains no unreportable volume)
	public Double latestTrdVWAP = 0.0;
	public Double latestTrdRTTurnover = 0.0; 
	//public long lastVolumeTimeStamp = 0;   		// 以毫秒计数
	//public long lastPriceTimeStamp = 0;   		// 以毫秒计数
	public Double latestBestAsk = -1.0;
	public Double latestBestBid = -1.0;
	
	public String stockCode;
	public Contract contract;
	public String fileWriterMainPath = "D:\\stock data\\IB\\realtime data\\";
	public String AVAT_ROOT_PATH = "";
	public FileWriter fileWriter_raw;
	public FileWriter fileWriter_trade;  // records trade data
	public FileWriter fileWriter_orderbook;  // records orderbook data (level 1)
	public FileWriter fileWriter_rt;  // records real time day OHLCV
	public FileWriter filerWriter_temp; // write temp data: time & volume, time & price
	public String todayDate = "";
	
	/*
	 * avat - this arrayList contains 3 elements, each is also a list which contains Time/Volume/Price information
	 * ArrayList<Object> -> {ArrayList<Date>, ArrayList<Double>, ArrayList<Double>}
	 * Everyday time range: 9:31:00 - 12:00:00 & 13:00:01 - 16:10:00   time interval: 1 min
	 */
	//public ArrayList<Object> avat = new ArrayList<Object> (); 
	
	public AvatITopMktDataHandler(Contract contract, String OUTPUT_ROOT_PATH, String todayDate) {
		super();
		this.contract = contract;
		this.stockCode = contract.symbol();
		this.todayDate =todayDate;
		fileWriterMainPath = utils.Utils.addBackSlashToPath(OUTPUT_ROOT_PATH) + todayDate + "\\";
		
		try {
			File f = new File(fileWriterMainPath);
			if(!f.exists())
				f.mkdir();
			
			fileWriter_raw = new FileWriter(fileWriterMainPath + stockCode + ".csv", true); // append
			fileWriter_trade = new FileWriter(fileWriterMainPath + stockCode + " trade.csv", true); // append
			fileWriter_orderbook = new FileWriter(fileWriterMainPath + stockCode + " orderbook.csv", true); // append
			fileWriter_rt = new FileWriter(fileWriterMainPath + stockCode + " rt.csv", true); // append
			//filerWriter_temp = new FileWriter(fileWriterMainPath + "temp data\\" + stockCode + ".csv", false); // not append
			
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
			latestBestBid = price;
			break;
		case ASK:  // ask price
			orderBookInfo = info;
			latestBestAsk = price;
			break;
		case LAST: // last price
			tradeInfo =  info;
			latestPrice = price;
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
			logger.trace("Unknown tick price!");
			break;
		}
		logger.trace("[" + stockCode + "]" + info);
		
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
			orderBookInfo = info; //logger.info(info);
			break;
		case ASK_SIZE:
			orderBookInfo = info;
			break;
		case LAST_SIZE:
			tradeInfo =  info;
			break;
		case VOLUME:
			rtInfo = info;
			latestVolume = (double) size;
			break;
		default:
			logger.trace("Unknown tick size!");
			break;
		}
		
		logger.trace("[" + stockCode + "]" + info);
		
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
		switch(tickType) {
		case LAST_TIMESTAMP:
			info = sysTime + "," + tickType.name() + "," + timeStamp2Date(value, dateFormat);
			tradeInfo =  info; 
			break;
		case RT_VOLUME:
			info = sysTime + "," + tickType.name() + "," + value;
			tradeInfo =  info;
			String[] valueArr = value.split(";"	);
			latestRTVolume = Double.parseDouble(valueArr[3]);
			latestVWAP = Double.parseDouble(valueArr[4]);
			latestRTTurnover = latestRTVolume * latestVWAP;  
			break;
		case RT_TRD_VOLUME:
			info = sysTime + "," + tickType.name() + "," + value;
			tradeInfo =  info;
			String[] valueArr2 = value.split(";"	);
			latestTrdRTVolume = Double.parseDouble(valueArr2[3]);
			latestTrdVWAP = Double.parseDouble(valueArr2[4]);
			latestTrdRTTurnover = latestTrdRTVolume * latestTrdVWAP;  
			break;
		default:
			logger.trace("Unknown tick string!");
			break;
		}
		
		logger.trace("[" + stockCode + "]" + info);
		
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
		logger.trace("[" + stockCode + "]" + info);
		
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
		logger.trace("[" + stockCode + "]" + info);
		
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
		logger.trace("[" + stockCode + "]" + info);
		
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
	
	/**
	 * For ib only. ib传回来的timestamp以秒计数，而系统自带的timestamp以毫秒计数
	 * @param timeStamp
	 * @param format
	 * @return
	 */
	private String timeStamp2Date(String timeStamp, String format) {
		Long timestampL = Long.parseLong(timeStamp) * 1000;
        String date = new SimpleDateFormat(format).format(new Date(timestampL));
        return date;
	}

}
