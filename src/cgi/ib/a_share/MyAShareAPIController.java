package cgi.ib.a_share;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.ib.client.Contract;
import com.ib.client.ContractDetails;
import com.ib.client.ExecutionFilter;
import com.ib.client.Order;
import com.ib.client.ScannerSubscription;
import com.ib.client.TickAttr;
import com.ib.client.Types.BarSize;
import com.ib.client.Types.DurationUnit;
import com.ib.client.Types.ExerciseType;
import com.ib.client.Types.FADataType;
import com.ib.client.Types.FundamentalType;
import com.ib.client.Types.WhatToShow;
import com.ib.controller.AccountSummaryTag;
import com.ib.controller.ApiController;
import com.ib.controller.Group;
import com.ib.controller.Profile;
import com.ib.controller.ApiConnection.ILogger;
import com.ib.controller.ApiController.IAccountHandler;
import com.ib.controller.ApiController.IAccountSummaryHandler;
import com.ib.controller.ApiController.IAccountUpdateMultiHandler;
import com.ib.controller.ApiController.IAdvisorHandler;
import com.ib.controller.ApiController.IBulletinHandler;
import com.ib.controller.ApiController.IConnectionHandler;
import com.ib.controller.ApiController.IContractDetailsHandler;
import com.ib.controller.ApiController.IDeepMktDataHandler;
import com.ib.controller.ApiController.IEfpHandler;
import com.ib.controller.ApiController.IFamilyCodesHandler;
import com.ib.controller.ApiController.IFundamentalsHandler;
import com.ib.controller.ApiController.IHeadTimestampHandler;
import com.ib.controller.ApiController.IHistogramDataHandler;
import com.ib.controller.ApiController.IHistoricalNewsHandler;
import com.ib.controller.ApiController.IHistoricalTickHandler;
import com.ib.controller.ApiController.IMarketRuleHandler;
import com.ib.controller.ApiController.IMarketValueSummaryHandler;
import com.ib.controller.ApiController.IMktDepthExchangesHandler;
import com.ib.controller.ApiController.INewsArticleHandler;
import com.ib.controller.ApiController.INewsProvidersHandler;
import com.ib.controller.ApiController.IOptHandler;
import com.ib.controller.ApiController.IOrderHandler;
import com.ib.controller.ApiController.IPnLHandler;
import com.ib.controller.ApiController.IPnLSingleHandler;
import com.ib.controller.ApiController.IPositionHandler;
import com.ib.controller.ApiController.IPositionMultiHandler;
import com.ib.controller.ApiController.IRealTimeBarHandler;
import com.ib.controller.ApiController.IScannerHandler;
import com.ib.controller.ApiController.ISecDefOptParamsReqHandler;
import com.ib.controller.ApiController.ISmartComponentsHandler;
import com.ib.controller.ApiController.ISoftDollarTiersReqHandler;
import com.ib.controller.ApiController.ISymbolSamplesHandler;
import com.ib.controller.ApiController.ITickNewsHandler;
import com.ib.controller.ApiController.ITimeHandler;
import com.ib.controller.ApiController.ITopMktDataHandler;

public class MyAShareAPIController extends ApiController{
	private static Logger logger = Logger.getLogger(MyAShareAPIController.class.getName());

	LinkedHashMap<Integer, RequestType> reqIdMap = new LinkedHashMap<Integer, RequestType> ();
	
	// ---------------------------------------- Constructor and Connection handling ----------------------------------------
	public MyAShareAPIController( IConnectionHandler handler, ILogger inLogger, ILogger outLogger) {
		super(handler, inLogger, outLogger);
	}
	
	@Override
	public void connect( String host, int port, int clientId, String connectionOpts ) {
		super.connect(host, port, clientId, connectionOpts);
	}
	
	@Override
	public void disconnect() {
		super.disconnect();
	}
	
	@Override public void managedAccounts(String accounts) {
		super.managedAccounts(accounts);
	}
	
	@Override public void nextValidId(int orderId) {
		super.nextValidId(orderId);
	}
	
	@Override public void error(Exception e) {
		super.error(e);
	}
	@Override public void error(int id, int errorCode, String errorMsg) {
		super.error(id, errorCode, errorMsg);
		
		if(id == -1)
			return;
		
		String errMsgHead = "[MyAPIConnection - error] ";
		
		logger.error(errMsgHead + errorMsg);
		
		RequestType t = reqIdMap.get(id);
		if(t == null) {
			logger.error(errMsgHead + " request type not found!");
		}else {
			switch(t) {
			case HistoricalData:
				// codes ...
				break;
			case TopMarketData:
				//
				break;
			case RealTimeBar:
				//
				break;
			default:
				//
				break;
			}
		}
		
	}

