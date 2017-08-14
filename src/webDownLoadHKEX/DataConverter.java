package webDownLoadHKEX;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import utils.Utils;;

public class DataConverter {
	
	public static ArrayList<Calendar> allTradingDate = new ArrayList<Calendar>();  // ascending,i.e. 20170715 is before 20170815
	
	public static String today_all_CCASS_value_name = "today all CCASS shares";
	public static String today_all_CCASS_stake_name = "today all CCASS stake";
	public static boolean today_all_CCASS_write_CCASS_summary = true;  // if to write column B&C
	
	public static String today_BL_CCASS_stake_name = "today BL CCASS stake";
	public static String today_BL_CCASS_value_name = "today BL CCASS shares";
	public static String hist_BL_CCASS_stake_name = "hist BL CCASS stake";
	public static String hist_all_CCASS_stake_name = "hist all CCASS stake";
	public static String hist_stock_info_name = "hist stock info";
	
	public static String all_broker_name_list_path = "D:\\stock data\\HKEX\\CCASS Participants List.csv";
	public static String blacklist_broker_name_list_path = "D:\\stock data\\HKEX\\cgi blacklist broker.csv";
	
	public static boolean setAllTradingDate() {
		boolean isOK = true;
		try {
			if(allTradingDate == null || allTradingDate.size() == 0) {
				// get trading date
				BufferedReader bf = utils.Utils.readFile_returnBufferedReader(utils.Utils.TRADING_DATE_FILE_PATH);
				String dateLine = bf.readLine();
				String[] dateLineArr = dateLine.split(",");
				allTradingDate = utils.Utils.dateStr2Cal(dateLineArr, "dd/MM/yyyy");
				Collections.sort(allTradingDate); // sorting ascendingly
			}
		}catch(Exception e) {
			e.printStackTrace();
			isOK = false;
		}
		
		return isOK;
	}
	
	/**
	 * Convert HTML data into single csv
	 * @param stockCode
	 * @param date
	 * @return
	 */
	public static boolean dataConverter(String stockCode, String date) {
		boolean isOK = true;
		try {
				/////////// parse html and rewrite data /////////////////
				// write file
				String str_output_path = utils.Utils.OUTPUT_ROOT_PATH_HKEX + "\\" + date + "\\" + stockCode + ".csv";
				FileWriter fw2 = new FileWriter(str_output_path);
				
				//parse html
				File html_read = new File(utils.Utils.OUTPUT_ROOT_PATH_HKEX + "\\" + date + "\\" + stockCode + ".html");
				Document doc = (Document) Jsoup.parse(html_read, "utf-8", "");
				
				//check if the file is correct
				Element alertMsg = doc.getElementById("alertMsg");
				String alertMsgStr = alertMsg.val();  
				if(alertMsgStr.contains("does not exist OR not available for enquiry")) {
					fw2.close();
					return isOK;  // if there is an alter msg, the stock was not yet listed then
				}
				
				//============= get CCASS data =============
				String stakesInCCASS = "";
				String stakesNotInCCASS = "";
				Element pnlResultSummary = doc.getElementById("pnlResultSummary");
				Elements pnlTRArr = pnlResultSummary.getElementsByTag("tr");
				Element pnlTD = null;
				boolean isExistPercentageCol = true;  // sometimes there will be no col for "percentage"
				if(pnlTRArr.size() >= 2) {
					Element pnlTR = pnlTRArr.get(pnlTRArr.size()-2);
					Elements pnlTDArr = pnlTR.getElementsByTag("td");
					pnlTD = pnlTDArr.get(3);
					
					DecimalFormat df = new DecimalFormat("0.00");
					
					String temp = pnlTD.text();
					if(temp == null || temp.length() == 0 || temp.equals("")) {
						stakesInCCASS = "NA";
						stakesNotInCCASS = "NA";
						isExistPercentageCol = false;
					}else {
						//System.out.println("temp = " + temp + "--");
						if(utils.Utils.isDouble(temp.substring(0, temp.indexOf('%')))){
							stakesInCCASS = temp.substring(0, temp.indexOf('%'));
							stakesNotInCCASS = String.valueOf(df.format(100.0 - Double.parseDouble(stakesInCCASS)));
						}else {
							stakesInCCASS = "NA";
							stakesNotInCCASS = "NA";
							isExistPercentageCol = false;
						}
					}
				}else { // there's no head table at all
					stakesInCCASS = "NA";
					stakesNotInCCASS = "NA";
					isExistPercentageCol = false;
				}
				
				
				fw2.write("CCASS ID,Name,Holding,Last Change,Stake%,"
							+ "Cum Stake%,stakes in CCASS(%)," + stakesInCCASS 
							+ ",stakes not in CCASS(%)," + stakesNotInCCASS + "\n"); // writer header
				
				//============= get holding data =============
				Element participantShareholdingList = doc.getElementById("participantShareholdingList");
				if(participantShareholdingList != null) {  // if there is such table
					Elements dataRowArr = participantShareholdingList.getElementsByTag("tr");
					for(int i = 3; i < dataRowArr.size(); i++) {
						Element dataRow = dataRowArr.get(i);
						Elements dataRowTD = dataRow.getElementsByTag("td");
						
						String ParticipantID = dataRowTD.get(0).text();
						String ParticipantName = dataRowTD.get(1).text().replace(",", ""); //ParticipantName.replace("\t", "");
						String Shareholding = dataRowTD.get(3).text().replace(",", "");
						
						String Percentage = "";
						if(isExistPercentageCol) {
							Percentage = dataRowTD.get(4).text();
							Percentage = Percentage.substring(0, Percentage.indexOf('%'));
						}
						String toWrite = ParticipantID + "," + ParticipantName + ","
								+ Shareholding + ",," + Percentage + ",\n";
						fw2.write(toWrite);
					}
				}
				
				fw2.close();
		}catch(Exception e) {
			e.printStackTrace();
			isOK = false;
		}
		
		return isOK;
	}

