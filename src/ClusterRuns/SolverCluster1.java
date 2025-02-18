package ClusterRuns;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import BR.BRCalculator;
import algorithm.PBEContext;
import algorithm.Solver.IterationType;
import domains.Belief;
import pointwiseBR.Optimizer;
import strategy.PWCStrategy1Dto2D;
import strategy.Strategy;
import strategy.UnivariatePWCStrategy;
import strategy.UnivariatePWLStrategy;
import transition.FingerPrint;
import utility.PWCUtility;
import utility.PWLUtility;
import utility.Utility;
import verification.Verifier;

public class SolverCluster1 {
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
	public Path analysisPath;
	public int nB;
	public List<List<Utility<Double>>> cachedUtility = new ArrayList<List<Utility<Double>>>();
	public List<List<Strategy<Double, Double>>> cachedStrategy = new ArrayList<List<Strategy<Double,Double>>>();
	Double[][] verificationTable;

	
	public SolverCluster1(PBEContext context, Path output, Path analysisPath) throws IOException {
		this.context=context;
		this.output=output;
		this.analysisPath = analysisPath;
		this.nB = context.getIntParameter("SG0.VerificationStep.Gridsize");
		verificationTable = context.getVerificationTable();
				Path tempInputUtil;
		Path tempInputStrat;
		for(int sg=1;sg<=2;sg++) {
			for(int b1=0;b1<nB;b1++) {
				for(int b2=0;b2<nB;b2++) {
					tempInputUtil=output.resolve(String.format("%d,%d,%d.util", sg,b1,b2));
					tempInputStrat=output.resolve(String.format("%d,%d,%d.strat", sg,b1,b2));
					parseVerification(output.resolve(String.format("%d,%d,%d.epsilons", sg,b1,b2)),sg);
					List<Utility<Double>> cachedUtil = parseUtility(tempInputUtil,nB,sg);
					List<Strategy<Double, Double>> cachedStrat = parseStrategy(tempInputStrat,nB,sg);
					cachedUtility.add(cachedUtil);
					cachedStrategy.add(cachedStrat);
				}
			}
		}
		
	}


	private void parseVerification(Path path,int sg) throws IOException {
		int nBidder=context.getnBidders(0);
		Scanner input = new Scanner(path);
		for(int bidder=0;bidder<nBidder;bidder++) {
			verificationTable[bidder][sg]=Math.max(verificationTable[bidder][sg], input.nextDouble());
		}
		if (input.hasNext()) {
			throw new RuntimeException("something went wrong with the length of cached utilites");
		}
	}
	private List<Strategy<Double,Double>> parseStrategy(Path path,int nB,int sg) throws IOException {
		int gridsize= context.getIntParameter(String.format("SG%d.VerificationStep.Gridsize",sg));
		Scanner input = new Scanner(path);
		double[] values = new double[gridsize+3];
		values[0] = Double.MIN_VALUE;
		values[gridsize+2] = Double.MAX_VALUE; 
		int[] canonicalBidders = context.getCanonicalBidders(sg);
		int nBidder = canonicalBidders.length;
		double[][] strategies = new double[nBidder][gridsize+3];
		for(int k=0;k<=gridsize;k++) {
			values[k+1]=input.nextDouble();
		}
		for(int bidder=0;bidder<nBidder;bidder++) {
			for(int k=0;k<=gridsize;k++) {
				strategies[bidder][k+1]=input.nextDouble();
			}
			strategies[bidder][0] = strategies[bidder][1];
			strategies[bidder][gridsize+2] = strategies[bidder][gridsize+1];

			
		}
		if (input.hasNext()) {
			throw new RuntimeException("something went wrong with the length of cached strategies");
		}
		input.close();
		
				List<Strategy<Double,Double>> listStrat = new ArrayList<Strategy<Double,Double>>();
		for(int i=0;i<nBidder;i++) {
			listStrat.add(new UnivariatePWCStrategy(values,strategies[i]));
		}
		return listStrat;
	}

