package webbDownload.outstanding;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.Arrays;

public class Main {

	public static void main(String[] args) {
		try {
			//System.out.println(DataGetter.getStockDataField("1", DataGetter.OutstandingDataField.PRICE, "19920801", "yyyyMMdd"));
			
			ArrayList<String> arr = new ArrayList<String>();
			arr.add("8328");
			
			DataDownloader.dataDownloader(arr);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
