package cgi.ib.avat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.ib.client.Contract;
import com.ib.client.Order;
import com.ib.client.OrderState;
import com.ib.client.OrderStatus;
import com.ib.controller.ApiController.ILiveOrderHandler;

public class MyILiveOrderHandler implements ILiveOrderHandler{
	private static Logger logger = Logger.getLogger(MyILiveOrderHandler.class);
	
	public boolean isEnd = false;
	
	public Map<Integer, ArrayList<Object>> orderContract = new HashMap<Integer, ArrayList<Object>>(); // 存储order和contract,其中key是orderId 
	public Map<Integer, ArrayList<Object>> orderStatus = new HashMap<Integer, ArrayList<Object>>();
	
	@Override
	public void openOrder(Contract contract, Order order, OrderState orderState) {
		logger.trace("[MyILiveOrderHandler - openOrder] con.symbol=" + contract.symbol() + " order.id=" + order.orderId());
		//logger.trace("[MyILiveOrderHandler - openOrder]");
		
		ArrayList<Object> a = new ArrayList<Object>();
		a.add(order);
		a.add(contract);
		
		orderContract.put(order.orderId(), a);
	}

	@Override
	public void openOrderEnd() {
		isEnd = true;
		logger.trace("[MyILiveOrderHandler - openOrderEnd]");
	}

	@Override
	public void orderStatus(int orderId, OrderStatus status, double filled, double remaining, double avgFillPrice,
			long permId, int parentId, double lastFillPrice, int clientId, String whyHeld, double mktCapPrice) {
		logger.trace("[MyILiveOrderHandler - orderStatus] orderId=" + orderId + " status=" + status.toString() + " filled=" + filled + " remain=" + remaining);
		
		ArrayList<Object> a = new ArrayList<Object>();
		a.add(orderId);
		a.add(status);
		a.add(filled);
		a.add(remaining);
		a.add(avgFillPrice);
		a.add(permId);
		a.add(parentId);
		a.add(lastFillPrice);
		a.add(clientId);
		a.add(whyHeld);
		a.add(mktCapPrice);
		
		orderStatus.put(orderId, a);
	}

	@Override
	public void handle(int orderId, int errorCode, String errorMsg) {
		logger.trace("[MyILiveOrderHandler - handle] orderId=" + orderId + " errorCode=" + errorCode + " errMsg=" + errorMsg); 
		
	}

}
