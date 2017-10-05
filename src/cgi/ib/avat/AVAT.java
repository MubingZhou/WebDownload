package cgi.ib.avat;

import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.ib.client.Contract;
import com.ib.controller.ApiController;

public class AVAT {
	// -------- main variables -------------
	public static ArrayList<Contract> conArr = new ArrayList<Contract>();
	public static ApiController myController;
	public static String AVAT_ROOT_PATH = "";
	public static ArrayList<MyITopMktDataHandler> topMktDataHandlerArr = new ArrayList<MyITopMktDataHandler>();
	public static int numOfTopMktDataStock = 0;
	
	// -------- auxiliar variables ----------
	public static Map<String, Map<Date,ArrayList<Double>>> avatHist = new HashMap<String, Map<Date,ArrayList<Double>>> ();
	public static Map<String, Double> avatPrevClose = new HashMap<String, Double> ();
	public static Map<String, String> avatIndustry = new HashMap<String, String> ();
	public static Map<String, ArrayList<String>> avatIndustry_byIndustry = new HashMap<String, ArrayList<String>> ();
	public static ArrayList<String> avatIndexMembers = new ArrayList<String> ();
	public static ArrayList<Date> avatTimePath = new ArrayList<Date> ();
	
	// --------- other variables ---------
	private static Logger logger = Logger.getLogger(AVAT.class.getName());
	public static String dateFormat = "yyyyMMdd HH:mm:ss";
	public static SimpleDateFormat sdf = new SimpleDateFormat (dateFormat); 
	public static String todayDate /*= new SimpleDateFormat ("yyyyMMdd").format(new Date())*/;
	public static ArrayList<Calendar> allTradingDate = new ArrayList<Calendar>();
	public static SimpleDateFormat sdf_100 = new SimpleDateFormat ("yyyyMMdd HH_mm_ss"); 
	private static String errMsgHead = "[AVAT - error] ";
	
	public static void setting(ApiController myController0, ArrayList<Contract> conArr0, String AVAT_ROOT_PATH0) {
		myController = myController0;
		conArr = (ArrayList<Contract>) conArr0.clone();
		AVAT_ROOT_PATH = AVAT_ROOT_PATH0;
		
		numOfTopMktDataStock = conArr.size();
	}
	
	public static void start() {
		if(myController == null) {
			logger.error(errMsgHead + "ApiController null!");
			return;
		}
		if(conArr == null || conArr.size() == 0) {
			logger.error(errMsgHead + "contract array null or size zero!");
			return;
		}
		if(AVAT_ROOT_PATH == null || AVAT_ROOT_PATH.length() == 0) {
			logger.error(errMsgHead + "AVAT_ROOT_PATH null!");
			return;
		}
		
		prepare();  // 在正式prepare之前，需要先更新昨天的auction数据和收盘数据
		logger.info("preparation done");
		
		//request();
		logger.info("request done");
		
		//output();
		logger.info("output done");
	}
	
