package test_no_use;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.net.*;

@SuppressWarnings("unused")
public class Test {

	 //static Logger logger = LogManager.getLogger(Test.class.getName());
	 static Logger logger = Logger.getLogger(Test.class.getName());
	 static int i = 0;
			
	 public static StringBuilder params = new StringBuilder();
	 
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd"); 
		try {			

			// test date
			ArrayList<String> stockListStrArr= utils.Utils.getHSCEI_HSIStocks("20171124", "yyyyMMdd", true, true);
			for(String s : stockListStrArr) {
				System.out.print("s=" + s);
				if(s.equals("1997")) {
					System.out.print("---------------");
				}
				System.out.print("\n");
			}
			
			
			
			//新建一个DatagramSocket
	        DatagramSocket server = new DatagramSocket(5050);
	        
	        //接收客户端发送来的消息
	        byte[] recvBuf = new byte[100];
	        DatagramPacket recvPacket = new DatagramPacket(recvBuf, recvBuf.length);
	        server.receive(recvPacket);
	        String recvStr = new String(recvPacket.getData(), 0, recvPacket    .getLength());
	        System.out.println("客户端传来消息:" + recvStr);
	        
	        //往客户端发送消息
	        int port = recvPacket.getPort();
	        InetAddress addr = recvPacket.getAddress();
	        String sendStr = "Hello ! I'm Server";
	        byte[] sendBuf;
	        sendBuf = sendStr.getBytes();
	        DatagramPacket sendPacket = new DatagramPacket(sendBuf, sendBuf.length,addr, port);
	        server.send(sendPacket);
	        
	        //关闭DatagramSocket
	        server.close();
	        
//	        
//			String url = "http://www.cninfo.com.cn/cninfo-new/index";
//			URL realUrl = new URL(url);
//            
//            //System.out.println("params = \n" + params);
//        	
//        	HttpURLConnection conn = (HttpURLConnection) realUrl.openConnection();
//            // set properties
//            conn.setRequestProperty("accept", "*/*");
//            conn.setRequestProperty("connection", "Keep-Alive");
//            conn.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
//            conn.setRequestMethod("POST");
//            conn.setReadTimeout(30 * 1000); // set timeout
//            
//            Document doc = Jsoup.connect(url).get();	
//            //setParams("__VIEWSTATE", doc.select("input#__VIEWSTATE").first().val());
//            //setParams("__VIEWSTATEGENERATOR", doc.select("input#__VIEWSTATEGENERATOR").first().val());
//            //setParams("__EVENTVALIDATION", doc.select("input#__EVENTVALIDATION").first().val());
//            
//			setParams("index_hq_input_obj", "600519");
//            setParams("hq_start_select_obj", "2017");
//            setParams("hq_end_select_obj", "2018");
//            //setParams("btnSearch.y", "15");
//            //setParams("btnSearch.x", "15");
//          
//            
//            conn.setRequestProperty( "Content-Length", Integer.toString( params.toString().getBytes("utf-8").length ));
//            conn.setRequestProperty("Cookie", getCookie("http://www.cninfo.com.cn/cninfo-new/index"));
//            
//            // for POST
//            conn.setDoOutput(true);
//            conn.setDoInput(true);
//            
//            // sent parameters
//            DataOutputStream dataOutputStream = new DataOutputStream( conn.getOutputStream()); 
//            dataOutputStream.write(params.toString().getBytes("utf-8"));
//            dataOutputStream.flush();
//            
//            //write out the response
//            webDownLoadHKEX.Utils.writeFile(conn.getInputStream(), "D:\\test.html");
//            
//            conn.disconnect();
//            
			
		}catch(Exception e) {
			e.printStackTrace();
		}
		
	}
	
	
	private static void test1(int i, int[] arr) {
		arr[0] += i;
	}

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

