package Analysis;

import algorithm.PBEContext;
import algorithm.SolverCluster;
import domains.Belief;
import integrator.MCIntegrator;
import strategy.PWCStrategy1Dto2D;
import strategy.Strategy;
import strategy.UnivariatePWCStrategy;

import java.util.List;
import java.util.Random;

public class KokottAnalyser implements Analyser{
    private final int mcSamples;
    private PBEContext context;

    public KokottAnalyser(PBEContext context, int mcSamples) {
        this.context = context;
        this.mcSamples = mcSamples;
    }

    @Override
    public GameAnalysis analyse(SolverCluster.Result result) {
        int nrounds = 2;

                        Double[] l2_dist = new Double[nrounds];
        for (int j = 0; j < nrounds; j++) {
            l2_dist[j] = 0.0;
        }
        Random random = new Random();
        double minCost = context.getDoubleParameter("minCost");
        double maxCost = context.getDoubleParameter("maxCost");

        for(int i=0;i<mcSamples;i++) {
                                    
            

            for(int sg=0;sg<nrounds;sg++) {
                if(sg==0){
                    double value = random.nextDouble()*(maxCost-minCost)+minCost;
                                                                                                                        calc_l2(l2_dist,sg,result.strategies,value);
                }
                else{
                                                            int gridsize = context.getIntParameter("SG0.OuterLoop.Gridsize");
                    int ref = random.nextInt(gridsize);
                    List<Strategy> strategies = context.getCache().getStrategies(ref);
                                        Double costStep = (maxCost-minCost)/gridsize;
                    Double lowBelief = minCost + ref*costStep;
                    double value = random.nextDouble()*(maxCost-lowBelief)+lowBelief;
                    calc_l2(l2_dist,sg,strategies,value);
                }
            }
        }
                for(int sg=0;sg<nrounds;sg++) {
            l2_dist[sg] = Math.sqrt(l2_dist[sg]/mcSamples);
        }
                        double expUtility=0.0;
                        
        Belief beliefs = context.getInitialBelief();
        MCIntegrator integrator = (MCIntegrator)    context.getIntegrator(0);
                        for (int i = 0; i < mcSamples/10; i++) {
            double value = random.nextDouble()*(maxCost-minCost)+minCost;
            expUtility += integrator.computeExpectedUtility(0, value, result.strategies.get(0).getBid(value), beliefs, result.strategies, mcSamples/10);
        }
        expUtility = expUtility/(mcSamples/10);
        GameAnalysis gameAnalysis = new GameAnalysis(l2_dist, expUtility);
        return gameAnalysis;
    }

    private void calc_l2(Double[] l2Dist, int sg, List<Strategy> strategies, double value) {
                        int n = context.getIntParameter("nBidders");
        double c = context.getDoubleParameter("efficiency");
        if (sg==0){
                                    PWCStrategy1Dto2D strategy = (PWCStrategy1Dto2D) strategies.get(0);
                                    l2Dist[sg] += Math.pow(strategy.getBid(value)[1]-c*(((n-1)*value+2)/n + (2-value)/n),2);         } else if (sg==1) {
                        int inducedPlayer = 1;
                                    UnivariatePWCStrategy strategy = (UnivariatePWCStrategy) strategies.get(inducedPlayer);
            l2Dist[sg] += Math.pow(strategy.getBid(value)-c*(value+(2-value)/(n-1)),2);
        }else {
            throw new RuntimeException("subgame should be 0 or 1");
        }
    }
}
