package test_no_use;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import backtesting.backtesting.Portfolio;

public class Test {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			SimpleDateFormat sdf = new SimpleDateFormat ("yyyyMMdd");
			Calendar c1 = Calendar.getInstance();
			
			String s = "D:\\test\\";
			if(!s.substring(s.length() - 1).equals("\\")) {
				System.out.println(s + "\\");
			}
			
			Portfolio p = new Portfolio(100);
			
		}catch(Exception e) {
			e.printStackTrace();
		}
		
	}

}
