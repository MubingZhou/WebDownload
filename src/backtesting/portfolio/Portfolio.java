package backtesting.portfolio;

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

import backtesting.backtesting.Order;
import backtesting.backtesting.OrderStatus;
import backtesting.backtesting.OrderType;
import backtesting.backtesting.Trade;
import backtesting.backtesting.TradeType;

@XmlAccessorType(XmlAccessType.FIELD)  
//XML文件中的根标识  
@XmlRootElement(name = "Portfolio")  
//控制JAXB 绑定类中属性和字段的排序  
@XmlType(propOrder = {   
     "marketValue",   
     "availableCash",   
     "totalCash",
     "tradingCost",
     "shortMarginReq",
     "stockHeld",   
     "todayCal",   
     "histSnap",
     "allTrades",
     "allOrders",
})  

public class Portfolio implements Serializable {
	private static final long serialVersionUID = 1L;
	
	// configuration
	public Double tradingCost = 0.001;
	public Double shortMarginReq = 1.0;
	
	// variables
	public Double marketValue;
	public Double availableCash;
	public Double totalCash;
	public Map<String, Underlying> stockHeld = new HashMap();  // String = date string, Underlying = only contains current holdings
	public  Calendar todayCal;  // today's calendar 
	
	/*
	 * historical snapshot of the whole portfolio
	 * [
	 * date1, [marketValue1, cashRemained1, stockHeld1]
	 * date2, [marketValue2, cashRemained2, stockHeld2]
	 * ]
	 */
	public Map<Calendar, PortfolioOneDaySnapshot> histSnap = new HashMap();
	public ArrayList<Trade> allTrades = new ArrayList<Trade>();
	public ArrayList<Order> allOrders = new ArrayList<Order>();
	
	public Portfolio(double initialFunding) {
		super();
		availableCash = initialFunding;
		totalCash  = initialFunding;
	}
	public Portfolio() {
		super();
	}
	
	//==========================================================================================
	/**
	 * buy stocks. All orders should have BUY type
	 * @param orderArr
	 * @return
	 */
	public boolean buyStocks(ArrayList<Order> orderArr) {
		boolean isOK = true;
		try {
			for(int i = 0; i < orderArr.size(); i++) {
				Order order = orderArr.get(i);
				if(order.type != OrderType.BUY) {
					System.out.println("[Portfolio] Order type (buy) incorrect! stock=" + order.stock);
					continue;
				}
				
				MsgType msg = executeOrder(order);
				
				String stock = order.stock;
				if(!msg.equals(MsgType.Successful)) {
					System.out.println("Errors occur! Buying stock " + stock + " invalid");
					//break;
				}else {
					System.out.println("Bought stock " + stock + " price=" + order.price + " amt=" + order.amount);
				}
			}
		}catch(Exception e) {
			isOK = false;
		}
		
		return isOK;
	}
	
	/**
	 * sell stock. All orders should have SELL type
	 * @param orderArr
	 * @return
	 */
	public boolean sellStocks(ArrayList<Order> orderArr) {
		boolean isOK = true;
		try {
			for(int i = 0; i < orderArr.size(); i++) {
				Order order = orderArr.get(i);
				if(order.type != OrderType.SELL) {
					System.out.println("[Portfolio] Order type (sell) incorrect! stock=" + order.stock);
					continue;
				}
				
				MsgType msg = sellStock(orderArr.get(i));
				
				String stock = orderArr.get(i).stock;
				if(!msg.equals(MsgType.Successful)) {
					System.out.println("Errors occur! Selling stock " + stock + " invalid");
				}else {
					System.out.println("Sold stock " + stock + " price=" + orderArr.get(i).price + " amt=" + orderArr.get(i).amount);
				}
			}
		}catch(Exception e) {
			isOK = false;
		}
		
		return isOK;
	}
	
	public boolean executeOrders(ArrayList<Order> orderArr) {
		boolean isOK = true;
		try {
			for(int i = 0; i < orderArr.size(); i++) {
				Order o = orderArr.get(i);
				MsgType msg = executeOrder(o);
				
				String stock = o.stock;
				if(!msg.equals(MsgType.Successful)) {
					System.out.println("Errors occur! Executing stock " + stock + " invalid, type=" + o.type);
				}else {
					System.out.println("Sold stock " + stock + " price=" + o.price + " amt=" + o.amount);
				}
			}
		}catch(Exception e) {
			isOK = false;
		}
		
		return isOK;
	}
	
