package webbDownload.southboundData;

import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
	public static String WEBB_URL_SOUTHBOUND_SH = "https://webb-site.com/ccass/cholder.asp?part=1323";
	public static String WEBB_URL_SOUTHBOUND_SZ = "https://webb-site.com/ccass/cholder.asp?part=1456";
	public static String FILE_OUTPUT_PATH = "D:\\stock data\\HK CCASS - WEBB SITE";
	
	public static void dataDownloader() {
		try{
			Boolean toDownloadSH = true;
			Boolean toDownloadSZ = true;
			
			// get tradings dates
			String startDate_sz = "2016-12-07";
			String startDate_sh = "2014-11-19";
			
			ArrayList<String> dates = utils.Utils.getWorkingDaysBetweenDates("2017-09-19", "2017-09-21", "yyyy-MM-dd");
			//ArrayList<String> dates = utils.Utils.getWorkingDaysBetweenDates(startDate_sz, "2017-08-25", "yyyy-MM-dd");
			
			utils.Utils.trustAllCertificates();
			
			/////////////////// downloading files //////////////////
			long startTime = System.currentTimeMillis();    
			
			for(int i = 0; i < dates.size(); i++){
				String date = dates.get(i);
				//System.out.println("========== date = " + date + " ============");
				
				String outputFilePath = FILE_OUTPUT_PATH + "\\southbound" ;
				utils.Utils.checkDir(outputFilePath);
				
				String webb_sh_str = WEBB_URL_SOUTHBOUND_SH + "&d=" + date;
				String webb_sz_str = WEBB_URL_SOUTHBOUND_SZ + "&d=" + date;
				URL webb_sh = new URL(webb_sh_str);
				URL webb_sz = new URL(webb_sz_str);
				
			////////// downloading SH data //////////////
				if(toDownloadSH){
					HttpURLConnection connection_sh = (HttpURLConnection) webb_sh.openConnection();
					connection_sh.setRequestProperty("User-Agent", "Mozilla/31.0 (compatible; MSIE 10.0; Windows NT; DigExt)");
					if(HttpURLConnection.HTTP_OK != connection_sh.getResponseCode()){ // get connection
						System.out.println("not connected! (SH)");
						continue;
					}  
					InputStream inputStream_sh = connection_sh.getInputStream();
					
					// output file path
					String outputFilePath_sh = outputFilePath + "\\sh\\" + date + ".html";
					
					//store html
					utils.Utils.storeHTML(inputStream_sh, outputFilePath_sh);
					
					//write to csv
					String outputCSVPath_sh = outputFilePath + "\\sh\\" + date + ".csv";
					convertHTML2CSV_WebbCCASS(outputFilePath_sh, outputCSVPath_sh);
				}
				
			////////// downloading SZ data //////////////
				if(toDownloadSZ){
					HttpURLConnection connection_sz = (HttpURLConnection) webb_sz.openConnection();
					connection_sz.setRequestProperty("User-Agent", "Mozilla/31.0 (compatible; MSIE 10.0; Windows NT; DigExt)");
					if(HttpURLConnection.HTTP_OK != connection_sz.getResponseCode()){ // get connection
						System.out.println("not connected! (SZ)");
						continue;
					}  
					InputStream inputStream_sz = connection_sz.getInputStream();
					
					// output file path
					String outputFilePath_sz = outputFilePath + "\\sz\\" + date + ".html";
					
					//store html
					utils.Utils.storeHTML(inputStream_sz, outputFilePath_sz);
					
					//write to csv
					String outputCSVPath_sz = outputFilePath + "\\sz\\" + date + ".csv";
					convertHTML2CSV_WebbCCASS(outputFilePath_sz, outputCSVPath_sz);
				}
			/////////////// pause ///////////
				Thread.sleep((long) (Math.random() * 3 + 2) * 1000);
				
			}  ///// end of "for"
				
			long endTime = System.currentTimeMillis();    //
			System.out.println("Total running time: " + (endTime - startTime)/1000 + "s");    //
		}catch(Exception e){ // end of "try"
			e.printStackTrace();
		}
	}
	
	/**
	 * convert HTML to csv. inputFilePath & outputFilePath should be the full path
	 * @param inputFilePath
	 * @param outputFilePath
	 */
	private static void convertHTML2CSV_WebbCCASS(String inputFilePath, String outputFilePath) {
		try {
			// write file
			FileWriter fw2 = new FileWriter(outputFilePath);
						
			//read html
			File html_read = new File(inputFilePath);
			Document doc = (Document) Jsoup.parse(html_read, "utf-8", "");
			
			///////////////  get stock holdings data ////////////////////
			Elements html_table = doc.getElementsByClass("optable");
			//System.out.println(html_table);
			Elements html_table_tr = html_table.get(0).select("tr");  // the 2nd table contains main info
								
			int counter2 = 0;
			for(Element data_tr:html_table_tr ){
				Elements data_td = data_tr.getElementsByTag("td");
				
				String to_write_str = "";
				if(counter2 == 0){
					to_write_str = "Last Code,Issue,Holding,Value,Stake%,Date\n";
				}
				else{
					to_write_str = formatStockCode(data_td.get(1).text().replace(",", "")) + "," 
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
		}catch(Exception e) {
			e.printStackTrace();
			System.out.print("Extract HTML error: input: " + inputFilePath + " output: " + outputFilePath);
		}
		
	}
	
	/**
	 * All stock stock should in the form of "290","1", etc. i.e. no zeros in the front. "0290" will be formatted into "290"
	 * @param code
	 * @return
	 */
	private static String formatStockCode(String code) {
		char[] c = code.toCharArray();
		int ind = 0;
		for(int i = 0; i < c.length; i++) {
			if(c[i] != '0') {
				ind = i;
				break;
			}
				
		}
		return code.substring(ind);
	}

}
