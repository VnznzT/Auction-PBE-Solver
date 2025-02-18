package integrator;
import java.util.List;

import algorithm.PBEContext;
import domains.Belief;
import strategy.Strategy;

public abstract class Integrator<Value, Bid> {
	PBEContext context;
	public int subGameRef;
	
	public Integrator(PBEContext context, int subGameRef) {
		this.context = context;
		this.subGameRef=subGameRef;
	}

			public abstract double computeExpectedUtility(int i, Value v, Bid b, Belief<Value> beliefDistribution, List<Strategy<Value,Bid>> strats);
}
