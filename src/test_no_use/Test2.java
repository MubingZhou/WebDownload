package test_no_use;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Stack;

public class Test2 {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			int num = 15;
			String ticker = "700.HK";
			Double quota = 6.5;
			
			ArrayList<String> tickerArr = new ArrayList<String>();
			Stack<String> unusedTicker = new Stack<String>();  // to hold un-used tickers, a stack
			ArrayList<String> holdingTicker = new ArrayList<String>();  // to hold on-hand tickers, a queue
			
			for(int i=1; i <= num; i++) {
				tickerArr.add(ticker+"_"+i);
				unusedTicker.push(ticker+"_"+(num-i+1));
			}
			System.out.println("tickerSt end=" + unusedTicker.get(unusedTicker.size()-1));
			
			String filePath="T:\\test_data\\tencent_backtest_cumsig_10.csv";
			BufferedReader bf = utils.Utils.readFile_returnBufferedReader(filePath);
			
			String dateLine = bf.readLine();
			String sigLine = bf.readLine();
			String[] dateArrStr = dateLine.split(",");
			String[] sigArrStr = sigLine.split(",");
			
			ArrayList<Double> posArr = new ArrayList<Double>();
			ArrayList<String> ulyArr = new ArrayList<String>(); // underlying ticker
			
			Double cumPos = 0.0;
			for(int i = 0; i < dateArrStr.length; i++) {
				System.out.println("i=" + i + " date=" + dateArrStr[i]);
				String sig = sigArrStr[i];
				
				if(sig.equals("1")) { // buy signal
					String t = unusedTicker.pop();  // pop from unused ticker stack
					holdingTicker.add(t); // queue in holding ticker
					
					ulyArr.add(t);
					cumPos += quota;
					posArr.add(cumPos);
				}
				if(sig.equals("-1")) { // sell
					String t = holdingTicker.get(0);
					holdingTicker.remove(0);
					unusedTicker.push(t);
					
					ulyArr.add(t);
					cumPos -= quota;
					posArr.add(cumPos);
				}
			}
			
			// get data to writer
			String posLine = "";
			String ulyLine = "";
			for(int i = 0; i < dateArrStr.length; i++) {
				if(i != 0) {
					posLine += ",";
					ulyLine += ",";
				}
				posLine += String.valueOf(posArr.get(i));
				ulyLine += String.valueOf(ulyArr.get(i));
			}
			
			String outputPath = "T:\\test_data\\tencent_backest_ab_10.csv";
			FileWriter fw = new FileWriter(outputPath);
			fw.write(dateLine + "\n");
			fw.write(sigLine + "\n");
			fw.write(posLine + "\n");
			fw.write(ulyLine + "\n");
			fw.close();
			
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void xgbOosConverter(String inputFile) {
		try {
			
		}catch(Exception e) {
			e.printStackTrace();
		}
	}

}
