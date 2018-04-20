package cgi.ib.avat;

import java.util.List;

import org.apache.log4j.Logger;

import com.ib.controller.ApiController.IConnectionHandler;

public class AvatIConnectionHandler implements IConnectionHandler{
	public static Logger logger = Logger.getLogger(AvatIConnectionHandler.class.getName());
	
	@Override
	public void connected() {
		// TODO Auto-generated method stub
		logger.info("[MyIConnectionHandler - connected] connected");
	}

	@Override
	public void disconnected() {
		// TODO Auto-generated method stub
		logger.info("[MyIConnectionHandler - disconnected] disconnected");
	}

	@Override
	public void accountList(List<String> list) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void error(Exception e) {
		// TODO Auto-generated method stub
		e.printStackTrace();
	}

	@Override
	public void message(int id, int errorCode, String errorMsg) {
		// TODO Auto-generated method stub
		if(id != -1)
			logger.info("[MyIConnectionHandler - message] id=" + id + " errorCode=" + errorCode + " errorMsg=" + errorMsg);
		if(errorCode == 162) { // historical data downloader error: such stock may be suspended trading that date
			
		}
	}

	@Override
	public void show(String string) {
		// TODO Auto-generated method stub
		logger.info("[MyIConnectionHandler - show] " + string);
	}

}
