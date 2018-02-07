package strategy.db_southboundFlowPortfolio;

import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Map;

import org.apache.log4j.Logger;

public class FeatureCalculator {
	public static Logger logger = Logger.getLogger(FeatureCalculator.class);
	
	public static String SOUTHBOUND_DATA_PATH = utils.PathConifiguration.SOUTHBOUND_DATA_PATH;
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String southboundOutputPath = utils.PathConifiguration.STOCK_DATA_ROOT_PATH + "\\HK CCASS - WEBB SITE\\southbound\\by stock\\";
		getSBHoldings_byStock(SOUTHBOUND_DATA_PATH, southboundOutputPath);
	}
	
	/**
	 * 目前southbound的数据是按照天来存到，即一个文件存储了某一天所有股票的southbound holding。这个函数将数据转为按股票存，即一个文件存储了一只股票所有天数的southbound holding
	 * @param dataRootPath   southbound holding数据的根目录
	 * @param outputRootPath  输出的根目录
	 */
	public static void getSBHoldings_byStock(String dataRootPath, String outputRootPath) {
		logger.info("===================== getSBHoldings_byStock =====================");
		try {
			logger.info("----------- get southbound data map ---------");
			Map<String, Map<Date,ArrayList<Double>>> sbDataMap = PortfolioScreening.getAllSbData_return(dataRootPath);
			logger.info("----------- get southbound data map END ---------");
			
			logger.info("----------- output southbound data by stock ---------");
			SimpleDateFormat sdf_out = new SimpleDateFormat("dd/MM/yyyy"); 
			
			int count = 0;
			for(String stock : sbDataMap.keySet()) {
				count ++;
				
				Map<Date,ArrayList<Double>> thisStockSBData = sbDataMap.get(stock);
				
				ArrayList<Date> dateArr = new ArrayList<Date>(thisStockSBData.keySet());
				Collections.sort(dateArr);  //从最旧到最新
				
				FileWriter fw = new FileWriter(utils.Utils.addBackSlashToPath(outputRootPath) + stock + ".csv");
				fw.write("date,holding shares,holding value\n");
				int dateArrSize = dateArr.size();
				for(int j = 0; j < dateArrSize; j++	) {
					Date date = dateArr.get(j);
					
					ArrayList<Double> data = thisStockSBData.get(date);
					Double holdingShares = data.get(0);
					Double holdingValue = data.get(1);
					
					fw.write(sdf_out.format(date) + "," + holdingShares + "," + holdingValue + "\n");
				}
				fw.close();
				
				if(Math.floorMod(count, 50) == 0)
					logger.info("    " + (int) Math.floor(count / 50) * 50 + " stocks done!");
			}
			logger.info("----------- output southbound data by stock END ---------");
			
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		logger.info("===================== getSBHoldings_byStock END =====================");
	}
	
	public static void getHSI_HSCEI_Members(Date cutoffDate) {
		try {
			ArrayList<Date> allTradingDate = utils.Utils.getAllTradingDate();
			
			//数据最早从2010/1/1开始
			Date earliestDate = new SimpleDateFormat("yyyyMMdd").parse("20100104");
			int startInd = allTradingDate.indexOf(earliestDate);
			int endInd = allTradingDate.indexOf(utils.Utils.getMostRecentDate(cutoffDate, allTradingDate));
			
			for(int i = startInd; i < endInd; i++) {
				
			}
			
			
			
			
		}catch(Exception e) {
			e.printStackTrace();
		}
	}

}
