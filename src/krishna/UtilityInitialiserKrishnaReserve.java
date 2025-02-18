package krishna;

import algorithm.PBEContext;
import domains.Belief;
import helpers.UtilityInitialiser;
import utility.PWCUtility;
import utility.Utility;

import java.util.ArrayList;
import java.util.List;

public class UtilityInitialiserKrishnaReserve implements UtilityInitialiser {
	PBEContext context;
	public UtilityInitialiserKrishnaReserve(PBEContext context){
		this.context=context;
	}
	@Override
	public List<Utility> init(int subgame, Belief beliefdistribution) {
		double minVal = context.getDoubleParameter("minVal");
		double maxVal = context.getDoubleParameter("maxVal");
		int nBidders = context.getnBidders(subgame);
		
		List<Utility> utils = new ArrayList<>();
		for(int k=0;k<nBidders;k++) {
			utils.add(PWCUtility.makeZero(minVal, maxVal));
		}
		return utils;
	}

}
