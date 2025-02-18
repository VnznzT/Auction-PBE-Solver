package LLG;

import algorithm.PBEContext;
import domains.AuctionRound;
import domains.Belief;
import strategy.Strategy;

import java.util.List;

public class LLG1AsymSP implements AuctionRound<Double,Double> {
    PBEContext context;
    int subgame;
    public LLG1AsymSP(PBEContext context, int subgame) {
        this.context=context;
        this.subgame=subgame;
    }


    @Override
    public double computeUtility(int i, Double v, List<Strategy<Double, Double>> currentStrategies, Belief beliefDistribution, Double[] bids) {
                
        int winner=0;
        double maxBid=bids[0];
        int nBidders = bids.length;
        double secondBid=0.0;
                double reward=0.0;

        for(int k=1; k<nBidders;k++) {
            if(bids[k]>maxBid) {
                secondBid=maxBid;
                maxBid=bids[k];
                winner=k;
            } else if(bids[k] > secondBid){
                secondBid = bids[k];
            }
        }


        if(i==winner) {
                        reward = (v-secondBid);
        }


        return reward;
    }
}


