package BR;

import java.util.List;
import java.util.TreeMap;

import algorithm.PBEContext;
import domains.Belief;
import helpers.UtilityHelpers;
import pointwiseBR.Optimizer;
import strategy.Strategy;
import utility.PWCUtility;
import strategy.PWCStrategy1Dto2D;
public class BRCalculator1Dto2D implements BRCalculator<Double,Double[]>{
	PBEContext context;
	int subGameRef;

	public BRCalculator1Dto2D(PBEContext context, int subGameRef) {
		this.context = context;
		this.subGameRef=subGameRef;
	}
	
	@Override
	public Result computeBR(int i, List<Strategy<Double, Double[]>> s, Belief beliefDistribution){
		int nPoints = Integer.parseInt(context.config.get("gridsize"));
				TreeMap<Double, Double> pointwiseBRsSole = new TreeMap<>();
		TreeMap<Double, Double> pointwiseBRsSplit = new TreeMap<>();
		
				TreeMap<Double, Double> utilityMap = new TreeMap<>();
		
		double epsilonAbs = 0.0;
		double epsilonRel = 0.0;
		
		double maxValue = s.get(i).getMaxValue();
		double minValue = s.get(i).getMinValue();
		
		for (int j = 0; j<=nPoints; j++) {
			Double v = minValue + (maxValue-minValue) * ((double) j) / (nPoints);
			Double[] oldbid = s.get(i).getBid(v);
			Optimizer.Result<Double[]> result = context.getOptimizer(subGameRef).findBR(i, v, oldbid,beliefDistribution, s);
			epsilonAbs = Math.max(epsilonAbs, UtilityHelpers.absoluteLoss(result.oldutility, result.utility));
			epsilonRel = Math.max(epsilonRel, UtilityHelpers.relativeLoss(result.oldutility, result.utility));

			Double[] newbid = (Double[]) context.getUpdateRule(subGameRef).update(oldbid, result.bid, result.oldutility, result.utility);
			pointwiseBRsSole.put(v,  newbid[0]);
			pointwiseBRsSplit.put(v, newbid[1]);	
			
			utilityMap.put(v, result.utility);
		}
		
		return new Result(new PWCStrategy1Dto2D(pointwiseBRsSole,pointwiseBRsSplit),new PWCUtility(utilityMap),epsilonAbs, epsilonRel);
	}
}