	/**
	 * The CCASS data is stored separately in a file for each stock
	 * This function combines all data into one file 
	 * @param stockArr
	 * @param date
	 * @return
	 */
	public static boolean csvDataConsolidation(ArrayList<String> stockArr, String date){
		boolean isOK = true;
		
		try {
			// writer file path
			String str_output_path1 = utils.Utils.OUTPUT_ROOT_PATH_HKEX + "\\" + date + "\\" + today_all_CCASS_value_name + ".csv";
			FileWriter fw1 = new FileWriter(str_output_path1);
			String str_output_path2 = utils.Utils.OUTPUT_ROOT_PATH_HKEX + "\\" + date + "\\" + today_all_CCASS_stake_name + ".csv";
			FileWriter fw2 = new FileWriter(str_output_path2);
			
			//=========== get all brokers' names & short names ===========
			ArrayList<String> brokerFullNameArr = new ArrayList<String>();
			ArrayList<String> brokerShortNameArr = new ArrayList<String>();
			ArrayList<String> brokerIDArr = new ArrayList<String>();
			BufferedReader bf = utils.Utils.readFile_returnBufferedReader(all_broker_name_list_path);
			String line = "";
			int counter1 = 0;
			String header1;
			String header2;
			if(today_all_CCASS_write_CCASS_summary) { // header to write
				header1 = date + ",,"; 
				header2 = ",stake in CCASS (%),stake not in CCASS (%)";
			}else {
				header1 = date;
				header2 = "";
			}
			while((line = bf.readLine()) != null) {
				String[] lineArr = line.split(",");
				if(counter1 == 0) {  // first line
					counter1++;
					continue;
				}
				counter1++;
				
				if(lineArr[0] == null || lineArr[0] == "" || lineArr[0].length() == 0) // only store brokers with CCASS ID
					break;
				
				brokerIDArr.add(lineArr[0]);
				header1 = header1 + "," + lineArr[0];
				
				brokerFullNameArr.add(lineArr[1]);
				header2 = header2 + "," + lineArr[1];
				
				brokerShortNameArr.add(lineArr[2]);
				//header2 = header2 + "," + lineArr[2];
			}
			
			//=========== write header ===========
			fw1.write(header1 + "\n");
			fw1.write(header2 + "\n");
			fw2.write(header1 + "\n");
			fw2.write(header2 + "\n");
			
			//=========== construct the template for each line data to write ===========
			ArrayList<String> dataTemplate = new ArrayList<String>();
			for(int i = 0; i < brokerShortNameArr.size(); i++)
				dataTemplate.add("");
			
			//=========== read data for every stock and write ===========
			for(int i = 0; i < stockArr.size(); i++) {
				String stockCode = stockArr.get(i);
				
				// to write holding and stake into diff files
				ArrayList<String> holdingDataLine = new ArrayList<String>(dataTemplate);
				ArrayList<String> stakeDataLine = new ArrayList<String>(dataTemplate);
				String stakeInCCASS = "";
				String stakeNotInCCASS = "";
				
				BufferedReader stockReader = utils.Utils.readFile_returnBufferedReader(utils.Utils.OUTPUT_ROOT_PATH_HKEX + "\\" + date + "\\" + stockCode + ".csv");
				int counter2 = 0;
				String stockDataLine = "";
				while((stockDataLine = stockReader.readLine()) != null) {
					String[] lineArr = stockDataLine.split(",");
					
					if(counter2 == 0) {  // first line
						stakeInCCASS = lineArr[7];
						stakeNotInCCASS = lineArr[9];
						
						counter2++;
						continue;
					}
					counter2++;
					
					
					String ccassID = lineArr[0];
					String brokerName = lineArr[1];
					String holding = lineArr[2];
					String stake = lineArr[4];
					
					int ind = brokerIDArr.indexOf(ccassID);
					if(ind != -1) {
						holdingDataLine.set(ind, holding);
						stakeDataLine.set(ind, stake);
						
						//System.out.println("id = " + ccassID + " stake = " + stake + " ind = " + ind);
					}
					
				}
				
				// write holding data and stake data into file
				fw1.write(stockCode);
				fw2.write(stockCode);
				if(today_all_CCASS_write_CCASS_summary) {
					fw1.write("," + stakeInCCASS + "," + stakeNotInCCASS);
					fw2.write("," + stakeInCCASS + "," + stakeNotInCCASS);
				}
				for(int j = 0; j < holdingDataLine.size(); j++) {
					fw1.write("," + holdingDataLine.get(j));
					fw2.write("," + stakeDataLine.get(j));
				}
				fw1.write("\n");
				fw2.write("\n");
				
			}// end of for...
			
			fw1.close();
			fw2.close();
		}catch(Exception e) {
			e.printStackTrace();
			isOK = false;
		}
		
		return isOK;
		
	}
	
