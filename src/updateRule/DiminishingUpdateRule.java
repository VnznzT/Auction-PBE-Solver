package updateRule;

import algorithm.PBEContext;

public class DiminishingUpdateRule implements UpdateRule<Double> {
	double wMin, wMax;
	PBEContext context;

	public DiminishingUpdateRule(double wMin,double wMax, PBEContext context) {
		this.wMin = wMin;
		this.wMax = wMax;
		this.context = context;
	}

	@Override
	public Double update(Double oldbid, Double newbid, double oldutility, double newutility) {
		int iteration = context.getIteration();
		int maxIterations = context.getIntParameter("maxiters");
				double newWeight;
				newWeight = wMax - (wMax - wMin) * iteration / maxIterations;
		return oldbid * (1-newWeight) + newbid * newWeight;
	}
}
