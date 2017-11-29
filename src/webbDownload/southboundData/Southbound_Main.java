package webbDownload.southboundData;

public class Southbound_Main {

	public static void main(String[] args) {
		String shPath = "D:\\stock data\\HK CCASS - WEBB SITE\\southbound\\sh";
		String szPath = "D:\\stock data\\HK CCASS - WEBB SITE\\southbound\\sz";
		String outputPath = "D:\\stock data\\HK CCASS - WEBB SITE\\southbound\\combined";
		
		String startDate = "2017-11-27";
		String endDate = startDate;
		String dateFormat = "yyyy-MM-dd";
		
		DataDownloader.dataDownloader(startDate, endDate, dateFormat, true, true);
		
		DataCombiner.dataCombiner(shPath, szPath, outputPath);

	}

}
