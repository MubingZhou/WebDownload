package cgi.ib.avat;

import java.io.Serializable;

import org.apache.log4j.Logger;

import com.ib.client.Contract;
import com.ib.client.Order;
import com.ib.client.OrderState;
import com.ib.client.OrderStatus;
import com.ib.controller.ApiController.IOrderHandler;

public class AvatIOrderHandler implements IOrderHandler,Serializable {
	private static Logger logger = Logger.getLogger(AvatIOrderHandler.class.getName());
	
	private int orderId = -1;
	public Contract contract;
	public Order order;
	
	// ------- some state variables -----
	public int errorCode = -1;
	public int isSubmitted = -1;
	
	public Double newLostSize = -1.0;  // error code = 461
	
	public boolean isTransmit = true;
	
	public AvatIOrderHandler (Contract con, Order order) {
		this.contract = con.clone();
		this.order = order;
	}
	
	public int getOrderId() {
		return orderId;
	}
	public void setOrderId(int orderId) {
		this.orderId = orderId;
		order.orderId(orderId);
	}
	
	// call backs...
	@Override
	public void orderState(OrderState orderState) {
		logger.trace("[MyIOrderHandler - orderState - " + this.contract.symbol() + "] " + orderState.getStatus() );
		
	}

	@Override
	public void orderStatus(OrderStatus status, double filled, double remaining, double avgFillPrice, long permId,
			int parentId, double lastFillPrice, int clientId, String whyHeld, double mktCapPrice) {
		logger.info("[MyIOrderHandler - orderStatus - " + this.contract.symbol() + "] " + status.toString());
		if(isTransmit && (status.equals(OrderStatus.Submitted) || status.equals(OrderStatus.PreSubmitted))) {
			isSubmitted = 1;
			//logger.info("         Confirm submitted! - isTransmit=True");
		}
			
		if(!isTransmit && status.equals(OrderStatus.PendingSubmit)) {
			isSubmitted = 1;
			//logger.info("         Confirm submitted! - isTransmit=false");
		}
			
	}

	@Override
	public void handle(int errorCode, String errorMsg) {
		logger.info("[MyIOrderHandler - handle - " + this.contract.symbol() + "] errCode=" + errorCode + " errMsg=" + errorMsg);
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
