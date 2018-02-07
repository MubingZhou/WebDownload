package webbDownload.southboundData;

public class Southbound_Main {

	public static void main(String[] args) {
		String rootPath = "Z:\\Mubing\\stock data\\HK CCASS - WEBB SITE\\southbound";
		String shPath = rootPath + "\\sh";
		String szPath = rootPath + "\\sz";
		String outputPath = rootPath + "\\combined";
		DataDownloader.FILE_OUTPUT_PATH = rootPath;
		
		String startDate = "2018-02-05";
		String endDate = startDate;//"2017-12-05";
		String dateFormat = "yyyy-MM-dd";
		
		DataDownloader.dataDownloader(startDate, endDate, dateFormat, true, true);
		
		DataCombiner.dataCombiner(shPath, szPath, outputPath);

	}

}
