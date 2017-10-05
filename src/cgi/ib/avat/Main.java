package cgi.ib.avat;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.ib.client.Contract;
import com.ib.client.Types.BarSize;
import com.ib.client.Types.DurationUnit;
import com.ib.client.Types.WhatToShow;
import com.ib.controller.ApiConnection;
import com.ib.controller.ApiController.IConnectionHandler;

public class Main {
	public static Logger logger = Logger.getLogger(Main.class.getName());
	public static String AVAT_ROOT_PATH = "Z:\\AVAT\\";
	
	public static void main(String[] args) {
		try {
			String dateFormat = "yyyyMMdd HH:mm:ss";
			SimpleDateFormat sdf = new SimpleDateFormat (dateFormat); 
			String todayDate = new SimpleDateFormat ("yyyyMMdd").format(new Date());todayDate="20171006";
			ArrayList<Calendar> allTradingDate = utils.Utils.getAllTradingDate("D:\\stock data\\all trading date - hk.csv");
			SimpleDateFormat sdf_100 = new SimpleDateFormat ("yyyyMMdd HH_mm_ss"); 
			
			AVAT.todayDate = todayDate;
			AvatUtils.todayDate = todayDate;
			
			// ------------ MODE -----------
			int mode = 1;
			/*
			 * 0 - download historical data
			 * 1 - avat: real time running
			 */
			
			String host = "127.0.0.1";   //  "127.0.0.1" the local host
			int port = 7496;
			int clientId = (int) (Math.random() * 100) + 1;  // a self-specified unique client ID
			
			MyLogger inLogger = new MyLogger();
			MyLogger outLogger = new MyLogger();
			
			
			MyIConnectionHandler myConnectionHandler = new MyIConnectionHandler();
			//****** the main controller **********
			MyAPIController myController = new MyAPIController(myConnectionHandler, inLogger, outLogger	);
			myController.connect(host, port, clientId, null);
			
			// create EClient
			//MyEReaderSignal signal = new MyEReaderSignal();
			//ApiConnection myConnection = new ApiConnection(myController, inLogger, outLogger);
			ApiConnection myClient = myController.client();  
			//myClient.eConnect(host, port, clientId, true);
			if(myClient.isConnected()){
				System.out.println("Is connected!");
				try {
					Thread.sleep(1000 * 3);   
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			else{
				System.out.println("Not connected!");
				return;
			}
			
			//======== constructing contracts ===========
			ArrayList<Contract> conArr = new ArrayList<Contract> ();
			ArrayList<String> stockList = new ArrayList<String>();
			//ArrayList<String> industryList = new ArrayList<String>();
			
			BufferedReader bf = utils.Utils.readFile_returnBufferedReader(AVAT_ROOT_PATH + "stocklist.csv");
			stockList.addAll(Arrays.asList(bf.readLine().split(",")));
			for(int i = 0; i < stockList.size(); i ++) {
				String symbol = stockList.get(i);
				
				Contract con1 = new Contract();
				con1.symbol(stockList.get(i));
				con1.exchange("SEHK");
				con1.secType("STK");
				con1.currency("HKD");
				
				conArr.add(con1);
			}
			//industryList.addAll(Arrays.asList(bf.readLine().split(",")));
			bf.close();
			
			if(mode == 0) {
				//AvatUtils.downloadHistorical1MinData_20D(myController, conArr, "20170908", "yyyyMMdd");
				AvatUtils.downloadHistorical1MinData(myController, conArr, "20171004", "yyyyMMdd");
				//AvatUtils.preparePrevCrossSectionalAvat2(conArr,"20170929", "yyyyMMdd");
				logger.trace("prepare ends...");
				return;
			}
			
			if(mode == 1) {
				AVAT.setting(myController, conArr, AVAT_ROOT_PATH);
				
				AVAT.start();
			}
			
			if(false) {
				
				// ========== requesting top mkt data =========
				int numOfTopMktDataStock = conArr.size();
				// ------- avat - historical avat ---------
				Map<String, Map<Date,ArrayList<Double>>> avatHist = AvatUtils.getPrevCrossSectionalAvat(conArr);
				
				// ------- avat - yesterday close ---------
				Map<String, Double> avatPrevClose = AvatUtils.getPrevClose();
				
				// ------- avat - industry ---------
				ArrayList<Object> data = AvatUtils.getIndustry();
				Map<String, String> avatIndustry = (Map<String, String>) data.get(0);
				Map<String, ArrayList<String>> avatIndustry_byIndustry = (Map<String, ArrayList<String>>) data.get(1);  // industry - stock list
				
				// ------ avat index members ----------
				ArrayList<String> avatIndexMembers = AvatUtils.getIndexMembers();
				
				// ------- avat time path by 1min ---------
				ArrayList<Date> avatTimePath = AvatUtils.getTimePath();
				
				// -------- avat get today's auction --------
				//Map<String, Double> todayAuction = AvatUtils.getTodayAuction();
				
				// ---------- requesting data -----------
				ArrayList<MyITopMktDataHandler> topMktDataHandlerArr = new ArrayList<MyITopMktDataHandler>();
				if(true) {
					for(int i = 0; i < numOfTopMktDataStock; i++) {
						Contract con = conArr.get(i);
						MyITopMktDataHandler myTop = new MyITopMktDataHandler(con.symbol(), AVAT_ROOT_PATH, todayDate);
						topMktDataHandlerArr.add(myTop);
						myController.reqTopMktData(con, "233,375", false, false, myTop);
						/*
						 * Generic tick type:
						 * 233 - RT volume
						 * 375 - RT trade volume
						 */
						
					}
				}
				Thread.sleep(1000 * 5);
				
				// ------- output data regularly --------
				String avatRecordPath = AVAT_ROOT_PATH + "avat record\\";
						
				Date now = new Date();
				Map<String,ArrayList<Double>> avatRatioNow = new HashMap();
				ArrayList<AvatRecordSingleStock> avatRecord = new ArrayList<AvatRecordSingleStock>();
				ArrayList<AvatRecordSingleStock> lastAvatRecord = new ArrayList<AvatRecordSingleStock>();
				Map<String, Integer> lastRankingData = new HashMap();
				
				boolean isFirst = true;
				while(now.before(avatTimePath.get(avatTimePath.size() - 1))) {
					logger.info("now = " + sdf.format(now));
					if(now.before(sdf.parse(todayDate + " 09:30:00"))) {
						logger.info("Market not open!");
						Thread.sleep(1000 * 60);
						now = new Date();
					}
					logger.info("Generating avat!");
					
					avatRecord = new ArrayList<AvatRecordSingleStock>();
						
					logger.debug("-- topMktDataHandlerArr.size=" + topMktDataHandlerArr.size());
					for(int i = 0; i < topMktDataHandlerArr.size(); i++) {
						MyITopMktDataHandler myTop = topMktDataHandlerArr.get(i);
						
						Double volume = myTop.lastestVolume;
						String stock = myTop.stockCode;
						Double price = myTop.lastestPrice;
						Double turnover = myTop.lastestRTTurnover;
						logger.debug("------- stock = " + stock);
						
						// find the nearest date
						Map<Date,ArrayList<Double>> avatHist_stock = avatHist.get(stock);
						Set<Date> dateSet = avatHist_stock.keySet();
						long diff = 1000 * 1000 * 1000;
						Date minDate = new Date();
						for(Date d : dateSet) {
							if(d.before(now)) {
								long thisDiff = now.getTime() - d.getTime();
								if(thisDiff < diff) {
									diff = thisDiff;
									minDate = (Date) d.clone();
								}
							}
						}
						//logger.debug("------- get min date=" + sdf.format(minDate));
						ArrayList<Double> avatHist_nearData = avatHist_stock.get(minDate);
						
						// get historical avat data
						Double avat5D = avatHist_nearData.get(0);
						Double avat20D = avatHist_nearData.get(1);
						
						// get current avat
						Double ratio5D = volume / avat5D;
						Double ratio20D = volume / avat20D;
						
						ArrayList<Double> temp = new ArrayList<Double>();
						temp.add(ratio5D);
						temp.add(ratio20D);
						
						avatRatioNow.put(stock, temp);
						
						//logger.debug("------- avat 5D = " + ratio5D);
						
						// get prev close
						Double prevClose = avatPrevClose.get(stock);
						//logger.debug("------- prevClose="+prevClose);
						Double prevCloseChgPct = (price / prevClose - 1) * 100.0;
						//logger.debug("------- 1222");
						// get industry
						String industry = avatIndustry.get(stock);
						//logger.debug("------- 1223");
						
						AvatRecordSingleStock at = new AvatRecordSingleStock(now.getTime(), stock, price, prevCloseChgPct, ratio5D, ratio20D, industry);
						at.turnover = turnover;
						avatRecord.add(at);
						
						logger.debug("------- next ");
					}  // end of for
					
					logger.info("-- sorting avat record");
					// sorting
					Collections.sort(avatRecord, AvatRecordSingleStock.getComparator());
					
					// calculate industry avg
					logger.info("-- calculating industry avg");
					ArrayList<String> industryList = new ArrayList<String> ();
					ArrayList<Double> industryCum = new ArrayList<Double>();
					ArrayList<Integer> industryNum = new ArrayList<Integer>();
					for(int i = 0; i < avatRecord.size(); i++) {
						AvatRecordSingleStock rec = avatRecord.get(i);
						String thisIndustry = rec.industry;
						Double thisRatio = rec.avatRatio5D;
						
						int ind = industryList.indexOf(thisIndustry);
						if(ind == -1) {
							industryList.add(thisIndustry);
							industryCum.add(thisRatio);
							industryNum.add(1);
						}else {
							industryCum.set(ind, industryCum.get(ind) + thisRatio);
							industryNum.set(ind, industryNum.get(ind) + 1);
						}
					}
					ArrayList<Double> industryAvg = new ArrayList<Double> ()	;
					for(int i = 0;i < industryList.size(); i++) {
						industryAvg.add(industryCum.get(i) / industryNum.get(i));
					}
					
					// fill avatRecord & output
					logger.info("-- fill avatRecord & output");
					FileWriter fw = new FileWriter(avatRecordPath + sdf_100.format(now) + ".csv");
					fw.write("Equity,Last_Price,Px Chg % vs T-1 Close,Vol / 5D AVAT"
							+ ",Vol / 20D AVAT,Industry,Industry Average,Turnover,Rank Difference"
							+ ",New Rank,Original Rank,Index Member?,Turn > Req.?\n");
					for(int i = 0;i < avatRecord.size(); i++) {
						AvatRecordSingleStock rec = avatRecord.get(i);
						
						// industry avg
						rec.industryAvg = industryAvg.get(industryList.indexOf(rec.industry));
						
						// ranking
						rec.newRank = i+1;
						Integer lastRank = lastRankingData.get(rec.stockCode);
						if(lastRank == null)
							lastRank = 0;
						rec.oldRank = lastRank;
						rec.rankDiff = i+1 - lastRank;
						
						lastRankingData.put(rec.stockCode, i+1);  // update lastRankingData
						
						// index member
						int isIndexMember = avatIndexMembers.indexOf(rec.stockCode);
						if(isIndexMember == -1)
							rec.isIndexMember = "N";
						else
							rec.isIndexMember = "Y";
						
						// output
						fw.write(rec.toString() + "\n");
					}
					fw.close();
					
					// industry table
					logger.info("-- industry table");
					FileWriter fw2 = new FileWriter(avatRecordPath + sdf_100.format(now) + " industry.csv");
					fw2.write("Industry,Industry Average\n");
					ArrayList<Double> industryAvgCopy = (ArrayList<Double>) industryAvg.clone();
					Collections.sort(industryAvgCopy, Collections.reverseOrder());  // 从高到低排列
					for(int i = 0;i < industryAvgCopy.size(); i++) {
						Double thisAvg = industryAvgCopy.get(i);
						int ind1 = industryAvg.indexOf(thisAvg);
						
						String thisIndustry = industryList.get(ind1);
						ArrayList<String> industryStockList = avatIndustry_byIndustry.get(thisIndustry);
						
						fw2.write(thisIndustry + "," + String.valueOf(thisAvg));
						for(int j = 0; j < industryStockList.size(); j++) {
							fw2.write("," + industryStockList.get(j));
						}
						fw2.write("\n");
					}
					fw2.close();
					
					logger.info("Generating avat ends!");
					Thread.sleep(1000 * 60); // wait for 1 min
					lastAvatRecord = (ArrayList<AvatRecordSingleStock>) avatRecord.clone();
					isFirst = false;
					now = new Date();
				}  // end of while
			}
			
			// ==== requesting real time bars ========
			ArrayList<MyIRealTimeBarHandler> rtBarHandlerArr = new ArrayList<MyIRealTimeBarHandler>();
			if(false) {
				boolean rthOnly_realtime = true;
				for(int i = 0; i < conArr.size(); i++) {
					MyIRealTimeBarHandler myRt = new MyIRealTimeBarHandler(conArr.get(i).symbol());
					rtBarHandlerArr.add(myRt);
					myController.reqRealTimeBars(conArr.get(i), WhatToShow.TRADES, rthOnly_realtime, myRt);
				}
				
			}
			
			// ========== requesting historical bar data ========
			//System.out.println("here68423");
			ArrayList<MyIHistoricalDataHandler> histHandlerArr = new ArrayList<MyIHistoricalDataHandler>();
			if(false) {
				int numOfRead = 2;
				boolean rthOnly = true;
				int counter  = 1;
				for(int i = 0; i < conArr.size(); i++) {
					logger.debug("i=" + i + " Downloading " + conArr.get(i).symbol());
					
					MyIHistoricalDataHandler myHist = new MyIHistoricalDataHandler(conArr.get(i).symbol(),"D:\\stock data\\IB\\historical data\\");
					histHandlerArr.add(myHist);
					myController.reqHistoricalData(conArr.get(i), "20170927 14:30:00", 30, DurationUnit.DAY, BarSize._1_day, WhatToShow.TRADES, rthOnly, false, myHist);
					//myController.reqHistoricalData(contract, endDateTime, duration, durationUnit, barSize, whatToShow, rthOnly, keepUpToDate, handler);
					
					//MyIHistoricalTickHandler myHistTick = new MyIHistoricalTickHandler(conArr.get(i).symbol());
					//myController.reqHistoricalTicks(conArr.get(i), null, "20170927 16:10:00", 1000, WhatToShow.TRADES.toString(), 1, true, myHistTick);
					
					// 每次只读取numOfRead的整数倍只股票的信息
					int cum = 0;
					if(i == (numOfRead * counter - 1)) {  // i 是45的整数倍
						int startInd = numOfRead * (counter - 1);
						
						while(cum != numOfRead) { 
							cum = 0;
							for(int j = startInd; j <= i; j++) {
								MyIHistoricalDataHandler thisHist = histHandlerArr.get(j);
								int isEnd = thisHist.isEnd;
								cum += isEnd;
							}
							
							Thread.sleep(1000 * 3);
							logger.debug("Ping Pong!");
						}
						logger.info("i = " + i + " Download END!");
						counter ++;
					}
					
				}
			}
			
			//============== requesting historical tick data ===============
			ArrayList<MyIHistoricalTickHandler> histTickHandlerArr = new ArrayList<MyIHistoricalTickHandler>();
			if(false) {
				int numOfRead = 1;
				int counter  = 1;
				String startTimeS = "20170831 09:00:00";
				String endTimeS = "20170926 17:00:30";
				Date endTime = sdf.parse(endTimeS);
				Date startTime = sdf.parse(startTimeS);
				Long endTimeL = endTime.getTime();
				Long startTimeL = startTime.getTime();
				
				String dataType = "TRADES";   // 
				for(int i = 1; i < conArr.size(); i++) {
					logger.debug("i=" + i + " Downloading " + conArr.get(i).symbol());
	
					int numOfData = 1000;
					long nextTimeL = startTimeL;
					String lastTimeS = startTimeS;
					MyIHistoricalTickHandler myHistTick = new MyIHistoricalTickHandler(conArr.get(i).symbol());
					histTickHandlerArr.add(myHistTick);
					
					//myController.reqHistoricalTicks(conArr.get(i), startTimeS, null, numOfData, dataType, 1, true, myHistTick);
					
					int isNo_trades = 0;
					while(nextTimeL <= endTimeL ) {  // 这样只可以读一天的数据
						logger.trace("Downloading ... " + sdf.format(new Date(nextTimeL)));
						// ========= step 1: flush data and initializing ==========
						ArrayList<Object> data_trades = myHistTick.getData_trades();
						
						myHistTick.flushData_trades();
						myHistTick.initialize();
						
						// ============ step 2: downloading data ============				
						String nextTimeS =  sdf.format(new Date(nextTimeL));
						logger.info("Downloading... nextTimeS = " + nextTimeS + " stock=" + conArr.get(i).symbol());
						myController.reqHistoricalTicks(conArr.get(i), nextTimeS, null, numOfData, dataType, 1, true, myHistTick);
						
						// =========== step 3: update next time & indicator ===========
						while(myHistTick.getIsEnd_trades() != 1) {  // 判断是否将这次的数据都读完了
							Thread.sleep(500);
						}
						isNo_trades = myHistTick.getIsNo_trades();
						//nextTimeL = myHistTick.getLastTime_trades(); // 更新next time
						
						if(isNo_trades == 1) { // day end
							// get next trading date
							Calendar lastTimeCal = (Calendar) allTradingDate.get(0).clone();
							SimpleDateFormat sdf_temp = new SimpleDateFormat("yyyyMMdd");
							String lastTimeS2 =  sdf_temp.format(new Date(nextTimeL));
							logger.trace("----- last time date = " + lastTimeS2);
							
							lastTimeCal.setTime(sdf_temp.parse(lastTimeS2));  // change to yyyyMMdd
							lastTimeCal.add(Calendar.DATE, 1);
							
							//int ind = allTradingDate.indexOf(lastTimeCal);
							//Calendar nextDate = allTradingDate.get(ind + 1);
							String nextDateS = sdf_temp.format(lastTimeCal.getTime().getTime());
							logger.trace("--- next day = " + nextDateS);
							
							nextTimeL = sdf.parse(nextDateS + " 09:00:00").getTime();
							
						}else {
							nextTimeL = myHistTick.getLastTime_trades() + 1000;   // lastTime的下一秒
							//lastTimeNextS =  sdf.format(new Date(lastTimeNextL));
						}
					}
					myHistTick.close();
				}
			}
			
			System.out.println("here11234");
			// pause and disconnect
			try {   
				Thread.sleep(1000 * 1);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			//========= pre-close actions ============
			for(int i = 0; i < rtBarHandlerArr.size(); i++) {
				MyIRealTimeBarHandler myRt = rtBarHandlerArr.get(i);
				myRt.fileWriter.close();
			}
			
			
			// ======== close =========
			myController.disconnect();
			if(myClient.isConnected()){
				System.out.println("Is connected!");
			}
			else{
				System.out.println("Not connected!");
			}
			System.out.println("========================== END ==========================");
			
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public void getRecentNDayTickData(Contract con, String endDate, int NDays, String filePath) {
		
	}
	
	//public String getNextTradeDate(String date, ArrayList<Calendar> allTradingDate)

}
