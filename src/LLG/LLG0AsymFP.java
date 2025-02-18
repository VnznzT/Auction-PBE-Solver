package LLG;

import algorithm.PBEContext;
import domains.AuctionRound;
import domains.Belief;
import strategy.Strategy;
import transition.SubGameTransition;
import utility.Utility;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class LLG0AsymFP implements AuctionRound<Double,Double> {
    PBEContext context;
    int subgame;
    public LLG0AsymFP(PBEContext context, int subgame) {
        this.context=context;
        this.subgame=subgame;
    }


    @Override
    public double computeUtility(int i, Double v, List<Strategy<Double, Double>> currentStrategies, Belief beliefDistribution, Double[] bids) {
                
        int winner=0;
        double maxBid=bids[0];
        int nBidders = bids.length;

                double reward=0.0;

        for(int k=1; k<nBidders;k++) {
            if(bids[k]>maxBid) {
                winner=k;
                maxBid=bids[k];
            }
        }



        if(i == winner){
            if (i==0){
                                                SubGameTransition.Reference reference = context.getSubGameTransition().transition(subgame, i, currentStrategies, beliefDistribution, bids,winner);
                List<Utility<Double>> expectedUtilities;
                if(context.isVerification()) {
                    expectedUtilities = context.getCache().getPrevUtilities(reference.index, reference.subgame);
                }else {
                    expectedUtilities = context.getCache().getUtilities(reference.index, reference.subgame);
                }
                                reward = expectedUtilities.get(reference.player).getUtility(v)-maxBid;

            }else if (i==1){
                                reward = (v-maxBid);
            }
        }else{
            reward = 0.0;
        }
        return reward;
    }
}

