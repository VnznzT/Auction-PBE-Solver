package BR;
import java.util.List;

import domains.Belief;
import strategy.Strategy;
import utility.Utility;

public interface BRCalculator<Value,Bid> {

	public class Result {

		public Strategy s;
		public Utility util;
		public double epsilonAbs, epsilonRel;
		public Result(Strategy s, Utility util, double epsilonAbs, double epsilonRel) {
			this.s = s;
			this.util = util;
			this.epsilonAbs = epsilonAbs;
			this.epsilonRel = epsilonRel;
		}

	}

	BRCalculator.Result computeBR(int i, List<Strategy<Value,Bid>> strategies, Belief beliefDistribution);


}
