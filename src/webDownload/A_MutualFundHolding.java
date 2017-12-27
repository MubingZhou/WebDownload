package webDownload;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import webDownLoadHKEX.Utils;

public class A_MutualFundHolding {
	public static Logger logger = Logger.getLogger(A_MutualFundHolding.class);
	public static String CSV_OUTPUT_PATH = utils.PathConifiguration.STOCK_DATA_ROOT_PATH + "\\A share data\\mutual fund\\holding\\csv";
	public static String HTML_OUTPUT_PATH = utils.PathConifiguration.STOCK_DATA_ROOT_PATH + "\\A share data\\mutual fund\\holding\\html";
	public static String MUTUAL_FUND_LIST_PATH = "Z:\\Mubing\\stock data\\A share data\\mutual fund\\mutual fund list.csv";
			// URL example: http://jingzhi.funds.hexun.com/DataBase/cgmx.aspx?fundcode=001274
	public static String ROOT_URL = "http://jingzhi.funds.hexun.com/DataBase/cgmx.aspx?fundcode=";
	
	public static StringBuilder params = new StringBuilder();
	public static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd"); 
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		downloadHTML("");
	}
	
	public static void downloadHTML(String fundListPath) {
		try {
			// first to get mutual fund list and their codes
			ArrayList<String> FUND_LIST = new ArrayList<String>();  // code list
			FUND_LIST.add("001274");
			//ArrayList<String> FUND_NAME_LIST = new ArrayList<String>();
			
			// download html
			ArrayList<String> tempFailedList = new ArrayList<String>();
            ArrayList<String> failedList = new ArrayList<String>();
            failedList.addAll(FUND_LIST);
            while(failedList != null && failedList.size() > 0) {
            	for(int j = 0; j < failedList.size(); j++) {
            		String fundCode = failedList.get(j);
            		String url = ROOT_URL + fundCode;
            		// download....
            		try {
						URL realUrl = new URL(url); 
						HttpURLConnection connection = (HttpURLConnection) realUrl.openConnection();
						connection.setRequestProperty("User-Agent", "Mozilla/31.0 (compatible; MSIE 10.0; Windows NT; DigExt)");
						
						if(HttpURLConnection.HTTP_OK != connection.getResponseCode()){ // get connection
							System.out.println(fundCode + " - Not connected! ");
							continue;
						}  
						InputStream inputStream = connection.getInputStream();
						
						// output file path
						String outputFilePath_sh = HTML_OUTPUT_PATH + "\\" + fundCode + ".html";
						
						//store html
						utils.Utils.storeHTML(inputStream, outputFilePath_sh,"GB2312");
			            
			            logger.info("    ------------ Download Done -----------");
			            
			            // read the file and get relevant dates
			            
			            
					}catch(Exception e) {
	            		e.printStackTrace();
	            		logger.info("    ------------ Download Failed -----------");
	            		tempFailedList.add(fundCode);
	            	}
            	}
            	failedList.clear();
            	failedList.addAll(tempFailedList);
            	tempFailedList.clear();
            }  // end of while
			
		}catch(Exception e) {
			e.printStackTrace();
		}
		
	}
	
	// --------------- utility functions ------------
		private static void setParams(String name, String value) throws Exception{
			if(params.length() != 0) { // not first value
				params.append('&');
			}
			
			params.append(URLEncoder.encode(name, "UTF-8"));
			params.append('=');
			params.append(URLEncoder.encode(value, "UTF-8"));
		}
		
		private static String getCookie(String url) throws IOException {
			URL website = new URL(url);
			URLConnection connection = website.openConnection();
			
			String cookie = connection.	getHeaderField("Set-Cookie");
	        return cookie;
		}

}