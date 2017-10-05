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
	
	public MyIOrderHandler (Contract con, Order order) {
		
	}
	
	// call backs...
	@Override
	public void orderState(OrderState orderState) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void orderStatus(OrderStatus status, double filled, double remaining, double avgFillPrice, long permId,
			int parentId, double lastFillPrice, int clientId, String whyHeld, double mktCapPrice) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void handle(int errorCode, String errorMsg) {
		// TODO Auto-generated method stub
		
	}

}
