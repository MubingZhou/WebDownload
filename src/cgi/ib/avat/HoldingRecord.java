package cgi.ib.avat;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.ib.client.Contract;
import com.ib.client.OrderStatus;

public class HoldingRecord {
	public String stockCode;
	public Contract contract;
	
	public long orderTimeStamp = 0;
	public Double orderPrice = 0.0;
	public Double orderQty = 0.0;
	public int orderId = -1;
	
	public long filledTimeStamp = 0;
	public Double avgFillPrice = 0.0;
	public Double lastFillPrice = 0.0;
	public Double filledQty = 0.0;
	
	public OrderStatus status = OrderStatus .Submitted;

	public Double tradingCost = 0.0;
	public boolean isFilled = false;  // 是否完全fill
	public boolean isCancelled = false;
	
	public HoldingRecord(String stockCode, Contract contract, long orderTimeStamp, Double orderPrice, Double orderQty) {
		this.stockCode = stockCode;
		this.contract = contract;
		this.orderTimeStamp = orderTimeStamp;
		this.orderPrice = orderPrice;
		this.orderQty = orderQty;
	}
	
	
	public String toString() {
		SimpleDateFormat sdf = new SimpleDateFormat ("yyyyMMdd HH:mm:ss"); 
		String s = stockCode + "," + sdf.format(new Date(orderTimeStamp)) + ","
				+ orderPrice + "," + orderQty + "," + orderId + "," + status.toString() ;
		
		return s;
	}
}
