package cluster;

import Analysis.GameAnalysis;
import Analysis.KokottAnalyser;
import BR.BRCalculator1Dto1D;
import BR.BRCalculator1Dto2D;
import algorithm.PBEContext;
import algorithm.SolverCluster;
import distribution.Uniform1D;
import distribution.Uniform2D;
import domains.Belief;
import helpers.VolumeEstimator1D;
import integrator.MCIntegrator;
import kokottetal.*;
import pointwiseBR.BoxPattern2Dindep;
import pointwiseBR.PatternSearch;
import pointwiseBR.UnivariatePattern;
import randomsampling.CommonRandomGenerator;
import transition.GameFPKokott;
import transition.SubGameTransitionKokott;
import updateRule.DiminishingUpdateRule;
import updateRule.MultivariateDimishingUpdateRule;
import verification.BoundingVerifier1Dto1D;
import verification.BoundingVerifier1Dto2D;
import writer.StratExporter1to1;
import writer.UtilExporter1to1;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Map;


public class KokottEntryAsym {

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
		
				
		double efficiency = context.getDoubleParameter("efficiency");
		double minCost = context.getDoubleParameter("minCost");
		double maxCost = context.getDoubleParameter("maxCost");
		int nBidders = context.getIntParameter("nBidders");
		int gridsize = context.getIntParameter("SG0.OuterLoop.Gridsize");
		int subgame =0;
		GameAnalysis analysis = null;
		
				context.setPath(path);


				context.setAuctionSetting("kokott");
		
				if (index < gridsize) {
			subgame = 1;
			context.setSubGame(1);
						
						context.setIntegrator(1, new MCIntegrator<Double,Double>(context,1));
			
						context.setOptimizer(1,new PatternSearch<Double,Double>(context, 1, new UnivariatePattern()));
			
			
						context.setVolumeEstimator(1, new VolumeEstimator1D());
			
						context.setRng(1, new CommonRandomGenerator(nBidders));
			
									Double wMax = context.getDoubleParameter("wMax");
			Double wMin = context.getDoubleParameter("wMin");
									context.setUpdateRule(subgame, new DiminishingUpdateRule(wMin,wMax,context));


						context.setBR(1, new BRCalculator1Dto1D(context,1));
			

															int canonicalBidders[][]=new int[2][nBidders];
			for (int m=0;m<nBidders;m++) {
				canonicalBidders[0][m]= 0;
				canonicalBidders[1][m]=1;
			}
			canonicalBidders[1][0]=0; 			context.setCanonicalBidders(canonicalBidders);
				
						context.setDistributions(1, new Uniform1D());
			
						context.setSampler(1, new SamplerKokottSG1(context,1));
			
						context.setAuctionRound(1, new KokottSG1Asym(context,1));
			
						context.setStrategyInitialiser(new StrategyInitialiserKokott(context));
			context.setUtilityInitialiser(new UtilityInitialiserKokott(context));		
				
									
			Double[] beliefAbtWinner= new Double[2];
			Double[] beliefAbtLoser = new Double[2];
			
						Double costStep = (maxCost-minCost)/gridsize;
			Double lowBelief = minCost + index*costStep;
			
			beliefAbtWinner[0]=lowBelief;
			beliefAbtLoser[0]=lowBelief;
			
			beliefAbtWinner[1]=lowBelief+costStep;
			beliefAbtLoser[1]=maxCost;
			
						Double[][] newBeliefArray = new Double[nBidders][2];
			
						newBeliefArray[0][0]=beliefAbtWinner[0];
			newBeliefArray[0][1]=beliefAbtWinner[1];
			
			for(int k=1; k<nBidders;k++) {
				newBeliefArray[k][0]=beliefAbtLoser[0];
				newBeliefArray[k][1]=beliefAbtLoser[1];
				if(newBeliefArray[k][1]<newBeliefArray[k][0]) {
					throw new RuntimeException("Beliefupdate, tried to update inconsistent beliefs. Upper interval needs to be higher than lower interval end");
				}				
			}
			Belief newBelief = new BeliefKokott(newBeliefArray);
						context.setInitialBeliefs(newBelief);
			
						context.setCallback(1, new CallbackKokottSG1(path.resolve(String.format("round1")),context));

						context.setFP(new GameFPKokott());
			
						context.setSubGameTransition(new SubGameTransitionKokott());
			
						context.setVerifier(1,new BoundingVerifier1Dto1D(context,1));
			
						context.setSolverCluster(new SolverCluster(context));
			
						context.setStratExporter(new StratExporter1to1());
			context.setUtilExporter(new UtilExporter1to1());
						SolverCluster.Result result;
			result = context.getSolverCluster().solve(1,context.getInitialBelief());
			
			/*have a writer method which fetches the strategies and writes them in a format
			* meaningful format.
			*/
			context.writeResult(result);
		}else {
			subgame = 0;
			context.setSubGame(0);
						context.setIntegrator(0,new MCIntegrator<Double,Double[]>(context,0));
			
						context.setOptimizer(0, new PatternSearch<Double,Double[]>(context, 0, new BoxPattern2Dindep()));
			
						context.setVolumeEstimator(0, new VolumeEstimator1D());
		
						context.setRng(0, new CommonRandomGenerator(nBidders));
			
						Double wMax = context.getDoubleParameter("wMax");
			Double wMin = context.getDoubleParameter("wMin");

						context.setUpdateRule(0,new MultivariateDimishingUpdateRule(wMin,wMax,context));
						context.setBR(0,new BRCalculator1Dto2D(context,0));
			
															int canonicalBidders[][]=new int[2][nBidders];
			for (int m=0;m<nBidders;m++) {
				canonicalBidders[0][m]= 0;
				canonicalBidders[1][m]=1;
			}
			canonicalBidders[1][0]=0; 			context.setCanonicalBidders(canonicalBidders);

						context.setDistributions(0, new Uniform2D());
			
						context.setSampler(0, new SamplerKokottSG0(context,0));
			
						context.setAuctionRound(0, new KokottSG0ClusterAsym(context));
			
						context.setStrategyInitialiser(new StrategyInitialiserKokott(context));
			context.setUtilityInitialiser(new UtilityInitialiserKokott(context));		
		
									Double beliefs[][]=new Double[nBidders][];
			for (int m=0;m<nBidders;m++) {
					beliefs[m]= new Double[] {minCost,maxCost};
			}
			context.setInitialBeliefs(new BeliefKokott(beliefs));
			
						context.setFP(new GameFPKokott());
			
						context.setSubGameTransition(new SubGameTransitionKokott());
			
						context.setVerifier(0,new BoundingVerifier1Dto2D(context,0));
			
						context.setCallback(0,new CallbackKokottSG0(path.resolve(String.format("round0")),context));
			
						context.setSolverCluster(new SolverCluster(context));
			
						Double[][] verificationTableInit = new Double[nBidders][2];
			for(int m=0;m<2;m++) {
				for(int l=0;l<nBidders;l++) {
					verificationTableInit[l][m]=0.0;
				}
			}
			context.setVerificationTable(verificationTableInit);
			
						Cache<Double,Double> cache = new CacheKokott(context);
			cache.parse();
			context.setCache(cache);

						context.setAnalyser(new KokottAnalyser(context,50000));
			
						SolverCluster.Result result;
			result = context.getSolverCluster().solve(0,context.getInitialBelief());
			
			/*have a writer method which fetches the strategies and writes them in a format
			* meaningful format.
			*/
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
		builder.append("efficiency =");
		builder.append(efficiency);
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
