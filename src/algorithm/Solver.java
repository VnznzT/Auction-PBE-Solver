package algorithm;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import BR.BRCalculator;
import domains.Belief;
import strategy.Strategy;
import transition.FingerPrint;
import utility.Utility;
import verification.Verifier;

public class Solver {
    public class Result {
        double epsilon;
        public List<Strategy> strategies;
        public List<Utility> utilities;
        public Result(double epsilon, List<Strategy> strategies, List<Utility> utilities) {
            this.epsilon = epsilon;
            this.strategies = strategies;
            this.utilities = utilities;
        }

    }

    public enum IterationType {INNER, OUTER, VERIFICATION}
    public PBEContext context;
    public Map<String, Result> cache;
    double highestEpsilon = 0.0;
    public int maxIters;
    double targetEpsilon;

    public Solver(PBEContext context) {
        this.context=context;
        this.cache = new HashMap<>(context.getIntParameter("SG1.OuterLoop.Gridsize")^3);     }


    /*the idea is that solve is the function which we can always rely on providing us with the equilibrium
     * This is more or less only called when the Mechanism tries to give a utility because then it needs to
     * understand the utility associated with the following subgame. At the start of the algorithm each
     * computation of the first round BR will trigger a solve routine for later rounds. As the results are cached
     * solve can rely over time on these cached values and must not compute the equilibria anew.
     */
    public Result solve(int subgame, Belief beliefDistribution) {
        if(!cache.containsKey(context.getFP().fp(subgame, beliefDistribution))){
                        int nBidders =context.getnBidders(subgame);
            int iteration = 1;
            int lastOuterIteration=1;
            int[] canonicalBidders = context.getCanonicalBidders(subgame);

                                    String prefixConfig = context.getActivatedConfig();
            context.activateConfig("SG"+subgame+".innerloop");
            maxIters = context.getIntParameter("maxiters");
            targetEpsilon =context.getDoubleParameter("epsilon");

                        BRCalculator brc = context.getBR(subgame);

            List<Strategy> strategies = context.makeInitialStrategies(subgame,beliefDistribution);
            List<Utility> utilities = context.makeInitialUtilities(subgame,beliefDistribution);                         callbackAfterIteration(subgame, 0, IterationType.INNER, beliefDistribution,strategies,utilities, targetEpsilon*5);

            
                        while(iteration <= maxIters) {
                                while(iteration < maxIters) {	                    context.activateConfig("SG"+subgame+".innerloop");
                    highestEpsilon = 0.0;
                    for (int i=0; i<nBidders; i++) {
                        if (canonicalBidders[i] == i) {
                            BRCalculator.Result result = brc.computeBR(i, strategies, beliefDistribution);
                            Strategy s = result.s;
                            Utility util =result.util;
                            highestEpsilon = Math.max(highestEpsilon, result.epsilonAbs);

                            s=callbackAfterBR(subgame,iteration,beliefDistribution, s, highestEpsilon);

                                                        for (int j=0; j<nBidders; j++) {
                                if (canonicalBidders[j]==i) {
                                    strategies.set(j, s);
                                    utilities.set(j, util);                                 }
                            }
                        }
                    }
                    context.advanceRng(subgame);
                    callbackAfterIteration(subgame, iteration,IterationType.INNER,beliefDistribution, strategies, utilities, highestEpsilon);
                    ++iteration;
                    if (highestEpsilon <= 0.8*targetEpsilon && iteration >= lastOuterIteration + 3) {
                        break;
                    }
                }
                                if (iteration > maxIters) {
                    highestEpsilon=Double.POSITIVE_INFINITY;
                                        Result result = new Result(highestEpsilon, strategies, utilities);
                    cache.put(context.getFP().fp(subgame, beliefDistribution), result);

                                        context.activateConfig(prefixConfig);
                    return result;
                }
                highestEpsilon=0.0;
                lastOuterIteration=iteration;
                context.activateConfig("SG"+subgame+".outerloop");
                for (int i=0; i<nBidders; i++) {
                    if (canonicalBidders[i] == i) {
                        BRCalculator.Result result = brc.computeBR(i, strategies, beliefDistribution);
                        Strategy s = result.s;
                        Utility util =result.util;
                        highestEpsilon = Math.max(highestEpsilon, result.epsilonAbs);

                        s=callbackAfterBR(subgame,iteration, beliefDistribution, s, highestEpsilon);

                                                for (int j=0; j<nBidders; j++) {
                            if (canonicalBidders[j]==i) {
                                strategies.set(j, s);
                                utilities.set(j, util);
                            }
                        }
                    }
                }
                context.advanceRng(subgame);
                callbackAfterIteration(subgame, iteration,IterationType.OUTER,beliefDistribution, strategies, utilities, highestEpsilon);
                ++iteration;
                if(highestEpsilon<=targetEpsilon) {
                    break;
                }

            }
            
            context.activateConfig("SG"+subgame+".verificationstep");
            boolean outerloopConverged = highestEpsilon <= targetEpsilon;
            if (context.getVerifier(subgame) == null || !outerloopConverged) {
                highestEpsilon=Double.POSITIVE_INFINITY;
                                Result result = new Result(highestEpsilon, strategies, utilities);
                cache.put(context.getFP().fp(subgame, beliefDistribution), result);

                                context.activateConfig(prefixConfig);
                return result;
            }
            context.advanceRng(subgame);
            Result result = verify(strategies, utilities, context.getVerifier(subgame),subgame,beliefDistribution);
            callbackAfterIteration(subgame, iteration,IterationType.VERIFICATION,beliefDistribution, strategies, utilities, highestEpsilon);


                                                cache.put(context.getFP().fp(subgame, beliefDistribution), result);

                        context.activateConfig(prefixConfig);
            return result;

        }else {
                        return cache.get(context.getFP().fp(subgame, beliefDistribution));
        }



    }

