package backtesting.backtesting;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)  
//XML文件中的根标识  
@XmlRootElement(name = "Order")  
//控制JAXB 绑定类中属性和字段的排序  
@XmlType(propOrder = {   
       "type",
       "date",
       "stock",
       "price",
       "amount",
       "orderId",
       "status",
       "dummy100",
       "dummy200",
}) 
public class Order implements Serializable,Cloneable {
	

	private static final long serialVersionUID = 1L;
	
	public OrderType type;
	
	public Calendar date;
	public String stock;
	public Double price;
	public Double amount;
	public Integer orderId = -1;
	public OrderStatus status = OrderStatus.UNSPECIFIED;
	public String comment = "";
	
	public Double dummy100 = 0.0;
	
	public String dummy200 = "";

	public Order () {
		super();
	}
	
	public Order(OrderType type, Calendar date, String stock, Double price, Double amount, Integer orderId ) {
		super();
		this.type = type;
		this.date = date;
		this.stock = stock;
		this.price = price;
		this.amount = amount;
		this.orderId = orderId ;
	}
	
	@Override
	public Object clone() throws CloneNotSupportedException {
		Order new_t = (Order) super.clone();
		new_t.date = (Calendar) date.clone();
		
		return new_t;
	}
	
	public String toString() {
		//String str = "";
		
		String dateStr = new SimpleDateFormat("yyyy-MM-dd").format(date.getTime());
		
		return "stock=" + stock + " date=" + dateStr 
				+ " type=" + type + " price=" + String.valueOf(price) 
				+ " amt=" + String.valueOf(amount) 
				+ " orderId=" + String.valueOf(orderId);
	}

}
