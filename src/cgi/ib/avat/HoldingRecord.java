package cgi.ib.avat;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.ib.client.Contract;
import com.ib.client.OrderStatus;


public class HoldingRecord  implements Serializable {
	public String stockCode;
	public Contract contract;
	
	public long orderTimeStamp = 0;
	public Double orderPrice = 0.0;
	public Double orderQty = 0.0;
	public int orderId = -1;
	
	// buy order成交了多少
	public long filledTimeStamp = 0;
	public Double avgFillPrice = 0.0;
	public Double lastFillPrice = 0.0;
	public Double filledQty = 0.0;
	public ArrayList<String> executionIdArr = new ArrayList<String>();
	
	// sell order的情况
	public Double sellOrderQty = 0.0;
	public Double sellOrderFilled = 0.0;
	
	public OrderStatus status = OrderStatus .Submitted;

	public Double tradingCost = 0.0;
	public boolean isFilled = false;  // 是否完全fill
	public boolean isCancelled = false;
	
	// buy signals
	public int buyCond2_1 = 0;
	public int buyCond2_2 = 0;
	public int buyCond2_3 = 0;
	public String buyReason = "";
	
	// order handler 
	public MyIOrderHandler buyOrderHanlder = null;
	public MyIOrderHandler sellOrderHanlder = null;
	//public MyIOrderHandler myOrderH2 = null;
	
	/*
	public HoldingRecord(String stockCode, Contract contract, long orderTimeStamp, Double orderPrice, Double orderQty) {
		this.stockCode = stockCode;
		this.contract = contract;
		this.orderTimeStamp = orderTimeStamp;
		this.orderPrice = orderPrice;
		this.orderQty = orderQty;
	}
	*/
	
	public HoldingRecord(MyIOrderHandler handler, long orderTimeStamp) {
		this.buyOrderHanlder = handler;
		this.stockCode = handler.contract.symbol();
		this.contract = handler.contract;
		this.orderTimeStamp = orderTimeStamp;
		this.orderPrice = handler.order.lmtPrice();
		this.orderQty = handler.order.totalQuantity();
		this.orderId = handler.getOrderId();
	}
	
	public HoldingRecord() {
		
	}
	
	public String toString() {
		SimpleDateFormat sdf = new SimpleDateFormat ("yyyyMMdd HH:mm:ss"); 
		String s = stockCode + "," + sdf.format(new Date(orderTimeStamp)) + "," + buyOrderHanlder.order.action() + ","
				+ orderPrice + "," + orderQty + "," + orderId + "," + status.toString() + "," + buyReason;
		
		return s;
	}
	
	/**
	 * 将holding record转换为逗号分隔符的形式的字符串
	 * @return
	 */
	public String toSaveToString() {
		return stockCode + "," 
				+ orderTimeStamp + "," + orderPrice + "," + orderQty + "," + orderId + ","
				+ filledTimeStamp + "," + avgFillPrice + "," + lastFillPrice + "," + filledQty + ","
				+ buyCond2_1 + "," + buyCond2_2 + "," + buyCond2_3 + "," + buyReason;   // 共13项
 	}
	
	public void recoverFromString(String s) {
		recoverFromString(new ArrayList<String> (Arrays.asList(s.split(","))));
	}
	public void recoverFromString(ArrayList<String> sArr) {
		if(sArr.size() != 13)
			System.out.println("[HoldingRecord - recoverFromString] string size not correct!");
		
		try {
			this.stockCode = sArr.get(0);
			this.orderTimeStamp = Long.parseLong(sArr.get(1));
			this.orderPrice = Double.parseDouble(sArr.get(2));
			this.orderQty = Double.parseDouble(sArr.get(3));
			this.orderId = Integer.parseInt(sArr.get(4));
			this.filledTimeStamp = Long.parseLong(sArr.get(5));
			this.avgFillPrice = Double.parseDouble(sArr.get(6));
			this.lastFillPrice = Double.parseDouble(sArr.get(7));
			this.filledQty = Double.parseDouble(sArr.get(8));
			this.buyCond2_1 = Integer.parseInt(sArr.get(9));
			this.buyCond2_2 = Integer.parseInt(sArr.get(10));
			this.buyCond2_3 = Integer.parseInt(sArr.get(11));
			this.buyReason = sArr.get(11);
		}catch(Exception e) {
			System.out.println("[HoldingRecord - recoverFromString] Unknown error!");
			e.printStackTrace();
		}
	}
}
