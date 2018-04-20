package cgi.ib;

import java.io.Serializable;

import org.apache.log4j.Logger;

import com.ib.client.Contract;
import com.ib.client.Order;
import com.ib.client.OrderState;
import com.ib.client.OrderStatus;

import cgi.ib.MyAPIController.IOrderHandler;


public class MyIOrderHandler implements IOrderHandler,Serializable {
	private static Logger logger = Logger.getLogger(MyIOrderHandler.class.getName());

	
	public MyIOrderHandler () {
	}
	
	// call backs...
	@Override
	public void orderState(OrderState orderState) {
		
	}

	@Override
	public void orderStatus(OrderStatus status, double filled, double remaining, double avgFillPrice, long permId,
			int parentId, double lastFillPrice, int clientId, String whyHeld, double mktCapPrice) {

			
	}

	@Override
	public void handle(int errorCode, String errorMsg) {

	}

}
