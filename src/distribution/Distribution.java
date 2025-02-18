package distribution;

import domains.Belief;

public interface Distribution<value,bid> {
/*
	public class Sample<value>{
		public value sample;
		public Double density;
		public Sample(value sample, Double density) {
			this.sample = sample;
			this.density = density;
		}
		
	}
*/
	public Double density(Belief<value> belief,bid[] bids,int bidderToExclude);
	
}
