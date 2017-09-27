package cgi.ib;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;

import com.ib.controller.ApiConnection.ILogger;
import com.ib.controller.ApiController.IAccountHandler;
import com.ib.controller.ApiController.IConnectionHandler;
import com.ib.controller.ApiController.IEfpHandler;
//import com.ib.controller.ApiController.IInternalHandler;
import com.ib.controller.ApiController.ILiveOrderHandler;
import com.ib.controller.ApiController.IOptHandler;
import com.ib.controller.ApiController.IOrderHandler;
import com.ib.controller.ApiController.ITopMktDataHandler;
import com.ib.client.Contract;
import com.ib.client.MarketDataType;
import com.ib.client.TickAttr;
import com.ib.client.TickType;
import com.ib.controller.ApiController;
import com.ib.controller.Bar;
import com.ib.controller.Position;

public class MyAPIController extends ApiController{
	ArrayList<MyITopMktDataHandler> topMktDataHandlerArr = new ArrayList<MyITopMktDataHandler>();
	
	
	// ---------------------------------------- Constructor and Connection handling ----------------------------------------
	public MyAPIController( IConnectionHandler handler, ILogger inLogger, ILogger outLogger) {
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
		System.out.println("MyAPIConnection: " + errorMsg);
	}

	@Override public void connectionClosed() {
		super.connectionClosed();
		
		System.out.println("[MyAPIController] Closed!!");
	}
	
	// ---------------------------------------- Account and portfolio updates ----------------------------------------
	public class MyIAccountHandler implements IAccountHandler{

		@Override
		public void accountValue(String account, String key, String value, String currency) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void accountTime(String timeStamp) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void accountDownloadEnd(String account) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void updatePortfolio(Position position) {
			// TODO Auto-generated method stub
			
		}
		
	}
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
	
	// ---------------------------------------- Top Market Data handling ----------------------------------------
	/*	
	public interface IEfpHandler extends ITopMktDataHandler {
		void tickEFP(int tickType, double basisPoints, String formattedBasisPoints, double impliedFuture, int holdDays, String futureLastTradeDate, double dividendImpact, double dividendsToLastTradeDate);
	}

	public interface IOptHandler extends ITopMktDataHandler {
		void tickOptionComputation( TickType tickType, double impliedVol, double delta, double optPrice, double pvDividend, double gamma, double vega, double theta, double undPrice);
	}
	*/
	
	public static class TopMktDataAdapter implements ITopMktDataHandler {
		@Override public void tickPrice(TickType tickType, double price, TickAttr attribs) {
		}
		@Override public void tickSize(TickType tickType, int size) {
		}
		@Override public void tickString(TickType tickType, String value) {
		}
		@Override public void tickSnapshotEnd() {
		}
		@Override public void marketDataType(int marketDataType) {
		}
		@Override public void tickReqParams(int tickerId, double minTick, String bboExchange, int snapshotPermissions) {
		}
	}

    public void reqTopMktData(Contract contract, String genericTickList, boolean snapshot, boolean regulatorySnapshot, ITopMktDataHandler handler) {
		super.reqTopMktData(contract, genericTickList, snapshot, regulatorySnapshot, handler);
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

	
}
