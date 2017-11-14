package math;

import java.util.ArrayList;

import org.apache.log4j.Logger;

public class MyMath {
	public static Logger logger = Logger.getLogger(MyMath.class.getName());
	
	public static Double average(ArrayList<Double> arr) {
		return average(arr, 0.0);
	}
	
	public static Double average(ArrayList<Double> arr, Double drift) {
		Double cum = 0.0;
		for(int i = 0; i < arr.size(); i++) {
			cum += arr.get(i) + drift;
		}
		
		return cum / arr.size();
	}
	
	public static Double var(ArrayList<Double> arr) {
		Double avg = average(arr);
		Double cumSQ = 0.0;
		for(int i = 0; i < arr.size(); i++) {
			cumSQ += (arr.get(i) - avg) * (arr.get(i) - avg);
		}
		
		return (cumSQ) / (arr.size() - 1);
	}
	
	public static Double std(ArrayList<Double> arr) {
		return Math.sqrt(var(arr));
	}
	
	/**
	 * caculating covariance
	 * @param a1
	 * @param a2
	 * @return
	 */
	public static Double cov(ArrayList<Double> a1, ArrayList<Double> a2) {
		Double cov = 0.0;
		
		if(a1.size() != a2.size()) {
			logger.error("[Calculating covariance] Two arrays should have the same length!");
			return null;
		}
		
		Double a1_avg = average(a1);
		Double a2_avg = average(a2);
		
		Double cum = 0.0;
		for(int i = 0; i < a1.size(); i++) {
			cum += (a1.get(i) - a1_avg) * (a2.get(i) - a2_avg);
		}
		
		cov = cum / (a1.size() - 1);
		
		return cov;
	}

	public static Double corr(ArrayList<Double> a1, ArrayList<Double> a2) {
		if(a1.size() != a2.size()) {
			logger.error("[Calculating correlation coefficient] Two arrays should have the same length! Arr1 Len = " + a1.size() + " Arr2 Len = " + a2.size());
			return null;
		}
		
		Double a1_avg = average(a1);
		Double a2_avg = average(a2);
		
		Double cumCov = 0.0;
		Double cumStdA1 = 0.0;
		Double cumStdA2 = 0.0;
		for(int i = 0; i < a1.size(); i++) {
			Double s1 = a1.get(i) - a1_avg;
			Double s2 = a2.get(i) - a2_avg;
			
			cumCov += s1 * s2;
			cumStdA1 += s1 * s1;
			cumStdA2 += s2 * s2;
		}
		
		Double cov = cumCov / (a1.size() - 1);
		Double std1 = Math.sqrt(cumStdA1 / (a1.size() - 1));
		Double std2 = Math.sqrt(cumStdA2 / (a2.size() - 1));
		
		return cov / (std1 * std2);
		
	}

	/**
	 * Calculate the volatility of a portfolio. Please input the portfolio value, not the return array
	 * @param portfolioArr
	 * @return
	 */
	public static double volatility(ArrayList<Double> portfolioArr) {
		double vol = 0.0;
		
		double retSum = 0.0;
		ArrayList<Double> retArr = new ArrayList<Double>();
		
		for(int i = 1; i < portfolioArr.size(); i++) {
			double ret = Math.log(portfolioArr.get(i) / portfolioArr.get(i-1));
			
			retArr.add(ret);
		}
		
		double std = std(retArr);
		
		vol = std * Math.sqrt(252);
		return vol;
		
	}

}
