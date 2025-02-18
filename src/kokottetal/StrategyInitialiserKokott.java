package kokottetal;

import java.util.ArrayList;
import java.util.List;

import algorithm.PBEContext;
import domains.Belief;
import helpers.StrategyInitialiser;
import strategy.PWCStrategy1Dto2D;
import strategy.Strategy;
import strategy.UnivariatePWCStrategy;

public class StrategyInitialiserKokott implements StrategyInitialiser {
	PBEContext context;
	public StrategyInitialiserKokott(PBEContext context){
		this.context=context;
	}
	@Override
	public List<Strategy> init(int subgame, Belief beliefdistribution) {
		double efficiency = context.getDoubleParameter("efficiency");
		double minCost = context.getDoubleParameter("minCost");
		double maxCost = context.getDoubleParameter("maxCost");
		int nBidders = context.getIntParameter("nBidders");
		int gridsize = context.getIntParameter("Gridsize");
		
		List<Strategy> strats = new ArrayList<>();
		if(subgame==0) {
			for(int k=0;k<nBidders;k++) {
				strats.add(PWCStrategy1Dto2D.makeTruthful(minCost, maxCost, efficiency, gridsize));
			}
		}else if(subgame==1) {
			strats.add(UnivariatePWCStrategy.makeTruthful(minCost, maxCost, efficiency, true, gridsize));
			for(int k=1;k<nBidders;k++) {
				strats.add(UnivariatePWCStrategy.makeTruthful(minCost, maxCost, efficiency, false,gridsize));
			}
		}else {
			throw new RuntimeException("illegal subgame reference");
		}
		return strats;
	}

}
