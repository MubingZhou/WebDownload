package utils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import backtesting.backtesting.Portfolio;
import backtesting.backtesting.PortfolioOneDaySnapshot;

public class TestXML {

	public static void main(String[] args) {
		// see http://blog.csdn.net/sd4015700/article/details/39474893
		
		Portfolio p = new Portfolio(100.0);
		Double b = 0.0;
		
		/*
		//XMLUtil.convertToXml(p, "D:\\test.xml");
		Portfolio p2 = (Portfolio) XMLUtil.convertXmlFileToObject(Portfolio.class,"D:\\stock data\\southbound flow strategy - db\\20170905 160433\\portfolio.xml");
		System.out.println(p2.cashRemained);
		Map<Calendar, ArrayList<Object>> histSnap = p2.histSnap;
		Set<Calendar> keys = histSnap.keySet();
		ArrayList<Calendar> keysArr = new ArrayList<Calendar>(keys); 
		
		ArrayList<Object> ob1 = (ArrayList<Object>) histSnap.get(keysArr.get(0));
		//System.out.println(keysArr.get(0).getTime());
		//System.out.println(ob1.get(0));
		*/
		PortfolioOneDaySnapshot pos = new PortfolioOneDaySnapshot();
		pos.cashRemained = 10.0;
		pos.marketValue = 11.0;
		
		Map<String, Double> stockHeld = new HashMap();
		stockHeld.put("1", 100.0);
		stockHeld.put("2", 200.0);
		pos.stockHeld = stockHeld;
		
		XMLUtil.convertToXml(pos, "D:\\test.xml");
		PortfolioOneDaySnapshot pos2 = (PortfolioOneDaySnapshot) XMLUtil.convertXmlFileToObject(PortfolioOneDaySnapshot.class,"D:\\test.xml");
		System.out.println(pos2.stockHeld.get("1"));
		
	}

}
