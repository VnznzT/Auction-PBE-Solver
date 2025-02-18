package integrator;

import java.util.Iterator;
import java.util.List;

import algorithm.PBEContext;
import domains.Belief;
import domains.BidSampler;
import strategy.Strategy;




public class MCIntegrator<Value, Bid> extends Integrator<Value, Bid> {

	public MCIntegrator(PBEContext context, int subGameRef) {
		super(context, subGameRef);
	}

	@Override
	public double computeExpectedUtility(int i, Value v, Bid b, Belief<Value> beliefDistribution, List<Strategy<Value, Bid>> strats) {
		int nsamples = context.getIntParameter("mcsamples");
		double result = 0.0;
		Iterator<BidSampler<Value, Bid>.Sample> biditer = context.getSampler(subGameRef).conditionalBidIterator(i, v, b, beliefDistribution, strats);
		BidSampler<Value, Bid>.Sample sample;
		
		for (int MCsample=0; MCsample<nsamples; MCsample++) {
			sample = biditer.next();
			
						result += sample.density * context.getAuctionRound(subGameRef).computeUtility(i, v, strats, beliefDistribution, sample.bids);
			if (Double.isNaN(result)) {
				throw new RuntimeException("MC integrator could not approximate integral.");
			}
		}
		
						result*=context.getVolumeEstimator(subGameRef).getVolume(i,beliefDistribution,strats.size());

		return result / nsamples;
	}

		public double computeExpectedUtility(int i, Value v, Bid b, Belief<Value> beliefDistribution, List<Strategy<Value, Bid>> strats, int nsamples) {
		double result = 0.0;
		Iterator<BidSampler<Value, Bid>.Sample> biditer = context.getSampler(subGameRef).conditionalBidIterator(i, v, b, beliefDistribution, strats);
		BidSampler<Value, Bid>.Sample sample;

		for (int MCsample=0; MCsample<nsamples; MCsample++) {
			sample = biditer.next();

						result += sample.density * context.getAuctionRound(subGameRef).computeUtility(i, v, strats, beliefDistribution, sample.bids);
			if (Double.isNaN(result)) {
				throw new RuntimeException("MC integrator could not approximate integral.");
			}
		}

						result*=context.getVolumeEstimator(subGameRef).getVolume(i,beliefDistribution,strats.size());

		return result / nsamples;
	}
}
