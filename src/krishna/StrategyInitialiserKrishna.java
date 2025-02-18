package krishna;

import java.util.ArrayList;
import java.util.List;

import algorithm.PBEContext;
import domains.Belief;
import helpers.StrategyInitialiser;
import strategy.PWCStrategy1Dto2D;
import strategy.Strategy;
import strategy.UnivariatePWCStrategy;

public class StrategyInitialiserKrishna implements StrategyInitialiser {
	PBEContext context;
	public StrategyInitialiserKrishna(PBEContext context){
		this.context=context;
	}
	@Override
	public List<Strategy> init(int subgame, Belief beliefdistribution) {
		double minVal = context.getDoubleParameter("minVal");
		double maxVal = context.getDoubleParameter("maxVal");
		int nBidders = context.getIntParameter("nBidders")-subgame;		int gridsize = context.getIntParameter("Gridsize");
		
		List<Strategy> strats = new ArrayList<>();
		
		for(int k=0;k<nBidders;k++) {
			strats.add(UnivariatePWCStrategy.makeTruthful(minVal, maxVal, gridsize));
		}
		
		return strats;
	}
}
