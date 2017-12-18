package backtesting.analysis;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.log4j.Logger;

import math.MyMath;

public class Main {
	public static Logger logger = Logger.getLogger(Main.class.getName());
	public static void main(String[] args) {
		try {
			double[] a = {100,101,102,101};
			
			ArrayList<Double> aa = new ArrayList<Double>();
			aa.add(1000.0);
			aa.add(101.0);
			aa.add(102.0);
			aa.add(101.0);
			aa.add(100.0);
			aa.add(99.0);
			aa.add(100.0);
			aa.add(101.0);
			aa.add(100.5);
			aa.add(101.5);
			aa.add(101.0);
			aa.add(102.0);
			aa.add(103.0);
			aa.add(104.0);
			aa.add(105.0);
			aa.add(106.0);
			aa.add(104.0);
			aa.add(100.0);
			
			//DrawDownAnalysis.maxDrawdown(aa);
			ArrayList<Double> port = new ArrayList<Double>	();
			
			String filePath = "Z:\\A share strategy\\T-2 backtest\\~~~EQUITY.csv";
			BufferedReader bf = utils.Utils.readFile_returnBufferedReader(filePath);
			String line = "";
			while((line = bf.readLine()) != null) {
				String[] lineArr = line.split(",");
				port.add(Double.parseDouble(lineArr[1]));
			}
			
			
			ArrayList<ArrayList<Double>> maxDD_rolling5 = DrawDownAnalysis.maxDrawdown_Rolling(port, 5);
			ArrayList<ArrayList<Double>> maxDD_rolling10 = DrawDownAnalysis.maxDrawdown_Rolling(port, 10);
			ArrayList<ArrayList<Double>> maxDD_rolling20 = DrawDownAnalysis.maxDrawdown_Rolling(port, 20);
			
			FileWriter fw = new FileWriter("Z:\\A share strategy\\T-2 backtest\\rollingMaxDD.csv");
			fw.write("5 day abs val,5 day pct,10 day abs val,10 day pct,20 day abs val,20 day pct\n");
			ArrayList<Double> absVal5 = maxDD_rolling5.get(0);
			ArrayList<Double> pct5 = maxDD_rolling5.get(1);
			ArrayList<Double> absVal10 = maxDD_rolling10.get(0);
			ArrayList<Double> pct10 = maxDD_rolling10.get(1);
			ArrayList<Double> absVal20 = maxDD_rolling20.get(0);
			ArrayList<Double> pct20 = maxDD_rolling20.get(1);
			for(int i = 0; i < port.size(); i++) {
				fw.write(absVal5.get(i) + ",");
				fw.write(pct5.get(i) + ",");
				fw.write(absVal10.get(i) + ",");
				fw.write(pct10.get(i) + ",");
				fw.write(absVal20.get(i) + ",");
				fw.write(pct20.get(i) + "\n");
			}
			fw.close();
			
		}catch(Exception e) {
			e.printStackTrace();
		}

	}

}
