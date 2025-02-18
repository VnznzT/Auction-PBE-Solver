package krishna;

import algorithm.PBEContext;
import domains.AuctionRound;
import domains.Belief;
import strategy.Strategy;
import transition.SubGameTransition;
import utility.Utility;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class KrishnaClusterSPSBasym implements AuctionRound<Double, Double>{
		int subgame;
	int maxrounds;
	PBEContext context;

	public KrishnaClusterSPSBasym(PBEContext context, int subgame, int maxrounds) {
		this.context=context;
		this.subgame=subgame;
		this.maxrounds=maxrounds;
	}
	
	@Override
	public double computeUtility(int i, Double v, List<Strategy<Double,Double>> currentStrategies, Belief beliefDistribution, Double[] bids) {
		
				
				int winner=0;
		double maxBid=bids[0];
		double secondBid=0.0;
		int nBidders = bids.length;
		double reward;

		for (int k = 1; k < nBidders; k++) {
			if (bids[k] > maxBid) {
				secondBid = maxBid;
				maxBid = bids[k];
				winner = k;
			} else if (bids[k] > secondBid && k != winner) {
				secondBid = bids[k];
			}
		}


		if(i==winner) {
						reward = (v-secondBid);
		}else if(subgame<maxrounds-1){						SubGameTransition.Reference reference = context.getSubGameTransition().transition(subgame, i, currentStrategies, beliefDistribution, bids,winner);
			List<Utility> expectedUtilities;
			if(context.isVerification()){
				expectedUtilities = context.getCache().getPrevUtilities(reference.index);
			}else{
				expectedUtilities = context.getCache().getUtilities(reference.index);
			}
			reward = expectedUtilities.get(reference.player).getUtility(v);
		} else {
			reward = 0.0;
		}
		return reward;
	}
}