	@Override public void connectionClosed() {
		super.connectionClosed();
		
		System.out.println("[MyAPIController] Closed!!");
	}
	
	// ---------------------------------------- Account and portfolio updates ----------------------------------------
	public void reqAccountUpdates(boolean subscribe, String acctCode, IAccountHandler handler) {
		super.reqAccountUpdates(subscribe, acctCode, handler);
    }

	@Override public void updateAccountValue(String tag, String value, String currency, String account) {
		super.updateAccountValue(tag, value, currency, account);
	}

	@Override public void updateAccountTime(String timeStamp) {
		super.updateAccountTime(timeStamp);
	}

	@Override public void accountDownloadEnd(String account) {
		super.accountDownloadEnd(account);
	}

	@Override public void updatePortfolio(Contract contract, double positionIn, double marketPrice, double marketValue, double averageCost, double unrealizedPNL, double realizedPNL, String account) {
		super.updatePortfolio(contract, positionIn, marketPrice, marketValue, averageCost, unrealizedPNL, realizedPNL, account);
	}
	
	
	// ---------------------------------------- Account Summary handling ----------------------------------------
	/** @param group pass "All" to get data for all accounts */
	public void reqAccountSummary(String group, AccountSummaryTag[] tags, IAccountSummaryHandler handler) {
		super.reqAccountSummary(group, tags, handler);
	}

	public void cancelAccountSummary(IAccountSummaryHandler handler) {
		super.cancelAccountSummary(handler);
	}

	public void reqMarketValueSummary(String group, IMarketValueSummaryHandler handler) {
		super.reqMarketValueSummary(group, handler);
	}

	public void cancelMarketValueSummary(IMarketValueSummaryHandler handler) {
		super.cancelMarketValueSummary(handler);
	}

	@Override public void accountSummary( int reqId, String account, String tag, String value, String currency) {
		super.accountSummary(reqId, account, tag, value, currency);
	}

	@Override public void accountSummaryEnd( int reqId) {
		super.accountSummaryEnd(reqId);
	}
	
	// ---------------------------------------- Position handling ----------------------------------------
	public void reqPositions( IPositionHandler handler) {
		super.reqPositions(handler);
	}

	public void cancelPositions( IPositionHandler handler) {
		super.cancelPositions(handler);
	}

	@Override public void position(String account, Contract contract, double pos, double avgCost) {
		super.position(account, contract, pos, avgCost);
	}

	@Override public void positionEnd() {
		super.positionEnd();
	}

	// ---------------------------------------- Contract Details ----------------------------------------
	public void reqContractDetails( Contract contract, final IContractDetailsHandler processor) {
		super.reqContractDetails(contract, processor);
	}

	@Override public void contractDetails(int reqId, ContractDetails contractDetails) {
		super.contractDetails(reqId, contractDetails);
	}

	@Override public void bondContractDetails(int reqId, ContractDetails contractDetails) {
		super.bondContractDetails(reqId, contractDetails);
	}

	@Override public void contractDetailsEnd(int reqId) {
		super.contractDetailsEnd(reqId);
	}

	// ---------------------------------------- Top Market Data handling ----------------------------------------
    public void reqTopMktData(Contract contract, String genericTickList, boolean snapshot, boolean regulatorySnapshot, MyAShareTopMktDataHandler handler) {
		super.reqTopMktData(contract, genericTickList, snapshot, regulatorySnapshot, handler);
		//super.client().reqIds(numIds);
		handler.contract = contract;
    }

    public void reqOptionMktData(Contract contract, String genericTickList, boolean snapshot, boolean regulatorySnapshot, IOptHandler handler) {
		super.reqOptionMktData(contract, genericTickList, snapshot, regulatorySnapshot, handler);
    }

    public void reqEfpMktData(Contract contract, String genericTickList, boolean snapshot, boolean regulatorySnapshot, IEfpHandler handler) {
		super.reqEfpMktData(contract, genericTickList, snapshot, regulatorySnapshot, handler);
    }

    public void cancelTopMktData( ITopMktDataHandler handler) {
		super.cancelTopMktData(handler);
    }

    public void cancelOptionMktData( IOptHandler handler) {
    	super.cancelOptionMktData(handler);
    }

    public void cancelEfpMktData( IEfpHandler handler) {
    	super.cancelEfpMktData(handler);
    }

