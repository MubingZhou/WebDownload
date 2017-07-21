package webDownload;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class AnalyzeData {

	private static final int topNStocks_notOwnedByCGI = 5; // stock not owned by CGI: num of stocks for each blacklist broker
	
	private static ArrayList<String> cgiBlacklist = new ArrayList<String>(Arrays.asList(ConstVal.CGI_BLACKLIST_BROKERS));
	
	/**
	 * this class is to extract and present the data in a good-looking way after all data is downloaded
	 * @param args
	 */
	public static void main(String[] args) {
		try{
			System.out.println("**************** Analyzing Data ******************");
			
			// configurations
			String[] dates = {"2017-07-19"};
			
			
			for(int i = 0; i < dates.length; i++) {
				String date = dates[i];
				analyzeMain(date);
			}
			
		}
		catch(Exception e){
			e.printStackTrace();
		}

	}
	
	/**
	 * Main func to deal with the analysis
	 * @param date
	 * @throws Exception
	 */
	private static void analyzeMain(String date) throws Exception{
		System.out.println("============== date = " + date + " ===============");
		
		// get stock list
		BufferedReader bf_stocklist= new BufferedReader(new FileReader(ConstVal.FILE_OUTPUT_PATH + "\\cgi stock list.csv"));
		String[] stockOwnedByCGIList = bf_stocklist.readLine().split(",");
		
		ArrayList<String> stockArrayList = new ArrayList<String>(Arrays.asList(stockOwnedByCGIList));
		int stockListLen = stockOwnedByCGIList.length;
		
		//file to write
		File toWriteDir = new File(ConstVal.FILE_OUTPUT_PATH);
		if(!toWriteDir.exists() && !toWriteDir.isDirectory()){  // create dir if not exists
			toWriteDir.mkdir();
		}
		
		FileWriter fw = new FileWriter(ConstVal.FILE_OUTPUT_PATH + "\\" + date + "\\margin loan mon.csv");
		
		// data to be written in the file
		ArrayList<String> toWriteStrList = new ArrayList<String>(); // store the content to write in an ArrayList first
		
		// get header
		String header = "stock code,stakes in CCASS(%),stakes not in CCASS(%),CGI(HKD),Blacklist(HKD),CGI stake(%),"
				+ "Blacklist stake(%),CGI stake/Blacklist stake,CGI/CCASS,Blacklist/CCASS,(CGI+Blacklist)/CCASS";
		String header_2ndpart="";
		for(int i = 0; i < ConstVal.CGI_BLACKLIST_BROKERS.length; i++){
			String brokerFullName = ConstVal.CGI_BLACKLIST_BROKERS[i];
			String[] brokerFullNameList = brokerFullName.split(" ");
			String brokerShortName = brokerFullNameList.length == 1?
					brokerFullNameList[0]:brokerFullNameList[0] + " " + brokerFullNameList[1];
					
			header = header + "," + brokerShortName + "(HKD)" ;
			header_2ndpart = header_2ndpart + "," + brokerShortName + "(%)";
		}
		header = header + header_2ndpart;
		toWriteStrList.add(header);
	
		// ================== section: stocks owned by CGI =================== 	
		for(int i = 0;  i < stockListLen; i++){
			// data to collect
			String stockCode = stockOwnedByCGIList[i];  //每读一个stock的文件，肯定会写一行数据
			System.out.println(stockCode);
			
			toWriteStrList = addStockInfoLine(stockCode, date, toWriteStrList);
        	
		} // end of "for(int i = 0..."
		
		//toWriteStrList.add("stocks not owned by CGI \n");
		
		//================= section: stocks not owned by CGI ========================
		for(int i = 0; i < ConstVal.CGI_BLACKLIST_BROKERS.length-10000; i++) {
			// read file
			String brokerName = ConstVal.CGI_BLACKLIST_BROKERS[i];
			String readBrokerPath = ConstVal.FILE_OUTPUT_PATH + "\\" + date + "\\holders\\" + brokerName + ".csv";
			FileInputStream in = new FileInputStream(readBrokerPath);  
            InputStreamReader inReader = new InputStreamReader(in, "UTF-8");  
            BufferedReader bufReader = new BufferedReader(inReader);  
            String line = null;  
            int counter = 0;
            while((line = bufReader.readLine()) != null){  
            	if(counter >= 1 && counter <= topNStocks_notOwnedByCGI) {
            		String[] lineList = line.split(",");
            		String stockCode = lineList[0];
            		
            		if(!stockArrayList.contains(stockCode)) {  // if "stocks owned by CGI" section doesn't contain this stock
            			toWriteStrList = addStockInfoLine(stockCode, date, toWriteStrList);
            		}
            		
            	}else {
            		break;
            	}// end of if
            	counter++;
            }// end of while
            bufReader.close();  
            inReader.close();  
            in.close(); 
		} // end of for
		
		
		// ===================== to write into the file ========================
		for(int i = 0; i < toWriteStrList.size(); i++){
			fw.write(toWriteStrList.get(i) + "\n");
		}
		fw.close();
		
		//writeDataIntoExcel(toWriteStrList, date);
	}
	
	/**
	 * read into a single stock file. Read this file line by line and extract relevant info. 
	 * Then write the info into toWriteStrList (storing the info of every line of the final consolidated csv)
	 * @param stockCode
	 * @param date
	 * @param toWriteStrList
	 * @return
	 * @throws Exception
	 */
	private static ArrayList<String> addStockInfoLine(String stockCode, String date, ArrayList<String> toWriteStrList) throws Exception{
		// data for each stock
		String[] holdingValueList = new String[ConstVal.CGI_BLACKLIST_BROKERS.length];
		String[] holdingStakeList = new String[ConstVal.CGI_BLACKLIST_BROKERS.length];
		for(int j = 0; j < ConstVal.CGI_BLACKLIST_BROKERS.length; j++){  // initializing
			holdingValueList[j] = "0";
			holdingStakeList[j] = "0";
		}
		String stockInCCASS = "NA";
    	String stockNotInCCASS= "NA";
    	String cgiHolding = "NA";
    	String blacklistHolding = "NA";
    	String cgiHolding_per = "NA";  // cgi holding (%)
    	String blacklistHolding_per = "NA"; // blacklist holding in total (%)
    	String cgiOVERblacklist = "NA";  	// cgiHolding_per /blacklistHolding_per 
    	String cgiOVERCCASS  = "NA";		// cgiHolding_per / stockInCCASS
    	String blacklistOVERCCASS  = "NA";		// blacklistHolding_per / stockInCCASS
    	String cgiADDblacklist = "NA";	// (cgiHolding_per + blacklistHolding_per) / stockInCCASS
		
		// read into the stock
		String readStockPath = ConstVal.FILE_OUTPUT_PATH + "\\" + date + "\\" + stockCode + ".csv";
		
		// if the file doesn't exists, it needs downloading
		// codes here.......
		
		FileInputStream in = new FileInputStream(readStockPath);  
        InputStreamReader inReader = new InputStreamReader(in, "UTF-8");  
        BufferedReader bufReader = new BufferedReader(inReader);  
        String line = null;  
        int counter = 0;
        while((line = bufReader.readLine()) != null){  
        	// items to be extracted from the line
        	String brokerName= "";
        	String holdingValue= "";
        	String holdingStake= "";
        	
        	String toWriteStr = null;
        	String[] splitStr = line.split(",");
        	
        	// deal with header
        	if(counter == 0){
            	 stockInCCASS = splitStr[7];
            	 stockNotInCCASS = splitStr[9];
             }
             
        	// deal with other lines
        	brokerName = splitStr[1];
        	holdingValue = splitStr[2];
        	holdingStake = splitStr[4];
        	
        	// found the target broker
        	if(cgiBlacklist.contains(brokerName)){
        		int k = cgiBlacklist.indexOf(brokerName);
        		holdingValueList[k] = holdingValue;
        		holdingStakeList[k] = holdingStake;
        	}
        	
        	// get cgi data
        	if(brokerName.equals(ConstVal.CGI_NAME)){
        		cgiHolding = holdingValue;
        		cgiHolding_per = holdingStake; // cgi stake
        		cgiOVERCCASS = String.valueOf(Double.parseDouble(cgiHolding_per) / Double.parseDouble(stockInCCASS));
        	}
             
        	counter++;
        }  
        bufReader.close();  
        inReader.close();  
        in.close(); 
        
     // add to write list
    	long blacklistTotalHKD = 0;
    	double blacklistTotalStake = 0;
    	for(int k = 0; k < holdingValueList.length; k++){  // calculating "blacklist (HKD)" & "blacklist stake" 
    		//toWrite = toWrite + "," + holdingValueList[k] + "," + holdingStakeList[k];
    		blacklistTotalHKD = blacklistTotalHKD + Long.parseLong(holdingValueList[k]);
    		blacklistTotalStake = blacklistTotalStake + Double.parseDouble(holdingStakeList[k]);
    	}
    	
    	if(counter > 0) {  // to avoid no-data case
    		blacklistHolding = String.valueOf(blacklistTotalHKD);
        	blacklistHolding_per = String.valueOf(blacklistTotalStake);
        	cgiOVERblacklist = String.valueOf(Double.parseDouble(cgiHolding_per) / Double.parseDouble(blacklistHolding_per));
        	cgiOVERCCASS = String.valueOf(Double.parseDouble(cgiHolding_per) / Double.parseDouble(stockInCCASS));
        	blacklistOVERCCASS = String.valueOf(Double.parseDouble(blacklistHolding_per) / Double.parseDouble(stockInCCASS));
        	cgiADDblacklist = String.valueOf(Double.parseDouble(cgiOVERCCASS) + Double.parseDouble(blacklistOVERCCASS));
    	}
    	
    	String toWrite = stockCode + "," + stockInCCASS + "," + stockNotInCCASS + "," + cgiHolding + ","
    			+ blacklistHolding + "," + cgiHolding_per + "," + blacklistHolding_per + "," + cgiOVERblacklist + ","
    			+ cgiOVERCCASS + "," + blacklistOVERCCASS + "," + cgiADDblacklist;
    	
    	String toWrite_2ndpart = "";
    	for(int k = 0; k < holdingValueList.length; k++){  // update "toWrite" 
    		String currentValue = holdingValueList[k];
    		String currentStake = holdingStakeList[k];
    		
    		// if currentValue == 0, change it to "-"
    		String toWrite_value = UtilityFunction.isDouble(currentValue )?Double.parseDouble(currentValue)==0.0?"-":currentValue :currentValue ;
    		
    		// if currentStake == 0, change it to "-"
    		String toWrite_stake = UtilityFunction.isDouble(currentStake)?Double.parseDouble(currentStake)==0.0?"-":currentStake :currentStake;
    		
    		toWrite = toWrite + "," + toWrite_value ;
    		toWrite_2ndpart = toWrite_2ndpart + "," + toWrite_stake ;
    	}
    	
    	toWrite = toWrite + toWrite_2ndpart;
    	
    	toWriteStrList.add(toWrite);
    	
		return toWriteStrList;
	}

	/**
	 * to write the output data into Excel and format
	 * @param toWriteStrList
	 * @throws Exception
	 */
	private static void writeDataIntoExcel(ArrayList<String> toWriteStrList, String date) throws Exception{
		try {
			//output file 
			FileOutputStream excelOut = new FileOutputStream(ConstVal.FILE_OUTPUT_PATH + "\\" + date + "\\margin loan mon " + date + ".xlsx"); 
			
			// create Excel
			Workbook wb = new XSSFWorkbook();
			
			Sheet sheet  = wb.createSheet(date); // create sheet
			
			// set front and size
			XSSFFont font = (XSSFFont) wb.createFont();
			font.setFontHeightInPoints((short) 12);
			font.setFontName("Arial Unicode MS");
			font.setColor(IndexedColors.BLACK.getIndex());
			font.setBold(false);
			font.setItalic(false);
			
			// set cell border
			XSSFCellStyle border_rightThick =  (XSSFCellStyle) wb.createCellStyle();
			XSSFCellStyle border_bottomThick =  (XSSFCellStyle) wb.createCellStyle();
			XSSFCellStyle border_rightBottomThick =  (XSSFCellStyle) wb.createCellStyle();
			XSSFCellStyle border_blank =  (XSSFCellStyle) wb.createCellStyle();
			border_rightThick.setBorderRight(BorderStyle.MEDIUM);
			border_bottomThick.setBorderBottom(BorderStyle.MEDIUM);
			border_rightBottomThick.setBorderBottom(BorderStyle.MEDIUM);
			border_rightBottomThick.setBorderRight(BorderStyle.MEDIUM);
			
			for(int i = 0; i < toWriteStrList.size(); i++) { // write toWriteStrList into Excel sheet 
				String line = toWriteStrList.get(i);
				String[] lineSplit = line.split(",");
				
				Row row = sheet.createRow(i); // create row
				
				for(int j = 0; j < lineSplit.length; j++) {
					Cell currentCell = row.createCell(j);
					String currentContent = lineSplit[j];
					
					// set cell style
					XSSFCellStyle currentCellStyle = (XSSFCellStyle) wb.createCellStyle();
					
					// if the value is a number
					boolean isDouble = UtilityFunction.isDouble(currentContent);
					boolean isInt = UtilityFunction.isInteger(currentContent);
					
					// write info of each cell into the current row
					if(isDouble && !isInt) { // is double
						currentCell.setCellValue(Double.parseDouble(currentContent)); 
						currentCellStyle.setDataFormat(1);
					}else if(isDouble && isInt) { // is int
						if(j == 1 && i > 0) { // stock code 
							currentCell.setCellValue(Long.parseLong(currentContent));
						}else { // other int, usage commas
							currentCell.setCellValue(Long.parseLong(currentContent));
						}
					}else { // others: shown in String
						currentCell.setCellValue(currentContent);
					}
					
					
					// set cell style
					if(i == 0) {// first row
						if(j == 0 || j == 2 || j == 4 || j == 7 || j == 10 || j == ((57-11)/2 + 10)) {
							currentCellStyle = border_rightBottomThick;
						}else {
							currentCellStyle = border_bottomThick;
						}
					}else {  // other rows
						if(j == 0 || j == 2 || j == 4 || j == 7 || j == 10 || j == ((57-11)/2 + 10)) {
							currentCellStyle = border_rightThick;
						}else {
							currentCellStyle = border_blank;
						}
					}
					currentCellStyle.setFont(font);
					
					currentCell.setCellStyle(currentCellStyle);
					
				}
			}
			
			
			wb.write(excelOut); // output excel
			
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
}
