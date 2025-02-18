package krishna;

import java.util.ArrayList;
import java.util.List;

import algorithm.PBEContext;
import domains.Belief;
import helpers.UtilityInitialiser;
import utility.PWCUtility;
import utility.Utility;

public class UtilityInitialiserKrishna implements UtilityInitialiser {
	PBEContext context;
	public UtilityInitialiserKrishna(PBEContext context){
		this.context=context;
	}
	@Override
	public List<Utility> init(int subgame, Belief beliefdistribution) {
		double minVal = context.getDoubleParameter("minVal");
		double maxVal = context.getDoubleParameter("maxVal");
		int nBidders = context.getIntParameter("nBidders")-subgame;		
		List<Utility> utils = new ArrayList<>();
		for(int k=0;k<nBidders;k++) {
			utils.add(PWCUtility.makeZero(minVal, maxVal));
		}
		return utils;
	}

}
