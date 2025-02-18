package strategy;

import java.util.SortedMap;
import java.util.TreeMap;


public class UnivariatePWLStrategy implements Strategy<Double, Double> {
	public double[] values;
	public double[] bids;
	SortedMap<Double, Double> data;
	public int n;
	boolean isAscending;
	double maxValue;
	double minValue;
	
	public UnivariatePWLStrategy(SortedMap<Double, Double> intervals) {
								data = intervals;
		
		n = intervals.size();
		values = new double[n+2];
		bids = new double[n+2];
		
		int i = 0;
		for (double key : intervals.keySet()) {
			i++;
			values[i] = key;
			bids[i] = intervals.get(key);
		}
		values[0] = -1.0;
		values[n+1] = Double.MAX_VALUE; 		bids[0] = bids[1];
		bids[n+1] = bids[n];
		
		isAscending = true;
		for (i=0; i<n+1; i++) {
			if (bids[i+1] < bids[i]) {
				isAscending = false;
				break;
			}
		}
		
		maxValue = values[n];
		minValue = values[1];
	}
	
	public UnivariatePWLStrategy(double[] values, double[] bids) {
		this.values=values;
		this.bids=bids;
		this.n = bids.length-2;
		this.maxValue = values[this.n];
		this.minValue = values[1];
	}
	
	public static UnivariatePWLStrategy makeTruthful(double lower, double upper) {
		SortedMap<Double, Double> intervals = new TreeMap<>();
		intervals.put(lower, lower);
		intervals.put(upper, upper);
		return new UnivariatePWLStrategy(intervals);		
	}
	
		public static UnivariatePWLStrategy makeTruthful(double lower, double upper, double efficiency, boolean winner) {
		SortedMap<Double, Double> intervals = new TreeMap<>();
		if(winner) {
			intervals.put(lower, lower*(1-efficiency));
			intervals.put(upper, upper*(1-efficiency));
		}else {
			intervals.put(lower, lower*efficiency);
			intervals.put(upper, upper*efficiency);
		}
		return new UnivariatePWLStrategy(intervals);		
	}
	
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
				
		double floor = values[lo];
		double ceiling = values[hi];
		
				if (n==2) return value;
        
        double weight = (value - floor) / (ceiling - floor);
                return bids[lo] + weight * (bids[hi] - bids[lo]); 	}
	
	public Double invert(Double bid) {
		if (!isAscending) {
			throw new RuntimeException("Can't invert nonmonotonic strategy.");
		}
		
				int lo = 0, hi=n+1;
		while (lo + 1 < hi) {
			int middle = (lo + hi)/2;
			if (bids[middle] <= bid) {
				lo = middle;
			} else {
				hi = middle;
			}
		}
				
		double floor = values[lo];
		double ceiling = values[hi];
		
        double weight = (bid - floor) / (ceiling - floor);
        return weight * values[hi] + (1 - weight) * values[lo];        
	}
	
	@Override
    public String toString() {
        return "PiecewiseLinearStrategy{" +
                "values=" + values + " bids=" + bids +
                '}';
    }

	public SortedMap<Double, Double> getData() {
		return data;
	}

	@Override
	public Double getMaxValue() {
		return maxValue;
	}
	@Override
	public Double getMinValue() {
		return minValue;
	}
	
	public void makeMonotone() {
		for (int i=0; i<n+1; i++) {
			if (bids[i+1] < bids[i]) {
				bids[i+1] = bids[i];
			}
		}
	}
	public void makeMonotoneReverseOrder() {
		for (int i=n; i>0; i--) {
			if (bids[i-1] > bids[i]) {
				bids[i-1] = bids[i];
			}
		}
	}
	public void makeMonotoneMean() {
		double[] bidsLow = new double[bids.length];
		double [] bidsHigh= new double[bids.length];
		
		for (int i = 0; i < n+2; i++) {
			bidsLow[i] = bids[i];
			bidsHigh[i]=bids[i];
		}
				for (int i=0; i<n+1; i++) {
			if (bidsLow[i+1] < bidsLow[i]) {
				bidsLow[i+1] = bidsLow[i];
			}
		}
				for (int i=n+1; i>0; i--) {
			if (bidsHigh[i-1] > bidsHigh[i]) {
				bidsHigh[i-1] = Math.max(bidsHigh[i],0.0);
			}
		}
		for (int i = 0; i < n+1; i++) {
			bids[i]=(bidsLow[i]+bidsHigh[i])/2;
		}
		
		
	}
	public void makeStrictMonotone() {
		for (int i=0; i<n+1; i++) {
			if (bids[i+1] < bids[i]) {
				bids[i+1] = bids[i]+1e-5;
			}
		}
	}
		public void makeStrictMonotoneReverseOrder() {
		for (int i=n; i>0; i--) {
			if (bids[i-1] > bids[i]) {
				bids[i-1] = bids[i]-1e-5;
			}
		}
	}
	
	public void makeStrictMonotoneMean() {
		double[] bidsLow = new double[bids.length];
		double [] bidsHigh= new double[bids.length];
		
		for (int i = 0; i < n+2; i++) {
			bidsLow[i] = bids[i];
			bidsHigh[i]=bids[i];
		}
				for (int i=0; i<n+1; i++) {
			if (bidsLow[i+1] < bidsLow[i]) {
				bidsLow[i+1] = bidsLow[i]+1e-5;
			}
		}
				for (int i=n+1; i>0; i--) {
			if (bidsHigh[i-1] > bidsHigh[i]) {
				bidsHigh[i-1] = Math.max(bidsHigh[i]-1e-5,0.0);
			}
		}
		for (int i = 0; i < n+1; i++) {
			bids[i]=(bidsLow[i]+bidsHigh[i])/2;
		}
		
		
	}
	
	public double[] getValues() {
		return values;
	}
	public double[] getBids() {
		return bids;
	}
}
