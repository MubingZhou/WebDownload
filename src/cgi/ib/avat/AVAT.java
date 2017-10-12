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
import com.ib.client.Execution;
import com.ib.client.ExecutionFilter;
import com.ib.client.Order;
import com.ib.client.OrderStatus;
import com.ib.client.OrderType;
import com.ib.client.Types.Action;
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
	private static Logger logger = Logger.getLogger(AVAT.class.getName());
	public static String dateFormat = "yyyyMMdd HH:mm:ss";
	public static SimpleDateFormat sdf = new SimpleDateFormat (dateFormat); 
	public static String todayDate /*= new SimpleDateFormat ("yyyyMMdd").format(new Date())*/;
	public static ArrayList<Calendar> allTradingDate = new ArrayList<Calendar>();
	public static SimpleDateFormat sdf_100 = new SimpleDateFormat ("yyyyMMdd HH_mm_ss"); 
	private static String errMsgHead = "[AVAT - error] ";
	
	private static Map<Double, String> msg_eligibleStocksMap = new HashMap<Double, String>();  // 用来存储每次运行完之后符合要求的股票，用于在对话框中显示，key是avat，value是stock code
	private static String alertToShow = "";   // 将要显示在弹出框的内容
	private static JFrame frame = new JFrame();
	
	// -------- orders ----------
	//private static ArrayList<String> boughtRecords = new ArrayList<String>(); // stocks that have been bought 
	public static boolean isStartOrders = true; // 是否开始监视order并落单
	private static int isLotSizeMapToUpdate = 0;  // lot size map是否需要升级
	private static Map<String, HoldingRecord> holdingRecords = new HashMap<String, HoldingRecord>();  // String是stock code
	private static String orderWriterPath ;
	private static FileWriter orderWriter;
	private static boolean transmitToIB = true;  // 是否transmit 到 IB
	
	
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
		allTradingDate = utils.Utils.getAllTradingDate("D:\\stock data\\all trading date - hk.csv");
		
		// ------- avat - prepare historical avat --------
		AvatUtils.preparePrevCrossSectionalAvat2(conArr, todayDate, "yyyyMMdd");
		logger.info("prepare prev cross setional avat - done");
		try {
			//Thread.sleep(1000 * 10000000);
		} catch (Exception e) {
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
			 * 375 - RT trade volume
			 */
			
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
							   JOptionPane.showMessageDialog(frame, alertToShow, "AVAT Results", JOptionPane.PLAIN_MESSAGE);
						        
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
			Double fixedBuyAmount = 100000.0;  // fix buying amount for each stock, HKD
			Double buyPriceDiscount = 1.0;  // 为了testing，不让order被fill，可以将buyprice设小点
			
			// ------- 与买入有关的variables ----------
			String buyStartTimeStr = todayDate + " 09:30:00";
			Date buyStartTime = sdf.parse(buyStartTimeStr);
			String buyEndTimeStr = todayDate + " 16:00:00";
			Date buyEndTime = sdf.parse(buyEndTimeStr);
			
			double avatThld5D = 3.0;  // avat threshold
			double avatThld20D = 2.0;  // avat threshold
			double turnoverThld = 5000000.0;
			
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
				 * 		(2.1 当前的avat5D ratio超过2，并且股价上涨，涨幅不超过3%；或者
				 * 		2.2 avat20D ratio超过1，并且股价上涨，涨幅不超过3%
				 * 		2.3. 当时的volume超过了昨天全天的volume)
				 * 		3. turnover 大于3 million 
				 * 		4. 已经买过的股票不再买入
				 */
				
				Date thisTime = new Date(singleRec.timeStamp);
				if(thisTime.after(buyStartTime) && thisTime.before(buyEndTime)) {  // 只在合适的时间段内判读是否出现买入信号
					int buyCond2_1 = 0;
					int buyCond2_2 = 0;
					int buyCond2_3 = 0;
					int buyCond3 = 0;
					int buyCond4 = 0;
					
					double priceChg = singleRec.currentPrice / avatPrevClose.get(singleRec.stockCode) - 1;
					
					if(singleRec.avatRatio5D > avatThld5D && (priceChg > 0 && priceChg <= 0.03))
						buyCond2_1 = 1;
					if(singleRec.avatRatio20D > avatThld20D && (priceChg > 0 && priceChg <= 0.03))
						buyCond2_2 = 1;
					if(singleRec.volume >= singleRec.prevVolume)
						buyCond2_3 = 1;
					if(singleRec.turnover >= turnoverThld)
						buyCond3 = 1;
					
					//buyCond2_2 = 0;
					//buyCond2_3 = 0;  // 暂时，先不考虑这两个factor的影响
					
					if(!holdingRecords.keySet().contains(singleRec.stockCode))  // 之前没买过
						buyCond4 = 1;
					
					// ----------- 处理 buy signal -------------
					if((buyCond2_1 == 1 || buyCond2_2 == 1 || buyCond2_3 == 1) && buyCond3 == 1 && buyCond4 == 1) {  
						//logger.info("[scan for orders] found stock! stock=" + );
						// 新开一个线程来处理似乎不妥当，因为每个order的id必须大于之前order的id，所以如果很多线程并行的话，不能保证先提交给ib的order的id是最小的
						
						String stockCode = singleRec.stockCode;
						logger.debug("    stock=" + stockCode + " BUY ");
						
						Contract con = conMap.get(stockCode);
						
						Order order = new Order();
						order.action("BUY");
						order.orderType(OrderType.LMT);
						
						Double buyPrice = singleRec.latestBestBid;
						buyPrice = AvatUtils.getCorrectPrice_up(buyPrice * buyPriceDiscount);
						order.lmtPrice(buyPrice);  // 以best bid作为买入价
						
						Double lotSize = avatLotSize.get(stockCode);
						Double orderQty = lotSize * (int)(fixedBuyAmount / lotSize / buyPrice) ;
						order.totalQuantity(orderQty);
						order.transmit(transmitToIB);  // false - 只在api平台有这个order
						
						MyIOrderHandler myOrderH = new MyIOrderHandler (con, order); 
						myOrderH.isTransmit = transmitToIB;
						myController.placeOrModifyOrder(con, order, myOrderH);
						
						// --------- submit orders ---------
						boolean control= false;
						if(!control) {
							HoldingRecord hld = new HoldingRecord(con.symbol(), con, now.getTime(), buyPrice, orderQty);
							hld.orderId = myOrderH.orderId;
							
							holdingRecords.put(con.symbol(), hld);  // 存储holding
							
							orderWriter.write(hld.toString() + "\n");
							orderWriter.flush();
						}
						while(control) {
							if(myOrderH.isSubmitted == 1) {
								logger.info("Order submitted! stock=" + con.symbol() + " " + orderQty + " " + order.action() + " " + order.lmtPrice() + " scanTime" + sdf.format(now) + " realTime=" + sdf.format(new Date()));
								HoldingRecord hld = new HoldingRecord(con.symbol(), con, now.getTime(), buyPrice, orderQty);
								hld.orderId = myOrderH.orderId;
								
								holdingRecords.put(con.symbol(), hld);  // 存储holding
								
								orderWriter.write(hld.toString() + "\n");
								orderWriter.flush();
								
								break;
							}
							
							// 处理error
							if(myOrderH.errorCode == 461) {
								isLotSizeMapToUpdate=1;
								Double newLotSize = myOrderH.newLostSize;  // 修改然后resubmit
								orderQty = newLotSize * (int)(fixedBuyAmount / newLotSize / buyPrice);
								order.totalQuantity(orderQty );
								
								myController.placeOrModifyOrder(con, order, myOrderH);
								
								logger.info("need new lot size = " + newLotSize + " resubmit order!");
								
								// update avatLotSize
								avatLotSize.put(stockCode, newLotSize);
								myOrderH.errorCode = -1;  // 处理完毕，reset errorcode
							}
							
							if(!transmitToIB) // for simulation
								myOrderH.isSubmitted = 1;
							
							Thread.sleep(30);
						} // end of while	
					}
				}  // 买入信号的if结束
				
				/*
				 * --------------- 卖出信号 ---------------
				 * 卖出条件：
				 * 		1. 获利超过3%，以最优bid价卖出 （止盈）
				 * 		2. 持股到当日15：00仍未卖出，以买入成本价（需要考虑交易成本）卖出
				 */
				int sellCond1 = 0;
				int sellCond2 = 0;
				
				if(holdingStocks .contains(singleRec.stockCode)) {  // 只有在已经买了这只股票之后才判断是否需要卖出
					HoldingRecord thisHolding = holdingRecords.get(singleRec.stockCode);
					
					if(thisHolding.filledQty > 0) {   // 如果有filled的qty，先看看是否符合卖出条件，如果是，则cancel剩余的qty（如有），然后卖出filled的qty
						Double currentPrice = singleRec.currentPrice;
						Double avgFillPrice = thisHolding.avgFillPrice;
						
						if(currentPrice > avgFillPrice * 1.03) { // 止盈卖出(不考虑交易成本)
							sellCond1 = 1;
						}
						if(now.after(sellThldTime)) {
							sellCond2 = 1;
						}
						
						if(sellCond1 == 1 || sellCond2 == 1) {
							
							if(thisHolding.filledQty < thisHolding.orderQty) {// partially filled, 先cancel剩余的qty，然后卖出filled的qty
								int orderId = thisHolding.orderId;
								myController.cancelOrder(orderId);
							}
							
							Contract con = singleRec.contract;
							
							Order sellOrder = new Order();
							sellOrder.action(Action.SELL);
							Double sellPrice = 0.0;
							if(sellCond1 == 1) {
								sellPrice = singleRec.latestBestAsk;
							}
							if(sellCond2 == 1) {
								sellPrice = AvatUtils.getCorrectPrice_down(thisHolding.orderPrice * (1+bilateralTrdCost));
							}
							sellOrder.lmtPrice(sellPrice);
							sellOrder.totalQuantity(thisHolding.filledQty);
							
							sellOrder.transmit(transmitToIB);  // false - 只在api平台有这个order
							
							// 不用监视了吧
							MyIOrderHandler myOrderH = new MyIOrderHandler (con, sellOrder); 
							myController.placeOrModifyOrder(con, sellOrder, myOrderH);
							
						}					
					}
				}
					
			}
			
			logger.info("---------- scanForOrders ENDS ---------");
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * 监视所有Open orders，然后update holdingRecords
	 */
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
				
				/*
				boolean c = false;
				while(c) {
					if(myLiveOrder.isEnd) {  // order收集完全
						myLiveOrder.isEnd = false;
						break;
					}
					Thread.sleep(100);
				} // end of while
				*/
				
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
	
	public static void executionMonitor() {
		try {
			long lastRequestTime = new Date().getTime() - 5000;
			lastRequestTime=0;
			
			String executionsRecPath = AVAT_ROOT_PATH + "orders\\" + todayDate + "\\executions records.csv";
			
			String cancelOrderStartStr = todayDate + " 16:31:30";
			Date cancelOrderStartDate = sdf.parse(cancelOrderStartStr);
			String dayEndStr = todayDate + " 23:10:00";
			Date dayEndDate = sdf.parse(dayEndStr);
			
			MyITradeReportHandler myTradeReport = new MyITradeReportHandler(executionsRecPath);
			while(true) {
				// ---------- MyITradeReportHandler的一些setting -----------
				ExecutionFilter filter = new ExecutionFilter();
				filter.secType("FUT");
				if(lastRequestTime != 0)
					filter.time(sdf.format(new Date(lastRequestTime)));
				myTradeReport.initialize();
				myController.reqExecutions(filter, myTradeReport);
				lastRequestTime = new Date().getTime();
				myTradeReport.isCalledByMonitor = 0;
				
				Date thisNow = new Date();
				
				
				if(thisNow.after(dayEndDate))
					break;
				
				while(true) {
					if(myTradeReport.isEnd == 1)
						break;
					Thread.sleep(10);
				}
				
				// ---------- 读取MyITradeReportHandler返回的数据 ---------------
				logger.info("[Start scanning execution details...] " + sdf.format(new Date()));
				ArrayList<ArrayList<Object>> tradeReportArr = myTradeReport.tradeReportArr;
				for(ArrayList<Object> tradeReport : tradeReportArr ) {
					Contract contract = (Contract) tradeReport.get(1);
					Execution execution = (Execution) tradeReport.get(2);
					
					String stockCode = contract.symbol();
					Double filled = execution.shares();
					Double avgFillPrice = execution.avgPrice(); // not include commission
					String executionId = execution.execId();
					
					// ------ update holdingRecords ----
					HoldingRecord thisHldRecord = holdingRecords.get(stockCode);
				
					if(thisHldRecord == null)
						continue;
					
					Double oldQty = thisHldRecord.filledQty;
					Double oldAvgFillPrice = thisHldRecord.avgFillPrice;
					//Double oldLastFillPrice = thisHldRecord.lastFillPrice;
					ArrayList<String> executionIdArr = thisHldRecord.executionIdArr;
					
					if(executionIdArr.contains(executionId)) // 不知道为什么，server有时候会重复发来之前的execution
						continue;
					
					thisHldRecord.executionIdArr.add(executionId);
					thisHldRecord.filledQty += filled;
					Double newAvgFillPrice  = (oldAvgFillPrice * oldQty + filled * avgFillPrice)/thisHldRecord.filledQty;
					thisHldRecord.avgFillPrice = newAvgFillPrice;
					//thisHldRecord.lastFillPrice = lastFillPrice;
					
					
					logger.info("      [execution details] new hlding rec: stock=" + stockCode + " filledQty=" + thisHldRecord.filledQty 
							+ " avgFillPrice=" + newAvgFillPrice );
					holdingRecords.put(stockCode, thisHldRecord);
					
				} // end of for
				
				// -------  如果时间在11点之后，所有的open buy order都要cancel ---------
				if(thisNow.after(cancelOrderStartDate)) {
					MyILiveOrderHandler myLiveOrder = new MyILiveOrderHandler();
					myController.reqLiveOrders(myLiveOrder);
					
					while(true) {
						if(myLiveOrder.isEnd) {
							break;
						}
						Thread.sleep(10);
					}
					
					for(ArrayList<Object> orderStatus : myLiveOrder.orderStatusArr) {
						int orderId = (int) orderStatus.get(0);
						
						for(ArrayList<Object> openOrder : myLiveOrder.openOrderArr) {
							Order order = (Order) openOrder.get(1);
							if(order.orderId() == orderId) {
								if(order.action().equals(Action.BUY))
									myController.cancelOrder(orderId);
								break;
							}
						}
					}
				}
				
				Thread.sleep(1000 * 5);  // wait for 5 sec
			}
			
		}catch(Exception e) {
			e.printStackTrace();
		}
	}

}
