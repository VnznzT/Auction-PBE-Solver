package BR;

import java.util.List;
import java.util.TreeMap;

import BR.BRCalculator.Result;
import algorithm.PBEContext;
import domains.Belief;
import helpers.UtilityHelpers;
import pointwiseBR.Optimizer;
import strategy.Strategy;
import strategy.UnivariatePWCStrategy;
import utility.PWCUtility;
import utility.PWLUtility;

public class BRCalculator1Dto1D implements BRCalculator<Double, Double> {
	PBEContext context;
	int subGameRef;

	public BRCalculator1Dto1D(PBEContext context, int subGameRef) {
		this.context = context;
		this.subGameRef=subGameRef;
	}
	
	@Override
	public Result computeBR(int i, List<Strategy<Double, Double>> s, Belief beliefDistribution){
		int nPoints = Integer.parseInt(context.config.get("gridsize"));
		TreeMap<Double, Double> utilityMap = new TreeMap<>();
		TreeMap<Double, Double> pointwiseBRs = new TreeMap<>();
		
		double epsilonAbs = 0.0;
		double epsilonRel = 0.0;

		double maxValue = s.get(i).getMaxValue();
		double minValue = s.get(i).getMinValue();
		
		for (int j = 0; j<=nPoints; j++) {
			Double v = minValue + (maxValue-minValue) * ((double) j) / (nPoints);
			Double oldbid = s.get(i).getBid(v);
			Optimizer.Result<Double> result = context.getOptimizer(subGameRef).findBR(i, v, oldbid,beliefDistribution, s);
			epsilonAbs = Math.max(epsilonAbs, UtilityHelpers.absoluteLoss(result.oldutility, result.utility));
			epsilonRel = Math.max(epsilonRel, UtilityHelpers.relativeLoss(result.oldutility, result.utility));

			Double newbid = (Double) context.getUpdateRule(subGameRef).update(oldbid, result.bid, result.oldutility, result.utility);
			
						pointwiseBRs.put(v,  newbid);	
			utilityMap.put(v, result.utility);
		}
		
				return new Result(new UnivariatePWCStrategy(pointwiseBRs), new PWCUtility(utilityMap),epsilonAbs,epsilonRel); 
	}
}
