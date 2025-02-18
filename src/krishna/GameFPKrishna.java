package krishna;

import domains.Belief;
import transition.FingerPrint;

public class GameFPKrishna implements FingerPrint {
	
	public GameFPKrishna() {
			}
	@Override
	public String fp(int subgame, Belief beliefDistribution) {
		
		StringBuilder builder = new StringBuilder();
		
				builder.append(String.format("%d|", subgame));
		if(subgame!=0) {
									builder.append(String.format("%9.6f", beliefDistribution.getMax(0)));
		}
		
		return builder.toString();
	}

}