	/*
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
	*/
	public MsgType executeOrder(Order order) {
		MsgType msg = MsgType.Successful;
		try {
			switch(order.type) {
			case BUY:
				msg = buyStock(order);
				break;
			case SELL:
				msg = sellStock(order);
				break;
			case SHORT:
				msg = shortStock(order);
				break;
			case COVER:
				msg = coverStock(order);
				break;
			default:
				msg = MsgType.OrderTypeIncorrect;
					
			}
		}catch(Exception e) {
			e.printStackTrace();
			msg = MsgType.Unknown;
		}
		
		if(msg == MsgType.Successful) {
			order.status = OrderStatus.FILLED;
		}else {
			order.status = OrderStatus.FAILED;
		}
		allOrders.add(order);	
		
		return msg;
	}
	/**
	 * "order" should in the type of BUY
	 * @param order
	 * @return
	 */
	protected MsgType buyStock(Order order) {
		MsgType msg = MsgType.Successful;
		Double _tradingCost = 0.0;
		
		if(order.type != OrderType.BUY)
			return MsgType.OrderTypeIncorrect;
		
		try {
			String stockCode = order.stock;
			Double price = order.price;
			Double amt = Math.abs(order.amount);
			Calendar date = order.date;
			
			if(price <= 0) {
				System.out.println("[Buy stock - price not positive] stock=" + stockCode + " price=" + price + " amt=" + amt + " date=" + date.getTime());
				msg = MsgType.PriceNotPositive;
				return msg;
			}
			
			//======== the core action ========
			Double cashToUse = price * amt * (1 + tradingCost);
			_tradingCost = price * amt * tradingCost;
			//System.out.println("cash remained = " + cashRemained + " cash to use = " + cashToUse);
			if(cashToUse.compareTo(availableCash) < 0) {
				availableCash = availableCash - cashToUse;
				totalCash = totalCash - cashToUse;
				
				if(stockHeld.containsKey(stockCode)) {
					Underlying uly = stockHeld.get(stockCode);
					
					uly.average_price = (uly.amount * uly.average_price + amt * price + _tradingCost) / (amt + uly.amount);
					uly.amount = uly.amount + amt;
					
					
					stockHeld.put(stockCode, uly);
				}else {
					Underlying uly = new Underlying(stockCode, amt, price);
					stockHeld.put(stockCode, uly);
				}
			}else {
				System.out.println("[Buy stock - insufficient fund] stock=" + stockCode + " price=" + price + " amt=" + amt + " date=" + date.getTime());
				msg = MsgType.InsufficientFund;
				return msg;
			}
		}catch(Exception e) {
			e.printStackTrace();
			msg = MsgType.Unknown;
		}
		
		if(msg == MsgType.Successful) {
			Trade t = new Trade(TradeType.BUY, this.todayCal, order.stock, order.price, order.amount, allTrades.size());
			t.tradingCost = _tradingCost;
			allTrades.add(t);
		}
		
		return msg;
	}
	
	/**
	 * "order" should in the type of SELL
	 * @param order
	 * @return
	 */
	protected MsgType sellStock(Order order) {
		MsgType msg = MsgType.Successful;
		Double _tradingCost = 0.0;
		
		if(order.type != OrderType.SELL)
			return MsgType.OrderTypeIncorrect;
		
		try {
			String stockCode = order.stock;
			Double price = order.price;
			Double amt = Math.abs(order.amount);
			Calendar date = order.date;
			
			if(price <= 0) {
				System.out.println("[Sell stock - price not positive] stock=" + stockCode + " price=" + price + " amt=" + amt + " date=" + date.getTime());
				msg = MsgType.PriceNotPositive;
				return msg;
			}
			if(!stockHeld.containsKey(stockCode) || stockHeld.get(stockCode).amount.compareTo(amt) < 0) {
				System.out.println("[Sell stock - amount exceeds total holding] stock=" + stockCode 
						+ " price=" + price + " amt=" + amt + " date=" + date.getTime()
						+ " stock held=" + stockHeld.get(stockCode));
				msg = MsgType.AmountExceedTotal;
				return msg;
			}
			
			
			// ======= core action ========
			Underlying uly = stockHeld.get(stockCode);
			Double prevAmt = uly.amount;
			if(amt == 0.0) {
				amt = prevAmt;
			}
			double cashToReceive = price * amt * (1 - tradingCost);
			availableCash = availableCash + cashToReceive;
			totalCash = totalCash +  cashToReceive;
			_tradingCost = price * amt * tradingCost;
			
			Double postAmt = prevAmt - amt;
			Double thisRealizedPnL = amt * (price - uly.average_price) - _tradingCost;
			uly.realized_PnL = uly.realized_PnL + thisRealizedPnL;
			
			if(postAmt.equals(0.0)) {
				uly.average_price = 0.0;
			}
			
			uly.amount = postAmt;
			
			stockHeld.put(stockCode, uly);
			
		}catch(Exception e) {
			e.printStackTrace();
			msg = MsgType.Unknown;
		}
		
		if(msg == MsgType.Successful) {
			Trade t = new Trade(TradeType.SELL, this.todayCal, order.stock, order.price, order.amount, allTrades.size());
			t.tradingCost = _tradingCost;
			allTrades.add(t);
		}
		return msg;
	}
	
