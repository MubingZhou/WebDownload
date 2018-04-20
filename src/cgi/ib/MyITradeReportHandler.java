package cgi.ib;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.ib.client.CommissionReport;
import com.ib.client.Contract;
import com.ib.client.Execution;

import cgi.ib.MyAPIController.ITradeReportHandler;


public class MyITradeReportHandler implements ITradeReportHandler {
	Logger logger = Logger.getLogger(MyITradeReportHandler.class.getName());
	

	public MyITradeReportHandler(String executionRecordPath) {
		try {

		} catch (Exception e) {
			e.printStackTrace();

		}
	}
	
	@Override
	public void tradeReport(String tradeKey, Contract contract, Execution execution) {

		try {

			
		} catch (Exception e) {
			
			e.printStackTrace();
		}
		

	}

	@Override
	public void tradeReportEnd() {
		try {

		} catch (Exception e) {
			
			e.printStackTrace();
		}

	}

	@Override
	public void commissionReport(String tradeKey, CommissionReport commissionReport) {
		try {
			
		} catch (Exception e) {
			
			e.printStackTrace();
		}
		
	}
	
	public void initialize() {

	}

}
