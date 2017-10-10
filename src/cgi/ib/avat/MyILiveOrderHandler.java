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
	private static Logger logger = Logger.getLogger(MyILiveOrderHandler.class.getName());
	
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
		a.add(orderId);  	// 0
		a.add(status);  	// 1
		a.add(filled);		// 2	
		a.add(remaining);	// 3	
		a.add(avgFillPrice);// 4
		a.add(permId);		// 5
		a.add(parentId);	// 6
		a.add(lastFillPrice);//7
		a.add(clientId);	// 8
		a.add(whyHeld);		// 9
		a.add(mktCapPrice); // 10
		
		orderStatus.put(orderId, a);
	}

	@Override
	public void handle(int orderId, int errorCode, String errorMsg) {
		logger.trace("[MyILiveOrderHandler - handle] orderId=" + orderId + " errorCode=" + errorCode + " errMsg=" + errorMsg); 
		
	}
	
	public String toString() {
		String s = "";
		for(Integer orderId : orderContract.keySet()) {
			ArrayList<Object> thisOrderContract = orderContract.get(orderId);
			Order order = (Order) thisOrderContract .get(0);
			Contract contract = (Contract) thisOrderContract .get(1);
			
			ArrayList<Object> thisOrderStatus = orderStatus.get(orderId);
			
			s += "stock=" + contract.symbol() + " " 
				+ "orderId=" + order.orderId() + " " 
				+ "orderStatus=" + ((OrderStatus) thisOrderStatus.get(1)).toString() + " " 
				+ "orderAction=" + order.action() + " " 
				+ "orderLmtPrice=" + order.lmtPrice() + " " 
				+ "orderQty=" + order.totalQuantity() + " " 
				+ "filledQty=" + (Double) thisOrderStatus.get(2) + " " 
				+ "remainingQty=" + (Double) thisOrderStatus.get(3) + " " 
				+ "avgFillPrice=" + (Double) thisOrderStatus.get(4) + " " 
				+ "\n";
		}
		return s;
	}

}
