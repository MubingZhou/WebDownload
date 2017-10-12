package cgi.ib.avat;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.ib.client.CommissionReport;
import com.ib.client.Contract;
import com.ib.client.Execution;
import com.ib.controller.ApiController.ITradeReportHandler;

public class MyITradeReportHandler implements ITradeReportHandler {
	Logger logger = Logger.getLogger(MyITradeReportHandler.class.getName());
	
	public int isEnd = 0;
	public int isCalledByMonitor = 0;
	
	public String executionRecordPath = "";
	private FileWriter fileWriter;
	
	public ArrayList<ArrayList<Object>> tradeReportArr = new ArrayList<ArrayList<Object>>();
	public ArrayList<ArrayList<Object>> commissionReportArr = new ArrayList<ArrayList<Object>>();
	
	
	public MyITradeReportHandler(String executionRecordPath) {
		this.executionRecordPath = executionRecordPath;
		
		try {
			fileWriter = new FileWriter(executionRecordPath, true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void tradeReport(String tradeKey, Contract contract, Execution execution) {
		String info = "[Trade Report - tradeReport] stock=" + contract.symbol() 
		+ " execution.shares=" + execution.shares() + " execution.price=" + execution.price() 
		+ " execution.avgPrice=" + execution.avgPrice() + " execution.id=" + execution.execId() 
		+ " side=" + execution.side() +" time=" + execution.time();
		
		
		
		try {
			if(isCalledByMonitor == 1) {
				logger.info(info);
				
				fileWriter.write(info + "\n");
				fileWriter.flush();
			}
		} catch (Exception e) {
			
			e.printStackTrace();
		}
		
		ArrayList<Object> thisRec = new ArrayList<Object>();
		thisRec.add(tradeKey);
		thisRec.add(contract);
		thisRec.add(execution);
		
		tradeReportArr.add(thisRec);
	}

	@Override
	public void tradeReportEnd() {
		String info = "[Trade Report - end]";

		try {
			if(isCalledByMonitor == 1) {
				logger.info(info);
				
				fileWriter.write(info + "\n");
				fileWriter.flush();
			}
			
		} catch (Exception e) {
			
			e.printStackTrace();
		}
		
		isEnd = 1;
	}

	@Override
	public void commissionReport(String tradeKey, CommissionReport commissionReport) {
		String info = "[Trade Report - commissionReport] tradeKey=" + tradeKey + " exe_id=" +  commissionReport.m_execId
				+ " m_commission=" + commissionReport.m_commission;
		
		
		
		try {
			if(isCalledByMonitor == 1) {
				logger.info(info);
				
				fileWriter.write(info + "\n");
				fileWriter.flush();
			}
			
		} catch (Exception e) {
			
			e.printStackTrace();
		}
		
		ArrayList<Object> thisRec = new ArrayList<Object>();
		thisRec.add(tradeKey);
		thisRec.add(commissionReport);
		commissionReportArr.add(thisRec);
	}
	
	public void initialize() {
		isEnd = 0;
		isCalledByMonitor = 0;
		
		tradeReportArr = new ArrayList<ArrayList<Object>>();
		commissionReportArr = new ArrayList<ArrayList<Object>>();
	}

}
