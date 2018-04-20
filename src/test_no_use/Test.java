package test_no_use;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.log4j.Logger;

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
			
			
	        
			//getPrice();
			//getSBFromHKEx();
			
//			ArrayList<String> stockListStrArr= utils.Utils.getSouthboundStocks("20180314", "yyyyMMdd", true, true);
//			System.out.println(stockListStrArr.indexOf("839"));
//			
//			
//			//utils.Utils.getSouthboundStocks("20180305", "yyyyMMdd", true, true);
//			//Thread.sleep(1000*3);
//			System.out.println("Read to play!");
//			
//			utils.PlayWAV.play("voices\\dingdong.wav");
//			utils.PlayWAV.play("voices\\0.wav");
//			utils.PlayWAV.play("voices\\1.wav");
//			utils.PlayWAV.play("voices\\2.wav");
//			
////			utils.PlayWAV.play("voices\\dingdong.wav");
////			utils.PlayWAV.play("voices\\1.wav");
////			utils.PlayWAV.play("voices\\2.wav");
////			utils.PlayWAV.play("voices\\3.wav");
//			
////			utils.PlayWAV.play("3_f.wav");
////			utils.PlayWAV.play("4_f.wav");
////			utils.PlayWAV.play("5_f.wav");
////			utils.PlayWAV.play("6_f.wav");
//			//utils.PlayWAV.play("voices\\0_f.wav");
////			utils.PlayWAV.play("8_f.wav");
////			utils.PlayWAV.play("9_f.wav");
//			System.out.println("Done!");
			
			//Runtime.getRuntime().exec( "shutdown -s -t 1");
			
