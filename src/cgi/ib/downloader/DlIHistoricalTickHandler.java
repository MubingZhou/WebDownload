package cgi.ib.downloader;

import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.ib.client.HistoricalTick;
import com.ib.client.HistoricalTickBidAsk;
import com.ib.client.HistoricalTickLast;
import cgi.ib.MyIHistoricalTickHandler;

public class DlIHistoricalTickHandler extends MyIHistoricalTickHandler{
	private static Logger logger = Logger.getLogger(DlIHistoricalTickHandler.class.getName());
	private static String dateFormat = "yyyyMMdd HH:mm:ss";
	private static SimpleDateFormat sdf = new SimpleDateFormat (dateFormat);
	private static SimpleDateFormat sdf_date = new SimpleDateFormat ("yyyyMMdd");
	private static SimpleDateFormat sdf_time = new SimpleDateFormat("HH:mm:ss");
	
	private int numOfData_trades = 0;  // used to judge if have received all data

	private long lastTime = 0; // last time we download the data (not the trade time)
	
	//private int isEnd_trades = 0;   // whether finished receiving all data
	//private int isNo_trades = 0;	// whether no trades are received in this round (if so, the contract expires after the last time for futures)
	private boolean isNoData  = false;   // whether there is no data this round 
	
	private Map<Long, Object> data_trades = new HashMap<Long, Object>();  // long - time in miliseconds
	

	public String stockCode;
	public FileWriter fileWriter;
	public String rootPath; 
	public int request_numOfData = 0;
	public String type;   // TRADES, BID_ASK, MIDPOINT
	public boolean writeOrder = true; // true - write data in asc time order (oldest data first), false - desc time order
	
	public DlIHistoricalTickHandler(String stockCode, String rootPath, String type, int request_numOfData) {
		super();
		this.stockCode = stockCode;
		this.rootPath = utils.Utils.addBackSlashToPath(rootPath);
		this.request_numOfData = request_numOfData;
		this.type = type;
		try {
			boolean isAppend = true;
			fileWriter = new FileWriter(this.rootPath + stockCode + ".csv", isAppend); 
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		initialize();	
	}
	public void initialize() {
		//isEnd_trades = 0;
		data_trades = null;
		data_trades = new HashMap<Long, Object>(); 
		
		lastTime = 0;
		
		isNoData = false;
		
		//isNo_trades = 0;
		
		numOfData_trades = 0;
	}
	
	@Override
	public void historicalTick(int reqId, List<HistoricalTick> ticks) {   // mid point
		try {
			ArrayList<Long> timeArr = new ArrayList<Long>();
			
			for(int i = 0; i < ticks.size(); i++) {
				HistoricalTick tick = ticks.get(i);
				String time = unixDate2StringTime(tick.time() * 1000);
				String date = unixDate2StringDate(tick.time() * 1000);
				
				fileWriter.write(date + "," + time + "," + String.valueOf(tick.price()) + "," + String.valueOf(tick.size()) + "\n");
				fileWriter.flush();
				
				timeArr.add(tick.time() * 1000);
			}
			
			lastTime = findMax(timeArr);
		}catch(Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void historicalTickBidAsk(int reqId, List<HistoricalTickBidAsk> ticks) {  // bid & ask
		try {			
			for(int i = 0; i < ticks.size(); i++) {
				HistoricalTickBidAsk tick = ticks.get(i);
				String time = unixDate2StringTime(tick.time() * 1000);
				String date = unixDate2StringDate(tick.time() * 1000);
				
				String toWrite = date + "," + time + "," + String.valueOf(tick.mask()) + "," + String.valueOf(tick.priceBid())
				+ "," + String.valueOf(tick.sizeBid()) + "," + String.valueOf(tick.priceAsk())
				+ "," + String.valueOf(tick.sizeAsk())  + "\n";
				fileWriter.write(toWrite);

			}
			

		}catch(Exception e) {
			e.printStackTrace();
		}
		
	}

	@Override
	public void historicalTickLast(int reqId, List<HistoricalTickLast> ticks) {  // trades
		try {
			System.out.println("Got data, size=" + ticks.size());
			
			if(ticks == null || ticks.size() == 0) {
				isNoData = true;
				return;
			}
			ArrayList<String> toWriteArr = new ArrayList<String> (); 
			for(int i = 0; i < ticks.size(); i++) {			
				HistoricalTickLast tick = ticks.get(i);
				Long timeL = tick.time() * 1000;
				lastTime = timeL;
				String time = unixDate2StringTime(timeL);
				String date = unixDate2StringDate(timeL);
				
				String toWrite = date + "," + time + "," 
						//+ String.valueOf(tick.mask()) + "," 
						+ String.valueOf(tick.price()) + "," 
						+ String.valueOf(tick.size()) + "," 
						//+ String.valueOf(tick.exchange()) + "," 
						//+ String.valueOf(tick.specialConditions())  
						+ "\n";
				toWriteArr.add(toWrite);
				numOfData_trades++;
				//System.out.println("i=" + i + " numOfData_trades=" + numOfData_trades + "\n" + toWrite);
			}			
			
			int t1 = 0;
			int t2 = 0;
			if(!writeOrder) {  // reverse order
				t1 = -1;
				t2 = toWriteArr.size()-1;
			}
			for(int i = 0; i < toWriteArr.size(); i++) {
				fileWriter.write(toWriteArr.get(t1 * i + t2));
			}
			fileWriter.flush();
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	
	public boolean flushData_trades(Map<Long, Object> thisTradeData) {
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
	private String unixDate2StringDate(Long ms) {  // mili seconds
		return sdf_date.format(new Date(ms));
	}
	private String unixDate2StringTime(Long ms) {  // mili seconds
		return sdf_time.format(new Date(ms));
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
	
	public Long getLastDataTime() {
		return lastTime;
	}
	
	public boolean isNoData() {
		return isNoData;
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
