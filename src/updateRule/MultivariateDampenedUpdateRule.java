package updateRule;

import helpers.UtilityHelpers;

public class MultivariateDampenedUpdateRule<Value> implements UpdateRule<Double[]> {
	double wMin, wMax;
	double c;
	boolean useAbsolute;
	
	public MultivariateDampenedUpdateRule(double wMin, double wMax, double c, boolean useAbsolute) {
								this.wMin = wMin;
		this.wMax = wMax;
		this.c = c;
		this.useAbsolute = useAbsolute;
	}

	@Override
	public Double[] update(Double[] oldbid, Double[] newbid, double oldutility, double newutility) {
        				
		double utilityLoss = UtilityHelpers.loss(oldutility, newutility, useAbsolute);
        double w = 2 / Math.PI * Math.atan(c * utilityLoss) * (wMax - wMin) + wMin;
        
        Double[] result = new Double[oldbid.length];
        
        for (int i=0; i<oldbid.length; i++) {
        	result[i] =  oldbid[i] * (1-w) + newbid[i] * w; 
        }
        
		return result;
	}
}
