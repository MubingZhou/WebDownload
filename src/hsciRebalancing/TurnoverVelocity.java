package hsciRebalancing;

import java.io.BufferedReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import math.MyMath;
import utils.PathConifiguration;

public class TurnoverVelocity {
	public static String STOCK_PRICE_PATH = PathConifiguration.STOCK_PRICE_PATH;

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
	
	public static void getVolumeMedian(ArrayList<String> stockList, ArrayList<Date> monthList) {
		try {
			Collections.sort(monthList);   //从前到后进行sort
			
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			
			Map<String, Map<Date, Double>> turnoverVolumeMap = new HashMap<String, Map<Date, Double>>();    
			
			for(String stock : stockList) {
				String stockPath = PathConifiguration.getStockPriceDataPath(stock);
				ArrayList<Double> thisMonthVolumeArr = new ArrayList<Double>(); 
				
				ArrayList<Date> dateArr0 = getMonthStartEnd(monthList.get(0));
				Date	firstMonthStart = dateArr0.get(0);
				//Date thisMonthEnd = dateArr.get(1);
				
				ArrayList<ArrayList<Double>> thisStockVolData_byMonth = new ArrayList<ArrayList<Double>>(); 
				for(int j = 0; j < monthList.size(); j++) {
					thisStockVolData_byMonth.add(new ArrayList<Double>());
				}
				
				BufferedReader bf = utils.Utils.readFile_returnBufferedReader(stockPath);   //读的是david webb上面的data
				String line = "";
				int c = 0;
				while((line = bf.readLine()) != null) {
					if(c == 0) {
						c++;
						continue;
					}
					
					String[] lineArr = line.split(",");
					String turnStr = lineArr[0];
					String dateStr = lineArr[0];
					
					Date date = sdf.parse(dateStr);
					ArrayList<Date> dateArr = getMonthStartEnd(date);
					Date mStart = dateArr.get(0);
					
					int ind = monthList.indexOf(mStart);
					if(ind > -1) {
						ArrayList<Double> turnData = thisStockVolData_byMonth.get(ind);
						turnData.add(Double.parseDouble(turnStr));
						thisStockVolData_byMonth.set(ind, turnData);
					}
					if(date.before(firstMonthStart))
						break;
					
				}
				bf.close();
				
				//calculate the median volume for each month
				for(int j = 0; j < thisStockVolData_byMonth.size(); j++) {
					ArrayList<Double> volData = thisStockVolData_byMonth.get(j);
					Collections.sort(volData);
					double median = MyMath.median(volData);
					
					Map<Date, Double> thisStockVolMed = turnoverVolumeMap.get(stock);
					if(thisStockVolMed == null)
						thisStockVolMed = new HashMap<Date, Double>();
					thisStockVolMed.put(monthList.get(j), median);
					
					turnoverVolumeMap.put(stock, thisStockVolMed);
					
				}
			}
			
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	
	public static ArrayList<Date> getMonthStartEnd(Date d){
		ArrayList<Date> drr = new ArrayList<Date>();
		
		Calendar thisMonthStartCal = Calendar.getInstance();
		thisMonthStartCal.setTime(d);
		thisMonthStartCal.set(Calendar.DATE, 1);
		drr.add(thisMonthStartCal.getTime());
		
		thisMonthStartCal.add(Calendar.MONTH, 1);   // next month
		thisMonthStartCal.add(Calendar.DATE, -1);
		drr.add(thisMonthStartCal.getTime());
		
		return drr;
	}
	
}
