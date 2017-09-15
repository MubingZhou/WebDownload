package webbDownload.outstanding;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.Arrays;

public class Main {

	public static void main(String[] args) {
		try {
			//System.out.println(DataGetter.getStockDataField("1", DataGetter.OutstandingDataField.PRICE, "19920801", "yyyyMMdd"));
			
			BufferedReader bf = utils.Utils.readFile_returnBufferedReader("D:\\stock data\\all trading date - hk.csv");
			String str = bf.readLine();
			String[] s  =str.split(",");
			ArrayList<String> arr = new ArrayList<String>(Arrays.asList(s));
			
			DataDownloader.dataDownloader(arr);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
