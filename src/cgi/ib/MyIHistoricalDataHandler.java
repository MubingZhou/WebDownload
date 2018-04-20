package cgi.ib;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.log4j.Logger;

import com.ib.client.Contract;
import com.ib.controller.Bar;

import cgi.ib.MyAPIController.IHistoricalDataHandler;

public class MyIHistoricalDataHandler implements IHistoricalDataHandler{
	private static Logger logger = Logger.getLogger(MyIHistoricalDataHandler.class.getName());

	
	public MyIHistoricalDataHandler() {	
		try {

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	

	@Override
	public void historicalData(Bar bar) {
		try {

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	@Override
	public void historicalDataEnd() {
		try {
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
