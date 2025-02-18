package kokottetal;

import java.util.List;
import java.util.StringJoiner;

import algorithm.Solver.IterationType;
import strategy.PWCStrategy1Dto2D;
import strategy.PWLStrategy1Dto2D;
import strategy.Strategy;
import domains.Belief;


public class KokottWriterRound1 {
    public String write(int subGame, int iteration, IterationType type, Belief<Double> beliefDistribution, List<Strategy<Double,Double[]>> strategies,
			double epsilon)  {
    	PWCStrategy1Dto2D s= (PWCStrategy1Dto2D) strategies.get(0);
    	Double minValue=s.getMinValue();
    	Double maxValue=s.getMaxValue();
    	
    	    	Double minBeliefWin = beliefDistribution.getMin(0);
    	Double maxBeliefWin = beliefDistribution.getMax(0);
    	
    	StringBuilder builder = new StringBuilder();
    	builder.append("Game: "+subGame);
    	builder.append(" Belief: ");
    	builder.append(String.format(" %7.6f  ", minBeliefWin));
    	builder.append(String.format(" %7.6f  ", maxBeliefWin));
    	builder.append("\n");
    	
        builder.append("Iteration("+type+"): "+String.format("%2d", iteration));
        builder.append(" Epsilon: "+String.format(" %7.6f  ", epsilon));
        builder.append(" sole ");


        Double[] v = s.getValues();

        for (int i=1; i<v.length-1; i++) {
            builder.append(String.format("%5.4f",v[i]));
            builder.append("  ");
            builder.append(String.format("%5.4f", s.getBid(v[i])[0]));
            builder.append("  ");
        }

        
            builder.append("\n");
            builder.append("Iteration: "+ String.format("%2d", iteration));
            builder.append(" Epsilon: " + String.format(" %7.6f  ", epsilon));
            builder.append(" split ");

        for (int i=1; i<v.length-1; i++) {
            builder.append(String.format("%5.4f",v[i]));
            builder.append("  ");
            builder.append(String.format("%5.4f", s.getBid(v[i])[1]));
            builder.append("  ");
        }
    	return builder.toString();
    }
}