package helpers;

import domains.Belief;

public interface VolumeEstimator {
	public double getVolume(int i,Belief beliefDistribution, int nBidders);
	
}
