package math;

import java.util.ArrayList;

public class MyMath {
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
}
