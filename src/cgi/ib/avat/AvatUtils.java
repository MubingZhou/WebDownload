package cgi.ib.avat;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.ib.client.Contract;
import com.ib.client.Types.BarSize;
import com.ib.client.Types.DurationUnit;
import com.ib.client.Types.WhatToShow;

public class AvatUtils {
	private static Logger logger = Logger.getLogger(AvatUtils.class.getName());
	public static String allTrdingDatePath = "D:\\stock data\\all trading date - hk.csv";
	
	public static String dateFormat = "yyyyMMdd HH:mm:ss";
	public static SimpleDateFormat sdf = new SimpleDateFormat (dateFormat); 
	public static String dateFormat2 = "yyyyMMdd";
	public static SimpleDateFormat sdf2 = new SimpleDateFormat (dateFormat2);
	public static String todayDate /*= sdf2.format(new Date())*/;
	public static String dateFormat3 = "HH:mm:ss";
	public static SimpleDateFormat sdf3 = new SimpleDateFormat (dateFormat3);
	
	public static String AVAT_ROOT_PATH = "Z:\\AVAT\\";
	private static String avatHistPath = ""; // 用来存储过去20天的avat平均值的文件夹
	
	private static LinkedHashMap<String, LinkedHashMap<Date, Double>> auctionData = new LinkedHashMap<String, LinkedHashMap<Date, Double>>();
	
