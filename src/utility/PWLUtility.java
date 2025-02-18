package utility;

import java.util.TreeMap;

public class PWLUtility extends PWCUtility {

	public PWLUtility(TreeMap<Double, Double> utilityMap) {
		super(utilityMap);
	}

	public PWLUtility(double[] values, double[] utilities) {
		super(values,utilities);
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
		double floor = values[lo];
		double ceiling = values[hi];
        
        double weight = (value - floor) / (ceiling - floor);
                return utilities[lo] + weight * (utilities[hi] - utilities[lo]); 	}
}
