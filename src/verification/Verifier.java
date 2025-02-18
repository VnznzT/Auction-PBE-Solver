package verification;

import java.util.List;

import domains.Belief;
import strategy.Strategy;
import utility.Utility;

public interface Verifier<Value> {
	
	public class Result{
		public double epsilon;
		public Strategy strategy;
		public Strategy oldStrategy;
		public Utility utility;
		public Utility oldUtility;
		public Result(double epsilon, Strategy strategy, Strategy oldStrategy, Utility utility,
				Utility oldUtility) {
			this.epsilon = epsilon;
			this.strategy = strategy;
			this.oldStrategy = oldStrategy;
			this.utility = utility;
			this.oldUtility = oldUtility;
		}
		
	}
				Result computeVer(int i,List<Strategy> s, Belief beliefDistribution);
	
}