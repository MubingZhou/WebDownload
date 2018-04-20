package cgi.ib;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.apache.log4j.Logger;

import com.ib.client.Contract;
import com.ib.client.TickAttr;
import com.ib.client.TickType;

import cgi.ib.MyAPIController.ITopMktDataHandler;


public class MyITopMktDataHandler implements ITopMktDataHandler{
	private static Logger logger = Logger.getLogger(MyITopMktDataHandler.class.getName());
	
	
	public MyITopMktDataHandler(Contract contract, String OUTPUT_ROOT_PATH, String todayDate) {
		super();
		
		try {

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void tickPrice(TickType tickType, double price, TickAttr attribs) {
		try {
	
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	@Override
	public void tickSize(TickType tickType, int size) {	
		try {

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void tickString(TickType tickType, String value) {
		try {
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	@Override
	public void tickSnapshotEnd() {
		
		try {

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	@Override
	public void marketDataType(int marketDataType) {
		
		try {

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void tickReqParams(int tickerId, double minTick, String bboExchange, int snapshotPermissions) {		
		try {

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
