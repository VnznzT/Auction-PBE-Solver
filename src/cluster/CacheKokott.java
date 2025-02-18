package cluster;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import algorithm.PBEContext;
import domains.Belief;
import kokottetal.BeliefKokott;
import strategy.Strategy;
import strategy.UnivariatePWCStrategy;
import transition.FingerPrint;
import utility.PWLUtility;
import utility.Utility;

public class CacheKokott implements Cache<Double, Double> {
	PBEContext context;
	public List<List<Utility<Double>>> cachedUtility = new ArrayList<List<Utility<Double>>>();
	public List<List<Strategy<Double, Double>>> cachedStrategy = new ArrayList<List<Strategy<Double,Double>>>();

	public List<List<Utility<Double>>> cachedPrevUtility = new ArrayList<List<Utility<Double>>>();
	
	public CacheKokott(PBEContext context) {
		super();
		this.context = context;
	}

	@Override
	public void parse() {
		double minCost = context.getDoubleParameter("minCost");
		double maxCost = context.getDoubleParameter("maxCost");
		int nBidders = context.getIntParameter("nBidders");
		int gridsize = context.getIntParameter("SG0.OuterLoop.Gridsize");
		int subgame = context.getSubGame()+1;
		Path path = context.getPath();
		
		
		Path importPath = path.resolve(String.format("%d_ver", subgame));
		Path importPathPrev = importPath.resolve("prev");
		Double costStep = (maxCost-minCost)/gridsize;
		Double[] beliefAbtWinner= new Double[2];
		Double[] beliefAbtLoser = new Double[2];
		Double[][] beliefArray = new Double[nBidders][2];
		for(int b=0;b<gridsize;b++) {
			Double lowBelief = minCost + b*costStep;
			beliefAbtWinner[0]=lowBelief;
			beliefAbtLoser[0]=lowBelief;
			
			beliefAbtWinner[1]=lowBelief+costStep;
			beliefAbtLoser[1]=maxCost;
			
						beliefArray[0][0]=beliefAbtWinner[0];
			beliefArray[0][1]=beliefAbtWinner[1];
			
			for(int k=1; k<nBidders;k++) {
				beliefArray[k][0]=beliefAbtLoser[0];
				beliefArray[k][1]=beliefAbtLoser[1];				
			}
			Belief belief = new BeliefKokott(beliefArray);
			String fp = context.getFP().fp(subgame, belief);
			List<Utility<Double>> cachedUtil;
			List<Strategy<Double, Double>> cachedStrat;
			List<Double> tempEpsilon;
			List<Utility<Double>> cachedPrevUtil;
			try {
				cachedStrat = parseStrategy(fp,importPath);
				cachedUtil = parseUtility(fp, importPath);
				cachedPrevUtil = parsePrevUtility(fp, importPathPrev,cachedUtil);
				parseEpsilon(fp,importPath);
				cachedUtility.add(cachedUtil);
				cachedStrategy.add(cachedStrat);
				cachedPrevUtility.add(cachedPrevUtil);
			} catch (IOException e) {
			    System.err.println("Error while parsing strategies and utilities");
			    e.printStackTrace();
			}	

		}	
	}
	
	
	private void parseEpsilon(String fp, Path path) throws IOException {
		int subgame = context.getSubGame()+1;
		int gridsize= context.getIntParameter(String.format("SG%d.VerificationStep.Gridsize",subgame));
		Path importPath = path.resolve(String.format("%s.epsilon", fp));
		Scanner input= new Scanner(importPath);
		Double[][] verificationTable = context.getVerificationTable();
		int nBidders = context.getnBidders(subgame);
		for (int i=0; i<nBidders;i++) {
			Double nextEpsilon = input.nextDouble();
			verificationTable[i][subgame] = Math.max(nextEpsilon, verificationTable[i][subgame]);
		}
		if (input.hasNext()) {
			throw new RuntimeException("something went wrong with the length of cached epsilons");
		} 
		input.close();
		
	}

	private List<Strategy<Double, Double>> parseStrategy(String fp, Path path) throws IOException {
		int subgame = context.getSubGame()+1;
		int gridsize= context.getIntParameter(String.format("SG%d.VerificationStep.Gridsize",subgame));
		Path importPath = path.resolve(String.format("%s.strats", fp));
		Scanner input= new Scanner(importPath);
	
		double[] values = new double[gridsize+3];
		values[0] = Double.MIN_VALUE;
		values[gridsize+2] = Double.MAX_VALUE;

		int[] canonicalBidders = context.getCanonicalBidders(subgame);
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
	private List<Utility<Double>> parsePrevUtility(String fp, Path importPathPrev,List<Utility<Double>> cachedUtil) throws IOException {
		int subgame = context.getSubGame()+1;
		int gridsize= context.getIntParameter(String.format("SG%d.VerificationStep.Gridsize",subgame));
		Path importPath = importPathPrev.resolve(String.format("%s.utils", fp));
		Scanner input= new Scanner(importPath);
		double[] values = new double[gridsize+3];
		values[0] = Double.MIN_VALUE;
		values[gridsize+2] = Double.MAX_VALUE;

		int canonicalBidders[] = context.getCanonicalBidders(subgame);
		int nBidder = canonicalBidders.length;
		double[][] utilities = new double[nBidder][gridsize+3];
				double firstValue = context.getDoubleParameter("minCost");
		values[1]=firstValue;
		for(int k=1;k<=gridsize;k++) {
			values[k+1]=input.nextDouble();
		}
		for(int bidder=0;bidder<nBidder;bidder++) {
			utilities[bidder][1]=cachedUtil.get(bidder).getUtility(firstValue);
			for(int k=1;k<=gridsize;k++) {
				utilities[bidder][k+1]=input.nextDouble();
			}
			utilities[bidder][0] = utilities[bidder][1];
			utilities[bidder][gridsize+2] = utilities[bidder][gridsize+1];
		}
		if (input.hasNext()) {
			throw new RuntimeException("something went wrong with the length of cached prev utilites");
		}
		input.close();

				List<Utility<Double>> listUtil = new ArrayList<Utility<Double>>();
		for(int i=0;i<nBidder;i++) {
			listUtil.add(new PWLUtility(values,utilities[i]));
		}
		return listUtil;
	}
	private List<Utility<Double>> parseUtility(String fp, Path path) throws IOException {
		int subgame = context.getSubGame()+1;
		int gridsize= context.getIntParameter(String.format("SG%d.VerificationStep.Gridsize",subgame));
		Path importPath = path.resolve(String.format("%s.utils", fp));
		Scanner input= new Scanner(importPath);
		double[] values = new double[gridsize+3];
		values[0] = Double.MIN_VALUE;
		values[gridsize+2] = Double.MAX_VALUE;

		int[] canonicalBidders = context.getCanonicalBidders(subgame);
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

	@Override
	public List<Utility<Double>> getUtilities(int ref) {
				return cachedUtility.get(ref);
	}

	@Override
	public List<Strategy<Double, Double>> getStrategies(int ref) {
		return cachedStrategy.get(ref);
	}

	@Override
	public List<Utility<Double>> getUtilities(int ref, int subgame) {
		return null;
	}

	@Override
	public List<Strategy<Double, Double>> getStrategies(int ref, int subgame) {
		return null;
	}

	@Override
	public List<Utility<Double>> getPrevUtilities(int ref) {
		return cachedPrevUtility.get(ref);
	}

	@Override
	public List<Utility<Double>> getPrevUtilities(int ref, int subgame) {
		return null;
	}

}
