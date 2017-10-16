package test_no_use;

import java.awt.Toolkit;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
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

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.apache.log4j.Logger;

import com.ib.client.Contract;
import com.ib.client.Order;

import org.apache.log4j.LogManager;

import backtesting.backtesting.OrderType;
import backtesting.portfolio.Portfolio;
import cgi.ib.avat.AvatRecordSingleStock;
import cgi.ib.avat.AvatUtils;
import cgi.ib.avat.HoldingRecord;
import cgi.ib.avat.MyIOrderHandler;
import strategy.db_southboundFlowPortfolio.PortfolioScreening;

@SuppressWarnings("unused")
public class Test {

	 //static Logger logger = LogManager.getLogger(Test.class.getName());
	 static Logger logger = Logger.getLogger(Test.class.getName());
	 static int i = 0;
			
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			
			String p = "D:\\testV.javaobj";
			FileOutputStream fos = new FileOutputStream(p);
			ObjectOutputStream out = new ObjectOutputStream(fos);
			
			
			Map<String, Map<Date,ArrayList<Double>>> hh = new HashMap();
			Map<Date,ArrayList<Double>> hhh = new HashMap();
			ArrayList<Double> hhhh = new ArrayList<Double>();
			hhhh.add(1.0);
			hhhh.add(2.0);
			hhh.put(new Date(), hhhh);
			hh.put("test", hhh);
			
			out.writeObject(hh);
			
			FileInputStream fis = new FileInputStream(p);
			ObjectInputStream in = new ObjectInputStream(fis);
			Map<String, Map<Date,ArrayList<Double>>>  a = (Map<String, Map<Date,ArrayList<Double>>> ) in.readObject();
			
			System.out.print(a.get("test"));
			
			
			
		}catch(Exception e) {
			e.printStackTrace();
		}
		
	}
	
	

}

