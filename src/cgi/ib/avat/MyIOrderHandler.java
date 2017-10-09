package cgi.ib.avat;

import org.apache.log4j.Logger;

import com.ib.client.Contract;
import com.ib.client.Order;
import com.ib.client.OrderState;
import com.ib.client.OrderStatus;
import com.ib.controller.ApiController.IOrderHandler;

public class MyIOrderHandler implements IOrderHandler {
	private static Logger logger = Logger.getLogger(MyIOrderHandler.class.getName());
	
	public int orderId = -1;
	public Contract contract;
	public Order order;
	
	// ------- some state variables -----
	public int errorCode = -1;
	public int isSubmitted = -1;
	
	public Double newLostSize = -1.0;  // error code = 461
	
	
	public MyIOrderHandler (Contract con, Order order) {
		this.contract = con.clone();
		this.order = order;
	}
	
	// call backs...
	@Override
	public void orderState(OrderState orderState) {
		logger.trace("[MyIOrderHandler - orderStatus] " + orderState.toString());
		
	}

	@Override
	public void orderStatus(OrderStatus status, double filled, double remaining, double avgFillPrice, long permId,
			int parentId, double lastFillPrice, int clientId, String whyHeld, double mktCapPrice) {
		logger.trace("[MyIOrderHandler - orderStatus] " + status.toString());
		if(status.equals(OrderStatus.Submitted))
			isSubmitted = 1;
		
	}

	@Override
	public void handle(int errorCode, String errorMsg) {
		logger.trace("[MyIOrderHandler - handle] errCode=" + errorCode + " errMsg=" + errorMsg);
		this.errorCode = errorCode;
		
		if(errorCode == 461) {  
			/*
			 *  lot size not correct, a sample error msg is :
			 *  BUY 166,600 1 SEHK @ 300.00:
				Order size 166,600 is not correct; it should be a multiple of 500
			 */
			String[] lineArr = errorMsg.split(" ");
			newLostSize = Double.parseDouble(lineArr[lineArr.length - 1]);
			logger.trace("[MyIOrderHandler - handle] new lot size = " + newLostSize);
		}
	}

}
