package analyzeData;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
*/
import utils.Utils;
import webDownload.ConstVal;
import webDownload.UtilityFunction;

//þýºæœ¬ä¸Šèþýä¸ªclassæ²¡ç”¨äº†ãþýþý‚ãþý
public class AnalyzeData {

	private static final int topNStocks_notOwnedByCGI = 5; // stock not owned by CGI: num of stocks for each blacklist broker
	
	private static ArrayList<String> cgiBlacklist = new ArrayList<String>(Arrays.asList(ConstVal.CGI_BLACKLIST_BROKERS));
	
	private static final String DATE_FORMAT = "yyyy-MM-dd"; // in this class, the date format is always "yyyy-MM-dd"
	
	private static final String OUTPUT_FILE_NAME = "margin loan mon";
	private static final String OUTPUT_FILE_PATH = "D:\\stock data\\HKEX";
	
	private static final boolean TO_GET_CHANGE_DATA = true;
	
	public static int currentStockNum; //which stock is dealing, if it is deal the 10th stock, the value is 10
	/**
	 * this class is to extract and present the data in a good-looking way after all data is downloaded
	 * @param args
	 */
	public static void main(String[] args) {
		
		try{
			System.out.println("**************** Analyzing Data ******************");
			
			// configurations
			//String[] dates = {"2017-07-27","2017-07-28","2017-07-31","2017-08-01","2017-08-02","2017-08-03"};
			/*String[] dates = {"2017-07-03","2017-07-04","2017-07-05","2017-07-06","2017-07-07","2017-07-10","2017-07-11",
					"2017-07-12","2017-07-13","2017-07-14","2017-07-17","2017-07-18","2017-07-19",
					"2017-07-20","2017-07-21","2017-07-24","2017-07-25","2017-07-26","2017-07-27",
					"2017-07-28","2017-07-31","2017-08-01","2017-08-02","2017-08-03","2017-08-04"};*/
			String[] dates = {"2017-07-21"};
			
			
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
		BufferedReader bf_stocklist= new BufferedReader(new FileReader(OUTPUT_FILE_PATH + "\\cgi stock list.csv"));
		String[] stockOwnedByCGIList = bf_stocklist.readLine().split(",");
		
		ArrayList<String> stockArrayList = new ArrayList<String>(Arrays.asList(stockOwnedByCGIList));
		int stockListLen = stockOwnedByCGIList.length;
		
		//file to write
		File toWriteDir = new File(OUTPUT_FILE_PATH);
		if(!toWriteDir.exists() && !toWriteDir.isDirectory()){  // create dir if not exists
			toWriteDir.mkdir();
		}
		
		FileWriter fw = new FileWriter(OUTPUT_FILE_PATH + "\\" + date + "\\" + OUTPUT_FILE_NAME + "---.csv");
		
		
		// data to be written in the file
		ArrayList<String> toWriteStrList = new ArrayList<String>(); // store the content to write in an ArrayList first
		
		// get header
		String header = "stock code,CCASS(%),non CCASS(%),CCASS % daily change, CCASS % weekly change,CCASS % monthly change,"
				+ "BL stake % daily change, BL stake % weekly change,BL stake % monthly change,"
				+ "Sum of Flags,"
				+ "If BL/ CCASS > 25%,If BL/ CCASS > 50%,CCASS change > 5%,Price down > 5% in 3D,Yday Drop & Price Hi Lo > 6%,T-2 Drop & Price Hi Lo > 6%,"
				+ "Avg Volume 1D > (5D X 2),Avg Volume 5D > (20D X 2),"
				+ "CGI no of shares,CGI(HKD),BL(HKD),CGI stake(%),BL stake(%),CGI stake/BL stake," 
				+ "CGI/CCASS,BL/CCASS,(CGI+BL)/CCASS,"
				+ "CGI/ Avg 20D volume,Avg Volume 1D/5D -1,Avg Volume 5D/20D -1,PX_LAST,CHG_PCT_1D,CHG_PCT_2D,CHG_PCT_3D,CHG_PCT_5D,CHG_PCT_1M,"
				+ "VOLUME,VOLUME_AVG_5D,VOLUME_AVG_20D,"
				+ "Price High (T-1),Price Low (T-1),Price High (T-2),Price Low (T-2),Price High (T-3),Price Low (T-3)";
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
			currentStockNum = i;
			
			// data to collect
			String stockCode = stockOwnedByCGIList[i];  //þý©éþýþý¨è·ºstockþý”æþýþýƒãþýè««éþýþý—è¿¡þý¨ä¿´þý…æþý
			System.out.println("========== stock code = " + stockCode + " " + date + " ==========");
			
			toWriteStrList = addStockInfoLine(stockCode, date, toWriteStrList);
			
			//if(stockCode.equals("1140"))
				//Thread.sleep(1000 * 10000);
        	
		} // end of "for(int i = 0..."
		
		// ===================== to write into the file ========================
		for(int i = 0; i < toWriteStrList.size(); i++){
			fw.write(toWriteStrList.get(i) + "\n");
		}
		fw.close();
		
		//writeDataIntoExcel(toWriteStrList, date);
		//test
		//writePriceFile(new ArrayList<String>(Arrays.asList(stockOwnedByCGIList)), date);
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
		String[] holdingSharesList = new String[ConstVal.CGI_BLACKLIST_BROKERS.length];
		String[] holdingStakeList = new String[ConstVal.CGI_BLACKLIST_BROKERS.length];
		for(int j = 0; j < ConstVal.CGI_BLACKLIST_BROKERS.length; j++){  // initializing
			holdingSharesList[j] = "0";
			holdingStakeList[j] = "0";
		}
		String ccass = "NA";
    	String nonCCASS= "NA";
    	
    	String ccassDailyChange = "NA";
    	String ccassWeeklyChange = "NA";
    	String ccassMonthlyChange = "NA";
    	
    	String blDailyChange = "NA";
    	String blWeeklyChange = "NA";
    	String blMonthlyChange = "NA";
    	
    	String currentRowNum = String.valueOf(currentStockNum + 2);  // row number in the export excel
    	
    	String temp_sum = "SUM(K" + currentRowNum + ":R" + currentRowNum + ")"; // e.g.SUM(K2:R2)
    	String sumOfFlags = "\"=IF(" + temp_sum + "=0,\"\"\"\"," + temp_sum + ")\""; // e.g. =IF(SUM(K2:R2)=0,"",SUM(K2:R2))
    	
    	String bl_ccass_largerThan25 = "";
    	String bl_ccass_largerThan50 = "";
    	String ccassChange_largerThan5 = "";
    	String priceDown_largerThan5In3D = "\"=IFERROR(IF(AH2<=-5,1,\"\"),\"\")";
    	String YDrop_PriceHiLo_LargerThan6 = "";
    	String T_2Drop_PriceHiLo_LargerThan6 = "";
    	//String Avg1DVol_largerThanDoubleOf_5D = "\"=IFERROR(IF(X" + currentRowNum + ">1,1,\"\"\"\"),\"\"\"\")\"";
    	String Avg1DVol_largerThanDoubleOf_5D = "";
    	//String Avg1DVol_largerThanDoubleOf_20D ="\"=IFERROR(IF(Y" + currentRowNum + ">1,1,\"\"\"\"),\"\"\"\")\"";;
    	String Avg1DVol_largerThanDoubleOf_20D ="";
    	
    	String cgiShares = "NA";
    	
    	// e.g. =N2*Z2
    	//String cgiHKD = "=N" + currentRowNum + "*Z" + currentRowNum;
    	String cgiHKD = "NA";
    	String blHKD = "NA";
    	
    	String cgiStake = "NA";  // cgi holding (%)
    	String blStake = "NA"; // blacklist holding in total (%)
    	String cgiOverBl = "NA";  	// cgiHolding_per /blacklistHolding_per 
    	String cgiOverCCASS  = "NA";		// cgiHolding_per / stockInCCASS
    	String blOverCCASS  = "NA";		// blacklistHolding_per / stockInCCASS
    	String cgiADDblacklist = "NA";	// (cgiHolding_per + blacklistHolding_per) / stockInCCASS
    	
    	//e.g. =N3/AF3
    	//String cgiSharesOver20Vol = "=N" + currentRowNum + "/AF" + currentRowNum;	// cgi shares / 20D avg vol
    	String cgiSharesOver20Vol = "";
    	//e.g. =AD3/AE3-1
    	//String avgVol1DOver5DMinus1 = "=AD" + currentRowNum + "/AE" + currentRowNum;	// (avg vol 1D/5D) - 1
    	String avgVol1DOver5DMinus1 = "";
    	//e.g. =AE3/AF3-1
    	//String avgVol1DOver20DMinus1 = "=AE" + currentRowNum + "/AF" + currentRowNum; // (avg vol 1D/20D) - 1
    	String avgVol1DOver20DMinus1 = "";
    	
    	String price = "NA"; 
    	
    	String priceChange1D = "";
    	String priceChange2D = "";
    	String priceChange3D = "";
    	String priceChange5D = "";
    	String priceChange1M = "";
    	
    	String vol = "";
    	String volAvg5D = "";
    	String volAvg20D = "";
    	
    	String priceHigh_T_1 = "";
    	String priceLow_T_1 = "";
    	String priceHigh_T_2 = "";
    	String priceLow_T_2 = "";
    	String priceHigh_T_3 = "";
    	String priceLow_T_3 = "";
    	
    	
    	String blShares = "NA";
    	
    	// ================= to get "daily/weekly/monthly CCASS change" data =================
    	Map<String, ArrayList<String>> lastDateData = new HashMap();  // to store last date's data of a specific stock
    	Map<String, ArrayList<String>> lastWeekData = new HashMap();
    	Map<String, ArrayList<String>> lastMonthData = new HashMap();
    	if(TO_GET_CHANGE_DATA) {
	    	SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
	    	Calendar todayDate = Calendar.getInstance();
	    	todayDate.setTime(sdf.parse(date));
	    	
	    	// get dir list
	    	ArrayList<Calendar> fileDirDate = new ArrayList<Calendar>(); // get the dir with a form of date
	    	File rootDir = new File(OUTPUT_FILE_PATH);
	    	String[] listFilesStr = rootDir.list();
	    	File[] listFiles = rootDir.listFiles();
	
	    	for(int i = 0; i < listFiles.length; i++) {
	    		File thisFile = listFiles[i];
	    		if(thisFile.isDirectory() && UtilityFunction.isDate(listFilesStr[i], DATE_FORMAT)) { // if this file is a dir & is in the form of a date
	    			Calendar dirDate = Calendar.getInstance();
	    			dirDate.setTime(sdf.parse(listFilesStr[i]));
	    			
	    			fileDirDate.add(dirDate);
	    		}
	    	}
	    	
	    	// to get "CCASS % daily change, CCASS % weekly change,CCASS % monthly change" & "BL stake % daily change, BL stake % weekly change,BL stake % monthly change"
	    	// get daily change data
	    	Calendar lastDate = getPreviousDate(fileDirDate, todayDate, 1);
	    	//System.out.println("-- Previous date = " + sdf.format(lastDate.getTime()));
	    	String readPreviousDatePath = OUTPUT_FILE_PATH + "\\" + sdf.format(lastDate.getTime())+ "\\" + OUTPUT_FILE_NAME + ".csv";
	    	lastDateData = extractInfoFromCSV(readPreviousDatePath );
	    	
	    	// get weekly change data
	    	Calendar lastWeek = getPreviousDate(fileDirDate, todayDate, 2);
	    	//System.out.println("-- Previous week = " + sdf.format(lastWeek.getTime()));
	    	String readPreviousWeekPath = OUTPUT_FILE_PATH + "\\" + sdf.format(lastWeek.getTime())+ "\\" + OUTPUT_FILE_NAME + ".csv";
	    	lastWeekData = extractInfoFromCSV(readPreviousWeekPath );
	    	
	    	// get monthly change data
	    	Calendar lastMonth = getPreviousDate(fileDirDate, todayDate, 3);
	    	//System.out.println("-- Previous month = " + sdf.format(lastMonth.getTime()));
	    	String readPreviousMonthPath = OUTPUT_FILE_PATH + "\\" + sdf.format(lastMonth.getTime())+ "\\" + OUTPUT_FILE_NAME + ".csv";
	    	lastMonthData = extractInfoFromCSV(readPreviousMonthPath );
    	
    	}
    	//================ get stock's price & volume information & priceDown_largerThan5In3D ==================
    	ArrayList<Object> stockData = getStockHistDailyData(stockCode);
    	
    	SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT); 
    	Calendar refDate = Calendar.getInstance();
    	refDate.setTime(sdf.parse(date));
    	
