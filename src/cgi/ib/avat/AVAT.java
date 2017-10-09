package cgi.ib.avat;

import java.awt.Toolkit;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
import com.ib.client.Order;
import com.ib.client.OrderType;
import com.ib.controller.ApiController;

import utils.XMLUtil;

public class AVAT {
	// -------- main variables -------------
	public static ArrayList<Contract> conArr = new ArrayList<Contract>();
	public static Map<String, Contract> conMap = new HashMap<String, Contract>();
	public static MyAPIController myController;
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
	public static Map<String, Double> avatLotSize = new HashMap<String, Double> ();
	public static Map<String, Double> prevVolume = new HashMap<String, Double>(); // previous day's volume for every stock
	
	// --------- other variables ---------
	private static Logger logger = Logger.getLogger(AVAT.class.getName());
	public static String dateFormat = "yyyyMMdd HH:mm:ss";
	public static SimpleDateFormat sdf = new SimpleDateFormat (dateFormat); 
	public static String todayDate /*= new SimpleDateFormat ("yyyyMMdd").format(new Date())*/;
	public static ArrayList<Calendar> allTradingDate = new ArrayList<Calendar>();
	public static SimpleDateFormat sdf_100 = new SimpleDateFormat ("yyyyMMdd HH_mm_ss"); 
	private static String errMsgHead = "[AVAT - error] ";
	
	private static Map<Double, String> msg_eligibleStocksMap = new HashMap<Double, String>();  // 用来存储每次运行完之后符号要求的股票，用于在对话框中显示，key是avat，value是stock code
	private static String alertToShow = "";   // 将要显示在弹出框的内容
	private static JFrame frame = new JFrame();
	
	// -------- orders ----------
	private static ArrayList<String> boughtRecords = new ArrayList<String>(); // stocks that have been bought 
	private static int isLotSizeMapToUpdate = 0;  // lot size map是否需要升级
	
	
	public static void setting(MyAPIController myController0, ArrayList<Contract> conArr0, String AVAT_ROOT_PATH0) {
		myController = myController0;
		conArr = (ArrayList<Contract>) conArr0.clone();
		AVAT_ROOT_PATH = AVAT_ROOT_PATH0;
		
		numOfTopMktDataStock = conArr.size();
		for(Contract con : conArr) {  // construct conMap
			String stock = con.symbol();
			conMap.put(stock, con);
		}
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
		
		request();
		logger.info("request done");
		
		output();
		logger.info("output done");
	}
	
	private static void prepare() {
		allTradingDate = utils.Utils.getAllTradingDate("D:\\stock data\\all trading date - hk.csv");
		
		// ------- avat - prepare historical avat --------
		AvatUtils.preparePrevCrossSectionalAvat2(conArr, todayDate, "yyyyMMdd");
		logger.info("prepare prev cross setional avat - done");
		
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
		
		// ------ avat - get previous day's volume --------
		prevVolume = AvatUtils.getPreviousVolume(conArr);   // 这个volume不包好unreportable的volume
		
		// ------ avat lot size --------
		avatLotSize = AvatUtils.getLotSize();
		logger.info("get lot size - done");
		
		// ------- 设置弹出窗口的容器 ----------
	   frame.setLocation(0,0);
	   frame.setSize(300, 700);
	   frame.setVisible(true);
	}
	
	/**
	 * 请求实时数据
	 */
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
					
					AvatRecordSingleStock at = new AvatRecordSingleStock(now.getTime(), stock, price, prevCloseChgPct, ratio5D, ratio20D, industry);
					at.turnover = trdRtTurnover;
					at.latestBestAsk = latestBestAsk;
					at.latestBestBid = latestBestBid;
					at.prevVolume = prevVolume.get(stock);
					at.volume = trdRtVolume;
					
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
				
