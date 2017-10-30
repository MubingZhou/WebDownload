package webDownload;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import utils.Utils;

public class GetPrice { // download stock data (price, vol etc.) from webb-site
	
	public static void main(String[] args) {
		try {
			//getHistoricalData("2098", "2098.csv", filePath);
			
			ArrayList<String> stockCodeList = WebDownload.getCGITopHoldingStocks("D:\\stock data\\all stock list.csv");
			//List<String> stockCodeList = stockCodeList0.subList(100, stockCodeList0.size() );
			
			// to download all stocks
			while(stockCodeList.size() > 0) {
				ArrayList<String> failedList = new ArrayList<String>() ; 
				
				
				for(int i = 0; i < stockCodeList.size(); i++ ) {
					String stockCode = stockCodeList.get(i);
					System.out.println("=========== i = " + i + "/" + stockCodeList.size() + " " + stockCode + " ================");
					
					String fileName = stockCode + ".csv";
					//String outFilePath = "D:\\stock data\\stock hist data - webb";
					String outFilePath = "Z:\\Mubing\\stock data\\stock hist data - webb";
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
					System.out.println("-- get toDownloadUrl = " + toDownloadUrl);
					break;
				}
			} // end of while
			
			Utils.downLoadFromUrl(toDownloadUrl,outputFileName,outputPath);
			
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

	//public static ArrayList<String> getHistoricalDataSinceDate(Str)
}
