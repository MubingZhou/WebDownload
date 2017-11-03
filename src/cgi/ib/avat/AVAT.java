package cgi.ib.avat;

import java.awt.Toolkit;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.apache.log4j.Logger;

import com.ib.client.Contract;
import com.ib.client.Execution;
import com.ib.client.ExecutionFilter;
import com.ib.client.Order;
import com.ib.client.OrderStatus;
import com.ib.client.OrderType;
import com.ib.client.Types.Action;
import com.ib.controller.ApiController;

import utils.PlayWAV;
import utils.XMLUtil;

public class AVAT {
	// -------- main variables -------------
	public static ArrayList<Contract> conArr = new ArrayList<Contract>();
	public static Map<String, Contract> conMap = new HashMap<String, Contract>();
	public static MyAPIController myController;
	public static String AVAT_ROOT_PATH = "";
	public static ArrayList<MyITopMktDataHandler> topMktDataHandlerArr = new ArrayList<MyITopMktDataHandler>();
	public static int numOfTopMktDataStock = 0;
	public static double bilateralTrdCost = 0.003;
	
	// -------- auxiliar variables ----------
	public static Map<String, Map<Date,ArrayList<Double>>> avatHist = new HashMap<String, Map<Date,ArrayList<Double>>> ();
	public static Map<String, Double> avatPrevClose = new HashMap<String, Double> ();
	public static Map<String, String> avatIndustry = new HashMap<String, String> ();
	public static Map<String, ArrayList<String>> avatIndustry_byIndustry = new HashMap<String, ArrayList<String>> ();
	public static ArrayList<String> avatIndexMembers = new ArrayList<String> ();
	public static ArrayList<Date> avatTimePath = new ArrayList<Date> ();
	public static Map<String, Double> avatLotSize = new HashMap<String, Double> ();
	public static Map<String, Double> prevVolume = new HashMap<String, Double>(); // previous day's volume for every stock
	
	// --------- other variables ---------
	public static String TRADING_DATE_PATH = "D:\\stock data\\all trading date - hk.csv";
	private static Logger logger = Logger.getLogger(AVAT.class.getName());
	public static String dateFormat = "yyyyMMdd HH:mm:ss";
	public static SimpleDateFormat sdf = new SimpleDateFormat (dateFormat); 
	public static String todayDate /*= new SimpleDateFormat ("yyyyMMdd").format(new Date())*/;
	public static ArrayList<Calendar> allTradingDate = new ArrayList<Calendar>();
	public static SimpleDateFormat sdf_100 = new SimpleDateFormat ("yyyyMMdd HH_mm_ss"); 
	private static String errMsgHead = "[AVAT - error] ";
	
	private static Map<Double, String> msg_eligibleStocksMap = new HashMap<Double, String>();  // 用来存储每次运行完之后符合要求的股票，用于在对话框中显示，key是avat，value是stock code
	private static String alertToShow = "";   // 将要显示在弹出框的内容
	private static JFrame avatDisplayFrame = new JFrame();   // 
	private static String buyOrdersToShow = "";
	private static JFrame buyOrdersFrame = new JFrame();    // show buy orders
	
	// -------- orders ----------
	//private static ArrayList<String> boughtRecords = new ArrayList<String>(); // stocks that have been bought 
	public static boolean isStartOrders = true; // 是否开始监视order并落单
	private static int isLotSizeMapToUpdate = 0;  // lot size map是否需要升级
	private static Map<String, Map<Integer, HoldingRecord>> holdingRecords = new HashMap<String, Map<Integer, HoldingRecord>>();  // String是stock code, Integer是order id（只存buy order）
	private static String holdingRecordsPath = "";  // 存储 holdingRecords 的路径
	private static String orderWriterPath ;
	private static FileWriter orderWriter;
	private static boolean transmitToIB = true;  // 是否transmit 到 IB server
	
	
	public static void setting(MyAPIController myController0, ArrayList<Contract> conArr0, String AVAT_ROOT_PATH0) {
		myController = myController0;
		conArr = (ArrayList<Contract>) conArr0.clone();
		AVAT_ROOT_PATH = AVAT_ROOT_PATH0;
		
		numOfTopMktDataStock = conArr.size();
		for(Contract con : conArr) {  // construct conMap
			String stock = con.symbol();
			conMap.put(stock, con);
		}
		
		// 创建文件…
		String avatRecordRootPath = AVAT_ROOT_PATH + "\\avat record\\";
		String avatParaRootPath = AVAT_ROOT_PATH + "\\avat para\\";
		String avatRtDataRootPath = AVAT_ROOT_PATH + "\\realtime data\\";
		String avatOrdersRootPath = AVAT_ROOT_PATH + "\\orders\\";
		File f1 = new File(avatRecordRootPath);  if(!f1.exists()) f1.mkdirs();
		File f2 = new File(avatParaRootPath ); if(!f2.exists()) f2.mkdirs();
		File f3 = new File(avatRtDataRootPath); if(!f3.exists()) f3.mkdirs();
		File f4 = new File(avatOrdersRootPath); if(!f4.exists()) f4.mkdirs();
		
		String avatRecordPath = avatRecordRootPath + todayDate + "\\";
		String avatParaPath = avatParaRootPath + todayDate + "\\";
		String avatRtDataPath = avatRtDataRootPath + todayDate + "\\";
		String avatOrdersPath = avatOrdersRootPath + todayDate + "\\";
		File f11 = new File(avatRecordPath);  if(!f11.exists()) f11.mkdirs();
		File f12 = new File(avatParaPath ); if(!f12.exists()) f12.mkdirs();
		File f13 = new File(avatRtDataPath); if(!f13.exists()) f13.mkdirs();
		File f14 = new File(avatOrdersPath); if(!f14.exists()) f14.mkdirs();
		
		
		holdingRecordsPath = AVAT_ROOT_PATH + "orders\\" + todayDate + "\\holdingRecords.csv";
	}
	
