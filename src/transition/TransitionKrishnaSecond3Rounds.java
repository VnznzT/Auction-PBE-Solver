package transition;

import domains.Belief;
import krishna.BeliefKrishna;
import strategy.Strategy;
import strategy.UnivariatePWCStrategy;

import java.util.List;

public class TransitionKrishnaSecond3Rounds extends SubGameTransition<Double,Double> {

    public TransitionKrishnaSecond3Rounds() {
            }

    public Reference transition(int subgame, int player, List<Strategy<Double, Double>> currentStrategies,
                                Belief<Double> beliefDistribution, Double[] bids, int winner, double secondBid, double value) {
        BeliefKrishna beliefs = (BeliefKrishna) beliefDistribution;

        int inducedPlayer = 0;


                        if (Math.abs(bids[player]-secondBid)<1e-10) {
                        inducedPlayer = 0;
        } else {
            inducedPlayer = 1;
        }
                UnivariatePWCStrategy winningStrategy = (UnivariatePWCStrategy) currentStrategies.get(winner);
        int gridsize = winningStrategy.values.length-2;

        double[] values = winningStrategy.getValues();
        double[] strategyBids = winningStrategy.getBids();         Double winningBid = bids[winner];

                int lo = 1, hi=values.length-2;        while (lo + 1 < hi) {
            int middle = (lo + hi)/2;
            if (strategyBids[middle]< winningBid) {
                lo = middle;
            } else {
                hi = middle;
            }
        }
        Double[] belief_long = new Double[2];
        Double[] belief_short = new Double[2];

        
        belief_short[0]=values[hi-1];         belief_short[1]=values[hi]; 
        belief_long[0]=values[1];         belief_long[1]=values[hi];
        int index = hi-2;
        Double[][] newBeliefArray = new Double[beliefs.beliefSets.length-1][2];         if(subgame==0){
            newBeliefArray[0]=belief_short;
            newBeliefArray[1]=belief_long;
        } else if (subgame==1) {
                        Boolean oneWinTwoSecond = (winner==1 && Math.abs(bids[2]-secondBid)<1e-10);
            Boolean twoWinOneSecond = (winner==2 && Math.abs(bids[1]-secondBid)<1e-10);
            Boolean consistent = Math.abs(belief_short[1]-beliefDistribution.getMax(0))<1e-10;
            if(consistent &&(oneWinTwoSecond || twoWinOneSecond)) {
                index = index+gridsize;
                newBeliefArray[0]=belief_short;
                newBeliefArray[1]=belief_short;
            } else {
                newBeliefArray[0]=belief_short;
                newBeliefArray[1]=belief_long;
            }
        }else{
            throw new RuntimeException("Invalid subgame round, maximum set to 2");
        }


        Belief newBelief = new BeliefKrishna(newBeliefArray);

                Reference reference = new Reference(inducedPlayer,subgame+1,newBelief,index);
        if(subgame+1>2) {
                        throw new RuntimeException("Invalid subgame round, maximum set to 3");
        }
        return reference;



    }
    @Override
    public Reference transition(int subgame, int player, List<Strategy<Double, Double>> currentStrategies,
                                Belief<Double> beliefDistribution, Double[] bids, int winner) {
                BeliefKrishna beliefs = (BeliefKrishna) beliefDistribution;


        int inducedPlayer = 0; 

                                UnivariatePWCStrategy winningStrategy = (UnivariatePWCStrategy) currentStrategies.get(winner);

        double[] values = winningStrategy.getValues();
        double[] strategyBids = winningStrategy.getBids();         Double winningBid = bids[winner];

                int lo = 1, hi=values.length-2;        while (lo + 1 < hi) {
            int middle = (lo + hi)/2;
            if (strategyBids[middle]< winningBid) {
                lo = middle;
            } else {
                hi = middle;
            }
        }
        Double[] belief = new Double[2];

        
        belief[0]=values[1];         belief[1]=values[hi];


        Double[][] newBeliefArray = new Double[beliefs.beliefSets.length-1][2]; 

        for(int k=0; k<beliefs.beliefSets.length-1;k++) {
            newBeliefArray[k]=belief;
            if(newBeliefArray[k][1]<newBeliefArray[k][0]) {
                                throw new RuntimeException("Beliefupdate, tried to update inconsistent beliefs. Upper interval needs to be higher than lower interval end");
            }
        }


        Belief newBelief = new BeliefKrishna(newBeliefArray);

                Reference reference = new Reference(inducedPlayer,subgame+1,newBelief,hi-2);
        if(subgame+1>2) {
                        throw new RuntimeException("Invalid subgame round, maximum set to 3");
        }
        return reference;
    }
}
