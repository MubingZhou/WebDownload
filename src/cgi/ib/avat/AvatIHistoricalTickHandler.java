package cgi.ib.avat;

import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import com.ib.client.HistoricalTick;
import com.ib.client.HistoricalTickBidAsk;
import com.ib.client.HistoricalTickLast;
import com.ib.controller.ApiController.IHistoricalTickHandler;

public class AvatIHistoricalTickHandler implements IHistoricalTickHandler{
	private static Logger logger = Logger.getLogger(AvatITopMktDataHandler.class.getName());
	private static String dateFormat = "yyyyMMdd HH:mm:ss";
	private static SimpleDateFormat sdf = new SimpleDateFormat (dateFormat); 
	
	private int numOfData_trades = 0;  // used to judge if have received all data
	private long lastTime_midpoint = 0;
	public ArrayList<Object> getData_trades() {
		return data_trades;
	}
	public void setData_trades(ArrayList<Object> data_trades) {
		this.data_trades = data_trades;
	}

	private long lastTime_bidnask = 0;
	private long lastTime_trades = 0;
	
	private int isEnd_trades = 0;
	private int isNo_trades = 0;
	
	private ArrayList<Object> data_trades = new ArrayList<Object>(); 
	

	public String stockCode;
	public FileWriter fileWriter;
	
	public AvatIHistoricalTickHandler(String stockCode) {
		super();
		
		this.stockCode = stockCode;
		
		try {
			//fileWriter = new FileWriter("D:\\stock data\\IB\\historical data\\" + stockCode + ".csv", true); // append
			fileWriter = new FileWriter("D:\\stock data\\IB\\historical tick data\\" + stockCode + ".csv", false); // not append
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void initialize() {
		isEnd_trades = 0;
		data_trades = new ArrayList<Object>(); 
		
		lastTime_midpoint = 0;
		lastTime_bidnask = 0;
		lastTime_trades = 0;
		
		isNo_trades = 0;
		
	}
	
	@Override
	public void historicalTick(int reqId, List<HistoricalTick> ticks) {   // mid point
		try {
			ArrayList<Long> timeArr = new ArrayList<Long>();
			
			for(int i = 0; i < ticks.size(); i++) {
				HistoricalTick tick = ticks.get(i);
				String time = unixDate2String(tick.time() * 1000);
				
				fileWriter.write(time + ",historicalTick," + String.valueOf(tick.price()) + "," + String.valueOf(tick.size()) + "\n");
				fileWriter.flush();
				
				timeArr.add(tick.time() * 1000);
			}
			
			lastTime_midpoint = findMax(timeArr);
		}catch(Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void historicalTickBidAsk(int reqId, List<HistoricalTickBidAsk> ticks) {  // bid & ask
		try {
			ArrayList<Long> timeArr = new ArrayList<Long>();
			
			for(int i = 0; i < ticks.size(); i++) {
				HistoricalTickBidAsk tick = ticks.get(i);
				String time = unixDate2String(tick.time() * 1000);
				
				fileWriter.write(time + ",historicalTickBidAsk," + String.valueOf(tick.mask()) + "," + String.valueOf(tick.priceBid())
					+ "," + String.valueOf(tick.sizeBid()) + "," + String.valueOf(tick.priceAsk())
					+ "," + String.valueOf(tick.sizeAsk())  + "\n");
				fileWriter.flush();
				
				timeArr.add(tick.time() * 1000);
			}
			
			lastTime_bidnask = findMax(timeArr);
		}catch(Exception e) {
			e.printStackTrace();
		}
		
	}

	@Override
	public void historicalTickLast(int reqId, List<HistoricalTickLast> ticks) {  // trades
		try {
			logger.trace("Received data! stock=" + stockCode + " data size=" + ticks.size());
			
			ArrayList<Long> timeArr = new ArrayList<Long>();
			
			isNo_trades = 1;
			for(int i = 0; i < ticks.size(); i++) {
				isNo_trades = 0;
				HistoricalTickLast tick = ticks.get(i);
				Long timeL = tick.time() * 1000;
				
				/*
				String time = unixDate2String(timeL);
				
				fileWriter.write(time + ",historicalTickLast," + String.valueOf(tick.mask()) + "," + String.valueOf(tick.price())
					+ "," + String.valueOf(tick.size()) + "," + String.valueOf(tick.exchange())
					+ "," + String.valueOf(tick.specialConditions())  + "\n");
				fileWriter.flush();
				*/
				
				ArrayList<Object> data = new ArrayList<Object>();
				data.add(timeL);
				data.add(tick.price());
				data.add(tick.size());
				
				data_trades.add(data);   // 将所有的数据存到trades data里面
				//numOfData_trades++;
				
				timeArr.add(timeL);
			}
			isEnd_trades = 1;
			//flushData_trades();
			
			if(isNo_trades == 0)
				lastTime_trades = findMax(timeArr);
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	
	public boolean flushData_trades(ArrayList<Object> thisTradeData) {
		boolean ok = true;
		try {
			for(int i = 0; i < thisTradeData.size(); i++) {
				ArrayList<Object > data = new ArrayList<Object > ();
				data = (ArrayList<Object>) thisTradeData.get(i);
				
				Long time = (Long) data.get(0);
				Double price = (Double) data.get(1);
				Long size = (Long ) data.get(2);
				
				fileWriter.write(sdf.format(new Date(time)) + "," + price + "," + size + "\n");
				fileWriter.flush();
			}
		}catch(Exception e) {
			e.printStackTrace();
			ok = false;
		}
		
		return ok;
	}
	
	/**
	 * write trades data into file
	 * @return
	 */
	public boolean flushData_trades() {
		return flushData_trades(data_trades);
	}
	
	
	public int getNumOfData_trades() {
		return numOfData_trades;
	}
	
	private String unixDate2String(Long ms) {  // 毫秒
		return sdf.format(new Date(ms));
	}
	
	private Long findMin(ArrayList<Long> a) {
		if(a == null || a.size() == 0)
			return null;
		Long min = a .get(0);
		for(int i = 1; i < a.size(); i++) {
			if(a.get(i) < min)
				min = a.get(i);
		}
		
		return min;
	}
	private Long findMax(ArrayList<Long> a) {
		if(a == null || a.size() == 0)
			return null;
		Long max = a .get(0);
		for(int i = 1; i < a.size(); i++) {
			if(a.get(i) > max)
				max = a.get(i);
		}
		
		return max;
	}

	public long getLastTime_trades() {
		return lastTime_trades;
	}

	public long getLastTime_bidnask() {
		return lastTime_bidnask;
	}

	public long getLastTime_midpoint() {
		return lastTime_midpoint;
	}

	
	public int getIsEnd_trades() {
		return isEnd_trades;
	}

	public void setIsEnd_trades(int isEnd_trades) {
		this.isEnd_trades = isEnd_trades;
	}
	public int getIsNo_trades() {
		return isNo_trades;
	}
	public void setIsNo_trades(int isNo_trades) {
		this.isNo_trades = isNo_trades;
	}
	
	public void close() {
		try {
			fileWriter.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
