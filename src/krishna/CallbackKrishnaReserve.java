package krishna;

import algorithm.Callback;
import algorithm.PBEContext;
import algorithm.Solver.IterationType;
import domains.Belief;
import strategy.Strategy;
import strategy.UnivariatePWCStrategy;
import utility.Utility;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;

public class CallbackKrishnaReserve implements Callback<Double,Double> {
	Path path;
	PBEContext context;
	KrishnaWriter writer = new KrishnaWriter();

	public CallbackKrishnaReserve(Path path, PBEContext context) {
		this.context=context;
		this.path=path;
	}

	@Override
	public void afterIteration(int subGame, int iteration, IterationType type, Belief<Double> beliefDistribution, List<Strategy<Double,Double>> strategies,
			List<Utility<Double>> utilities, double epsilon) {
		context.setIteration(iteration);

		String s;
		String u;
		s=writer.write(subGame, iteration, type, beliefDistribution, strategies, epsilon);
		u=writer.write(subGame, iteration, type, beliefDistribution, strategies, utilities, epsilon);
		System.out.println(s.toString());

		Path output = path.resolve("round"+subGame);
		Path outputStrat = output.resolve(String.format("belief %.3f", beliefDistribution.getMin(0)).toString() +String.format("- %.3f", beliefDistribution.getMax(0)).toString()+String.format("iter%03d.strats", iteration).toString());
		Path outputUtil = output.resolve("Util "+String.format("belief %.3f", beliefDistribution.getMin(0)).toString() +String.format("- %.3f", beliefDistribution.getMax(0)).toString()+String.format("iter%03d.strats", iteration).toString());
		try {
			Files.createDirectories(outputStrat.getParent());
			
			Files.write(
				outputStrat, s.getBytes(), 
				StandardOpenOption.CREATE, 
				StandardOpenOption.WRITE, 
				StandardOpenOption.TRUNCATE_EXISTING
			);
		} catch (IOException e) {
		}
		
		try {
			Files.createDirectories(outputUtil.getParent());
			
			Files.write(
				outputUtil, u.getBytes(), 
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
				s.makeStrictMonotoneMean();
						return s;
	}
	

}