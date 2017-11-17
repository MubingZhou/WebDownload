package test_no_use;

import java.applet.Applet;
import java.applet.AudioClip;
import java.awt.Toolkit;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.math.BigDecimal;
import java.net.URL;
import java.text.DecimalFormat;
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

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
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
import utils.PlayWAV;

@SuppressWarnings("unused")
public class Test {

	 //static Logger logger = LogManager.getLogger(Test.class.getName());
	 static Logger logger = Logger.getLogger(Test.class.getName());
	 static int i = 0;
			
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd"); 
		try {			
			
			Portfolio pf = new Portfolio(1000000.0);
			
			backtesting.backtesting.Order s1 = new backtesting.backtesting.Order();
			s1.type = OrderType.SHORT;
			Calendar cal1 = Calendar.getInstance();
			cal1.setTime(sdf.parse("20171115"));
			s1.date = (Calendar) cal1.clone();
			s1.price = 10.;
			s1.stock  = "1"	;
			s1.amount = -100.0;
			s1.orderId = 1;
			
			backtesting.backtesting.Order s2 = new backtesting.backtesting.Order();
			s2 = (backtesting.backtesting.Order) s1.clone();
			cal1.setTime(sdf.parse("20171116"));
			s2.date = (Calendar) cal1.clone();
			s2.price = 8.0;
			s2.orderId = 2;
			
			backtesting.backtesting.Order s3 = new backtesting.backtesting.Order();
			s3 = (backtesting.backtesting.Order) s1.clone();
			s3.type = OrderType.COVER;
			cal1.setTime(sdf.parse("20171117"));
			s3.date = (Calendar) cal1.clone();
			s3.price = 7.0;
			s3.amount = 100.0;
			s3.orderId = 3;
			
			backtesting.backtesting.Order s4 = new backtesting.backtesting.Order();
			s4 = (backtesting.backtesting.Order) s3.clone();
			cal1.setTime(sdf.parse("20171120"));
			s4.date = (Calendar) cal1.clone();
			s4.price = 7.0;
			s4.amount = 100.0;
			s4.orderId = 4;
			
			ArrayList<backtesting.backtesting.Order> soArr = new ArrayList<backtesting.backtesting.Order>();
			soArr.add(s1);
			soArr.add(s2);
			//ArrayList<backtesting.backtesting.Order> coArr = new ArrayList<backtesting.backtesting.Order>();
			soArr.add(s3);
			soArr.add(s4);
			
			pf.executeOrders(soArr);
			pf.commitDayEndValue();
			
			/*
			ArrayList<String> thisBuyStocks = new ArrayList<String>();
			//Thread.sleep(100000000000000l);
			thisBuyStocks.add("1898");
			thisBuyStocks.add("1928");
			thisBuyStocks.add("700");
			
			if(thisBuyStocks.size() > 0) {
				   for(int i = 0; i < 2; i++) {
					   for(int j = 0; j < thisBuyStocks.size(); j++) {
						   String stock = thisBuyStocks.get(j);
						   char[] c = stock.toCharArray();
							for(int k = 0; k < c.length; k++) {
								switch(c[k]) {
								case '1':
									PlayWAV.play("1.wav");
									break;
								case '2':
									PlayWAV.play("2.wav");
									break;
								case '3':
									PlayWAV.play("3.wav");
									break;
								case '4':
									PlayWAV.play("4.wav");
									break;
								case '5':
									PlayWAV.play("5.wav");
									break;
								case '6':
									PlayWAV.play("6.wav");
									break;
								case '7':
									PlayWAV.play("7.wav");
									break;
								case '8':
									PlayWAV.play("8.wav");
									break;
								case '9':
									PlayWAV.play("9.wav");
									break;
								case '0':
									PlayWAV.play("0.wav");
									break;
								default:
									break;
								}
							}
							
							Thread.sleep(300);
							if(false && j < thisBuyStocks.size()-1) {
								PlayWAV.play("tungLF.wav",2);
								PlayWAV.play("maiLF.wav",2);
							}
							//Thread.sleep(1);
							
					   }
					   
					   if(i == 0) {
						   PlayWAV.play("chungLT.wav",2);
							//PlayWAV.play("fukHT.wav",2);
							//PlayWAV.play("1.wav",2);
							//PlayWAV.play("chiMR.wav",2);
					   }
					   	
				   }
			   }
		*/
			
		}catch(Exception e) {
			e.printStackTrace();
		}
		
	}
	
	
	private static void test1(int i, int[] arr) {
		arr[0] += i;
	}
}

