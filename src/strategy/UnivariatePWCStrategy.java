package strategy;

import java.util.SortedMap;
import java.util.TreeMap;


public class UnivariatePWCStrategy extends UnivariatePWLStrategy {
	
	public UnivariatePWCStrategy(SortedMap<Double, Double> intervals) {
		super(intervals);
	}

	public UnivariatePWCStrategy(double[] values, double[] ds) {
		super(values,ds);
	}

	@Override
	public Double getBid(Double value) {
				int lo = 0, hi=n+1;
		while (lo + 1 < hi) {
			int middle = (lo + hi)/2;
			if (values[middle] <= value) {
				lo = middle;
			} else {
				hi = middle;
			}
		}
				
        return bids[lo];
	}
	
	public static UnivariatePWCStrategy makeTruthful(double lower, double upper,int nPoints) {
		SortedMap<Double, Double> s = new TreeMap<>();
		for(int i=0;i<=nPoints;i++) {
			Double v = lower + (upper-lower) * ((double) i) / (nPoints);
			s.put(v, v);
		}
		return new UnivariatePWCStrategy(s);		
	}
	
	
		public static UnivariatePWCStrategy makeTruthful(double lower, double upper, double efficiency, boolean winner, int nPoints) {
		SortedMap<Double, Double> s = new TreeMap<>();
		
		if(winner) {
			for(int i=0;i<=nPoints;i++) {
				Double v = lower + (upper-lower) * ((double) i) / (nPoints);
				s.put(v, v*(1-efficiency));
			}
		}else {
			for(int i=0;i<=nPoints;i++) {
				Double v = lower + (upper-lower) * ((double) i) / (nPoints);
				s.put(v, v*efficiency);
			}
		}	
		return new UnivariatePWCStrategy(s);		
	}
}
