package webDownload;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import utils.Utils;

public class GetPrice { // download stock data (price, vol etc.) from webb-site
	private static String histDownloadURL = "";
	private static Map<String, String> histDownloadURL_map = new HashMap<String, String>();  // stock code - downloading url
	public static String outFilePath = "Z:\\Mubing\\stock data\\stock hist data - webb";
	public static String allStockListPath = "Z:\\Mubing\\stock data\\all stock list.csv";  //"Z:\\Mubing\\stock data\\all stock list.csv"
	
	public static void main(String[] args) {
		try {
			//getHistoricalData("2098", "2098.csv", filePath);
			
			//Thread.sleep(1000 * 3600 * 4);
			
			
			downloadData_2()	;
			/*
			ArrayList<String> stockCodeList = WebDownload.getCGITopHoldingStocks(allStockListPath);
			//List<String> stockCodeList = stockCodeList0.subList(100, stockCodeList0.size() );
			
			// to download all stocks
			while(stockCodeList.size() > 0) {
				ArrayList<String> failedList = new ArrayList<String>() ; 
				
				
				for(int i = 0; i < stockCodeList.size(); i++ ) {
					String stockCode = stockCodeList.get(i);
					System.out.println("=========== i = " + i + "/" + stockCodeList.size() + " " + stockCode + " ================");
					
					String fileName = stockCode + ".csv";
					//String outFilePath = "D:\\stock data\\stock hist data - webb";
					
					boolean isOK = getHistoricalData(stockCode, fileName, outFilePath);
					if(!isOK) {
						failedList.add(stockCode);
					}
				}
				stockCodeList = new ArrayList<String>(failedList);
				
				if(failedList.size() > 0) {
					System.out.println("******** total failed stock: " + failedList.size() + " *********");
				}
			}
			*/
			
		}catch(Exception e) {
			e.printStackTrace();
		}
		
	}
	
