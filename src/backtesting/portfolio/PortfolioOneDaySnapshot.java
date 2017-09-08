package backtesting.portfolio;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import backtesting.backtesting.Order;
import backtesting.backtesting.Trade;

@XmlAccessorType(XmlAccessType.FIELD)  
//XML文件中的根标识  
@XmlRootElement(name = "PortfolioOneDaySnapshot")  
//控制JAXB 绑定类中属性和字段的排序  
@XmlType(propOrder = {   
     "marketValue",   
     "cashRemained",     
     "stockHeld",   
     "todayTrades",
     "todayOrders",
     "todayCal",
})  
public class PortfolioOneDaySnapshot implements Serializable {
	private static final long serialVersionUID = 1L;
	
	public Double marketValue;
	public Double cashRemained;
	public Map<String, Underlying> stockHeld = new HashMap();  // String = stock, Double = amt
	public ArrayList<Trade> todayTrades;
	public ArrayList<Order> todayOrders;
	public Calendar todayCal;
	
	public PortfolioOneDaySnapshot() {
		super();
	}

}