	public void reqMktDataType( int mktDataType) {
		super.reqMktDataType(mktDataType);
	}

	@Override public void tickPrice(int reqId, int tickType, double price, TickAttr attribs) {
		super.tickPrice(reqId, tickType, price, attribs);
	}

	@Override public void tickGeneric(int reqId, int tickType, double value) {
		super.tickGeneric(reqId, tickType, value);
	}

	@Override public void tickSize(int reqId, int tickType, int size) {
		super.tickSize(reqId, tickType, size);
	}

	@Override public void tickString(int reqId, int tickType, String value) {
		super.tickString(reqId, tickType, value);
	}

	@Override public void tickEFP(int reqId, int tickType, double basisPoints, String formattedBasisPoints, double impliedFuture, int holdDays, String futureLastTradeDate, double dividendImpact, double dividendsToLastTradeDate) {
		super.tickEFP(reqId, tickType, basisPoints, formattedBasisPoints, impliedFuture, holdDays, futureLastTradeDate, dividendImpact, dividendsToLastTradeDate);
	}

	@Override public void tickSnapshotEnd(int reqId) {
		super.tickSnapshotEnd(reqId);
	}

	@Override public void marketDataType(int reqId, int marketDataType) {
		super.marketDataType(reqId, marketDataType);
	}


	// ---------------------------------------- Deep Market Data handling ----------------------------------------
    public void reqDeepMktData( Contract contract, int numRows, IDeepMktDataHandler handler) {
		super.reqDeepMktData(contract, numRows, handler);
    }

    public void cancelDeepMktData( IDeepMktDataHandler handler) {
		super.cancelDeepMktData(handler);
    }

	@Override public void updateMktDepth(int reqId, int position, int operation, int side, double price, int size) {
		super.updateMktDepth(reqId, position, operation, side, price, size);
	}

	@Override public void updateMktDepthL2(int reqId, int position, String marketMaker, int operation, int side, double price, int size) {
		super.updateMktDepthL2(reqId, position, marketMaker, operation, side, price, size);
	}

	// ---------------------------------------- Option computations ----------------------------------------
	public void reqOptionVolatility(Contract c, double optPrice, double underPrice, IOptHandler handler) {
		super.reqOptionVolatility(c, optPrice, underPrice, handler);
	}

	public void reqOptionComputation( Contract c, double vol, double underPrice, IOptHandler handler) {
		super.reqOptionComputation(c, vol, underPrice, handler);
	}


	@Override public void tickOptionComputation(int reqId, int tickType, double impliedVol, double delta, double optPrice, double pvDividend, double gamma, double vega, double theta, double undPrice) {
		super.tickOptionComputation(reqId, tickType, impliedVol, delta, optPrice, pvDividend, gamma, vega, theta, undPrice);
	}


	// ---------------------------------------- Trade reports ----------------------------------------
    public void reqExecutions( ExecutionFilter filter, ITradeReportHandler handler) {
		super.reqExecutions(filter, handler);
		//handler.isCalledByMonitor = 1;
    }

	

	// ---------------------------------------- Advisor info ----------------------------------------
	public void reqAdvisorData( FADataType type, IAdvisorHandler handler) {
		super.reqAdvisorData(type, handler);
	}

	public void updateGroups( List<Group> groups) {
		super.updateGroups(groups);
	}

	public void updateProfiles(List<Profile> profiles) {
		super.updateProfiles(profiles);
	}


	// ---------------------------------------- Trading and Option Exercise ----------------------------------------
	public void placeOrModifyOrder(Contract contract, final Order order, final IOrderHandler handler) {
		super.placeOrModifyOrder(contract, order, handler);
		
	}

	public void cancelOrder(int orderId) {
		super.cancelOrder(orderId);
	}

	public void cancelAllOrders() {
		super.cancelAllOrders();
	}

	public void exerciseOption( String account, Contract contract, ExerciseType type, int quantity, boolean override) {
		super.exerciseOption(account, contract, type, quantity, override);
	}

	public void removeOrderHandler( IOrderHandler handler) {
		super.removeOrderHandler(handler);
	}


	// ---------------------------------------- Live order handling ----------------------------------------
	public void reqLiveOrders( ILiveOrderHandler handler) {
		super.reqLiveOrders(handler);
	}

	public void takeTwsOrders( ILiveOrderHandler handler) {
		super.takeTwsOrders(handler);
	}

	public void takeFutureTwsOrders( ILiveOrderHandler handler) {
		super.takeFutureTwsOrders(handler);
	}

