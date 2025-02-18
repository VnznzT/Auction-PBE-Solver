package domains;
import java.util.Iterator;
import java.util.List;

import algorithm.PBEContext;
import strategy.Strategy;

public abstract class BidSampler<Value, Bid> {
	public class Sample {
		public double density;
		public Bid[] bids;
		
		public Sample(double density, Bid[] bids) {
			this.density = density;
			this.bids = bids;
		}
	};
	
	public PBEContext context;
	public int subGameNr;
	public BidSampler(PBEContext context,int subGameNr) {
		this.context = context;
		this.subGameNr=subGameNr;
	}
	
		public void setContext(PBEContext context) {
		this.context = context;
	}
	
		public abstract Iterator<Sample> conditionalBidIterator(int i, Value v, Bid b, Belief<Value> beliefDistribution, List<Strategy<Value,Bid>> s);
}
