package utils;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
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
                //设置超时间为3秒  
        conn.setConnectTimeout(3*1000);  
        //防止屏蔽程序抓取而返回403错误  
        conn.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");  
  
        //得到输入流  
        InputStream inputStream = conn.getInputStream();    
        //获取自己数组  
        byte[] getData = readInputStream(inputStream);      
  
        //文件保存位置  
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
		
		public static BufferedReader readFile_returnBufferedReader(String filePath) throws Exception{
			ArrayList<String> data = new ArrayList<String> ();
			
			InputStream input = new FileInputStream(new File(filePath));
			InputStreamReader inputStreamReader = new InputStreamReader(input);
			BufferedReader bufReader = new BufferedReader(inputStreamReader);
			
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
		
		public static String date2Str(Calendar cal, String dateFormat) throws Exception{
			SimpleDateFormat sdf =   new SimpleDateFormat(dateFormat);
			
			return sdf.format(cal.getTime());
		}
	  
}
