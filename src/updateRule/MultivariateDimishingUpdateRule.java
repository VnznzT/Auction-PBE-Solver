package updateRule;

import algorithm.PBEContext;

public class MultivariateDimishingUpdateRule implements UpdateRule<Double[]>{
    double wMin, wMax;
    PBEContext context;

    public MultivariateDimishingUpdateRule(double wMin, double wMax,PBEContext context) {
        this.wMin = wMin;
        this.wMax = wMax;
        this.context = context;
    }

    @Override
    public Double[] update(Double[] oldbid, Double[] newbid, double oldutility, double newutility) {
        int iteration = context.getIteration();
        int maxIterations = context.getIntParameter("maxiters");
                double newWeight;
                newWeight = wMax - (wMax - wMin) * iteration / maxIterations;
        Double[] result = new Double[oldbid.length];

        for (int i=0; i<oldbid.length; i++) {
        	result[i] =  oldbid[i] * (1-newWeight) + newbid[i] * newWeight;
        }
        return result;
    }
}
