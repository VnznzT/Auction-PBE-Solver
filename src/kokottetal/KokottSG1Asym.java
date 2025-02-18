package kokottetal;

import algorithm.PBEContext;
import domains.AuctionRound;
import domains.Belief;
import strategy.Strategy;

import java.util.List;


public class KokottSG1Asym implements AuctionRound<Double, Double>{
	/*Round 2 of Kokott 2 round Dutch-FPSB auction, which is equivalent to a FPSB auction between n-1
	* ex-ante symmetric loosers with costs C*theta_l and one winner with cost (1-C)*\theta
	* We use this to compute both utility if player is winner or looser. This information is initialised
	* at the beginning with the boolean won.
	*/
	double[] efficiency;
	PBEContext context;

	public KokottSG1Asym(PBEContext context, int subGameNr) {
		double eff = context.getDoubleParameter("efficiency");
		this.context=context;
		int nrBidders=context.getnBidders(subGameNr);
		
		efficiency= new double[nrBidders];
		for (int i=0;i<nrBidders;i++) {
			this.efficiency[i]=eff;
		}
		/*since one bidder won split award in previous round their efficiency is 1-C. By symmetry
		 * we  assume bidder 0 is the winner. Note that the transition function then handles directing us to the right
		 * bidder
		*/
		this.efficiency[0]= 1-eff;
	}

	@Override
	public double computeUtility(int i, Double cost, List<Strategy<Double,Double>> currentStrategies, Belief beliefDistribution, Double[] bids) {
				
				int minIndex=0;
		double minBid=bids[0];

		for(int k=1; k<bids.length;k++) {
			if(bids[k]<bids[minIndex]) {
				minIndex=k;
				minBid=bids[k];
			}
		}

		if(i==minIndex) {
			return (bids[i]-efficiency[i]*cost);
		}else {
			return 0.0;
		}


	}
}