				// ----------- place orders --------
				
				
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
							   JOptionPane.showMessageDialog(frame, alertToShow, "AVAT Results", JOptionPane.PLAIN_MESSAGE);
						        
						   }
						});
						t.start();
				}
				
				
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
		try {
			String errMsgHead  = "[trading strategy] ";
			Double fixedBuyAmount = 500000.0;  // fix buying amount for each stock, HKD
			boolean transmitToIB = false;
			
			// 浏览各个AvatRecordSingleStock 
			for(AvatRecordSingleStock singleRec : avatRecord) {
				
				/*
				 * ------- 先看是否有买入信号 ---------
				 * 买入条件：
				 * 		1. 时间是11:00之前；并且
				 * 		(2. 当前的avat5D ratio超过3，并且估价上涨，涨幅不超过3%；或者
				 * 		3. 当时的volume超过了全天的volume)
				 * 		4. 已经买过的股票不再买入
				 */
				String buyStartTimeStr = todayDate + " 09:30:00";
				Date buyStartTime = sdf.parse(buyStartTimeStr);
				String buyEndTimeStr = todayDate + " 11:00:00";
				Date buyEndTime = sdf.parse(buyEndTimeStr);
				
				double avatThld = 3.0;  // avat threshold
				
				Date thisTime = new Date(singleRec.timeStamp);
				if(thisTime.after(buyStartTime) && thisTime.before(buyEndTime)) {  // 只在合适的时间段内判读是否出现买入信号
					int buyCond1 = 0;
					int buyCond2 = 0;
					int buyCond3 = 0;
					
					if(singleRec.avatRatio5D > avatThld)
						buyCond1 = 1;
					if(singleRec.volume >= singleRec.prevVolume)
						buyCond2 = 1;
					if(boughtRecords.indexOf(singleRec.stockCode) >= 0)
						buyCond3 = 1;
					
					if((buyCond1 == 1 || buyCond2 == 1) && buyCond3 == 1) {  // buy signal
						
						// 新开一个线程来处理
						Thread t = new Thread(new Runnable(){
							   public void run(){
								   	String stockCode = singleRec.stockCode;
									
									Contract con = conMap.get(stockCode);
									
									Order order = new Order();
									order.action("BUY");
									order.orderType(OrderType.LMT);
									
									Double buyPrice = singleRec.latestBestBid;
									order.lmtPrice(buyPrice);  // 以best bid作为买入价
									
									Double lotSize = avatLotSize.get(stockCode);
									Double orderQty = lotSize * (int)(fixedBuyAmount / lotSize / buyPrice) ;
									order.totalQuantity(orderQty);
									order.transmit(true);  // false - 只在api平台有这个order
									
									MyIOrderHandler myOrderH = new MyIOrderHandler (con, order); 
									myController.placeOrModifyOrder(con, order, myOrderH);
									
									while(true) {
										if(myOrderH.isSubmitted == 1) {
											logger.info("Order submitted! " + con.symbol() + " " + orderQty + " " + order.action() + " " + order.lmtPrice());
											break;
										}
										
										// 处理error
										if(myOrderH.errorCode == 461) {
											isLotSizeMapToUpdate=1;
											Double newLotSize = myOrderH.newLostSize;  // 修改然后resubmit
											orderQty = newLotSize * (int)(500000 / newLotSize / buyPrice);
											order.totalQuantity(orderQty );
											
											myController.placeOrModifyOrder(con, order, myOrderH);
											
											logger.info("need new lot size = " + newLotSize + " resubmit order!");
											
											// update avatLotSize
											avatLotSize.put(stockCode, newLotSize);
										}
										
										try {
											Thread.sleep(500);
										} catch (InterruptedException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										}
									} // end of while
							   }
							});
						t.start();
					}
				}
				
				/*
				 * --------------- 卖出信号 ---------------
				 * 卖出条件：
				 * 		1. 获利超过3%，以最优bid价卖出 （止盈）
				 * 		2. 持股到当日15：00仍未卖出，以买入成本价（需要考虑交易成本）卖出
				 */
					
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
	}

}
