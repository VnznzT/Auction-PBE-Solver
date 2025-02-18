package algorithm;
import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

import Analysis.GameAnalysis;
import BR.BRCalculator;
import ClusterRuns.Statistics;
import algorithm.SolverCluster.Result;
import Analysis.Analyser;
import cluster.Cache;
import pointwiseBR.Optimizer;
import distribution.Distribution;
import domains.AuctionRound;
import domains.Belief;
import domains.BidSampler;
import helpers.StrategyInitialiser;
import helpers.UtilityInitialiser;
import helpers.VolumeEstimator;
import integrator.Integrator;
import randomsampling.RandomGenerator;
import strategy.Strategy;
import transition.FingerPrint;
import transition.SubGameTransition;
import updateRule.UpdateRule;
import utility.Utility; import verification.Verifier;
import writer.StratExporter;
import writer.UtilExporter;

public class PBEContext {
	
	/*notice most things are set as lists. This is because the modularity
	* of the algorithm allows to use different methods in different subgames.
	*/
	
		public String activatedConfig="SG0.innerloop"; 	public HashMap<String, String> config;
	public Solver solver;
	public List<Optimizer> optimizers;
	public List<BRCalculator> brcs;
	public List<RandomGenerator> rngs;
	public List<UpdateRule> updates;
	public List<Integrator> integrators;
	public List<VolumeEstimator> estimators;
	public List<Verifier> verifiers;
		public Belief initialBeliefs;
	public FingerPrint fingerprint;
	public int[][] canonicalBidders;
	public SubGameTransition subGameTransition;
	public List<Distribution> distributions;
	public List<BidSampler> samplers;
	public List<AuctionRound> auctions;
	public List<Callback> callbacks;
	public StrategyInitialiser strategyInitialiser;
	public UtilityInitialiser utilityInitialiser;
	public Double[][] verificationTable;
	private List<List<Utility<Double>>> cachedUtility;
	private int numberEffConfigs;
	private int c1ref;
	private int c2ref;
	private double efficiency1;
	private double efficiency2;
	private Statistics analysis;
	private List<List<Strategy<Double, Double>>> cachedStrategy;
	

		public boolean analyseMC = false;
	public Double[] currentValues;
	private SolverCluster solverCluster;
	private Path path;
	private StratExporter stratExporter;
	private UtilExporter utilExporter;
	private int subGame;
	public Cache cache;
	private Analyser analyser;
	private int iteration;



	private boolean verification = false;




	private double[] reservePrices;



	private String auctionSetting;

		public PBEContext() {
		rngs = new ArrayList<>(40);
		optimizers= new ArrayList<>(40);
		brcs= new ArrayList<>(40);
		updates= new ArrayList<>(40);
		integrators= new ArrayList<>(40);
		estimators= new ArrayList<>(40);
		distributions= new ArrayList<>(40);
		samplers= new ArrayList<>(40);
		auctions= new ArrayList<>(40);
		callbacks = new ArrayList<>(40);
		verifiers = new ArrayList<>(40);
		
		for (int i=0; i<40; i++) {
			rngs.add(null);
			optimizers.add(null);
			brcs.add(null);
			updates.add(null);
			integrators.add(null);
			estimators.add(null);
			distributions.add(null);
			samplers.add(null);
			auctions.add(null);
			callbacks.add(null);
			verifiers.add(null);
		}
	}
	
	public void setInitialBeliefs(Belief initialBeliefs) {
		this.initialBeliefs=initialBeliefs;
	}
	public Belief getInitialBelief() {
		return initialBeliefs;
	}
	public void writeResult(Solver.Result result) {

	}
	
	public void writeResult(SolverCluster.Result result) {

	}
	
	public void setSolver (Solver solver) {
		this.solver=solver;
	}
	public Solver getSolver() {
		return solver;
	}
	public void setFP(FingerPrint fingerprint) {
		this.fingerprint=fingerprint;
	}
	public FingerPrint getFP() {
		return fingerprint;
	}
	
	public void setConfig(HashMap<String, String> config) {
		this.config = config;
	}
	
