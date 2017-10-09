package cgi.ib.avat;

public class HoldingRecords {
	public String stockCode;
	
	public long orderTimeStamp = 0;
	public Double orderPrice = 0.0;
	public Double orderQty = 0.0;
	
	public long filledTimeStamp = 0;
	public Double filledPrice = 0.0;
	public Double filledQty = 0.0;

	public Double tradingCost = 0.0;
	public int isFilled = 0;  // 是否完全fill
	
	public HoldingRecords(String stockCode, long orderTimeStamp, Double orderPrice, Double orderQty) {
		this.stockCode = stockCode;
		this.orderTimeStamp = orderTimeStamp;
		this.orderPrice = orderPrice;
		this.orderQty = orderQty;
	}
}
