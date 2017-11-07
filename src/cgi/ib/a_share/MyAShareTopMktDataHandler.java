package cgi.ib.a_share;

import org.apache.log4j.Logger;

import com.ib.client.Contract;
import com.ib.client.TickAttr;
import com.ib.client.TickType;
import com.ib.controller.ApiController.ITopMktDataHandler;

public class MyAShareTopMktDataHandler implements ITopMktDataHandler{
	private static Logger logger = Logger.getLogger(MyAShareTopMktDataHandler.class);
	private static String msgHeader = "[A Share Top Mkt Handler]";
	
	public Contract contract;
	public MyAShareTopMktDataHandler(Contract con) {
		contract = con.clone();
		
	}
	
	@Override
	public void tickPrice(TickType tickType, double price, TickAttr attribs) {
		logger.trace(msgHeader + " tickPrice: tickType=" + tickType + " price=" + price);
		
	}

	@Override
	public void tickSize(TickType tickType, int size) {
		logger.trace(msgHeader + " tickSize: tickType=" + tickType + " price=" + size);
		
	}

	@Override
	public void tickString(TickType tickType, String value) {
		logger.trace(msgHeader + " tickString: tickType=" + tickType + " price=" + value);
		
	}

	@Override
	public void tickSnapshotEnd() {
		// TODO Auto-generated method stub
		logger.info("tickSnapshotEnd!");
	}

	@Override
	public void marketDataType(int marketDataType) {
		logger.trace(msgHeader + " marketDataType: " + marketDataType);
		
	}

	@Override
	public void tickReqParams(int tickerId, double minTick, String bboExchange, int snapshotPermissions) {
		logger.trace(msgHeader + " tickReqParams: tickerId=" + tickerId + " minTick=" + minTick + " bboExchange=" + bboExchange + " snapshotPermissions=" + snapshotPermissions);
		
	}

}
