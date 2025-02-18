package algorithm;

import java.util.List;

import algorithm.Solver.IterationType;
import strategy.Strategy;
import utility.Utility;
import domains.Belief;

public interface Callback<Value,Bid> {

	public void afterIteration(int subGame,int iteration, IterationType type, Belief<Value> beliefDistribution, List<Strategy<Value,Bid>> strategies, List<Utility<Value>> utilities, double epsilon);
	public Strategy<Value,Bid> afterBR(int subGame, int iteration, Belief<Value> beliefDistribution, Strategy<Value,Bid> strategy, double epsilon);
}