	/**
	 * download stock data (price, volume, etc) from Webb-site.com
	 * @param stockCode
	 */
	public static boolean getHistoricalData(String stockCode, String outputFileName, String outputPath) {
		boolean isOK = true;
		
		Utils.trustAllCertificates();
		
		String urlStr = "https://webb-site.com/dbpub/hpu.asp?sc=" + stockCode + "&s=datedn";
		
		try {
			// connect to the website
			URL url = new URL(urlStr);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestProperty("User-Agent", "Mozilla/31.0 (compatible; MSIE 10.0; Windows NT; DigExt)");
			connection.setReadTimeout(30 * 1000); // timeout = 30s
			
			BufferedReader bufReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			
			histDownloadURL ="https://webb-site.com/dbpub/"; // url to download file
			String line ;
			//int counter = 0;
			while ((line = bufReader.readLine()) != null) {
				String targetStr = "pricesCSV.asp?i=";
				if(line.contains(targetStr)) {
					//System.out.println("url.lastIndexOf(targetStr) = " + line.lastIndexOf(targetStr));

					String leftStr = line.substring(line.lastIndexOf(targetStr) + targetStr.length());
					//System.out.println(leftStr);
					
					int stopInd=0;
					byte[] leftStrByte = leftStr.getBytes(); 
					for(int i = 0; i < leftStrByte.length; i++) {
						if(leftStrByte[i] == "\"".getBytes()[0]) {
							stopInd = i;
							break;
						}
					}
					
					histDownloadURL = histDownloadURL + targetStr + leftStr.substring(0, stopInd);
					System.out.println("-- get toDownloadUrl = " + histDownloadURL);
					histDownloadURL_map.put(stockCode, histDownloadURL);   //更新map
					break;
				}
			} // end of while
			
			Thread downloader = new Thread(new Runnable(){
				   public void run(){
					   //ordersMonitor();
					   try {
						Utils.downLoadFromUrl(histDownloadURL,outputFileName,outputPath);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				   }
			});
			downloader.run();
			
			
			/*
			CloseableHttpClient httpClient = HttpClients.createDefault();
			HttpGet httpGet = new HttpGet(url);
			CloseableHttpResponse response = httpClient.execute(httpGet);
		
			HttpEntity entity = response.getEntity();
			if(entity != null) {
				InputStream inputStream = entity.getContent();
				
				BufferedReader bufReader = new BufferedReader(new InputStreamReader(inputStream));
				
				String toDownloadUrl ="https://webb-site.com/dbpub/"; // url to download file
				
				String line ;
				//int counter = 0;
				while ((line = bufReader.readLine()) != null) {
					String targetStr = "pricesCSV.asp?i=";
					if(line.contains(targetStr)) {
						//System.out.println("url.lastIndexOf(targetStr) = " + line.lastIndexOf(targetStr));

						String leftStr = line.substring(line.lastIndexOf(targetStr) + targetStr.length());
						//System.out.println(leftStr);
						
						int stopInd=0;
						byte[] leftStrByte = leftStr.getBytes(); 
						for(int i = 0; i < leftStrByte.length; i++) {
							if(leftStrByte[i] == "\"".getBytes()[0]) {
								stopInd = i;
								break;
							}
						}
						
						toDownloadUrl = toDownloadUrl + targetStr + leftStr.substring(0, stopInd);
						//System.out.println("toDownloadUrl = " + toDownloadUrl);
						break;
					}
				} // end of while
				
				Utils.downLoadFromUrl(toDownloadUrl,outputFileName,outputPath);
				
			}*/
		}catch(Exception e) {
			e.printStackTrace();
			isOK = false;
		}
		
		return isOK;
	}

	public static void downloadData_2() {
		try {
			// get the map
			String mapPath = outFilePath + "\\stock hist data - map.csv";
			//String mapPath = "Z:\\Mubing\\stock data\\all sb stocks - webb map.csv";
			File mapFile = new File(mapPath);
			if(mapFile.exists()) {
				//histDownloadURL_map;
				BufferedReader bf = utils.Utils.readFile_returnBufferedReader(mapPath);
				String line = "";
				int count = 0;
				while((line = bf.readLine()) != null) {
					String[] sArr = line.split(",");
					
					//if(count >= 1250)
					histDownloadURL_map.put(sArr[0], sArr[1]);
					
					count++;
				}
				bf.close();
			}
			
			ArrayList<String> stocksInMap = new ArrayList(histDownloadURL_map.keySet());  //已经有url的stocks
			
			ArrayList<String> stockCodeList = WebDownload.getCGITopHoldingStocks(allStockListPath);
			stockCodeList.clear();
			stockCodeList.addAll(stocksInMap);
			
			//stockCodeList = new ArrayList<String>( stockCodeList.subList(2100,stockCodeList.size()) );
			//List<String> stockCodeList = stockCodeList0.subList(100, stockCodeList0.size() );
			
			// to download all stocks
			while(stockCodeList.size() > 0) {
				ArrayList<String> failedList = new ArrayList<String>() ; 
				
				
				for(int i = 0; i < stockCodeList.size(); i++ ) {
					String stockCode = stockCodeList.get(i);
					System.out.println("=========== i = " + i + "/" + stockCodeList.size() + " " + stockCode + " ================");
					
					//------------ temp code --------------
					if(false) {
						File f = new File("Z:\\Mubing\\stock data\\stock hist data - webb\\" + stockCode + ".csv");
						if(f.exists()) {
							System.out.println("   Already existed");
							continue;   //已经存在的不用下载
						}
					}
					
					// ------------ temp code end ----------------
					
					boolean isOK = true;
					// 如果已经存在于list中
					if(stocksInMap.indexOf(stockCode) > -1) {
						histDownloadURL = histDownloadURL_map.get(stockCode);
						
						Thread downloader = new Thread(new Runnable(){
							   public void run(){
								   //ordersMonitor();
								   try {
									Utils.downLoadFromUrl(histDownloadURL,stockCode + ".csv",outFilePath);
								   } catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
									failedList.add(stockCode);
								}
							   }
						});
						downloader.run();
					}else {  //不存在于map中，需要读取网页，并取得map
						String fileName = stockCode + ".csv";
						//String outFilePath = "D:\\stock data\\stock hist data - webb";
						
						isOK = getHistoricalData(stockCode, fileName, outFilePath);
					}
					
					
					if(!isOK) {
						failedList.add(stockCode);
					}
				}
				stockCodeList = new ArrayList<String>(failedList);
				
				if(failedList.size() > 0) {
					System.out.println("******** total failed stock: " + failedList.size() + " *********");
				}
			}
			
			//update the map
			FileWriter fw = new FileWriter(mapPath);
			for(String stockCode : histDownloadURL_map.keySet()) {
				fw.write(stockCode + "," + histDownloadURL_map.get(stockCode) + "\n");
			}
			fw.close();
			
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
}