	/**
	 * Short a stock, amt must be negative
	 * @param order
	 * @return
	 */
	protected MsgType shortStock(Order order) {
		MsgType msg = MsgType.Successful;
		Double _tradingCost = 0.0;
		
		if(order.type != OrderType.SHORT)
			return MsgType.OrderTypeIncorrect;
		
		try {
			String stockCode = order.stock;
			Double price = order.price;
			Double amt = order.amount;
			Calendar date = order.date;
			
			if(price <= 0) {
				System.out.println("[Short stock - price not positive] stock=" + stockCode + " price=" + price + " amt=" + amt + " date=" + date.getTime());
				msg = MsgType.PriceNotPositive;
				return msg;
			}
			if(amt > 0.0) {
				System.out.println("[Short stock - amt not negative] stock=" + stockCode + " price=" + price + " amt=" + amt + " date=" + date.getTime());
				msg = MsgType.ShortAmountNotNegative;
				return msg;
			}
			
			//======== the core action ========
			Underlying uly;
			if(stockHeld.containsKey(stockCode)) {  
				uly = stockHeld.get(stockCode);
				if(uly.amount <= 0.0) {// normal condition - the underlying amout is zero or negative before this short
					Double cashToOccupy = price * (-amt) * (1 + tradingCost) / shortMarginReq;
					_tradingCost = price * (-amt) * tradingCost;
					
					// enough cash to be collateral
					if(cashToOccupy.compareTo(availableCash) < 0) {
						availableCash = availableCash - cashToOccupy;
						totalCash = totalCash + price * (-amt) * (1- tradingCost);
						uly.average_price = (uly.amount * uly.average_price + amt * price + _tradingCost) / (uly.amount + amt);
						
						uly.marginOccupied = uly.marginOccupied + cashToOccupy + price * (-amt) * (1- tradingCost);
						
						stockHeld.put(stockCode, uly);
					}else {
						System.out.println("[Short stock - insufficient margin] stock=" + stockCode + " price=" + price + " amt=" + amt + " date=" + date.getTime());
						msg = MsgType.ShortInsufficientMargin;
						return msg;
					}
				}else { // if we have positive positions before we short, this short is actually a sell order
					Order sellOrder = (Order) order.clone();
					sellOrder.type = OrderType.SELL;
					sellOrder.amount = - sellOrder.amount;
					sellStock(order);
					return MsgType.ShortOrderToSellOrder;
				}
					
			}else {
				uly = new Underlying(stockCode, amt, price);
				stockHeld.put(stockCode, uly);
			}
			
			
		}catch(Exception e) {
			e.printStackTrace();
			msg = MsgType.Unknown;
		}
		
		if(msg == MsgType.Successful) {
			Trade t = new Trade(TradeType.SHORT, this.todayCal, order.stock, order.price, order.amount, allTrades.size());
			allTrades.add(t);
		}
		
		return msg;
	}
	
