package myAbstract;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.Arrays;

public class DataFetch {
	
	/**
	 * Read a .csv file and return specified value. e.g. if the csv file is in the form 
		 * stock,price,volume
		 * 1,10,100000
		 * 5,60,1003400
		 * ....
	 * then dataFetch(filePath,"1",0,",",errMsgHead) will return the dataline for stock "1" i.e. "1,10,100000"
	 * @param filePath
	 * @param identifier, e.g. stock code
	 * @param indPlace, e.g. 0
	 * @param separator, e.g. "," this means that for every row, data is separated by comma
	 * @param errMsgHead
	 * @return
	 */
	public static ArrayList<String> dataFetch(String filePath, String identifier, int indPlace, String separator, String errMsgHead	) {
		ArrayList<String> dataLine = new ArrayList<String> ();
		
		try{
			BufferedReader bf = utils.Utils.readFile_returnBufferedReader( filePath );
			if(bf==null){
				System.out.println(errMsgHead + "no such file.");
			}else{
				String line = "";
				while((line = bf.readLine()) != null){
					String[] lineArr = line.split(separator);
					String thisIdentifier = lineArr[indPlace];
					//System.out.println("thisIdentifier = " + thisIdentifier);
					
					if(thisIdentifier.equalsIgnoreCase(identifier)){
						dataLine.addAll(Arrays.asList(lineArr));
						break;
					}
					
				} // end of while
				bf.close();
				
				if(dataLine == null || dataLine.size() == 0){
					//System.out.println("Southbound - get stock data:no data for " + stockCode + " at " + date);
					System.out.println(errMsgHead + "no data.");
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return dataLine;
		
	}
}
