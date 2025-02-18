package ClusterRuns;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import BR.BRCalculator;
import algorithm.PBEContext;
import algorithm.Solver.IterationType;
import domains.Belief;
import strategy.Strategy;
import strategy.UnivariatePWCStrategy;
import transition.FingerPrint;
import utility.PWCUtility;
import utility.Utility;
import verification.Verifier;

public class SolverCluster2 {
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
	
	public PBEContext context;
	double highestEpsilon = 0.0;
	public int maxIters;
	double targetEpsilon;
	public Path output;
	
	public SolverCluster2(PBEContext context, Path output) {
		this.context=context;
		this.output=output;
	}


	public Result solve(int subgame, Belief beliefDistribution, int c1ref, int c2ref, int b1ref, int b2ref) throws IOException {
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
		List<Utility> utilities = context.makeInitialUtilities(subgame,beliefDistribution); 				callbackAfterIteration(subgame, 0, IterationType.INNER, beliefDistribution,strategies,utilities, targetEpsilon*5);

				
				while(iteration <= maxIters) {
						while(iteration < maxIters) {					context.activateConfig("SG"+subgame+".innerloop");
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
								utilities.set(j, util); 							}
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
				context.advanceRng(subgame);
				Result result = verify(strategies, utilities, context.getVerifier(subgame),subgame,beliefDistribution,c1ref,c2ref,b1ref,b2ref);
				callbackAfterIteration(subgame, iteration,IterationType.VERIFICATION,beliefDistribution, strategies, utilities, result.epsilon);
				exportUtil(utilities,c1ref,c2ref,b1ref,b2ref,subgame);
				exportStrat(strategies,c1ref,c2ref,b1ref,b2ref,subgame);
				
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
							utilities.set(j, util); 						}
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
			context.advanceRng(subgame);
			Result result = verify(strategies, utilities, context.getVerifier(subgame),subgame,beliefDistribution,c1ref,c2ref,b1ref,b2ref);
			callbackAfterIteration(subgame, iteration,IterationType.VERIFICATION,beliefDistribution, strategies, utilities, result.epsilon);
			exportUtil(utilities,c1ref,c2ref,b1ref,b2ref,subgame);
			exportStrat(strategies,c1ref,c2ref,b1ref,b2ref,subgame);
			
						context.activateConfig(prefixConfig);
			return result;	
		}
		context.advanceRng(subgame);
		Result result = verify(strategies, utilities, context.getVerifier(subgame),subgame,beliefDistribution,c1ref,c2ref,b1ref,b2ref);
		callbackAfterIteration(subgame, iteration,IterationType.VERIFICATION,beliefDistribution, strategies, utilities, result.epsilon);
		
		
										exportUtil(utilities,c1ref,c2ref,b1ref,b2ref,subgame);
		exportStrat(strategies,c1ref,c2ref,b1ref,b2ref,subgame);
				context.activateConfig(prefixConfig);
		return result;		
			

		
		
	}
	
	private Result verify(List<Strategy> strategies, List<Utility> utilities,Verifier verifier, int subgame, Belief beliefDistribution,int c1ref, int c2ref, int b1ref, int b2ref) throws IOException {
		double highestEpsilon=0.0;
		Map<Integer, Strategy> sMap = new HashMap<>();
		Map<Integer, Utility> uMap = new HashMap<>();
		int nBidders= context.getIntParameter("nBidders");
		int[] canonicalBidders = context.getCanonicalBidders(subgame);
		double[] epsilons = new double[nBidders];
		
		for (int i=0; i<nBidders; i++) {
			if (canonicalBidders[i] == i) {
				Verifier.Result result = verifier.computeVer(i, strategies, beliefDistribution);
				Strategy s = result.oldStrategy;	
				Utility util =result.oldUtility;
				highestEpsilon=Math.max(highestEpsilon, result.epsilon);
				epsilons[i] = Math.max(result.epsilon, epsilons[i]);
				sMap.put(i, s);
				uMap.put(i, util);	
			}else {
				epsilons[i] = epsilons[canonicalBidders[i]];
			}
			
		}
		
				Path exportPath=output.resolve(String.format("%d,%d", c1ref,c2ref));
		exportPath = exportPath.resolve(String.format("%d,%d,%d.epsilons", subgame,b1ref,b2ref));
		StringBuilder builder = new StringBuilder();
		for(int l=0;l<nBidders;l++) {
			builder.append(String.format("%7.10f ", epsilons[l]));
		}
		        Files.createDirectories(exportPath.getParent());
        
		Files.write(exportPath, builder.toString().getBytes(), 
			    StandardOpenOption.CREATE, 
			    StandardOpenOption.WRITE, 
			    StandardOpenOption.TRUNCATE_EXISTING);
		
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
	
	private void exportUtil(List<Utility> utilities, int c1ref, int c2ref, int b1ref, int b2ref, int subgame) throws IOException {
		StringBuilder builder = new StringBuilder();
		PWCUtility util = (PWCUtility) utilities.get(0);
		double[] v = util.values;
		double[] u = util.utilities;
		for(int l=1;l<v.length-1;l++) {
			builder.append(String.format("%7.6f ", v[l]));
		}
		builder.append("\n");
		
		for(int l=1;l<v.length-1;l++) {
			builder.append(String.format("%7.6f ", u[l]));
		}
		builder.append("\n");
		
				for(int k=1;k<utilities.size();k++) {
			util = (PWCUtility) utilities.get(k);
			u = util.utilities;
			for(int l=1;l<v.length-1;l++) {
				builder.append(String.format("%7.6f ", u[l]));
			}
			builder.append("\n");
		}
		
		Path exportPath=output.resolve(String.format("%d,%d", c1ref,c2ref));
		exportPath = exportPath.resolve(String.format("%d,%d,%d.util", subgame,b1ref,b2ref));
		        Files.createDirectories(exportPath.getParent());
		Files.write(exportPath, builder.toString().getBytes(), 
			    StandardOpenOption.CREATE, 
			    StandardOpenOption.WRITE, 
			    StandardOpenOption.TRUNCATE_EXISTING);
		
	}
	
	private void exportStrat(List<Strategy> strategies, int c1ref, int c2ref, int b1ref, int b2ref, int subgame) throws IOException {
		StringBuilder builder = new StringBuilder();
		UnivariatePWCStrategy strat = (UnivariatePWCStrategy) strategies.get(0);
		double[] v = strat.values;
		double[] b = strat.bids;
		for(int l=1;l<v.length-1;l++) {
			builder.append(String.format("%7.6f ", v[l]));
		}
		builder.append("\n");
		
		for(int l=1;l<v.length-1;l++) {
			builder.append(String.format("%7.6f ", b[l]));
		}
		builder.append("\n");
		
				for(int k=1;k<strategies.size();k++) {
			strat = (UnivariatePWCStrategy) strategies.get(k);
			b = strat.bids;
			for(int l=1;l<v.length-1;l++) {
				builder.append(String.format("%7.6f ", b[l]));
			}
			builder.append("\n");
		}
		
		Path exportPath=output.resolve(String.format("%d,%d", c1ref,c2ref));
		exportPath = exportPath.resolve(String.format("%d,%d,%d.strat", subgame,b1ref,b2ref));
		        Files.createDirectories(exportPath.getParent());
		Files.write(exportPath, builder.toString().getBytes(), 
			    StandardOpenOption.CREATE, 
			    StandardOpenOption.WRITE, 
			    StandardOpenOption.TRUNCATE_EXISTING);
		
	}
		
}

