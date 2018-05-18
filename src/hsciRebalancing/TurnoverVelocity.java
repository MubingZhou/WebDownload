package hsciRebalancing;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import math.MyMath;
import utils.PathConifiguration;

public class TurnoverVelocity {
	public static Logger logger = Logger.getLogger(TurnoverVelocity.class);
	public static String STOCK_PRICE_PATH = PathConifiguration.STOCK_PRICE_PATH;

	public static void main(String[] args) {
		try {
			//String stockListPath = "T:\\Mubing\\HSCI Methodology\\201712 rebal\\stocks after MV filter.csv";
			String stockListPath = "Z:\\Mubing\\HSCI Methodology\\201712 rebal\\stocks after MV filter.csv";
			BufferedReader bf = utils.Utils.readFile_returnBufferedReader(stockListPath);
			String line = bf.readLine();
			ArrayList<String> stockList = new ArrayList(Arrays.asList(line.split(",")));
			
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			ArrayList<Date> monthList = new ArrayList<Date>();
//			monthList.add(sdf.parse("2016-07-01"));
//			monthList.add(sdf.parse("2016-08-01"));
//			monthList.add(sdf.parse("2016-09-01"));
//			monthList.add(sdf.parse("2016-10-01"));
//			monthList.add(sdf.parse("2016-11-01"));
//			monthList.add(sdf.parse("2016-12-01"));
//			monthList.add(sdf.parse("2017-01-01"));
//			monthList.add(sdf.parse("2017-02-01"));
//			monthList.add(sdf.parse("2017-03-01"));
//			monthList.add(sdf.parse("2017-04-01"));
//			monthList.add(sdf.parse("2017-05-01"));
//			monthList.add(sdf.parse("2017-06-01"));
			monthList.add(sdf.parse("2017-07-01"));
			monthList.add(sdf.parse("2017-08-01"));
			monthList.add(sdf.parse("2017-09-01"));
			monthList.add(sdf.parse("2017-10-01"));
			monthList.add(sdf.parse("2017-11-01"));
			monthList.add(sdf.parse("2017-12-01"));
			monthList.add(sdf.parse("2018-01-01"));
			monthList.add(sdf.parse("2018-02-01"));
			monthList.add(sdf.parse("2018-03-01"));
			monthList.add(sdf.parse("2018-04-01"));
//			monthList.add(sdf.parse("2018-05-01"));
//			monthList.add(sdf.parse("2018-06-01"));
			for(int i = 1; i <= -11; i++) {   // no use now
				String m = "";
				if( i <= 9) {
					m += "0";
				}
				m += String.valueOf(i);
				
				String dStr = "2017-" + m + "-01";
				//logger.info(dStr);
				monthList.add(sdf.parse(dStr));
			}
			
			String outputPath = "Z:\\Mubing\\HSCI Methodology\\201806 rebal\\stocks monthly volume median 201806.csv";
			getVolumeMedian(stockList, monthList,outputPath);
			
			
		}catch(Exception e) {
			
		}
	}
	
	public static Map<String, Map<Date, Double>> getVolumeMedian(ArrayList<String> stockList, ArrayList<Date> monthList, String outputFilePath) {
		Map<String, Map<Date, Double>> turnoverVolumeMap = new HashMap<String, Map<Date, Double>>();  
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat sdf_month = new SimpleDateFormat("yyyy-MM");
		
		try {
			Collections.sort(monthList);   //从前到后进行sort
			
			// write header
			FileWriter fw = new FileWriter(outputFilePath);
			for(Date d : monthList) {
				String dStr = sdf_month.format(d);
				fw.write("," + dStr);
			}
			fw.write("\n");
			
			ArrayList<Date> dateArr0 = getMonthStartEnd(monthList.get(0));
			Date	firstMonthStart = dateArr0.get(0);
			logger.info("  firstMonthStart=" + sdf.format(firstMonthStart));
			
			ArrayList<ArrayList<Double>> thisStockVolData_byMonth = new ArrayList<ArrayList<Double>>(); 
			for(int j = 0; j < monthList.size(); j++) {
				thisStockVolData_byMonth.add(new ArrayList<Double>());
			}
			
			for(int i = 0; i < stockList.size(); i++) {
				String stock = stockList.get(i); 
				logger.info("i=" + i + " stock=" + stock);
				String stockPath = PathConifiguration.getStockPriceDataPath(stock);
				//ArrayList<Double> thisMonthVolumeArr = new ArrayList<Double>(); 
				
				BufferedReader bf = utils.Utils.readFile_returnBufferedReader(stockPath);   //读的是david webb上面的data
				if(bf == null) {
					logger.info(" -- stock data not found, skip");
					continue;
				}
				
				
				String line = "";
				int c = 0;
				while((line = bf.readLine()) != null) {
					if(c == 0) {
						c++;
						continue;
					}
					
					String[] lineArr = line.split(",");
					String volStr = lineArr[8];
					String dateStr = lineArr[0];
					String suspStr = lineArr[2];
					
					if(suspStr.equals("1"))
						continue;
					
					Date date = sdf.parse(dateStr);
					ArrayList<Date> dateArr = getMonthStartEnd(date);
					Date mStart = dateArr.get(0);
					
					int ind = monthList.indexOf(mStart);
					if(ind > -1) {
						ArrayList<Double> volData = thisStockVolData_byMonth.get(ind);
						volData.add(Double.parseDouble(volStr));
						thisStockVolData_byMonth.set(ind, volData);
					}
					if(date.before(firstMonthStart))
						break;
					
				}
				bf.close();
				bf = null;
				
				//calculate the median volume for each month
				fw.write(stock);
				for(int j = 0; j < thisStockVolData_byMonth.size(); j++) {
					ArrayList<Double> volData = thisStockVolData_byMonth.get(j);
					//Collections.sort(volData);
					double median = -1.0;
					if(volData != null && volData.size() > 0)
						median = MyMath.median(volData);
					fw.write("," + median);
					
					Map<Date, Double> thisStockVolMed = turnoverVolumeMap.get(stock);
					if(thisStockVolMed == null)
						thisStockVolMed = new HashMap<Date, Double>();
					thisStockVolMed.put(monthList.get(j), median);
					
					turnoverVolumeMap.put(stock, thisStockVolMed);
					
				}
				fw.write("\n");
				if(Math.floorMod(i, 50) == 0) {
					fw.flush();
					logger.info("    i=" + i  +" flush");
				}
					
				//fw.flush();
				for(int j = 0; j < thisStockVolData_byMonth.size(); j++	) {
					thisStockVolData_byMonth.get(j).clear();
				}
				
			}   // END of: for(String stock : stockList) {
			
			fw.close();
			
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		return turnoverVolumeMap;
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