    private Result verify(List<Strategy> strategies, List<Utility> utilities,Verifier verifier, int subgame, Belief beliefDistribution) {
        double highestEpsilon=0.0;
        Map<Integer, Strategy> sMap = new HashMap<>();
        Map<Integer, Utility> uMap = new HashMap<>();
        int nBidders= context.getIntParameter("nBidders");
        int[] canonicalBidders = context.getCanonicalBidders(subgame);
        Double[][] verificationTable = context.getVerificationTable();

        for (int i=0; i<nBidders; i++) {
            if (canonicalBidders[i] == i) {
                Verifier.Result result = verifier.computeVer(i, strategies, beliefDistribution);
                Strategy s = result.oldStrategy;
                Utility util =result.oldUtility;
                highestEpsilon=Math.max(highestEpsilon, result.epsilon);
                verificationTable[i][subgame] = Math.max(result.epsilon, verificationTable[i][subgame]);
                sMap.put(i, s);
                uMap.put(i, util);

            }
        }
        for (int i=0; i<nBidders; i++) {
            strategies.set(i, sMap.get(canonicalBidders[i]));
            utilities.set(i, uMap.get(canonicalBidders[i]));
        }
        return new Result(highestEpsilon,strategies, utilities);
    }


    private void callbackAfterIteration(int subGame, int iteration,IterationType type, Belief beliefDistribution, List<Strategy> strategies, List<Utility> utilities, double epsilon) {
        if (context.getCallback(subGame) != null) {
            context.getCallback(subGame).afterIteration(subGame, iteration, type, beliefDistribution, strategies, utilities, epsilon);
        }
    }

    private Strategy callbackAfterBR(int subGame, int iteration, Belief beliefDistribution, Strategy strategy, double epsilon) {
        if (context.getCallback(subGame) != null) {
            return context.getCallback(subGame).afterBR(subGame, iteration,beliefDistribution, strategy, epsilon);
        }else {
            throw new RuntimeException("wrong subgame");
        }
    }

}