	private List<Utility<Double>> parseUtility(Path path,int nB,int sg) throws IOException {
		int gridsize= context.getIntParameter(String.format("SG%d.VerificationStep.Gridsize",sg));
		Scanner input = new Scanner(path);
		double[] values = new double[gridsize+3];
		values[0] = Double.MIN_VALUE;
		values[gridsize+2] = Double.MAX_VALUE; 
		int[] canonicalBidders = context.getCanonicalBidders(sg);
		int nBidder = canonicalBidders.length;
		double[][] utilities = new double[nBidder][gridsize+3];
		for(int k=0;k<=gridsize;k++) {
			values[k+1]=input.nextDouble();
		}
		for(int bidder=0;bidder<nBidder;bidder++) {
			for(int k=0;k<=gridsize;k++) {
				utilities[bidder][k+1]=input.nextDouble();
			}
			utilities[bidder][0] = utilities[bidder][1];
			utilities[bidder][gridsize+2] = utilities[bidder][gridsize+1];

			
		}
		if (input.hasNext()) {
			throw new RuntimeException("something went wrong with the length of cached utilites");
		}
		input.close();
		
				List<Utility<Double>> listUtil = new ArrayList<Utility<Double>>();
		for(int i=0;i<nBidder;i++) {
			listUtil.add(new PWLUtility(values,utilities[i]));
		}
		return listUtil;
	}


	public Result solve(int subgame, Belief beliefDistribution) throws IOException {
				context.setCachedUtility(cachedUtility);
		context.setCachedStrategy(cachedStrategy);
		
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
				Result result = verify(strategies, utilities, context.getVerifier(subgame),subgame,beliefDistribution);
				callbackAfterIteration(subgame, iteration,IterationType.VERIFICATION,beliefDistribution, strategies, utilities, result.epsilon);
			
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
			Result result = verify(strategies, utilities, context.getVerifier(subgame),subgame,beliefDistribution);
			callbackAfterIteration(subgame, iteration,IterationType.VERIFICATION,beliefDistribution, strategies, utilities, result.epsilon);
		
						context.activateConfig(prefixConfig);
			return result;	
		}
		context.advanceRng(subgame);
		Result result = verify(strategies, utilities, context.getVerifier(subgame),subgame,beliefDistribution);
		callbackAfterIteration(subgame, iteration,IterationType.VERIFICATION,beliefDistribution, strategies, utilities, result.epsilon);
		