	public void removeLiveOrderHandler(ILiveOrderHandler handler) {
		super.removeLiveOrderHandler(handler);
		try {
			//handler.fw.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error("[MyAIPController - removeLiveOrderHandler] fileWriter can't closed!");
		}
	}


	// ---------------------------------------- Market Scanners ----------------------------------------
	public void reqScannerParameters( IScannerHandler handler) {
		super.reqScannerParameters(handler);
	}

	public void reqScannerSubscription( ScannerSubscription sub, IScannerHandler handler) {
		super.reqScannerSubscription(sub, handler);
	}

	public void cancelScannerSubscription( IScannerHandler handler) {
		super.cancelScannerSubscription(handler);
	}



	// ----------------------------------------- Historical data handling ----------------------------------------
	/** @param endDateTime format is YYYYMMDD HH:MM:SS [TMZ]
	 *  @param duration is number of durationUnits */
    public void reqHistoricalData(Contract contract, String endDateTime, int duration, DurationUnit durationUnit, BarSize barSize, WhatToShow whatToShow, boolean rthOnly, boolean keepUpToDate, IHistoricalDataHandler handler) {
		super.reqHistoricalData(contract, endDateTime, duration, durationUnit, barSize, whatToShow, rthOnly, keepUpToDate, handler);
    
		int reqId = super.m_reqId;
		reqIdMap.put(reqId, RequestType.HistoricalData);
		
		//handler.isActive = 1;
		//handler.reqId = reqId;
    }
    

    public void cancelHistoricalData( IHistoricalDataHandler handler) {
		super.cancelHistoricalData(handler);
		//handler.isActive = 0;
    }

	//----------------------------------------- Real-time bars --------------------------------------
    public void reqRealTimeBars(Contract contract, WhatToShow whatToShow, boolean rthOnly, IRealTimeBarHandler handler) {
		super.reqRealTimeBars(contract, whatToShow, rthOnly, handler);
    }

    public void cancelRealtimeBars( IRealTimeBarHandler handler) {
		super.cancelRealtimeBars(handler);
    }

    @Override public void realtimeBar(int reqId, long time, double open, double high, double low, double close, long volume, double wap, int count) {
    	super.realtimeBar(reqId, time, open, high, low, close, volume, wap, count);
	}

    // ----------------------------------------- Fundamentals handling ----------------------------------------
    public void reqFundamentals( Contract contract, FundamentalType reportType, IFundamentalsHandler handler) {
		super.reqFundamentals(contract, reportType, handler);
    }

    @Override public void fundamentalData(int reqId, String data) {
    	super.fundamentalData(reqId, data);
	}


	// ---------------------------------------- Time handling ----------------------------------------
	public void reqCurrentTime( ITimeHandler handler) {
		super.reqCurrentTime(handler);
	}

	@Override public void currentTime(long time) {
		super.currentTime(time);
	}

	// ---------------------------------------- Bulletins handling ----------------------------------------
	public void reqBulletins( boolean allMessages, IBulletinHandler handler) {
		super.reqBulletins(allMessages, handler);
	}

	public void cancelBulletins() {
		super.cancelBulletins();
	}

	@Override public void updateNewsBulletin(int msgId, int msgType, String message, String origExchange) {
		super.updateNewsBulletin(msgId, msgType, message, origExchange);
	}
	

	// ---------------------------------------- Position Multi handling ----------------------------------------
	public void reqPositionsMulti( String account, String modelCode, IPositionMultiHandler handler) {
		super.reqPositionsMulti(account, modelCode, handler);
	}

	public void cancelPositionsMulti( IPositionMultiHandler handler) {
		super.cancelPositionsMulti(handler);
	}

	@Override public void positionMulti( int reqId, String account, String modelCode, Contract contract, double pos, double avgCost) {
		super.positionMulti(reqId, account, modelCode, contract, pos, avgCost);
	}

	@Override public void positionMultiEnd( int reqId) {
		super.positionMultiEnd(reqId);
	}
	
	// ---------------------------------------- Account Update Multi handling ----------------------------------------
	public void reqAccountUpdatesMulti( String account, String modelCode, boolean ledgerAndNLV, IAccountUpdateMultiHandler handler) {
		super.reqAccountUpdatesMulti(account, modelCode, ledgerAndNLV, handler);
	}

	public void cancelAccountUpdatesMulti( IAccountUpdateMultiHandler handler) {
		super.cancelAccountUpdatesMulti(handler);
	}

