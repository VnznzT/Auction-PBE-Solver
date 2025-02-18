package algorithm;

import algorithm.Solver.Result;
import domains.Belief;

public class AlgorithmDP {

	
	PBEContext context;
	
	
	public AlgorithmDP(PBEContext context) {
		this.context=context;
	}
	
		public void run() {
		Solver.Result result;
		Belief initialDistribution = context.getInitialBelief();
		
						
		result = context.getSolver().solve(0,initialDistribution);
		
		
				
		/*have a writer method which fetches the strategies and writes them in a format
		* meaningful format.
		*/
		context.writeResult(result);
	}

}
