package backtesting.backtesting;

import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import backtesting.portfolio.Portfolio;
import backtesting.portfolio.Underlying;

public class Backtesting {
	public static Logger logger = Logger.getLogger(Backtesting.class);
	
	public static Integer orderNum = 0;
	
	public String startDate = "20170101";
	public Calendar startDateCal = Calendar.getInstance();
	public String endDate = "20170825";
	public Calendar endDateCal = Calendar.getInstance();
	
	public double initialFunding = 1000000.0;
	public double tradingCost = 0.001; // in percentage
	public ArrayList<Calendar> allTradingDate = utils.Utils.getAllTradingCal(utils.PathConifiguration.ALL_TRADING_DATE_PATH_HK);
	
	public Portfolio portfolio = new Portfolio(initialFunding);
	// at this stage, assume using equal-value method
	
	public String executionOutputFilePath = "";
	
	/**
	 * 这里用到的rebalStockData的数据形式如下
	 * {
	 * 	{
	 * 		{stock1, stock2, ...},
	 * 		{direction1, direction2, ...}, // 1 - buy, -1 - sell
	 * 		{weighting1, weighting2, ...},	 // positive #: num of shares to buy/sell; negative #: percentage to buy/sell (e.g. if buy, -10 represents buying 10% of total portfolio value; if sell, -10 represents selling 10% of total holding value, that is, if holding 10000 shares, -10 means selling 1000 shares
	 * 		{price1, price2, ...}
	 * 		{comment1, comment2, ...}
	 * 	}
	 * 	,
	 * 	{
	 * 		{stock1, stock2, ...},
	 * 		{direction1, direction2, ...},
	 * 		{weighting1, weighting2, ...},
	 * 		{price1, price2, ...}
	 * 		{comment1, comment2, ...}
	 * 	}
	 * 	...
	 * }
	 * rebalStockData存储了每个需要调仓的日子的调仓数据，包括调仓的股票名称、方向、权重等
	 * @param date
	 * @param dateFormat
	 * @param rebalStockData
	 */
	public void rotationalTrading(ArrayList<String> date, String dateFormat, ArrayList<ArrayList<ArrayList<Object>>> rebalStockData) {
		System.out.println("*********** Backtesting - " + date + " ***********");
		portfolio.executionOutputFilePath=executionOutputFilePath;
		try {
			portfolio.fw = new  FileWriter(executionOutputFilePath, true);
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
						ArrayList<ArrayList<Object>> thisAllStockData = rebalStockData.get(rotationalInd);  //在这个rebalancing date需要进行rebalancing的股票数据
						ArrayList<Order> buyOrdersArr = new ArrayList<Order>();
						ArrayList<Order> sellOrdersArr = new ArrayList<Order>();
						
						ArrayList<Object> thisAllStocks = thisAllStockData.get(0);  	// String
						ArrayList<Object> thisAllDirections = thisAllStockData.get(1);  // Integer
						ArrayList<Object> thisAllWeightings = thisAllStockData.get(2);	// Double
						ArrayList<Object> thisAllPrices = thisAllStockData.get(3);		// Double
						ArrayList<Object> thisAllComments = new ArrayList<Object>();
						if(thisAllStockData.size() >= 5) {
							thisAllComments = thisAllStockData.get(4);
							logger.info("thisAllComments size=" + thisAllComments.size());
							logger.info("thisAllPrices size=" + thisAllPrices.size());
						}
						
						// ======== sell first ===============
						Map<String, Double> thisAllSellStocks_map = new HashMap<String, Double>();   // 存储需要卖出的股票的名称
						final int size = thisAllStocks.size();
						for(int j = 0; j < size; j++) {
							String stock = (String) thisAllStocks.get(j);
							Integer direction = (Integer) thisAllDirections.get(j);
							Double weighting = (Double) thisAllWeightings.get(j);
							Double price = (Double) thisAllPrices.get(j);
							String comment = "";
							if(thisAllComments != null && thisAllComments.size() > 0)
								comment = (String) thisAllComments.get(j);
							
							if(direction.equals(-1)) { // sell order
								logger.trace("[Sell] stock=" + stock + " direction=" + direction + " weighting=" + weighting + " price=" + price);
								
								double amt = 0.0;
								Underlying uly = portfolio.stockHeld.get(stock);
								if(uly != null) {  // uly == null表示当前无持仓
									if((weighting > 0) || (weighting >= -100 && weighting <= 0)) {
										if(weighting > 0)
											amt = weighting;
										if(weighting >= -100 && weighting <= 0) {
											Double currentAmt = uly.amount;
											amt = currentAmt * weighting / -100.0;
										}
										Order order = new Order(OrderType.SELL, thisCal, stock, price, amt, orderNum++);
										order.comment = comment;
										sellOrdersArr.add(order);
										thisAllSellStocks_map.put(stock, Math.abs(amt)*price);
									}else {
										logger.error("[Backtesting - Sell Amt Not Correct!] stock=" + stock + " date=" + thisDateStr + " amt=" + weighting);
									}
									
								}
							}
							
						}  // 过完一遍sell order
						portfolio.sellStocks(sellOrdersArr);
						ArrayList<String> thisAllSellStocks = new ArrayList<String>(thisAllSellStocks_map.keySet());
						
						// ======== then buy ===============		
						portfolio.commitDayEndValue();
						for(int j = 0; j < size; j++) {
							String stock = (String) thisAllStocks.get(j);
							Integer direction = (Integer) thisAllDirections.get(j);
							Double weighting = (Double) thisAllWeightings.get(j);
							Double price = (Double) thisAllPrices.get(j);
							String comment = "";
							if(thisAllComments != null && thisAllComments.size()>0)
								comment = (String) thisAllComments.get(j);
							
							if(direction.equals(1)) { // buy order
								logger.trace("[Buy] stock=" + stock + " direction=" + direction + " weighting=" + weighting + " price=" + price);
								
								double amt = 0.0;
								if(weighting > 0 || (weighting >= -100 && weighting <= 0)) {
									if(weighting > 0)
										amt = weighting;
									else if(weighting >= -100 && weighting <= 0){
										amt = portfolio.marketValue * weighting / -100 / price;
									}
									Order order = new Order(OrderType.BUY, thisCal, stock, price, amt, orderNum++);
									order.comment = comment;
									buyOrdersArr.add(order);
									
									//如果一只股票在当期既买又卖，则实际上相当于不操作，应该返还手续费
									Double valueSold = thisAllSellStocks_map.get(stock);
									if(valueSold != null) {
										Double value = Math.min(valueSold, price * amt);
										portfolio.addCash(value * 2 * tradingCost);
									}
								}else {
									logger.error("[Backtesting - Buy Amt Not Correct!] stock=" + stock + " date=" + thisDateStr + " amt=" + weighting);
								}
								
							}
						}
						portfolio.buyStocks(buyOrdersArr);
						
						/*
						if(rotationalInd > 0) { // not the first time to buy
							ArrayList<String> prevAllStocks = rebalStockData.get(rotationalInd-1);
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
						Double equalValue = cashRemained / thisAllStockData.size();
						for(int j = 0; j < thisAllStockData.size(); j++) {
							String stock = thisAllStockData.get(j);
							
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
						portfolio.buyStocks(buyOrdersArr );*/
						System.out.println("After buying - Cash Remained = " + portfolio.availableCash);
				
						
						portfolio.commitDayEndValue();
						rotationalInd++;
						
						//portfolio.fw.close();
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