	@Override public void verifyMessageAPI( String apiData) {}
	@Override public void verifyCompleted( boolean isSuccessful, String errorText) {}
	@Override public void verifyAndAuthMessageAPI( String apiData, String xyzChallenge) {}
	@Override public void verifyAndAuthCompleted( boolean isSuccessful, String errorText) {}
	@Override public void displayGroupList(int reqId, String groups) {}
	@Override public void displayGroupUpdated(int reqId, String contractInfo) {}
	
	

	// ---------------------------------------- other methods ----------------------------------------
	public void reqSecDefOptParams( String underlyingSymbol, String futFopExchange, /*String currency,*/ String underlyingSecType, int underlyingConId, ISecDefOptParamsReqHandler handler) {
		super.reqSecDefOptParams(underlyingSymbol, futFopExchange, underlyingSecType, underlyingConId, handler);
	} 
	
	public void reqSoftDollarTiers(ISoftDollarTiersReqHandler handler) {
		super.reqSoftDollarTiers(handler);
	}

    public void reqFamilyCodes(IFamilyCodesHandler handler) {
        super.reqFamilyCodes(handler);
    }

    public void reqMatchingSymbols(String pattern, ISymbolSamplesHandler handler) {
        super.reqMatchingSymbols(pattern, handler);
    }

	public void reqMktDepthExchanges(IMktDepthExchangesHandler handler) {
		super.reqMktDepthExchanges(handler);
	}

	public void reqNewsTicks(Contract contract, ITickNewsHandler handler) {
		super.reqNewsTicks(contract, handler);
	}
	
	public void reqSmartComponents(String bboExchange, ISmartComponentsHandler handler) {
		super.reqSmartComponents(bboExchange, handler);
	}

	public void reqNewsProviders(INewsProvidersHandler handler) {
		super.reqNewsProviders(handler);
	}

	public void reqNewsArticle(String providerCode, String articleId, INewsArticleHandler handler) {
		super.reqNewsArticle(providerCode, articleId, handler);
	}

	public void reqHistoricalNews( int conId, String providerCodes, String startDateTime, String endDateTime, int totalResults, IHistoricalNewsHandler handler) {
		super.reqHistoricalNews(conId, providerCodes, startDateTime, endDateTime, totalResults, handler);
	}
	
	public void reqHeadTimestamp(Contract contract, WhatToShow whatToShow, boolean rthOnly, IHeadTimestampHandler handler) {
		super.reqHeadTimestamp(contract, whatToShow, rthOnly, handler);
	}
	
	
	public void reqHistogramData(Contract contract, int duration, DurationUnit durationUnit, boolean rthOnly, IHistogramDataHandler handler) {
		super.reqHistogramData(contract, duration, durationUnit, rthOnly, handler);
	}
	
    public void cancelHistogramData(IHistogramDataHandler handler) {
		super.cancelHistogramData(handler);
    }

	@Override public void rerouteMktDataReq(int reqId, int conId, String exchange) {
		super.rerouteMktDataReq(reqId, conId, exchange);
	}

	@Override public void rerouteMktDepthReq(int reqId, int conId, String exchange) {
		super.rerouteMktDepthReq(reqId, conId, exchange);
	}

    public void reqMarketRule(int marketRuleId, IMarketRuleHandler handler) {
	      super.reqMarketRule(marketRuleId, handler);
    }

	public void reqPnL(String account, String modelCode, IPnLHandler handler) {
	    super.reqPnL(account, modelCode, handler);
	}

	public void cancelPnL(IPnLHandler handler) {
	    super.cancelPnL(handler);
	}	

    public void reqPnLSingle(String account, String modelCode, int conId, IPnLSingleHandler handler) {
        super.reqPnLSingle(account, modelCode, conId, handler);
    }

    public void cancelPnLSingle(IPnLSingleHandler handler) {
       super.cancelPnLSingle(handler);
    }    

    public void reqHistoricalTicks(Contract contract, String startDateTime,
            String endDateTime, int numberOfTicks, String whatToShow, int useRth, boolean ignoreSize, IHistoricalTickHandler handler) {
       super.reqHistoricalTicks(contract, startDateTime, endDateTime, numberOfTicks, whatToShow, useRth, ignoreSize, handler);
    }   


    // ---------------- request types ----------
    public enum RequestType{
    	HistoricalData("HistoricalData"), TopMarketData("TopMktData"),RealTimeBar("RealTimeBar");
    	
    	private String type;
    	
    	RequestType(String name){
    		this.type = name;
    	}
    	//HistoricalBar;
    	
    	public String toString() {
    		return this.type;
    	}
    }

}
