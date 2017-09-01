package webbDownload.outstanding;

import java.util.ArrayList;
import java.util.Arrays;

public class Main {

	public static void main(String[] args) {
		String[] s = {"116","93","89","86"};
		ArrayList<String> arr = new ArrayList<String>(Arrays.asList(s));
		//DataDownloader.dataDownloader(arr);
		
		try {
			System.out.println(DataGetter.getStockDataField("1", DataGetter.OutstandingDataField.PRICE, "19920801", "yyyyMMdd"));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
