package factorsDataCombiner;

public class DataCombiner {
	public static String rootPath = "Z:\\Mubing\\stock data\\";
	
	enum FactorPath {
		Southbound("");
		
		String rootPath = DataCombiner.rootPath;
		String path;
		FactorPath(String s) {
			this.path = s;
		}
		
	}
	
}
