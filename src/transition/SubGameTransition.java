package transition;
import java.util.List;

import domains.Belief;
import strategy.Strategy;

public abstract class SubGameTransition<Value,Bid> {
	
				public class Reference {
		public int player;
		public int subgame;
		public Belief beliefDistribution;
		public Reference(int player, int subgame, Belief<Value> beliefDistribution) {
			this.player=player;
			this.subgame = subgame;
			this.beliefDistribution = beliefDistribution;
		}
		public Reference(int player, int subgame, Belief<Value> beliefDistribution, int index) {
			this.player=player;
			this.subgame = subgame;
			this.beliefDistribution = beliefDistribution;
			this.index = index;
		}
		public int index;
	}
	
	public abstract Reference transition(int subgame, int i, List<Strategy<Value,Bid>> currentStrategies, Belief<Value> beliefDistribution, Bid[] bids, int winner);
	

}
