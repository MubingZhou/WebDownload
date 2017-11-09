package webbDownload.southboundData;

public class Southbound_Main {

	public static void main(String[] args) {
		String shPath = "D:\\stock data\\HK CCASS - WEBB SITE\\southbound\\sh";
		String szPath = "D:\\stock data\\HK CCASS - WEBB SITE\\southbound\\sz";
		String outputPath = "D:\\stock data\\HK CCASS - WEBB SITE\\southbound\\combined";
		
		DataDownloader.dataDownloader();
		
		//DataCombiner.dataCombiner(shPath, szPath, outputPath);

	}

}