	/**
	 * Write price data into a csv (all with formulas)
	 * @param stockArr
	 * @param date
	 * @return
	 */
	public static boolean writePriceFile(ArrayList<String> stockArr, String date){
		boolean isOK = true;
		try {
			int numOfHistDays = 20;
			
			// get trading date
			if(!setAllTradingDate()) {
				System.out.println("-- write price file: getting all trading date failed");
				return false;
			}
			
			// write file path
			String str_output_path1 = utils.Utils.OUTPUT_ROOT_PATH_HKEX + "\\" + date + "\\" + hist_stock_info_name + ".csv";
			FileWriter fw = new FileWriter(str_output_path1);
			
			//header (2 lines)
			fw.write("Trading date =");
			int ind = allTradingDate.indexOf(utils.Utils.dateStr2Cal(date, "yyyy-MM-dd"));
			if(ind == -1) {
				System.out.println("-- writing price file: no such date in allTradingDate. date = " + date);
				return false;
			}
			for(int i= 0; i <= numOfHistDays; i++) {  // write first header
				String tradingDate = utils.Utils.date2Str(allTradingDate.get(ind - i), "yyyy-MM-dd");
				fw.write("," + tradingDate);
			}
			fw.write("\n");
			for(int i= 0; i <= numOfHistDays; i++) {  // write second header
				if(i == 0)
					fw.write(",T Close,T High,T Low,T Vol");
				else
					fw.write(",T-" + i + " Close,T-" + i + " High,T-" + i + " Low,T-" + i + " Vol");
			}
			fw.write("\n");
			
			// write price formula
			for(int i = 0; i < stockArr.size(); i++) {
				String stockCode = stockArr.get(i);
				fw.write(stockCode);
				
				String rowNum = String.valueOf(i + 3);
				
				for(int j = 0; j <= numOfHistDays; j++) {
					String currentDateCell = "$" + String.valueOf((char) (65 + 1 + j)) + "$1";
					
					int baseColNum = 65 + 4*j;
					
					String colClose = String.valueOf((char) (baseColNum + 1));
					String colHigh = String.valueOf((char) (baseColNum + 2));
					String colLow = String.valueOf((char) (baseColNum + 3));
					String colVol = String.valueOf((char) (baseColNum + 4));
					
					String closeFormula = ",\"=BDH(A" + rowNum + "&\"\" HK Equity\"\",\"\"PX_LAST\"\"," + currentDateCell + ")\"";
					String highFormula = ",\"=BDH(A" + rowNum + "&\"\" HK Equity\"\",\"\"PX_HIGH\"\"," + currentDateCell + ")\"";
					String lowFormula = ",\"=BDH(A" + rowNum + "&\"\" HK Equity\"\",\"\"PX_LOW\"\"," + currentDateCell + ")\"";
					String volFormula = ",\"=BDH(A" + rowNum + "&\"\" HK Equity\"\",\"\"VOLUME\"\"," + currentDateCell + ")\"";
					
					fw.write(closeFormula);
					fw.write(highFormula);
					fw.write(lowFormula);
					fw.write(volFormula);
				}
				fw.write("\n");
			}
			
			fw.close();
						
		}catch(Exception e) {
			isOK = false;
			e.printStackTrace();
		}
		return isOK;
	}

