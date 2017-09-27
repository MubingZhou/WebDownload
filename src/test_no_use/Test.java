package test_no_use;

import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.log4j.LogManager;

import backtesting.backtesting.Order;
import backtesting.backtesting.OrderType;
import backtesting.portfolio.Portfolio;
import strategy.db_southboundFlowPortfolio.PortfolioScreening;

@SuppressWarnings("unused")
public class Test {

	 //static Logger logger = LogManager.getLogger(Test.class.getName());
	 static Logger logger = Logger.getLogger(Test.class.getName());
	 
			
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			
			// initializing avat
			String dateFormat = "yyyyMMdd HH:mm:ss";
			SimpleDateFormat sdf = new SimpleDateFormat (dateFormat); 
			ArrayList<Date> timeArr = new ArrayList<Date>();
			ArrayList<Double> volArr = new ArrayList<Double>();
			ArrayList<Double> priceArr = new ArrayList<Double>();
			
			SimpleDateFormat sdf2 = new SimpleDateFormat("yyyyMMdd");
			String todayDate = sdf2.format(new Date());
			
		
			FileWriter fw_tmp = new FileWriter("D:\\test.csv");
			
			Long _1min = (long) (1000 * 60);
			// first one
			Date start1 = sdf.parse(todayDate + " 09:31:00"	); 
			fw_tmp.write(todayDate + " 09:31:00\n" );
			timeArr.add(start1);
			volArr.add(0.0);
			priceArr.add(0.0);
			
			Date break1 = sdf.parse(todayDate + " 12:00:01");
			Date start2 = sdf.parse(todayDate + " 13:00:59");
			Date break2 = sdf.parse(todayDate + " 16:10:01");
			
			Date nextTime = new Date(start1.getTime() + _1min);
			while(nextTime.before(break2)) {
				if(nextTime.before(break1) || nextTime.after(start2)) { // trading hours
					timeArr.add(nextTime);
					volArr.add(0.0);
					priceArr.add(0.0);
					fw_tmp.write(sdf.format(nextTime) + "\n");
				}
				nextTime = new Date(nextTime.getTime() + _1min);
				
			}
			fw_tmp.close();
			
		}catch(Exception e) {
			e.printStackTrace();
		}
		
	}

}
