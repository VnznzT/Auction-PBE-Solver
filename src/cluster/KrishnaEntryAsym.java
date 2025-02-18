package cluster;

import Analysis.GameAnalysis;
import Analysis.KrishnaAnalyser;
import Analysis.KrishnaAnalyserSP;
import BR.BRCalculator1Dto1D;
import algorithm.PBEContext;
import algorithm.SolverCluster;
import distribution.Uniform1D;
import helpers.VolumeEstimator1D;
import integrator.MCIntegrator;
import krishna.*;
import pointwiseBR.PatternSearch;
import pointwiseBR.UnivariatePattern;
import randomsampling.CommonRandomGenerator;
import transition.TransitionKrishnaSecond3Rounds;
import updateRule.DiminishingUpdateRule;
import verification.BoundingVerifier1Dto1D;
import writer.StratExporter1to1;
import writer.UtilExporter1to1;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Map;


public class KrishnaEntryAsym {

	public static void main(String[] args) throws InterruptedException, IOException {
				/*
		 * Arguments are config file
		 * and output path 
		 * and index of subgame/belief
		 * 	config/Kokott_debug.config
			output/kokott_debug
		 */
		
				long startTime = System.nanoTime();
		
						
				PBEContext context = new PBEContext();
		String configfile = args[0];
		context.parseConfig(configfile);
		Path path = Paths.get(args[1]);
		int index = Integer.parseInt(args[2]);
		
				
		double minVal = context.getDoubleParameter("minVal");
		double maxVal = context.getDoubleParameter("maxVal");
		int nBidders = context.getIntParameter("nBidders");
		int gridsize = context.getIntParameter("SG0.OuterLoop.Gridsize");
		int nRounds = context.getIntParameter("nrounds");
		int subgame =0;
		GameAnalysis analysis = null;
		String payRule= context.getStringParameter("payrule");
		
				context.setPath(path);

				context.setAuctionSetting("krishna");
		
		
		
		subgame = nRounds - index/gridsize-1;

		context.setSubGame(subgame);
						context.setIntegrator(subgame, new MCIntegrator<Double,Double>(context,subgame));
				context.setOptimizer(subgame,new PatternSearch<Double,Double>(context, subgame, new UnivariatePattern()));
				context.setVolumeEstimator(subgame, new VolumeEstimator1D());
				context.setRng(subgame, new CommonRandomGenerator(nBidders));
						Double wMax = context.getDoubleParameter("wMax");
		Double wMin = context.getDoubleParameter("wMin");
								context.setUpdateRule(subgame, new DiminishingUpdateRule(wMin,wMax,context));
				context.setBR(subgame, new BRCalculator1Dto1D(context,subgame));
		
						int canonicalBidders[][] = new int[nRounds][];

		for (int k = 0; k < nRounds; k++) {
				canonicalBidders[k] = new int[nBidders - k];
				for (int j = 0; j < nBidders - k; j++) {
					canonicalBidders[k][j] = 0; 				}
			}

		context.setCanonicalBidders(canonicalBidders);

		context.setCanonicalBidders(canonicalBidders);	
				context.setDistributions(subgame, new Uniform1D());
		
				context.setSampler(subgame, new SamplerKrishna(context,subgame));
		
		
				if (payRule.equals("first")) {
									context.setAuctionRound(subgame, new KrishnaClusterFPSBasym(context,subgame, nRounds));
		}else if(payRule.equals("second")) {
			context.setAuctionRound(subgame, new KrishnaClusterSPSBasym(context,subgame, nRounds));
		}else {
			throw new IllegalArgumentException("payment rule msut be first or second"); 
		}
		
		
				context.setStrategyInitialiser(new StrategyInitialiserKrishna(context));
		context.setUtilityInitialiser(new UtilityInitialiserKrishna(context));	 			
						int beliefIndex = index % gridsize+1;		
				Double maxBelief;
		if(subgame>0) {
			Double valStep = (maxVal-minVal)/gridsize;
			maxBelief = minVal+beliefIndex*valStep;
		}else {
			maxBelief = maxVal;
		}
		Double beliefs[][]=new Double[nBidders-subgame][];

			for (int m=0;m<nBidders-subgame;m++) {
				beliefs[m]= new Double[] {minVal,maxBelief};
			}

		context.setInitialBeliefs(new BeliefKrishna(beliefs));

		
				context.setCallback(subgame,new CallbackKrishna(path.resolve(String.format("sg_%d",subgame)),context));
		

				context.setFP(new GameFPKrishna());

		
				context.setSubGameTransition(new TransitionKrishna());

		
				context.setVerifier(subgame,new BoundingVerifier1Dto1D(context,subgame));
		
		if(subgame==0) {
						Double[][] verificationTableInit = new Double[nBidders][2];
			for(int m=0;m<2;m++) {
				for(int l=0;l<nBidders;l++) {
					verificationTableInit[l][m]=0.0;
				}
			}
			context.setVerificationTable(verificationTableInit);
			
			context.setAnalyser(new KrishnaAnalyser(context,50000));


		}
		if(subgame < nRounds-1) {
								Cache<Double,Double> cache = new CacheKrishna(context);				cache.parse();
				context.setCache(cache);
		}
			
		
				context.setSolverCluster(new SolverCluster(context));
		
				context.setStratExporter(new StratExporter1to1());
		context.setUtilExporter(new UtilExporter1to1());
		
		
				SolverCluster.Result result;
		result = context.getSolverCluster().solve(subgame,context.getInitialBelief());
		
		/*have a writer method which fetches the strategies and writes them in a format
		* meaningful format.
		*/
		context.writeResult(result);

		if (subgame==0) {
						analysis = context.analyseResult(result);
		}

				long elapsedTime = System.nanoTime() - startTime;
		StringBuilder builder = new StringBuilder();

				if(subgame==0) {
			Double[] l2Dist = analysis.getL2Dist();
			Double expUtility = analysis.getExpUtility();
			for(int i=0;i<l2Dist.length;i++) {
				builder.append(String.format("l2_round_%d= %s\n", i, l2Dist[i]));
			}
			builder.append(String.format("expUtility= %s\n", expUtility));

		}


		builder.append("time =");
        builder.append(elapsedTime*1e-9);
        builder.append("\n");

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
