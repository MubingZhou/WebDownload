package utils;

import java.awt.Toolkit;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.log4j.Logger;

import test_no_use.AA;

public class Utils {
	// OUTPUT root path
	public static final String OUTPUT_ROOT_PATH_HKEX = "D:\\stock data\\HKEX";
	
	// csv file storing all brokers' names & short names
	public static final String BROKER_NAME_FILE_PATH = "D:\\stock data\\CCASS Participants List.csv";
	
	// csv file storing all trading date
	public static final String TRADING_DATE_FILE_PATH = "D:\\stock data\\all trading date.csv";
	
	public static final String SH_SOUTHBOUND_STOCKLIST_PATH = utils.Utils.addBackSlashToPath(utils.PathConifiguration.STOCK_DATA_ROOT_PATH) + "Southbound stocks\\SSE Southbound Stocks.csv" ;
	public static final String SZ_SOUTHBOUND_STOCKLIST_PATH = utils.Utils.addBackSlashToPath(utils.PathConifiguration.STOCK_DATA_ROOT_PATH) + "Southbound stocks\\SZSE Southbound Stocks.csv" ;
	public static final String HSI_STOCKLIST_PATH = utils.PathConifiguration.STOCK_DATA_ROOT_PATH + "\\HSI HSCEI index\\HSI history from 2010.csv";
	public static final String HSCEI_STOCKLIST_PATH = utils.PathConifiguration.STOCK_DATA_ROOT_PATH + "\\HSI HSCEI index\\HSCEI history from 2010.csv";
			
	private static Logger logger = Logger.getLogger(Utils.class.getName());
	
