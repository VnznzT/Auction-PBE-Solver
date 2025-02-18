package kokottetal;

import domains.Belief;

public class BeliefKokott implements Belief<Double> {
	
	public BeliefKokott(Double[][] newBeliefArray) {
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
