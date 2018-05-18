package webbDownload.outstanding;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.Arrays;

import cgi.ib.avat.AVAT;

public class Main {

	public static void main(String[] args) {
		try {
			//System.out.println(DataGetter.getStockDataField("1", DataGetter.OutstandingDataField.PRICE, "19920801", "yyyyMMdd"));
			
			ArrayList<String> arr = new ArrayList<String>();
			arr.add("839");
			arr.add("1140");
			arr.add("772");
			arr.add("1886");
			arr.add("78");
			arr.add("2858");
			arr.add("2232");
			
			DataDownloader.dataDownloader();
			//DataDownloader.dataDownloader(arr);
			
//			BufferedReader bf = 
//					utils.Utils.readFile_returnBufferedReader(DataDownloader.ALL_STOCK_LIST_PATH);
//			String bf_line = bf.readLine();
//			ArrayList<String> allStockList = 
//					new ArrayList<String>(Arrays.asList(bf_line.split(",")));
//			
//			int parts = 20;
//			int len = allStockList.size();
//			int numEachPart = (int) (len / parts);
//			ArrayList<Thread> thArr  = new ArrayList<Thread>();
//			for(int i = 0; i < parts; i++) {
//				int startInd = numEachPart*i;
//				int endInd = Math.min(numEachPart*(i+1), len);
//				
//				ArrayList<String> thisStockList = new ArrayList<String>(allStockList.subList(startInd, endInd));
//				
//				Thread t = new Thread(new Runnable(){
//					   public void run(){
//						   
//						   DataDownloader.dataDownloader(thisStockList);  
//					   }
//				});
//				thArr.add(t);
//			}
//			for(Thread t : thArr) {
//				t.run();
//			}
//			
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
