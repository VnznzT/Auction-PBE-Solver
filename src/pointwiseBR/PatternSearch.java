package pointwiseBR;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import algorithm.PBEContext;
import domains.Belief;
import strategy.Strategy;


public class PatternSearch<Value, Bid> extends Optimizer<Value, Bid> {
	protected Pattern<Bid> pattern;
	double initialScale;
	
	public PatternSearch(PBEContext context, int subGameRef, Pattern<Bid> pattern) {
		super(context, subGameRef);
		this.pattern = pattern;
		initialScale = 1.0;
	}


	public double getInitialScale() {
		return initialScale;
	}


	public void setInitialScale(double initialScale) {
		this.initialScale = initialScale;
	}


		@Override
	public Result<Bid> findBR(int i, Value v, Bid currentBid, Belief beliefDistribution, List<Strategy<Value, Bid>> strats) {
		int patternSize = context.getIntParameter("patternsearch.size");
		double patternscale = context.getDoubleParameter("patternsearch.stepsize") * initialScale;
		int nSteps = context.getIntParameter("patternsearch.nsteps");
		
        double[] fxx = new double[patternSize];
        Bid bestbid = currentBid;
        
                Map<String, Double> cache = new Hashtable<>();

        for (int iter=0; iter<nSteps; iter++) {
        	List<Bid> patternPoints = pattern.getPatternPoints(bestbid, patternSize, patternscale);
        	for (int j=0; j<patternSize; j++) {
        		Bid bid = patternPoints.get(j);

        		if (cache.containsKey(pattern.bidHash(bid))) {
        			fxx[j] = cache.get(pattern.bidHash(bid));
            	} else {
            		Double util = context.getIntegrator(subGameRef).computeExpectedUtility(i, v, bid, beliefDistribution, strats);
            		cache.put(pattern.bidHash(bid), util);
            		fxx[j] = util;
            	}
            }
        	
        	int bestIndex= pattern.getCenterIndex(patternSize);

            for (int j=0; j<patternSize; j++) {
            	if (fxx[j] > fxx[bestIndex]) {
            		bestIndex = j;
            	}
            }
            if (bestIndex == pattern.getCenterIndex(patternSize)) {
        		        		patternscale *= 0.5;
        	} else {
        		        		iter++;
        	}
            bestbid = patternPoints.get(bestIndex);
        }
        
        return new Result<Bid>(bestbid, cache.get(pattern.bidHash(bestbid)), cache.get(pattern.bidHash(currentBid)));
	}
}
