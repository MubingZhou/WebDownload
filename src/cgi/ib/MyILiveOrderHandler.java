package cgi.ib;

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

import cgi.ib.MyAPIController.ILiveOrderHandler;


public class MyILiveOrderHandler implements ILiveOrderHandler{
	private static Logger logger = Logger.getLogger(MyILiveOrderHandler.class.getName());
	
	public MyILiveOrderHandler() {
		 
	}
	
	@Override
	public void openOrder(Contract contract, Order order, OrderState orderState) {
		try {

		}catch(Exception e) {
			e.printStackTrace();
		}
		
	}

	@Override
	public void openOrderEnd() {
		try {

		}catch(Exception e) {
			e.printStackTrace();
		}
		
	}

	@Override
	public void orderStatus(int orderId, OrderStatus status, double filled, double remaining, double avgFillPrice,
			long permId, int parentId, double lastFillPrice, int clientId, String whyHeld, double mktCapPrice) {
		try {

		}catch(Exception e) {
			e.printStackTrace();
		}
		
		
	}

	@Override
	public void handle(int orderId, int errorCode, String errorMsg) {
		try {

		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	


}
