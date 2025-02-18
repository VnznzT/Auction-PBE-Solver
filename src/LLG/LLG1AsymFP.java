package LLG;

import algorithm.PBEContext;
import domains.AuctionRound;
import domains.Belief;
import strategy.Strategy;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class LLG1AsymFP implements AuctionRound<Double,Double> {
    PBEContext context;
    int subgame;
    public LLG1AsymFP(PBEContext context, int subgame) {
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
                maxBid=bids[k];
                winner=k;
            }
        }


        if(i==winner) {
                        reward = (v-maxBid);
        }


        return reward;
    }
}

