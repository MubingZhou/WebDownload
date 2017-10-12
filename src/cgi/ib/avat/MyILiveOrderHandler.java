package cgi.ib.avat;

import java.io.FileWriter;
import java.io.IOException;
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
	public String liveOrderRecPath = "";
	
	private boolean toWrite = false;
	FileWriter fw;
	
	//public Map<Integer, ArrayList<Object>> orderContract = new HashMap<Integer, ArrayList<Object>>(); // 存储order和contract,其中key是orderId 
	//public Map<Integer, ArrayList<Object>> orderStatus = new HashMap<Integer, ArrayList<Object>>();
	
	public ArrayList<ArrayList<Object>> openOrderArr = new ArrayList<ArrayList<Object>>();
	public ArrayList<ArrayList<Object>> orderStatusArr = new ArrayList<ArrayList<Object>>();
	
	public MyILiveOrderHandler(String path) {
		this.liveOrderRecPath = path;
		
		try {
			fw = new FileWriter(liveOrderRecPath, true);
			toWrite = true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.info("[MyILiveOrderHandler - FileWriter Creation failed!]");
		}
	}
	public MyILiveOrderHandler() {
		 toWrite = false;
	}
	
	@Override
	public void openOrder(Contract contract, Order order, OrderState orderState) {
		try {
			String info = "[MyILiveOrderHandler - openOrder] con.symbol=" + contract.symbol() + " order.id=" + order.orderId();
			logger.info(info);
			if(toWrite) {
				fw.write(info + "\n");
				fw.flush();
			}
			//logger.trace("[MyILiveOrderHandler - openOrder]");
			
			ArrayList<Object> a = new ArrayList<Object>();
			a.add(contract);
			a.add(order);
			a.add(orderState);
			
			openOrderArr.add(a);
		}catch(Exception e) {
			e.printStackTrace();
		}
		
	}

	@Override
	public void openOrderEnd() {
		try {
			isEnd = true;
			String info = "[MyILiveOrderHandler - openOrderEnd]";
			logger.info(info);
			if(toWrite) {
				fw.write(info + "\n");
				fw.flush();
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
		
	}

	@Override
	public void orderStatus(int orderId, OrderStatus status, double filled, double remaining, double avgFillPrice,
			long permId, int parentId, double lastFillPrice, int clientId, String whyHeld, double mktCapPrice) {
		try {
			String info = "[MyILiveOrderHandler - orderStatus] orderId=" + orderId + " status=" + status.toString() + " filled=" + filled + " remain=" + remaining;
			
			logger.info(info);
			if(toWrite) {
				fw.write(info + "\n");
				fw.flush();
			}
			
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
			
			orderStatusArr.add(a);
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		
	}

	@Override
	public void handle(int orderId, int errorCode, String errorMsg) {
		try {
			String info = "[MyILiveOrderHandler - handle] orderId=" + orderId + " errorCode=" + errorCode + " errMsg=" + errorMsg;
			logger.info(info); 
			
			if(toWrite) {
				fw.write(info + "\n");
				fw.flush();
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public String toString() {
		String s = "";
		for(int i = 0; i < openOrderArr.size(); i++) {
			ArrayList<Object> thisOrderContract = openOrderArr.get(i);
			Order order = (Order) thisOrderContract .get(0);
			Contract contract = (Contract) thisOrderContract .get(1);
			
			ArrayList<Object> thisOrderStatus = orderStatusArr.get(i);
			
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
