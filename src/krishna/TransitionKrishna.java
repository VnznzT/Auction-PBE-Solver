package krishna;

import java.util.List;

import domains.Belief;
import strategy.Strategy;
import strategy.UnivariatePWCStrategy;
import transition.SubGameTransition;
import transition.SubGameTransition.Reference;

public class TransitionKrishna extends SubGameTransition<Double,Double> {

	/*transition gets a bidarray and beliefset from MCIntegrator and then returns the 
	* subgame and beliefdistribution that this specific play induces, which we can use to
	* call Solver.solve. If it is cashed then it will return the equilibrium utilities
	* else it will solve this instance
	*/
	
	public TransitionKrishna() {
			}
	
	@Override
	public Reference transition(int subgame, int player, List<Strategy<Double, Double>> currentStrategies, Belief<Double> beliefDistribution, Double[] bids, int winner) {
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
				return reference;
	}
	

}
