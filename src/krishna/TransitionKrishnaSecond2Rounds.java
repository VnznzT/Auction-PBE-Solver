package krishna;

import domains.Belief;
import strategy.Strategy;
import strategy.UnivariatePWCStrategy;
import transition.SubGameTransition;

import java.util.List;

public class TransitionKrishnaSecond2Rounds extends SubGameTransition<Double,Double> {

		public TransitionKrishnaSecond2Rounds() {
			}

	public Reference transition(int subgame, int player, List<Strategy<Double, Double>> currentStrategies,
									Belief<Double> beliefDistribution, Double[] bids, int winner, double secondBid,double value) {
		BeliefKrishna beliefs = (BeliefKrishna) beliefDistribution;

		int inducedPlayer = 0;

						if (Math.abs(bids[player]-secondBid)<1e-10) {
						inducedPlayer = 0;
		} else {
			inducedPlayer = 1;
		}
				UnivariatePWCStrategy winningStrategy = (UnivariatePWCStrategy) currentStrategies.get(winner);
		
		double[] values = winningStrategy.getValues();
		double[] strategyBids = winningStrategy.getBids(); 		
				int lo = 1, hi=values.length-2;		while (lo + 1 < hi) {
			int middle = (lo + hi)/2;
			if (strategyBids[middle]< secondBid) {
				lo = middle;
			} else {
				hi = middle;
			}
		}
		Double[] belief1 = new Double[2];
		Double[] belief0 = new Double[2];

		
		belief0[0]=values[hi-1]; 		belief0[1]=values[hi]; 
		belief1[0]=values[1]; 		belief1[1]=values[hi];


		assert(beliefs.beliefSets.length==3);
		Double[][] newBeliefArray = new Double[beliefs.beliefSets.length-1][2]; 
		newBeliefArray[0]=belief0;
		newBeliefArray[1]=belief1;


		Belief newBelief = new BeliefKrishna(newBeliefArray);

				Reference reference = new Reference(inducedPlayer,subgame+1,newBelief,hi-2);
		if(subgame+1>2) {
						throw new RuntimeException("Invalid subgame round, maximum set to 3");
		}
		return reference;



	}
	@Override
	public Reference transition(int subgame, int player, List<Strategy<Double, Double>> currentStrategies,
								Belief<Double> beliefDistribution, Double[] bids, int winner) {
				BeliefKrishna beliefs = (BeliefKrishna) beliefDistribution;
		
		
		int inducedPlayer = 0; 
		
								UnivariatePWCStrategy winningStrategy = (UnivariatePWCStrategy) currentStrategies.get(winner);
        
		double[] values = winningStrategy.getValues();
		double[] strategyBids = winningStrategy.getBids(); 		Double winningBid = bids[winner];
        
				int lo = 1, hi=values.length-2;		while (lo + 1 < hi) {
			int middle = (lo + hi)/2;
			if (strategyBids[middle]< winningBid) {
				lo = middle;
			} else {
				hi = middle;
			}
		}
		Double[] belief = new Double[2];
		
				
		belief[0]=values[1]; 		belief[1]=values[hi];
		
		
		Double[][] newBeliefArray = new Double[beliefs.beliefSets.length-1][2]; 
			
		for(int k=0; k<beliefs.beliefSets.length-1;k++) {
			newBeliefArray[k]=belief;
			if(newBeliefArray[k][1]<newBeliefArray[k][0]) {
								throw new RuntimeException("Beliefupdate, tried to update inconsistent beliefs. Upper interval needs to be higher than lower interval end");
			}				
		}
		
		
		Belief newBelief = new BeliefKrishna(newBeliefArray);
		
				Reference reference = new Reference(inducedPlayer,subgame+1,newBelief,hi-2);
		if(subgame+1>2) {
						throw new RuntimeException("Invalid subgame round, maximum set to 3");
		}	
		return reference;
	}
	

}
