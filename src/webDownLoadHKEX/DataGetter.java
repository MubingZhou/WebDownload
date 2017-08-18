package webDownLoadHKEX;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class DataGetter {
	public static StringBuilder params = new StringBuilder();
	
	public static boolean dataGetter(String stockCode, String dateStr) {
        boolean isOK = true;
        try {
        	// new url
            URL realUrl = new URL(Utils.HKEX_DATA_URL);
            
            //System.out.println("params = \n" + params);
        	
        	HttpURLConnection conn = (HttpURLConnection) realUrl.openConnection();
            // set properties
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            conn.setRequestMethod("POST");
            conn.setReadTimeout(30 * 1000); // set timeout
            
            // set parameters for POST
            Document doc = Jsoup.connect("http://www.hkexnews.hk/sdw/search/searchsdw.aspx").get();	
            setParams("__VIEWSTATE", doc.select("input#__VIEWSTATE").first().val());
            setParams("__VIEWSTATEGENERATOR", doc.select("input#__VIEWSTATEGENERATOR").first().val());
            setParams("__EVENTVALIDATION", doc.select("input#__EVENTVALIDATION").first().val());
    	    
            SimpleDateFormat sdf = new SimpleDateFormat(Utils.DATE_FORMAT); 
            SimpleDateFormat sdf_year = new SimpleDateFormat("yyyy"); 
            SimpleDateFormat sdf_month = new SimpleDateFormat("MM"); 
            SimpleDateFormat sdf_day = new SimpleDateFormat("dd"); 
            Date date = sdf.parse(dateStr);
            
        	setParams("txtStockCode", getRightStockCode(stockCode));
        	setParams("ddlShareholdingDay", sdf_day.format(date));
            setParams("ddlShareholdingMonth", sdf_month.format(date));
            setParams("ddlShareholdingYear", sdf_year.format(date));
            setParams("btnSearch.y", "15");
            setParams("btnSearch.x", "15");
            
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
            Utils.writeFile(conn.getInputStream(), Utils.OUTPUT_ROOT_PATH + "\\" + dateStr + "\\" + stockCode + ".html");
            
            conn.disconnect();
            
            //params.delete(0, params.length());
            params = null;
            params = new StringBuilder();

        } catch (Exception e) {
        	isOK = false;
            e.printStackTrace();
        }
        
        return isOK;

	}
	
	public static void setParams(String name, String value) throws Exception{
		if(params.length() != 0) { // not first value
			params.append('&');
		}
		
		params.append(URLEncoder.encode(name, "UTF-8"));
		params.append('=');
		params.append(URLEncoder.encode(value, "UTF-8"));
	}
	
	public static String getCookie(String url) throws IOException {
		URL website = new URL(url);
		URLConnection connection = website.openConnection();
		
		String cookie = connection.	getHeaderField("Set-Cookie");
        return cookie;
	}
	
	public static String getRightStockCode(String stockCode) {
		String rightSC = stockCode;
		
		for(int i = 0; i < 5 - stockCode.length(); i ++) {
			rightSC = "0" + rightSC;
		}
		
		return rightSC;
	}

}
