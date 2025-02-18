package distribution;

import domains.Belief;

public class UniformOrderStat implements Distribution<Double,Double> {

	@Override
	public Double density(Belief<Double> belief,Double[] bids,int i) {
		int n=bids.length;
		double density =1.0;
		for(int k=0;k<n;k++) {
			if(k!=i) {
				density*= (belief.getMax(k)-belief.getMin(k));
			}	
		}
		density=1/density;
		return density;
	}
	}
