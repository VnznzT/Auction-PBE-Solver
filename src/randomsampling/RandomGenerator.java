package randomsampling;

import java.util.Iterator;

public interface RandomGenerator {
	public Iterator<double[]> nextVectorIterator();
	
	default public void advance() {
			};
}
