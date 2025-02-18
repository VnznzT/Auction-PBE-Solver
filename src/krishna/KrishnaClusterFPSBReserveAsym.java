package krishna;

import algorithm.PBEContext;
import domains.AuctionRound;
import domains.Belief;
import strategy.Strategy;
import transition.SubGameTransition;
import utility.Utility;

import java.util.List;
import java.util.Random;

public class KrishnaClusterFPSBReserveAsym implements AuctionRound<Double, Double>{
		int subgame;
	int maxrounds;
	PBEContext context;

	double[] reservePrices;

	public KrishnaClusterFPSBReserveAsym(PBEContext context, int subgame, int maxrounds, double[] reservePrices) {
		this.context=context;
		this.subgame=subgame;
		this.maxrounds=maxrounds;
		this.reservePrices=reservePrices;
	}
	
	@Override
	public double computeUtility(int i, Double v, List<Strategy<Double,Double>> currentStrategies, Belief beliefDistribution, Double[] bids) {
		
						
				int winner=0;
		double maxBid=bids[0];
		int nBidders = bids.length;
		double reward=0.0;
		
		for(int k=1; k<nBidders;k++) {
			if(bids[k]>maxBid) {
				winner=k;
				maxBid=bids[k];
			}
		}

				if(i==winner && maxBid>=reservePrices[subgame]) {
						reward = (v-maxBid);
		}else if(subgame==0){						SubGameTransition.Reference reference = context.getSubGameTransition().transition(subgame, i, currentStrategies, beliefDistribution, bids,winner);
			List<Utility<Double>> expectedUtilities;
			if(context.isVerification()) {
				expectedUtilities = context.getCache().getPrevUtilities(reference.index, reference.subgame);
			}else {
				expectedUtilities = context.getCache().getUtilities(reference.index, reference.subgame);
			}
			reward = expectedUtilities.get(reference.player).getUtility(v);
		}else {
			reward = 0.0;
		}
		return reward;
	}
}
