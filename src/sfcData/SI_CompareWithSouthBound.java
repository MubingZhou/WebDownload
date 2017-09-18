package sfcData;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Map;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import test_no_use.Test;

public class SI_CompareWithSouthBound {
	private static Logger logger = LogManager.getLogger(SI_CompareWithSouthBound.class.getName());
	
	public static boolean generateReport(ArrayList<String> stockArr, String date, String dateFormat, String outputPath){
		logger.info("Start generateReport....");
		
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
			trdDate = utils.Utils.date2Str(allTrdDate, "dd/MM/yyyy");
			
			//System.out.println("last wk = " + T_5);
			
			// ====== handle outputPath & writer header ==========
			if(!outputPath.substring(outputPath.length() - 4).equals(".csv")){
				if(!outputPath.substring(outputPath.length() - 2).equals("\\")){
					outputPath = outputPath + "\\";
				}
				outputPath= outputPath + date + ".csv";
			}
			FileWriter fw = new FileWriter(outputPath);
			
			// ========= get sector & freefloat ===========
			Map<String, String> sectorMap = utils.Utils.getStockSectors("D:\\stock data\\stock sector - karen.csv");
			Map<String, String> ffPctMap = utils.Utils.getFreeFloatPct("D:\\stock data\\freefloat pct - hk.csv");
			
			// ========= handle columns ===========
			ArrayList<Calendar> SI_FileDateList = new ArrayList<Calendar>();
			ArrayList<Calendar> SI_FileDateList_All = new ArrayList<Calendar>();
			
			File SI_File = new File(ShortPosition_DataGetter.SI_DATAPATH);
			File[] SI_FileList = SI_File.listFiles();
			
			Calendar calBenchmark = Calendar.getInstance();
			calBenchmark.setTime(new SimpleDateFormat("yyyyMMdd").parse("20170101"));
			for(int i = 0; i < SI_FileList.length; i++) {
				File thisFile = SI_FileList[i];
				String thisFileName = thisFile.getName();
				//System.out.println(thisFileName);
				if(thisFileName.length() > 4 && thisFileName.substring(thisFileName.length()-4).equalsIgnoreCase(".csv")) { // get a csv file
					try {
						Calendar cal = Calendar.getInstance();
						cal.setTime(new SimpleDateFormat("yyyyMMdd").parse(thisFileName));
						
						Calendar thisDate = Calendar.getInstance();
						thisDate .setTime(new SimpleDateFormat("yyyyMMdd").parse(date));
						thisDate.set(Calendar.DATE, 1);
						if(cal.after(calBenchmark) /*&& cal.before(thisDate)*/) {  // pick up the dates after benchmark date
							SI_FileDateList.add(cal);
							//System.out.println(new SimpleDateFormat("yyyyMMdd").format(cal.getTime()));
						}
						SI_FileDateList_All.add(cal);
					}catch(Exception e) {
						
					}
				}
			}
			//Thread.sleep(1000 * 1000000);
			Collections.sort(SI_FileDateList); // ascending order, i.e. 2017 Jan 1 will be in front of 2017 Jan 2
			Collections.sort(SI_FileDateList_All); // ascending order
			ArrayList<String> colDateStr = utils.Utils.date2Str(SI_FileDateList, "yyyyMMdd");
			
			// ========= calculate date =======
			String _now = new SimpleDateFormat("yyyyMMdd").format(SI_FileDateList.get(SI_FileDateList.size() - 1).getTime());
			String _1WkAgo = "";
			String _2WkAgo = "";
			String _1MnAgo = ""; // 从最后一天往前推1个月的日子
			String _3MnAgo = "";
			Calendar _1WkAgo_benchmark = (Calendar) SI_FileDateList.get(SI_FileDateList.size() - 2) .clone();  // 1周之前的日期很容易拿到
			Calendar _2WkAgo_benchmark = (Calendar) SI_FileDateList.get(SI_FileDateList.size() - 3) .clone();  // 2周之前的日期很容易拿到
			Calendar _1MnAgo_benchmark = (Calendar) SI_FileDateList.get(SI_FileDateList.size() - 1) .clone();
			Calendar _3MnAgo_benchmark = (Calendar) _1MnAgo_benchmark.clone();
			_1MnAgo_benchmark.add(Calendar.MONTH, -1); // 标准一个月之前的日期
			_3MnAgo_benchmark.add(Calendar.MONTH, -3);
			long _1Mn_diff = Math.abs(SI_FileDateList.get(0).getTimeInMillis() - _1MnAgo_benchmark.getTimeInMillis());
			int _1Mn_ind = 0;
			long _3Mn_diff = Math.abs(SI_FileDateList.get(0).getTimeInMillis() - _3MnAgo_benchmark.getTimeInMillis());
			int _3Mn_ind = 0;
			for(int i = 1; i < SI_FileDateList.size(); i++) { // 找到SI_FileDateList中和标准的一个月之前的日期最近的日期
				Calendar thisCal = SI_FileDateList.get(i);
				
				long this_1Mn_diff = Math.abs(thisCal.getTimeInMillis() - _1MnAgo_benchmark.getTimeInMillis());
				long this_3Mn_diff = Math.abs(thisCal.getTimeInMillis() - _3MnAgo_benchmark.getTimeInMillis());
				
				logger.debug("last diff = "+ String.valueOf(_1Mn_diff) + " this diff = " + String.valueOf(this_1Mn_diff));
				if(this_1Mn_diff < _1Mn_diff) {
					_1Mn_diff = this_1Mn_diff;
					_1Mn_ind = i;
					//logger.debug("here");
				}
				
				if(this_3Mn_diff < _3Mn_diff) {
					_3Mn_diff = this_3Mn_diff;
					_3Mn_ind = i;
				}
			}
			_1WkAgo = new SimpleDateFormat("yyyyMMdd").format(_1WkAgo_benchmark.getTime());
			_2WkAgo = new SimpleDateFormat("yyyyMMdd").format(_2WkAgo_benchmark.getTime());
			_1MnAgo = new SimpleDateFormat("yyyyMMdd").format(SI_FileDateList.get(_1Mn_ind).getTime());
			_3MnAgo = new SimpleDateFormat("yyyyMMdd").format(SI_FileDateList.get(_3Mn_ind).getTime());
			
			//logger.debug("1wk ago = " + _1WkAgo);
			//logger.debug("2wk ago = " + _2WkAgo);
			logger.debug("1mn ago = " + _1MnAgo 
					+ " benchmark = " + new SimpleDateFormat("yyyyMMdd").format(_1MnAgo_benchmark.getTime())
					+ " mili-seconds = " + String.valueOf(_1MnAgo_benchmark.getTimeInMillis()));
			logger.debug("3mn ago = " + _3MnAgo);
			
			//Thread.sleep(1000 * 1000000);
			
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
				
				if(i == (colDateStr.size() - 1))
					header_SI_5DVol = header_SI_5DVol +",SI/5D avg vol-" + thisDateStr;
			}
			fw.write("Stock,Sector,SI " + _now + ",SI/FF " + _now + ",SB " + _now + ",SB/FF " + _now + ","
					+ "price weekly change,price biweekly change,price monthly change,price 3-month change,"
					+ "SI/FF weekly change,SI/FF biweekly change,SI/FF monthly change,SI/FF 3-month change,"
					+ "SB/FF weekly change,SB/FF biweekly change,SB/FF monthly change,SB/FF 3-month change" 
					+ /*header_SI + */header_SI_Stake /*+ header_SI_WeeklyChg*/
					+ /*header_SB + */header_SB_Stake /*+ header_SB_WeeklyChg*/
					+ "\n"); // columns
			