	/**
	 * 获得conArr中每只股票昨天的历史avat数据。这些历史数据，每只股票的数据存在一个文件，每行是每个时刻该股票过去5天在这个时刻的avat的平均值以及过去20天的平均值
	 * Map<String, Map<Date,ArrayList<Double>>>
	 *      ↑股票                        ↑时间	    ↑这个ArrayList的第一个数据是avat 5D avg，第二个是20天平均
	 * @param conArr
	 * @return
	 */
	public static Map<String, Map<Date,ArrayList<Double>>> getPrevCrossSectionalAvat(ArrayList<Contract> conArr) {
		Map<String, Map<Date,ArrayList<Double>>> avatHist = new HashMap();
		//String avatHistJavaobjPath = AVAT_ROOT_PATH + "\\avat para\\" + todayDate + "\\avatHist.javaobj";
		try {
			//String avatHistPath = AVAT_ROOT_PATH + "avat para\\" + todayDate + "\\";
			
			for(int i = 0; i < conArr.size(); i++) {
				String stock = conArr.get(i).symbol();
				BufferedReader bf_avat = utils.Utils.readFile_returnBufferedReader(avatHistPath+ stock + ".csv");
				String line = "";
				
				Map<Date,ArrayList<Double>> thisStockData = new HashMap();
				while((line = bf_avat.readLine()) != null) {
					String[] lineArr = line.split(",");
					
					String dateS = lineArr[0];
					Date date = sdf.parse(dateS);
					
					Double _5d = Double.parseDouble(lineArr[1]);
					Double _10d = Double.parseDouble(lineArr[2]);
					ArrayList<Double> _avat_now = new ArrayList<Double> ();
					_avat_now.add(_5d);
					_avat_now.add(_10d);
					
					thisStockData.put(date, _avat_now);
				}
				bf_avat.close();
				
				avatHist.put(stock, thisStockData);
			}
			//utils.Utils.saveObject(avatHist, avatHistJavaobjPath);
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		return avatHist;
	}
	
	/**
	 * 获取昨日的收盘价，以Map的形式返回，其中key是股票代码
	 * @return
	 */
	public static Map<String, Double> getPrevClose(){
		Map<String, Double> avatPrevClose = new HashMap();
		try {
			String avatPrevClosePath = AVAT_ROOT_PATH + "avat para\\prev close.csv";
			BufferedReader bf_avat_p = utils.Utils.readFile_returnBufferedReader(avatPrevClosePath);
			String line = "";
			while((line = bf_avat_p.readLine()) != null) {
				String[] lineArr =line.split(",");
				avatPrevClose.put(lineArr[0], Double.parseDouble(lineArr[1]));
			}
			bf_avat_p.close();
		
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		return avatPrevClose;
	}
	
	/**
	 * 第一个元素是获取股票和对应的行业，以Map的形式返回，其中key是股票代码
	 * 第二个元素是获取相应行业对应的股票，以Map的形式返回，其中key是行业
	 * @return
	 */
	public static ArrayList<Object> getIndustry() {
		Map<String, String> avatIndustry = new HashMap();  // stock - industry
		Map<String, ArrayList<String>> avatIndustry_byIndustry = new HashMap();  // industry - stock list
		try {
			String avatIndustryPath = AVAT_ROOT_PATH + "avat para\\industry.csv";
			BufferedReader bf_avat_i = utils.Utils.readFile_returnBufferedReader(avatIndustryPath);
			String line = "";
			while((line = bf_avat_i.readLine()) != null) {
				String[] lineArr =line.split(",");
				String industry = lineArr[1];
				String stock = lineArr[0];
				avatIndustry.put(stock , industry);
				
				// update avatIndustry_byIndustry
				ArrayList<String> stockList_industry = avatIndustry_byIndustry.get(industry);
				if(stockList_industry == null)
					stockList_industry  = new ArrayList<String>();
				stockList_industry  .add(stock);
				
				avatIndustry_byIndustry.put(industry, stockList_industry);
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		ArrayList<Object> data = new ArrayList<Object>();
		data.add(avatIndustry);
		data.add(avatIndustry_byIndustry);
		return data;
	}
	
	/**
	 * 返回HSI和HSCEI成分股的所有股票代码
	 * @return
	 */
	public static ArrayList<String> getIndexMembers() {
		ArrayList<String> avatIndexMembers = new ArrayList<String>();
		try {
			BufferedReader bf_im = utils.Utils.readFile_returnBufferedReader(AVAT_ROOT_PATH + "avat para\\index members.csv");
			avatIndexMembers.addAll(Arrays.asList(bf_im.readLine().split(",")));
		}catch(Exception e) {
			e.printStackTrace();
		}
		return avatIndexMembers;
	}
	
	/**
	 * 取得每天以每分钟为step的固定时间点
	 * e.g. {"20170928 09:31:00", "20170928 09:32:00", ..., "20170928 12:00:00",...,"20170928 13:01:00",...,"20170928 16:10:00"}
	 * @return
	 */
	public static ArrayList<Date> getTimePath(){
		ArrayList<Date> avatTimePath = new ArrayList<Date>();
		try {
			//String todayDate = sdf.format(new Date());
			Long _1min = (long) (1000 * 60);
			// first one
			Date start1 = sdf.parse(todayDate + " 09:30:00"	); 
			//fw_tmp.write(todayDate + " 09:31:00\n" );
			avatTimePath.add(start1);
			
			Date break1 = sdf.parse(todayDate + " 11:59:59");
			Date start2 = sdf.parse(todayDate + " 12:59:59");
			Date break2 = sdf.parse(todayDate + " 15:59:01");
			
			Date nextTime = new Date(start1.getTime() + _1min);
			while(nextTime.before(break2)) {
				if(nextTime.before(break1) || nextTime.after(start2)) { // trading hours
					avatTimePath.add(nextTime);
				}
				nextTime = new Date(nextTime.getTime() + _1min);
				
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
		return avatTimePath;
	}
	
	public static Map<String, Double> getTodayAuction(){
		Map<String, Double> todayAuction = new HashMap();
		try {
			BufferedReader bf_ta = utils.Utils.readFile_returnBufferedReader(AVAT_ROOT_PATH + "avat para\\today auction.csv");
			String line = "";
			int count = 0;
			while((line = bf_ta.readLine()) != null) {
				if(count == 0) {
					count ++;
					continue;
				}
				
				String[] lineArr = line.split(",");
				todayAuction.put(lineArr[0], Double.parseDouble(lineArr[1]));
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		return todayAuction;
	}
	
	public static Map<String, Double> getLotSize(){
		Map<String, Double> lotSize = new HashMap();
		try {
			BufferedReader bf_lz = utils.Utils.readFile_returnBufferedReader(AVAT_ROOT_PATH + "avat para\\lot size.csv");
			String line = "";
			int count = 0;
			while((line = bf_lz.readLine()) != null) {
				
				String[] lineArr = line.split(",");
				lotSize.put(lineArr[0], Double.parseDouble(lineArr[1]));
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		return lotSize;
	}
	
	/**
	 * get previous day's volume
	 * @param conArr
	 * @return
	 */
	public static Map<String, Double> getPreviousVolume(ArrayList<Contract> conArr){
		Map<String, Double> prevV = new HashMap<String, Double>();
		try {
			//  看看之前是否已经完成了该项工作并进行了保存
			String prevVPath = AVAT_ROOT_PATH + "avat para\\" + todayDate + "\\prev volume.csv";
			ArrayList<String> alreadyExist = new ArrayList<String> ();
			
			// ------- 看文件是否存在 ---------
			File f = new File(prevVPath);
			if(f.exists()){
				// --------- 读取已经存在的  ---------
				BufferedReader bf0 = utils.Utils.readFile_returnBufferedReader(prevVPath);
				String line ="";
				while((line = bf0.readLine()) != null) {
					String[] lineArr = line.split(",");
					String stock = lineArr[0];
					String volS = lineArr[1];
					Double vol = Double.parseDouble(volS);
					
					//System.out.println(stock + "," + vol );
					prevV.put(stock, vol);
					alreadyExist.add(stock);
				}
			}
			
		
			// ------- 没有完成这项工作，现在完成 --------
			FileWriter fw = new FileWriter(prevVPath, true);
			ArrayList<Calendar> allTradingDate = utils.Utils.getAllTradingDate();
			
			Calendar thisCal = (Calendar) allTradingDate.get(0).clone();
			thisCal.setTime(sdf2.parse(todayDate));
			Calendar lastCal = allTradingDate.get(allTradingDate.indexOf(thisCal) - 1);
			
			Date lastDate = lastCal.getTime();
			String lastCalStr = sdf2.format(lastCal.getTime());
			
			String hist_1min_path = AVAT_ROOT_PATH + "historical 1min data\\" + lastCalStr + "\\";
			
			if(auctionData == null || auctionData.size() == 0)
				getAuctionData();
			
			//FileWriter f = new FileWriter("D:\\test.csv");
			for(Contract con : conArr) {
				String stock = con.symbol();
				
				if(alreadyExist.indexOf(stock) > -1) {
					continue;
				}
				
				String path = hist_1min_path + stock + ".csv";
				
				Double vol = 0.0;
				
				BufferedReader bf = utils.Utils.readFile_returnBufferedReader(path);
				String line = "";
				while((line = bf.readLine()) != null ) {
					String[] lineArr = line.split(",");
					
					Double thisVol = Double.parseDouble(lineArr[5]);
					vol += thisVol;
				}
				bf.close();
				
				Double auction = auctionData.get(stock).get(lastDate);
				
				vol += auction;
				
				prevV.put(stock, vol);
				fw.write(stock + "," + vol + "\n");
				//System.out.println(stock + "," + vol );
			}
			fw.close();
			
			
			
		}catch(Exception e)	{
			e.printStackTrace();
		}
		
		return prevV;
	}
	
	/**
	 * 得到auction data
	 */
	private static void getAuctionData(){
		try {
			String auctionPath = AVAT_ROOT_PATH + "avat para\\auction.csv";
			//logger.trace("read auction - auctionPath=" + auctionPath);
			
			BufferedReader bf = utils.Utils.readFile_returnBufferedReader(auctionPath);
			int count = 0;
			String line = "";
			ArrayList<Date> auctionDateArr = new ArrayList<Date>();
			auctionData = new LinkedHashMap<String, LinkedHashMap<Date, Double>> ();  // auction data stored in this variable
			while((line = bf.readLine()) != null) {
				String[] lineArr = line.split(",");
				String stock = "";
				LinkedHashMap<Date, Double> thisLineData = new LinkedHashMap<Date, Double>();
				//logger.trace("read auction - lineArr.length=" + lineArr.length );
				for(int i = 0; i < lineArr.length; i++) {
					if(count == 0 && i == 0) {
						continue;
					}
					if(count == 0 && i > 0) {
						String dateS = lineArr[i];
						Date date = sdf2.parse(dateS);
						auctionDateArr.add(date);
						//logger.trace("read auction - dateS=" + dateS);
					}
					
					if(count > 0 && i == 0) {
						stock = lineArr[i];
					}
					if(count > 0 && i > 0) {
						Double thisData = Double.parseDouble(lineArr[i]);
						thisLineData.put(auctionDateArr.get(i-1), thisData);
					}
				}
				auctionData.put(stock, thisLineData);
				
				count++;
			}
			bf.close();
		}catch(Exception e) {
			e.printStackTrace();
		}
		
	}
	
	/**
	 * 从过去20天的1min数据中得到历史横截面avat数据
	 * @return
	 */
	public static boolean preparePrevCrossSectionalAvat(ArrayList<Contract> conArr) {
		boolean isOK = true;
		try {
			logger.trace("read auction data");
			// ------------ read auction data ------------ 
			String auctionPath = AVAT_ROOT_PATH + "avat para\\auction.csv";
			//logger.trace("read auction - auctionPath=" + auctionPath);
			
			BufferedReader bf = utils.Utils.readFile_returnBufferedReader(auctionPath);
			int count = 0;
			String line = "";
			ArrayList<Date> auctionDateArr = new ArrayList<Date>();
			LinkedHashMap<String, ArrayList<Double>> auctionData = new LinkedHashMap<String, ArrayList<Double>> ();  // auction data stored in this variable
			while((line = bf.readLine()) != null) {
				String[] lineArr = line.split(",");
				
				String stock = "";
				ArrayList<Double> thisLineData = new ArrayList<Double>();
				//logger.trace("read auction - lineArr.length=" + lineArr.length );
				for(int i = 0; i < lineArr.length; i++) {
					if(count == 0 && i == 0) {
						continue;
					}
					if(count == 0 && i > 0) {
						String dateS = lineArr[i];
						Date date = sdf2.parse(dateS);
						auctionDateArr.add(date);
						//logger.trace("read auction - dateS=" + dateS);
					}
					
					if(count > 0 && i == 0) {
						stock = lineArr[i];
					}
					if(count > 0 && i > 0) {
						Double thisData = Double.parseDouble(lineArr[i]);
						thisLineData.add(thisData);
					}
				}
				auctionData.put(stock, thisLineData);
				
				count++;
			}
			bf.close();
			
			logger.trace("read 1min bar data");
			// ------------  read 1min bar data ------------ 
			String _1minBarRootPath = "D:\\stock data\\IB\\historical data 20170928\\";
			LinkedHashMap<String, LinkedHashMap<Date, LinkedHashMap<Date, Double>>> _1minCumVolData = new LinkedHashMap<String, LinkedHashMap<Date, LinkedHashMap<Date, Double>>>();
			for(int i = 0; i < conArr.size(); i++) {
				String stock = conArr.get(i).symbol();
				String _1minBarPath = _1minBarRootPath + stock + ".csv";
				//logger.trace("------- read 1 min: stock=" + stock);
				
				LinkedHashMap<Date, LinkedHashMap<Date, Double>> singleStockData = new LinkedHashMap<Date, LinkedHashMap<Date, Double>>();
				BufferedReader bf1 = utils.Utils.readFile_returnBufferedReader(_1minBarPath);
				int outerCount = -1;  // count for the 日期
				Date lastDate_yyyyMMdd = new Date();
				while((line = bf1.readLine()) != null) {  // 读个文件的每一行，文件存储了每只股票在不同时间的volume数据
					String[] lineArr = line.split(",");
					String dateS = lineArr[0];
					//logger.trace("------- read 1 min: stock=" + stock + " this time=" + dateS);
					
					String thisDateFormat = "yyyy-MM-dd HH:mm:ss";
					SimpleDateFormat sdf100 = new SimpleDateFormat (thisDateFormat);
					Date thisDate = sdf100.parse(dateS);
					
					String date_yyyyMMdd = sdf2.format(thisDate);
					Date thisDate_yyyyMMdd = sdf2.parse(date_yyyyMMdd);  // change to yyyyMMdd
					
					if(outerCount == -1)
						lastDate_yyyyMMdd = sdf.parse("19700101 09:30:00");
					
					if(thisDate_yyyyMMdd.after(lastDate_yyyyMMdd)) {  // day begining
						// get auction data
						ArrayList<Double> auctionDataSingleStock = auctionData.get(stock);
						//logger.debug("date_yyyyMMdd=" + date_yyyyMMdd);
						int ind1 = auctionDateArr.indexOf(thisDate_yyyyMMdd);
						Double thisAuctionData = auctionDataSingleStock .get(ind1);
						
						// fill the beginning data
						LinkedHashMap<Date, Double> thisData = new LinkedHashMap<Date, Double>();
						Date beginTime = sdf.parse(date_yyyyMMdd + " 09:30:00");
						Date nextTime = (Date) beginTime.clone();
						while(nextTime.before(thisDate)) {
							//logger.trace("------- read 1 min: stock=" + stock + " this time(while)=" + sdf.format(nextTime));
							thisData.put((Date) nextTime.clone(), thisAuctionData);
							
							nextTime = new Date(nextTime.getTime() + 1000 * 60);  // next min
						}
						thisData.put(thisDate, Double.parseDouble(lineArr[5]) + thisAuctionData);
						
						singleStockData.put(thisDate_yyyyMMdd, thisData);
					}
					/*
					if(sdf3.format(thisDate).equals("09:30:00") || sdf3.format(thisDate).equals("09:31:00")) { // day beginning (有些数据不全，从31分才开始)
						logger.trace("------- read 1 min: day begin=" + date_yyyyMMdd);
						// get auction data
						ArrayList<Double> auctionDataSingleStock = auctionData.get(stock);
						//logger.debug("date_yyyyMMdd=" + date_yyyyMMdd);
						int ind1 = auctionDateArr.indexOf(thisDate_yyyyMMdd);
						Double thisAuctionData = auctionDataSingleStock .get(ind1);
						
						LinkedHashMap<Date, Double> thisData = new LinkedHashMap<Date, Double>();
						if(sdf3.format(thisDate).equals("09:30:00"))
							thisData.put(thisDate, Double.parseDouble(lineArr[5]) + thisAuctionData); // add auction data
						if(sdf3.format(thisDate).equals("09:31:00")){
							Date beginDate = sdf.parse(date_yyyyMMdd + " 09:30:00");
							thisData.put(beginDate, thisAuctionData);
							thisData.put(thisDate, Double.parseDouble(lineArr[5]) + thisAuctionData);
						}
						
						singleStockData.put(thisDate_yyyyMMdd, thisData);
						
						outerCount++;
					}*/
					else {
						LinkedHashMap<Date, Double> thisData = singleStockData.get(thisDate_yyyyMMdd);
						ArrayList<Date> indArr = new ArrayList<Date>(thisData.keySet());
						Double lastVolData = thisData.get(indArr.get(indArr.size() - 1));
						Double thisCumVolData = lastVolData + Double.parseDouble(lineArr[5]);
						
						thisData.put(thisDate, thisCumVolData);
					}
					
					outerCount++;
					lastDate_yyyyMMdd = (Date) thisDate_yyyyMMdd.clone();
				} // end of while
				_1minCumVolData.put(stock, singleStockData);
			}
			
			logger.trace("calculate 20D cum avat & output");
			//  ------------ calculate 20D cum avat & output ------------ 
			// 从9:30 - 15:59共330分钟
			ArrayList<Date> timePath = getTimePath();
			int numOfMins = 330;
			//LinkedHashMap<String, LinkedHashMap<Date, Double>> histCumVolByStock = new LinkedHashMap<String, LinkedHashMap<Date, Double>>();
			LinkedHashMap<String, ArrayList<Double>> histCumVolByStock20D = new LinkedHashMap<String, ArrayList<Double>> (); 
			LinkedHashMap<String, ArrayList<Double>> histCumVolByStock5D = new LinkedHashMap<String, ArrayList<Double>> (); 
			for(int i = 0; i < conArr.size(); i++) {
				String stock = conArr.get(i).symbol();
				logger.trace("[cal] - stock=" + stock);
				
				LinkedHashMap<Date, LinkedHashMap<Date, Double>> singleStockData = _1minCumVolData.get(stock);
				ArrayList<Double> thisStock20DCumVol = new ArrayList<Double> ();
				ArrayList<Double> thisStock20D_realDays = new ArrayList<Double> ();
				ArrayList<Double> thisStock5DCumVol = new ArrayList<Double> ();
				ArrayList<Double> thisStock5D_realDays = new ArrayList<Double> ();
				
				// initializing 
				for(int j = 0; j < numOfMins; j++) {
					thisStock20DCumVol.add(0.0);
					thisStock20D_realDays.add(0.0);
					thisStock5DCumVol.add(0.0);
					thisStock5D_realDays.add(0.0);
				}
				
				ArrayList<Date> allDates = new ArrayList<Date>(singleStockData.keySet());
				if(allDates.size() != 20) {
					logger.trace("[cal] - stock=" + stock + " allDates not 20!") ;
				}
				for(int j = 0; j < allDates.size(); j++) {  // 循环每一天
					Date thisDate = allDates.get(j);
					LinkedHashMap<Date, Double> thisDateData = singleStockData.get(thisDate);
					
					ArrayList<Date> intrayDate = new ArrayList<Date>(thisDateData.keySet());
					for(int k = 0; k < numOfMins; k++) {  //  循环一天内每个时间
						Double thisCumVol = thisDateData.get(intrayDate.get(k));
						thisStock20DCumVol.set(k, thisStock20DCumVol.get(k) + thisCumVol);
						thisStock20D_realDays.set(k, thisStock20D_realDays.get(k) + 1);
						if(j >= 15) {
							thisStock5DCumVol.set(k, thisStock5DCumVol.get(k) + thisCumVol);
							thisStock5D_realDays.set(k, thisStock5D_realDays.get(k) + 1);
						}
					}
					
				} // end of for j
				histCumVolByStock20D.put(stock, thisStock20DCumVol);
				histCumVolByStock5D.put(stock, thisStock5DCumVol);
				
				// output
				String avatPath = AVAT_ROOT_PATH + "avat para\\" + stock + ".csv";
				FileWriter fw = new FileWriter (avatPath);
				for(int j = 0; j < timePath.size(); j++) {
					String timeS = sdf.format(timePath.get(j));
					fw.write(timeS + "," 
							+ String.valueOf(thisStock20DCumVol.get(j) / thisStock20D_realDays.get(j)) + ","
							+ String.valueOf(thisStock5DCumVol.get(j) / thisStock5D_realDays.get(j)) + "\n");
				}
				fw.close();
			} // end of for i
			
			
			
		}catch(Exception e) {
			e.printStackTrace();
			isOK = false;
		}
		
		return isOK;
	}

	/**
	 * 准备过去20天的横截面avat
	 * @param conArr
	 * @param lastDateStr  这一天往前20天的历史数据（不包括这一天）
	 * @param dateFormat
	 * @return
	 */
	public static boolean preparePrevCrossSectionalAvat2(ArrayList<Contract> conArr, String lastDateStr /*exclusive*/, String dateFormat) {
		boolean isOK = true;
		try {
			int numOfTradingDays = 20;
			avatHistPath = AVAT_ROOT_PATH + "avat para\\" + todayDate + "\\" ;  // 存放avat历史数据的文件夹
			
			// 先判断哪些已经download了
			ArrayList<String> alreadyExists = new ArrayList<String> (); 
			File fileList = new File(avatHistPath);
			if(!fileList.exists())
				fileList.mkdirs();
			else {
				String[] files = fileList.list();
				for(int i = 0; i < files.length; i++) {
					String file = files[i];
					String suffix = file.length() >= 4? file.substring(file.length()-4, file.length()):"";
					if(suffix.equals(".csv")) {
						alreadyExists.add(file.substring(0,file.length() - 4));
					}
				}
			}
			
			logger.trace("read auction data");
			// ------------ read auction data ------------ 
			if(auctionData == null || auctionData.size() == 0)
				getAuctionData();
			
			// --------- initializing varaibles ----------
			// 从9:30 - 15:59共330分钟
			ArrayList<Date> timePath = getTimePath();
			int numOfMins = 330;
			LinkedHashMap<String, LinkedHashMap<Date, Double>> cumHistAvat20D_byStock = new LinkedHashMap<String, LinkedHashMap<Date, Double>>();
			LinkedHashMap<String, LinkedHashMap<Date, Double>> cumHistAvat5D_byStock = new LinkedHashMap<String, LinkedHashMap<Date, Double>>();
						
			logger.trace("read 1min bar data");
			// ------------  read 1min bar data ------------  (还没考虑auction data) 
			String _1minBarRootPath = AVAT_ROOT_PATH + "\\historical 1min data\\";
			
			SimpleDateFormat sdf_temp = new SimpleDateFormat(dateFormat);
			Date lastDate = sdf_temp.parse(lastDateStr);
			
			ArrayList<Calendar> allTradingCal = utils.Utils.getAllTradingDate(allTrdingDatePath);
			ArrayList<Date> allTradingDate = new ArrayList<Date> ();
			Calendar lastDateCal = (Calendar) allTradingCal .get(0).clone();
			lastDateCal.setTime((Date) lastDate.clone()); 
			Calendar recentCal = utils.Utils.getMostRecentDate(lastDateCal, allTradingCal);
			
			int lastDateInd = allTradingCal.indexOf(recentCal);
			lastDateInd = lastDateInd - 1;
			//lastDate = (Date) recentCal .getTime().clone();
			
			for(int i = 0; i < conArr.size(); i++) {
				String stock = conArr.get(i).symbol();
				//String _1minBarPath = _1minBarRootPath + stock + ".csv";
				//logger.info("[Prepare avat]  stock=" + stock);
				if(alreadyExists.indexOf(stock) > -1) // 对于已经存在的股票就不用再进行处理
					continue;
	
				LinkedHashMap<Date, Double> thisStockCumAvat20D = new LinkedHashMap<Date, Double>();
				LinkedHashMap<Date, Double> thisStockCumAvat5D = new LinkedHashMap<Date, Double>();
				LinkedHashMap<Date, Double> thisStockAuctionData = auctionData.get(stock);
				
				for(int j = 0; j < numOfTradingDays; j++) {
					//logger.info("   [prepare avat] j=" + j);
					Calendar thisTrdCal = allTradingCal.get(lastDateInd - j);
					String thisTrdDateStr = sdf2.format(thisTrdCal.getTime());
					
					//logger.trace("stock=" + stock + " " + thisTrdDateStr);
					
					String _1minBarPath = _1minBarRootPath + thisTrdDateStr + "\\" + stock + ".csv";
					
					BufferedReader bf_ha = utils.Utils.readFile_returnBufferedReader(_1minBarPath);
					String line = "";
					int timePathInd = 0;
					Double todayAvat = thisStockAuctionData.get(sdf2.parse(thisTrdDateStr));
					int temp_count = 0;
					while((line = bf_ha.readLine()) != null) {
						temp_count++;
						//logger.info("             [prepare avat] line=" + temp_count + " content=" + line);
						if(timePathInd == timePath.size())  // 不需要15：59之后的数据
							break;

						String[] lineArr = line.split(",");
						
						Double thisTimeVol = Double.parseDouble(lineArr[5]);
						if(thisTimeVol == null)
							thisTimeVol = 0.0;
						//todayAvat += vol;
						
						SimpleDateFormat sdf_temp2 =  new SimpleDateFormat ("yyyy-MM-dd HH:mm:ss"); 
						Date readTime0 = sdf_temp2.parse(lineArr[0]);
						String readTime1Str = sdf3.format(readTime0); // change to hh:MM:ss
						Date readTime1 = sdf3.parse(readTime1Str);
						
						String timePath1Str = sdf3.format(timePath.get(timePathInd)); // change to hh:MM:ss
						Date timePath1 = sdf3.parse(timePath1Str);
						
						while(!timePath1.after(readTime1)){ // timePath1要去追 readTime1
							if(timePath1.equals(readTime1))
								todayAvat += thisTimeVol;
							
							Date timePath0 = timePath.get(timePathInd);
							logger.trace("readTime=" + sdf.format(readTime1) + " timePath1=" + sdf.format(timePath1) + " timePathInd=" + timePathInd);
							
							Double todayAvat20D = todayAvat.doubleValue();
							Double todayAvat5D = todayAvat.doubleValue();
							
							Double avat20D_prevDay = 0.0;	// 上一天的值，比如现在是9：34，则这个值存储了昨天9：34的累计的avat
							avat20D_prevDay = thisStockCumAvat20D.get(timePath.get(timePathInd));
							if(avat20D_prevDay == null)
								avat20D_prevDay = 0.0;
							
							Double todayCumAvat20D = todayAvat20D + avat20D_prevDay;
							
							thisStockCumAvat20D.put(timePath0, todayCumAvat20D);
							logger.trace(" todayAvat20D=" + todayAvat20D + " vol20D_prevDay=" + avat20D_prevDay + " todayCumAvat20D=" + todayCumAvat20D);
							
							// update 5D data
							if(j <= 4) {
								Double vol5D_prevDay = 0.0;	// 上一天的值，比如现在是9：34，则这个值存储了昨天9：34的累计的avat
								vol5D_prevDay = thisStockCumAvat5D.get(timePath.get(timePathInd));
								if(vol5D_prevDay == null)
									vol5D_prevDay = 0.0;
								
								Double todayCumAvat5D = todayAvat5D + vol5D_prevDay;
								
								thisStockCumAvat5D.put(timePath0, todayCumAvat5D);
								//logger.trace(" todayAvat5D=" + todayAvat5D + " vol5D_prevDay=" + vol5D_prevDay + " todayCumAvat5D=" + todayCumAvat5D);
								
							}
							timePathInd++;
							
							if(timePathInd == timePath.size())  // 不需要15：59之后的数据
								break;
							
							timePath1Str = sdf3.format(timePath.get(timePathInd)); // change to hh:MM:ss
							timePath1 = sdf3.parse(timePath1Str);
						
						} // timePath1要去追 readTime1
						
					} // end of while
					bf_ha.close();
					
				} // end of for - 每天循环
				cumHistAvat20D_byStock.put(stock, thisStockCumAvat20D);
				
				// ----------- output ---------------
				logger.trace("--- outoput data, stock=" + stock + " thisStockCumAvat5D.size=" + thisStockCumAvat5D.size() + " thisStockCumAvat20D.size=" + thisStockCumAvat20D.size());
				
				String avatPath = avatHistPath + stock + ".csv";  // 虽然路径是以今日命名的，但是包含的历史数据不含今日的数据
				FileWriter fw = new FileWriter (avatPath);
				for(int j = 0; j < timePath.size(); j++) {
					String timeS = sdf.format(timePath.get(j));
					fw.write(timeS + "," 
							+ String.valueOf(thisStockCumAvat5D.get(timePath.get(j)) / 5.0) + ","
							+ String.valueOf(thisStockCumAvat20D.get(timePath.get(j)) / 20.0) + "\n");
				}
				fw.close();
				
				
				//Thread.sleep(1000 * 100000);
			} // 每只股票循环
			
		}catch(Exception e) {
			e.printStackTrace();
			isOK = false;
		}
		
		return isOK;
	}
	
	//-------------------------------- historical bar data (1min, 3min, ..., daily, ... ) ------------------------
	public static boolean downloadHistoricalBarData(MyAPIController myController, ArrayList<Contract> conArr, 
			String endDateTime /*yyyyMMdd HH:mm:ss*/, int duration, DurationUnit durationUnit, BarSize barSize, WhatToShow whatToShow,
			String outputRootPath) {
		
		boolean isOK = true;
		try {
			//String outputPath = ibRootPath + "historical ";
			ArrayList<MyIHistoricalDataHandler> histHandlerArr = myController.histHandlerArr;
			int numOfRead = 20;
			boolean rthOnly = true;
			int counter  = 1;
			int counter2 = 0;
			for(int i = 0; i < conArr.size(); i++) {
				logger.debug("[historical bar] i=" + i + " Downloading " + conArr.get(i).symbol());
				
				MyIHistoricalDataHandler myHist = new MyIHistoricalDataHandler(conArr.get(i).symbol(), outputRootPath);
				histHandlerArr.add(myHist);
				myController.reqHistoricalData(conArr.get(i), endDateTime, duration, durationUnit, barSize, whatToShow, rthOnly, false, myHist);
				
				// 每次只读取numOfRead的整数倍只股票的信息
				int cum = 0;
				if(i == (numOfRead * counter - 1)) {  // i 是numOfRead的整数倍
					int startInd = numOfRead * (counter - 1);
					
					while(cum != numOfRead) { 
						cum = 0;
						for(int j = startInd; j <= i; j++) {
							MyIHistoricalDataHandler thisHist = histHandlerArr.get(j);
							int isEnd = thisHist.isEnd;
							int isActive = thisHist.isActive;
							if(isEnd == 1 && isActive == 1) {
								//myController.cancelHistoricalData(thisHist);
								//thisHist.isActive = 0;
							}
							cum += isEnd;
						}
						
						Thread.sleep(1000 * 2);
						logger.debug("Ping Pong!");
						
						counter2 ++;
						if(counter2 >= 30) { // if wait for too long, 强制断开
							break;
						}
					}
					
					counter2 = 0;
					logger.debug("i = " + i + " Download END!");
					counter ++;
				}
				
			}
		}catch(Exception e) {
			e.printStackTrace();
			isOK = false;
		}
		
		return isOK;
	}
	
	/**
	 * 下载当天的1min bar的数据
	 * @param myController
	 * @param conArr
	 * @param date
	 * @param dateFormat
	 * @return
	 */
	public static boolean downloadHistorical1MinData(MyAPIController myController, ArrayList<Contract> conArr, String date, String dateFormat) {
		boolean isOK = true;
		try {
			Date d = new SimpleDateFormat(dateFormat).parse(date);
			String d_yyyyMMdd = sdf2.format(d);
			
			logger.debug("historical 1min - " + d_yyyyMMdd);
			
			isOK = downloadHistoricalBarData(myController, conArr, 
					d_yyyyMMdd + " 16:10:00", 1, DurationUnit.DAY, BarSize._1_min, WhatToShow.TRADES,
					AVAT_ROOT_PATH + "\\historical 1min data\\" + d_yyyyMMdd + "\\"
					);
			
		}catch(Exception e) {
			e.printStackTrace();
			isOK = false;
			//logger.error();
		}
		
		logger.info("historical 1min data: downloading completed");
		return isOK;
	}
	
	public static boolean downloadHistorical1MinData_20D(MyAPIController myController, ArrayList<Contract> conArr, String endDate, String dateFormat) {
		boolean isOK = true;
		try {
			ArrayList<Calendar> allTrdCal = utils.Utils.getAllTradingDate();
			Calendar todayCal = (Calendar) allTrdCal.get(0).clone();
			todayCal.setTime(new SimpleDateFormat(dateFormat).parse(endDate));
			int todayInd = allTrdCal.indexOf(todayCal);
			
			for(int i = 0 ; i < 20; i++) {
				Date d = allTrdCal.get(todayInd - i).getTime();
				String d_yyyyMMdd = sdf2.format(d);
				logger.debug("-- downloading... date=" + d_yyyyMMdd);
				
				boolean isOK2 = downloadHistorical1MinData(myController, conArr, d_yyyyMMdd, dateFormat2);

				if(!isOK2) {
					isOK = false;
					logger.error("date=" + d_yyyyMMdd + " downloading failed....");
				}
			}
			
		}catch(Exception e) {
			e.printStackTrace();
			isOK = false;
		}
		
		logger.info("historical 1min data (20 Day): downloading completed");
		return isOK;
	}
	
	/**
	 * 【仅适用于港股】
	 * 得到正确的股票价格，主要是tick size要对，比如对于股价在100-200之间的股票，tick size为0.1，如果股票价格是170.01，就不正确
	 * 这个函数会返回170.1
	 * @param p
	 * @return
	 */
	public static Double getCorrectPrice_up(Double p) {
		ArrayList<Double> arr = getCorrectPrice( p);
		
		double tickSize = arr.get(0);
		double lowerBound = arr.get(1);
		double upperBound = arr.get(2);
		
		int multiples = (int) (Math.floor((p-lowerBound)/tickSize) + 1);
		
		return lowerBound + multiples * tickSize;
	}
	
	/**
	 * 【仅适用于港股】
	 * 得到正确的股票价格，主要是tick size要对，比如对于股价在100-200之间的股票，tick size为0.1，如果股票价格是170.01，就不正确
	 * 这个函数会返回170
	 * @param p
	 * @return
	 */
	public static Double getCorrectPrice_down(Double p) {
		ArrayList<Double> arr = getCorrectPrice( p);
		
		double tickSize = arr.get(0);
		double lowerBound = arr.get(1);
		double upperBound = arr.get(2);
		
		int multiples = (int) (Math.floor((p-lowerBound)/tickSize));
		
		return lowerBound + multiples * tickSize;
	}
	
	private static ArrayList<Double> getCorrectPrice(Double p) {
		double tickSize = 0.0;
		double lowerBound = 0.0;
		double upperBound = 0.0;
		
		if(p >= 0.01 && p <= 0.25) {
			tickSize = 0.001;
			lowerBound = 0.01;
			upperBound = 0.25;
		}
		if(p > 0.25 && p <= 0.50) {
			tickSize = 0.005;
			lowerBound = 0.25;
			upperBound = 0.50;
		}
		if(p > 0.50 && p <= 10.0) {
			tickSize = 0.01;
			lowerBound = 0.50;
			upperBound = 0.10;
		}if(p >10 && p <= 20) {
			tickSize = 0.02;
			lowerBound = 10;
			upperBound = 20;
		}
		if(p > 20 && p <= 100.0) {
			tickSize = 0.05;
			lowerBound = 20;
			upperBound = 100;
		}if(p > 100.0 && p <= 200.0) {
			tickSize = 0.1;
			lowerBound = 100.0;
			upperBound = 200.0;
		}
		if(p > 200 && p <= 500) {
			tickSize = 0.2;
			lowerBound = 200;
			upperBound = 500;
		}
		if(p > 500 && p <= 1000) {
			tickSize = 0.5;
			lowerBound = 500;
			upperBound = 1000;
		}
		if(p > 1000 && p <= 2000) {
			tickSize = 1;
			lowerBound = 1000;
			upperBound = 2000;
		}
		if(p > 2000 && p <= 5000) {
			tickSize = 2;
			lowerBound = 2000;
			upperBound = 5000;
		}
		if(p > 5000 && p <= 9995) {
			tickSize = 5;
			lowerBound = 5000;
			upperBound = 9995;
		}
		
		ArrayList<Double> arr = new ArrayList<Double>();
		arr.add(tickSize);
		arr.add(lowerBound);
		arr.add(upperBound);
		
		return arr;
	}

}
