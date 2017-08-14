package webDownLoadHKEX;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Utils {
	public static final String HKEX_DATA_URL = "http://www.hkexnews.hk/sdw/search/searchsdw.aspx";
	
	// under this project, all date are in the format of yyyy-mm-dd
	public static final String DATE_FORMAT = "yyyy-MM-dd";
	
	// OUTPUT root path
	public static final String OUTPUT_ROOT_PATH = "D:\\stock data\\HKEX";
	
	// csv file storing all brokers' names & short names
	public static final String BROKER_NAME_FILE_PATH = "D:\\stock data\\CCASS Participants List.csv";
	
	public static boolean writeFile(InputStream inputStream, String outputPath) {
		boolean isOK = true;
		
		try {
			InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "utf-8");
			//System.out.println("here15847");
			
			BufferedReader bufReader = new BufferedReader(inputStreamReader);
			
			FileWriter fw = new FileWriter(outputPath);
			String line = "";
			StringBuilder contentBuf = new StringBuilder();
			int counter = 0;
			while ((line = bufReader.readLine()) != null) {
				contentBuf.append(line);
				//fw.write(line);
				//System.out.println(line);
				if(counter % 10 == 0){
					//System.out.println("====The " + counter + "th line=====");
				}
				counter++;
			}
			fw.write(contentBuf.toString());
			fw.close();
			inputStream.close();
		}catch(IOException ioe) {
			ioe.printStackTrace();
			isOK = false;
		}
		
		
		return isOK;
	}

}
