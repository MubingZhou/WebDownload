package cgi.ib.avat;

import com.ib.client.Contract;

public class HoldingRecord {
	public String stockCode;
	public Contract contract;
	
	public long orderTimeStamp = 0;
	public Double orderPrice = 0.0;
	public Double orderQty = 0.0;
	
	public long filledTimeStamp = 0;
	public Double filledPrice = 0.0;
	public Double filledQty = 0.0;

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
	
	public HoldingRecord() {
		
	}
}
