package southboundData;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class DataCombiner {
	/*
	 * When downloading data, usually data for SH and SZ is separated. 
	 * This class aims to combine them together
	 */
	public static boolean dataCombiner(String shFilePath, String szFilePath, String outputPath){
		boolean isOK = true;
		
		if(!shFilePath.substring(shFilePath.length() - 2).equals("\\"))
			shFilePath = shFilePath + "\\";
		if(!szFilePath.substring(szFilePath.length() - 2).equals("\\"))
			szFilePath = szFilePath + "\\";
		if(!outputPath.substring(outputPath.length() - 2).equals("\\"))
			outputPath = outputPath + "\\";
		
		try {
			// ======== get all .csv files in the directory containig SH data ========
			ArrayList<String> shFileList = new ArrayList<String>();
			File shFile = new File(shFilePath);
			String[] fileListSH = shFile.list();
			for(String file : fileListSH) {
				if(file.length() > 4 && file.substring(file.length() - 4).equalsIgnoreCase(".csv")) {  // only get .csv files
					shFileList.add(file);  // only file names, not including full path
				}
			}
			
			// ======== get all .csv files in the directory containig SH data ========
			ArrayList<String> szFileList = new ArrayList<String>();
			File szFile = new File(szFilePath);
			String[] fileListSZ = szFile.list();
			for(String file : fileListSZ) {
				if(file.length() > 4 && file.substring(file.length() - 4).equalsIgnoreCase(".csv")) {  // only get .csv files
					szFileList.add(file);  // only file names, not including full path
				}
			}
			
			// ======= compare sh & sz files ===========
			ArrayList<String> dealNames = new ArrayList<String>();
			for(String shFileName : shFileList) {
				// read file by file, each file contains data for one day for all stocks
				if(szFileList.contains(shFileName)) {
					// read sh data first
					Map<String, ArrayList<String>> allData = new HashMap();
					BufferedReader bf = utils.Utils.readFile_returnBufferedReader(shFilePath + shFileName);
					String line = "";
					int counter = 0;
					while((line = bf.readLine()) != null) {
						if(counter == 0) {  // ignore the first line
							counter ++;
							continue;
						}
						
						ArrayList<String> dataLine = new ArrayList<String>(Arrays.asList(line.split(",")));
						allData.put(dataLine.get(0), dataLine);
					} // end of while
					bf.close();
					
					// read sz data
					BufferedReader bf2 = utils.Utils.readFile_returnBufferedReader(szFilePath + shFileName);
					counter = 0;
					while((line = bf2.readLine()) != null) {
						if(counter == 0) {  // ignore the first line
							counter ++;
							continue;
						}
						
						ArrayList<String> szDataLine = new ArrayList<String>(Arrays.asList(line.split(",")));
						String stockCode = szDataLine.get(0);
						
						if(allData.containsKey(stockCode)) { // if both sh & sz hold the stock, combine the together
							String szHoldingStr = szDataLine.get(2);
							String szValueStr = szDataLine.get(3);
							String szStakeStr = szDataLine.get(4);
							
							ArrayList<String> shDataLine = allData.get(stockCode);
							String shHoldingStr = shDataLine.get(2);
							String shValueStr = shDataLine.get(3);
							String shStakeStr = shDataLine.get(4);
							
							String combineHoldingStr = String.valueOf(Double.parseDouble(szHoldingStr) + Double.parseDouble(shHoldingStr));
							String combineValueStr = String.valueOf(Double.parseDouble(szValueStr) + Double.parseDouble(shValueStr));
							String combineStakeStr = String.valueOf(Double.parseDouble(szStakeStr) + Double.parseDouble(shStakeStr));
							
							ArrayList<String> newDataLine = shDataLine;
							newDataLine.set(2, combineHoldingStr);
							newDataLine.set(3, combineValueStr);
							newDataLine.set(4, combineStakeStr);
							
							allData.put(stockCode, newDataLine);
						}else { // if sh doesn't contain this stock
							allData.put(stockCode, szDataLine);
						}
					} // end of while
					bf2.close();
					
					// =====  write the new data to the output file path ======
					FileWriter fw = new FileWriter(outputPath + shFileName);
					String header = "Last Code,Issue,Holding,Value,Stake%,Date\n";
					fw.write(header);
					Set<String> keysList = allData.keySet();
					for(String stockCode : keysList) {
						ArrayList<String> dataLine = new ArrayList<String>();
						dataLine = allData.get(stockCode);
						String dateLineStr = utils.Utils.arrayToString(dataLine, ",");
						
						fw.write(dateLineStr);
						fw.write("\n");
					}
					fw.close();
					
					
				}else { // if sz doesn't contain this file, then just copy the file into dest path
					utils.Utils.copyFile(shFilePath + shFileName, outputPath + shFileName);
				}
			} // end of for
			
			
		} catch (Exception e) {
			isOK = false;
			e.printStackTrace();
		}
		
		
		return isOK;
	}
	
}
