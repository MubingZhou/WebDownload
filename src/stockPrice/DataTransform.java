package stockPrice;

import java.io.BufferedReader;
import java.io.File;

public class DataTransform {
	public static String STOCK_DATA_PATH = DataGetter.STOCK_DATA_PATH;  //"Z:\\Mubing\\stock data\\stock hist data - webb\\";
	
	/**
	 * 将从David Webb上面下载的数据转换成以下格式：
	 * date,open,high,low,close,volume
	 * 如果当天股票停牌，则沿用上一个交易日的价格，volume=0
	 */
	public static void transformData() {
		try {
			File[] allFiles = new File(STOCK_DATA_PATH).listFiles();
			for(File f : allFiles) {
				String fileName = f.getName();
				if(fileName.substring(fileName.length()-4).equals(".csv") ) {
					boolean isStockFile = true;
					try {
						Double num = Double.parseDouble(fileName.substring(0, fileName.length()-4));
					}catch(Exception ee) {
						isStockFile = false;
					}
					
					if(!isStockFile)
						continue;
					
					BufferedReader bf = utils.Utils.readFile_returnBufferedReader(utils.Utils.addBackSlashToPath(STOCK_DATA_PATH) + fileName);
					String line = "";
					int lineC  =0;
					while((line = bf.readLine()) != null) {
						if(lineC == 0) {
							lineC ++;
							continue;
						}
						String[] lineArr = line.split(",");
						String dateStr = lineArr[0];
						String openStr = lineArr[1];
						String highStr = lineArr[1];
						String lowStr = lineArr[1];
						String closeStr = lineArr[1];
						String volumeStr = lineArr[1];
						String suspStr = lineArr[1];
					}
				
				}
			}
			
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	
}
