package sfcData;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Locale;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class ShortInterest_Downloader {

	/**
	 * Download the SFC short Interest data 
	 * The output data will be group by date, i.e. each file contains data for a specific date
	 */
	public static void dataDownloader(String outputFilePath, String outputDateFormat) {
		try {
			String SFC_URL = "http://www.sfc.hk/web/EN/regulatory-functions/market-infrastructure-and-trading/short-position-reporting/aggregated-short-positions-of-specified-shares.html";
			String SFC_CSV_DOWNLOAD_URL = "http://www.sfc.hk/web/EN/";
			String csvOutputFilePath = outputFilePath;  //;
			
			String sfcDownloadingPagePath = "D:\\stock data\\SFC Short Interest\\downloadingPage.html" ;
			utils.Utils.downLoadHTMLFromUrl(SFC_URL, sfcDownloadingPagePath);
			
			SimpleDateFormat sdf1 = new SimpleDateFormat("d MMM yyyy", Locale.ENGLISH);
			SimpleDateFormat sdf2 = new SimpleDateFormat(outputDateFormat);
			//======== parse the "downloading page" ==========
			File html_read = new File(sfcDownloadingPagePath);
			Document doc = (Document) Jsoup.parse(html_read, "utf-8", "");
			
			// main table is "spr_table"
			Element mainTable = doc.getElementById("spr_table");
			Elements mainBody = mainTable.getElementsByTag("tbody");
			Elements trRow = mainBody.get(0).getElementsByTag("tr");
			
			for(int i = 0; i < trRow.size(); i++) {
				Elements tdCol = trRow.get(i).getElementsByTag("td");
				
				String date = tdCol.get(0).text();
				String link = tdCol.get(2).getElementsByTag("a").get(0).attr("href");
				
				//System.out.println("date = " + date + " link = " + link);
				//Thread.sleep(1000 * 10000);
				
				String csvDownloadingURL = SFC_CSV_DOWNLOAD_URL + link;
				
				String newDate = sdf2.format(sdf1.parse(date));
				
				utils.Utils.downLoadFromUrl(csvDownloadingURL, newDate+".csv", csvOutputFilePath);
				
				//Thread.sleep(1000 * 10000);
			}
			
		}catch(Exception e) {
			e.printStackTrace();
		}

	}
	
	public static void dataDownloader() {
		dataDownloader("D:\\stock data\\SFC Short Interest", "yyyyMMdd");
	}

}
