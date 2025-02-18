package krishna;

import algorithm.PBEContext;
import domains.Belief;
import strategy.Strategy;
import strategy.UnivariatePWCStrategy;
import transition.SubGameTransition;

import java.util.List;

public class TransitionKrishnaReserve extends SubGameTransition<Double,Double> {

	/*transition gets a bidarray and beliefset from MCIntegrator and then returns the
	* subgame and beliefdistribution that this specific play induces, which we can use to
	* call Solver.solve. If it is cashed then it will return the equilibrium utilities
	* else it will solve this instance
	*/
	double[] reservePrices;
	PBEContext context;
	int gridsize;
	public TransitionKrishnaReserve(double[] reservePrices, PBEContext context) {
		this.reservePrices=reservePrices;
		this.context=context;
		this.gridsize = context.getIntParameter("SG0.OuterLoop.Gridsize");
			}
	
	@Override
	public Reference transition(int subgame, int player, List<Strategy<Double, Double>> currentStrategies, Belief<Double> beliefDistribution, Double[] bids, int winner) {
				BeliefKrishna beliefs = (BeliefKrishna) beliefDistribution;
		
		
		int inducedPlayer = 0; 
		
								UnivariatePWCStrategy winningStrategy = (UnivariatePWCStrategy) currentStrategies.get(winner);
        
		double[] values = winningStrategy.getValues();
		double[] strategyBids = winningStrategy.getBids(); 
		double winningBid = bids[winner];
		double reservePrice = reservePrices[subgame];
				int newSubgame;
		if(reservePrice<=winningBid){
						newSubgame = 1;
		}else{
						newSubgame = 2;
		}
		double highestPriceInfo = Math.max(reservePrice,winningBid);

				int lo = 1, hi=values.length-2;		while (lo + 1 < hi) {
			int middle = (lo + hi)/2;
			if (strategyBids[middle]< highestPriceInfo) {
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



		Reference reference = new Reference(inducedPlayer,newSubgame,newBelief,hi-2);
		if(subgame+1>2) {
						throw new RuntimeException("Invalid subgame round, maximum set to 3");
		}	
		return reference;
	}
	

}