	/**
	 * Using HttpConnection to link url with "https://"
	 * to trust all certificates
	 */
	public static void trustAllCertificates(){
		// to access to HTTPS url, we need below codes to trust all certificates
		// Create a new trust manager that trust all certificates
		TrustManager[] trustAllCerts = new TrustManager[]{
			    new X509TrustManager() {
			        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
			            return null;
			        }
			        public void checkClientTrusted(
			            java.security.cert.X509Certificate[] certs, String authType) {
			        }
			        public void checkServerTrusted(
			            java.security.cert.X509Certificate[] certs, String authType) {
			        }
			    }
			};
		// Activate the new trust manager
			try {
			    SSLContext sc = SSLContext.getInstance("SSL");
			    sc.init(null, trustAllCerts, new java.security.SecureRandom());
			    HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
			} catch (Exception e) {
			}
	}
	
	/**
	 * to download file from a specified url (if open that url from the browser, it should start to download file)
	 * @param urlStr
	 * @param fileName
	 * @param savePath
	 * @throws IOException
	 */
    public static void  downLoadFromUrl(String urlStr,String fileName,String savePath) throws IOException{  
    	trustAllCertificates(); // just in case the url is "https"
    	
    	URL url = new URL(urlStr);    
        HttpURLConnection conn = (HttpURLConnection)url.openConnection();    
                //扢离閉奀潔峈3鏃  
        conn.setConnectTimeout(3*1000);  
        //滅砦敖最唗蚰奧殿隙403渣昫  
        conn.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");  
  
        //腕善怀霜  
        InputStream inputStream = conn.getInputStream();    
        //鳳赻撩杅郪  
        byte[] getData = readInputStream(inputStream);      
  
        //恅璃悵湔弇离  
        File saveDir = new File(savePath);  
        if(!saveDir.exists()){  
            saveDir.mkdir();  
        }  
        File file = new File(saveDir+File.separator+fileName);      
        FileOutputStream fos = new FileOutputStream(file);       
        fos.write(getData);   
        if(fos!=null){  
            fos.close();    
        }  
        if(inputStream!=null){  
            inputStream.close();  
        }  
  
  
        System.out.println("info:"+url+" download success");   
  
    }  
    
    /**
     * download the HTML page and store it
     * @param urlStr
     * @param outputFilePath
     * @throws Exception
     */
    public static void downLoadHTMLFromUrl(String urlStr, String outputFilePath) throws Exception{
    	trustAllCertificates(); // just in case the url is "https"
    	
    	URL url = new URL(urlStr);    
        HttpURLConnection conn = (HttpURLConnection)url.openConnection();    
                //扢离閉奀潔峈3鏃  
        conn.setConnectTimeout(3*1000);  
        //滅砦敖最唗蚰奧殿隙403渣昫  
        conn.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");  
  
        //腕善怀霜  
        InputStream inputStream = conn.getInputStream();  
        
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "utf-8");
		//System.out.println("here15847");
		
		BufferedReader bufReader = new BufferedReader(inputStreamReader);
		
		FileWriter fw = new FileWriter(outputFilePath);
		String line = "";
		StringBuilder contentBuf = new StringBuilder();
		int counter = 0;
		while ((line = bufReader.readLine()) != null) {
			contentBuf.append(line + "\n");
			//fw.write(line);
			//System.out.println(line);
			if(counter % 10 == 0){
				//System.out.println("====The " + counter + "th line=====");
			}
			counter++;
		}
		fw.write(contentBuf.toString());
		fw.close();
    }
    
    /**
     * Only serve downLoadFromUrl(....)
     * @param inputStream
     * @return
     * @throws IOException
     */
	  private static  byte[] readInputStream(InputStream inputStream) throws IOException {    
	        byte[] buffer = new byte[1024];    
	        int len = 0;    
	        ByteArrayOutputStream bos = new ByteArrayOutputStream();    
	        while((len = inputStream.read(buffer)) != -1) {    
	            bos.write(buffer, 0, len);    
	        }    
	        bos.close();    
	        return bos.toByteArray();    
	    }   
	  
		/**
		 * check if a String is a double
		 * @param str
		 * @return
		 */
		public static boolean isDouble(String str) {
			boolean isOK = true;
			
			try {
				double d = Double.parseDouble(str);
			}catch(NumberFormatException nfe) {
				isOK = false;
			}
			catch(Exception e) {
				isOK = false;
			}
			
			return isOK;
		}
		
		public static Double safeParseDouble(String s, Double defaultV) {
			if(isDouble(s))
				return Double.parseDouble(s);
			else
				return defaultV;
			
		}
		
		/**
		 * check if a String is a int
		 * @param str
		 * @return
		 */
		public static boolean isInteger(String str) {
			boolean isOK = true;
			
			try {
				int i = Integer.parseInt(str);
			}catch(NumberFormatException nfe) {
				isOK = false;
			}
			catch(Exception e) {
				isOK = false;
			}
			
			return isOK;
		}
		
		/**
		 * check if a String is a date
		 * @param str
		 * @return
		 */
		public static boolean isDate(String str, String dateFormat) {
			boolean isOK = true;
			
			try {
				SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
				sdf.parse(str);
			}catch(ParseException pe) {
				isOK = false;
			}
			catch(Exception e) {
				isOK = false;
			}
			
			return isOK;
		}
		
		public static ArrayList<String> readFileByLine(String filePath) throws Exception{
			ArrayList<String> data = new ArrayList<String> ();
			
			InputStream input = new FileInputStream(new File(filePath));
			InputStreamReader inputStreamReader = new InputStreamReader(input);
			BufferedReader bufReader = new BufferedReader(inputStreamReader);
			
			String line = "";
			while ((line = bufReader.readLine()) != null) {
				data.add(line);
			}
			
			return data;
		}
		
		public static BufferedReader readFile_returnBufferedReader(String filePath, String code) {
			// code - file formatting
			
			//ArrayList<String> data = new ArrayList<String> ();
			BufferedReader bufReader = null;
			
			try{
				InputStream input = new FileInputStream(new File(filePath));
				InputStreamReader inputStreamReader = new InputStreamReader(input, code);
				bufReader = new BufferedReader(inputStreamReader);
			}catch(Exception e){
				e.printStackTrace();
			}
			return bufReader;
		}
		
		public static BufferedReader readFile_returnBufferedReader(String filePath) {
			//ArrayList<String> data = new ArrayList<String> ();
			BufferedReader bufReader = null;
			
			try{
				InputStream input = new FileInputStream(new File(filePath));
				InputStreamReader inputStreamReader = new InputStreamReader(input);
				bufReader = new BufferedReader(inputStreamReader);
			}catch(Exception e){
				e.printStackTrace();
			}
			return bufReader;
		}
		
		/**
		 * to convert a date array in String to in the form of ArrayList<Calendar>
		 * @param dateArr
		 * @param dateFormat
		 * @return date arrray
		 * @throws Exception
		 */
		public static ArrayList<Calendar> dateStr2Cal(ArrayList<String> dateArr, String dateFormat) throws Exception{
			ArrayList<Calendar> toReturn = new ArrayList<Calendar> ();
			
			for(int i = 0; i < dateArr.size(); i++) {
				String todayDateStr = dateArr.get(i);
				
				toReturn.add(dateStr2Cal(todayDateStr, dateFormat));
			}
			
			return toReturn;
		}
		
		public static ArrayList<Calendar> dateStr2Cal(String[] dateArr, String dateFormat) throws Exception{
			return dateStr2Cal(new ArrayList<String>(Arrays.asList(dateArr)), dateFormat);
		}
		
		public static Calendar dateStr2Cal(String dateStr, String dateFormat) throws Exception{
			SimpleDateFormat sdf =   new SimpleDateFormat(dateFormat);
			Date date = sdf.parse(dateStr);
			Calendar cal = Calendar.getInstance();
			cal.setTime(date);
			
			return cal;
		}
		
		/**
		 * convert Calendar to String
		 * @param cal
		 * @param dateFormat
		 * @return
		 * @throws Exception
		 */
		public static String date2Str(Calendar cal, String dateFormat) throws Exception{
			SimpleDateFormat sdf =   new SimpleDateFormat(dateFormat);
			
			return sdf.format(cal.getTime());
		}
		
		/**
		 * Input: an ArrayList of Calendar
		 * output: an ArrayList of String with specified date format
		 * @param calArr
		 * @param dateFormat
		 * @return
		 * @throws Exception
		 */
		public static ArrayList<String> date2Str(ArrayList<Calendar> calArr, String dateFormat) throws Exception{
			ArrayList<String> toReturn = new ArrayList<String> ();
			
			for(int i = 0; i < calArr.size(); i++) {
				Calendar cal = calArr.get(i);
				toReturn.add(date2Str(cal, dateFormat));
			}
			
			return toReturn;
		}
		
		/**
		 * get the days between two dates (yyyy-MM-dd) (both start & end date inclusive)
		 * @param startDate
		 * @param endDate
		 * @return
		 * @throws Exception
		 */
		public static ArrayList<String> getWorkingDaysBetweenDates(String startDate, String endDate, String dateFormat) throws Exception{
			ArrayList<String> datesList = new ArrayList<String>();
			
			SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
			
			Calendar cal_start = new GregorianCalendar();
			cal_start.setTime(sdf.parse(startDate));
			
			Calendar cal_end = new GregorianCalendar();
			cal_end.setTime(sdf.parse(endDate));
			
			while(cal_start.getTime().before(cal_end.getTime())){
				if(cal_start.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY && cal_start.get(Calendar.DAY_OF_WEEK) != Calendar.SATURDAY){
					String date = sdf.format(cal_start.getTime());
					//System.out.println(date);
					datesList.add(date);
				}
				cal_start.add(Calendar.DATE, 1);
			}
			
			if(cal_end.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY && cal_end.get(Calendar.DAY_OF_WEEK) != Calendar.SATURDAY){
				String date = sdf.format(cal_end.getTime());
				//System.out.println(date);
				datesList.add(date);
			}
			
			
			return datesList ;
		}
		
		/**
		 * check if dir exists, if not, create one
		 * @param dir
		 */
		public static void checkDir(String dir){
			File dirFile = new File(dir);
			if(!dirFile.exists() && !dirFile.isDirectory()){
				dirFile.mkdir();
			}
		}
		
		/**
		 * Write the stream data to file. Please ensure the file path exists and is correct (with suffix .html)
		 * @param inputStream
		 * @param outputPath
		 * @throws Exception
		 */
		public static void storeHTML(InputStream inputStream, String outputPath) throws Exception{
			storeHTML(inputStream, outputPath,"utf-8");
		}
		
		/**
		 * format - utf-8/gbk/....
		 * @param inputStream
		 * @param outputPath
		 * @param format
		 * @throws Exception
		 */
		public static void storeHTML(InputStream inputStream, String outputPath, String format) throws Exception{
			InputStreamReader inputStreamReader = new InputStreamReader(inputStream, format);
			//System.out.println("inputStreamReader type=" + inputStreamReader.getEncoding());
			//System.out.println("here15847");
			
			BufferedReader bufReader = new BufferedReader(inputStreamReader);
			
			FileWriter fw = new FileWriter(outputPath);
			String line = "";
			StringBuilder contentBuf = new StringBuilder();
			int counter = 0;
			while ((line = bufReader.readLine()) != null) {
				//line = new String(line.getBytes("GB2312"), "UTF-8");
				//System.out.println(line);
				contentBuf.append(line);
				//fw.write(line);
				//fw.write(line);
				//System.out.println(line);
				if(counter % 10 == 0){
					//System.out.println("====The " + counter + "th line=====");
				}
				counter++;
			}
			bufReader.close();
			fw.write(contentBuf.toString());
			fw.close();
		}
		
		/** 
		* 葩秶等跺恅璃 
		* @param oldPath String 埻恅璃繚噤 ㄩc:/fqf.txt 
		* @param newPath String 葩秶綴繚噤 ㄩf:/fqf.txt 
		* @return boolean 
		*/ 
		public static boolean copyFile(String oldPath, String newPath) { 
			boolean isOK = true;
			try { 
				int bytesum = 0; 
				int byteread = 0; 
				File oldfile = new File(oldPath); 
				if (oldfile.exists()) { //恅璃湔婓奀 
					InputStream inStream = new FileInputStream(oldPath); //黍埻恅璃 
					FileOutputStream fs = new FileOutputStream(newPath); 
					byte[] buffer = new byte[1444]; 
					int length; 
					while ( (byteread = inStream.read(buffer)) != -1) { 
						bytesum += byteread; //趼誹杅 恅璃湮苤 
						System.out.println(bytesum); 
						fs.write(buffer, 0, byteread); 
					} 
					inStream.close(); 
				} 
			} 
			catch (Exception e) { 
				System.out.println("Error when copying file. old path: " + oldPath + " - new path: " + newPath); 
				e.printStackTrace(); 
				isOK = false;
		
			} 
			
			return isOK;
		} 
		
		/**
		 * To change an array of String to a whole String
		 * @param arr
		 * @param separator
		 * @return
		 */
		public static String arrayToString(ArrayList<String> arr, String separator) {
			String s="";
			for(int i = 0; i < arr.size(); i++) {
				String str = arr.get(i);
				
				if(i != 0) {
					s = s + separator + str;
				}else {
					s = s + str;
				}
			}
			
			return s;
		}
		
		/**
		 * to format the date into specified format. This function will automatically recognize the original date format
		 * @param date
		 * @param toFormat
		 * @return
		 */
		public static String formatDate(String date, String fromFormat, String toFormat){
			/*
			ArrayList<SimpleDateFormat> sdf = new ArrayList<SimpleDateFormat>();
			sdf.add(new SimpleDateFormat("yyyyMMdd"));
			sdf.add(new SimpleDateFormat("yyyy-MM-dd"));
			sdf.add(new SimpleDateFormat("dd/MM/yyyy"));
			sdf.add(new SimpleDateFormat("d/MM/yyyy"));
			sdf.add(new SimpleDateFormat("yyyy/MM/dd"));
			sdf.add(new SimpleDateFormat("yyyy/M/dd"));
			
			SimpleDateFormat sdf2 = new SimpleDateFormat(toFormat);
			Date d = null;
			
			for(int i = 0; i < sdf.size(); i++){
				boolean isRecognized = true;
				
				try{
					d = sdf.get(i).parse(date);
					System.out.println("pase");
				}catch(Exception e){
					isRecognized = false;
				}
				
				if(isRecognized){
					break;
				}
			}
			
			String toReturn = sdf2.format(d);
			return toReturn;*/
			
			SimpleDateFormat sdf1 = new SimpleDateFormat(fromFormat);
			SimpleDateFormat sdf2 = new SimpleDateFormat(toFormat);
			
			try{
				return sdf2.format(sdf1.parse(date));
			}catch(Exception e){
				return null;
			}
			
		}
		
		/**
		 * to return Bloomberg formula. Compatible with .csv file.
		 * to use the formula in csv file, you have to manually add " to the front and end of the formula
		 * e.g. this function may return BDH(""1 HK Equity"",""EQ_SH_OUT"", ""20170801"", ""20170818"")
		 * but to show it correctly in csv file, you have to use "=BDH(""1 HK Equity"",""EQ_SH_OUT"", ""20170801"", ""20170818"")" 
		 * @param stock e.g. 1 HK Equity
		 * @param field, e.g. PX_LAST
		 * @param startDate e.g. 20170801
		 * @param endDate e.g.20170819
		 * @return
		 */
		public static String BBG_BDH_Formula(String stock, String field, String startDate, String endDate){
			return "BDH(" + "\"\"" + stock + "\"\"" 
					+ ",\"\"" + field + "\"\"," + "\"\"" + startDate + "\"\"" + "," + "\"\"" + endDate + "\"\"" 
					+ ")";  //e.g. =BDH(""1 HK Equity"",""EQ_SH_OUT"", ""20170801"", ""20170818"")
		}
		
		public static String BBG_BDP_Formula(String stock, String field) {
			return "BDH(" + "\"\"" + stock + "\"\"" 
					+ ",\"\"" + field + "\"\"" + ")";  // e.g. =BDP(""1 HK Equity"", ""EQ_SH_OUT"")
		}
		
		/**
		 * to get the southbound stock list for some specified date
		 * @param date
		 * @param dateFormat
		 * @param isSH
		 * @param isSZ
		 * @return
		 */
		public static ArrayList<String> getSouthboundStocks(String date, String dateFormat, boolean isSH, boolean isSZ){
			ArrayList<String> allStockList = new ArrayList<String>();
			try {
				if(isSH && !isSZ)
					return getStocksFromStockAdjHistory(SH_SOUTHBOUND_STOCKLIST_PATH, date, dateFormat);
				if(!isSH && isSZ)
					return getStocksFromStockAdjHistory(SZ_SOUTHBOUND_STOCKLIST_PATH, date, dateFormat);
				if(!isSH && !isSZ)
					return null;
				
				ArrayList<String> shStockList = getStocksFromStockAdjHistory(SH_SOUTHBOUND_STOCKLIST_PATH, date, dateFormat);
				ArrayList<String> szStockList = getStocksFromStockAdjHistory(SZ_SOUTHBOUND_STOCKLIST_PATH, date, dateFormat);
				
				allStockList = shStockList;
				for(int i = 0; i < szStockList.size(); i++) {
					if(allStockList.indexOf(szStockList.get(i)) == -1) {
						allStockList.add(szStockList.get(i));
					}
				}
			}catch(Exception e) {
				e.printStackTrace();
			}
			
			return allStockList;
		}
		
		/**
		 * to get the HSI/HSCEI stock list for some specified date
		 * @param date
		 * @param dateFormat
		 * @param isHSCEI
		 * @param isHSI
		 * @return
		 */
		public static ArrayList<String> getHSCEI_HSIStocks(String date, String dateFormat, boolean isHSCEI, boolean isHSI){
			ArrayList<String> allStockList = new ArrayList<String>();
			try {
				if(isHSCEI && !isHSI)
					return getStocksFromStockAdjHistory(HSCEI_STOCKLIST_PATH, date, dateFormat);
				if(!isHSCEI && isHSI)
					return getStocksFromStockAdjHistory(HSI_STOCKLIST_PATH, date, dateFormat);
				if(!isHSCEI && !isHSI)
					return null;
				
				ArrayList<String> hsiStockList = getStocksFromStockAdjHistory(HSI_STOCKLIST_PATH, date, dateFormat);
				ArrayList<String> hsceiStockList = getStocksFromStockAdjHistory(HSCEI_STOCKLIST_PATH, date, dateFormat);
				
				allStockList = hsiStockList;
				for(int i = 0; i < hsceiStockList.size(); i++) {
					if(allStockList.indexOf(hsceiStockList.get(i)) == -1) {
						allStockList.add(hsceiStockList.get(i));
					}
				}
			}catch(Exception e) {
				e.printStackTrace();
			}
			
			return allStockList;
		}
		
		/**
		 * 从某个指数的调整历史中获取stock list，比如可以是获取HSI的成分股，所输入的文件需要符合以下格式：
		 * 	Code,	Chinese,	Eng,		Change,	Date (dd/MM/yyyy)
			288,	萬洲國際,		WH Group,	1,		4/9/2017
			700,	腾讯,			Tencent,	-1,		4/9/2017
			...
			
		 * @param filePath
		 * @param date
		 * @param dateFormat
		 * @return
		 */
		private static ArrayList<String> getStocksFromStockAdjHistory(String filePath, String date, String dateFormat) {
			BufferedReader bf = null;
			ArrayList<String> stockList = new ArrayList<String>();
			try {
				//String special = "2969";  // it seems that this code always represents a temporary code, so if it appears in the list, it should be mapped back to its original code
				
				// allData is in the following form:
				// stock code1, direction, date
				// stock code2, direction, date ...
				ArrayList<ArrayList<Object>> allData = new ArrayList<ArrayList<Object>>();
				
				// read data
				bf = readFile_returnBufferedReader(filePath);
				String line = "";
				int counter = 0;
				SimpleDateFormat thisSdf = new SimpleDateFormat("dd/MM/yyyy");
				ArrayList<Object> dataLine;
				Calendar cal =Calendar.getInstance();
				Date readDate;
				while((line = bf.readLine())!= null) {
					if(counter == 0) {
						counter ++; // skip the first line
						continue;
					}
					
					String[] thisLineArr = line.split(",");
					String stockCode = thisLineArr[0];
					String direction = thisLineArr[3];
					String thisDate = thisLineArr[4];

					// cal = ;
					readDate = thisSdf.parse(thisDate);
					cal.setTime(thisSdf.parse(thisDate));
					
					dataLine = new ArrayList<Object> ();
					dataLine.add(stockCode);
					dataLine.add(direction);
					//dataLine.add(cal);
					dataLine.add(readDate);
					
					allData.add(dataLine);
					dataLine = null;
					//cal = null;
				}
				/*
				// sort data with date descending, latest date in the front
				Comparator cp = new Comparator() {
					public int compare(Object o0, Object o1) {
						
						ArrayList<Object> arg0 = (ArrayList<Object>) o0;
						ArrayList<Object> arg1 = (ArrayList<Object>) o1;
						int cp = 0;
						
						Calendar c0 = (Calendar) arg0.get(2);
						Calendar c1 = (Calendar) arg1.get(2);
						
						if(c0.before(c1)) {
							cp = -1;
							if(c0.after(c1)) {
								cp = 1;
							}else {
								cp = 0;
							}
								
						}
						return cp;
					}
					
				};
				//Collections.sort(allData, cp);
				*/
				// display [temp]
				if(false)
				for(int i = 0; i < 20; i++) {
					ArrayList<Object> thisLine = allData.get(i);
					Calendar c = (Calendar) thisLine.get(2);
					String stockCode = (String) thisLine.get(0);
					String dir = (String) thisLine.get(1);
					
					System.out.println(stockCode + " " + dir + " " + new SimpleDateFormat("yyyyMMdd").format(c.getTime())); 
				}
				
				//get the stock list
				//Calendar benchDate = Calendar.getInstance();
				//benchDate.setTime(new SimpleDateFormat(dateFormat).parse(date));
				Date benchDate = new SimpleDateFormat(dateFormat).parse(date);
				for(int i = allData.size()-1; i > -1; i--) {
					ArrayList<Object> thisLine = allData.get(i);
					
					Date thisReadDate = (Date) thisLine.get(2);
					String stockCode = (String) thisLine.get(0);
					String dir = (String) thisLine.get(1);
					
					if(!thisReadDate.after(benchDate)) {
						if(dir.equals("1")) {
							int ind = stockList.indexOf(stockCode);
							if(ind == -1)
								stockList.add(stockCode);
						}else if(dir.equals("-1")) {
							int ind = stockList.indexOf(stockCode);
							if(ind != -1)
								stockList.remove(ind);
						}else {
							System.out.println("[Get Stock List Data] direction not correct! " + stockCode + benchDate.getTime());
						}
					}else
						break;
				}
				
				// print out [temp]
				if(false) {
					FileWriter fw = new FileWriter("D:\\test.csv");
					for(int i = 0; i < stockList.size(); i++) {
						fw.write(stockList.get(i) + "\n");
					}
					fw.close();
				}
			}catch(Exception e) {
				e.printStackTrace();
			}finally {
				try {
					bf.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			
			return stockList;
		}
		
		/**
		 * get all trading date and sort them ascendingly, i.e. older dates in the front
		 * date should be in "dd/MM/yyyy"
		 * @param filePath
		 * @return
		 */
		public static ArrayList<Calendar> getAllTradingCal(String filePath){
			ArrayList<Calendar> allTradingDate = new ArrayList<Calendar>();
			
			try {
				BufferedReader bf  = utils.Utils.readFile_returnBufferedReader(filePath);
				String line = bf.readLine();
				String[] lineArr = line.split(",");
				
				String dateFormat = "dd/MM/yyyy";
				SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
				
				for(int i = 0; i < lineArr.length; i++) {
					Calendar cal = Calendar.getInstance();
					cal.setTime(sdf.parse(lineArr[i]));
					allTradingDate.add(cal);
				}
				
				// sorting - ascending, i.e. older date at the front
				Collections.sort(allTradingDate);
			}catch(Exception e) {
				System.out.println("getting all trading date failed!");
			}
			
			
			return allTradingDate;
		}
		
		/**
		 * get all trading date and sort them ascendingly, i.e. older dates in the front
		 * @param filePath
		 * @return
		 */
		public static ArrayList<Date> getAllTradingDate(String filePath){
			ArrayList<Date> allTradingDate = new ArrayList<Date>();
			
			try {
				BufferedReader bf  = utils.Utils.readFile_returnBufferedReader(filePath);
				String line = bf.readLine();
				String[] lineArr = line.split(",");
				
				String dateFormat = "dd/MM/yyyy";
				SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
				
				for(int i = 0; i < lineArr.length; i++) {
					Calendar cal = Calendar.getInstance();
					cal.setTime(sdf.parse(lineArr[i]));
					allTradingDate.add(cal.getTime());
				}
				
				// sorting - ascending, i.e. older date at the front
				Collections.sort(allTradingDate);
			}catch(Exception e) {
				System.out.println("getting all trading date failed!");
			}
			
			
			return allTradingDate;
		}
		
		public static ArrayList<Calendar> getAllTradingCal(){
			return getAllTradingCal(utils.PathConifiguration.ALL_TRADING_DATE_PATH_HK);
		}
		public static ArrayList<Date> getAllTradingDate(){
			return getAllTradingDate(utils.PathConifiguration.ALL_TRADING_DATE_PATH_HK);
		}
		
		/**
		 * Get the most recent date before thisCal, for example, if thisCal = 20170801, calArr = {20170701,20170820,20170920}
		 * this function will return 20170701
		 * if thisCal is before every date of calArr, it returns null
		 * @param thisCal
		 * @param calArr
		 * @return
		 * @throws Exception
		 */
		public static Calendar getMostRecentDate(Calendar thisCal, ArrayList<Calendar> calArr) throws Exception{
			Collections.sort(calArr); // ascending
			
			if(calArr== null || calArr.size() == 0) {
				return null;
			}
			if(calArr.size() == 1) {
				if(thisCal.before(calArr.get(0))) {
					logger.info("[Utils - getMostRecentDate] array size 1 and today before all dates!");
					return null;
				}
					
				else
					return calArr.get(0);
			}
			
			boolean isFound = false;
			for(int i = calArr.size() - 2; i > -1 ; i--) {
				Calendar nextTradingDate = calArr.get(i+1);
				Calendar thisTradingDate = calArr.get(i);
				
				if(i == calArr.size() - 2) {
					if(thisCal.after(nextTradingDate) || thisCal.equals(nextTradingDate))
						return nextTradingDate;
				}
				if(!thisCal.before(thisTradingDate) && thisCal.before(nextTradingDate)) {
					thisCal = thisTradingDate;
				}
				if(i == 0) {  // thisCal is before every date of calArr
					if(thisCal.before(thisTradingDate)) {
						logger.info("[Utils - getMostRecentDate] today before all dates!");
						return null;
					}
						
				}
			}
			
			return thisCal;
		}
		
		public static Date getMostRecentDate(Date thisDate, ArrayList<Date> dateArr) throws Exception{
			Calendar cal = Calendar.getInstance();
			cal.setTime(thisDate);
			
			ArrayList<Calendar> calArr = new ArrayList<Calendar>();
			for(int i = 0 ; i< dateArr.size(); i++) {
				Calendar thisCal = Calendar.getInstance();
				thisCal.setTime(dateArr.get(i));
				calArr.add(thisCal);
			}
			
			Calendar mostRecentCal = getMostRecentDate(cal, calArr);
			
			return mostRecentCal.getTime();
		}
		

		
		/**
		 * 获得相对于“某个日期”位移一段时间的日期，shift为正数，表示往后shift。比如有三个日期是相连的：1月22日，1月25日，1月26日
		 * 那么如果“某个日期”为1月25日，shift为1，则得到1月26日，如果shift为-1，则得到1月22日
		 * 注：需要确保dateArr是按照由旧到新排列的
		 * @param thisDate  “某个日期”
		 * @param dateArr
		 * @param shift
		 * @return
		 * @throws Exception
		 */
		public static Date getRefDate(Date thisDate, ArrayList<Date> dateArr, int shift) throws Exception{
			Date toReturn = new Date();
			
			if(dateArr == null || dateArr.size() == 0) {
				logger.error("utils.Utils.getRefDate: dateArr size 0 or null!");
				return null;
			}
				
			
			if(thisDate.before(dateArr.get(0)) || thisDate.after(dateArr.get(dateArr.size()-1))) {
				logger.error("utils.Utils.getRefDate: thisDate outside of dateArr range!");
				return null;
			}
			
			for(int i = 0; i < dateArr.size()-1; i++) {
				Date dateArr_thisDate = dateArr.get(i);
				Date dateArr_nextDate = dateArr.get(i+1);
				if(!thisDate.before(dateArr_thisDate) 
						&& !thisDate.after(dateArr_nextDate)) {  // thisDate在dateArr_thisDate和dateArr_nextDate之间
					int toGetDateInd =  i + shift;
					if(toGetDateInd < 0) {
						logger.error("utils.Utils.getRefDate: shift range negative!");
						return null;
					}
					if(toGetDateInd > dateArr.size()) {
						logger.error("utils.Utils.getRefDate: shift range outside largest range!");
						return null;
					}
					
					toReturn = dateArr.get(toGetDateInd);
				}
			}
			
			return toReturn;
				
		}
		
		/**
		 * Add "\" to the end of a string if it is not ended with "\"
		 * e.g String s = "D:\\test" will become "D:\\test\\"
		 * @param s
		 * @return
		 */
		public static String checkPath(String s) {
			if(!s.substring(s.length() - 1).equals("\\")) {
				return (s + "\\");
			}else
				return s;
			
		}
		
		/**
		 * To get the stock-sector pair value.
		 * The default file path might be "D:\\stock data\\stock sector - karen.csv"
		 * @param path
		 * @return
		 */
		public static Map<String, String> getStockSectors(String path){
			Map<String, String> map = new HashMap();
			
			try {
				BufferedReader bf = readFile_returnBufferedReader(path);
				String line1 = bf.readLine();
				String line2 = bf.readLine();
				bf.close();
				
				ArrayList<String> stock = new ArrayList<String>(Arrays.asList(line1.split(",")));
				ArrayList<String> sector = new ArrayList<String>(Arrays.asList(line2.split(",")));
				
				if(stock.size() != sector.size()) {
					logger.error("stock list length and sector list length not match!");
					return null;
				}
				
				for(int i = 0; i < stock.size(); i++) {
					map.put(stock.get(i), sector.get(i));
				}
				
				
			}catch(Exception e) {
				e.printStackTrace();
				map = null;
			}
			
			
			return map;
		}
		
		/**
		 * To get the stock-free_float_pct pair value.
		 * The default file path might be "D:\\stock data\\freefloat pct - hk.csv"
		 * @param path
		 * @return
		 */
		public static Map<String, String> getFreeFloatPct(String path){
			Map<String, String> map = new HashMap();
			try {
				BufferedReader bf = readFile_returnBufferedReader(path);
				
				String line = ""	;
				while((line = bf.readLine())!= null) {
					String[]  lineArr = line.split(",");
					map.put(lineArr[0], lineArr[1]);
				}
				
			}catch(Exception e) {
				e.printStackTrace();
				map = null;
			}
			
			return map;
	}
		
	/**
	 * 将一个变量存到指定路径，但是该变量需要implements Serializable
	 * path必须带文件名（后缀名可以是任何名，比如test.javaobj
	 * @param obj
	 * @param path
	 * @throws Exception
	 */
		/*
	public static void saveObject(Object obj, String path) throws Exception { 
		FileOutputStream fos = new FileOutputStream(path);
		ObjectOutputStream out = new ObjectOutputStream(fos);
		
		out.writeObject(obj);
		out.close();
		fos.close();
	}
	*/
	/**
	 * 读取某变量
	 * @param path
	 * @return
	 * @throws Exception
	 */
		/*
	public static Object readObject(String path) throws Exception{
		File f = new File(path);
		if(!f.exists())
			return null;
		
		FileInputStream fis = new FileInputStream(path);
		ObjectInputStream in = new ObjectInputStream(fis);
		
		Object r = in.readObject();
		fis.close();
		in.close();
		
		return r;
	}
	*/
		
	public static String addBackSlashToPath(String path) {
		if(!path.substring(path.length()-1, path.length()).equals("\\"))
			return path + "\\";
		else
			return path;
	}
	
	public static String removeBackSlashFromPath(String path) {
		if(path.substring(path.length()-1, path.length()).equals("\\"))
			return path.substring(0, path.length()-1);
		else
			return path;
	}

	/**
	 * Extract the numbers from a string. e.g. if str="sdf345sdrew2134", then the function will return "3452134" (in the form of String)
	 * @param str
	 * @return
	 */
	public static String getNumFromString(String str) {
		String num = "";
		
		if(str != null && !"".equals(str)){
			for(int i=0;i<str.length();i++){
				if(str.charAt(i)>=48 && str.charAt(i)<=57){
					num+=str.charAt(i);
				}
			}
		}
		
		return num;
	}
	
	/**
	 * To check if the input string contains numbers
	 * @param input
	 * @return
	 */
	public static boolean isContainNumbers(String input) {
		boolean output = false;
		
		for(int i = 0; i < input.length(); i++){
			char c = input.charAt(i);
			int n = (int)c;
			
			if( n >= 48 && n <= 57){
				output = true;
				break;
			}
		}
		
		return output;
	}
	
	
	/**
	 * to convert daily sectional data from TDX to the format accepted by AmiBroker
	 * @param readFilePath
	 * @param writeFilePath
	 * @param todayDate
	 */
	public static void convertTDX2AB_Daily(String readFilePath, String writeFilePath, String todayDate) {
		try {
			BufferedReader br = readFile_returnBufferedReader(addBackSlashToPath(readFilePath )+ "自选股" + todayDate + ".txt", "gbk");
			BufferedReader br2 = readFile_returnBufferedReader(addBackSlashToPath(readFilePath )+ "沪深主要指数" + todayDate + ".txt", "gbk");
			
			// writing files
			FileWriter fw = new FileWriter(addBackSlashToPath(writeFilePath) + "A Share - Strutured Fund - ETF" + todayDate + ".txt");
			FileWriter fw2 = new FileWriter(addBackSlashToPath(writeFilePath) + "A Indexes" + todayDate + ".txt");
			
			String txt  = null;
			ArrayList<String> items = new ArrayList<String>();     // 用来存储自己想要的列
			items.add("代码");
			items.add("名称");	items.add("今开");	items.add("最高");	
			items.add("最低");	items.add("现价"); 	items.add("总量");	
			items.add("流通股(亿)"); 	items.add("总股本(亿)");
			//ArrayList<Integer> itemsIndex = new ArrayList<Integer>();
			
			ArrayList<String> items2 = new ArrayList<String>();     // 用来存储自己想要的列
			items2.add("代码");
			items2.add("名称");	items2.add("今开");	items2.add("最高");	
			items2.add("最低");	items2.add("现价"); 	items2.add("总量");
			//ArrayList<Integer> itemsIndex2 = new ArrayList<Integer>();
			
			
			// conversion
			convertTDX2AB_Daily_temp(br, fw, items, todayDate, 1);
			convertTDX2AB_Daily_temp(br2, fw2, items2, todayDate, 2);  // 沪深指数数据
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Only designed for function convertTDX2AB_Daily
	 * @param br
	 * @param fw
	 * @param items
	 * @param todayDate
	 * @param type
	 */
	private static void convertTDX2AB_Daily_temp(BufferedReader br, FileWriter fw, ArrayList<String> items, String todayDate, int type) {
		try {
			ArrayList<Integer> itemsIndex = new ArrayList<Integer>();
			
			String txt = "";
			int isFirstLine = 1;
			int numCol = 0;   // num of columns
			int jjj = 0;
			while((txt = br.readLine())!=null){//使用readLine方法，一次读一行
				String[] txtArr = txt.split("\t");
				
				if(isFirstLine == 1){  // 获取表头，找到自己想要的列的序号
					isFirstLine = 0;
					
					List<String> listA = Arrays.asList(txtArr);
					ArrayList<String> txtArrList = new ArrayList<String>(listA);   // 将String[]转换成ArrayList
					  
					System.out.println("Biaotou");
					for(String str : txtArrList) {
						System.out.println(str);
					}
					
					for(int i = 0; i < items.size(); i++){
						itemsIndex.add(txtArrList.indexOf(items.get(i)));   // 得到items中每个item的列号
						System.out.println("item = " + items.get(i) + " index = " + itemsIndex.get(i));
						
						// 写表头???
						fw.write(items.get(i) + ",");
						if(items.get(i).equals("代码"))
							fw.write("日期,");
					}
					fw.write("\n");
					
					numCol = txtArr.length;
				}
				else{
					if(txtArr.length == numCol){   // 除去那些不符合要求的行
						String output = "";
						
						// 先检查是否停牌了
						int isHaltTrading = 0;   // 是否停牌
						String closeStr = "";    // 停牌时的收盘价
						String tempStr1 = txtArr[itemsIndex.get(items.indexOf("今开"))];
						String tempStr2 = txtArr[itemsIndex.get(items.indexOf("总量"))];
						if(!isContainNumbers(tempStr1)){
							isHaltTrading = 1;
							closeStr = txtArr[itemsIndex.get(items.indexOf("现价"))];
							//System.out.println("== 停牌 close = " + closeStr);
						}
						//System.out.println("========== tempStr = " + tempStr);
						
						
						for(int i = 0; i < items.size(); i++){
							String str = txtArr[itemsIndex.get(i)];
							
							// 处理停牌的情况, 停牌时，开盘价，最低价，最高价都是“--”，要替换成收盘价呢
							if(isHaltTrading == 1){  
								if(items.get(i).equals("今开") || items.get(i).equals("最高") || items.get(i).equals("最低")){
									str = closeStr;
								}
							}
							
							str = removeSpaceTab(str);
							
							// "代码"加后缀（前缀）
							if(items.get(i).equals("代码")){
								str = addSuffixPrefixForAShares_TDX(str, type);
								str = "tdx_" + str + "," + todayDate;
							}
							
							// 处理停牌的情况，开高低收，四个价格应该都一样
							//if(items.get(i).equals("总量")){
							//	isHaltTrading = Integer.parseInt(str) == 0?1:0;
							//}
							
							// 流通股都乘以1亿
							if(items.get(i).equals("流通股(亿)") || items.get(i).equals("总股本(亿)")){
								Double floatingShares = safeParseDouble(str, 0.0)  * 10000 * 10000;
								DecimalFormat df = new DecimalFormat("0"); 
								
								//str = floatingShares.toString();
								str = df.format(floatingShares.doubleValue());
							}
							
							// 本来单位是“手”，这里改为股
							if(items.get(i).equals("总量")){
								Double volume = safeParseDouble(str, 0.0) * 100;
								DecimalFormat df = new DecimalFormat("0"); 
								
								//str = floatingShares.toString();
								str = df.format(volume.doubleValue());
							}
							
							if( i == items.size() - 1){
								output = output + str;
							}
							else{
								output = output + str + ",";
							}
							
						} // END of "for(int i = 0; i < items.size(); i++)"
						
						// 将output输出到文件中
						fw.write(output + "\n");
						
						// only for displaying
						if(jjj < 0){
							System.out.println(output);
							
						}
						jjj++;
					}
					else{
						//System.out.println("=========" + txt);
					} // END of "if(txtArr.length == numCol)"
					
				} // END of "if(isFirstLine == 1)"
				
			}   // END of "while((txt = br.readLine())!=null)"
			br.close();
			fw.close();
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * add suffix and prefix for A shares (only for function convertTDX2AB_Daily_temp)
	 * @param stockCode
	 * @param type
	 * @return
	 */
	private static String addSuffixPrefixForAShares_TDX(String stockCode, int type){
		// type:
		//	1 - Stock, structured fund, ETF
		//	2 - Index
		
		String output = "";
		String suffix = "";
		String prefix = "";
		
		String suffixSH = ".SH";
		String suffixSZ = ".SZ";
		String prefixSH = "SH";
		String prefixSZ = "SZ";
		
		if(type == 1)
		{
			if(stockCode.length() == 6){   // 处理六位数的情况，比如stockCode = "002123"
				String first2Charac = stockCode.substring(0, 2);
				String first3Charac = stockCode.substring(0, 3);
				
				if(first2Charac.equals("60")){
					suffix = suffixSH;
					//prefix = prefixSH;   // 如何制定suffix和prefix是开发者自己的事情
				}
				
				if(first2Charac.equals("00") || first2Charac.equals("30")){
					suffix = suffixSZ;
					//prefix = prefixSZ;   // 如何制定suffix和prefix是开发者自己的事情
				}
				
				if(first3Charac.equals("159") || first3Charac.equals("150") ){   // 159 - 深市的ETF，150 - 深市的分级基金
					suffix = suffixSZ;
				}
				
				if(first2Charac.equals("50") || first2Charac.equals("51")){
					suffix = suffixSH;
				}
				
			}
			if(stockCode.length() < 6){  // 比如原本code应该是 “000050”结果不知为何变成了“50”，这种情况不会发生在以60开头的股票上，也就是只可能发生在深市的股票上
				suffix = suffixSZ;
				//prefix = prefixSZ;
			}
		}
		
		if(type == 2){
			// 如果是指数的话，以000或者00开头的一般是上海的指数，以399开头的一般是深圳的指数
			String first3Charac = stockCode.substring(0, 3);
			
			if(first3Charac.equals("000")){
				suffix = suffixSH;
			}
			if(first3Charac.equals("399")){
				suffix = suffixSZ;
			}
			
			// 对于上证指数的一些特殊情况
			if(stockCode.equals("999999") || stockCode.equals("1A0001")){
				suffix = suffixSH;
				stockCode = "000001";
			}
		}
		
		output = prefix + stockCode + suffix;
		return output;
	}
	
	/**
	 * Remove space and tab within a string
	 * @param input
	 * @return
	 */
	public static String removeSpaceTab(String input){    // 移除空格和制表符
		String output = "";
		
		for(int i = 0; i < input.length(); i++){
			char c = input.charAt(i);
			int n = (int)c;
			
			if( !(n == 9 || n == 32) ) {  
				output = output + c;
			}
		}
		return output;
	}
	
	public static void beepNTimes(int N) throws Exception{
		Toolkit toolkit = Toolkit.getDefaultToolkit();
		for(int i = 0; i < N; i++) {
			toolkit.beep();
			Thread.sleep(100);
		}
	}
	
}
