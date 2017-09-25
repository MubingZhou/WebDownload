package backtesting.analysis;

import java.util.ArrayList;
import java.util.Arrays;

public class Main {

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
			
			DrawDownAnalysis.maxDrawdown(aa);
		}catch(Exception e) {
			e.printStackTrace();
		}

	}

}
