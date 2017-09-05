package backtesting.backtesting;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)  
//XML文件中的根标识  
@XmlRootElement(name = "PortfolioOneDaySnapshot")  
//控制JAXB 绑定类中属性和字段的排序  
@XmlType(propOrder = {   
     "marketValue",   
     "cashRemained",     
     "stockHeld",   
})  
public class PortfolioOneDaySnapshot implements Serializable {
	private static final long serialVersionUID = 1L;
	
	public Double marketValue;
	public Double cashRemained;
	public Map<String, Double> stockHeld = new HashMap();
	
	public PortfolioOneDaySnapshot() {
		super();
	}

}
