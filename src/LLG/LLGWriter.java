package LLG;

import algorithm.Solver;
import domains.Belief;
import strategy.Strategy;
import strategy.UnivariatePWCStrategy;
import utility.PWCUtility;
import utility.Utility;

import java.util.List;

public class LLGWriter {
    public String write(int subGame, int iteration, Solver.IterationType type, Belief<Double> beliefDistribution, Strategy<Double,Double> strategy,
                        double epsilon){
        UnivariatePWCStrategy s= (UnivariatePWCStrategy) strategy;
        Double minValue=s.getMinValue();
        Double maxValue=s.getMaxValue();

                Double minBeliefGlobal = beliefDistribution.getMin(0);
        Double maxBeliefGlobal = beliefDistribution.getMax(0);

        StringBuilder builder = new StringBuilder();
        builder.append("Game: "+subGame);
        builder.append(" Belief: ");
        builder.append(String.format(" %7.6f  ", minBeliefGlobal));
        builder.append(String.format(" %7.6f  ", maxBeliefGlobal));
        builder.append("\n");
        builder.append("Iteration("+type+"): "+ String.format("%2d", iteration));
        builder.append(" Epsilon: "+String.format(" %7.6f  ", epsilon));
        builder.append(" strategy ");
        double[] v = s.values;
        double[] b = s.bids;
        for(int l=1;l<v.length-1;l++) {
            builder.append(String.format("%7.6f ", v[l]));
            builder.append("  ");
            builder.append(String.format("%7.6f ", b[l]));
            builder.append("  ");
        }
        builder.append("\n");
        return builder.toString();
    }
    public String write(int subGame, int iteration, Solver.IterationType type, Belief<Double> beliefDistribution, Strategy<Double,Double> strategy,
                        Utility<Double> utility, double epsilon){
        PWCUtility u= (PWCUtility) utility;
        UnivariatePWCStrategy s= (UnivariatePWCStrategy) strategy;
        Double minValue=s.getMinValue();
        Double maxValue=s.getMaxValue();

                Double minBeliefGlobal = beliefDistribution.getMin(0);
        Double maxBeliefGlobal = beliefDistribution.getMax(0);

        StringBuilder builder = new StringBuilder();
        builder.append("Game: "+subGame);
        builder.append(" Belief: ");
        builder.append(String.format(" %7.6f  ", minBeliefGlobal));
        builder.append(String.format(" %7.6f  ", maxBeliefGlobal));
        builder.append("\n");
        builder.append("Iteration("+type+"): "+ String.format("%2d", iteration));
        builder.append(" Epsilon: "+String.format(" %7.6f ", epsilon));

        builder.append("Utility ");

        double[] vals = u.values;
        double[] utils = u.utilities;
        for(int l=1;l<vals.length-1;l++) {
            builder.append(String.format("%7.6f ", vals[l]));
            builder.append("  ");
            builder.append(String.format("%7.6f ", utils[l]));
            builder.append("  ");
        }

        builder.append("\n");
        return builder.toString();
    }
}
