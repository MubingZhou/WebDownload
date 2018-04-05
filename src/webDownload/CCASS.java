package webDownload;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class CCASS {
	public static String CCASS_BASE_URL = "https://webb-site.com/ccass/ctothist.asp?sc=";
	public static String CCASS_DATA_ROOT_PATH = "Z:\\Mubing\\stock data\\HK CCASS - WEBB SITE\\CCASS holding";
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		downloadCCASSHolding_main(CCASS_DATA_ROOT_PATH);
	}
	
	public static void downloadCCASSHolding_main(String rootPath) {
		UtilityFunction.trustAllCertificates();
		
		try {
			String stocklistPath = "Z:\\Mubing\\stock data\\HK CCASS - WEBB SITE\\CCASS stock list - row.csv";
			BufferedReader bf = utils.Utils.readFile_returnBufferedReader(stocklistPath);
			String line = bf.readLine();
			String[] stocklist_strArr = line.split(",");
			ArrayList<String> stockList = new ArrayList<String>(Arrays.asList(stocklist_strArr));
//			stockList.clear();
//			stockList.add("2312");
//			stockList.add("1488");
			ArrayList<String> failedStockList = new ArrayList<String>();
			
			while(stockList.size() > 0) {
				failedStockList.clear();
				
				for(int i = 0; i < stockList.size(); i++) {
					String stockCode = stockList.get(i);
					System.out.println("============= i=" + i + "/" + stockList.size() + " stock=" + stockCode + " =========="); 
					
					boolean isDownloadOK = downloadCCASSHolding_HTML(rootPath, stockCode);
					//boolean isDownloadOK = true;
					if(isDownloadOK) {
						boolean isConvertOK = extractHTMLByStock(rootPath, stockCode, rootPath);
						if(!isConvertOK) {
							System.out.println("---- Convert unsuccessful! stock=" + stockCode + " --------");
							failedStockList.add(stockCode);
						}
					}else {
						System.out.println("---- Download unsuccessful! stock=" + stockCode + " --------");
						failedStockList.add(stockCode);
					}
				}
				
				stockList.clear();
				stockList.addAll(failedStockList);
			}
			
			
			
		}catch(Exception e) {
			e.printStackTrace();
		}
		
	}
	
	/**
	 * To download html file from Webb
	 * @param outputRootPath
	 * @param stockCode
	 * @return
	 */
	public static boolean downloadCCASSHolding_HTML(String outputRootPath, String stockCode) {
		boolean isOK = true;
		try {
			String url_str = CCASS_BASE_URL + stockCode;
			
			// get url
			System.out.println(url_str);
			URL webb_url = new URL(url_str);
			
			// connect to the website
			HttpURLConnection connection = (HttpURLConnection) webb_url.openConnection();
			connection.setRequestProperty("User-Agent", "Mozilla/31.0 (compatible; MSIE 10.0; Windows NT; DigExt)");
			connection.setReadTimeout(30 * 1000); // timeout = 30s
			if(HttpURLConnection.HTTP_OK != connection.getResponseCode()){ // get connection
				isOK = false;
				return isOK;
			}  
			InputStream inputStream = connection.getInputStream();
			//InputStream inputStream = downloadWebpage(webb_url);
			/*if(inputStream == null){
				isOK = false;
				return isOK;
			}*/
			
			String html_output_path = utils.Utils.addBackSlashToPath(outputRootPath) + stockCode + ".html";
			
			//store Html
			UtilityFunction.storeHTML(inputStream,html_output_path);
			
			connection.disconnect();  // disconnect from the website
			
		}catch(Exception e) {
			e.printStackTrace();
			isOK = false;
		}
		
		return isOK;
	}
	
	public static Boolean extractHTMLByStock(String dataReadRootPath, String stockCode, String dataOutputRootPath){
		//String html_output_path = ConstVal.FILE_OUTPUT_PATH + "\\" + stockCode + ".html";
		Boolean isOK= true;
		try {
				/////////// parse html and rewrite data /////////////////
				// write file
				String str_output_path = utils.Utils.addBackSlashToPath(dataOutputRootPath) + stockCode + ".csv";
				FileWriter fw2 = new FileWriter(str_output_path);
				
				//parse html
				File html_read = new File(utils.Utils.addBackSlashToPath(dataOutputRootPath) + stockCode + ".html");
				Document doc = (Document) Jsoup.parse(html_read, "utf-8", "");
				
//				// get title
//				Elements html_title = doc.getElementsByTag("h2");
//				String title = html_title.get(0).text();
//				//title = title.replace("CCASS holdings: ", "");
//				System.out.println(title);
				
				///////////////  get stock holdings data ////////////////////
				Elements html_table = doc.getElementsByClass("numtable");
				//System.out.println(html_table);
				Elements html_table_tr = html_table.get(1).select("tr");  // the 2nd table contains main info
				
				// get stakes in CCASS 
				int counter2 = 0;
				for(Element data_tr:html_table_tr ){
					Elements data_td = data_tr.getElementsByTag("td");
					//System.out.println("data_td=" + data_td);
					
					String to_write_str = "";
					if(counter2 == 0){  // skip first line
						Elements data_th = data_tr.getElementsByTag("th");
						to_write_str = data_th.get(0).text().replace(",", "") + "," 
								+ data_th.get(1).text().replace(",", "") + ","
								+ data_th.get(2).text().replace(",", "") + ","
								+ data_th.get(3).text().replace(",", "") + ","
								+ data_th.get(4).text().replace(",", "") + ","
								+ data_th.get(5).text().replace(",", "") + ","
								+ data_th.get(6).text().replace(",", "") + ","	
								+ data_th.get(7).text().replace(",", "") + "\n";
						
					}
					else{
						to_write_str = data_td.get(0).text().replace(",", "") + "," 
										+ data_td.get(1).text().replace(",", "") + ","
										+ data_td.get(2).text().replace(",", "") + ","
										+ data_td.get(3).text().replace(",", "") + ","
										+ data_td.get(4).text().replace(",", "") + ","
										+ data_td.get(5).text().replace(",", "") + ","
										+ data_td.get(6).text().replace(",", "") + ","	
										+ data_td.get(7).text().replace(",", "") + "\n";
						
						//Thread.sleep(1000 * 3);
					}
					counter2++;
					
					fw2.write(to_write_str);
				}
				fw2.close();
		}catch(Exception e) {
			e.printStackTrace();
			isOK = false;
		}
		return isOK;
	}

}
