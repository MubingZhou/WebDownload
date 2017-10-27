package ashare.t_2limitup;

import java.util.Date;

public class TradeRecord implements Cloneable{
	public String stock;
	
	public Date buyDate;
	public Date sellDate;
	public Double buyPrice;
	public Double sellPrice;
	
	public Double numShares;
	public Double posValue;  // postion value when entered
	
	public Double profit;
	public Double profitPercent; // profit as entry pos value
	
	public Double cumProfit;
	
	public TradeRecord(String stock, Date buyDate, Date sellDate, Double buyPrice, Double sellPrice, Double numShares) {
		this.stock = stock;
		this.buyDate = buyDate;
		this.sellDate = sellDate;
		this.buyPrice = buyPrice;
		this.sellPrice = sellPrice;
		this.numShares = numShares;
	}
	
	public TradeRecord() {
		
	}
	
	protected Object clone() throws CloneNotSupportedException {  
		TradeRecord tr = (TradeRecord)super.clone();  
		tr.buyDate = (Date) this.buyDate.clone();
		tr.sellDate = (Date) this.sellDate.clone();
        return tr;
    }  
	
	
}