	public void parseConfig(String path) throws FileNotFoundException {
		File file = new File(path);
		Scanner input = new Scanner(file);
		HashMap<String, String> config = new HashMap<>();
		while (input.hasNext()) {
			String key = input.next().toLowerCase();
			String value = input.next().trim();
			config.put(key, value);
		}
		input.close();
		this.config = config;
	}
	public HashMap<String, String> getConfig(){
		return config;
	}
	public int getIntParameter(String name) {
		return Integer.parseInt(getParameter(name));
	}

	public double getDoubleParameter(String name) {
		return Double.parseDouble(getParameter(name));
	}
	
	public String getStringParameter(String name) {
		return getParameter(name);
	}
	
	public boolean getBooleanParameter(String name) {
										String value = hasParameter(name) ? getParameter(name) : "false";
		return Boolean.parseBoolean(value) || (value.equals("1"));
	}
	
	public boolean hasParameter(String name) {
		return config.containsKey(name.toLowerCase());
	}
	
	private String getParameter(String name) {
		if (!config.containsKey(name.toLowerCase())) {
			throw new RuntimeException(String.format("Parameter '%s' not found in config", name));
		}
		return config.get(name.toLowerCase());
	}
	public String getActivatedConfig() {
		return activatedConfig;
	}
	public void activateConfig(String prefix) {
						activatedConfig=prefix;
		prefix = prefix.toLowerCase();
		HashMap<String, String> newEntries = new HashMap<>();
		for (String key : config.keySet()) {
			if (!key.startsWith(prefix)) {
				continue;
			}
			String newkey = key.substring(prefix.length() + 1);
			newEntries.put(newkey, config.get(key));
		}
		config.putAll(newEntries);
	}
	public int getnBidders(int subgame) {
		return canonicalBidders[subgame].length; 
	}
	public void setCanonicalBidders(int[][] canonicalBidders) {
		this.canonicalBidders=canonicalBidders;
	}
	public int[] getCanonicalBidders(int subgame) {
		return canonicalBidders[subgame];
	}
	public void setBR(int subgame, BRCalculator br) {
		brcs.set(subgame, br);
	}
	public BRCalculator getBR(int subgame) {
		return brcs.get(subgame);
	}
	public void setSubGameTransition(SubGameTransition subGameTransition) {
		this.subGameTransition = subGameTransition;
	}
	public SubGameTransition getSubGameTransition() {
		return subGameTransition;
	}
	public RandomGenerator getRng(int subgame) {
		return rngs.get(subgame);
	}
	public void setRng(int subgame, RandomGenerator rng) {
		rngs.set(subgame, rng);
	}
	public void advanceRng(int subgame) {
		rngs.get(subgame).advance();
	}
	public void setDistributions(int subgame, Distribution distribution) {
		distributions.set(subgame, distribution);
	}
	public Distribution getDistribution(int subGameNr) {
		return distributions.get(subGameNr);
	}
	
	public void setOptimizer(int subGame, Optimizer optimizer) {
		optimizers.set(subGame, optimizer);
	}
	public Optimizer getOptimizer(int subGameRef) {
		return optimizers.get(subGameRef);
	}
	public void setUpdateRule(int subgame, UpdateRule update) {
		updates.set(subgame, update);
	}
	public UpdateRule getUpdateRule(int subGameRef) {
		return updates.get(subGameRef);
	}
	public void setIntegrator(int subgame, Integrator integrator) {
		integrators.set(subgame, integrator);
	}
	public Integrator getIntegrator(int subGameRef) {
		return integrators.get(subGameRef);
	}
	public void setSampler(int subgame, BidSampler sampler) {
		samplers.set(subgame, sampler);
	}
	public BidSampler getSampler(int subGameRef) {
		return samplers.get(subGameRef);
	}
	public void setAuctionRound(int subgame, AuctionRound auction) {
		auctions.set(subgame, auction);
	}
	public AuctionRound getAuctionRound(int subGameRef) {
		return auctions.get(subGameRef);
	}
	public void setVolumeEstimator(int subgame, VolumeEstimator estimator) {
		estimators.set(subgame, estimator);
	}
	public VolumeEstimator getVolumeEstimator(int subGameRef) {
		return estimators.get(subGameRef);
	}
	public void setCallback(int subgame, Callback callback) {
		callbacks.set(subgame, callback);
	}
	public Callback getCallback(int subGame) {
		return callbacks.get(subGame);
	}
	
