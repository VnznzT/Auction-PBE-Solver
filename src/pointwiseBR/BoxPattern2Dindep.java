package pointwiseBR;

import java.util.ArrayList;
import java.util.List;




public class BoxPattern2Dindep extends Pattern<Double[]> {
	
	public BoxPattern2Dindep() {
		super(2);
	}

	@Override
	List<Double[]> getPatternPoints(Double[] center, int npoints, double scale) {
		if (npoints != 9) throw new RuntimeException();
		
				int[] refX = new int[] {0,1,-1};
		int[] refY =new int[] {0,1,-1}; 		List<Double[]> result = new ArrayList<>(npoints);
		for (int k=0; k<=2; k++) {
			for (int r=0; r<=2; r++) {
				int x=refX[k];
				int y=refY[r];
				Double[] nextPoint = new Double[]{
					Math.max(center[0] + x*scale, 0.0),
					Math.max(center[1] + y*scale, 0.0)
				};
				result.add(nextPoint);					
							}
		}
				
				result.set(getCenterIndex(npoints), center);
		return result;
	}

	@Override
	int getCenterIndex(int npoints) {
		return 0;
	}
	
	@Override
	protected String bidHash(Double[] key) {
		StringBuilder builder = new StringBuilder();
		for (int x=0; x<key.length; x++) {
			builder.append(String.format("%9.6f|", key[x]));
		}
		return builder.toString();
	}

}
