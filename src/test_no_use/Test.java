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

@SuppressWarnings("unused")
public class Test {

	 //static Logger logger = LogManager.getLogger(Test.class.getName());
	 static Logger logger = Logger.getLogger(Test.class.getName());
	 
			
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			
			Map<String, String> m = new HashMap();
			m.put("1", "a");
			
			System.out.println(m.get("2"));
		}catch(Exception e) {
			e.printStackTrace();
		}
		
	}

}