    	price = getStockPrice(stockData, refDate, "close");
    	String priceClose_T_1 = getStockPrice(stockData, refDate, "close", -1);
    	priceHigh_T_1 = getStockPrice(stockData, refDate, "high", -1);
    	priceLow_T_1 = getStockPrice(stockData, refDate, "low", -1);
    	String priceClose_T_2 = getStockPrice(stockData, refDate, "close", -2);
    	priceHigh_T_2 = getStockPrice(stockData, refDate, "high", -2);
    	priceLow_T_2 = getStockPrice(stockData, refDate, "low", -2);
    	String priceClose_T_3 = getStockPrice(stockData, refDate, "close", -3);
    	priceHigh_T_3 = getStockPrice(stockData, refDate, "high", -3);
    	priceLow_T_3 = getStockPrice(stockData, refDate, "low", -3);
    	
    	priceChange1D = getStockNDayPriceChange(stockData, 1, refDate);
    	priceChange2D = getStockNDayPriceChange(stockData, 2, refDate);
    	priceChange3D = getStockNDayPriceChange(stockData, 3, refDate);
    	priceChange5D = getStockNDayPriceChange(stockData, 5, refDate);
    	priceChange1M = getStockNDayPriceChange(stockData, 20, refDate);
    	
    	if(UtilityFunction.isDouble(priceChange3D) && !priceChange3D.equals("")) {
    		double priceChange = Double.parseDouble(priceChange3D);
    		if(priceChange <= -0.05)
    			priceDown_largerThan5In3D = "1";
    	}
    	
