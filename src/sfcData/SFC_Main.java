package sfcData;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.Arrays;

//import sfcData.ShortPosition_DataGetter.SFC_SHORTINTEREST_FIELD;

public class SFC_Main {

	public static void main(String[] args) {
		ArrayList<String> stockList;
		try {
			BufferedReader bf = utils.Utils.readFile_returnBufferedReader("D:\\stock data\\SFC Short Interest\\compare with southbound\\stock list.csv");
			String line0 = bf.readLine();
			stockList = new ArrayList<String>(Arrays.asList(line0.split(",")));
			
			//stockList = new ArrayList<String>(); stockList.add("1"); stockList.add("2");
			//;for(int i = 0; i < stockList.size(); i++){
			SI_CompareWithSouthBound.generateReport(stockList, "20170804", "yyyyMMdd", "D:\\stock data\\SFC Short Interest\\compare with southbound");
	
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

}