//			Map<String, String> voiceMap_m = new HashMap<String, String> ();   // male voice
//			Map<String, String> voiceMap_f = new HashMap<String, String> ();   // female voice
//			String voiceF = "voices\\";
//			for(int i = 0; i <= 9; i++) {
//				String n = String.valueOf(i);
//				voiceMap_m.put(n, voiceF + n + ".wav");
//				voiceMap_f.put(n, voiceF + n + "_f.wav");
//			}
//			System.out.println("voice map=" + voiceMap_m.get("2"));
//			
//			ArrayList<String> thisBuyStocks = new ArrayList<String>();
//			thisBuyStocks.add("2689");
//			ArrayList<String> stockWachlist = new ArrayList<String>();
//			stockWachlist.add("26891");
//			if(thisBuyStocks.size() > 0) {
//				int repeatTimes = 1;
//				
//				for(int i = 0; i < repeatTimes; i++) {
//					   for(int j = 0; j < thisBuyStocks.size(); j++) {
//						   String stock = thisBuyStocks.get(j);
//						   char[] c = stock.toCharArray();	
//						   
//						   if(stockWachlist.indexOf(stock) == -1) {  // the stock is not in the watchlist
//							   for(int k = 0; k < c.length; k++) {
//									PlayWAV.play(voiceMap_m.get(String.valueOf(c[k])));
//								}
//						   }else {
//							   for(int k = 0; k < c.length; k++) {
//								   System.out.println("voice file: " + voiceMap_f.get(String.valueOf(c[k])));
//									PlayWAV.play(voiceMap_f.get(String.valueOf(c[k])));
//								}
//						   }
//							
//							Thread.sleep(500);
//							if(false && j < thisBuyStocks.size()-1) {
//								PlayWAV.play("tungLF.wav");
//								PlayWAV.play("maiLF.wav");
//							}
//							//Thread.sleep(1);
//							
//					   }
//					   
//					   if(i == 0 && repeatTimes > 1) {
//						   PlayWAV.play("chungLT.wav");
//							PlayWAV.play("fukHT.wav");
//							PlayWAV.play("1.wav");
//							PlayWAV.play("chiMR.wav");
//					   }
//					   	
//				   }
//			}
//			
//			//新建一个DatagramSocket
//	        DatagramSocket server = new DatagramSocket(5050);
//	        
//	        //接收客户端发送来的消息
//	        byte[] recvBuf = new byte[100];
//	        DatagramPacket recvPacket = new DatagramPacket(recvBuf, recvBuf.length);
//	        server.receive(recvPacket);
//	        String recvStr = new String(recvPacket.getData(), 0, recvPacket    .getLength());
//	        System.out.println("客户端传来消息:" + recvStr);
//	        
//	        //往客户端发送消息
//	        int port = recvPacket.getPort();
//	        InetAddress addr = recvPacket.getAddress();
//	        String sendStr = "Hello ! I'm Server";
//	        byte[] sendBuf;
//	        sendBuf = sendStr.getBytes();
//	        DatagramPacket sendPacket = new DatagramPacket(sendBuf, sendBuf.length,addr, port);
//	        server.send(sendPacket);
//	        
//	        //关闭DatagramSocket
//	        server.close();
//	        
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

	private static void getPrice() {
		try {
			String Stockpath = "Z:\\Mubing\\stock data\\temp stock list.csv";
			
			ArrayList<String> allStocks;
			BufferedReader bf_s = utils.Utils.readFile_returnBufferedReader(Stockpath);
			String line = bf_s.readLine();
			String[] lineArr1 = line.split(",");
			allStocks = new ArrayList<String>(Arrays.asList(lineArr1));
			bf_s.close();
			
			String bbg_stockDataPath = "Z:\\Mubing\\stock data\\temp stock data\\bbg data.csv";
			Map<String, ArrayList<Double>> bbgData = new HashMap<String, ArrayList<Double>>();
			BufferedReader bf_bbg = utils.Utils.readFile_returnBufferedReader(bbg_stockDataPath);
			line = "";
			int count = 0;
			while((line = bf_bbg.readLine()) != null) {
				if(count == 0) {
					count ++;
					continue;
				}
				
				String[] lineArr = line.split(",");
				String stock = lineArr[0];
				String vol = lineArr[1];
				String turn = lineArr[2];
				String close = lineArr[3];
				
				Double volDouble = 0.0;
				Double turnDouble = 0.0;
				Double closeDouble = 0.0;
				Double isSusp = 0.0;
				if(utils.Utils.isDouble(vol)) {
					volDouble = Double.parseDouble(vol);
					turnDouble = Double.parseDouble(turn);
					closeDouble = Double.parseDouble(close);	
				}else {
					isSusp = 1.0;
				}
				
				
				ArrayList<Double> d = new ArrayList<Double>();
				d.add(volDouble);
				d.add(turnDouble);
				d.add(closeDouble);
				d.add(isSusp);
				
				bbgData.put(stock, d);
			}
			
			String webbDataRootPath = "Z:\\Mubing\\stock data\\stock hist data - webb\\";
			String outputDataRootPath = "Z:\\Mubing\\stock data\\temp stock data\\";
			for(String stock : allStocks) {
				logger.info("stock=" + stock);
				
				String webbStockDataPath = webbDataRootPath + stock + ".csv";
				
				
				BufferedReader bf_oldData = utils.Utils.readFile_returnBufferedReader(webbStockDataPath);
				ArrayList<String> oldData = new ArrayList<String>();
				line = "";
				while((line = bf_oldData.readLine()) != null) {
					oldData.add(line);
				}
				bf_oldData.close();
				
				FileWriter fw = new FileWriter(outputDataRootPath + stock + ".csv");
				fw.write(oldData.get(0)); fw.write("\n");
				
				ArrayList<Double> stockBBgData = bbgData.get(stock);
				Double close = stockBBgData.get(2);
				Double vol = stockBBgData.get(0);
				Double turn = stockBBgData.get(1);
				Double isSusp = stockBBgData.get(3);
				
				fw.write("2018-2-5,2018-2-7," + isSusp + "," + close + ",1,1,1,1," + vol + "," + turn + ",1,1,1,1,1,1,1,1,1\n");
				for(int j = 1; j < oldData.size(); j++) {
					fw.write(oldData.get(j));
					fw.write("\n");
				}
				
				fw.close();
			}
			
			
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		
	}
	
	public static void getSBFromHKEx() {
		try {
			String Stockpath = "Z:\\Mubing\\stock data\\temp stock list.csv";
			
			ArrayList<String> allStocks;
			BufferedReader bf_s = utils.Utils.readFile_returnBufferedReader(Stockpath);
			String line = bf_s.readLine();
			String[] lineArr1 = line.split(",");
			allStocks = new ArrayList<String>(Arrays.asList(lineArr1));
			bf_s.close();
			
			Map<String, ArrayList<Double>> hkexData = new HashMap<String, ArrayList<Double>>();
			String hkexRootPath = "D:\\stock data\\HKEX\\2018-02-05\\";
			for(String stock : allStocks) {
				logger.info("stock = "+ stock);
				String dataPath = hkexRootPath + stock + ".csv";
				
				BufferedReader bf = utils.Utils.readFile_returnBufferedReader(dataPath);
				line = bf.readLine(); // skip first line
				boolean found3 = false;
				boolean found4 = false;
				while((line = bf.readLine()) != null) {
					String[] lineArr = line.split(",");
					if(lineArr.length == 0)
						continue;
					
					if(lineArr[0].equals("A00003"))
						found3 = true;
					if(lineArr[0].equals("A00004"))
						found4 = true;
					
					if(found3 || found4) {
						String name = lineArr[1];
						String holdStr = lineArr[2];
						
						String stakeStr = "0.0";
						if(lineArr.length >= 5)
							stakeStr  = lineArr[4];
						
						Double hold = Double.parseDouble(holdStr);
						Double stake = Double.parseDouble(stakeStr);
						
						ArrayList<Double> d = hkexData.get(stock);
						if(d == null) {
							d = new ArrayList<Double> ();
							d.add(0.0);
							d.add(0.0);
						}
						d.set(0, d.get(0) + hold);
						d.set(1, d.get(1) + stake);
						
						hkexData.put(stock, d);
					}
					
					if(found3 && found4)
						break;
					
					
				}
				bf.close();
				
			}
			
			String outputPath = "Z:\\Mubing\\stock data\\HK CCASS - WEBB SITE\\southbound\\combined\\2018-02-05";
			FileWriter fw = new FileWriter(outputPath);
			fw.write("Last Code,Issue,Holding,Value,Stake%,Date\n");
			for(String stock : hkexData.keySet()) {
				ArrayList<Double> d = hkexData.get(stock);
				
				fw.write(stock + ",," + d.get(0) + ",," + d.get(1) + ",5/2/2018\n" );
			}
			fw.close();
			
		}catch(Exception e) {
			e.printStackTrace();
		}
		
	}
	
	
	public static boolean ftpUploadFile(String url,int port,String username, String password, String path, String filename, InputStream input) {  
	    boolean success = false;  
	    FTPClient ftp = new FTPClient();  
	    try {  
	        int reply;  
	        ftp.connect(url, port);//连接FTP服务器  
	        //如果采用默认端口，可以使用ftp.connect(url)的方式直接连接FTP服务器  
	        ftp.login(username, password);//登录  
	        reply = ftp.getReplyCode();  
	        if (!FTPReply.isPositiveCompletion(reply)) {  
	            ftp.disconnect();  
	            return success;  
	        }
	        System.out.println("Login successful!");
	        
	        ftp.changeWorkingDirectory(path);  
	        ftp.storeFile(filename, input);           
	          
	        input.close();  
	        ftp.logout();  
	        success = true;  
	    } catch (IOException e) {  
	        e.printStackTrace();  
	    } finally {  
	        if (ftp.isConnected()) {  
	            try {  
	                ftp.disconnect();  
	            } catch (IOException ioe) {  
	            }  
	        }  
	    }  
	    return success;  
	}  

}

