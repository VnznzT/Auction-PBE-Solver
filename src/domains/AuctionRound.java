package domains;
import java.util.List;

import strategy.PWCStrategy1Dto2D;
import strategy.Strategy;

public interface AuctionRound<Value, Bid> {
	
	public double computeUtility(int i, Value v, List<Strategy<Value,Bid>> currentStrategies, Belief beliefDistribution, Bid[] bids);

}