	/**
	 * cover stock, amt must be positive
	 * @param order
	 * @return
	 */
	protected MsgType coverStock(Order order) {
		MsgType msg = MsgType.Successful;
		Double _tradingCost = 0.0;
		
		if(order.type != OrderType.COVER)
			return MsgType.OrderTypeIncorrect;
		
		try {
			String stockCode = order.stock;
			Double price = order.price;
			Double amt = order.amount;
			Calendar date = order.date;
			
			if(price <= 0) {
				System.out.println("[Cover stock - price not positive] stock=" + stockCode + " price=" + price + " amt=" + amt + " date=" + date.getTime());
				msg = MsgType.PriceNotPositive;
				return msg;
			}
			if(amt < 0.0) {
				System.out.println("[Cover stock - price not positive] stock=" + stockCode + " price=" + price + " amt=" + amt + " date=" + date.getTime());
				msg = MsgType.CoverAmountNotPositive;
				return msg;
			}
			
			// ========= core action =====
			if(stockHeld.containsKey(stockCode)) {  
				Underlying uly = stockHeld.get(stockCode);
				Double prevAmt = uly.amount;
				
				if(prevAmt > 0.0) { // if prev amount is > 0, then the cover order is failed
					System.out.println("[Cover stock - previous amount not negative] stock=" + stockCode + " price=" + price + " amt=" + amt + " date=" + date.getTime());
					msg = MsgType.CoverPrevAmountNotNegative;
					return msg;
				}
				
				if(amt == 0.0) {
					amt = prevAmt;
				}
				
				Double cashToUse = price * amt * (1 + tradingCost);
				if(cashToUse < uly.marginOccupied) { // if there is enough cash to buyback the stock
					totalCash = totalCash - cashToUse;
					availableCash = availableCash + uly.marginOccupied - cashToUse;
					// to be completed......
				}
				
				
			}else {
				System.out.println("[Cover stock - stock not exist] stock=" + stockCode + " price=" + price + " amt=" + amt + " date=" + date.getTime());
				msg = MsgType.StockNotExist;
				return msg;
			}
			// ========= core action =====
			Underlying uly = stockHeld.get(stockCode);
			Double prevAmt = uly.amount;
			if(amt == 0.0) {
				amt = prevAmt;
			}
			double cashToReceive = price * amt * (1 - tradingCost);
			availableCash = availableCash + cashToReceive;
			totalCash = totalCash +  cashToReceive;
			_tradingCost = price * amt * tradingCost;
			
			Double postAmt = prevAmt - amt;
			Double thisRealizedPnL = amt * (price - uly.average_price) - _tradingCost;
			uly.realized_PnL = uly.realized_PnL + thisRealizedPnL;
			
			if(postAmt.equals(0.0)) {
				uly.average_price = 0.0;
			}
			
			uly.amount = postAmt;
			
			stockHeld.put(stockCode, uly);
			
		}catch(Exception e) {
			e.printStackTrace();
			msg = MsgType.Unknown;
		}
		
		if(msg == MsgType.Successful) {
			Trade t = new Trade(TradeType.COVER, this.todayCal, order.stock, order.price, order.amount, allTrades.size());
			allTrades.add(t);
		}
		
		return msg;
	}
	
	//===================================================================================================
/*
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
	*/
	public void commitDayEndValue() {
		try {
			double cumStockValue = 0.0;
			
			String dateFormat = "yyyyMMdd";
			String date = new SimpleDateFormat(dateFormat).format(todayCal.getTime());
			
			Map<String, Underlying> stockHeld_copy = new HashMap(stockHeld);
			Set<String> allStocks = stockHeld_copy.keySet();
			for(String stock : allStocks) {
				String closePriceStr = stockPrice.DataGetter.getStockDataField(stock, stockPrice.DataGetter.StockDataField.adjclose, date, dateFormat);
				Double closePrice = 0.0;
				
				try {
					closePrice = Double.parseDouble(closePriceStr);
				}catch(Exception e) {
				}
				
				Underlying uly = (Underlying) stockHeld_copy.get(stock).clone();
				
				cumStockValue = cumStockValue + closePrice * uly.amount;
				
				uly.unrealized_PnL = (closePrice - uly.average_price) * uly.amount;
				
				stockHeld_copy.put(stock, uly);
			}
			
			marketValue = cumStockValue + availableCash;
			
			PortfolioOneDaySnapshot snapData = new PortfolioOneDaySnapshot();
			snapData.marketValue = marketValue;
			snapData.cashRemained = availableCash;
			snapData.todayCal = todayCal;
			snapData.stockHeld = stockHeld_copy;
			
			histSnap.put(todayCal, snapData);
			
		}catch(Exception e) {
			e.printStackTrace();
		}
		
	}
	
	enum MsgType{
		Successful("Successful"),InsufficientFund("InsufficientFund"),
		PriceNotPositive("PriceNotPositive"),Unknown("Unknown"),
		AmountExceedTotal("AmountExceedTotal"),OrderTypeIncorrect("OrderTypeIncorrect"),StockNotExist("StockNotExist"),
		
		ShortAmountNotNegative("ShortAmountNotNegative"), ShortInsufficientMargin("ShortInsufficientMargin"),
		ShortOrderToSellOrder("ShortOrderToSellOrder"),
		
		CoverAmountNotPositive("CoverAmountNotPositive"),CoverPrevAmountNotNegative("CoverPrevAmountNotNegative")
		
		;
		
		private String s;
		
		MsgType(String s) {
			this.s = s;
		}
		
		@Override
		public String toString() {
			return this.s;
		}
	}
	
}