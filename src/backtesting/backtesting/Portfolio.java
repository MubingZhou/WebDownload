package backtesting.backtesting;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)  
//XML文件中的根标识  
@XmlRootElement(name = "Portfolio")  
//控制JAXB 绑定类中属性和字段的排序  
@XmlType(propOrder = {   
     "marketValue",   
     "cashRemained",   
     "tradingCost",   
     "stockHeld",   
     "todayCal",   
     "histSnap"
})  

public class Portfolio implements Serializable {
	private static final long serialVersionUID = 1L;
	
	public Double marketValue;
	public Double cashRemained;
	public Double tradingCost = 0.001;
	public Map<String, Double> stockHeld = new HashMap();  // String = date string, Double = holding amt, only holds current holdings
	public  Calendar todayCal;  // today's calendar 
	
	/*
	 * historical snapshot of the whole portfolio
	 * [
	 * date1, [marketValue1, cashRemained1, stockHeld1]
	 * date2, [marketValue2, cashRemained2, stockHeld2]
	 * ]
	 */
	public Map<Calendar, PortfolioOneDaySnapshot> histSnap = new HashMap();
	
	public Portfolio(double initialFunding) {
		super();
		cashRemained = initialFunding;
	}
	public Portfolio() {
		super();
	}
	
	/**
	 * Buy stocks. stockCodeArr & priceArr & amtArr should have the same length
	 * @param stockCodeArr
	 * @param priceArr
	 * @param amtArr
	 * @param date
	 * @return
	 */
	public boolean buyStocks(ArrayList<String> stockCodeArr, ArrayList<Double> priceArr, ArrayList<Double> amtArr, Calendar date) {
		boolean isOK = true;
		try {
			for(int i = 0; i < stockCodeArr.size(); i++) {
				MsgType msg = buyStock(stockCodeArr.get(i), priceArr.get(i), amtArr.get(i), date);
				
				if(!msg.equals(MsgType.Successful)) {
					System.out.println("Errors occur! Buying stock " + stockCodeArr.get(i) + " invalid");
					//break;
				}else {
					System.out.println("Bought stock " + stockCodeArr.get(i) + " price=" + priceArr.get(i) + " amt=" + amtArr.get(i));
				}
			}
		}catch(Exception e) {
			isOK = false;
		}
		
		return isOK;
	}
	
	/**
	 * Sell stocks. stockCodeArr & priceArr & amtArr should have the same length
	 * @param stockCodeArr
	 * @param priceArr
	 * @param amtArr
	 * @param date
	 * @return
	 */
	public boolean sellStocks(ArrayList<String> stockCodeArr, ArrayList<Double> priceArr, ArrayList<Double> amtArr, Calendar date) {
		boolean isOK = true;
		try {
			for(int i = 0; i < stockCodeArr.size(); i++) {
				MsgType msg = sellStock(stockCodeArr.get(i), priceArr.get(i), amtArr.get(i), date);
				
				if(!msg.equals(MsgType.Successful)) {
					System.out.println("Errors occur! Selling stock " + stockCodeArr.get(i) + " invalid");
				}else {
					System.out.println("Sold stock " + stockCodeArr.get(i) + " price=" + priceArr.get(i) + " amt=" + amtArr.get(i));
				}
			}
		}catch(Exception e) {
			isOK = false;
		}
		
		return isOK;
	}
	
	/**
	 * Buy single stock
	 * @param stockCode
	 * @param price
	 * @param amt
	 * @param date
	 * @return
	 */
	public MsgType buyStock(String stockCode, Double price, Double amt, Calendar date) {
		MsgType msg = MsgType.Successful;
		try {
			for(int i = 0; i < 1; i++) {
				if(price <= 0) {
					System.out.println("[Buy stock - price not positive] stock=" + stockCode + " price=" + price + " amt=" + amt + " date=" + date.getTime());
					msg = MsgType.PriceNotPositive;
					break;
				}
				Double cashToUse = price * amt * (1 + tradingCost);
				//System.out.println("cash remained = " + cashRemained + " cash to use = " + cashToUse);
				if(cashToUse.compareTo(cashRemained) < 0) {
					cashRemained = cashRemained - cashToUse;
					
					if(stockHeld.containsKey(stockCode)) {
						double prevAmt = stockHeld.get(stockCode);
						stockHeld.put(stockCode, amt + prevAmt);
					}else {
						stockHeld.put(stockCode, amt);
					}
				}else {
					System.out.println("[Buy stock - insufficient fund] stock=" + stockCode + " price=" + price + " amt=" + amt + " date=" + date.getTime());
					msg = MsgType.InsufficientFund;
					break;
				}
			} // end of for
		}catch(Exception e) {
			msg = MsgType.Unknown;
		}
		
		return msg;
	}
	
	/**
	 * sell single stock
	 * @param stockCode
	 * @param price
	 * @param amt
	 * @param date
	 * @return
	 */
	public MsgType sellStock(String stockCode, Double price, Double amt, Calendar date) {
		MsgType msg = MsgType.Successful;
		try {
			for(int i = 0; i < 1; i++) {
				if(price <= 0) {
					System.out.println("[Sell stock - price not positive] stock=" + stockCode + " price=" + price + " amt=" + amt + " date=" + date.getTime());
					msg = MsgType.PriceNotPositive;
					break;
				}
				if(!stockHeld.containsKey(stockCode) || stockHeld.get(stockCode).compareTo(amt) < 0) {
					System.out.println("[Sell stock - amount exceeds total holding] stock=" + stockCode 
							+ " price=" + price + " amt=" + amt + " date=" + date.getTime()
							+ " stock held=" + stockHeld.get(stockCode));
					msg = MsgType.AmountExceedTotal;
					break;
				}
				
				Double prevAmt = stockHeld.get(stockCode);
				if(amt == 0) {
					amt = prevAmt;
				}
				double cashToReceive = price * amt * (1 - tradingCost);
				cashRemained = cashRemained + cashToReceive;
				
				Double postAmt = prevAmt - amt;
				if(postAmt.equals(0.0))
					stockHeld.remove(stockCode);
				else
					stockHeld.put(stockCode, postAmt);
				
			} // end of for
		}catch(Exception e) {
			msg = MsgType.Unknown;
		}
		
		return msg;
	}
	
	public void commitDayEndValue() {
		try {
			double cumStockValue = 0.0;
			
			String dateFormat = "yyyyMMdd";
			String date = new SimpleDateFormat(dateFormat).format(todayCal.getTime());
			
			Map<String, Double> stockHeld_copy = new HashMap(stockHeld);
			Set<String> allStocks = stockHeld_copy.keySet();
			for(String stock : allStocks) {
				String closePriceStr = stockPrice.DataGetter.getStockDataField(stock, stockPrice.DataGetter.StockDataField.adjclose, date, dateFormat);
				Double closePrice = Double.parseDouble(closePriceStr);
				
				cumStockValue = cumStockValue + closePrice * stockHeld_copy.get(stock);
			}
			
			marketValue = cumStockValue + cashRemained;
			
			PortfolioOneDaySnapshot snapData = new PortfolioOneDaySnapshot();
			snapData.marketValue = marketValue;
			snapData.cashRemained = cashRemained;
			snapData.stockHeld = stockHeld_copy;
			
			histSnap.put(todayCal, snapData);
			
		}catch(Exception e) {
			e.printStackTrace();
		}
		
	}
	
	enum MsgType{
		Successful,InsufficientFund,PriceNotPositive,Unknown,AmountExceedTotal;
	}
	
}
