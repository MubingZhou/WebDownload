package cgi.ib;

import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import com.ib.client.HistoricalTick;
import com.ib.client.HistoricalTickBidAsk;
import com.ib.client.HistoricalTickLast;
import com.ib.controller.ApiController.IHistoricalTickHandler;

public class MyIHistoricalTickHandler implements IHistoricalTickHandler{
	private static Logger logger = Logger.getLogger(MyITopMktDataHandler.class.getName());
	private static String dateFormat = "yyyyMMdd HH:mm:ss";
	private static SimpleDateFormat sdf = new SimpleDateFormat (dateFormat); 
	
	private int numOfData_tickLast = 0;  // used to judge if have received all data
	private long lastTime = 0;
	
	
	public String stockCode;
	public FileWriter fileWriter;
	
	public MyIHistoricalTickHandler(String stockCode) {
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
	
	@Override
	public void historicalTick(int reqId, List<HistoricalTick> ticks) {
		try {
			for(int i = 0; i < ticks.size(); i++) {
				HistoricalTick tick = ticks.get(i);
				String time = unixDate2String(tick.time() * 1000);
				
				fileWriter.write(time + ",historicalTick," + String.valueOf(tick.price()) + "," + String.valueOf(tick.size()) + "\n");
				fileWriter.flush();
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void historicalTickBidAsk(int reqId, List<HistoricalTickBidAsk> ticks) {
		try {
			for(int i = 0; i < ticks.size(); i++) {
				HistoricalTickBidAsk tick = ticks.get(i);
				String time = unixDate2String(tick.time() * 1000);
				
				fileWriter.write(time + ",historicalTickBidAsk," + String.valueOf(tick.mask()) + "," + String.valueOf(tick.priceBid())
					+ "," + String.valueOf(tick.sizeBid()) + "," + String.valueOf(tick.priceAsk())
					+ "," + String.valueOf(tick.sizeAsk())  + "\n");
				fileWriter.flush();
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
		
	}

	@Override
	public void historicalTickLast(int reqId, List<HistoricalTickLast> ticks) {
		try {
			for(int i = 0; i < ticks.size(); i++) {
				HistoricalTickLast tick = ticks.get(i);
				String time = unixDate2String(tick.time() * 1000);
				
				fileWriter.write(time + ",historicalTickLast," + String.valueOf(tick.mask()) + "," + String.valueOf(tick.price())
					+ "," + String.valueOf(tick.size()) + "," + String.valueOf(tick.exchange())
					+ "," + String.valueOf(tick.specialConditions())  + "\n");
				fileWriter.flush();
				
				numOfData_tickLast++;
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public int getNumOfData_tickLast() {
		return numOfData_tickLast;
	}
	
	private String unixDate2String(Long ms) {  // 毫秒
		return sdf.format(new Date(ms));
	}

}
