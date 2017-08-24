package utils;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class Utils {
	// OUTPUT root path
	public static final String OUTPUT_ROOT_PATH_HKEX = "D:\\stock data\\HKEX";
	
	// csv file storing all brokers' names & short names
	public static final String BROKER_NAME_FILE_PATH = "D:\\stock data\\CCASS Participants List.csv";
	
	// csv file storing all trading date
	public static final String TRADING_DATE_FILE_PATH = "D:\\stock data\\all trading date.csv";
	
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
			InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "utf-8");
			//System.out.println("here15847");
			
			BufferedReader bufReader = new BufferedReader(inputStreamReader);
			
			FileWriter fw = new FileWriter(outputPath);
			String line = "";
			StringBuilder contentBuf = new StringBuilder();
			int counter = 0;
			while ((line = bufReader.readLine()) != null) {
				contentBuf.append(line);
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
		 * but to show is correctly in csv file, you have to use "=BDH(""1 HK Equity"",""EQ_SH_OUT"", ""20170801"", ""20170818"")" 
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
	  
}