	/**
	 * Write the historical CCASS data into one file
	 * @param stockArr
	 * @param date
	 * @return
	 */
	public static boolean writeHistAllCCASSData(ArrayList<String> stockArr, String date){
		boolean isOK = true;
		try {
			int numOfHistDays = 20;
			
			// get trading date
			if(!setAllTradingDate()) {
				System.out.println("-- write hist CCASS data: getting all trading date failed");
				return false;
			}
			
			// write file path
			String str_output_path1 = utils.Utils.OUTPUT_ROOT_PATH_HKEX + "\\" + date + "\\" + hist_all_CCASS_stake_name + ".csv";
			FileWriter fw = new FileWriter(str_output_path1);
			
			// header1
			fw.write("CCASS date =," + date + "\n");
			
			ArrayList<String> thisTradingDate = new ArrayList<String>();
			// header2
			int ind = allTradingDate.indexOf(utils.Utils.dateStr2Cal(date, "yyyy-MM-dd"));
			if(ind == -1) {
				System.out.println("-- write hist CCASS data: no such date in allTradingDate");
				return false;
			}
			for(int i= 0; i <= numOfHistDays; i++) {  // write 2nd header
				String tradingDate = utils.Utils.date2Str(allTradingDate.get(ind - i), "yyyy-MM-dd");
				fw.write("," + tradingDate);
				thisTradingDate.add(tradingDate);
			}
			fw.write("\n");
			
			// write hist CCASS data
			// the hist data should be a matrix, like below
			/*			date1	date2	date3	...
			 * stock1	*
			 * stock2	
			 * ...
			 */
			ArrayList<ArrayList<String>> histCCASSData = new ArrayList<ArrayList<String>>(); // first index is row number, 2nd index is col num
			
			// ================ intializing histCCASSData ================
			for(int i = 0; i < stockArr.size(); i++) {  
				histCCASSData.add(new ArrayList<String>());
				for(int j = 0; j < thisTradingDate.size(); j++) {
					histCCASSData.get(i).add("NA");
				}
			}
			
			// ================ constructing histCCASSData ================ 
			for(int j = 0; j < thisTradingDate.size(); j++) {
				String tradingDate = thisTradingDate.get(j);
				String readPath0 = utils.Utils.OUTPUT_ROOT_PATH_HKEX + "\\" + tradingDate + "\\";
				
				for(int i = 0; i < stockArr.size(); i++) {
					String stockCode = stockArr.get(i);
					
					String readPath = readPath0 + stockCode + ".csv";
					
					BufferedReader bf = null;
					try {
						bf = utils.Utils.readFile_returnBufferedReader(readPath);
						String line = bf.readLine();
						String[] lineArr = line.split(",");
						String CCASS = lineArr[7];
						
						histCCASSData.get(i).set(j, CCASS);
					}catch(Exception e) {
						System.out.println("-- write hist CCASS data: read stock file failed. date = " + tradingDate + " stock code = " + stockCode);
					}
				}
			}
			
			// ================ write data ================
			for(int i = 0; i < stockArr.size(); i++) {  
				fw.write(stockArr.get(i));
				for(int j = 0; j < thisTradingDate.size(); j++) {
					fw.write("," + histCCASSData.get(i).get(j));
				}
				fw.write("\n");
			}
			
			fw.close();
			
		}catch(Exception e) {
			isOK = false;
			e.printStackTrace();
		}
		return isOK;
	}
	