	public static void start() {
		try {
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
			
			requestForRtData();
			logger.info("request done");
			
			if(isStartOrders) {
				// 有关order的一些处理
				Thread orderMonitorThd = new Thread(new Runnable(){
					   public void run(){
						   //ordersMonitor();
						   executionMonitor();
					   }
				});
				orderMonitorThd.start();
				
				// 初始化记录orders的filewriter
				orderWriterPath = AVAT_ROOT_PATH + "orders\\" + todayDate + "\\";
				File f = new File(orderWriterPath);
				if(!f.exists())
					f.mkdir();
				orderWriter = new FileWriter(orderWriterPath + "orders.csv",true); // append
			}
			
			// get holdingRecords
			File f = new File(holdingRecordsPath);
			if(f.exists()) {
				try {
					//holdingRecords = (Map<String, Map<Integer, HoldingRecord>>) utils.Utils.readObject(holdingRecordsPath);
					recoverTradingRecord();
				}catch(Exception e) {
					logger.error("holdingRecords cannot be read!");
				}
			}
				
			
			scanForAvat();
			logger.info("output done");
			
			
			// before end actions
			if(isStartOrders)
				orderWriter.close();
			
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private static void prepare() {
		try {
			allTradingDate = utils.Utils.getAllTradingDate(TRADING_DATE_PATH);
			
			// ------- avat - prepare historical avat --------
			AvatUtils.preparePrevCrossSectionalAvat2(conArr, todayDate, "yyyyMMdd");
			logger.info("prepare prev cross setional avat - done");
			
			// ------- avat - historical avat ---------
			avatHist = AvatUtils.getPrevCrossSectionalAvat(conArr);
			logger.info("get prev cross setional avat - done");
			
				//Thread.sleep(1000 * 10000000);
			
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
			
			// ------ avat - get previous day's volume --------
			prevVolume = AvatUtils.getPreviousVolume(conArr);   // 这个volume不包好unreportable的volume
			
			// ------ avat lot size --------
			avatLotSize = AvatUtils.getLotSize();
			logger.info("get lot size - done");
			
			// ------- 设置弹出窗口的容器 ----------
		   avatDisplayFrame.setLocation(0,0);
		   avatDisplayFrame.setSize(300, 700);
		   avatDisplayFrame.setVisible(true);
		   
		   buyOrdersFrame.setLocation(0,300);
		   buyOrdersFrame.setSize(300, 700);
		   buyOrdersFrame.setVisible(true);
	   
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * 请求实时数据
	 */
	private static void requestForRtData() {
		for(int i = 0; i < numOfTopMktDataStock; i++) {
			Contract con = conArr.get(i);
			MyITopMktDataHandler myTop = new MyITopMktDataHandler(con.symbol(), AVAT_ROOT_PATH, todayDate);
			//myTop.fileWriterMainPath = AVAT_ROOT_PATH + "real time data\\";
			topMktDataHandlerArr.add(myTop);
			myController.reqTopMktData(con, "233,375", false, false, myTop);
			/*
			 * Generic tick type:
			 * 233 - RT volume
			 * 375 - RT trade volumes
			 */
			logger.info("Subscribe top market data. Stock=" + con.symbol() + " i=" + i);
		}
	}
	
	private static void scanForAvat() {
		try {
			String avatRecordPath = AVAT_ROOT_PATH + "avat record\\" + todayDate + "\\";
			File f_r = new File(avatRecordPath);
			if(!f_r.exists())
				f_r.mkdir();
			String avatRecordXMLPath = AVAT_ROOT_PATH + "avat record\\" + todayDate + "\\overview.xml";
			
			Date now = new Date();
			//Map<String,ArrayList<Double>> avatRatioNow = new HashMap();
			ArrayList<AvatRecordSingleStock> avatRecord = new ArrayList<AvatRecordSingleStock>();
			ArrayList<AvatRecordSingleStock> lastAvatRecord = new ArrayList<AvatRecordSingleStock>();
			Map<String, Integer> lastRankingData = new HashMap();
			
			boolean isFirst = true;
			long scanPeriod = 1000 * 60;  //每次隔多久扫描一次
			while(now.before(avatTimePath.get(avatTimePath.size() - 1))) {
				// --------- 判断时间 -----------
				logger.info("now = " + sdf.format(now));
				if(now.before(sdf.parse(todayDate + " 09:30:00"))) {
					logger.info("Market not open!");
					Thread.sleep(scanPeriod);
					now = new Date();
					continue;
				}
				
				//---------- 正式开始 -----------
				logger.info("Generating avat!");
				
				avatRecord = new ArrayList<AvatRecordSingleStock>();
				
				ArrayList<String> eligibleStocks = new ArrayList<String>(); // 看avat是否符合要求
				ArrayList<Double> eligibleStocksValue = new ArrayList<Double>(); 
				msg_eligibleStocksMap.clear();
				
				logger.debug("-- topMktDataHandlerArr.size=" + topMktDataHandlerArr.size());
				for(int i = 0; i < topMktDataHandlerArr.size(); i++) { // 每一个handler负责一只股票
					MyITopMktDataHandler myTop = topMktDataHandlerArr.get(i);
					
					Double trdRtVolume = myTop.latestTrdRTVolume;
					String stock = myTop.stockCode;
					Double price = myTop.latestPrice;
					Double trdRtTurnover = myTop.latestTrdRTTurnover;
					Double latestBestBid = myTop.latestBestBid;
					Double latestBestAsk = myTop.latestBestAsk;
					
					logger.trace("------- stock = " + stock);
					
					// find the nearest date
					Map<Date,ArrayList<Double>> avatHist_stock = avatHist.get(stock);
					if(avatHist_stock == null) {   // no such stock
						logger.debug("[real time data] stock=" + stock + " no such stock!");
						continue;
					}
					if(trdRtVolume == 0.0) {
						logger.debug("[real time data] stock=" + stock + " zero volume!");
						continue;
					}
					if(price == 0.0) {
						logger.debug("[real time data] stock=" + stock + " zero price!");
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
					
					if(ratio5D >= 3.0) {
						//eligibleStocks.add(stock);
						//msg_eligibleStocksMap.put(ratio5D, stock);
					}
					
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
					
					AvatRecordSingleStock at = new AvatRecordSingleStock(now.getTime(), stock, myTop.contract, price, prevCloseChgPct, ratio5D, ratio20D, industry);
					at.turnover = trdRtTurnover;
					at.latestBestAsk = latestBestAsk;
					at.latestBestBid = latestBestBid;
					at.prevVolume = prevVolume.get(stock);
					at.volume = trdRtVolume;
					
					avatRecord.add(at);
					
					logger.trace("------- next ");
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
				
				// ----------- place orders --------
				if(isStartOrders)
					scanForOrders(avatRecord,now);
				
				// ----------- 弹出对话框 ---------------
				BufferedReader bf_al = utils.Utils.readFile_returnBufferedReader(AVAT_ROOT_PATH + "avat para\\isShowAlert.txt");
				String line0 = bf_al.readLine();
				bf_al.close();
				if(line0.equals("1")) {  // 当avat ratio5D大于3的时候输出
					String title = "AVAT results";
					String nowTimeStr = sdf.format(now);
					String blank = "    ";
					
					String avatLargerThan5 = "";
					String avatLargerThan3 = "";
					String rankDiffLargerThan20 = "";  // 排名上升超过20
					String rankDiffLargerThan10 = "";  // 排名上升超过10
					
					DecimalFormat    df   = new DecimalFormat("######0.00");   
					for(AvatRecordSingleStock rec: avatRecord) {
						Double ratio5D = rec.avatRatio5D;
						String stockCode = rec.stockCode;
						int rankDiff = rec.rankDiff;
						String industry = rec.industry;
						
						if(ratio5D >= 5) {
							avatLargerThan5 += blank + stockCode + " : " + df.format(ratio5D) + " - " + industry + "\n";
						}
						if(ratio5D >=3 && ratio5D < 5) {
							avatLargerThan3 += blank + stockCode + " : " + df.format(ratio5D)  + " - " + industry + "\n";
						}
						if(rankDiff <= -20) { // 上升超过20名
							rankDiffLargerThan20 = blank + stockCode + " rank diff=" + (-rankDiff) + " avat=" + df.format(ratio5D)  + " - " + industry +  "\n";
						}
						if(rankDiff <= -10 && rankDiff > -20) { // 上升超过10名
							rankDiffLargerThan10 = blank + stockCode + " rank diff=" + (-rankDiff) + " avat=" + df.format(ratio5D)  + " - " + industry +  "\n";
						}
					}
					
					alertToShow = nowTimeStr + "\n"
								+ "AVAT >= 5\n" + avatLargerThan5
								+ "AVAT >= 3 && AVAT < 5\n" + avatLargerThan3
								+ "Ranking increase > 20\n" + rankDiffLargerThan20
								+ "Ranking increase > 10\n" + rankDiffLargerThan10;
					
					Thread t = new Thread(new Runnable(){
						   public void run(){
							   JOptionPane.showMessageDialog(avatDisplayFrame, alertToShow, "AVAT Results", JOptionPane.PLAIN_MESSAGE);
						        
						   }
						});
						t.start();
				}
				
				
				// -industry table
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
				// ---------- 存储avat record --------
				/*
				Map<String, AvatRecordSingleStock> recordMap = new HashMap<String, AvatRecordSingleStock>();
				for(AvatRecordSingleStock rec : avatRecord) {
					recordMap.put(rec.stockCode, rec);
				}
				XMLUtil.convertToXml(recordMap, avatRecordXMLPath);
				*/
				
				// -------- update 一下 lot size map --------
				if(isLotSizeMapToUpdate == 1) {
					FileWriter f = new FileWriter(AVAT_ROOT_PATH + "avat para\\lot size.csv");
					for(String key : avatLotSize.keySet()) {
						f.write(key + "," + avatLotSize.get(key) + "\n");
					}
					f.close();
				}
				isLotSizeMapToUpdate = 0;
				
				Thread.sleep(scanPeriod); // wait for 1 min
				lastAvatRecord = (ArrayList<AvatRecordSingleStock>) avatRecord.clone();
				isFirst = false;
				now = new Date();
			}  // end of while
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private static void scanForOrders(ArrayList<AvatRecordSingleStock> avatRecord, Date now) {
		try {
			logger.info("---------- scanForOrders ---------");
			
			String errMsgHead  = "[trading strategy] ";
			Double fixedBuyAmount = 500000.0;  // fix buying amount for each stock, HKD
			Double buyPriceDiscount = 1.0;  // 为了testing，不让order被fill，可以将buyprice设小点
			
			// ------- 与买入有关的variables ----------
			String buyStartTimeStr = todayDate + " 09:30:00";  // 这个时间点之后才进行一切buy
			Date buyStartTime = sdf.parse(buyStartTimeStr);

			//String buyEndTimeStr = todayDate + " 11:00:00";
			String buyEndTimeStr = todayDate + " 16:00:00";    // 这个时间点之后才停止一切buy

			Date buyEndTime = sdf.parse(buyEndTimeStr);
			String volumeCondEndTimeStr = todayDate + " 11:00:00";    // 这个时间点之后停止对于volume condition的判断 （buyCond2_3）
			Date volumeCondEndTime = sdf.parse(volumeCondEndTimeStr);
			
			double avatThld5D = 3.0;  // avat threshold
			double avatThld20D = 2.0;  // avat threshold
			double turnoverThld = 8000000.0;
			
			// ------- 与卖出有关的variables ----------
			String sellThldTimeStr = todayDate + " 16:10:00";
			Date sellThldTime = sdf.parse(sellThldTimeStr);
			Set<String> holdingStocks = holdingRecords.keySet();
			
			// --------- 浏览各个AvatRecordSingleStock ----------- 
			for(AvatRecordSingleStock singleRec : avatRecord) {
				/*
				 * ------- 先看是否有买入信号 ---------
				 * 买入条件：
				 * 		1. 时间是11:00之前；并且
				 * 		(2.1 当前的avat5D ratio超过2，并且股价上涨超过1.5%；或者
				 * 		2.2 avat20D ratio超过1，并且股价上涨超过1.5%；或者
				 * 		2.3. 当时的volume超过了昨天全天的volume)
				 * 		3. turnover 大于一个threshold 
				 * 		4. 已经买过的股票如果三注码都加满，则不再买入；同一注码重复出现，不再重复下注
				 * 
				 * 	其中2.1-2.3每满足一个条件就加一注码
				 */
			
				Date thisTime = new Date(singleRec.timeStamp);
				if(thisTime.after(buyStartTime) && thisTime.before(buyEndTime)) {  // 只在合适的时间段内判读是否出现买入信号
					
					boolean isBuy = false;
					//boolean isShort = false;
					
					int buyCond2_1 = 0;
					int buyCond2_2 = 0;
					int buyCond2_3 = 0;
					int buyCond3 = 0;
					//int buyCond4 = 0; 
					
					double priceChg = singleRec.currentPrice / avatPrevClose.get(singleRec.stockCode) - 1;
					
					if(singleRec.avatRatio5D > avatThld5D) {   // 如果股价变动是负的，则short
						if(priceChg >= 0.015) {
							buyCond2_1 = 1;
							isBuy = true;
						}
							
					}
						
					if(singleRec.avatRatio20D > avatThld20D ) {
						if(priceChg >= 0.015) {
							buyCond2_2 = 1;
							isBuy = true;
						}
						
					}
					if(singleRec.volume >= singleRec.prevVolume && thisTime.before(volumeCondEndTime) && priceChg > 0)
						buyCond2_3 = 1;
					if(singleRec.turnover >= turnoverThld )
						buyCond3 = 1;
					
					//isShort =  false; // 先不考虑short
					//buyCond2_2 = 0;
					//buyCond2_3 = 0;  // 暂时，先不考虑这两个factor的影响
					
					Double toBuyAmt = 0.0;
					int[] buyTracer = {0,0,0};  //看看到底是因为哪个信号使得要买入
					
					Map<Integer, HoldingRecord> thisHoldingMap = holdingRecords.get(singleRec.stockCode);
					// ---------- 是否出现buy signal------------
					if((buyCond2_1 == 1 || buyCond2_2 == 1 || buyCond2_3 == 1) && priceChg > 0) {
						int thisHoldingBuyCond2_1 = 0;
						int thisHoldingBuyCond2_2 = 0;
						int thisHoldingBuyCond2_3 = 0;
						if(thisHoldingMap != null) {
							for(HoldingRecord hld : thisHoldingMap.values()) {
								thisHoldingBuyCond2_1 = hld.buyCond2_1 == 1? 1: thisHoldingBuyCond2_1;
								thisHoldingBuyCond2_2 = hld.buyCond2_2 == 1? 1: thisHoldingBuyCond2_2;
								thisHoldingBuyCond2_3 = hld.buyCond2_3 == 1? 1: thisHoldingBuyCond2_3;
							}
						}
						
						HoldingRecord newHld = null;
						if(buyCond2_1 == 1 && thisHoldingBuyCond2_1 == 0) {  // 会在下面更新 thisHoldingRec.buyCond2_1
							toBuyAmt += fixedBuyAmount;
							buyTracer[0] = 1;
						}
						if(buyCond2_2 == 1 && thisHoldingBuyCond2_2 == 0) {
							toBuyAmt += fixedBuyAmount;
							buyTracer[1] = 1;
						}
						if(buyCond2_3 == 1 && thisHoldingBuyCond2_3 == 0) {
							toBuyAmt += fixedBuyAmount;
							buyTracer[2] = 1;
						}
					}
					
					// ----------- 处理 buy signal -------------
					if(buyCond3 == 1 && toBuyAmt > 0) {  
						//logger.info("[scan for orders] found stock! stock=" + );
						// 新开一个线程来处理似乎不妥当，因为每个order的id必须大于之前order的id，所以如果很多线程并行的话，不能保证先提交给ib的order的id是最小的
						
						String stockCode = singleRec.stockCode;
						
						Contract con = conMap.get(stockCode);
						Double lotSize = avatLotSize.get(stockCode);
						
						// ----------  其中一半的qty放在best bid上，另一半放在best offer上 --------------
						Order order1 = new Order();
						order1.action(Action.BUY);
						order1.orderType(OrderType.LMT);
						
						Double buyPrice1 = singleRec.latestBestBid;
						//buyPrice1 = AvatUtils.getCorrectPrice_up(buyPrice1 * buyPriceDiscount);
						order1.lmtPrice(buyPrice1);  // 以best bid作为买入价
						
						Double orderQty1 = lotSize * (int)(toBuyAmt/2 / lotSize / buyPrice1) ;
						order1.totalQuantity(orderQty1);
						order1.transmit(transmitToIB);  // false - 只在api平台有这个order
						
						Order order2 = new Order();
						order1.action(Action.BUY);
						order2.orderType(OrderType.LMT);
						
						Double buyPrice2 = singleRec.latestBestAsk;
						//buyPrice1 = AvatUtils.getCorrectPrice_up(buyPrice1 * buyPriceDiscount);
						order2.lmtPrice(buyPrice2);  // 以best bid作为买入价
						
						Double orderQty2 = lotSize * (int)(toBuyAmt/2 / lotSize / buyPrice2) ;
						order2.totalQuantity(orderQty2);
						order2.transmit(transmitToIB);  // false - 只在api平台有这个order
					

						// --------- submit orders ---------
						MyIOrderHandler myOrderH1 = new MyIOrderHandler (con, order1); 
						myOrderH1.isTransmit = transmitToIB;
						myController.placeOrModifyOrder(con, order1, myOrderH1);
						MyIOrderHandler myOrderH2 = new MyIOrderHandler (con, order2); 
						myOrderH2.isTransmit = transmitToIB;
						myController.placeOrModifyOrder(con, order2, myOrderH2);
						
						while(myOrderH1.getOrderId() == -1) {Thread.sleep(5);}
						while(myOrderH2.getOrderId() == -1) {Thread.sleep(5);}
						
						HoldingRecord hld1 = new HoldingRecord(myOrderH1, now.getTime());
						HoldingRecord hld2 = new HoldingRecord(myOrderH2, now.getTime());
						
						if(thisHoldingMap == null)
							thisHoldingMap = new HashMap<Integer, HoldingRecord>();
						
						String buyReason = "";
						if(buyTracer[0] == 1) {
							hld1.buyCond2_1 = 1;
							hld2.buyCond2_1 = 1;
							buyReason += "avat5D;";
						}
						if(buyTracer[1] == 1) {
							hld1.buyCond2_2 = 1;
							hld2.buyCond2_2 = 1;
							buyReason += "avat20D;";
						}
						if(buyTracer[2] == 1) {
							hld1.buyCond2_3 = 1;
							hld2.buyCond2_3 = 1;
							buyReason += "volume;";
						}
						hld1.buyReason = buyReason ;
						hld2.buyReason = buyReason ;
						
						thisHoldingMap.put(myOrderH1.getOrderId(), hld1);
						thisHoldingMap.put(myOrderH2.getOrderId(), hld2);
						holdingRecords.put(stockCode, thisHoldingMap);
						
						
						
						orderWriter.write(hld1.toString() + "\n");
						orderWriter.write(hld2.toString() + "\n");
						orderWriter.flush();
						
						logger.debug("    stock=" + stockCode + " BUY , orderId=" + myOrderH1.getOrderId()+ "&" + myOrderH2.getOrderId());
						buyOrdersToShow += "time=" + sdf_100.format(now) + " stock=" + stockCode + "\n";
						
						Thread placingOrderAlert = new Thread(new Runnable(){
							   public void run(){
								   try {
										//utils.Utils.saveObject(holdingRecords, holdingRecordsPath);  // 运行速度比较慢，新开个thread运行比较好
									   PlayWAV.play("hahaha.wav");
									   JOptionPane.showMessageDialog(buyOrdersFrame, buyOrdersToShow, "Buy Orders", JOptionPane.PLAIN_MESSAGE);
								        
										//logger.info("            logging holding records done!");
									}catch(Exception e) {
										logger.error("           Sound alert failed!");
									}
							   }
							});
						placingOrderAlert.start();
					}
				}  // 买入信号的if结束
				/*
				 * --------------- 卖出信号 ---------------
				 * 卖出条件：
				 * 		1. 获利超过3%，以最优bid价卖出 （止盈）,这个可以在monitor execution时，事先place好sell order，即buy order成交了多少，就将这些filled 的qty挂限价单卖出去
				 * 		2. 持股到当日15：00仍未卖出，以买入成本价（需要考虑交易成本）卖出
				 * 		3. 持股到当日15：50仍未卖出，以市价卖出
				 */
			}
			
			Thread t = new Thread(new Runnable(){
				   public void run(){
					   try {
							//utils.Utils.saveObject(holdingRecords, holdingRecordsPath);  // 运行速度比较慢，新开个thread运行比较好
						   saveHoldingRecords();
							//logger.info("            logging holding records done!");
						}catch(Exception e) {
							logger.error("           Can't log holding records!");
						}
				   }
				});
			t.start();
			
			logger.info("---------- scanForOrders ENDS ---------");
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * 监视所有Open orders，然后update holdingRecords
	 */
	/*
	private static void ordersMonitor() {
		try {
			int orderStatusInd = 0;
			String liveOrderRecPath = AVAT_ROOT_PATH + "orders\\" + todayDate + "\\live order records.csv";
			
			
			while(true) {
				MyILiveOrderHandler myLiveOrder = new MyILiveOrderHandler(liveOrderRecPath);
				myController.reqLiveOrders(myLiveOrder);
				
				Date thisNow = new Date();
				String cancelOrderStartStr = todayDate + " 14:00:00";
				Date cancelOrderStartDate = sdf.parse(cancelOrderStartStr);
				String dayEndStr = todayDate + " 16:10:00";
				Date dayEndDate = sdf.parse(dayEndStr);
				
				if(thisNow.after(dayEndDate))
					break;
				
				
				logger.info("[Start scanning live orders...] " + sdf.format(new Date()));
				
				for(int i = orderStatusInd; i < myLiveOrder.openOrderArr.size(); i++) {
					ArrayList<Object> thisOrderContract = myLiveOrder.openOrderArr.get(i);
					ArrayList<Object> thisOrderStatus = myLiveOrder.orderStatusArr.get(i);
					
					OrderStatus status = (OrderStatus) thisOrderStatus.get(1);
					Double filled = (Double) thisOrderStatus.get(2);
					Double remaining = (Double) thisOrderStatus.get(3);
					Double avgFillPrice  = (Double) thisOrderStatus.get(4);
					Double lastFillPrice  = (Double) thisOrderStatus.get(7);
					
					Order order = (Order) thisOrderContract.get(1);
					Double orderId = (double) order.orderId();
					Action action = order.action();
					
					Contract contract = (Contract) thisOrderContract.get(0);
					String stockCode = contract.symbol();
					
					logger.info("      [live order] orderId=" + orderId + " stock=" + stockCode + " remaining=" + remaining + " action=" + action.toString());
					
					// -------  如果时间在11点之后，所有的open buy order都要cancel ---------
					if(thisNow.after(cancelOrderStartDate)) { 
						if(action.equals(Action.BUY) && remaining > 0.0) {  // open buy orders, need to cancel
							myController.cancelOrder(order.orderId());
							logger.info("      [live order] cancel order! OrderId=" + order.orderId());
						}
					}  // 11点判断结束
					
					// ------ update holdingRecords ----
					HoldingRecord thisHldRecord = holdingRecords.get(stockCode);
				
					if(status.equals(OrderStatus.Filled)) {  //只看filled的status
						Double oldQty = thisHldRecord.filledQty;
						Double oldAvgFillPrice = thisHldRecord.avgFillPrice;
						Double oldLastFillPrice = thisHldRecord.lastFillPrice;
						
						thisHldRecord.filledQty += filled;
						Double newAvgFillPrice  = (oldAvgFillPrice * oldQty + filled * avgFillPrice)/thisHldRecord.filledQty;
						thisHldRecord.avgFillPrice = newAvgFillPrice;
						thisHldRecord.lastFillPrice = lastFillPrice;
						
						
						logger.info("      [live order] orderId=" + orderId + " new hlding rec: filledQty=" + thisHldRecord.filledQty 
								+ " avgFillPrice=" + newAvgFillPrice + " lastFillPrice=" + lastFillPrice);
						holdingRecords.put(stockCode, thisHldRecord);
					}
					
					orderStatusInd++;
				}	
				
				myController.removeLiveOrderHandler(myLiveOrder);
				Thread.sleep(1000 * 5);  // wait for 5 sec
			} // end of while
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	*/
	
	public static void executionMonitor() {
		try {
			Double stopProfitLevel1= 0.015;
			Double stopProfitLevel2= 0.03;
			long waitingSec = 30;
			
			long lastRequestTime = 0;
			//lastRequestTime=sdf.parse("20171018 09:00:00").getTime();
			lastRequestTime = new Date().getTime() - 1000 * waitingSec;
			
			String executionsRecPath = AVAT_ROOT_PATH + "orders\\" + todayDate + "\\executions records.csv";
			
			String cancelOrderStartStr = todayDate + " 11:00:00";
			Date cancelOrderStartDate = sdf.parse(cancelOrderStartStr);
			String dayEndStr = todayDate + " 23:10:00";
			Date dayEndDate = sdf.parse(dayEndStr);
			
			MyITradeReportHandler myTradeReportHandler = new MyITradeReportHandler(executionsRecPath);
			logger.info("getting into execution monitor....");
			while(true) {
				// ---------- MyITradeReportHandler的一些setting -----------
				ExecutionFilter filter = new ExecutionFilter();
				filter.secType("STK");
				if(lastRequestTime != 0)
					filter.time(sdf.format(new Date(lastRequestTime)));
				logger.info("last time = " + sdf.format(new Date(lastRequestTime)));
				lastRequestTime = new Date().getTime();
				myTradeReportHandler.initialize();
				myTradeReportHandler.isCalledByMonitor = 1;
				myController.reqExecutions(filter, myTradeReportHandler);
				
				Date thisNow = new Date();
				
				
				if(thisNow.after(dayEndDate))
					break;
				
				logger.info("check if received data");
				int count = 0;
				while(true) {
					if(myTradeReportHandler.isEnd == 1) {
						break;
					}
					count ++;
					if(count >= 500)
						break;
					Thread.sleep(10);
				}
				if(count >= 500) {
					logger.info("Execution Monitor, no executions....");
					Thread.sleep(waitingSec * 1000);
					continue;
				}
					
				
				Map<String, Map<Double, Double>> executionSummary = new HashMap ();  // String - stock code; 1st Double - buy order price; 2nd Double - cumulative filled qty
				// ---------- 读取MyITradeReportHandler返回的数据 ---------------
				ArrayList<ArrayList<Object>> tradeReportArr = myTradeReportHandler.tradeReportArr;
				logger.info("[Start scanning execution details...] " + sdf.format(new Date()) + " size=" + tradeReportArr.size());
				for(ArrayList<Object> tradeReport : tradeReportArr ) {
					Contract contract = (Contract) tradeReport.get(1);
					Execution execution = (Execution) tradeReport.get(2);
					
					String stockCode = contract.symbol();
					Double filled = execution.shares();
					Double avgFillPrice = execution.avgPrice(); // not include commission
					String executionId = execution.execId();
					int orderId = execution.orderId();
					
					// ------ update holdingRecords ----
					Map<Integer, HoldingRecord> thisHldRecordMap = holdingRecords.get(stockCode);
					if(thisHldRecordMap == null)  
						continue;
					
					HoldingRecord thisHldRecord = thisHldRecordMap.get(orderId);
					if(thisHldRecord == null)
						continue;
					
					Double oldQty = thisHldRecord.filledQty;
					Double oldAvgFillPrice = thisHldRecord.avgFillPrice;
					//Double oldLastFillPrice = thisHldRecord.lastFillPrice;
					ArrayList<String> executionIdArr = thisHldRecord.executionIdArr;
					
					if(executionIdArr.contains(executionId)) // 不知道为什么，server有时候会重复发来之前的execution
						continue;
					
					
					// update一下record
					thisHldRecord.executionIdArr.add(executionId);
					thisHldRecord.filledQty += filled;
					Double newAvgFillPrice  = (oldAvgFillPrice * oldQty + filled * avgFillPrice)/thisHldRecord.filledQty;
					thisHldRecord.avgFillPrice = newAvgFillPrice;
					//thisHldRecord.lastFillPrice = lastFillPrice;
					
					
					logger.info("      [execution details] new hlding rec: stock=" + stockCode + " filledQty=" + thisHldRecord.filledQty 
							+ " avgFillPrice=" + newAvgFillPrice );
					thisHldRecordMap.put(orderId, thisHldRecord);
					holdingRecords.put(stockCode, thisHldRecordMap);
					
					// 更新这次的 execution情况
					Map<Double, Double> thisExeSum = executionSummary.get(stockCode);
					if(thisExeSum == null)
						thisExeSum = new HashMap<Double, Double>();
					Double cumFilledQty = thisExeSum.get(thisHldRecord.orderPrice);
					if(cumFilledQty == null)
						cumFilledQty = 0.0;
					cumFilledQty += filled;
					
					thisExeSum.put(thisHldRecord.orderPrice, cumFilledQty);
					executionSummary.put(stockCode, thisExeSum);
				} // end of for
				
				// 如果有execution，则要相应地修改sell order
				for(String stockCode : executionSummary.keySet()) {
					Contract con = new Contract();
					con.symbol(stockCode);
					con.exchange("SEHK");
					con.secType("STK");
					con.currency("HKD");
					Double lotSize = avatLotSize.get(stockCode);
					
					Map<Double, Double> thisExeSum = executionSummary.get(stockCode);
					for(Double buyPrice : thisExeSum.keySet() ) {
						Double filledQty = thisExeSum.get(buyPrice);
						
						Order sellOrder1 = new Order();
						sellOrder1.action(Action.SELL);
						Double sellPrice1 = AvatUtils.getCorrectPrice_down(buyPrice * (1 + stopProfitLevel1));
						sellOrder1.lmtPrice(sellPrice1);
						Double sellQty1 = lotSize * Math.floor(filledQty*0.5/lotSize);
						sellOrder1.totalQuantity(sellQty1);  //
						sellOrder1.transmit(true);
						
						Order sellOrder2 = new Order();
						sellOrder2.action(Action.SELL);
						Double sellPrice2 = AvatUtils.getCorrectPrice_down(buyPrice * (1 + stopProfitLevel2));
						sellOrder2.lmtPrice(sellPrice2);
						Double sellQty2 = filledQty - sellQty1;
						sellOrder2.totalQuantity(sellQty2);  //
						sellOrder2.transmit(true);
						
						MyIOrderHandler sellOrderHandler1 = new MyIOrderHandler(con, sellOrder1);
						myController.placeOrModifyOrder(con, sellOrder1, sellOrderHandler1);  // sell order 放了就放了，不用monitor 
						MyIOrderHandler sellOrderHandler2 = new MyIOrderHandler(con, sellOrder2);
						myController.placeOrModifyOrder(con, sellOrder2, sellOrderHandler2);  // sell order 放了就放了，不用monitor 
					
						int count2 = 0;
						while(sellOrderHandler1.getOrderId() == -1 || sellOrderHandler2.getOrderId() == -1) {
							Thread.sleep(5); 
							count2++;
							if(count2 >= 1000) {
								logger.info("[Placing sell order no feedbacks...] stock=" + con.symbol() + " price=" + sellOrder1.lmtPrice() + "&" + sellOrder2.lmtPrice() + " qty=" + sellOrder1.totalQuantity() + "&" + sellOrder2.totalQuantity());
								break;
							}
								
						}
						
						long time = new Date().getTime();
						HoldingRecord hld1 = new HoldingRecord(sellOrderHandler1, time);
						HoldingRecord hld2 = new HoldingRecord(sellOrderHandler2, time);
						
						orderWriter.write(hld1.toString() + "\n");  // no need to add into holding records
						orderWriter.write(hld2.toString() + "\n");
						orderWriter.flush();
					}
				}		
				// --------- update完records之后，还要根据成交的情况来放sell orders --------------
				
				Thread.sleep(1000 * waitingSec);  // wait for 30 sec
			}
			
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 将holding records存到csv里面
	 */
	public static void saveHoldingRecords() {
		try {
			//Map<String, Map<Integer, HoldingRecord>> holdingRecords
			FileWriter fw = new FileWriter (AVAT_ROOT_PATH + "orders\\" + todayDate + "\\holdingRecords.csv");
			
			for(String stock : holdingRecords.keySet()) {
				String toWrite = "";
				Map<Integer, HoldingRecord> orderIdMap = holdingRecords.get(stock);
				for(Integer orderId : orderIdMap.keySet()) {
					toWrite += stock + "," + orderId + "," + orderIdMap.get(orderId).toSaveToString() + "\n";
				}
				fw.write(toWrite);
			}
			fw.close();
			logger.info("--> holding records saved!");
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void recoverTradingRecord() {
		try {
			String path = AVAT_ROOT_PATH + "orders\\" + todayDate + "\\holdingRecords.csv";
			if(!(new File(path).exists())) {
				logger.error("--> holding records recovery failed! No such file.");
				return;
			}
			
			BufferedReader bf = utils.Utils.readFile_returnBufferedReader(path);
			String line = "";
			while((line = bf.readLine()) != null) {
				ArrayList<String> arr = new ArrayList<String>(Arrays.asList(line.split(",")));
				String stock = arr.get(0);
				Integer orderId = Integer.parseInt(arr.get(1));
				ArrayList<String> subArr = new ArrayList<String>(arr.subList(2, arr.size()));
				
				HoldingRecord h = new HoldingRecord ();
				h.recoverFromString(subArr);
				
				Map<Integer, HoldingRecord> orderIdMap = holdingRecords.get(stock);
				if(orderIdMap  == null)
					orderIdMap = new HashMap<Integer, HoldingRecord>();
				orderIdMap.put(orderId, h);
				holdingRecords.put(stock, orderIdMap);
			}
			
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		
	}

}
