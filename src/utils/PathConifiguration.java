package utils;

public class PathConifiguration {
	public static String STOCK_DATA_ROOT_PATH = "T:\\Mubing\\stock data";
	public static String ALL_TRADING_DATE_PATH_HK = STOCK_DATA_ROOT_PATH + "\\all trading date - hk.csv" ;   // trading date for HK market
	
	public static String ALL_MMA_TRADING_DATE_PATH = STOCK_DATA_ROOT_PATH + "\\all MMA trading date - hk.csv" ;  
		// trading date for MMA. More specifically, it is the CCASS date for MMA.
		// e.g. for 2018 Spring Festival, HK trading date is 12/2, 13/2, 14/2, 15/2, 20/2, 21/2, 22/2, 23/2, 26/2 (24/2, 25/2 are in weekend)
		// Trading dates for Southbound investor are 8/2, 9/2, (10/2, 11/2 weekend) 12/2, 22/2, 23/2, 26/2
		// since CCASS is on T+2 basis, the CCASS date should be 12/2, 13/2, 14/2, 26/2...
	
	public static String STOCK_PRICE_PATH = STOCK_DATA_ROOT_PATH + "\\stock hist data - webb";
	public static String SOUTHBOUND_DATA_PATH = STOCK_DATA_ROOT_PATH + "\\HK CCASS - WEBB SITE\\southbound\\combined";
	
	public static String HK_STOCK_OUTSTANDING_DATA = STOCK_DATA_ROOT_PATH + "\\HK CCASS - WEBB SITE\\outstanding";
	
	public void updatePathStr(String new_STOCK_DATA_ROOT_PATH) {
		STOCK_DATA_ROOT_PATH = new_STOCK_DATA_ROOT_PATH;
		updatePathStr();
	}
	public void updatePathStr() {
		ALL_TRADING_DATE_PATH_HK = STOCK_DATA_ROOT_PATH + "\\all trading date - hk.csv" ;
		STOCK_PRICE_PATH = STOCK_DATA_ROOT_PATH + "\\stock hist data - webb";
		SOUTHBOUND_DATA_PATH = STOCK_DATA_ROOT_PATH + "\\HK CCASS - WEBB SITE\\southbound\\combined";
	}
	
	public static String getStockPriceDataPath(String stock) {
		return STOCK_PRICE_PATH + "\\" + stock + ".csv";
	}
}
