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
			ArrayList<Object> data = AvatUtils.getIndustry();
			Map<String, String> avatIndustry = (Map<String, String>) data.get(0);
			Map<String, ArrayList<String>> avatIndustry_byIndustry = (Map<String, ArrayList<String>>) data.get(1);  // industry - stock list
			
			FileWriter fw1 = new FileWriter("D:\\test1.csv");
			FileWriter fw2 = new FileWriter("D:\\test2.csv");
			
			for(String stock : avatIndustry.keySet()) {
				fw1.write(stock + "," + avatIndustry.get(stock) + "\n");
			}
			fw1.close();
			
			for(String industry : avatIndustry_byIndustry.keySet()) {
				ArrayList<String> memb = avatIndustry_byIndustry.get(industry);
				fw2.write(industry);
				for(int i = 0; i < memb.size(); i++) {
					String stock = memb.get(i);
					fw2.write("," + stock);
				}
				fw2.write("\n");
			}
			fw2.close();
			
		}catch(Exception e) {
			e.printStackTrace();
		}
		
	}

}
