package kokottetal;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import algorithm.Callback;
import algorithm.PBEContext;
import algorithm.Solver.IterationType;
import strategy.PWCStrategy1Dto2D;
import strategy.UnivariatePWCStrategy;
import transition.FingerPrint;
import domains.Belief;
import strategy.Strategy;
import utility.Utility;
import kokottetal.KokottWriterRound1;
import kokottetal.KokottWriterRound2;

public class CallbackKokottSG1 implements Callback<Double,Double> {
	Path path;
	KokottWriterRound2 writer = new KokottWriterRound2();
	PBEContext context;
	public CallbackKokottSG1(Path path,PBEContext context) {
		this.path=path;
		this.context=context;
	}

	@Override
	public void afterIteration(int subGame, int iteration, IterationType type, Belief<Double> beliefDistribution, 
			List<Strategy<Double,Double>> strategies,List<Utility<Double>> utilities ,double epsilon) {
		context.setIteration(iteration);
		String s;
		s=writer.write(subGame, iteration, type, beliefDistribution, strategies, epsilon);
		System.out.println(s.toString());
		Path output = path.resolve(String.format("belief %.3f", beliefDistribution.getMin(0)).toString() +String.format("- %.3f", beliefDistribution.getMax(0)).toString()+String.format("round2 iter%03d ", iteration).toString()+".strats");

		try {
			Files.createDirectories(output.getParent());
			
			Files.write(
				output, s.getBytes(), 
				StandardOpenOption.CREATE, 
				StandardOpenOption.WRITE, 
				StandardOpenOption.TRUNCATE_EXISTING
			);
		} catch (IOException e) {
		}

	}
	
	
	@Override
	public Strategy<Double,Double> afterBR(int subGame, int iteration, Belief<Double> beliefDistribution, Strategy<Double,Double> strategy, double epsilon) {
				UnivariatePWCStrategy s = (UnivariatePWCStrategy) strategy;
		s.makeMonotoneMean();
		return s;
		
	}

}
