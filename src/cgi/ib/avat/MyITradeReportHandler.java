package cgi.ib.avat;

import org.apache.log4j.Logger;

import com.ib.client.CommissionReport;
import com.ib.client.Contract;
import com.ib.client.Execution;
import com.ib.controller.ApiController.ITradeReportHandler;

public class MyITradeReportHandler implements ITradeReportHandler {
	Logger logger = Logger.getLogger(MyITradeReportHandler.class.getName());
	
	public MyITradeReportHandler() {
		
	}
	
	@Override
	public void tradeReport(String tradeKey, Contract contract, Execution execution) {
		// TODO Auto-generated method stub
		logger.info("[Trade Report - tradeReport] tradeKey=" + tradeKey + " contract=" + contract.symbol() 
			+ " execution.shares=" + execution.shares() + " execution.price=" + execution.price() 
			+ " execution.avgPrice=" + execution.avgPrice() + " execution.id=" + execution.execId() 
			+ " side=" + execution.side() +" time=" + execution.time());
	}

	@Override
	public void tradeReportEnd() {
		// TODO Auto-generated method stub
		logger.info("[Trade Report - end]");
	}

	@Override
	public void commissionReport(String tradeKey, CommissionReport commissionReport) {
		// TODO Auto-generated method stub
		logger.info("[Trade Report - commissionReport] tradeKey=" + tradeKey + " exe_id=" +  commissionReport.m_execId
				+ " m_commission=" + commissionReport.m_commission);
	}

}
