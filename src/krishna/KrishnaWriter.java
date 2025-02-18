package krishna;

import java.util.List;

import algorithm.Solver.IterationType;
import domains.Belief;
import strategy.Strategy;
import strategy.UnivariatePWCStrategy;
import utility.PWCUtility;
import utility.Utility;

public class KrishnaWriter {
	public String write(int subGame, int iteration, IterationType type, Belief<Double> beliefDistribution, List<Strategy<Double,Double>> strategies,
			double epsilon)  {
    	UnivariatePWCStrategy s= (UnivariatePWCStrategy) strategies.get(0);
    	Double minValue=s.getMinValue();
    	Double maxValue=s.getMaxValue();
    	
    	    	Double minBelief = beliefDistribution.getMin(0);
    	Double maxBelief = beliefDistribution.getMax(0);
    	
    	StringBuilder builder = new StringBuilder();
    	builder.append("Game: "+subGame);
    	builder.append(" Belief: ");
    	builder.append(String.format(" %7.6f  ", minBelief));
    	builder.append(String.format(" %7.6f  ", maxBelief));
    	builder.append("\n");
        builder.append("Iteration("+type+"): "+ String.format("%2d", iteration));
        builder.append(" Epsilon: "+String.format(" %7.6f  ", epsilon));
        builder.append(" strategy ");
        		double[] v = s.values;
		double[] b = s.bids;
		/*
		for (int i=0; i<=ngridpoints; i++) {
            double v = minValue+(maxValue-minValue) * i / ngridpoints;
            builder.append(String.format("%5.4f",v));
            builder.append("  ");
            builder.append(String.format("%5.4f", s.getBid(v)));
            builder.append("  ");
        }
		 */
		for(int l=1;l<v.length-1;l++) {
			builder.append(String.format("%5.4f ", v[l]));
			builder.append("  ");
			builder.append(String.format("%5.4f ", b[l]));
			builder.append("  ");
		}
		builder.append("\n");
    	return builder.toString();
    }
	public String write(int subGame, int iteration, IterationType type, Belief<Double> beliefDistribution, List<Strategy<Double,Double>> strategies,
			List<Utility<Double>> utilities, double epsilon)  {
    	PWCUtility u= (PWCUtility) utilities.get(0);
    	UnivariatePWCStrategy s= (UnivariatePWCStrategy) strategies.get(0);
    	Double minValue=s.getMinValue();
    	Double maxValue=s.getMaxValue();
    	
    	    	Double minBelief = beliefDistribution.getMin(0);
    	Double maxBelief = beliefDistribution.getMax(0);
    	
    	StringBuilder builder = new StringBuilder();
    	builder.append("Game: "+subGame);
    	builder.append(" Belief: ");
    	builder.append(String.format(" %7.6f  ", minBelief));
    	builder.append(String.format(" %7.6f  ", maxBelief));
    	builder.append("\n");
        builder.append("Iteration("+type+"): "+ String.format("%2d", iteration));
        builder.append(" Epsilon: "+String.format(" %7.6f  ", epsilon));
        builder.append(" strategy ");
        		double[] v = u.values;
		double[] util = u.utilities;

		for (int i=0; i<v.length; i++) {
			builder.append(String.format("%5.4f",v[i]));
			builder.append("  ");
			builder.append(String.format("%5.4f", util[i]));
			builder.append("  ");
		}

        builder.append("\n");
    	return builder.toString();
    }
}
