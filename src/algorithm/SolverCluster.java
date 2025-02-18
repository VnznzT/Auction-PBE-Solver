package algorithm;
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
import domains.Belief;
import strategy.Strategy;
import transition.FingerPrint;
import utility.Utility;
import verification.Verifier;

import algorithm.Solver.IterationType;

public class SolverCluster {
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
	
	public SolverCluster(PBEContext context) {
		this.context=context;
		}


	/*the idea is that solve is the function which we can always rely on providing us with the equilibrium
	* This is more or less only called when the Mechanism tries to give a utility because then it needs to
	* understand the utility associated with the following subgame. At the start of the algorithm each
	* computation of the first round BR will trigger a solve routine for later rounds. As the results are cached
	* solve can rely over time on these cached values and must not compute the equilibria anew.
	*/
	public Result solve(int subgame, Belief beliefDistribution) {

				
		
		int iteration = 1;
		int lastOuterIteration=1;
		int[] canonicalBidders = context.getCanonicalBidders(subgame);
		int nBidders =context.getnBidders(subgame);
		
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
								utilities.set(j, util);
							}
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

		context.advanceRng(subgame);
		Result result = verify(strategies, utilities, context.getVerifier(subgame),subgame,beliefDistribution);
		callbackAfterIteration(subgame, iteration,IterationType.VERIFICATION,beliefDistribution, strategies, utilities, highestEpsilon);
		
		
										
				context.activateConfig(prefixConfig);
		return result;		
			

		
	}
	
	private Result verify(List<Strategy> strategies, List<Utility> utilities,Verifier verifier, int subgame, Belief beliefDistribution) {
		double highestEpsilon=0.0;
		Map<Integer, Strategy> sMap = new HashMap<>();
		Map<Integer, Utility> uMap = new HashMap<>();
		Map<Integer, Utility> prevUMap = new HashMap<>();
		List<Utility> prevUtilities = new ArrayList<>(utilities);
		
		int[] canonicalBidders = context.getCanonicalBidders(subgame);
		int nBidders= canonicalBidders.length;
		Double[][] verificationTable = context.getVerificationTable();
		double[] epsilons = new double[nBidders];
		
		for (int i=0; i<nBidders; i++) {
			if (canonicalBidders[i] == i) {
				Verifier.Result result = verifier.computeVer(i, strategies, beliefDistribution);
				Strategy s = result.oldStrategy;	
				Utility util =result.oldUtility;
				Utility prevUtil = result.utility;
				highestEpsilon=Math.max(highestEpsilon, result.epsilon);
				epsilons[i] = Math.max(result.epsilon, epsilons[i]);
				if(subgame==0) {
					verificationTable[i][subgame] = Math.max(result.epsilon, verificationTable[i][subgame]);				}
				sMap.put(i, s);
				uMap.put(i, util);
				prevUMap.put(i, prevUtil);

			}else {
								epsilons[i] = epsilons[canonicalBidders[i]];
				if(subgame==0) {
					verificationTable[i][subgame]=verificationTable[canonicalBidders[i]][subgame];
				}
			}
		}
		for (int i=0; i<nBidders; i++) {
			strategies.set(i, sMap.get(canonicalBidders[i]));	
			utilities.set(i, uMap.get(canonicalBidders[i]));
			prevUtilities.set(i, prevUMap.get(canonicalBidders[i]));
		}
		if(subgame > 0) {
						String fp = this.context.getFP().fp(subgame, beliefDistribution);
			Path path = context.getPath().resolve(String.format("%d_ver", subgame));
			exportEpsilon(epsilons, fp, path,subgame);
			context.getStratExporter().exportStrat(strategies,fp,path);
			context.getUtilExporter().exportUtil(utilities,fp,path);
						context.getUtilExporter().exportUtil(prevUtilities,fp,path.resolve("prev"));
				
		}else { 
			String fp = this.context.getFP().fp(subgame, beliefDistribution);
			Path path = context.getPath().resolve(String.format("%d_ver", subgame));
			exportEpsilon(epsilons, fp, path,subgame);
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

	private void exportEpsilon(double[] epsilons, String fp, Path path, int subgame) {
		Path exportPath=path.resolve(String.format("%s.epsilon", fp));
		StringBuilder builder = new StringBuilder();
		for(int l=0;l<epsilons.length;l++) {
			builder.append(String.format("%7.10f ", epsilons[l]));
		}
				try {
        Files.createDirectories(exportPath.getParent());
        
		Files.write(exportPath, builder.toString().getBytes(), 
			    StandardOpenOption.CREATE, 
			    StandardOpenOption.WRITE, 
			    StandardOpenOption.TRUNCATE_EXISTING);
		} catch(IOException e) {
			System.err.println("An error occurred while writing verifcation epsilons: " + e.getMessage());
		}
		
		if(subgame==0) {
			Double highestEpsilon = 0.0;
			Double[][] verificationTable = context.getVerificationTable();
			double[] reservePrices = context.getReservePrices();

			if(context.getAuctionSetting()=="LLG"){
				double epsilon0 = verificationTable[0][0]+verificationTable[0][1];
				double epsilon1 = verificationTable[1][0];
				double epsilon2 = verificationTable[0][1];
				highestEpsilon = Math.max(epsilon0, Math.max(epsilon1, epsilon2));

			} else if  (context.getAuctionSetting()=="krishna_reserve") {
																for (int i=0; i<context.getnBidders(subgame);i++) {
					double sg0_eps = verificationTable[i][0];
					double sg1_eps = verificationTable[i][1];
					double sg2_eps = verificationTable[i][2];

					double epsilon = Math.max(sg0_eps + sg1_eps, sg0_eps + sg2_eps);
					highestEpsilon=Math.max(highestEpsilon, epsilon);
				}
			}else{
								for (int i=0; i<context.getnBidders(subgame);i++) {
					Double epsilon =0.0;
					for(int sg=0;sg<verificationTable[i].length;sg++) {
						epsilon+=verificationTable[i][sg];
					}
					highestEpsilon=Math.max(highestEpsilon, epsilon);
				}
			}

			exportPath=path.resolve(String.format("verified.epsilon"));
			StringBuilder builder2 = new StringBuilder();
			builder2.append(String.format("%7.10f ", highestEpsilon));
						try {
	        Files.createDirectories(exportPath.getParent());
	        
			Files.write(exportPath, builder2.toString().getBytes(), 
				    StandardOpenOption.CREATE, 
				    StandardOpenOption.WRITE, 
				    StandardOpenOption.TRUNCATE_EXISTING);
			} catch(IOException e) {
				System.err.println("An error occurred while writing verifcation epsilons: " + e.getMessage());
			}
		}
		
	}

		
}

