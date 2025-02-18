package kokottetal;

import algorithm.PBEContext;
import domains.AuctionRound;
import domains.Belief;
import strategy.Strategy;
import transition.SubGameTransition;
import utility.Utility;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class KokottSG0ClusterAsym implements AuctionRound<Double, Double[]>{
			double efficiency;
	PBEContext context;

	public KokottSG0ClusterAsym(PBEContext context) {
				this.efficiency=context.getDoubleParameter("efficiency");
		this.context=context;
	}
	
	@Override
	public double computeUtility(int i, Double v, List<Strategy<Double,Double[]>> currentStrategies, Belief beliefDistribution, Double[][] bids) {
				double reward=0.0;
		
				int minIndexSole=0;
		int minIndexSplit=0;
		double minSole=bids[0][0];
		double minSplit=bids[0][1];


		for(int k=1; k<bids.length;k++) {
			if(bids[k][0]<bids[minIndexSole][0]) {
				minIndexSole=k;
				minSole=bids[k][0];
			}
			if(bids[k][1]<bids[minIndexSplit][1]) {
				minIndexSplit=k;
				minSplit=bids[k][1];
			}
		}

				if(2*minSplit > minSole) {
						if(minIndexSole==i) {
								reward=(bids[i][0]-v);
				}
		} else {
									int winner =minIndexSplit;
			if(winner==i) {
				reward=(bids[i][1]-v*efficiency);
			}
						SubGameTransition.Reference reference = context.getSubGameTransition().transition(0,i, currentStrategies, beliefDistribution, bids,winner);
			List<Utility> expectedUtilities;
			if(context.isVerification()){
				expectedUtilities = context.getCache().getPrevUtilities(reference.index);
			}else{
				expectedUtilities = context.getCache().getUtilities(reference.index);
			}
			reward+=expectedUtilities.get(reference.player).getUtility(v);
		} 		
		return reward;
	}
}
