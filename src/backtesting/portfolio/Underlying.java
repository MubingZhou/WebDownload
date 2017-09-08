package backtesting.portfolio;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import utils.Currency;

@XmlAccessorType(XmlAccessType.FIELD)  
//XML文件中的根标识  
@XmlRootElement(name = "Underlying")  
//控制JAXB 绑定类中属性和字段的排序  
@XmlType(propOrder = {   
     "stock",
     "amount",
     "average_price",
     "unrealized_PnL",
     "realized_PnL",
     "currency",
     "marginOccupied",
     "dummy100",
     "dummy200",
}) 
public class Underlying implements Serializable, Cloneable{
	public Underlying(String stock, Double amount, Double average_price) {
		super();
		this.stock = stock;
		this.amount = amount;
		this.average_price = average_price;
	}

	private static final long serialVersionUID = 1L;
	
	public String stock;
	public Double amount;
	public Double average_price;
	public Double unrealized_PnL = 0.0; 
	public Double realized_PnL = 0.0;
	public Currency currency = Currency.HKD;
	public Double marginOccupied = 0.0;
	
	public Double dummy100 = 0.0;
	public String dummy200 = "";
	
	public Underlying() {
		super();
	}
	
	@Override 
	protected Object clone() throws CloneNotSupportedException {
		Underlying new_uly = (Underlying) super.clone();
		return new_uly;
	}
	
}
