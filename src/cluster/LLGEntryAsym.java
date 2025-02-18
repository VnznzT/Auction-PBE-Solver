package cluster;

import Analysis.GameAnalysis;
import BR.BRCalculator1Dto1D;
import LLG.*;
import algorithm.PBEContext;
import algorithm.SolverCluster;
import distribution.Uniform1D;
import helpers.VolumeEstimator1D;
import integrator.MCIntegrator;
import pointwiseBR.PatternSearch;
import pointwiseBR.UnivariatePattern;
import randomsampling.CommonRandomGenerator;
import transition.GameFPLLG;
import transition.SubGameTransitionLLG;
import transition.SubGameTransitionLLGAsymFP;
import transition.SubGameTransitionLLGAsymSP;
import updateRule.DiminishingUpdateRule;
import verification.BoundingVerifier1Dto1D;
import writer.StratExporter1to1Asym;
import writer.UtilExporter1to1Asym;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Map;

public class LLGEntryAsym {

    public static void main(String[] args) throws FileNotFoundException {
        long startTime = System.nanoTime();

        PBEContext context = new PBEContext();
        String configfile = args[0];
        context.parseConfig(configfile);
        Path path = Paths.get(args[1]);
        int index = Integer.parseInt(args[2]);

                double minValLocal = context.getDoubleParameter("minValLocal");
        double maxValLocal = context.getDoubleParameter("maxValLocal");
        double minValGlobal = context.getDoubleParameter("minValGlobal");
        double maxValGlobal = context.getDoubleParameter("maxValGlobal");
        int nBidders = context.getIntParameter("nBidders");
        assert(nBidders==2);
        int gridsize = context.getIntParameter("SG0.InnerLoop.Gridsize");
        int nRounds = context.getIntParameter("nrounds");
        int subgame =0;
        GameAnalysis analysis = null;
        String payRule= context.getStringParameter("payrule");

                if(!payRule.equals("first") && !payRule.equals("second")){
            throw new IllegalArgumentException("payrule must be first or second");
        }

                context.setPath(path);

                context.setAuctionSetting("LLG");


        subgame = 1 - (index/gridsize);
                context.setSubGame(subgame);
        context.setIntegrator(subgame, new MCIntegrator<Double,Double>(context,subgame));
        context.setOptimizer(subgame,new PatternSearch<Double,Double>(context, subgame, new UnivariatePattern()));
        context.setVolumeEstimator(subgame, new VolumeEstimator1D());
        context.setRng(subgame, new CommonRandomGenerator(nBidders));

        Double wMax = context.getDoubleParameter("wMax");
        Double wMin = context.getDoubleParameter("wMin");

        context.setUpdateRule(subgame, new DiminishingUpdateRule(wMin,wMax,context));
                context.setBR(subgame, new BRCalculator1Dto1D(context,subgame));

        int canonicalBidders[][] = new int [2][];
                canonicalBidders[0]= new int[2];
        canonicalBidders[1]= new int[2];

        for (int j=0; j<2;j++){
            canonicalBidders[0][j]=j;
            canonicalBidders[1][j]=j;
            
        }
        context.setCanonicalBidders(canonicalBidders);

        context.setDistributions(subgame, new Uniform1D());

        context.setSampler(subgame, new SamplerLLG(context,subgame));

        if (subgame==0){
            if (payRule.equals("first")){
                context.setAuctionRound(subgame, new LLG0AsymFP(context,subgame));
            }else {
                context.setAuctionRound(subgame, new LLG0AsymSP(context,subgame));
            }
        }else {
            if (payRule.equals("first")){
                context.setAuctionRound(subgame, new LLG1AsymFP(context,subgame));
            }else {
                context.setAuctionRound(subgame, new LLG1AsymSP(context,subgame));
            }
        }

                context.setStrategyInitialiser( new StrategyInitialiserLLG(context));
        context.setUtilityInitialiser( new UtilityInitialiserLLG(context));

                int beliefIndex = index%gridsize +1;

        
                Double maxBeliefGlobal = null;
        Double minBeliefGlobal = null;
        if (subgame==0){
            maxBeliefGlobal = maxValGlobal;
            minBeliefGlobal = minValGlobal;
        }else {
            Double valStep = (maxValGlobal - minValGlobal)/gridsize;
            if(payRule.equals("first")){
                maxBeliefGlobal = minValGlobal + beliefIndex*valStep;
                minBeliefGlobal = maxBeliefGlobal - valStep;
            }else if (payRule.equals("second")) {
                minBeliefGlobal = minValGlobal + (beliefIndex - 1) * valStep;
                maxBeliefGlobal = maxValGlobal;
            }
        }
        Double beliefs[][] = new Double[2][];
        beliefs[0]= new Double[]{minBeliefGlobal,maxBeliefGlobal};
        beliefs[1]= new Double[]{minValLocal,maxValLocal};

        context.setInitialBeliefs(new BeliefLLG(beliefs));

                context.setCallback(subgame,new CallbackLLG(path,context));

                context.setFP(new GameFPLLG());
        if (payRule.equals("first")){
            context.setSubGameTransition(new SubGameTransitionLLGAsymFP());
        }else if (payRule.equals("second")){
            context.setSubGameTransition(new SubGameTransitionLLGAsymSP());
        }
        context.setVerifier(subgame, new BoundingVerifier1Dto1D(context,subgame));

        if(subgame==0) {
            Double[][] verificationTableInit = new Double[2][2];
                        for (int i=0;i<2;i++){
                for (int j=0;j<2;j++){
                    verificationTableInit[i][j]=0.0;
                }
            }
            context.setVerificationTable(verificationTableInit);
                        Cache<Double,Double> cache = new CacheLLGAsym(context);
            cache.parse();
            context.setCache(cache);
        }
        context.setSolverCluster(new SolverCluster(context));
                context.setStratExporter(new StratExporter1to1Asym());
        context.setUtilExporter(new UtilExporter1to1Asym());

                SolverCluster.Result result;
        result = context.getSolverCluster().solve(subgame,context.getInitialBelief());

        context.writeResult(result);

        long elapsedTime = System.nanoTime() - startTime;
        StringBuilder builder = new StringBuilder();

        builder.append("Time= "+elapsedTime*1e-9+"\n");

                for (Map.Entry<String, String> entry : context.config.entrySet()) {
            builder.append(entry.getKey())
                    .append("=")
                    .append(entry.getValue())
                    .append("\n");         }

        String s = builder.toString();
        Path output = path.resolve(String.format("setup.txt"));

        if (subgame==0){
            try {
                Files.createDirectories(output.getParent());

                Files.write(
                        output, s.getBytes(),
                        StandardOpenOption.CREATE,
                        StandardOpenOption.WRITE,
                        StandardOpenOption.TRUNCATE_EXISTING
                );
            } catch (IOException e) {
            }
        }else{
            String name = context.getFP().fp(subgame, context.getInitialBelief());
            output = path.resolve(String.format("sg_%d",subgame)).resolve(String.format("setup_%s.txt",name));
            try {
                Files.createDirectories(output.getParent());

                Files.write(
                        output, s.getBytes(),
                        StandardOpenOption.CREATE,
                        StandardOpenOption.WRITE,
                        StandardOpenOption.TRUNCATE_EXISTING
                );
            } catch (IOException e) {
            }
        }
        System.out.println(s.toString());

    }
}
