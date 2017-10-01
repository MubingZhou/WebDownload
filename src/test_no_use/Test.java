package test_no_use;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.ib.client.Contract;

import org.apache.log4j.LogManager;

import backtesting.backtesting.Order;
import backtesting.backtesting.OrderType;
import backtesting.portfolio.Portfolio;
import cgi.ib.avat.AvatRecordSingleStock;
import cgi.ib.avat.AvatUtils;
import strategy.db_southboundFlowPortfolio.PortfolioScreening;

@SuppressWarnings("unused")
public class Test {

	 //static Logger logger = LogManager.getLogger(Test.class.getName());
	 static Logger logger = Logger.getLogger(Test.class.getName());
	 
			
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			/*
			ArrayList<AvatRecordSingleStock> list = new ArrayList<AvatRecordSingleStock>();
			AvatRecordSingleStock a1 = new AvatRecordSingleStock("100", 12.5, 1.0, 10.0, 3.0, "dfs");
			AvatRecordSingleStock a2 = new AvatRecordSingleStock("100", 12.5, 1.0, 8.0, 3.0, "dfs");
			AvatRecordSingleStock a3 = new AvatRecordSingleStock("100", 12.5, 1.0, 11.0, 3.0, "dfs");
			AvatRecordSingleStock a4 = new AvatRecordSingleStock("100", 12.5, 1.0, 1.0, 3.0, "dfs");
			
			list.add(a1);
			list.add(a2);
			list.add(a3);
			list.add(a4);
			
			Collections.sort(list, AvatRecordSingleStock.getComparator());
			
			System.out.println(list.get(0).avatRatio5D);*/
			
			ArrayList<Contract> conArr = new ArrayList<Contract> ();
			ArrayList<String> stockList = new ArrayList<String>();
			//ArrayList<String> industryList = new ArrayList<String>();
			
			File alreadyExited = new File("D:\\stock data\\IB\\historical data");
			String[] aListTemp = alreadyExited.list();
			ArrayList<String> aList = new ArrayList<String>(Arrays.asList(aListTemp));  // excluding those already existed in historical data
			aList = new ArrayList<String>();  
			
			BufferedReader bf = utils.Utils.readFile_returnBufferedReader("D:\\stock data\\IB\\stocklist.csv");
			stockList.addAll(Arrays.asList(bf.readLine().split(",")));
			for(int i = 0; i < stockList.size(); i ++) {
				String symbol = stockList.get(i);
				if(aList.indexOf(symbol + ".csv") != -1)
					continue;
				
				Contract con1 = new Contract();
				con1.symbol(stockList.get(i));
				con1.exchange("SEHK");
				con1.secType("STK");
				con1.currency("HKD");
				
				conArr.add(con1);
			}
			//industryList.addAll(Arrays.asList(bf.readLine().split(",")));
			bf.close();
			
			AvatUtils.preparePrevCrossSectionalAvat(new ArrayList<Contract>(conArr.subList(0, 3)));
			
		}catch(Exception e) {
			e.printStackTrace();
		}
		
	}

}
