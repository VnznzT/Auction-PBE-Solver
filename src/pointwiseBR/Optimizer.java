package pointwiseBR;
import java.util.List;

import algorithm.PBEContext;
import domains.Belief;
import strategy.Strategy;



public abstract class Optimizer<Value, Bid> {
	public static class Result<Bid> {
		public Bid bid;
		public double utility;
		public double oldutility;
		
		public Result(Bid bid, double utility, double oldutility) {
			this.bid = bid;
			this.utility = utility;
			this.oldutility = oldutility;
		}
	}
	
	PBEContext context;
	int subGameRef;
	
	public Optimizer(PBEContext context,int subGameRef) {
		this.context = context;
		this.subGameRef=subGameRef;
	}

	public abstract Result<Bid> findBR(int i, Value v, Bid oldbid,Belief beliefDistribution ,List<Strategy<Value,Bid>> s);
}