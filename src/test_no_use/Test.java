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
			
			String p = "Z:\\AVAT\\orders\\20171017\\holdingRecords.javaobj";
			//String p = "D:\\test.javaobj";
			
			/*
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
			hh.put("test1", hhh);
			out.close();
			fos.close();
			
			
			FileOutputStream fos2 = new FileOutputStream(p);
			ObjectOutputStream out2 = new ObjectOutputStream(fos2);
			out2.writeObject(hh);
			
			
			FileInputStream fis = new FileInputStream(p);
			ObjectInputStream in = new ObjectInputStream(fis);
			//Map<String, Map<Integer, HoldingRecord>>   a = (HashMap<String, Map<Integer, HoldingRecord>>  ) utils.Utils.readObject(p);
			Map<String, Map<Integer, HoldingRecord>> a = (Map<String, Map<Integer, HoldingRecord>>) utils.Utils.readObject(p);
			
			System.out.print(a.get("493"));
			
			in.close();
			fis.close();
			*/
			
			Thread t = new Thread(new Runnable(){
				   public void run(){
					   try {
							Thread.sleep(1000 * 3);
							System.out.println("2");
						}catch(Exception e) {
							logger.error("           Can't log holding records!");
						}
				   }
				});
			t.start();
			System.out.println("1");
			
		}catch(Exception e) {
			e.printStackTrace();
		}
		
	}
	
	

}

