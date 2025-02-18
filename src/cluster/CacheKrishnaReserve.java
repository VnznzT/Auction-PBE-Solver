package cluster;

import algorithm.PBEContext;
import domains.Belief;
import krishna.BeliefKrishna;
import strategy.Strategy;
import strategy.UnivariatePWCStrategy;
import utility.PWLUtility;
import utility.Utility;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class CacheKrishnaReserve implements Cache<Double, Double> {
	PBEContext context;
	public List<List<Utility<Double>>> cachedUtility = new ArrayList<List<Utility<Double>>>();
	public List<List<Strategy<Double, Double>>> cachedStrategy = new ArrayList<List<Strategy<Double,Double>>>();
		private List<List<List<Strategy<Double,Double>>>> cachedStrategies = new ArrayList<List<List<Strategy<Double,Double>>>>();
	private List<List<List<Utility<Double>>>> cachedUtilities = new ArrayList<List<List<Utility<Double>>>>();

	private List<List<List<Utility<Double>>>> cachedPrevUtilities = new ArrayList<List<List<Utility<Double>>>>();

	public CacheKrishnaReserve(PBEContext context) {
		super();
		this.context = context;
		int nrounds = context.getIntParameter("nrounds");
		for (int i = 0; i < 3; i++) {
			cachedStrategies.add(new ArrayList<>());			cachedUtilities.add(new ArrayList<>());
			cachedPrevUtilities.add(new ArrayList<>());
		}
	}

	@Override
	public void parse() {
		double minVal = context.getDoubleParameter("minVal");
		double maxVal = context.getDoubleParameter("maxVal");
		int nBidders = context.getnBidders(context.getSubGame());
		int gridsize = context.getIntParameter("SG0.OuterLoop.Gridsize");
		int nrounds = context.getIntParameter("nrounds");

		Path path = context.getPath();
		
		for(int subgame=1;subgame<=2;subgame++) {
			Path importPath = path.resolve(String.format("%d_ver", subgame));
			Path importPathPrev = importPath.resolve("prev");
			Double valStep = (maxVal-minVal)/gridsize;
			int nBiddersSubgame = context.getnBidders(subgame);
			Double beliefs[][]=new Double[nBiddersSubgame][];
			for(int b=1;b<gridsize+1;b++) {
				Double maxBelief = minVal+b*valStep;
				for (int m=0;m<nBiddersSubgame;m++) {
					beliefs[m]= new Double[] {minVal,maxBelief};
				}
				Belief belief = new BeliefKrishna(beliefs);
				String fp = context.getFP().fp(subgame, belief);
				List<Utility<Double>> cachedUtil;
				List<Strategy<Double, Double>> cachedStrat;
				List<Utility<Double>> cachedPrevUtil;
				List<Double> tempEpsilon;
				try {
					cachedStrat = parseStrategy(fp,importPath,subgame);
					cachedUtil = parseUtility(fp, importPath,subgame);
					cachedPrevUtil = parsePrevUtility(fp, importPathPrev,cachedUtil,subgame);
					parseEpsilon(fp,importPath,	subgame);

					cachedUtility.add(cachedUtil);
					cachedStrategy.add(cachedStrat);
					cachedStrategies.get(subgame).add(cachedStrat);
					cachedUtilities.get(subgame).add(cachedUtil);
					cachedPrevUtilities.get(subgame).add(cachedPrevUtil);
				} catch (IOException e) {
					System.err.println("Error while parsing strategies and utilities");
					e.printStackTrace();
				}

			}
		}
	}
	
	
	private void parseEpsilon(String fp, Path path,int subgame) throws IOException {
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

	private List<Strategy<Double, Double>> parseStrategy(String fp, Path path, int subgame) throws IOException {
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
	private List<Utility<Double>> parsePrevUtility(String fp, Path importPathPrev,List<Utility<Double>> cachedUtil,int subgame) throws IOException {
		int gridsize= context.getIntParameter(String.format("SG%d.VerificationStep.Gridsize",subgame));
		Path importPath = importPathPrev.resolve(String.format("%s.utils", fp));
		Scanner input= new Scanner(importPath);
		double[] values = new double[gridsize+3];
		values[0] = Double.MIN_VALUE;
		values[gridsize+2] = Double.MAX_VALUE; 		int canonicalBidders[] = context.getCanonicalBidders(subgame);
		int nBidder = canonicalBidders.length;
		double[][] utilities = new double[nBidder][gridsize+3];
				double firstValue = context.getDoubleParameter("minVal");
		values[1]=firstValue;
		for(int k=1;k<=gridsize;k++) {
			values[k+1]=input.nextDouble();
		}
		for(int bidder=0;bidder<nBidder;bidder++) {
			utilities[bidder][1]=cachedUtil.get(bidder).getUtility(firstValue);			for(int k=1;k<=gridsize;k++) {
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
	private List<Utility<Double>> parseUtility(String fp, Path path,int subgame) throws IOException {
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
		return null;
	}

	public List<Strategy<Double,Double>> getStrategies(int ref, int subgame) {
		return cachedStrategies.get(subgame).get(ref);
	}

	@Override
	public List<Utility<Double>> getPrevUtilities(int ref) {
		return null;
	}

	@Override
	public List<Utility<Double>> getPrevUtilities(int ref, int subgame) {
		return cachedPrevUtilities.get(subgame).get(ref);
	}

	public List<Utility<Double>> getUtilities(int ref, int subgame) {
		return cachedUtilities.get(subgame).get(ref);
	}

}
