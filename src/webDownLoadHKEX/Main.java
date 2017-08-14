package webDownLoadHKEX;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;

public class Main {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String[] stockCode = {"06881","00700"};  // stock code should have "0" in the front, e.g. "00700"
		String dateStr = "2017-07-25";
		//String dateStr = "20170724";
		
		//DataGetter.dataGetter(stockCode, dateStr);
		
		long startTime = System.currentTimeMillis();    //record start time
		System.out.println("******************** Web Download From HKEX ********************");
		try {
			//String[] dates = {"2017-08-04"};
			String[] dates = {"2017-08-02","2017-08-03","2017-08-04","2017-08-07","2017-08-08","2017-08-09","2017-08-10"};
			//String[] dates = {"2017-07-03"};
			
			for(int i = 0; i < dates.length; i++) {
				String date = dates[i];
				
				downloadMain(date);
			}
			
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		long endTime = System.currentTimeMillis();    //record end time
		System.out.println("Total running time: " + (endTime - startTime)/1000 + "s");    //time elapsed
		
		// test
		//DataConverter.dataConverter("2118", "2017-07-03");
		//DataGetter.dataGetter("1216", "2017-07-03");
	}
	
	public static void downloadMain(String date) throws Exception{
		//UtilityFunction.trustAllCertificates();
		
		// creating directories
		String dateDir = Utils.OUTPUT_ROOT_PATH + "//" + date;
		File dateDir_file = new File(dateDir);
		if(!dateDir_file.exists() && !dateDir_file.isDirectory()){
			dateDir_file.mkdir();
		}
		/*
		String holderDir = dateDir + "\\holders";
		File holderDir_file = new File(holderDir);
		if(!holderDir_file.exists() && !holderDir_file.isDirectory()){
			holderDir_file.mkdir();
		}*/
		
		//ArrayList<String> stockCodeList = getCGITopHoldingStocks(Utils.OUTPUT_ROOT_PATH + "\\cgi stock list.csv");
		ArrayList<String> stockCodeList = getCGITopHoldingStocks("D:\\stock data\\all stock list.csv");
		
		
		////////////// downloading webpage and parse /////////////////
		//double fixedTimePeriod = 5; //5s
		
		int counter = 0;
		//if(false)
		while(stockCodeList.size() > 0 || counter == 0) {
			counter ++;
			ArrayList<String> failedStocks = new ArrayList<String>();
			
			for(int i = 0; i < stockCodeList.size(); i++){
				long startTime_i = System.currentTimeMillis();
				
				String stockCode = stockCodeList.get(i);
				
				System.out.println("======== i = " + i + "/" + String.valueOf(stockCodeList.size()) +", stock code = " + stockCode + " " + date + " ==========");
				//Boolean isDownloadOK = downloadWebpageByStock(stockCode, date);
				boolean isDownloadOK = DataGetter.dataGetter(stockCode, date);
				
				if(isDownloadOK){
					Boolean isConvertOK = DataConverter.dataConverter(stockCode, date);
					if(!isConvertOK){
						System.out.println("Converting unsuccessful!");
						failedStocks.add(stockCode);
					}
				}else{
					System.out.println("Downloading unsuccessful!");
					failedStocks.add(stockCode);
				}	
				
				// pause for a random number of secs
				//Thread.sleep((long) (Math.random()*3 + 2) * 1000);
				
				long endTime_i = System.currentTimeMillis();    //��ȡ����ʱ��
				double runningTime = ((double) endTime_i - startTime_i) / 1000; // s
				//System.out.println("======== running time"  + runningTime + "s ==========");    //�����������ʱ��
				
			} // end of "for"
			
			// failed stocks exist
			if(failedStocks.size() > 0) {
				System.out.println("==========  num of failed stocks: " + failedStocks.size() + " ===========");
			}
			stockCodeList = new ArrayList<String>(failedStocks);
			
		} // end of "While"
		
		// ============ extract relevant files ============
		/*
		DataConverter.csvDataConsolidation(stockCodeList, date);
		DataConverter.writeTodayBLCCASSData(stockCodeList, date);
		DataConverter.writeHistAllCCASSData(stockCodeList, date);
		DataConverter.writePriceFile(stockCodeList, date);
		DataConverter.writeHistBLCCASSData(stockCodeList, date);  // it needs to read (historical "today BL CCASS data" so these file must be ready before running it
		*/
	}
	
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

        return topNStocks;
	}
}
