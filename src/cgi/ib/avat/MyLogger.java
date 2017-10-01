package cgi.ib.avat;

import org.apache.log4j.Logger;

import com.ib.controller.ApiConnection.ILogger;

public class MyLogger implements ILogger{

	@Override
	public void log(String valueOf) {
		Logger logger = Logger.getLogger(this.getClass().getName());
		logger.trace(valueOf);
	}

}
