package webDownload;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import cgi.ib.avat.AVAT;



public class WebDownload {
	//to store holders whose info has already been downloaded
	private static ArrayList<String> downloadedHolders = new ArrayList<String>();
	
	// num of brokers whose info is in the need for each stock
	private static final int numOfBrokers = 10; 
	
	// num of stocks to consider for CGI, will be determined later
	//private static final int numOfCGIStocks = 0;
	
	// if need to download holders' holding info
	private static final boolean toDownloadHolderInfo = false;
	
	// directories
	private static String dateDir;
	private static String holderDir;
	
	//
	private static String httpsConnectionAgent = "Mozilla/31.0 (compatible; MSIE 10.0; Windows NT; DigExt)";
		
	public static void main(String[] args) {
		long startTime = System.currentTimeMillis();    //��ȡ��ʼʱ��
		System.out.println("******************** Web Download ********************");
		try {
			String[] dates = {"2018-02-06"};
			//String[] dates = {"2017-07-28","2017-07-31","2017-08-01","2017-08-02","2017-08-03"};
			
			ArrayList<Date> allTradingDate = utils.Utils.getAllTradingDate();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			Date startDate = utils.Utils.getMostRecentDate(sdf.parse("2015-01-01"), allTradingDate);
			Date endDate = utils.Utils.getMostRecentDate(sdf.parse("2015-01-31"), allTradingDate);
			int startDateInd = allTradingDate.indexOf(startDate);
			int endDateInd = allTradingDate.indexOf(endDate);
			
			for(int i = startDateInd; i < endDateInd; i++) {
				String date = sdf.format(allTradingDate.get(i));
				
				Thread t = new Thread(new Runnable(){
					   public void run(){
						   try {
							   System.out.println("********* date = " + date + " **********");
							   downloadMain(date);
						} catch (Exception e) {
							e.printStackTrace();
						}
					   }
				});
				t.start();
				
			}
			
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		long endTime = System.currentTimeMillis();    //��ȡ����ʱ��
		System.out.println("Total running time: " + (endTime - startTime)/1000 + "s");    //�����������ʱ��

	}
	
	/**
	 * Main func to handle the downloading
	 * @param date
	 * @throws Exception
	 */
	public static void downloadMain(String date) throws Exception{
		UtilityFunction.trustAllCertificates();
		
		// creating directories
		dateDir = getDownloadedWebpageFilePath(date);
		File dateDir_file = new File(dateDir);
		if(!dateDir_file.exists() && !dateDir_file.isDirectory()){
			dateDir_file.mkdir();
		}
		holderDir = dateDir + "\\holders";
		File holderDir_file = new File(holderDir);
		if(!holderDir_file.exists() && !holderDir_file.isDirectory()){
			holderDir_file.mkdir();
		}
		
		//ArrayList<String> stockCodeList = getCGITopHoldingStocks(ConstVal.FILE_OUTPUT_PATH + "\\cgi stock list.csv");
		String stockListPath = "Z:\\Mubing\\stock data\\HK CCASS - WEBB SITE\\outstanding stock list.csv";
		ArrayList<String> stockCodeList = getCGITopHoldingStocks(stockListPath);
	
	////////////// downloading webpage and parse /////////////////
		//double fixedTimePeriod = 5; //5s
		
		int counter = 0;
		while(stockCodeList.size() > 0 || counter == 0) {
			counter ++;
			ArrayList<String> failedStocks = new ArrayList<String>();
			
			for(int i = 0; i < stockCodeList.size(); i++){
				long startTime_i = System.currentTimeMillis();
				
				String stockCode = stockCodeList.get(i);
				
				System.out.println("======== i = " + i + "/" + String.valueOf(stockCodeList.size()) +", stock code = " + stockCode + " " + date + " ==========");
				Boolean isDownloadOK = downloadWebpageByStock(stockCode, date);
				
				if(isDownloadOK){
					Boolean isConvertOK = extractHTMLByStock(stockCode, date);
					if(!isConvertOK){
						System.out.println("Converting unsuccessful!");
						failedStocks.add(stockCode);
					}
				}else{
					System.out.println("Downloading unsuccessful!");
					failedStocks.add(stockCode);
				}	
				
				// pause for a random number of secs
				Thread.sleep((long) (Math.random()*3 + 2) * 1000);
				
				long endTime_i = System.currentTimeMillis();    //��ȡ����ʱ��
				double runningTime = ((double) endTime_i - startTime_i) / 1000; // s
				System.out.println("======== running time"  + runningTime + "s ==========");    //�����������ʱ��
				
			} // end of "for"
			
			// failed stocks exist
			if(failedStocks.size() > 0) {
				System.out.println("==========  num of failed stocks: " + failedStocks.size() + " ===========");
			}
			stockCodeList = new ArrayList<String>(failedStocks);
			
		} // end of "While"
		
	}
	
	/**
	 * download the CCASS holding by stock code date, i.e. we will get the holdings of every
	 * broker on this stock on this date 
	 * @param stockCode
	 * @param date
	 * @return return true if downloading successful
	 */
	public static Boolean downloadWebpageByStock(String stockCode, String date){
		Boolean isOK = true;
		try{			
			// get url
			String url_str = ConstVal.WEBB_URL_BY_STOCK + "sc=" + stockCode 
					+ "&d=" + date + "&sort=holddn"; // get url and sort by holdings descending
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
			
			// creating file directory
			String html_output_dir = getDownloadedWebpageFilePath(date);
			File html_output_dir_file = new File(html_output_dir);
			if(!html_output_dir_file.exists() && !html_output_dir_file.isDirectory()){
				html_output_dir_file.mkdir();
			}
			String html_output_path = html_output_dir + "\\" + stockCode + ".html";
			
			//store Html
			UtilityFunction.storeHTML(inputStream,html_output_path);
			
			connection.disconnect();  // disconnect from the website
		}catch(Exception e){
			e.printStackTrace();
			isOK = false;
		}
		return isOK;
	}
	
	/**
	 * Convert the downloaded html to csv (extracting the main table)
	 * Will also download holder's holding data
	 * @param stockCode
	 * @param date
	 * @return
	 * @throws Exception
	 */
	public static Boolean extractHTMLByStock(String stockCode, String date){
		//String html_output_path = ConstVal.FILE_OUTPUT_PATH + "\\" + stockCode + ".html";
		Boolean isOK= true;
		
		try{
			/////////// parse html and rewrite data /////////////////
			// write file
			String str_output_path = getDownloadedWebpageFilePath(date) + "\\" + stockCode + ".csv";
			FileWriter fw2 = new FileWriter(str_output_path);
			
			//parse html
			File html_read = new File(getDownloadedWebpageFilePath(date) + "\\" + stockCode + ".html");
			Document doc = (Document) Jsoup.parse(html_read, "utf-8", "");
			
			// get title
			Elements html_title = doc.getElementsByTag("h2");
			String title = html_title.get(0).text();
			//title = title.replace("CCASS holdings: ", "");
			System.out.println(title);
			
			///////////////  get stock holdings data ////////////////////
			Elements html_table = doc.getElementsByClass("optable");
			//System.out.println(html_table);
			Elements html_table_tr = html_table.get(1).select("tr");  // the 2nd table contains main info
			
			// get stakes in CCASS and stakes not in CCASS
			Elements firstTable_td = html_table.get(0).getElementsByTag("td");
			String stakesInCCASS = firstTable_td.get(20).text();
			String stakesNotInCCASS = firstTable_td.get(23).text();
			System.out.println("stakes in CCASS (%) = " +stakesInCCASS ); 
			System.out.println("stakes not in CCASS (%) = " +stakesNotInCCASS ); 
								
			int counter2 = 0;
			for(Element data_tr:html_table_tr ){
				Elements data_td = data_tr.getElementsByTag("td");
				
				String to_write_str = "";
				if(counter2 == 0){
					to_write_str = "CCASS ID,Name,Holding,Last Change,Stake%,"
							+ "Cum Stake%,stakes in CCASS(%)," + stakesInCCASS 
							+ ",stakes not in CCASS(%)," + stakesNotInCCASS + "\n";
				}
				else{
					to_write_str = data_td.get(1).text().replace(",", "") + "," 
									+ data_td.get(2).text().replace(",", "") + ","
									+ data_td.get(3).text().replace(",", "") + ","
									+ data_td.get(4).text().replace(",", "") + ","
									+ data_td.get(5).text().replace(",", "") + ","
									+ data_td.get(6).text().replace(",", "") + "\n";
				}
				counter2++;
				
				fw2.write(to_write_str);
			}
			fw2.close();
			
			
			//////////////   downloading holder's holdings ////////////////
			if(toDownloadHolderInfo) {
				// creating file directory
				String html_output_dir = getDownloadedWebpageFilePath(date) + "\\holders";
				File html_output_dir_file = new File(html_output_dir);
				if(!html_output_dir_file.exists() && !html_output_dir_file.isDirectory()){
					//System.out.println("dir not exists");
					html_output_dir_file.mkdir();
				}
				
				// get already downloaded data
				String [] fileNames = html_output_dir_file.list();
				for(int i = 0; i < fileNames.length; i++){
					String fileName = fileNames[i];
					fileName = fileName.substring(0, fileName.length()-5); // delete ".html"
					downloadedHolders.add(fileName);
					//System.out.println(fileName);
				}
				
				// downloading data
				for(int i = 1; i <= numOfBrokers ; i ++){  // skip the first line
					Element data_tr = html_table_tr.select("tr").get(i); // get every row 
					Elements data_td = data_tr.getElementsByTag("td");
					
					//holder's name
					String holderName = data_td.get(2).text();
					//System.out.println("holder name " + holderName);
					
					//link to holder's holdings
					Element link = data_td.select("a").first();
					String url = link.attr("href");
					//System.out.println("link = " + url);
					
					// dowloading holder's holding data
					if(!downloadedHolders.contains(holderName)){
						downloadedHolders.add(holderName);
						
						URL holderURL = new URL(ConstVal.WEBB_URL_CCASS + "/" + url);
						HttpURLConnection connection = (HttpURLConnection) holderURL.openConnection();
						connection.setRequestProperty("User-Agent", "Mozilla/31.0 (compatible; MSIE 10.0; Windows NT; DigExt)");
						if(HttpURLConnection.HTTP_OK != connection.getResponseCode()){ // get connection
							continue;
						}  
						InputStream inputStream = connection.getInputStream();
						
						//store Html
						String html_output_path = html_output_dir + "\\" + holderName + ".html";
						UtilityFunction.storeHTML(inputStream,html_output_path);
						
						// Convert HTML to csv
						extractHTMLByHolder(holderName, date);
						
						// disconnect
						connection.disconnect();
					}
				} // end of "for"
			}
			
		}catch(Exception e){
			e.printStackTrace();
			isOK = false;
		}
		return isOK;
	}
	
	/**
	 * get the file path to store downloaded webpages
	 * @param date
	 * @return
	 */
	public static String getDownloadedWebpageFilePath(String date){
		return ConstVal.FILE_OUTPUT_PATH + "\\CCASS\\" + date;
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
	
	public static InputStream downloadWebpage(URL url) throws Exception {
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setRequestProperty("User-Agent", "Mozilla/31.0 (compatible; MSIE 10.0; Windows NT; DigExt)");
		if(HttpURLConnection.HTTP_OK != connection.getResponseCode()){ // get connection
			return null;
		}  
		InputStream inputStream = connection.getInputStream();
		//connection.disconnect();
		return inputStream;
	}
	
	/**
	 * extract html containing holder's info to CSV
	 * @param holderName (e.g. CHINA GALAXY INTERNATIONAL SECURITIES.html)
	 * @param date
	 * @throws Exception
	 */
	public static void extractHTMLByHolder(String holderName, String date) throws Exception{
		String html_output_dir = getDownloadedWebpageFilePath(date) + "\\holders";
		File html_output_dir_file = new File(html_output_dir);
		
		// write file
		String str_output_path = html_output_dir_file + "\\" + holderName + ".csv";
		FileWriter fw2 = new FileWriter(str_output_path);
		
		//parse html
		File html_read = new File(html_output_dir_file + "\\" + holderName + ".html");
		Document doc = (Document) Jsoup.parse(html_read, "utf-8", "");
		
		// get stock holdings data
		Elements html_table = doc.getElementsByClass("optable");
		//System.out.println(html_table);
		Elements html_table_tr = html_table.get(0).select("tr"); 

		
		int counter2 = 0;
		for(Element data_tr:html_table_tr ){
			Elements data_td = data_tr.getElementsByTag("td");
			
			String to_write_str = "";
			if(counter2 == 0){
				to_write_str = "Last Code,Issue,Holding,Value,Stake%,Date\n";
			}
			else{
				to_write_str = data_td.get(1).text().replace(",", "") + "," 
								+ data_td.get(2).text().replace(",", "") + ","
								+ data_td.get(3).text().replace(",", "") + ","
								+ data_td.get(4).text().replace(",", "") + ","
								+ data_td.get(6).text().replace(",", "") + ","
								+ data_td.get(7).text().replace(",", "") + "\n";
			}
			counter2++;
			
			fw2.write(to_write_str);
		}
		fw2.close();
		
	}
	
	/**
	 * get cgi top holding stocks and, by the way, store and convert the html
	 * @param topN
	 * @param date
	 * @return
	 * @throws Exception
	 */
	public static ArrayList<String> getCGITopHoldingStocks(String readStockPath) throws Exception{
		ArrayList<String> topNStocks = new ArrayList<String>();
		
		FileInputStream in = new FileInputStream(readStockPath);  
        InputStreamReader inReader = new InputStreamReader(in, "UTF-8");  
        BufferedReader bufReader = new BufferedReader(inReader);  
        String line = null;  
        
        // read the first line 
        line = bufReader.readLine();
        
        String[] topNStocks_str = line.split(",");
        
        topNStocks.addAll(Arrays.asList(topNStocks_str ));
        
		/////////////////// get CGI CCASS data from webb (obsoleted now) ////////////////////
		/*
		URL CGIURL = new URL("https://webb-site.com/ccass/cholder.asp?part=1184&"+ date + "&z=0&sort=stakdn");
		// connect to the website
		HttpURLConnection connection = (HttpURLConnection) CGIURL.openConnection();
		connection.setRequestProperty("User-Agent", "Mozilla/31.0 (compatible; MSIE 10.0; Windows NT; DigExt)");
		if(HttpURLConnection.HTTP_OK != connection.getResponseCode()){ // get connection
			System.out.println("CGI: not connected!");
			return topNStocks;
		}  
		InputStream inputStream = connection.getInputStream();
		
		String outputPath = holderDir + "\\" + ConstVal.CGI_NAME + ".html";
		UtilityFunction.storeHTML(inputStream, outputPath);
		
		//extractHTMLByHolder("CHINA GALAXY INTERNATIONAL SECURITIES.html", date);
		
		// write file
		String str_output_path = holderDir + "\\CHINA GALAXY INTERNATIONAL SECURITIES.csv";
		FileWriter fw2 = new FileWriter(str_output_path);
				
		//parse html
		File html_read = new File(outputPath);
		Document doc = (Document) Jsoup.parse(html_read, "utf-8", "");
		
		// get stock holdings data
		Elements html_table = doc.getElementsByClass("optable");
		//System.out.println(html_table);
		Elements html_table_tr = html_table.get(0).select("tr"); 

		int counter2 = 0;
		for(Element data_tr:html_table_tr ){
			Elements data_td = data_tr.getElementsByTag("td");
			
			String to_write_str = "";
			if(counter2 == 0){
				to_write_str = "Last Code,Issue,Holding,Value,Stake%,Date\n";
			}
			else{
				int lastCode = Integer.parseInt(data_td.get(1).text().replace(",", ""));
				String lastCodeStr = String.valueOf(lastCode);
				to_write_str = lastCodeStr + "," 
								+ data_td.get(2).text().replace(",", "") + ","
								+ data_td.get(3).text().replace(",", "") + ","
								+ data_td.get(4).text().replace(",", "") + ","
								+ data_td.get(6).text().replace(",", "") + ","
								+ data_td.get(7).text().replace(",", "") + "\n";
			}
			
			if(counter2 >=1 && counter2 <= topN){ // add stock code
				String stockCode = String.valueOf(Integer.parseInt(data_td.get(1).text().replace(",", "")));
				topNStocks.add(stockCode);
				//System.out.println(stockCode);
			}
			counter2++;
			
			fw2.write(to_write_str);
		}
		fw2.close();
		
		// output the stock list
		String stockListPath = ConstVal.FILE_OUTPUT_PATH + "\\" + date + "\\stock list.csv";
		FileWriter fw3 = new FileWriter(stockListPath);
		fw3.write(topNStocks.get(0));
		for(int i = 1; i < topNStocks.size(); i++){
			fw3.write("," + topNStocks.get(i));
		}
		fw3.close();
		*/
		return topNStocks ;
	}
}


/*
 Typical data structure for "finding holdings by stock code"
 
 <table class="optable">
	<tr>
		<th class="colHide1">Row</th>
		<th><a href='/ccass/cholder.asp?part=1186&d=7/12/2017&z=0&sort=codeup'>Last<br/>code</a></th>
		<th class="left"><a href='/ccass/cholder.asp?part=1186&d=7/12/2017&z=0&sort=nameup'>Issue</a></th>
		<th class="colHide1"><a href='/ccass/cholder.asp?part=1186&d=7/12/2017&z=0&sort=holddn'>Holding</a></th>
		<th class="colHide3"><a href='/ccass/cholder.asp?part=1186&d=7/12/2017&z=0&sort=valnup'>Value</a></th>
		<th></th>
		<th><a href='/ccass/cholder.asp?part=1186&d=7/12/2017&z=0&sort=stakdn'>Stake<br>%</a></th>
		<th class="colHide2"><a href='/ccass/cholder.asp?part=1186&d=7/12/2017&z=0&sort=datedn'>Date</a></th>
	</tr>

	<tr>
		<td class="colHide1">1</td>
		<td>2888</td>
		<td class="left"><a href="choldings.asp?issue=3448&amp;d=2017-07-12">STANDARD CHARTERED PLC:O</a></td>
		<td class="colHide1">13,000</td>
		<td class="colHide3">1,060,800</td>
		<td></td>
		<td><a href="chistory.asp?issue=3448&amp;part=1186">0.00</a></td>
		<td class="colHide2" style="white-space:nowrap"><a href="chldchg.asp?issue=3448&d=2015-09-15">2015-09-15</a></td>
	</tr>
	
	<tr>
		<td class="colHide1">2</td>
		<td>1007</td>
		<td class="left"><a href="choldings.asp?issue=6317&amp;d=2017-07-12">Daqing Dairy Holdings Limited:O</a></td>
		<td class="colHide1">382,000</td>
		<td class="colHide3">641,760</td>
		<td>*</td>
		<td><a href="chistory.asp?issue=6317&amp;part=1186">0.04</a></td>
		<td class="colHide2" style="white-space:nowrap"><a href="chldchg.asp?issue=6317&d=2012-05-22">2012-05-22</a></td>
	</tr>
	
</table>

 */


