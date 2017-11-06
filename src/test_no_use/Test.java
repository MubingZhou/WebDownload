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
import java.net.URL;
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
		try {
			Map<String, Map<Date,Double>> sbDataMap = new HashMap<String, Map<Date,Double>>();
			
			SimpleDateFormat sdf = new SimpleDateFormat ("yyyy-MM-dd");
			String sbDataPath = "D:\\stock data\\HK CCASS - WEBB SITE\\southbound\\combined";
            File allFile = new File(sbDataPath);
            for(File f : allFile.listFiles()) {
            	String fileName = f.getName();  // "2017-07-21.csv"
            	String filePath = sbDataPath + "\\" + fileName	;
            	String dateStr = fileName.substring(0, fileName.length() - 4); // "2017-07-21"   
            	Date date = sdf.parse(dateStr);
            	System.out.println(dateStr);
            	
            	BufferedReader bf = utils.Utils.readFile_returnBufferedReader(filePath);
            	String line ="";
            	int count  = 0;
            	while((line = bf.readLine()) != null) {
            		if(count  == 0) {
            			count ++;
            			continue;
            		}
            		String[] lineArr = line.split(",");
            		String stock = lineArr[0];
            		String holding = lineArr[2];
            		Double holdingD = Double.parseDouble(holding);
            		
            		Map<Date,Double> stockData = sbDataMap.get(stock);
            		if(stockData == null)
            			stockData = new HashMap<Date,Double>(); 
            		stockData.put(date, holdingD);
            		
            		sbDataMap.put(stock, stockData);
            		count++;
            	} // end of while
            } // end of file for
			
           System.out.println("123");
           for(int i = 0; i < 100; i++) {
        	   System.out.println(sbDataMap.get("700").get(sdf.parseObject("2017-11-02")));
           }
           
		}catch(Exception e) {
			e.printStackTrace();
		}
		
	}
	
	
	private static void test1(int i, int[] arr) {
		arr[0] += i;
	}
}

