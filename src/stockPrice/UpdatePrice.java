package stockPrice;

import java.io.BufferedReader;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class UpdatePrice {
	public static String filePath = "Z:\\Mubing\\stock data\\stock hist data - webb";
	public static String allStockListPath = "D:\\stock data\\all stock list.csv";
	
	public void updatePrice() {
		try {
			BufferedReader bf_allStock = utils.Utils.readFile_returnBufferedReader(allStockListPath);
			ArrayList<String> allStockList = new ArrayList<String>(); 
			allStockList.addAll(Arrays.asList(bf_allStock.readLine().split(",")));
			
			Map<String, Date> lastDateMap  = new HashMap<String, Date>(); // stock code - last updated date 
			
			
			
			File f = new File(filePath);
			ArrayList<String> allStocksArr = new ArrayList<String> (); 
			//Map<String, Date> lastDateMap  = new HashMap<String, Date>(); // stock code - last updated date 
			
			for(File f0 : f.listFiles()) {
				String fileName = f0.getName();  // e.g "700.csv"
				String stockCode = fileName.substring(0,fileName.length() - 4);
				allStocksArr.add(stockCode);
				
				BufferedReader bf = utils.Utils.readFile_returnBufferedReader(filePath + "\\" + fileName);
				String line = "";
				while((line = bf.readLine()) != null) {
					
				}
			}
			
		}catch(Exception e) {
			e.printStackTrace();
		}

		
	}
	
}
