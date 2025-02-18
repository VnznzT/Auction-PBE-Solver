package updateRule;

import helpers.UtilityHelpers;

public class UnivariateDampenedUpdateRule implements UpdateRule< Double> {
	double wMin, wMax;
	double c;
	boolean useAbsolute;
	
	public UnivariateDampenedUpdateRule(double wMin, double wMax, double c, boolean useAbsolute) {
								this.wMin = wMin;
		this.wMax = wMax;
		this.c = c;
		this.useAbsolute = useAbsolute;
	}

	@Override
	public Double update(Double oldbid, Double newbid, double oldutility, double newutility) {
        				
		double utilityLoss = UtilityHelpers.loss(oldutility, newutility, useAbsolute);
        double w = 2 / Math.PI * Math.atan(c * utilityLoss) * (wMax - wMin) + wMin;

		return oldbid * (1-w) + newbid * w;
	}
}
