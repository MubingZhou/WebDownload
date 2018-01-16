package utils;

public class PathConifiguration {
	public static String STOCK_DATA_ROOT_PATH = "Z:\\Mubing\\stock data";
	public static String ALL_TRADING_DATE_PATH_HK = STOCK_DATA_ROOT_PATH + "\\all trading date - hk.csv" ;
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