	private static void prepare() {
		allTradingDate = utils.Utils.getAllTradingDate("D:\\stock data\\all trading date - hk.csv");
		
		// ------- avat - prepare historical avat --------
		AvatUtils.preparePrevCrossSectionalAvat2(conArr, todayDate, "yyyyMMdd");
		logger.info("prepare prev cross setional avat - done");
		try {
			Thread.sleep(1000 * 1000000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// ------- avat - historical avat ---------
		avatHist = AvatUtils.getPrevCrossSectionalAvat(conArr);
		logger.info("get prev cross setional avat - done");
		
		// ------- avat - yesterday close ---------
		avatPrevClose = AvatUtils.getPrevClose();
		logger.info("get prev close - done");
		
		// ------- avat - industry ---------
		ArrayList<Object> data = AvatUtils.getIndustry();
		avatIndustry = (Map<String, String>) data.get(0);
		avatIndustry_byIndustry = (Map<String, ArrayList<String>>) data.get(1);  // industry - stock list
		logger.info("get industry - done");
		
		// ------ avat index members ----------
		avatIndexMembers = AvatUtils.getIndexMembers();
		logger.info("get index memebers - done");
		
		// ------- avat time path by 1min ---------
		avatTimePath = AvatUtils.getTimePath();
		logger.info("get time path - done");
		
		// -------- avat get today's auction --------
		//Map<String, Double> todayAuction = AvatUtils.getTodayAuction();
	}
	
	private static void request() {
		for(int i = 0; i < numOfTopMktDataStock; i++) {
			Contract con = conArr.get(i);
			MyITopMktDataHandler myTop = new MyITopMktDataHandler(con.symbol(), AVAT_ROOT_PATH, todayDate);
			//myTop.fileWriterMainPath = AVAT_ROOT_PATH + "real time data\\";
			topMktDataHandlerArr.add(myTop);
			myController.reqTopMktData(con, "233,375", false, false, myTop);
			/*
			 * Generic tick type:
			 * 233 - RT volume
			 * 375 - RT trade volume
			 */
			
		}
	}
	
	private static void output() {
		try {
			String avatRecordPath = AVAT_ROOT_PATH + "avat record\\" + todayDate + "\\";
			
			Date now = new Date();
			//Map<String,ArrayList<Double>> avatRatioNow = new HashMap();
			ArrayList<AvatRecordSingleStock> avatRecord = new ArrayList<AvatRecordSingleStock>();
			ArrayList<AvatRecordSingleStock> lastAvatRecord = new ArrayList<AvatRecordSingleStock>();
			Map<String, Integer> lastRankingData = new HashMap();
			
			boolean isFirst = true;
			while(now.before(avatTimePath.get(avatTimePath.size() - 1))) {
				// --------- 判断时间 -----------
				logger.info("now = " + sdf.format(now));
				if(now.before(sdf.parse(todayDate + " 09:30:00"))) {
					logger.info("Market not open!");
					Thread.sleep(1000 * 60);
					now = new Date();
				}
				
				//---------- 正式开始 -----------
				logger.info("Generating avat!");
				
				avatRecord = new ArrayList<AvatRecordSingleStock>();
					
				logger.debug("-- topMktDataHandlerArr.size=" + topMktDataHandlerArr.size());
				for(int i = 0; i < topMktDataHandlerArr.size(); i++) { // 每一个handler负责一只股票
					MyITopMktDataHandler myTop = topMktDataHandlerArr.get(i);
					
					Double trdRtVolume = myTop.lastestTrdRTVolume;
					String stock = myTop.stockCode;
					Double price = myTop.lastestPrice;
					Double trdRtTurnover = myTop.lastestTrdRTTurnover;
					logger.debug("------- stock = " + stock);
					
					// find the nearest date
					Map<Date,ArrayList<Double>> avatHist_stock = avatHist.get(stock);
					if(avatHist_stock == null) {   // no such stock
						logger.info("[real time data] stock=" + stock + " no such stock!");
						continue;
					}
					if(trdRtVolume == 0.0) {
						logger.info("[real time data] stock=" + stock + " zero volume!");
						continue;
					}
					if(price == 0.0) {
						logger.info("[real time data] stock=" + stock + " zero price!");
						continue;
					}
					
					// ------ find the time benchmark -------
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
					if(avatHist_nearData == null) {  // 虽然这种情况不太可能出现，但是也要做好应对措施
						logger.info("[real time data] stock=" + stock + " avatHist_nearData null!");
						continue;
					}
					
					// get historical avat data
					Double avat5D = avatHist_nearData.get(0);
					Double avat20D = avatHist_nearData.get(1);
					
					// get current avat
					Double ratio5D = trdRtVolume / avat5D;
					Double ratio20D = trdRtVolume / avat20D;
					
					ArrayList<Double> temp = new ArrayList<Double>();
					temp.add(ratio5D);
					temp.add(ratio20D);
					
					//avatRatioNow.put(stock, temp);
					
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
					at.turnover = trdRtTurnover;
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
				
				// place orders
				
				
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
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private static void placeOrder(ArrayList<AvatRecordSingleStock> avatRecord) {
		
	}

}
