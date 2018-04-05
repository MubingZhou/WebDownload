package wind;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Wind2Q {

	public static Map<Date, Map<String, ArrayList<Double>>> allData = new HashMap<Date, Map<String, ArrayList<Double>>>(); // Date - date, String - stock code, ArrayList<Double> - data:open high low close volume turnover
	public static SimpleDateFormat sdf_hyphen = new SimpleDateFormat("yyyy-MM-dd");
	public static SimpleDateFormat sdf_dot = new SimpleDateFormat("yyyy.MM.dd");
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String rootPath = "Z:\\Mubing\\stock data\\HK data - Wind";
		String outputRootPath = "Z:\\Mubing\\stock data\\HK data - Wind\\revise";
		
		File f = new File(rootPath);
		String[] fileList = f.list();
		for(String s:fileList) {
			if(s.substring(s.length()-4, s.length()).equalsIgnoreCase(".csv")){
				System.out.println(s);
				dataFromStock(utils.Utils.addBackSlashToPath(rootPath) + s);
			}
		}
		
		outputData(outputRootPath);
	}
	
	/**
	 * Put single stock data into a large dictionary
	 * @param path
	 */
	public static void dataFromStock(String path) {
		try {
			BufferedReader bf = utils.Utils.readFile_returnBufferedReader(path);
			String line = "";
			int count = 0;
			while((line = bf.readLine()) != null) {
				if(count == 0) {
					count++;
					continue;
				}
				String[] dateArr = line.split(",");
				
				String ticker = rightStockCode(dateArr[0]);   // If the future configuration is different, please modify here
				Date date = sdf_hyphen.parse(dateArr[2]); // date
				String oStr = dateArr[3];
				String hStr = dateArr[4];
				String lStr = dateArr[5];
				String cStr = dateArr[6];
				String vStr = dateArr[7]; // vol
				String tStr = dateArr[8]; // turnover
				
				Double close = Double.parseDouble(cStr);
				
				Double open = close;
				if(utils.Utils.isDouble(oStr))
					open = Double.parseDouble(oStr);
				
				Double high = close;
				if(utils.Utils.isDouble(hStr))
					high = Double.parseDouble(hStr);
				
				Double low = close;
				if(utils.Utils.isDouble(lStr))
					low = Double.parseDouble(lStr);
				
				Double volume = 0.0;
				if(utils.Utils.isDouble(vStr))
					volume = Double.parseDouble(vStr);
				
				Double turnover = 0.0;
				if(utils.Utils.isDouble(tStr))
					turnover = Double.parseDouble(tStr);

				ArrayList<Double> data = new ArrayList<Double>();
				data.add(open);
				data.add(high);
				data.add(low);
				data.add(close);
				data.add(volume);
				data.add(turnover);
				
				Map<String, ArrayList<Double>> thisDateData = allData.get(date);
				if(thisDateData == null || thisDateData.size() == 0)
					thisDateData = new HashMap<String, ArrayList<Double>>();
				
				thisDateData.put(ticker, data);
				allData.put(date, thisDateData);
			}
			
			
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void outputData(String outputPath) {
		try {
			for(Date date: allData.keySet()) {
				FileWriter fw = new FileWriter(utils.Utils.addBackSlashToPath(outputPath) + sdf_dot.format(date) + ".csv");
				String dateStr = sdf_dot.format(date);
				
				Map<String, ArrayList<Double>> todayData = allData.get(date);
				
				for(String stock : todayData.keySet()) {
					ArrayList<Double> data = todayData.get(stock);
					fw.write(stock + "," + dateStr);
					for(Double d : data) {
						fw.write("," + String.valueOf(d));
					}
					fw.write("\n");
				} // todayData
				
				fw.close();
			}
			
			
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * input is the in the form of xxxx.HK e.g. 0001.HK, 0700.HK
	 * output in the form of 1.HK, 700.HK
	 * @param in
	 * @return
	 */
	public static String rightStockCode(String in) {
		String out="";
		boolean getNonZero = false;
		
		for(int i = 0; i < in.length(); i++) {
			char c = in.charAt(i);
			if(c != '0' && !getNonZero) {
				getNonZero = true;
			}
			
			if(getNonZero)
				out += String.valueOf(c);
		}
		
		
		
		return out;
	}
}
