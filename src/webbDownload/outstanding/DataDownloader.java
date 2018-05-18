package webbDownload.outstanding;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class DataDownloader { // downloading southbound CCASS data
	public static String WEBB_URL_OUTSTANDING = "https://webb-site.com/dbpub/outstanding.asp?sc=";
	public static String FILE_OUTPUT_PATH = 
			utils.PathConifiguration.HK_STOCK_OUTSTANDING_DATA;
	public static String ALL_STOCK_LIST_PATH = 
			utils.PathConifiguration.STOCK_DATA_ROOT_PATH + "\\HK CCASS - WEBB SITE\\outstanding stock list.csv";
	private static boolean isTrustedAllCertificates = false;
	
	/**
	 * Downloading outstanding data for all stocks
	 * Stock list stored in DataDownloader.ALL_STOCK_LIST_PATH
	 * Downloaded file saved in DataDownloader.FILE_OUTPUT_PATH
	 */
	public static void dataDownloader() {  // downloading by stock
		try{
			
			BufferedReader bf = 
					utils.Utils.readFile_returnBufferedReader(ALL_STOCK_LIST_PATH);
			String bf_line = bf.readLine();
			ArrayList<String> allStockList = 
					new ArrayList<String>(Arrays.asList(bf_line.split(",")));
			
			dataDownloader(allStockList);
			
			
		}catch(Exception e){ // end of "try"
			e.printStackTrace();
		}
	}
	
	/**
	 * Downloaded file saved in DataDownloader.FILE_OUTPUT_PATH
	 * @param allStockList
	 * @return
	 */
	public static boolean dataDownloader(ArrayList<String> allStockList) {
		boolean isOK  = true;
		try {
			utils.Utils.trustAllCertificates();
			isTrustedAllCertificates = true;
			
			ArrayList<String> failedList = new ArrayList<String>();
			while(allStockList != null && allStockList.size() > 0) {
				failedList = new ArrayList<String>();
				System.out.println("******* # of stock = " + allStockList.size() + " *********");
				for(int i = 0; i < allStockList.size(); i++){
					String stockCode = allStockList.get(i);
					
					boolean isOK2 = dataDownloader(stockCode);
					if(!isOK2)
						failedList.add(stockCode);
					
					Thread.sleep((long) (Math.random() * 1000 + 1000));
				}// end of for
				allStockList = failedList;
			} // end of while
			
		}catch(Exception e){ // end of "try"
			e.printStackTrace();
			isOK = false;
		}
		
		isTrustedAllCertificates = false;
		return isOK;
	}
	
	/**
	 * Downloaded file saved in DataDownloader.FILE_OUTPUT_PATH
	 * @param stockCode
	 * @return
	 */
	public static boolean dataDownloader(String stockCode) {
		boolean isOK  = true;
		try {
			System.out.println("========== stock = " + stockCode + " ============");
			if(!isTrustedAllCertificates)
				utils.Utils.trustAllCertificates();
			
			// write
			utils.Utils.checkDir(FILE_OUTPUT_PATH);
			FILE_OUTPUT_PATH = utils.Utils.addBackSlashToPath(FILE_OUTPUT_PATH);
			FileWriter fw = new FileWriter( FILE_OUTPUT_PATH + stockCode + ".csv");
			
			String urlStr = WEBB_URL_OUTSTANDING + stockCode;
			URL url = new URL(urlStr);
			
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setReadTimeout(30 * 1000); // 30s time out
			connection.setRequestProperty("User-Agent", "Mozilla/31.0 (compatible; MSIE 10.0; Windows NT; DigExt)");
			if(HttpURLConnection.HTTP_OK != connection.getResponseCode()){ // get connection
				System.out.println("not connected! (Downloading outstanding shares)");
				return false;
			}  
			InputStream inputStream = connection.getInputStream();
			
			// output file path
			String outputFilePath = FILE_OUTPUT_PATH + stockCode + ".html";
			
			//store html
			utils.Utils.storeHTML(inputStream, outputFilePath);
			
			//write to csv
			String outputCSVPath = FILE_OUTPUT_PATH + stockCode + ".csv";
			convertHTML2CSV_outstandingShares(outputFilePath, outputCSVPath);
		}catch(Exception e){ // end of "try"
			e.printStackTrace();
			isOK = false;
		}
		
		return isOK;
	}
	
	/**
	 * convert HTML to csv. inputFilePath & outputFilePath should be the full path
	 * @param inputFilePath
	 * @param outputFilePath
	 */
	private static void convertHTML2CSV_outstandingShares(String inputFilePath, String outputFilePath) {
		try {
			if(!isTrustedAllCertificates)
				utils.Utils.trustAllCertificates();
			
			// write file
			FileWriter fw2 = new FileWriter(outputFilePath);
						
			//read html
			File html_read = new File(inputFilePath);
			Document doc = (Document) Jsoup.parse(html_read, "utf-8", "");
			
			///////////////  get stock holdings data ////////////////////
			Elements html_table = doc.getElementsByClass("numtable");
			//System.out.println(html_table);
			Elements html_table_tr = html_table.get(1).select("tr");  // the 2nd table  
								
			int counter2 = 0;
			for(Element data_tr:html_table_tr ){
				Elements data_td = data_tr.getElementsByTag("td");
				
				String to_write_str = "";
				if(counter2 == 0){
					to_write_str = "Date,Securities,Change,Price,Price Date,Market Cap,Pending Securities,Pending Mkt Cap\n";
				}
				else{
					String date = data_td.get(0).text().replace(",", "");
					String securities = data_td.get(1).text().replace(",", "");
					String change = data_td.get(2).text().replace(",", "");
					String price = data_td.get(3).text().replace(",", "");
					String priceDate = data_td.get(4).text().replace(",", "");
					String mktCapMStr = data_td.get(5).text().replace(",", "");
					String pendingSecurities = data_td.get(6).text().replace(",", "");
					String pendingMktCapMStr = data_td.get(7).text().replace(",", "");
					
					Double mktCap = 0.0;
					Double pendingMktCap = 0.0;
					try{
						mktCap = Double.parseDouble(mktCapMStr) * 1000000.0;
						pendingMktCap = Double.parseDouble(pendingMktCapMStr) * 1000000.0;
					}catch(Exception e) {
						
					}
					String mktCapStr = String.valueOf(mktCap);
					String pendingMktCapStr = String.valueOf(pendingMktCap);
					
					to_write_str = date + "," + securities + "," + change + "," + price + ","
									+ priceDate + "," + mktCapStr  + "," + pendingSecurities + "," + pendingMktCapStr + "\n";
				}
				counter2++;
				
				fw2.write(to_write_str);
			}
			fw2.close();
		}catch(Exception e) {
			e.printStackTrace();
			System.out.print("Extract HTML error: input: " + inputFilePath + " output: " + outputFilePath + "\n");
		}
		
	}

}