	public void setStrategyInitialiser(StrategyInitialiser strategyInitialiser) {
		this.strategyInitialiser=strategyInitialiser;
	}
	public void setUtilityInitialiser(UtilityInitialiser utilityInitialiser) {
		this.utilityInitialiser=utilityInitialiser;
	}
	public List<Strategy> makeInitialStrategies(int subgame, Belief beliefDistribution) {
		return strategyInitialiser.init(subgame,beliefDistribution);
	}

	public List<Utility> makeInitialUtilities(int subgame, Belief beliefDistribution) {
		return utilityInitialiser.init(subgame, beliefDistribution);
	}

	public Verifier getVerifier(int subgame) {
				return verifiers.get(subgame);
	}
	
	public void setVerifier(int subgame, Verifier verifier) {
		verifiers.set(subgame, verifier);
	}
	
	public void setVerificationTable(Double[][] verificationTable) {
		this.verificationTable=verificationTable;
	}
	public Double[][] getVerificationTable(){
		return verificationTable;
	}

	public void setCachedUtility(List<List<Utility<Double>>> cache) {
		this.cachedUtility=cache;
		
	}
	
	public List<List<Utility<Double>>> getCachedUtility(){
		return cachedUtility;
	}

	public void setCachedStrategy(List<List<Strategy<Double,Double>>> cache) {
		this.cachedStrategy=cache;
		
	}
	public List<List<Strategy<Double,Double>>> getCachedStrategy(){
		return cachedStrategy;
	}
	
	public void setnC(int nC) {
		this.numberEffConfigs=nC;
		
	}
	public int getnC() {
		return numberEffConfigs;
	}

	public void setc1ref(int c1ref) {
		this.c1ref=c1ref;
		
	}

	public void setc2ref(int c2ref) {
		this.c2ref=c2ref;
		
	}
	public int getc1ref() {
		return c1ref;
	}
	public int getc2ref() {
		return c2ref;
	}

	public void setEfficiency1(double efficiency1) {
		this.efficiency1=efficiency1;
		
	}
	public double getEfficiency1() {
		return efficiency1;
	}

	public void setEfficiency2(double efficiency2) {
		this.efficiency2=efficiency2;
	}
	public double getEfficiency2() {
		return efficiency2;
	}
	public void setAnalysis(Statistics analysis) {
		this.analysis=analysis;
	}
	public Statistics getAnalysis() {
		return analysis;
	}

	public Double[] getCurrentValues() {
		return currentValues;
	}

	public void setCurrentValues(Double[] currentValues) {
		this.currentValues = currentValues;
	}

	public void setSolverCluster(SolverCluster solverCluster) {
		this.solverCluster=solverCluster;
	}

	public SolverCluster getSolverCluster() {

		return this.solverCluster;
	}

	public void setPath(Path path) {
		this.path=path;
		
	}

	public Path getPath() {
		return path;
	}

	public void setStratExporter(StratExporter stratExporter) {
		this.stratExporter = stratExporter;
	}
	
	public StratExporter getStratExporter() {
		return stratExporter;
	}

	public void setUtilExporter(UtilExporter utilExporter) {
		this.utilExporter = utilExporter;
	}
	
	public UtilExporter getUtilExporter() {
		return utilExporter;
	}

	public void setSubGame(int i) {
		this.subGame =  i;
	}
	public int getSubGame() {
		return subGame;
	}
	public Cache getCache() {
		return cache;
	}

	public void setCache(Cache cache) {
		this.cache = cache;
	}

	public void setAnalyser(Analyser analyser) {
		this.analyser=analyser;
	}
	public Analyser getAnalyser() {
		return analyser;
	}
	public GameAnalysis analyseResult(Result result) {
		return analyser.analyse(result);
	}

	public void setIteration(int iteration) {
		this.iteration=iteration;
	}
	public int getIteration() {
		return iteration;
	}


    public void setReservePrices(double[] reservePrices) {
		this.reservePrices=reservePrices;
    }

	public double[] getReservePrices() {
		return reservePrices;
	}

	public String getAuctionSetting() {
		return auctionSetting;
	}

	public void setAuctionSetting(String auctionSetting) {
		this.auctionSetting = auctionSetting;
	}

	public boolean isVerification() {
		return verification;
	}

	public void setVerification(boolean verification) {
		this.verification = verification;
	}
}
