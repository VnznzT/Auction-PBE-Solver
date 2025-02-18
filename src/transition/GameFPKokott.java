package transition;

import domains.Belief;

public class GameFPKokott implements FingerPrint {

	public GameFPKokott() {
			}
	@Override
	public String fp(int subgame, Belief beliefDistribution) {
		
		StringBuilder builder = new StringBuilder();
		
				builder.append(String.format("%d|", subgame));
		if(subgame!=0) {
									builder.append(String.format("%9.6f|", beliefDistribution.getMin(0)));
			builder.append(String.format("%9.6f|", beliefDistribution.getMax(0)));
		}
		return builder.toString();
	}

}
