package helpers;

import domains.Belief;

public class VolumeEstimator1D implements VolumeEstimator {
	
	public VolumeEstimator1D() {
			}

	@Override
	public double getVolume(int i, Belief beliefDistribution, int nBidders) {
		Belief<Double> belief = (Belief<Double>) beliefDistribution;
	double vol =1.0;
	for(int k=0; k<nBidders; k++) {
		if(k!=i) {
			vol*=(belief.getMax(k)-belief.getMin(k));
		}
	}
	return vol;
	}

}
