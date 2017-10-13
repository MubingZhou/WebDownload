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

import org.apache.log4j.LogManager;

import backtesting.backtesting.Order;
import backtesting.backtesting.OrderType;
import backtesting.portfolio.Portfolio;
import cgi.ib.avat.AvatRecordSingleStock;
import cgi.ib.avat.AvatUtils;
import cgi.ib.avat.HoldingRecord;
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
			
			ArrayList<Double> arr = new ArrayList<Double> ();
			arr.add(10.0);
			arr.add(12.0);
			AA aa = new AA();
			Map<String, Double> map = new HashMap();
			map.put("ass", 123.0);
			
			out.writeObject(map);
			
			FileInputStream fis = new FileInputStream(p);
			ObjectInputStream in = new ObjectInputStream(fis);
			Map<String, Double> a = (Map<String, Double>) in.readObject();
			
			System.out.print(a.get("ass"));
			
			File fff = new File(p + "a");
			if(!fff.exists())
				System.out.print("adfsfds");
			
		}catch(Exception e) {
			e.printStackTrace();
		}
		
	}
	
	

}