												context.activateConfig(prefixConfig);
		return result;	
			

		
		
	}
	
	private Result verify(List<Strategy> strategies, List<Utility> utilities,Verifier verifier, int subgame, Belief beliefDistribution) throws IOException {
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
		double maxRound1Group1=0.0;
		double maxRound1Group2=0.0;
		double maxRound2Group1=0.0;
		double maxRound2Group2=0.0;
		for(int i=0;i<3;i++) {
			for(int sg=1;sg<=2;sg++) {
			maxRound2Group1=Math.max(maxRound2Group1, verificationTable[i][sg]);
			maxRound2Group2=Math.max(maxRound2Group2, verificationTable[i+3][sg]);
			}
			maxRound1Group1=Math.max(maxRound1Group1, verificationTable[i][0]);
			maxRound1Group2=Math.max(maxRound1Group2, verificationTable[i+3][0]);
		}
		highestEpsilon=Math.max(maxRound1Group1+maxRound2Group1, maxRound1Group2+maxRound2Group2);
		
		double[] premium = context.getAnalysis().premium;
		Double[][] maxDiscont = context.getAnalysis().maxdiscontinuity;
		Double[][][] minMax = context.getAnalysis().minMax;
		
		for(int i=0;i<nBidders;i++) {
			PWCStrategy1Dto2D si = (PWCStrategy1Dto2D) strategies.get(i);
			PWCUtility ui = (PWCUtility) utilities.get(i);
			Double[][] bidsi = si.bids;
			Double[] vali = si.values; 			
			minMax[i][0]=si.getBid(si.getMinValue());
			minMax[i][1]=si.getBid(si.getMaxValue());
			
			for (int k=1;k<bidsi.length;k++) {
				maxDiscont[i][0]=Math.max(maxDiscont[i][0], bidsi[k][0]-bidsi[k-1][0]);
				maxDiscont[i][1]=Math.max(maxDiscont[i][1], bidsi[k][1]-bidsi[k-1][1]);
			}
			double weightFactor = bidsi.length-2;
			double[] utili= ui.utilities;
			
			for(int k=1;k<utili.length-1;k++) {
				premium[i]+=utili[k];
			}
			premium[i]=premium[i]/weightFactor;
		}
		
		context.getAnalysis().minMax=minMax;
		context.getAnalysis().verifiedEpsilon=highestEpsilon;
		context.getAnalysis().maxdiscontinuity= maxDiscont;
		context.getAnalysis().premium=premium;
		
		exportAnalysis(strategies,utilities);
		
		return new Result(highestEpsilon,strategies, utilities);	
	}


	private void exportAnalysis(List<Strategy> strategies, List<Utility> utilities) throws IOException {
				
		Statistics analysis = context.getAnalysis();
		StringBuilder builder = new StringBuilder();
		int nBidders=context.getnBidders(0);
		
				builder.append("Epsilon: ");
		builder.append(String.format("%7.6f ", analysis.verifiedEpsilon));
		builder.append("\n");
		
				for(int bidder=0;bidder<nBidders;bidder++) {
		builder.append(String.format("MinMax%d: ",bidder+1));
		for(int type =0;type<2;type++) {
				builder.append(String.format("%7.6f ",analysis.minMax[bidder][0][type]));
				builder.append(String.format("%7.6f ",analysis.minMax[bidder][1][type]));
			}
			builder.append("\n");
		}
		
				for(int bidder=0;bidder<nBidders;bidder++) {
			builder.append(String.format("MaxDiscont%d: ",bidder+1));
			builder.append(String.format("%7.6f ",analysis.maxdiscontinuity[bidder][0]));
			builder.append(String.format("%7.6f ",analysis.maxdiscontinuity[bidder][1]));
			builder.append("\n");
		}
		
				for(int bidder=0;bidder<nBidders;bidder++) {
			builder.append(String.format("Premium%d: ", bidder+1));
			builder.append(String.format("%7.6f ",analysis.premium[bidder]));
			builder.append("\n");
		}
		
				for(int bidder=0;bidder<6;bidder++) {
			builder.append(String.format("revenue%d: ",bidder+1));
	        for (Map.Entry<Double, Double[][]> e : analysis.revenue.entrySet()) {	
	        	builder.append(String.format("%.1f ",e.getValue()[bidder][0]));
           		builder.append(String.format("%7.6f ",e.getValue()[bidder][1]));
	        } 
	        builder.append("\n");
        }
		
				for(int bidder=0;bidder<6;bidder++) {
			builder.append(String.format("efficiency%d: ",bidder+1));
	        for (Map.Entry<Double, Double[][]> e : analysis.efficiency.entrySet()) {	
	        	builder.append(String.format("%.1f ",e.getValue()[bidder][0]));
           		builder.append(String.format("%7.6f ",e.getValue()[bidder][1]));
           		builder.append(String.format("%7.6f ",e.getValue()[bidder][2]));

	        } 
	        builder.append("\n");
        }
		
				for(int bidder=0;bidder<6;bidder++) {
			builder.append(String.format("winningGroup%d: ",bidder+1));
	        for (Map.Entry<Double, Double[][]> e : analysis.winningGroup.entrySet()) {	
	        	builder.append(String.format("%.1f ",e.getValue()[bidder][0]));
           		builder.append(String.format("%7.6f ",e.getValue()[bidder][1]));
           		builder.append(String.format("%7.6f ",e.getValue()[bidder][2]));
	        } 
	        builder.append("\n");
        }
		
				for(int bidder=0;bidder<6;bidder++) {
			builder.append(String.format("ProbSole1stRound%d: ",bidder+1));
	        for (Map.Entry<Double, Double[][]> e : analysis.EquilibriumType.entrySet()) {          
	            	if(e.getValue()[bidder][0]>0) {
	            		double perc = e.getValue()[bidder][1]/e.getValue()[bidder][0];
	            		builder.append(String.format("%7.6f ",perc));
	            	}else {
	            		builder.append(String.format("%7.6f ",0.0));
	            	}
	            }
	        builder.append("\n");
        }
                for(int bidder=0;bidder<6;bidder++) {
			builder.append(String.format("ProbSole%d: ",bidder));
	        for (Map.Entry<Double, Double[][]> e : analysis.NumberWTA.entrySet()) {
	        	
	            	if(e.getValue()[bidder][0]>0) {
	            		double perc = e.getValue()[bidder][1]/e.getValue()[bidder][0];
	            		builder.append(String.format("%7.6f ",perc));
	            	}else {
	            		builder.append(String.format("%7.6f ",0.0));
	            	}
	            }
	        builder.append("\n");
        }
        
                for(int bidder=0;bidder<6;bidder++) {
			builder.append(String.format("group1Utility%d: ",bidder));
	        for (Map.Entry<Double, Double[][]> e : analysis.group1Utility.entrySet()) {
	        	
	            	if(e.getValue()[bidder][0]>0) {
	            		double perc = e.getValue()[bidder][1]/e.getValue()[bidder][0];
	            		builder.append(String.format("%7.6f ",perc));
	            	}else {
	            		builder.append(String.format("%7.6f ",0.0));
	            	}
	            }
	        builder.append("\n");
        }
                for(int bidder=0;bidder<6;bidder++) {
			builder.append(String.format("group2Utility%d: ",bidder));
	        for (Map.Entry<Double, Double[][]> e : analysis.group2Utility.entrySet()) {
	        	
	            	if(e.getValue()[bidder][0]>0) {
	            		double perc = e.getValue()[bidder][1]/e.getValue()[bidder][0];
	            		builder.append(String.format("%7.6f ",perc));
	            	}else {
	            		builder.append(String.format("%7.6f ",0.0));
	            	}
	            }
	        builder.append("\n");
        }
 
            	PWCStrategy1Dto2D s= (PWCStrategy1Dto2D) strategies.get(0);
    	Double minValue=s.getMinValue();
    	Double maxValue=s.getMaxValue();
    	int ngridpoints = context.getIntParameter("sg0"+".verificationstep.gridsize");      
      
        for(int k=0;k<nBidders;k++) {
        	builder.append( String.format("stratSoleBidder%d: ", k));
        	 for (int i=0; i<=ngridpoints; i++) {
                 double v = minValue+(maxValue-minValue) * i / ngridpoints;
                 builder.append(String.format("%5.4f ", ((PWCStrategy1Dto2D)strategies.get(k)).getBid(v)[0]));
                
             }
        	 builder.append("\n");
        	 builder.append( String.format("stratSplitBidder%d: ", k));
        	 for (int i=0; i<=ngridpoints; i++) {
                 double v = minValue+(maxValue-minValue) * i / ngridpoints;
                 builder.append(String.format("%5.4f ", ((PWCStrategy1Dto2D)strategies.get(k)).getBid(v)[1]));
                
             }
        	 builder.append("\n");
        }
        
            	PWCUtility u= (PWCUtility) utilities.get(0);
      
        for(int k=0;k<nBidders;k++) {
        	builder.append( String.format("util%d: ", k));
        	 for (int i=0; i<=ngridpoints; i++) {
                 double v = minValue+(maxValue-minValue) * i / ngridpoints;
                 builder.append(String.format("%5.4f ", ((PWCUtility)utilities.get(k)).getUtility(v)));
                 
             }
        	 builder.append("\n");
        }
    	
		Path exportPath=analysisPath.resolve("ana.lysis");
		        Files.createDirectories(exportPath.getParent());
        
		Files.write(exportPath, builder.toString().getBytes(), 
			    StandardOpenOption.CREATE, 
			    StandardOpenOption.WRITE, 
			    StandardOpenOption.TRUNCATE_EXISTING);
		
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

