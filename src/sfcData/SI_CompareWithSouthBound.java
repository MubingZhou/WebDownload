package sfcData;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;

public class SI_CompareWithSouthBound {
	public static boolean generateReport(ArrayList<String> stockArr, String date, String dateFormat, String outputPath){
		boolean isOK = true;
		
		try{
			date = utils.Utils.formatDate(date, dateFormat, "yyyyMMdd");
			//System.out.println("date = " + date);
			// ====== handle trading dates =======
			BufferedReader bf = utils.Utils.readFile_returnBufferedReader("D:\\stock data\\all trading date - hk.csv");
			String trdDateStr = bf.readLine();
			ArrayList<String> trdDate = new ArrayList<String>(Arrays.asList(trdDateStr.split(",")));
			ArrayList<Calendar> allTrdDate = utils.Utils.dateStr2Cal(trdDate, "dd/MM/yyyy");
			Collections.sort(allTrdDate); // ascending
			
			//System.out.println("last wk = " + T_5);
			
			// ====== handle outputPath & writer header ==========
			if(!outputPath.substring(outputPath.length() - 4).equals(".csv")){
				if(!outputPath.substring(outputPath.length() - 2).equals("\\")){
					outputPath = outputPath + "\\";
				}
				outputPath= outputPath + date + ".csv";
			}
			FileWriter fw = new FileWriter(outputPath);
			
			// ========= handle columns ===========
			ArrayList<Calendar> SI_FileDateList = new ArrayList<Calendar>();
			ArrayList<Calendar> SI_FileDateList_All = new ArrayList<Calendar>();
			
			File SI_File = new File(ShortPosition_DataGetter.SI_DATAPATH);
			File[] SI_FileList = SI_File.listFiles();
			for(int i = 0; i < SI_FileList.length; i++) {
				File thisFile = SI_FileList[i];
				String thisFileName = thisFile.getName();
				if(thisFileName.length() > 4 && thisFileName.substring(thisFileName.length()-4).equalsIgnoreCase(".csv")) { // get a csv file
					try {
						Calendar cal = Calendar.getInstance();
						cal.setTime(new SimpleDateFormat("yyyyMMdd").parse(thisFileName));
						Calendar calBenchmark = Calendar.getInstance();
						calBenchmark.setTime(new SimpleDateFormat("yyyyMMdd").parse("20170101"));
						
						Calendar thisDate = Calendar.getInstance();
						thisDate .setTime(new SimpleDateFormat("yyyyMMdd").parse(date));
						thisDate.set(Calendar.DATE, 1);
						if(cal.after(calBenchmark) && cal.before(thisDate))
							SI_FileDateList.add(cal);
						SI_FileDateList_All.add(cal);
					}catch(Exception e) {
						
					}
				}
			}
			Collections.sort(SI_FileDateList); // ascending order, i.e. 2017 Jan 1 will be in front of 2017 Jan 2
			Collections.sort(SI_FileDateList_All); // ascending order
			ArrayList<String> colDateStr = utils.Utils.date2Str(SI_FileDateList, "yyyyMMdd");
			
			
			// ========= write header ========
			String header_SI = "";
			String header_SI_Stake = "";
			String header_SI_WeeklyChg = "";
			String header_SB = "";
			String header_SB_Stake = "";
			String header_SB_WeeklyChg = "";
			String header_SI_5DVol = "";
			
			for(int i = 0; i < colDateStr.size(); i++) {
				String thisDateStr = colDateStr.get(i);
				header_SI = header_SI + ",SI-" + thisDateStr;
				header_SI_Stake  = header_SI_Stake  + ",SI/FF-" + thisDateStr;
				header_SI_WeeklyChg = header_SI_WeeklyChg + ",SI/FF Weekly Chg-" + thisDateStr;
				header_SB = header_SB + ",SB-" + thisDateStr;
				header_SB_Stake = header_SB_Stake + ",SB/FF-" + thisDateStr;
				header_SB_WeeklyChg = header_SB_WeeklyChg + ",SB/FF Weekly Chg-" + thisDateStr;
				header_SI_5DVol = header_SI_5DVol +"SI/5D avg vol-" + thisDateStr;
			}
			fw.write("Stock,Free Float (FF)" 
					//+ "," + date + "-Short Interest＃ & South Bound＃"
					+ /*header_SI + */header_SI_Stake /*+ header_SI_WeeklyChg*/
					+ /*header_SB + */header_SB_Stake /*+ header_SB_WeeklyChg*/
					+ header_SI_5DVol + ","
					+ "Price Weekly Change\n"); // columns
			
			//======= write each row =========
			int rowNum = 2;
			for(int i = 0; i < stockArr.size(); i++){
				// ======== columns ==========
				String stock;
				String freeFloatShares;
				
				String SI_SB_up = "";
				
				String SI="";
				String siStake="";
				String siChange="";
				
				String southboundHolding="";
				String southboundStake="";
				String southboundChange="";
				
				String SI_5DAvgVolume_Full;
				
				String priceChange;
				
				stock = stockArr.get(i);
				System.out.println("========= stock = " + stock + " =========");
				
				// ====== free float ========
				freeFloatShares = "\"=IFERROR(" + utils.Utils.BBG_BDH_Formula(stock + " HK Equity", "EQY_SH_OUT", date, date) + "*1000000"
							+ "*" + utils.Utils.BBG_BDH_Formula(stock + " HK Equity", "EQY_FREE_FLOAT_PCT", date, date) + "/100" + ",\"\"\"\")\"";  
							//e.g. "=BDH(""1 HK Equity"",""EQ_SH_OUT"", ""20170818"",""20170818"")*1000000*BDH(""1 HK Equity"",""EQY_FREE_FLOAT_PCT"", ""20170818"",""20170818"")/100"
				String freeFloatShareCol = "B";
				
				String thisSI="";
				for(int j = 0; j < colDateStr.size(); j++) {
					String thisDateStr = colDateStr.get(j);
					
					SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
					Calendar thisDateCal = Calendar.getInstance();
					thisDateCal.setTime(sdf.parse(thisDateStr));
					Calendar lastWk = Calendar.getInstance();
					lastWk = SI_FileDateList_All.get(SI_FileDateList_All.indexOf(thisDateCal) - 1);
					//lastWk.setTime(thisDateCal.getTime());
					//lastWk.set(Calendar.WEEK_OF_MONTH, -1);
					String lastWkStr = sdf.format(lastWk.getTime());
					
				// ======= short interest =======
					ArrayList<String> shortInterestLine = ShortPosition_DataGetter.getStockData(stock, thisDateStr, "yyyyMMdd");
					if(shortInterestLine == null || shortInterestLine.size() == 0) {
						SI = SI + ","; //if no short interest data, then skip this date
						siStake = siStake + ",";
						siChange = siChange + ",";
					}else {
						thisSI = shortInterestLine.get(3);
						
						SI = SI + "," + thisSI ;
						siStake = siStake + "," + "\"" + "=IFERROR(" + thisSI + "/" + freeFloatShareCol + rowNum + ",\"\"\"\")\""; // e.g. "=10000/B2"
						
						ArrayList<String> lastWkSILine = ShortPosition_DataGetter.getStockData(stock, lastWkStr, "yyyyMMdd");
						
						if(lastWkSILine == null || lastWkSILine.size() == 0) {
							siChange = siChange + ",";
						}else {
							String lastWkSI =lastWkSILine.get(3);
							siChange = siChange  + "," + "\"" + "=IFERROR((" + lastWkSI + "-" + thisSI + ")/" + freeFloatShareCol + rowNum + ",\"\"\"\")\""; // e.g. "=(9000-10000)/B2"
						}
					}
				// ======= southbound =========
					ArrayList<String> southboundLine = southboundData.DataGetter.getStockData(stock, thisDateStr, "yyyyMMdd");
					if(southboundLine == null || southboundLine.size() == 0) {
						southboundHolding = southboundHolding + ","; //if no south bound data, then skip this date
						southboundStake = southboundStake + ",";
						southboundChange = southboundChange + ",";
					}else {
						String thisSB = southboundLine.get(2);
						
						southboundHolding = southboundHolding + "," + thisSB;
						southboundStake = southboundStake+ "," + "\"" + "=IFERROR(" + thisSB + "/" + freeFloatShareCol + rowNum + ",\"\"\"\")\""; // e.g. "=10000/B2"
						
						ArrayList<String> lastWkSouthboundLine = southboundData.DataGetter.getStockData(stock, lastWkStr, "yyyyMMdd");
						if(lastWkSouthboundLine == null || lastWkSouthboundLine.size() == 0) {
							southboundChange = southboundChange  + ",";
						}else {
							String lastWkSouthboundHolding =lastWkSouthboundLine.get(2);
							southboundChange = southboundChange + "," + "\"=IFERROR((" + lastWkSouthboundHolding + "-" + thisSB + ")/" + freeFloatShareCol + rowNum + ",\"\"\"\")\""; // e.g. =(9000-10000)/B2	
						}
						
					}
				} // end of for(int j = 0...
				
				// ======= short interest ＃ & south bound ＃ ===========
				//SI_SB_up = "\"=IFERROR(IF(AND(F" + rowNum + ">0,H" + rowNum + ">0),1,\"\"\"\"),\"\"\"\")\"";  // if col for SI & SB change, there will also be changes here
				
				// ======= short interest over trading volume =========
				int thisDateCalInd = allTrdDate.indexOf(thisDateCal);
				String T_5 = "";
				String T_1 = "";
				String T_2 = "";
				String T_3 = "";
				String T_4 = "";
				for(int k = 5; i < trdDate.size(); i++){					
					if(utils.Utils.formatDate(trdDate.get(i), "dd/MM/yyyy", "yyyyMMdd").equals(date)){
						///System.out.println("trdDate.get(i - 5) = " + trdDate.get(i - 5));
						T_1 = utils.Utils.formatDate(trdDate.get(i - 1), "dd/MM/yyyy","yyyyMMdd");
						T_2 = utils.Utils.formatDate(trdDate.get(i - 2), "dd/MM/yyyy","yyyyMMdd");
						T_3 = utils.Utils.formatDate(trdDate.get(i - 3), "dd/MM/yyyy","yyyyMMdd");
						T_4 = utils.Utils.formatDate(trdDate.get(i - 4), "dd/MM/yyyy","yyyyMMdd");
						T_5 = utils.Utils.formatDate(trdDate.get(i - 5), "dd/MM/yyyy","yyyyMMdd");
					}
				}
				
				String _5DVolume = utils.Utils.BBG_BDH_Formula(stock + " HK Equity", "PX_VOLUME", date, date) + "+"
								+ utils.Utils.BBG_BDH_Formula(stock + " HK Equity", "PX_VOLUME", T_1, T_1) + "+"
								+ utils.Utils.BBG_BDH_Formula(stock + " HK Equity", "PX_VOLUME", T_2, T_2) + "+"
								+ utils.Utils.BBG_BDH_Formula(stock + " HK Equity", "PX_VOLUME", T_3, T_3) + "+"
								+ utils.Utils.BBG_BDH_Formula(stock + " HK Equity", "PX_VOLUME", T_4, T_4);
				String _5DAvgVolume = "(" + _5DVolume + ")/5";
				String SI_5DAvgVolume = thisSI + "/" + "(" + _5DAvgVolume + ")";
				SI_5DAvgVolume_Full = "\"=IFERROR(" + SI_5DAvgVolume + ",\"\"\"\")\""; 
				
				// ======= price ==========
				priceChange = "\"=(" 
						+ utils.Utils.BBG_BDH_Formula(stock + " HK Equity", "PX_LAST", date, date) + "-"
						+ utils.Utils.BBG_BDH_Formula(stock + " HK Equity", "PX_LAST", T_5, T_5)
						+ ")/" 
						+ utils.Utils.BBG_BDH_Formula(stock + " HK Equity", "PX_LAST", T_5, T_5)
						+ "\"";
				
				// ====== write ======
				fw.write(stock + "," + freeFloatShares 
						//+ "," + SI_SB_up 
						+ /*SI  + */siStake /* + siChange */
						+ /*southboundHolding + */southboundStake /*+ southboundChange */
						+ "," + SI_5DAvgVolume_Full
						+ "," + priceChange + "\n");
				
				rowNum++;
			}
			
			fw.close();
			
		}catch(Exception e){
			e.printStackTrace();
			isOK = false;
		}
		
		return isOK;
	}
}
