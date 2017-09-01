package backtesting;

import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Map;

public class Backtesting {
	public String startDate = "20170101";
	public Calendar startDateCal = Calendar.getInstance();
	public String endDate = "20170825";
	public Calendar endDateCal = Calendar.getInstance();
	
	public double initialFunding = 1000000.0;
	public double tradingCost = 0.001; // in percentage
	public ArrayList<Calendar> allTradingDate = utils.Utils.getAllTradingDate("D:\\stock data\\all trading date - hk.csv");
	
	public Portfolio portfolio = new Portfolio(initialFunding);
	// at this stage, assume using equal-value method
	public void rotationalTrading(ArrayList<String> date, String dateFormat, ArrayList<ArrayList<String>> data) {
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
			portfolio.cashRemained = initialFunding;
			portfolio.tradingCost = tradingCost;
			int rotationalInd = 0;
			for(int i = 0; i < allTradingDate.size(); i++) {
				Calendar thisCal = allTradingDate.get(i);
				String thisDateStr = new SimpleDateFormat("yyyyMMdd").format(thisCal.getTime());
				
				Calendar rotationalCal = Calendar.getInstance();
				if(rotationalInd < dateArr.size())
					rotationalCal = dateArr.get(rotationalInd);
				
				if(!thisCal.before(startDateCal) && !thisCal.after(endDateCal)) {
					portfolio.todayCal = thisCal;
					
					if(thisCal.equals(rotationalCal)) {  // date to rotation
						System.out.println("********** rebal date = " + sdf.format(thisCal.getTime()) + " ***********");
						// ========= re-balancing =========
						ArrayList<String> thisAllStocks = data.get(rotationalInd);
						
						if(rotationalInd == 0) { // first time to buy
							int numOfStocks = thisAllStocks.size();
							System.out.println("First time to buy. Num of stocks = " + numOfStocks);
							
							ArrayList<String> priceArrStr = stockPrice.DataGetter.getStockDataField(thisAllStocks, 
									stockPrice.DataGetter.StockDataField.adjclose, thisDateStr, "yyyyMMdd");
							ArrayList<Double> priceArr = new ArrayList<Double>();
							for(int j = 0; j < priceArrStr.size(); j++) {
								priceArr .add(Double.parseDouble(priceArrStr.get(j)));
							}
							
							int temp = allTradingDate.indexOf(thisCal);
							//Calendar oneDayBefore = allTradingDate.get(temp - 1);
							Double cashRemained = portfolio.cashRemained*0.99;
							Double equalValue = cashRemained / numOfStocks;
							
							// amount
							ArrayList<Double> amtArr = new ArrayList<Double>();
							for(int j = 0; j < priceArr.size(); j++) {
								amtArr.add(Math.floor(equalValue / (priceArr.get(j) * (1 + portfolio.tradingCost))));
								
								System.out.println("stock = " + thisAllStocks.get(j) + " price = " + priceArr.get(j) + " amt = " + amtArr.get(j));
							}
							portfolio.buyStocks(thisAllStocks, priceArr, amtArr, thisCal);
						}else {
							ArrayList<String> prevAllStocks = data.get(rotationalInd-1);
							ArrayList<String> prevAllStocksCopy = prevAllStocks; 
							ArrayList<String> stocksToBuy = new ArrayList<String>();
							ArrayList<String> stocksToSell = new ArrayList<String>();
							
							if(false) {
							for(int j = 0; j < thisAllStocks.size(); j++) {
								String thisStock = thisAllStocks.get(j);
								
								if(!prevAllStocks.contains(thisStock)) {
									stocksToBuy.add(thisStock);
								}else {
									prevAllStocksCopy.remove(thisStock);
								}
							}
							stocksToSell = prevAllStocksCopy;
							
							// amt
							ArrayList<Double> amtToSell = new ArrayList<Double> ();
							for(int j = 0; j < stocksToSell.size(); j++) {
								amtToSell.add(0.0);
							}
							
							// update  sell price
							ArrayList<String> priceToSellStr = stockPrice.DataGetter.getStockDataField(stocksToSell, 
									stockPrice.DataGetter.StockDataField.adjclose, thisDateStr, "yyyyMMdd");
							ArrayList<Double> priceToSell = new ArrayList<Double>();
							for(int j = 0; j < priceToSellStr.size(); j++) {
								priceToSell .add(Double.parseDouble(priceToSellStr.get(j)));
							}
							
							// sell first
							portfolio.sellStocks(stocksToSell, priceToSell, amtToSell, thisCal);
							
							// update buy price
							ArrayList<String> priceToBuyStr = stockPrice.DataGetter.getStockDataField(stocksToBuy, 
									stockPrice.DataGetter.StockDataField.adjclose, thisDateStr, "yyyyMMdd");
							ArrayList<Double> priceToBuy = new ArrayList<Double>();
							for(int j = 0; j < priceToBuyStr.size(); j++) {
								priceToBuy .add(Double.parseDouble(priceToBuyStr.get(j)));
							}
							
							// buy amount
							Double cashRemained = portfolio.cashRemained;
							Double equalValue = cashRemained / stocksToBuy.size();
							
							ArrayList<Double> amtToBuy = new ArrayList<Double>();
							for(int j = 0; j < stocksToBuy.size(); j++) {
								amtToBuy.add(Math.floor(equalValue / (priceToBuy.get(j) * (1 + portfolio.tradingCost))));
							}
							
							portfolio.buyStocks(thisAllStocks, priceToBuy, amtToBuy, thisCal);
							}
							
							if(true) {
								// ==== sell previous stocks first =======
								ArrayList<String> priceToSellStr = stockPrice.DataGetter.getStockDataField(prevAllStocks, 
										stockPrice.DataGetter.StockDataField.adjclose, thisDateStr, "yyyyMMdd");
								ArrayList<Double> amtArrToSell = new ArrayList<Double>();
								ArrayList<Double> priceArrToSell = new ArrayList<Double>();
								for(int j = 0; j < priceToSellStr.size(); j++) {
									priceArrToSell .add(Double.parseDouble(priceToSellStr.get(j)));
									amtArrToSell.add(0.0);  // amt=0 represents that to sell all holding stocks
								}
								portfolio.sellStocks(prevAllStocks, priceArrToSell, amtArrToSell, thisCal);
								
								// ======== then buy new stocks ========
								// update price
								ArrayList<String> priceToBuyStr = stockPrice.DataGetter.getStockDataField(thisAllStocks, 
										stockPrice.DataGetter.StockDataField.adjclose, thisDateStr, "yyyyMMdd");
								ArrayList<Double> priceArrToBuy = new ArrayList<Double>();
								
								// update amt to buy
								Double cashRemained = portfolio.cashRemained*0.99;
								Double equalValue = cashRemained / thisAllStocks.size();
								ArrayList<Double> amtArrToBuy = new ArrayList<Double>();
								
								for(int j = 0; j < priceToBuyStr.size(); j++) {
									Double thisPrice = Double.parseDouble(priceToBuyStr.get(j));
									priceArrToBuy .add(thisPrice );
									amtArrToBuy.add(equalValue/(thisPrice * (1 + tradingCost)));  // amt=0 represents that to sell all holding stocks
								}
								portfolio.buyStocks(thisAllStocks, priceArrToBuy, amtArrToBuy, thisCal);
								System.out.println("After buying - Cash Remained = " + portfolio.cashRemained);
								
							}
						}
						
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
