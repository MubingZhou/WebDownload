package kdb;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class DataConverter {

	public static SimpleDateFormat sdf_hyphen = new SimpleDateFormat("yyyy-MM-dd");
	public static SimpleDateFormat sdf_dot = new SimpleDateFormat("yyyy.MM.dd");
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String in = "";
		String outRoot = "";
		
		convertFromWindData(in, outRoot);
	}

	/**
	 * For data from Wind
	 * Put single stock data into a large dictionary
	 * 
	 * Raw data column: ticker, ticker name, date(yyyy-MM-dd), open, high, low, close, volume, turnover
	 * Transformed data col: date(yyyy.MM.dd), ticker, open, high, low, close, volume, turnover
	 * @param path
	 */
	public static void convertFromWindData(String rootpath, String outputRootPath) {
		try {
			outputRootPath = utils.Utils.addBackSlashToPath(outputRootPath);
			
			Map<Date, FileWriter> fwMap = new HashMap<Date, FileWriter>();
			
			File f = new File(rootpath);
			String[] fileList = f.list();
			for(String s:fileList) {
				if(s.substring(s.length()-4, s.length()).equalsIgnoreCase(".csv")){
					System.out.println(s);
					
					String path = utils.Utils.addBackSlashToPath(rootpath) + s;
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
						Double open = utils.Utils.safeParseDouble(oStr, close);
						Double high = utils.Utils.safeParseDouble(hStr, close);
						Double low = utils.Utils.safeParseDouble(lStr, close);
						Double volume = utils.Utils.safeParseDouble(vStr, 0.0);
						Double turnover = utils.Utils.safeParseDouble(tStr, 0.0);
						
						FileWriter fw = fwMap.get(date);
						String date_dot = sdf_dot.format(date);
						if(fw == null) {
							fw = new FileWriter(outputRootPath + date_dot + ".csv");
						}
						fw.write(date_dot + "," + ticker + "," 
								+ String.valueOf(open) + "," + String.valueOf(high) + "," 
								+ String.valueOf(low) + "," + String.valueOf(close) + "," 
								+ String.valueOf(volume)  + "," + String.valueOf(turnover) + "\n");
						
						fwMap.put(date, fw);
						
					} // while
					
				}  // read each file
			}
			
			for(FileWriter fw : fwMap.values())
				fw.close();
			
			
			
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * Delete the 0s of HK stock
	 * input is the in the form of xxxx.HK e.g. 0001.HK, 0700.HK
	 * output in the form of 1.HK, 700.HK
	 * @param in
	 * @return
	 */
	public static String rightStockCode(String in) {
		String out="";
		boolean getNonZero = false;
		
		String[] arr = in.split(".");
		String suffix = arr[arr.length-1];
		
		if(suffix.equalsIgnoreCase("HK")) {
			for(int i = 0; i < in.length(); i++) {
				char c = in.charAt(i);
				if(c != '0' && !getNonZero) {
					getNonZero = true;
				}
				
				if(getNonZero)
					out += String.valueOf(c);
			}
		}else {  // for A share (XXXXXX.SZ & XXXXXX.SH), no need to change
			out = in;
		}
		
		// convert to upper case
		out = out.toUpperCase();
		
		return out;
	}
}
