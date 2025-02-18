package Analysis;

import algorithm.PBEContext;
import algorithm.SolverCluster;
import cluster.CacheKrishna;
import cluster.CacheKrishnaReserve;
import domains.Belief;
import integrator.MCIntegrator;
import strategy.Strategy;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class KrishnaReserveAnalyser implements Analyser{
    private final int mcSamples;
    private PBEContext context;

    public KrishnaReserveAnalyser(PBEContext context, int mcSamples) {
        this.context = context;
        this.mcSamples = mcSamples;
    }

    @Override
    public GameAnalysis analyse(SolverCluster.Result result) {
        int nrounds = context.getIntParameter("nrounds");

                        Double[] l2_dist = new Double[3];
        for (int j = 0; j < 3; j++) {
            l2_dist[j] = 0.0;
        }
        Random random = new Random();
        double minVal = context.getDoubleParameter("minVal");
        double maxVal = context.getDoubleParameter("maxVal");
        double[] reservePrices = context.getReservePrices();

        for(int i=0;i<mcSamples;i++) {
                                    
            

            for(int sg=0;sg<3;sg++) {
                if(sg==0){
                    double value = random.nextDouble()*(maxVal-minVal)+minVal;
                                                                                                                        List<Strategy<Double,Double>> strategies = new ArrayList<>();
                    for (Strategy strategy : result.strategies) {
                        strategies.add((Strategy<Double,Double>) strategy);
                    }
                    calc_l2(l2_dist,sg,strategies,value);
                }else if(sg==1 || sg==2){
                                                            int gridsize = context.getIntParameter("SG0.OuterLoop.Gridsize")/2;
                    int ref = random.nextInt(gridsize);                                         CacheKrishnaReserve cache = (CacheKrishnaReserve) context.getCache();
                    List<Strategy<Double,Double>> strategies =cache.getStrategies(ref+gridsize, sg);
                                        double r = 0.5;
                    Double valStep = (maxVal-r)/gridsize;
                    Double maxBelief = r+ref*valStep;
                    double value = random.nextDouble()*(maxBelief-r)+r;
                    calc_l2(l2_dist,sg,strategies,value);
                }else {
                    throw new RuntimeException("subgame should be 0,1 or 2");
                }
            }
        }
                for(int sg=0;sg<3;sg++) {
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

        int nrounds = context.getIntParameter("nrounds");
        int inducedPlayer = 0;         if (n != 3 && n!= 4){
            throw new RuntimeException("everything currently set for n=3,4");
        }
        double r = context.getReservePrices()[1];         if(Objects.equals(payRule, "first")){

            if(sg==0){


                if (value<=r) {
                                                            l2Dist[sg] += Math.pow(strategies.get(inducedPlayer).getBid(value)-(double)(n-1.0)/(n)*value,2);
                } else {
                                                            l2Dist[sg] += Math.pow(strategies.get(inducedPlayer).getBid(value)-((double)(n-2.0)/(n)*value+Math.pow(r,n-1.0)/Math.pow(value,n-2.0)-(n-1.0)/n*Math.pow(r,n)/Math.pow(value,n-1.0)),2);
                }

            } else if (sg==1) {
                                                                if (value>r){
                    if(n==3){
                        l2Dist[sg] += Math.pow(strategies.get(inducedPlayer).getBid(value)-(value-0.5*value+0.5*Math.pow(r,2)/value),2);
                    }else if(n==4){
                        l2Dist[sg] += Math.pow(strategies.get(inducedPlayer).getBid(value) - (value - 1 / 3.0 * value + 1 / 3.0 * Math.pow(r, 3) / Math.pow(value, 2)), 2);
                    }


                }
            } else if (sg==2){
                                                                if(n==3){
                    if (value>r) {
                        l2Dist[sg] += Math.pow(strategies.get(inducedPlayer).getBid(value) - (value - 1 / 3.0 * value + 1 / 3.0 * Math.pow(r, 3) / Math.pow(value, 2)), 2);
                    }
                }else if(n==4){
                    if (value>r) {
                        l2Dist[sg] += Math.pow(strategies.get(inducedPlayer).getBid(value) - (value - 1 / 4.0 * value + 1 / 4.0 * Math.pow(r, 4) / Math.pow(value, 3)), 2);
                    }
                }

            }

        } else if (Objects.equals(payRule, "second")) {
                    if (sg == 0) {
                                if(value<=r){
                                        l2Dist[sg] += Math.pow(strategies.get(inducedPlayer).getBid(value)-value,2);
                }else{
                    l2Dist[sg] += Math.pow(strategies.get(inducedPlayer).getBid(value)-((n-2.0)*Math.pow(value,n-1.0)+Math.pow(r,n-1.0))/((n-1.0)*Math.pow(value,(n-2.0))),2);
                }


            } else if (sg == 1 || sg == 2) {
                                if (value>r){
                    l2Dist[sg] += Math.pow(strategies.get(inducedPlayer).getBid(value)-value,2);
                }

            } else {
                throw new RuntimeException("subgame should be 0,1 or 2");
            }
        } else {
            throw new RuntimeException("payrule should be first or second");
        }

    }
}
