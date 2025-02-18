package strategy;

import java.util.SortedMap;
import java.util.TreeMap;


public class PWLStrategy1Dto2D implements Strategy<Double, Double[]> {
	public Double[] values;
	public Double[][] bids;
	int n;
	double maxValue;
	double minValue;
	SortedMap<Double,Double> strategySplit;
	SortedMap<Double,Double> strategySole;
	
	
	public PWLStrategy1Dto2D(SortedMap<Double, Double> strategySole, SortedMap<Double,Double> strategySplit) throws RuntimeException{
								this.strategySplit=strategySplit;
		this.strategySole=strategySole;
		if (strategySole.size() != strategySplit.size()) {
			throw new RuntimeException("Values of Split and Sole strategy not equal. Maybe use 2D instead.");
		}
		n = strategySplit.size();
		values = new Double[n+2];
				bids = new Double[n+2][2];
		
		int i = 0;
		for (double key : strategySole.keySet()) {
			i++;
			values[i] = key;
			bids[i][0] = strategySole.get(key);
			bids[i][1] = strategySplit.get(key);
		}
		values[0] = Double.MIN_VALUE;
		values[n+1] = Double.MAX_VALUE; 		
		bids[n+1][0] = bids[n][0];
		bids[0][1] = bids[1][1];
		bids[n+1][1] = bids[n][1];
		bids[0][0] = bids[1][0]; 		
		
		maxValue = values[n];
		minValue =values[1];
	}
	
	public static PWLStrategy1Dto2D makeTruthful(double lower, double upper, double efficiency) {
		SortedMap<Double, Double> strategySole = new TreeMap<>();
		SortedMap<Double, Double> strategySplit = new TreeMap<>();
		
		strategySole.put(lower, lower);
		strategySole.put(upper, upper);
		
		strategySplit.put(lower, efficiency*lower);
		strategySplit.put(upper, efficiency*upper);
		
		return new PWLStrategy1Dto2D(strategySole, strategySplit);		
	}
	
	public Double[] getBid(Double value) {
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
        Double[] interpolatedBids = new Double[2];
        
        interpolatedBids[0] = bids[lo][0] + weight * (bids[hi][0] - bids[lo][0]);         interpolatedBids[1] = bids[lo][1] + weight * (bids[hi][1] - bids[lo][1]);
        return interpolatedBids; 
	}
	
	@Override
    public String toString() {
        return "PiecewiseLinearStrategy{" +
                "values=" + values + "Sole source bids= " + bids[0] + "Split award bids= " + bids[2] +
                '}';
    }


	@Override
	public Double getMaxValue() {
		return maxValue;
	}
	public Double getMinValue() {
		return minValue;
	}
	
	public Double[] getValues() {
		return values;
	}
	
	public Double[][] getBids() {
		return bids;
	}

	public SortedMap<Double,Double> getSplitMap() {
		return strategySplit;
	}
	public SortedMap<Double,Double> getSoleMap() {
		return strategySole;
	}
}
