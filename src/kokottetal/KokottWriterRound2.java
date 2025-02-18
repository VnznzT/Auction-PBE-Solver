package kokottetal;

import java.util.List;
import java.util.StringJoiner;

import algorithm.Solver.IterationType;
import domains.Belief;
import strategy.PWCStrategy1Dto2D;
import strategy.Strategy;
import strategy.UnivariatePWCStrategy;


public class KokottWriterRound2 {
	public String write(int subGame, int iteration, IterationType type, Belief<Double> beliefDistribution, List<Strategy<Double,Double>> strategies,
			double epsilon)  {
    	UnivariatePWCStrategy sWin= (UnivariatePWCStrategy) strategies.get(0);
    	UnivariatePWCStrategy sLost= (UnivariatePWCStrategy) strategies.get(1);
    	Double minValue=sWin.getMinValue();
    	Double maxValue=sWin.getMaxValue();

        double[] vWin = sWin.values;
        double[] bWin = sWin.bids;
        double[] vLost = sLost.values;
        double[] bLost = sLost.bids;

    	    	Double minBeliefWin = beliefDistribution.getMin(0);
    	Double maxBeliefWin = beliefDistribution.getMax(0);
    	
    	StringBuilder builder = new StringBuilder();
    	builder.append("Game: "+subGame);
    	builder.append(" Belief: ");
    	builder.append(String.format(" %7.6f  ", minBeliefWin));
    	builder.append(String.format(" %7.6f  ", maxBeliefWin));
    	builder.append("\n");
    	builder.append("w: ");
        builder.append("Iteration("+type+"): "+ String.format("%2d", iteration));
        builder.append(" Epsilon: "+String.format(" %7.6f  ", epsilon));
        builder.append(" split ");
        for (int i=1; i<vWin.length-1; i++) {
            builder.append(String.format("%5.4f",vWin[i]));
            builder.append("  ");
            builder.append(String.format("%5.4f", bWin[i]));
            builder.append("  ");
        }

        builder.append("\n");
        
    	builder.append("l: ");
        builder.append("Iteration: "+ String.format("%2d", iteration));
        builder.append(" Epsilon: "+String.format(" %7.6f  ", epsilon));
        builder.append(" split ");
        for (int i=1; i<vLost.length-1; i++) {
            builder.append(String.format("%5.4f",vLost[i]));
            builder.append("  ");
            builder.append(String.format("%5.4f", bLost[i]));
            builder.append("  ");
        }
        builder.append("\n");
    	return builder.toString();
    }
}