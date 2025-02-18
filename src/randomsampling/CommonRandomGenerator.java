package randomsampling;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.math3.random.SobolSequenceGenerator;


public class CommonRandomGenerator implements RandomGenerator {
	int dimension, batchsize;
	SobolSequenceGenerator generator;
	List<double[]> cachedValues;
	

	public CommonRandomGenerator(int dimension) {
		this.dimension = dimension;
		batchsize = 10000;
		cachedValues = new ArrayList<>();
		generator = new SobolSequenceGenerator(dimension);
	}
	
	public CommonRandomGenerator(int dimension, int skip) {
		this(dimension);
		generator.skipTo(skip);
	}

	@Override
	public Iterator<double[]> nextVectorIterator() {
		
		Iterator<double[]> it = new Iterator<double[]>() {
			private int index = -1;
			
            @Override
            public boolean hasNext() {
                return true;
            }

			@Override
			public double[] next() {
				index++;
				if (index >= cachedValues.size()) {
					moreSamples();
				}
				return cachedValues.get(index);
			}
		};
		return it;
	}
	
	public void advance() {
										cachedValues.clear();
	}
	
	private void moreSamples() {
		for (int i=0; i<batchsize; i++) {
			cachedValues.add(generator.nextVector());
		}
	}
}
