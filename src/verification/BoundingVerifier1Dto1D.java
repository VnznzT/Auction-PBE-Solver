package verification;

import java.util.List;
import java.util.TreeMap;

import algorithm.PBEContext;
import domains.Belief;
import helpers.UtilityHelpers;
import pointwiseBR.Optimizer;
import strategy.Strategy;
import strategy.UnivariatePWCStrategy;
import utility.PWCUtility;
import utility.Utility;

public class BoundingVerifier1Dto1D implements Verifier<Double> {
	
	PBEContext context;
	int subgame;
	
	public BoundingVerifier1Dto1D(PBEContext context, int subgame) {
		super();
		this.context = context;
		this.subgame= subgame;
	}
	
	@Override
	public Result computeVer(int i,List<Strategy> s, Belief beliefDistribution) {
				double highestEpsilon = 0.0;
		int gridsize = context.getIntParameter("gridsize");
		Optimizer.Result<Double> oldresult = null;
		UnivariatePWCStrategy si = (UnivariatePWCStrategy) s.get(i);
		double maxValue = si.getMaxValue();
		double minValue = si.getMinValue();
		TreeMap<Double, Double> oldUtilMap = new TreeMap<>();
		TreeMap<Double, Double> utilPrevMap = new TreeMap<>();
		TreeMap<Double, Double> sBRMap = new TreeMap<>();
		
		for (int j = 0; j<=gridsize; j++) {
			double v = minValue + (maxValue-minValue) * ((double) j) / (gridsize);
			Double equilibriumBid = si.getBid(v);
			Optimizer.Result<Double> result = context.getOptimizer(subgame).findBR(i, v, equilibriumBid,beliefDistribution ,s);
						oldUtilMap.put(v, result.oldutility);
						sBRMap.put(v,result.bid);
						double epsilon = UtilityHelpers.absoluteLoss(result.oldutility, result.utility);
			highestEpsilon = Math.max(highestEpsilon, epsilon);
			
									if (j!=0) {
				double vPrev= minValue + (maxValue-minValue) * ((double) j-1.0) / (gridsize);
				double bidPrev = si.getBid(vPrev);
								context.setVerification(true);
				Optimizer.Result<Double> resultj_1 = context.getOptimizer(subgame).findBR(i, v, equilibriumBid,beliefDistribution ,s);
				double utilj_1 = context.getIntegrator(subgame).computeExpectedUtility(i, v, bidPrev, beliefDistribution, s);
												utilPrevMap.put(v, utilj_1);
								context.setVerification(false);
								double epsilonj_1 = UtilityHelpers.absoluteLoss(utilj_1,resultj_1.utility);
				highestEpsilon = Math.max(highestEpsilon, epsilonj_1);
			}
			oldresult = result;
		}
		return new Result(highestEpsilon, new UnivariatePWCStrategy(sBRMap), si, new PWCUtility(utilPrevMap),new PWCUtility(oldUtilMap));
	}

}