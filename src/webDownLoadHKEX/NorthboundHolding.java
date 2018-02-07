package webDownLoadHKEX;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

//import webDownLoadHKEX.Utils;

public class NorthboundHolding {
	public static String OUTPUT_PATH = "Z:\\Mubing\\stock data\\A share data\\northbound holding\\sh sz data";
	public static String OUTPUT_PATH_COMBINE = "Z:\\Mubing\\stock data\\A share data\\northbound holding\\combined";
	public static String ASHARE_TRADING_DATE_FILE = "Z:\\Mubing\\stock data\\A share data\\all trading date a share.csv";
	
	public static String SH_HOLDING_URL = "http://www.hkexnews.hk/sdw/search/mutualmarket.aspx?t=sh";
	public static String SZ_HOLDING_URL = "http://www.hkexnews.hk/sdw/search/mutualmarket.aspx?t=sz";
	public static StringBuilder params = new StringBuilder();
	public static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd"); 
	
	private static Logger logger = Logger.getLogger(NorthboundHolding.class);
	
	public static void main(String[] args) {
		String dateStr = "20180205";
		downloader(dateStr,dateStr,"yyyyMMdd");
		combiner(dateStr,dateStr,"yyyyMMdd");
	}
	
	public static void downloader(Date startDate, Date endDate, boolean isSH, boolean isSZ) {
		try {
			String[] urlPath = {SH_HOLDING_URL, SZ_HOLDING_URL};
			boolean[] urlPathDownload = {isSH, isSZ};
			
			ArrayList<Calendar> allTrdCal = utils.Utils.getAllTradingCal(ASHARE_TRADING_DATE_FILE);
			int allTrdCal_size = allTrdCal.size();
			
			//SimpleDateFormat sdf = new SimpleDateFormat(Utils.DATE_FORMAT); 
            SimpleDateFormat sdf_year = new SimpleDateFormat("yyyy"); 
            SimpleDateFormat sdf_month = new SimpleDateFormat("MM"); 
            SimpleDateFormat sdf_day = new SimpleDateFormat("dd");
            
			final int size = urlPath.length;
			for(int i = 0; i < size; i++) {
				boolean isDownload = urlPathDownload[i];
				if(!isDownload)
					continue;
				
				String url = urlPath[i];
				
				String s = "";
	            if(i == 0) {
	            	s = "_sh";
	            	logger.debug("--------------- Download SH --------------");
	            }
	            if(i == 1) {
	            	s = "_sz";
	            	logger.debug("--------------- Download SZ --------------");
	            }
	            
	            // ------------- 每个日期都download --------------
	            ArrayList<Calendar> tempFailedList = new ArrayList<Calendar>();
	            ArrayList<Calendar> failedList = new ArrayList<Calendar>();
	            failedList.addAll(allTrdCal);
	            while(failedList != null && failedList.size() > 0) {
	            	for(int j = 0; j < allTrdCal_size; j++) {
						Date thisDate = allTrdCal.get(j).getTime();
						if(thisDate.before(startDate))
							continue;
						if(thisDate.after(endDate))
							continue;
						
						//logger.info("   date=" + sdf.format(thisDate));
						try {
							URL realUrl = new URL(url);
				            
				            //System.out.println("params = \n" + params);
				        	
				        	HttpURLConnection conn = (HttpURLConnection) realUrl.openConnection();
				            // set properties
				            conn.setRequestProperty("accept", "*/*");
				            conn.setRequestProperty("connection", "Keep-Alive");
				            conn.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
				            conn.setRequestMethod("POST");
				            conn.setReadTimeout(30 * 1000); // set timeout
				            
				            Document doc = Jsoup.connect("http://www.hkexnews.hk/sdw/search/mutualmarket.aspx?t=sz").get();	
				            setParams("__VIEWSTATE", doc.select("input#__VIEWSTATE").first().val());
				            setParams("__VIEWSTATEGENERATOR", doc.select("input#__VIEWSTATEGENERATOR").first().val());
				            setParams("__EVENTVALIDATION", doc.select("input#__EVENTVALIDATION").first().val());
				            
				            String day = sdf_day.format(thisDate);
				            String month = sdf_month.format(thisDate);
				            String year = sdf_year.format(thisDate);
							setParams("ddlShareholdingDay", day);
				            setParams("ddlShareholdingMonth", month);
				            setParams("ddlShareholdingYear", year);
				            setParams("btnSearch.y", "15");
				            setParams("btnSearch.x", "15");
				            logger.info("   date = " + year + "-" + month + "-" + day);
				            
				            conn.setRequestProperty( "Content-Length", Integer.toString( params.toString().getBytes("utf-8").length ));
				            conn.setRequestProperty("Cookie", getCookie("http://www.hkexnews.hk"));
				            
				            // for POST
				            conn.setDoOutput(true);
				            conn.setDoInput(true);
				            
				            // sent parameters
				            DataOutputStream dataOutputStream = new DataOutputStream( conn.getOutputStream()); 
				            dataOutputStream.write(params.toString().getBytes("utf-8"));
				            dataOutputStream.flush();
				            
				            //write out the response
				            Utils.writeFile(conn.getInputStream(), OUTPUT_PATH + "\\" + sdf.format(thisDate) + s + ".html");
				            
				            conn.disconnect();
				            
				            //params.delete(0, params.length());
				            params = null;
				            params = new StringBuilder();
				            
				            logger.info("    ------------ Done -----------");
						}catch(Exception e) {
		            		e.printStackTrace();
		            		logger.info("    ------------ Failed -----------");
		            		tempFailedList.add(allTrdCal.get(j));
		            	}
						
					} // end of for
	            	failedList = null;
	            	failedList = new ArrayList<Calendar>();
	            	failedList.addAll(tempFailedList);
	            	tempFailedList = null;
	            	tempFailedList = new ArrayList<Calendar>();	
	            	
	            }
	            
				
				
			}
			
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void downloader(String startDateStr, String endDateStr,String dateFormat) {
		try {
			downloader(startDateStr, endDateStr, dateFormat, true, true);
			
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	public static void downloader(String startDateStr, String endDateStr,String dateFormat, boolean isSH, boolean isSZ) {
		try {
			SimpleDateFormat sdf1 = new SimpleDateFormat(dateFormat);
			downloader(sdf1.parse(startDateStr), sdf1.parse(endDateStr), isSH, isSZ);
			
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void combiner(Date startDate, Date endDate) {
		try {
			//先读文件夹里面所有的
			File rootFile = new File(OUTPUT_PATH);
			ArrayList<String> fileNamesDate_sz = new ArrayList<String>();  
			ArrayList<String> fileNamesDate_sh = new ArrayList<String>();
			for(File f : rootFile.listFiles()) {
				String fName = f.getName();   // e.g. 2017-12-11_sh.html
				String suffix1 = fName.substring(fName.length() - 5, fName.length());
				if(suffix1.equals(".html")) {
					String suffix2 = fName.substring(fName.length() - 8, fName.length()-5);
					String fileDate = fName.substring(0,fName.length() - 8);
					if(suffix2.equals("_sh"))
						fileNamesDate_sh.add(fileDate);  // e.g. 2017-12-11
					if(suffix2.equals("_sz"))
						fileNamesDate_sz.add(fileDate);
				}
			}
			ArrayList<String> allFileNameDate = new ArrayList<String>();// 只存储sh和sz文件都全的日期
			for(String szFile : fileNamesDate_sz) {
				if(fileNamesDate_sh.indexOf(szFile) > -1)
					allFileNameDate.add(szFile);
			}
			
			// 开始读文件并且合并文件
			String[] suffixArr = {"_sh.html", "_sz.html"};
			for(String dateStr : allFileNameDate) {
				Date date = sdf.parse(dateStr);
				if(date.before(startDate))
					continue;
				if(date.after(endDate))
					continue;
				
				FileWriter fw = new FileWriter(OUTPUT_PATH_COMBINE + "\\" + dateStr + ".csv");
				logger.info("Writer: " + OUTPUT_PATH_COMBINE + "\\" + dateStr + ".csv");
				fw.write("code,name,shareholding,holding pct (as of total issued A shares)\n");
				for(int i = 0; i < suffixArr.length; i++) {  // sh & sz
					String suffix = suffixArr[i];
					
					String fileName = dateStr + suffix;
					
					// parse html
					Document doc = (Document) Jsoup.parse(new File(OUTPUT_PATH + "\\" + fileName), "utf-8", "");
					
					
					Elements resultTables = doc.getElementsByClass("result-table");  
					if(resultTables == null || resultTables.size() == 0) {
						continue;
					}
					logger.info("   Read: " + OUTPUT_PATH + "\\" + fileName);
					
					Element resultTable = resultTables.get(0);  // 肯定只有一个element
					Elements tr_rows = resultTable.getElementsByTag("tr");
					final int tr_size = tr_rows.size();
					for(int k = 2; k < tr_size; k++) {
						Elements td_cols = tr_rows.get(k).getElementsByTag("td");
						String code = td_cols.get(0).text();
						String name0 = td_cols.get(1).text().replace(",", "");
						String name  = new String(name0.getBytes(), "UTF-8"); 
						String shareholding = td_cols.get(2).text().replace(",", "");
						String pct = td_cols.get(3).text().replace("%", "");
						Double pctD = Double.parseDouble(pct);
						pct = String.valueOf(pctD/100);
						
						// transform code
						if(code.substring(0, 1).equals("9")) {
							code = "60" + code.substring(1);
						}
						if(code.substring(0, 2).equals("70")) {
							code = "000" + code.substring(2);
						}
						if(code.substring(0, 2).equals("72")) {
							code = "002" + code.substring(2);
						}
						if(code.substring(0, 2).equals("71")) {
							code = "001" + code.substring(2);
						}
						if(code.substring(0, 2).equals("77")) {
							code = "300" + code.substring(2);
						}
						
						fw.write(code + "," + name + "," + shareholding + "," + pct + "\n");
						
					}
				}
				fw.close();
			}
			
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void combiner(String startDateStr, String endDateStr, String dateFormat) {
		try {
			SimpleDateFormat sdf1 = new SimpleDateFormat(dateFormat);
			combiner(sdf1.parse(startDateStr), sdf1.parse(endDateStr));
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

