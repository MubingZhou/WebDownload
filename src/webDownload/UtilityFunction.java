package webDownload;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class UtilityFunction {
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
}
