package LLG;

import domains.Belief;
import domains.BidSampler;
import algorithm.PBEContext;
import strategy.Strategy;

import java.util.Iterator;
import java.util.List;

public class SamplerLLG extends BidSampler<Double,Double> {
public SamplerLLG(PBEContext context,int subGameNr) {
super(context,subGameNr);
}

    @Override
    public Iterator<BidSampler<Double, Double>.Sample> conditionalBidIterator(int i, Double v, Double b, Belief<Double> beliefDistribution, List<Strategy<Double, Double>> s) {
        int nBidders=s.size();

                Iterator<double[]> rngiter = context.getRng(subGameNr).nextVectorIterator();

        Iterator<Sample> it = new Iterator<Sample>() {
            @Override
            public boolean hasNext() {
                return true;
            }

            @Override
            public Sample next() {
                double[] r = rngiter.next();
                Double[] result = new Double[nBidders];

                                for(int k=0;k<nBidders;k++) {
                    if(k!=i) {
                        double val = r[k]*(beliefDistribution.getMax(k)-beliefDistribution.getMin(k))+beliefDistribution.getMin(k);
                        result[k] = s.get(k).getBid(val);
                    }else {
                        result[k] = b;
                    }
                }
                                double density = context.getDistribution(subGameNr).density(beliefDistribution, result,i);

                                return new Sample(density, result);
            }

        };
        return it;
    }

}
