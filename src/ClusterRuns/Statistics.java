package ClusterRuns;

import java.util.Hashtable;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

public class Statistics {
	
	
	
	public Statistics(SortedMap<Double, Double[][]> equilibriumType, SortedMap<Double, Double[][]> numberWTA, Double[][][] minMax,
			Double[][] maxdiscontinuity, double[] premium, double verifiedEpsilon) {
		EquilibriumType = equilibriumType;
		NumberWTA = numberWTA;
		this.minMax = minMax;
		this.maxdiscontinuity = maxdiscontinuity;
		this.premium = premium;
		this.verifiedEpsilon = verifiedEpsilon;
	}
	
	public Statistics(int nBidders) {
		Double[][] EquilibriumTypes=new Double[nBidders][2];
		Double[][] NumberWTA= new Double[nBidders][2];
		Double[][] revenue = new Double[nBidders][2];
		Double[][] efficiencyMetric = new Double[nBidders][3];
		Double[] currentValues = new Double[nBidders];
		Double[][] winningGroup = new Double[nBidders][3];
		Double[][] group1Utility = new Double[nBidders][2];
		Double[][] group2Utility = new Double[nBidders][2];
		
		for(int k=0;k<nBidders;k++) {
			EquilibriumTypes[k]=new Double[]{0.0,0.0}; 		}

		for(int k=0;k<nBidders;k++) {
			NumberWTA[k]=new Double[]{0.0,0.0}; 			revenue[k]=new Double[]{0.0,0.0}; 			efficiencyMetric[k]=new Double[]{0.0,0.0,0.0}; 			winningGroup[k]=new Double[]{0.0,0.0,0.0}; 			group1Utility[k]=new Double[]{0.0,0.0}; 			group2Utility[k]=new Double[]{0.0,0.0}; 		}
	}
	
	SortedMap<Double, Double[][]> EquilibriumType = new TreeMap<Double, Double[][]>(); 	SortedMap<Double, Double[][]> NumberWTA = new TreeMap<Double, Double[][]>(); 		
		public Double[][][] minMax;
	public Double[][] maxdiscontinuity; 	public double[] premium;  		public double verifiedEpsilon;
	
		SortedMap<Double, Double[][]> revenue = new TreeMap<Double, Double[][]>(); 	SortedMap<Double, Double[][]> efficiency = new TreeMap<Double, Double[][]>(); 	
	SortedMap<Double, Double[][]> winningGroup = new TreeMap<Double, Double[][]>(); 
	SortedMap<Double, Double[][]> group1Utility = new TreeMap<Double, Double[][]>(); 	SortedMap<Double, Double[][]> group2Utility = new TreeMap<Double, Double[][]>(); 
	
	}
