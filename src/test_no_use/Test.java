package test_no_use;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import backtesting.backtesting.Order;
import backtesting.backtesting.OrderType;
import backtesting.portfolio.Portfolio;

public class Test {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			
			
			ArrayList<String> list = utils.Utils.getSouthboundStocks("20170206", "yyyyMMdd", true, true);
			if(list.indexOf("3699") != -1) // 3699 exists
				System.out.println("dssdfsfsdfsdf " + list.get(list.indexOf("3699")));
			
			if(false)
			for(String l : list)
				System.out.println(l);
			
			
		}catch(Exception e) {
			e.printStackTrace();
		}
		
	}

}
