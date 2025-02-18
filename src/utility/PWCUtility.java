package utility;

import java.util.SortedMap;
import java.util.TreeMap;

import strategy.UnivariatePWLStrategy;

public class PWCUtility implements Utility<Double>,java.io.Serializable {
	public double[] values;
	public double[] utilities;
	int n;
	
	public PWCUtility(TreeMap<Double, Double> utilityMap) {
	
								n = utilityMap.size();
		values = new double[n+2];
		utilities = new double[n+2];
			
		int i = 0;
		for (double key : utilityMap.keySet()) {
			i++;
			values[i] = key;
			utilities[i] = utilityMap.get(key);
		}
		values[0] = Double.MIN_VALUE;
		values[n+1] = Double.MAX_VALUE; 		utilities[0] = utilities[1];
		utilities[n+1] = utilities[n];
	}
	
	public PWCUtility(double[] values, double[] utilities) {
		this.n = utilities.length-2;
		this.values=values;
		this.utilities=utilities;
	}

	@Override
	public Double getUtility(Double value) {
				int lo = 0, hi=n+1;
		while (lo + 1 < hi) {
			int middle = (lo + hi)/2;
			if (values[middle] <= value) {
				lo = middle;
			} else {
				hi = middle;
			}
		}
				
        return utilities[lo];
	}
	
	public static PWCUtility makeZero(Double lower, Double upper) {
		TreeMap<Double, Double> intervals = new TreeMap<>();
		intervals.put(lower, 0.0);
		intervals.put(upper, 0.0);
		return new PWCUtility(intervals);	
	}

}
