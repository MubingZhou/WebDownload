package factorsDataCombiner;

public enum FactorPath {
	Southbound("Z:\\Mubing\\stock data\\HK CCASS - WEBB SITE\\southbound\\combined\\"),
	StockData("Z:\\Mubing\\stock data\\stock hist data - webb\\"),
	StockOutstanding("Z:\\Mubing\\stock data\\HK CCASS - WEBB SITE\\outstanding\\")
	;
	
	private String path;
	public String rootPath = "Z:\\Mubing\\stock data\\";
	
	FactorPath(String s){
		this.path = s;
	}
	
	public String toString() {
		return this.path;
	}
	
}
