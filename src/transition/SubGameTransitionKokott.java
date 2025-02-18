package transition;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import domains.Belief;
import kokottetal.BeliefKokott;
import strategy.PWCStrategy1Dto2D;
import strategy.Strategy;

public class SubGameTransitionKokott extends SubGameTransition<Double,Double[]> {
	
	/*transition gets a bidarray and beliefset from MCIntegrator and then returns the 
	* subgame and beliefdistribution that this specific play induces, which we can use to
	* call Solver.solve. If it is cashed then it will return the equilibrium utilities
	* else it will solve this instance
	*/
	
	public SubGameTransitionKokott() {
			}
	
	@Override
	public Reference transition(int subgame, int player, List<Strategy<Double, Double[]>> currentStrategies, Belief<Double> beliefDistribution, Double[][] bids, int winner) {
				BeliefKokott beliefs = (BeliefKokott) beliefDistribution;
		
				Double[] splitBids = new Double[bids.length];
		for (int k=0;k<bids.length;k++) {
			splitBids[k]=bids[k][1];
		}
		
		
		int inducedPlayer = 0; 		if(winner!=player) {
			inducedPlayer =1;
		}
		
								PWCStrategy1Dto2D winningStrategy = (PWCStrategy1Dto2D) currentStrategies.get(winner);
        
		Double[] values = winningStrategy.getValues();
		Double[][] strategyBids = winningStrategy.getBids(); 		Double winningBid = bids[winner][1];
        
				int lo = 1, hi=values.length-2;		while (lo + 1 < hi) {
			int middle = (lo + hi)/2;
			if (strategyBids[middle][1] <= winningBid) { 				lo = middle;
			} else {
				hi = middle;
			}
		}
		Double[] beliefAbtWinner= new Double[2];
		Double[] beliefAbtLoser = new Double[2];
				beliefAbtWinner[0]=values[lo];
		beliefAbtLoser[0]=values[lo];
				/*
		while(lo+1<values.length-2 && Math.abs(strategyBids[lo+1][1]-strategyBids[lo][1])<1e-7) {
			lo++;
		}
		*/
		beliefAbtWinner[1]=values[lo+1];
		beliefAbtLoser[1]=values[values.length-2];
		
		Double[][] newBeliefArray = new Double[beliefs.beliefSets.length][2];
		
				
		newBeliefArray[0][0]=beliefAbtWinner[0];
		newBeliefArray[0][1]=beliefAbtWinner[1];
			
		for(int k=1; k<beliefs.beliefSets.length;k++) {
			newBeliefArray[k][0]=beliefAbtLoser[0];
			newBeliefArray[k][1]=beliefAbtLoser[1];
			if(newBeliefArray[k][1]<newBeliefArray[k][0]) {
				throw new RuntimeException("Beliefupdate, tried to update inconsistent beliefs. Upper interval needs to be higher than lower interval end");
			}				
		}
					
		
		Belief newBelief = new BeliefKokott(newBeliefArray);
		
				Reference reference = new Reference(inducedPlayer,1,newBelief,lo-1);
		return reference;
	}
	

}
