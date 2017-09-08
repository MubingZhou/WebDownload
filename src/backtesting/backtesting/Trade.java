package backtesting.backtesting;

import java.io.Serializable;
import java.util.Calendar;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import utils.Currency;

@XmlAccessorType(XmlAccessType.FIELD)  
//XML文件中的根标识  
@XmlRootElement(name = "Trade")  
//控制JAXB 绑定类中属性和字段的排序  
@XmlType(propOrder = {   
   "type",   
   "date",     
   "stock",
   "price",
   "amount",
   "tradingCost",
   "realizedPnL",
   "currency",
   "tradeId",
   "dummy100",
   "dummy200",
})  
public class Trade implements Serializable,Cloneable {
	public Trade(TradeType type, Calendar date, String stock, Double price, Double amount, Integer tradeId) {
		super();
		this.type = type;
		this.date = date;
		this.stock = stock;
		this.price = price;
		this.amount = amount;
		this.tradeId = tradeId;
	}

	private static final long serialVersionUID = 1L;

	public TradeType type;
	
	public Calendar date;
	public String stock;
	public Double price;
	public Double amount;
	public Double tradingCost = 0.0;
	public Double realizedPnL = 0.0;
	public Currency currency = Currency.HKD;
	public Integer tradeId = -1;
	
	public Double dummy100 = 0.0;
	
	public String dummy200 = "";
	
	public Trade() {
		super();
	}
	
	@Override
	protected Object clone() throws CloneNotSupportedException {
		Trade new_t = (Trade) super.clone();
		new_t.date = (Calendar) date.clone();
		
		return new_t;
	}

}