    	vol = getStockNDayAvgVolume(stockData, 1, refDate);
    	volAvg5D = getStockNDayAvgVolume(stockData, 5, refDate);
    	volAvg20D = getStockNDayAvgVolume(stockData, 20, refDate);
    	
    	// =============== YDrop_PriceHiLo_LargerThan6, T_2Drop_PriceHiLo_LargerThan6 ==============
    	if(UtilityFunction.isDouble(priceClose_T_1) && UtilityFunction.isDouble(priceHigh_T_1) && UtilityFunction.isDouble(priceLow_T_1) && UtilityFunction.isDouble(priceClose_T_2)) {
    		double T_1_closePrice_double = Double.parseDouble(priceClose_T_1);
    		double T_1_highPrice_double = Double.parseDouble(priceHigh_T_1);
    		double T_1_lowPrice_double = Double.parseDouble(priceLow_T_1);
    		double T_2_closePrice_double = Double.parseDouble(priceClose_T_2);
    		if(T_1_closePrice_double < T_2_closePrice_double && (T_1_highPrice_double - T_1_lowPrice_double) / T_2_closePrice_double >= 0.06)
    			YDrop_PriceHiLo_LargerThan6 = "1";
    	}
    	if(UtilityFunction.isDouble(priceClose_T_2) && UtilityFunction.isDouble(priceHigh_T_2) && UtilityFunction.isDouble(priceLow_T_2) && UtilityFunction.isDouble(priceClose_T_3)) {
    		double T_2_closePrice_double = Double.parseDouble(priceClose_T_2);
    		double T_2_highPrice_double = Double.parseDouble(priceHigh_T_2);
    		double T_2_lowPrice_double = Double.parseDouble(priceLow_T_2);
    		double T_3_closePrice_double = Double.parseDouble(priceClose_T_3);
    		if(T_2_closePrice_double < T_3_closePrice_double && (T_2_highPrice_double - T_2_lowPrice_double) / T_3_closePrice_double >= 0.06)
    			T_2Drop_PriceHiLo_LargerThan6 = "1";
    	}
    	
    			
		// ================== read into the stock ==================
		String readStockPath = OUTPUT_FILE_PATH + "\\" + date + "\\" + stockCode + ".csv";
		
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
        	String holdingShares= "";
        	String holdingStake= "";
        	