			//======= write each row =========
			int rowNum = 2;
			for(int i = 0; i < stockArr.size(); i++){
				// ======== columns ==========
				String stock;
				String sector;
				//String freeFloatShares;
				
				// recent changes
				String priceWklyChg = "";
				String priceMnlyChg = "";
				String price3MnlyChg = "";
				String priceBiwklyChg = "";

				String siNow = "";  // absolute volume, not percentage
				String siStakeNow = "";
				String siStake1WkAgo = "";  // SI stake 1 week before (from now), same below
				String siStake2WkAgo = "";
				String siStake1MnAgo = ""; 
				String siStake3MnAgo = "";
				
				String siStakeWklyChg = "";
				String siStakeBiwklyChg  = "";
				String siStakeMnlyChg = "";
				String siStake3MnlyChg = "";

				String southboundNow = "";  //  absolute volume, not percentage
				String southboundStakeNow = "";
				String southboundStake1WkAgo = "";
				String southboundStake2WkAgo = "";
				String southboundStake1MnAgo = "";
				String southboundStake3MnAgo = "";
				
				String southboundStakeWklyChg = "";
				String southboundStakeBiwklyChg = "";
				String southboundStakeMnlyChg = "";
				String southboundStake3MnlyChg = "";
				
				String SI_SB_up = "";
				
				// short interest details
				String SI="";
				String siStake="";
				//String siChange="";
				
				// southbound details
				String southboundHolding="";
				String southboundStake="";
				//String southboundChange="";
				
				// no use anymore
				//String SI_5DAvgVolume_Full;
				//String priceChange;
				
				stock = stockArr.get(i);
				
				logger.info("========= stock = " + stock + " =========");
				
				// ====== sector ======
				sector = sectorMap.get(stock);
				if(sector == null || sector.length() == 0) {
					sector = utils.Utils.BBG_BDP_Formula(stock + " HK Equity", "INDUSTRY_GROUP");
					sector  = "\"" + sector + "\"";
				}
				
				// ====== free float ========
				String ffPct = ffPctMap.get(stock);
				/*
				String osShare = webbDownload.outstanding.DataGetter.getStockDataField(stock, 
						webbDownload.outstanding.DataGetter.OutstandingDataField.OUTSTANDING_SHARES, colDateStr.get(colDateStr.size()-1), "yyyyMMdd");
				String freeFloatShareCol = "";
				if(!utils.Utils.isDouble(osShare) || !utils.Utils.isDouble(ffPct)) {
					freeFloatShares = "\"=IFERROR(" + utils.Utils.BBG_BDH_Formula(stock + " HK Equity", "EQY_SH_OUT", date, date) + "*1000000"
							+ "*" + utils.Utils.BBG_BDH_Formula(stock + " HK Equity", "EQY_FREE_FLOAT_PCT", date, date) + "/100" + ",\"\"\"\")\"";  
					//e.g. "=BDH(""1 HK Equity"",""EQ_SH_OUT"", ""20170818"",""20170818"")*1000000*BDH(""1 HK Equity"",""EQY_FREE_FLOAT_PCT"", ""20170818"",""20170818"")/100"
					freeFloatShareCol = "C";
				}else {
					freeFloatShares = String.valueOf(Double.parseDouble(osShare) * Double.parseDouble(ffPct) / 100.0);
				}
				*/
				
				for(int j = 0; j < colDateStr.size(); j++) {
					String thisSI="";
					String thisSIStake  = "";
					String thisSB = "";
					String thisSBStake  ="";
					
					String thisDateStr = colDateStr.get(j);
					logger.debug("thisDateStr = " + thisDateStr + " _3MnAgo = " + _3MnAgo);
					
					/*SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
					Calendar thisDateCal = Calendar.getInstance();
					thisDateCal.setTime(sdf.parse(thisDateStr));
					Calendar lastWk = Calendar.getInstance();
					lastWk = SI_FileDateList_All.get(SI_FileDateList_All.indexOf(thisDateCal) - 1);
					//lastWk.setTime(thisDateCal.getTime());
					//lastWk.set(Calendar.WEEK_OF_MONTH, -1);
					String lastWkStr = sdf.format(lastWk.getTime());*/
					
					// ====== calculate the freefloat today =====
					String thisDateOsShares = webbDownload.outstanding.DataGetter.getStockDataField(stock,
							webbDownload.outstanding.DataGetter.OutstandingDataField.OUTSTANDING_SHARES, thisDateStr, "yyyyMMdd");
					Double thisDateFfShares = 0.0;
					if(utils.Utils.isDouble(thisDateOsShares) && utils.Utils.isDouble(ffPct)) {
						thisDateFfShares = Double.parseDouble(thisDateOsShares) * Double.parseDouble(ffPct) / 100;
					}
					
					// ======= short interest =======
					ArrayList<String> shortInterestLine = ShortPosition_DataGetter.getStockData(stock, thisDateStr, "yyyyMMdd");
					if(shortInterestLine == null || shortInterestLine.size() == 0) {
						SI = SI + ","; //if no short interest data, then skip this date
						siStake = siStake + ",";
						//siChange = siChange + ",";
					}else {
						thisSI = shortInterestLine.get(3);
						
						SI = SI + "," + thisSI ;
						if(!thisDateFfShares.equals(0.0)) {
							thisSIStake = String.valueOf(Double.parseDouble(thisSI) / thisDateFfShares);
							siStake = siStake + "," + thisSIStake;
						}
					}
					// ======= southbound =========
					ArrayList<String> southboundLine = webbDownload.southboundData.DataGetter.getStockData(stock, thisDateStr, "yyyyMMdd");
					if(southboundLine == null || southboundLine.size() == 0) {
						southboundHolding = southboundHolding + ","; //if no south bound data, then skip this date
						southboundStake = southboundStake + ",";
						//southboundChange = southboundChange + ",";
					}else {
						thisSB = southboundLine.get(2);
						
						southboundHolding = southboundHolding + "," + thisSB;
						if(!thisDateFfShares.equals(0.0)) {
							thisSBStake = String.valueOf(Double.parseDouble(thisSB) / thisDateFfShares);
							southboundStake = southboundStake+ "," + thisSBStake;
						}
					}
					
					// ======= find the data for 1wk, 1mn and 3mn ago =======
					if(thisDateStr.equals(_1WkAgo)) {
						logger.debug("here _1WkAgo");
						siStake1WkAgo = thisSIStake;
						southboundStake1WkAgo = thisSBStake;
					}
					if(thisDateStr.equals(_2WkAgo)) {
						logger.debug("here _2WkAgo");
						siStake2WkAgo = thisSIStake;
						southboundStake2WkAgo = thisSBStake;
					}
					if(thisDateStr.equals(_1MnAgo)) {
						logger.debug("here _1MnAgo");
						siStake1MnAgo = thisSIStake;
						southboundStake1MnAgo = thisSBStake;
					}
					if(thisDateStr.equals(_3MnAgo)) {
						logger.debug("here _3MnAgo");
						siStake3MnAgo = thisSIStake;
						southboundStake3MnAgo = thisSBStake;
					}
					if( j == (colDateStr.size() - 1)) {
						logger.debug("here Now");
						siStakeNow = thisSIStake;
						siNow = thisSI;
						southboundStakeNow = thisSBStake;
						southboundNow = thisSB;
					}
					
					
				} // end of for(int j = 0...
				
				//Thread.sleep(1000 * 100000);
				// =========== calculate price, SB, SI change ===========
				if(utils.Utils.isDouble(siStake1WkAgo) && utils.Utils.isDouble(siStakeNow)) {
					siStakeWklyChg = String.valueOf(Double.parseDouble(siStakeNow) - Double.parseDouble(siStake1WkAgo));
				}
				if(utils.Utils.isDouble(siStake2WkAgo) && utils.Utils.isDouble(siStakeNow)) {
					siStakeBiwklyChg = String.valueOf(Double.parseDouble(siStakeNow) - Double.parseDouble(siStake2WkAgo));
				}
				if(utils.Utils.isDouble(siStake1MnAgo) && utils.Utils.isDouble(siStakeNow)) {
					siStakeMnlyChg = String.valueOf(Double.parseDouble(siStakeNow) - Double.parseDouble(siStake1MnAgo));
				}
				if(utils.Utils.isDouble(siStake3MnAgo) && utils.Utils.isDouble(siStakeNow)) {
					siStake3MnlyChg = String.valueOf(Double.parseDouble(siStakeNow) - Double.parseDouble(siStake3MnAgo));
				}
				
				if(utils.Utils.isDouble(southboundStake1WkAgo) && utils.Utils.isDouble(southboundStakeNow)) {
					southboundStakeWklyChg = String.valueOf(Double.parseDouble(southboundStakeNow) - Double.parseDouble(southboundStake1WkAgo));
				}
				if(utils.Utils.isDouble(southboundStake2WkAgo) && utils.Utils.isDouble(southboundStakeNow)) {
					southboundStakeBiwklyChg = String.valueOf(Double.parseDouble(southboundStakeNow) - Double.parseDouble(southboundStake2WkAgo));
				}
				if(utils.Utils.isDouble(southboundStake1MnAgo) && utils.Utils.isDouble(southboundStakeNow)) {
					southboundStakeMnlyChg = String.valueOf(Double.parseDouble(southboundStakeNow) - Double.parseDouble(southboundStake1MnAgo));
				}
				if(utils.Utils.isDouble(southboundStake3MnAgo) && utils.Utils.isDouble(southboundStakeNow)) {
					southboundStake3MnlyChg = String.valueOf(Double.parseDouble(southboundStakeNow) - Double.parseDouble(southboundStake3MnAgo));
				}
				
				String priceNow = utils.Utils.BBG_BDH_Formula(stock+" HK Equity", "PX_LAST", _now, _now);
				String price1WkAgo = utils.Utils.BBG_BDH_Formula(stock+" HK Equity", "PX_LAST", _1WkAgo, _1WkAgo);
				String price2WkAgo = utils.Utils.BBG_BDH_Formula(stock+" HK Equity", "PX_LAST", _2WkAgo, _2WkAgo);
				String price1MnAgo = utils.Utils.BBG_BDH_Formula(stock+" HK Equity", "PX_LAST", _1MnAgo, _1MnAgo);
				String price3MnAgo = utils.Utils.BBG_BDH_Formula(stock+" HK Equity", "PX_LAST", _3MnAgo, _3MnAgo);
				
				priceWklyChg = "\"=(" + priceNow + "-" + price1WkAgo + ")/" + price1WkAgo + "\"";
				priceBiwklyChg = "\"=(" + priceNow + "-" + price2WkAgo + ")/" + price2WkAgo + "\"";
				priceMnlyChg = "\"=(" + priceNow + "-" + price1MnAgo + ")/" + price1MnAgo + "\"";
				price3MnlyChg = "\"=(" + priceNow + "-" + price3MnAgo + ")/" + price3MnAgo + "\"";
				
				// ======= short interest �� & south bound �� ===========
				//SI_SB_up = "\"=IFERROR(IF(AND(F" + rowNum + ">0,H" + rowNum + ">0),1,\"\"\"\"),\"\"\"\")\"";  // if col for SI & SB change, there will also be changes here
				
				/*
				// ======= short interest over trading volume =========
			//	int thisDateCalInd = allTrdDate.indexOf(thisDateCal);
				String T_5 = "";
				String T_1 = "";
				String T_2 = "";
				String T_3 = "";
				String T_4 = "";
				for(int k = 5; k < trdDate.size(); k++){					
					if(utils.Utils.formatDate(trdDate.get(k), "dd/MM/yyyy", "yyyyMMdd").equals(date)){
						///System.out.println("trdDate.get(i - 5) = " + trdDate.get(i - 5));
						T_1 = utils.Utils.formatDate(trdDate.get(k - 1), "dd/MM/yyyy","yyyyMMdd");
						T_2 = utils.Utils.formatDate(trdDate.get(k - 2), "dd/MM/yyyy","yyyyMMdd");
						T_3 = utils.Utils.formatDate(trdDate.get(k - 3), "dd/MM/yyyy","yyyyMMdd");
						T_4 = utils.Utils.formatDate(trdDate.get(k - 4), "dd/MM/yyyy","yyyyMMdd");
						T_5 = utils.Utils.formatDate(trdDate.get(k - 5), "dd/MM/yyyy","yyyyMMdd");
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
				*/
				
				// ====== write ======
				fw.write(stock + "," + sector + "," 
						+ String.valueOf(siNow) + "," + String.valueOf(siStakeNow) + ","
						+ String.valueOf(southboundNow) + "," + String.valueOf(southboundStakeNow) + ","
						+ String.valueOf(priceWklyChg) + "," + String.valueOf(priceBiwklyChg) + "," + String.valueOf(priceMnlyChg) + "," + String.valueOf(price3MnlyChg) + ","
						+ String.valueOf(siStakeWklyChg) + "," + String.valueOf(siStakeBiwklyChg) + "," + String.valueOf(siStakeMnlyChg) + "," + String.valueOf(siStake3MnlyChg) + ","
						+ String.valueOf(southboundStakeWklyChg) + "," + String.valueOf(southboundStakeBiwklyChg) + "," + String.valueOf(southboundStakeMnlyChg) + "," + String.valueOf(southboundStake3MnlyChg)
						+ /*SI  + */siStake /* + siChange */
						+ /*southboundHolding + */southboundStake /*+ southboundChange */
						+ "\n");
				
				rowNum++;
			}
			
			fw.close();
			//System.out.println("is here?");
		}catch(Exception e){
			e.printStackTrace();
			isOK = false;
		}
		
		logger.info("End generateReport...");
		return isOK;
	}
}