	/**
	 * to write today's CCASS data for all BL brokers
	 * @param stockArr
	 * @param date
	 * @param blList
	 * @return
	 */
	public static boolean writeTodayBLCCASSData(ArrayList<String> stockArr, String date){
		boolean isOK = true;
		try {
			// make a copy
			String temp1 = today_all_CCASS_value_name;
			String temp2 = today_all_CCASS_stake_name;
			String temp3 = all_broker_name_list_path;
			
			// modify these parameters
			today_all_CCASS_value_name = today_BL_CCASS_value_name;
			today_all_CCASS_stake_name = today_BL_CCASS_stake_name;
			all_broker_name_list_path = blacklist_broker_name_list_path;
			today_all_CCASS_write_CCASS_summary = false;
			
			csvDataConsolidation(stockArr, date);
			
			today_all_CCASS_value_name =temp1;
			today_all_CCASS_stake_name = temp2;
			all_broker_name_list_path = temp3;
			today_all_CCASS_write_CCASS_summary = true;
			
		}catch(Exception e) {
			isOK = false;
			e.printStackTrace();
		}
		return isOK;
	}
	
	/**
	 * write the sum of CCASS num for all BL brokers as a group
	 * @param stockArr
	 * @param date
	 * @return
	 */
	public static boolean writeHistBLCCASSData(ArrayList<String> stockArr, String date){
		boolean isOK = true;
		try {//TO DO
			int numOfHistDays = 20;
			// ========= get trading date ========= 
			if(!setAllTradingDate()) {
				System.out.println("-- write hist BL CCASS data: getting all trading date failed");
				return false;
			}
			
			// ========= write file path ========= 
			String str_output_path1 = utils.Utils.OUTPUT_ROOT_PATH_HKEX + "\\" + date + "\\" + hist_BL_CCASS_stake_name + ".csv";
			FileWriter fw = new FileWriter(str_output_path1);
			
			// ========= header1 ========= 
			fw.write("CCASS date =," + date + "\n");
			
			ArrayList<String> thisTradingDate = new ArrayList<String>();
			// ========= header2 ========= (write hist ccass date)
			int ind = allTradingDate.indexOf(utils.Utils.dateStr2Cal(date, "yyyy-MM-dd"));
			if(ind == -1) {
				System.out.println("-- write hist BL CCASS data: no such date in allTradingDate");
				return false;
			}
			for(int i= 0; i <= numOfHistDays; i++) {  // write 2nd header
				String tradingDate = utils.Utils.date2Str(allTradingDate.get(ind - i), "yyyy-MM-dd");
				fw.write("," + tradingDate);
				thisTradingDate.add(tradingDate);
			}
			fw.write("\n");
			
			// =========== write hist BL CCASS data ===========
			// the hist data should be a matrix, like below
			/*			date1	date2	date3	...
			 * stock1	*
			 * stock2	
			 * ...
			 */
			ArrayList<ArrayList<String>> histBLCCASSData = new ArrayList<ArrayList<String>>(); // first index is row number, 2nd index is col num
			
			// ================ intializing histBLCCASSData ================
			for(int i = 0; i < stockArr.size(); i++) {  
				histBLCCASSData.add(new ArrayList<String>());
				for(int j = 0; j < thisTradingDate.size(); j++) {
					histBLCCASSData.get(i).add("NA");
				}
			}
			
			// ================ constructing histBLCCASSData ================ 
			for(int j = 0; j < thisTradingDate.size(); j++) {
				String tradingDate = thisTradingDate.get(j);
				String readPath = utils.Utils.OUTPUT_ROOT_PATH_HKEX + "\\" + tradingDate + "\\" + today_BL_CCASS_stake_name + ".csv";
				
				BufferedReader bf = utils.Utils.readFile_returnBufferedReader(readPath);
				String line;
				int i = -2;
				while((line = bf.readLine()) != null) {
					if(i < 0) { // first line
						i ++ ;
						continue;
					}
					
					String[] lineArr = line.split(",");
					double blStakeSum = 0.0;
					for(int k = 1; k < lineArr.length; k++) {
						String stake = lineArr[k];
						if(utils.Utils.isDouble(stake)) {
							//System.out.println("-- write hist BL CCASS data: stake not a number");
							blStakeSum = blStakeSum + Double.parseDouble(stake);
						}
					}
					
					histBLCCASSData.get(i).set(j, String.valueOf(blStakeSum));
					
					i++;
				} // end of while
			}
			
			// ================ write data ================
			for(int i = 0; i < stockArr.size(); i++) {  
				fw.write(stockArr.get(i));
				//System.out.print("stock code = " + stockArr.get(i) + " ");
				for(int j = 0; j < thisTradingDate.size(); j++) {
					fw.write("," + histBLCCASSData.get(i).get(j));
					//System.out.print("," + histBLCCASSData.get(i).get(j));
				}
				fw.write("\n");
				//System.out.println();
			}
			
			fw.close();
		}catch(Exception e) {
			isOK = false;
			e.printStackTrace();
		}
		return isOK;
	}
}
