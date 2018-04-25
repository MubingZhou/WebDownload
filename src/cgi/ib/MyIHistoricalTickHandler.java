package cgi.ib;

import java.util.List;

import com.ib.client.HistoricalTick;
import com.ib.client.HistoricalTickBidAsk;
import com.ib.client.HistoricalTickLast;

import cgi.ib.MyAPIController.IHistoricalTickHandler;


public class MyIHistoricalTickHandler implements IHistoricalTickHandler{
	public MyIHistoricalTickHandler() {	
		try {

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
	@Override
	public void historicalTick(int reqId, List<HistoricalTick> ticks) {   // mid point
		try {
	
		}catch(Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void historicalTickBidAsk(int reqId, List<HistoricalTickBidAsk> ticks) {  // bid & ask
		try {

		}catch(Exception e) {
			e.printStackTrace();
		}
		
	}

	@Override
	public void historicalTickLast(int reqId, List<HistoricalTickLast> ticks) {  // trades
		try {
	
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	

}
