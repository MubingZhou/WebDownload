package backtesting.backtesting;

import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Map;

import backtesting.portfolio.Portfolio;

public class Backtesting {
	public static Integer orderNum = 0;
	
	public String startDate = "20170101";
	public Calendar startDateCal = Calendar.getInstance();
	public String endDate = "20170825";
	public Calendar endDateCal = Calendar.getInstance();
	
	public double initialFunding = 1000000.0;
	public double tradingCost = 0.001; // in percentage
	public ArrayList<Calendar> allTradingDate = utils.Utils.getAllTradingDate("D:\\stock data\\all trading date - hk.csv");
	
	public Portfolio portfolio = new Portfolio(initialFunding);
	// at this stage, assume using equal-value method
	public void rotationalTrading(ArrayList<String> date, String dateFormat, ArrayList<ArrayList<String>> rebalStocks) {
		System.out.println("*********** Backtesting - " + date + " ***********");
		try {
			//FileWriter fw =  new FileWriter("D:\\stock data\\southbound flow strategy - db\\backtesting.csv");
			
			SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
			ArrayList<Calendar> dateArr = utils.Utils.dateStr2Cal(date, dateFormat);
			Collections.sort(dateArr); // ascending
			
			// set start & end date
			startDateCal.setTime(new SimpleDateFormat("yyyyMMdd").parse(startDate));
			endDateCal.setTime(new SimpleDateFormat("yyyyMMdd").parse(endDate));
			
			
			// ======== backtesting =====
			portfolio.availableCash = initialFunding;
			portfolio.tradingCost = tradingCost;
			int rotationalInd = 0; // trace the rotational date
			for(int i = 0; i < allTradingDate.size(); i++) {
				Calendar thisCal = allTradingDate.get(i);
				String thisDateStr = new SimpleDateFormat("yyyyMMdd").format(thisCal.getTime());
				//if(thisDateStr.equals("20160801"))
					//System.out.println("~~~ get date 20160801");
				
				Calendar rotationalCal = Calendar.getInstance();
				if(rotationalInd < dateArr.size())
					rotationalCal = dateArr.get(rotationalInd);
				
				if(!thisCal.before(startDateCal) && !thisCal.after(endDateCal)) {
					portfolio.todayCal = thisCal;
					
					if(thisCal.equals(rotationalCal)) {  // date to rotation
						System.out.println("********** rebal date = " + sdf.format(thisCal.getTime()) + " ***********");
						// ========= re-balancing =========
						ArrayList<String> thisAllStocks = rebalStocks.get(rotationalInd);
						ArrayList<Order> buyOrdersArr = new ArrayList<Order>();
						ArrayList<Order> sellOrdersArr = new ArrayList<Order>();
						
						if(rotationalInd > 0) { // not the first time to buy
							ArrayList<String> prevAllStocks = rebalStocks.get(rotationalInd-1);
							ArrayList<String> prevAllStocksCopy = prevAllStocks; 
							ArrayList<String> stocksToSell = new ArrayList<String>();
							
							// ============ sell old stocks first ============
							stocksToSell = prevAllStocksCopy;
							
							for(int j = 0; j < stocksToSell.size(); j++) {
								String stock = stocksToSell.get(j);
								Double amt = 0.0; // if amt=0.0, it will sell all quantities of the underlying stock
								
								// deal with price
								String priceStr = stockPrice.DataGetter.getStockDataField(stock,stockPrice.DataGetter.StockDataField.adjclose, thisDateStr, "yyyyMMdd");
								Double price = 0.0;
								try {
									price = Double.parseDouble(priceStr);
								}catch(Exception e) {
									
								}
								if(price.equals(0.0)) {
									System.out.println("[sell order failed - price incorrect] stock=" + stock + " price=" + priceStr);
									continue;
								}
								
								Order order = new Order(OrderType.SELL, thisCal, stock, price, amt, orderNum++);
								sellOrdersArr.add(order);
							}
							
							// sell first
							portfolio.sellStocks(sellOrdersArr);
						}
					
						// update amt to buy
						Double cashRemained = portfolio.availableCash*0.99;
						Double equalValue = cashRemained / thisAllStocks.size();
						for(int j = 0; j < thisAllStocks.size(); j++) {
							String stock = thisAllStocks.get(j);
							
							// deal with price
							String priceStr = stockPrice.DataGetter.getStockDataField(stock,stockPrice.DataGetter.StockDataField.adjclose, thisDateStr, "yyyyMMdd");
							Double price = 0.0;
							try {
								price = Double.parseDouble(priceStr);
							}catch(Exception e) {
								
							}
							if(price.equals(0.0)) {
								System.out.println("[buy order failed - price incorrect] stock=" + stock + " price=" + priceStr);
								continue;
							}
							
							// deal with amt
							Double amt = Math.floor(equalValue / (price * (1 + portfolio.tradingCost)));
							if(amt <= 0.0) {
								System.out.println("[buy order failed - amount incorrect] stock=" + stock + " price=" + priceStr + " amt=" + amt);
								continue;
							}
							
							Order order = new Order(OrderType.BUY, thisCal, stock, price, amt, orderNum++);
							buyOrdersArr .add(order);
						}
						portfolio.buyStocks(buyOrdersArr );
						System.out.println("After buying - Cash Remained = " + portfolio.availableCash);
				
						
						portfolio.commitDayEndValue();
						rotationalInd++;
						
						System.out.println("********** rebal END, date = " + sdf.format(thisCal.getTime()) + " ***********");
					}else {
						portfolio.commitDayEndValue();
					}
				}// end of if
			} // end of for
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	
		System.out.println("*********** Backtesting END - " + date + " ***********");
	}
}
