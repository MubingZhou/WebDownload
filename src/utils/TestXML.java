package utils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import backtesting.backtesting.Order;
import backtesting.backtesting.Trade;
import backtesting.backtesting.TradeType;
import backtesting.portfolio.Portfolio;
import backtesting.portfolio.PortfolioOneDaySnapshot;

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
		
		/*
		Trade tr = new Trade();
		tr.amount = 10.0;
		tr.stock = "sdfs"	;
		tr.type = TradeType.SHORT;
		
		
		String portFilePath = "D:\\stock data\\southbound flow strategy - db\\20170907 114703\\portfolio.xml";
		Portfolio pf = (Portfolio) XMLUtil.convertXmlFileToObject(Portfolio.class,portFilePath);
		Map<Calendar, PortfolioOneDaySnapshot> histSnap = pf.histSnap;
		
		ArrayList<Calendar> allDays = new ArrayList<Calendar>(histSnap.keySet());
		Collections.sort(allDays);
		System.out.println(allDays.get(0).getTime());
		*/
		
		Map<String, String> m = new HashMap();
		m.put("1", "aaa");
		m.put("2", "bbb");
		XMLUtil.convertToXml(m,"D:\\text.xml");
		
		
	}

}
