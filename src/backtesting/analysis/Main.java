package backtesting.analysis;

import java.io.BufferedReader;
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
			
			String filePath = "D:\\stock data\\southbound flow strategy - db\\20170925 075008 - filter\\test.csv";
			BufferedReader bf = utils.Utils.readFile_returnBufferedReader(filePath);
			String[] strArr = bf.readLine().split(",");
			ArrayList<Double> d = new ArrayList<Double>	();
			for(int i = 0; i < strArr.length; i++) {
				d.add(Double.parseDouble(strArr[i]));
			}
			
			//logger.info("Sharpe = " + DrawDownAnalysis.sharpeRatio(d, 0.0));
			logger.info("cov = " + MyMath.cov(d, d));
			logger.info("var = " + MyMath.var(d));
			
		}catch(Exception e) {
			e.printStackTrace();
		}

	}

}
