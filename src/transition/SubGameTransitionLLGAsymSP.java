package transition;

import LLG.BeliefLLG;
import domains.Belief;
import strategy.Strategy;
import strategy.UnivariatePWCStrategy;

import java.util.List;

public class SubGameTransitionLLGAsymSP extends SubGameTransition<Double,Double>{

    public SubGameTransitionLLGAsymSP() {
        super();
    }

    @Override
    public Reference transition(int subgame, int i, List<Strategy<Double, Double>> currentStrategies, Belief<Double> beliefDistribution, Double[] bids, int winner) {
                                                assert(i==winner);
        assert (i==0);


        int inducedPlayer = 0;         
                UnivariatePWCStrategy winningStrategy = (UnivariatePWCStrategy) currentStrategies.get(winner);
        double[] values = winningStrategy.getValues();
        double[] strategyBids = winningStrategy.getBids();         Double winningBid = bids[winner];

        int lo = 1, hi = values.length - 2;        while (lo + 1 < hi) {
            int middle = (lo + hi) / 2;
            if (strategyBids[middle] < winningBid) {
                lo = middle;
            } else {
                hi = middle;
            }
        }
        Double[] belief = new Double[2];

        
        belief[0] = values[hi-1];         belief[1] = values[values.length-2];

        Double[][] newBeliefArray = new Double[2][];
        newBeliefArray[0] = new Double[]{belief[0],belief[1]};
        newBeliefArray[1] = new Double[]{beliefDistribution.getMin(1),beliefDistribution.getMax(1)};
        Belief<Double> newBelief = new BeliefLLG(newBeliefArray);

        Reference reference = new Reference(inducedPlayer, 1, newBelief,hi-2);
        return reference;
    }
}
