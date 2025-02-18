package krishna;

import domains.Belief;

public class BeliefKrishna implements Belief<Double> {
	
	public BeliefKrishna(Double[][] newBeliefArray) {
		this.beliefSets=newBeliefArray;
	}
	public Double[][] beliefSets;
	
	public Double getMin(int player) {
		return beliefSets[player][0];
	}
	public Double getMax(int player) {
		return beliefSets[player][1];
	}

}
