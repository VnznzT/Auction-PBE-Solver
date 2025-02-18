package kokottetal;

import java.util.ArrayList;
import java.util.List;

import algorithm.PBEContext;
import domains.Belief;
import helpers.UtilityInitialiser;
import utility.PWCUtility;
import utility.Utility;

public class UtilityInitialiserKokott implements UtilityInitialiser {
	PBEContext context;
	public UtilityInitialiserKokott(PBEContext context){
		this.context=context;
	}
	@Override
	public List<Utility> init(int subgame, Belief beliefdistribution) {
		double minCost = context.getDoubleParameter("minCost");
		double maxCost = context.getDoubleParameter("maxCost");
		int nBidders = context.getIntParameter("nBidders");
		
		List<Utility> utils = new ArrayList<>();
		for(int k=0;k<nBidders;k++) {
			utils.add(PWCUtility.makeZero(minCost, maxCost));
		}
		return utils;
	}

}
