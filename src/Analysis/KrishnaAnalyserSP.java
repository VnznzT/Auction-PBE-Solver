package Analysis;

import algorithm.PBEContext;
import algorithm.SolverCluster;
import cluster.CacheKrishna;
import cluster.CacheKrishnaSP;
import domains.Belief;
import integrator.MCIntegrator;
import strategy.Strategy;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class KrishnaAnalyserSP implements Analyser{
    private final int mcSamples;
    private PBEContext context;

    public KrishnaAnalyserSP(PBEContext context, int mcSamples) {
        this.context = context;
        this.mcSamples = mcSamples;
    }

    @Override
    public GameAnalysis analyse(SolverCluster.Result result) {
        int nrounds = context.getIntParameter("nrounds");

                        Double[] l2_dist = new Double[nrounds];
        for (int j = 0; j < nrounds; j++) {
            l2_dist[j] = 0.0;
        }
        Random random = new Random();
        double minVal = context.getDoubleParameter("minVal");
        double maxVal = context.getDoubleParameter("maxVal");

        for(int i=0;i<mcSamples;i++) {
                                    
            

            for(int sg=0;sg<nrounds;sg++) {
                if(sg==0){
                    double value = random.nextDouble()*(maxVal-minVal)+minVal;
                                                                                                                        List<Strategy<Double,Double>> strategies = new ArrayList<>();
                    for (Strategy strategy : result.strategies) {
                        strategies.add((Strategy<Double,Double>) strategy);
                    }
                    calc_l2(l2_dist,sg,strategies,value);
                }
                else{
                                                            int gridsize = context.getIntParameter("SG0.OuterLoop.Gridsize");
                    int ref = random.nextInt(gridsize);
                    
                    CacheKrishnaSP cache = (CacheKrishnaSP) context.getCache();
                    List<Strategy<Double,Double>> strategies =cache.getStrategies(ref, sg);
                                        Double valStep = (maxVal-minVal)/gridsize;
                    Double maxBelief = minVal+ref*valStep;
                    double value = random.nextDouble()*(maxBelief-minVal)+minVal;
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
            double value = random.nextDouble()*(maxVal-minVal)+minVal;
            expUtility += integrator.computeExpectedUtility(0, value, result.strategies.get(0).getBid(value), beliefs, result.strategies, mcSamples/10);
        }
        expUtility = expUtility/(mcSamples/10);
        GameAnalysis gameAnalysis = new GameAnalysis(l2_dist, expUtility);
        return gameAnalysis;
    }

    private void calc_l2(Double[] l2Dist, int sg, List<Strategy<Double,Double>> strategies, double value) {
                        int n = context.getIntParameter("nBidders");
        String payRule = context.getStringParameter("payrule");
        int round = sg+1;         int nrounds = context.getIntParameter("nrounds");
        int inducedPlayer = 1;         if(Objects.equals(payRule, "first")){
                                                l2Dist[sg] += Math.pow(strategies.get(inducedPlayer).getBid(value)-(double)(n-nrounds)/(n-round+1.0)*value,2);

        } else if (Objects.equals(payRule, "second")) {
                                                l2Dist[sg] += Math.pow(strategies.get(inducedPlayer).getBid(value)-(double)(n-nrounds)/(n-round)*value,2);
        } else {
            throw new RuntimeException("payrule should be first or second");
        }

    }
}
