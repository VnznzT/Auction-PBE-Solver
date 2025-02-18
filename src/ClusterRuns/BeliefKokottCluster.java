package ClusterRuns;

import domains.Belief;

public class BeliefKokottCluster implements Belief<Double> {
	
	public BeliefKokottCluster(Double[][] newBeliefArray,int b1ref,int b2ref) {
		this.beliefSets=newBeliefArray;
		this.b1ref=b1ref;
		this.b2ref=b2ref;
	}
	public Double[][] beliefSets;
	public int b1ref;
	public int b2ref;
	
	public Double getMin(int player) {
		return beliefSets[player][0];
	}
	public Double getMax(int player) {
		return beliefSets[player][1];
	}

}