        	String toWriteStr = null;
        	String[] splitStr = line.split(",");
        	
        	// deal with header
        	if(counter == 0){
            	 ccass = splitStr[7];
            	 nonCCASS = splitStr[9];
             }
             
        	// deal with other lines
        	brokerName = splitStr[1];
        	holdingShares = splitStr[2];
        	holdingStake = splitStr[4];
        	
        	// found the target broker
        	if(cgiBlacklist.contains(brokerName)){
        		int k = cgiBlacklist.indexOf(brokerName);
        		holdingSharesList[k] = holdingShares;
        		holdingStakeList[k] = holdingStake;
        	}
        	
        	// get cgi data
        	if(brokerName.equals(ConstVal.CGI_NAME)){
        		cgiShares = holdingShares;
        		cgiHKD = String.valueOf(Double.parseDouble(holdingShares) * Double.parseDouble(price));
        		cgiStake = holdingStake; // cgi stake
        		cgiOverCCASS = String.valueOf(Double.parseDouble(cgiStake) / Double.parseDouble(ccass));
        	}
             
        	counter++;
        }  // end of while
        bufReader.close();  
        inReader.close();  
        in.close(); 
        
     // add to write list
    	long blacklistTotalShares = 0;
    	double blacklistTotalStake = 0;
    	for(int k = 0; k < holdingSharesList.length; k++){  // calculating "blacklist (HKD)" & "blacklist stake" 
    		//toWrite = toWrite + "," + holdingValueList[k] + "," + holdingStakeList[k];
    		blacklistTotalShares = blacklistTotalShares + Long.parseLong(holdingSharesList[k]);
    		blacklistTotalStake = blacklistTotalStake + Double.parseDouble(holdingStakeList[k]);
    	}
    	
    	if(counter > 0) {  // to avoid no-data case
    		blShares = String.valueOf(blacklistTotalShares);
    		blHKD = String.valueOf(blacklistTotalShares * Double.parseDouble(price));
        	blStake = String.valueOf(blacklistTotalStake);
        	cgiOverBl = String.valueOf(Double.parseDouble(cgiStake) / Double.parseDouble(blStake));
        	cgiOverCCASS = String.valueOf(Double.parseDouble(cgiStake) / Double.parseDouble(ccass));
        	blOverCCASS = String.valueOf(Double.parseDouble(blStake) / Double.parseDouble(ccass));
        	cgiADDblacklist = String.valueOf(Double.parseDouble(cgiOverCCASS) + Double.parseDouble(blOverCCASS));
    	}
    	
    	// ====================== get daily/weekly/monthly CCASS&BL change data ======================
    	if(TO_GET_CHANGE_DATA) {
    		// get daily change data
        	String lastDateCCASS = "";
        	String lastDateBlStake = "";
        	ArrayList<String> lastDateDataByStock = lastDateData.get(stockCode);
        	if(lastDateDataByStock != null) {  // get the stock
        		ArrayList<String> header = lastDateData.get("header");
        		
        		int ccass_ind = header.indexOf("CCASS(%)");
        		int blStake_ind = header.indexOf("BL stake(%)");
        		
        		lastDateCCASS = lastDateDataByStock.get(ccass_ind);
        		lastDateBlStake = lastDateDataByStock.get(blStake_ind);
        		
        		//System.out.println("-- lastDateCCASS = " + lastDateCCASS + " lastDateBlStake = " + lastDateBlStake );
        	}else {
        		lastDateCCASS = "NA";
        		lastDateBlStake = "NA";
        	}
        	if(UtilityFunction.isDouble(lastDateCCASS) && UtilityFunction.isDouble(ccass)) {
        		double dailyChange = Double.parseDouble(ccass) - Double.parseDouble(lastDateCCASS);
        		ccassDailyChange = dailyChange == 0? "":String.valueOf(dailyChange);
        		//System.out.println("-- ccassDailyChange = " + ccassDailyChange );
        	}
        	if(UtilityFunction.isDouble(lastDateBlStake) && UtilityFunction.isDouble(blStake)) {
        		double dailyChange = Double.parseDouble(blStake) - Double.parseDouble(lastDateBlStake);
        		blDailyChange = dailyChange == 0? "":String.valueOf(dailyChange);
        		//System.out.println("-- blDailyChange = " + blDailyChange );
        	}
        	//Thread.sleep(1000 * 100000); // pause
        	
        	// get weekly change data
        	String lastWeekCCASS = "";
        	String lastWeekBlStake = "";
        	ArrayList<String> lastWeekDataByStock = lastWeekData.get(stockCode);
        	if(lastWeekDataByStock!= null) {
        		ArrayList<String> header = lastWeekData.get("header");
        		
        		int ccass_ind = header.indexOf("CCASS(%)");
        		int blStake_ind = header.indexOf("BL stake(%)");
        		
        		lastWeekCCASS = lastWeekDataByStock.get(ccass_ind);
        		lastWeekBlStake = lastWeekDataByStock.get(blStake_ind);
        	}else {
        		lastWeekCCASS = "NA";
        		lastWeekBlStake = "NA";
        	}
        	if(UtilityFunction.isDouble(lastWeekCCASS) && UtilityFunction.isDouble(ccass)) {
        		double weeklyChange = Double.parseDouble(ccass) - Double.parseDouble(lastWeekCCASS);
        		ccassWeeklyChange = weeklyChange  == 0? "":String.valueOf(weeklyChange );
        	}
        	if(UtilityFunction.isDouble(lastWeekBlStake) && UtilityFunction.isDouble(blStake)) {
        		double weeklyChange = Double.parseDouble(blStake) - Double.parseDouble(lastWeekBlStake);
        		blWeeklyChange = weeklyChange == 0? "":String.valueOf(weeklyChange);
        	}
        	
        	// get monthly change data
        	String lastMonthCCASS = "";
        	String lastMonthBlStake = "";
        	ArrayList<String> lastMonthDataByStock = lastMonthData.get(stockCode);
        	if(lastMonthDataByStock != null) {
        		ArrayList<String> header = lastMonthData.get("header");
        		
        		int ccass_ind = header.indexOf("CCASS(%)");
        		int blStake_ind = header.indexOf("BL stake(%)");
        		
        		lastMonthCCASS = lastMonthDataByStock.get(ccass_ind);
        		lastMonthBlStake = lastMonthDataByStock.get(blStake_ind);
        	}else {
        		lastMonthCCASS = "NA";
        		lastMonthBlStake = "NA";
        	}
        	if(UtilityFunction.isDouble(lastMonthCCASS) && UtilityFunction.isDouble(ccass)) {
        		double monthlyChange = Double.parseDouble(ccass) - Double.parseDouble(lastMonthCCASS);
        		ccassMonthlyChange = monthlyChange  == 0? "":String.valueOf(monthlyChange );
        	}
        	if(UtilityFunction.isDouble(lastMonthBlStake) && UtilityFunction.isDouble(blStake)) {
        		double monthlyChange = Double.parseDouble(blStake) - Double.parseDouble(lastMonthBlStake);
        		blMonthlyChange = monthlyChange == 0? "":String.valueOf(monthlyChange);
        	}
    	}
    	
    	// =============== ccassChange_largerThan5 ============
    	boolean is_ccassChange_largerThan5 = false;
    	if(UtilityFunction.isDouble(ccassDailyChange) && ccassDailyChange != "") {
    		double dailyChange = Double.parseDouble(ccassDailyChange);
    		if(dailyChange >= 5 || dailyChange <= -5)
    			is_ccassChange_largerThan5 = true;
    	}
    	if(UtilityFunction.isDouble(ccassWeeklyChange) && ccassWeeklyChange != "") {
    		double weeklyChange = Double.parseDouble(ccassWeeklyChange);
    		if(weeklyChange >= 5 || weeklyChange <= -5)
    			is_ccassChange_largerThan5 = true;
    	}
    	if(UtilityFunction.isDouble(ccassMonthlyChange) && ccassMonthlyChange != "") {
    		double monthlyChange = Double.parseDouble(ccassMonthlyChange);
    		if(monthlyChange >= 5 || monthlyChange <= -5)
    			is_ccassChange_largerThan5 = true;
    	}
    	if(is_ccassChange_largerThan5)
    		ccassChange_largerThan5 = "1";
    	
    	// =============== bl_ccass_largerThan25, bl_ccass_largerThan50 ==================
    	if(UtilityFunction.isDouble(blStake) && UtilityFunction.isDouble(ccass)) {
    		if(Double.parseDouble(blStake) / Double.parseDouble(ccass) >= 0.25) {
    			bl_ccass_largerThan25 = "1";
        	}
    		if(Double.parseDouble(blStake) / Double.parseDouble(ccass) >= 0.5) {
    			bl_ccass_largerThan50 = "1";
        	}
    	}
    	
    	/*
    	// ============== sumOfFlags =============
    	int flagsSum = 0;
    	int dummy1 = bl_ccass_largerThan25.equals("1")?flagsSum++:0;
    	int dummy2 = bl_ccass_largerThan50.equals("1")?flagsSum++:0;
    	int dummy3 = ccassChange_largerThan5.equals("1")?flagsSum++:0;
    	int dummy4 = priceDown_largerThan5In3D.equals("1")?flagsSum++:0;
    	int dummy5 = YDrop_PriceHiLo_LargerThan6.equals("1")?flagsSum++:0;
    	int dummy6 = T_2Drop_PriceHiLo_LargerThan6.equals("1")?flagsSum++:0;
    	sumOfFlags = flagsSum == 0?"":String.valueOf(flagsSum);
    	*/
    	
    	// ============== cgiSharesOver20Vol, avgVol1DOver5DMinus1, avgVol1DOver20DMinus1 =======
    	try {
    		cgiSharesOver20Vol = String.valueOf(Double.parseDouble(cgiShares) / Double.parseDouble(volAvg20D));
    	}catch(Exception e) {
    		cgiSharesOver20Vol = "-";
    	}
    	try {
    		avgVol1DOver5DMinus1 = String.valueOf(Double.parseDouble(vol) / Double.parseDouble(volAvg5D) - 1);
    	}catch(Exception e) {
    		avgVol1DOver5DMinus1 = "-";
    	}
    	try {
    		avgVol1DOver20DMinus1 = String.valueOf(Double.parseDouble(vol) / Double.parseDouble(volAvg20D) - 1);
    	}catch(Exception e) {
    		avgVol1DOver20DMinus1 = "-";
    	}
    	//=============== Avg1DVol_largerThanDoubleOf_5D, Avg1DVol_largerThanDoubleOf_20D ==============
    	try {
    		if(Double.parseDouble(avgVol1DOver5DMinus1) > 1) {
    			Avg1DVol_largerThanDoubleOf_5D = "1";
    		}
    		else {
    			Avg1DVol_largerThanDoubleOf_5D = "";
    		}
    	}catch(Exception e) {
    		Avg1DVol_largerThanDoubleOf_5D = "";
    	}
    	try {
    		if(Double.parseDouble(avgVol1DOver20DMinus1) > 1) {
    			Avg1DVol_largerThanDoubleOf_20D = "1";
    		}
    		else {
    			Avg1DVol_largerThanDoubleOf_20D = "";
    		}
    	}catch(Exception e) {
    		Avg1DVol_largerThanDoubleOf_20D = "";
    	}
    	
    	// =============== consolidation =================
    	String toWrite = stockCode + "," + ccass + "," + nonCCASS + "," + ccassDailyChange + "," + ccassWeeklyChange + "," + ccassMonthlyChange + ","
    			+ blDailyChange + "," + blWeeklyChange + "," + blMonthlyChange + "," 
    			+ sumOfFlags + ","
    			+ bl_ccass_largerThan25 + "," + bl_ccass_largerThan50 + "," 
    			+ ccassChange_largerThan5 + "," + priceDown_largerThan5In3D + "," + YDrop_PriceHiLo_LargerThan6 + "," + T_2Drop_PriceHiLo_LargerThan6 + ","
    			+ Avg1DVol_largerThanDoubleOf_5D + "," + Avg1DVol_largerThanDoubleOf_20D + ","
    			+ cgiShares + "," + cgiHKD + "," + blHKD + "," 
    			+ cgiStake + "," + blStake + "," + cgiOverBl + ","
    			+ cgiOverCCASS + "," + blOverCCASS + "," + cgiADDblacklist + ","
    			+ cgiSharesOver20Vol + "," + avgVol1DOver5DMinus1 + "," + avgVol1DOver20DMinus1 + ","
    			+ price + "," + priceChange1D + "," + priceChange2D + ","+ priceChange3D + "," + priceChange5D + "," + priceChange1M + ","
    			+ vol + "," + volAvg5D + "," + volAvg20D + ","
    			+ priceHigh_T_1 + "," + priceLow_T_1 + "," + priceHigh_T_2 + "," + priceLow_T_2 + "," + priceHigh_T_3 + "," + priceLow_T_3
    			;
    	
    	// to write the holding details for every blacklist broker
    	String toWrite_2ndpart = "";
    	for(int k = 0; k < holdingSharesList.length; k++){  // update "toWrite" 
    		//e.g. =Z2*1158400
    		//String currentValue = "=Z" + currentRowNum + "*" + holdingSharesList[k];
    		String currentValue = String.valueOf(Double.parseDouble(price) * Double.parseDouble(holdingSharesList[k]));
    		String currentStake = holdingStakeList[k];
    		
    		// if currentValue == 0, change it to "-"
    		String toWrite_value = UtilityFunction.isDouble(holdingSharesList[k] )?Double.parseDouble(holdingSharesList[k])==0.0?"-":currentValue :currentValue ;
    		
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
			/*
			//output file 
			FileOutputStream excelOut = new FileOutputStream(OUTPUT_FILE_PATH + "\\" + date + "\\" + OUTPUT_FILE_NAME + " " + date + ".xlsx"); 
			
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
			*/
			
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * to extract info from a csv. Return in the format of "Map<String, ArrayList<String>>"
	 * "header" <-> header of every column
	 * other indexes are stock codes, e.g.
	 * "6881" <-> "....data"
	 * @param filePath
	 * @return
	 * @throws Exception
	 */
	private static Map<String, ArrayList<String>> extractInfoFromCSV(String filePath) throws Exception{
		Map<String, ArrayList<String>> stockData = new HashMap();
		
		// read the file
		FileInputStream in = new FileInputStream(filePath);  
        InputStreamReader inReader = new InputStreamReader(in, "UTF-8");  
        BufferedReader bufReader = new BufferedReader(inReader);  
        String line = null;  
        int counter = 0;
        ArrayList<String> header = new ArrayList<String>(); // to store the header
        while((line = bufReader.readLine()) != null){  
        	if(counter == 0) { // header
        		header.addAll(Arrays.asList(line.split(",")));
        		
        		stockData.put("header", header);
        	}else { // not the header
        		ArrayList<String> content = new ArrayList<String>();
        		String[] contentStrArr = line.split(",");
        		String stockCode = contentStrArr[0];
        		content.addAll(Arrays.asList(contentStrArr));
        		
        		stockData.put(stockCode, content);
        	}
        	counter++;
        }
        inReader.close();
        bufReader.close();
        in.close();
		
		return stockData;
	}
	
	/**
	 * 
	 * @param dateList
	 * @param todayDate
	 * @param period // 1 - get last date, 2 - get last week date, 3 - get last month date
	 * @return
	 * @throws Exception
	 */
	private static Calendar getPreviousDate(ArrayList<Calendar> dateList, Calendar todayDate, int period) throws Exception{
		Calendar  previousCal = Calendar .getInstance();
		previousCal.setTime(todayDate.getTime());
		
		String msg = "";
		switch(period) {
		case 1:
			previousCal.add(Calendar.DATE, -1);
			msg = "day";
			break;
		case 2:
			previousCal.add(Calendar.WEEK_OF_MONTH, -1);
			msg = "week";
			break;
		case 3:
			previousCal.add(Calendar.MONTH, -1);
			msg = "month";
			break;
		default:
			previousCal.add(Calendar.DATE, -1);
			break;
		}
	
		Date previousDate = previousCal.getTime();
		
		Calendar toReturn = Calendar.getInstance();
		long daysDiff = -157;
		Calendar earliestDate = Calendar.getInstance(); // if there was no satisfied date available, return the earliest date in the array
		earliestDate.setTime(todayDate.getTime());
		long temp_daysDiff = 0;
		for(Calendar cal: dateList) {
			Date date = cal.getTime();
			
			long thisDaysDiff = (long) (date.getTime() - previousDate.getTime()) / (1000*3600*24);
			
			if(thisDaysDiff <= 0 && thisDaysDiff > daysDiff) { // thisDaysDiff < 0: this date is previous of last month date
				toReturn.setTime(date);
				daysDiff = thisDaysDiff ;
			}
			
			// get the earliest date
			long temp_thisDaysDiff = (long) (date.getTime() - todayDate.getTime().getTime()) / (1000*3600*24);
			if(temp_thisDaysDiff < temp_daysDiff) {
				earliestDate.setTime(date);
				temp_daysDiff =temp_thisDaysDiff; 
			}
		}
		if(daysDiff == -157) { // no previous date
			//System.out.println("No such date one " + msg + " before! To return the earliest!");
			toReturn = earliestDate;
		}
		
		return toReturn ;
	}
	
	/**
	 * to return stock data in the form of ArrayList<Object>
	 * the 1st object: contains "header"
	 * the subsequent object: String[], contains a row of data with col names specified in "header"
	 * @param stockCode
	 * @return
	 */
	public static ArrayList<Object> getStockHistDailyData(String stockCode) throws Exception{
		ArrayList<Object> stockData = new ArrayList<Object>();
		
		//======= read file =========
		String readFilePath = "D:\\stock data\\stock daily data\\" + stockCode + ".csv";
		
		FileInputStream in = new FileInputStream(readFilePath);  
        InputStreamReader inReader = new InputStreamReader(in, "UTF-8");  
        BufferedReader bufReader = new BufferedReader(inReader);  
        String line = null;  
        int counter = 0;
        while((line = bufReader.readLine()) != null){  
        	if(counter == 0) { 
        		stockData.add(line.split(","));
        	}else{
        		String[] data = line.split(",");
        		
        		//e.g. 2017-07-26  to convert the date to standard form
        		SimpleDateFormat sdf = new SimpleDateFormat ("yyyy-MM-dd");
        		SimpleDateFormat sdf_standard = new SimpleDateFormat(DATE_FORMAT);
        		
        		Calendar cal0 = Calendar.getInstance();
        		//System.out.println("data[0] = " + data[0]);
        		cal0.setTime(sdf.parse(data[0]));
        		
        		Calendar cal1 = Calendar.getInstance();
        		cal1.setTime(sdf.parse(data[1]));
        		
        		data[0] = sdf_standard.format(cal0.getTime());
        		data[1] = sdf_standard.format(cal1.getTime());
        		
        		stockData.add(data);
        	}
        	counter++;
        }
		
		
		return stockData;
	}
	
	/**
	 * get N day avg volume. Input stock data in the form of ArrayList<Object>
	 * for more meanings of the input ArrayList<Object>, please refer to function "getStockHistDailyData(String)"
	 * N=1 -> get refDate's volume
	 * @param stockData
	 * @param N
	 * @param refDate
	 * @return
	 * @throws Exception
	 */
	public static String getStockNDayAvgVolume(ArrayList<Object> stockData, int N, Calendar refDate) throws Exception{
		String avgVol = null;
		
		SimpleDateFormat sdf_standard = new SimpleDateFormat(DATE_FORMAT);
		String refDateStr = sdf_standard.format(refDate.getTime());
		
		int refDateInd = -1;
		for(int i = 1; i < stockData.size(); i++) { // skip the first element because it is the "header"
			String[] thisLineData = (String[]) stockData.get(i);
			String date = thisLineData[0];
			
			if(date.equals(refDateStr)) {  // get the dates
				refDateInd = i;
				break;
			}
		}
		
		// calculate avg vol
		Double cumVol = (double) 0;  // cumulative vol
		if(refDateInd > 0) {  // if there is data at refDate
			for(int i = refDateInd; i < Math.min(refDateInd+N, stockData.size()-1); i++) {
				String[] thisLineData = (String[]) stockData.get(i);
				String vol = thisLineData[8];
				cumVol = cumVol + Double.parseDouble(vol);
				
				//System.out.println("i = " + i + " vol = " + vol);
			}
			
			if(N == 0) { // some error-proof codes
				avgVol = String.valueOf(cumVol);
			}else {
				avgVol = String.valueOf(cumVol/N);
			}
		}else {
			avgVol = "NA";
		}
		
		return avgVol;
	}
	
	/**
	 * get the time series data of a single stock
	 * field - close, high, low
	 * shift - positive number: shift into future, negative number: shift into past
	 * @param stockData
	 * @param refDate
	 * @param field
	 * @param shift
	 * @return
	 * @throws Exception
	 */
	public static String getStockPrice(ArrayList<Object> stockData, Calendar refDate, String field, int shift) throws Exception{
		// field - close, high, low
		// shift - positive number: shift into future, negative number: shift into past
		String price = null;
		String closePrice = null;
		String highPrice = null;
		String lowPrice = null;
		
		SimpleDateFormat sdf_standard = new SimpleDateFormat(DATE_FORMAT);
		String refDateStr = sdf_standard.format(refDate.getTime());
		
		int refDateInd = 0;
		for(int i = 1; i < stockData.size(); i++) { // skip the first element because it is the "header"
			String[] thisLineData = (String[]) stockData.get(i);
			String date = thisLineData[0];
			
			if(date.equals(refDateStr)) {  // get the dates
				refDateInd = i;
				break;
			}
		}
		
		// shift the refDate
		refDateInd = refDateInd - shift;
		if(refDateInd > stockData.size() - 1)
			refDateInd = stockData.size() - 1;
		if(refDateInd < 1)
			refDateInd = 1;
		
		// get the price
		int lastUnsuspendedInd = getLastUnsuspendedDataLineInd(stockData, refDateInd);
		if(lastUnsuspendedInd > 0) {
			String[] thisLineData = (String[]) stockData.get(lastUnsuspendedInd);
			closePrice = thisLineData[3];
			
			if(lastUnsuspendedInd > refDateInd) {  // if refDate is suspended for trading, then its high & low equal close
				highPrice = closePrice;
				lowPrice = closePrice;
			}else {  // if refDate is not suspended
				lowPrice = thisLineData[6];
				highPrice = thisLineData[7];
			}
		}else {
			// reach the last line, still halted for trading, use the "adjusted close price" instead
			String[] thisLineData = (String[]) stockData.get(stockData.size()-1);
			closePrice = thisLineData[11];
			highPrice = closePrice;
			lowPrice = closePrice;
		}
		
		switch(field.toLowerCase()) {
		case "close":
			price = closePrice;
			break;
		case "high":
			price = highPrice;
			break;
		case "low":
			price = lowPrice;
			break;
		default:
			price = closePrice;
			break;
		}
				
		return price;
	}
	
	public static String getStockPrice(ArrayList<Object> stockData, Calendar refDate, String field) throws Exception{
		return getStockPrice(stockData, refDate, field, 0);
	}
	
	public static String getStockNDayPriceChange(ArrayList<Object> stockData, int N, Calendar refDate) throws Exception{
		String priceChange = null; // in decimal, not percentage
		String priceRef = null;
		
		SimpleDateFormat sdf_standard = new SimpleDateFormat(DATE_FORMAT);
		String refDateStr = sdf_standard.format(refDate.getTime());
		
		int refDateInd = -1;
		for(int i = 1; i < stockData.size(); i++) { // skip the first element because it is the "header"
			String[] thisLineData = (String[]) stockData.get(i);
			String date = thisLineData[0];
			
			if(date.equals(refDateStr)) {  // get the dates
				refDateInd = i;
				break;
			}
		}
		
		// to calculate the price change, it is enough to use adj price
		// get the current price
		double currentPrice = 0;
		if(refDateInd  > 0) {
			String[] currentData = (String[]) stockData.get(refDateInd);  // always use the adj price
			currentPrice = Double.parseDouble(currentData[11]);
		}
		
		
		if(false) { // if stock trading haulted that date, it is not considerred as a trading date
			int lastUnsuspendedInd = getLastUnsuspendedDataLineInd(stockData, refDateInd);
			if(lastUnsuspendedInd > 0) {
				String[] thisLineData = (String[]) stockData.get(lastUnsuspendedInd);
				currentPrice = Double.parseDouble(thisLineData[3]);
			}else {
				// reach the last line, still halted for trading, only possible to return 0 
				return "0";
			}
		}
		
		
		// get N day before price
		double NDayBeforePrice = 0;
		if(refDateInd  > 0) {
			String[] NDayBeforeData = (String[]) stockData.get(Math.min(refDateInd + N, stockData.size()-1));  // always use the adj price
			NDayBeforePrice = Double.parseDouble(NDayBeforeData[11]);
		}
		
		if(false) {
			int lastUnsuspendedInd = getLastUnsuspendedDataLineInd(stockData, refDateInd);
			int lastUnsuspendedInd_NDay = getLastUnsuspendedDataLineInd(stockData, lastUnsuspendedInd + N);
			String[] thisLineData_NDay = (String[]) stockData.get(lastUnsuspendedInd);
			if(lastUnsuspendedInd_NDay > 0) {
				NDayBeforePrice = Double.parseDouble(thisLineData_NDay[3]);
			}else {
				// reach the last line, still halted for trading, only possible to return 0 
				NDayBeforePrice = Double.parseDouble(thisLineData_NDay[11]);
			}
		}
		
		try {
			priceChange = String.valueOf((currentPrice-NDayBeforePrice) / NDayBeforePrice);
		}catch(Exception e) {
			priceChange = "NA";
		}
		
		
		return priceChange;
	}
	
	/**
	 * some stocks got suspended during some trading days
	 * to get the index of the last un-suspended trading date
	 * e.g. if the stock hauts trading on 16/5/2017, the last un-suspended trading date may be 4/4/2017
	 * @param stockData
	 * @param ind
	 * @return
	 * @throws Exception
	 */
	public static int getLastUnsuspendedDataLineInd(ArrayList<Object> stockData, int ind) throws Exception{
		int unSuspendedInd = -1;
		
		for(int i = ind; i < stockData.size(); i++) {
			String[] thisLineData = (String[]) stockData.get(i);
			String isSusp = thisLineData[2]; // is the stock suspended trading
			
			if(isSusp.equals("0")) {
				unSuspendedInd = i;
				break;
			}
			if(isSusp.equals("1") && i == stockData.size() - 1) { 
				// reach the last line, still halted for trading, return -1
				unSuspendedInd = -2;
			}
			
		}
		
		return unSuspendedInd;
	}
	
	
	
}
