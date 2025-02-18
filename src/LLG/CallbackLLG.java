package LLG;

import java.nio.file.Path;
import java.util.List;

import algorithm.Callback;
import algorithm.PBEContext;
import algorithm.Solver;
import domains.Belief;
import strategy.Strategy;
import strategy.UnivariatePWCStrategy;
import utility.Utility;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.io.IOException;


public class CallbackLLG implements Callback<Double,Double>{
    Path path;
    PBEContext context;
    LLGWriter writer = new LLGWriter();

    public CallbackLLG(Path path, PBEContext context) {
        this.context=context;
        this.path=path;
    }
    @Override
    public void afterIteration(int subGame, int iteration, Solver.IterationType type, Belief<Double> beliefDistribution, List<Strategy<Double,Double>> strategies,
                               List<Utility<Double>> utilities, double epsilon) {

        context.setIteration(iteration);

        String sGlobal;
        String uGlobal;
        String sLocal;
        String uLocal;
        sGlobal=writer.write(subGame, iteration, type, beliefDistribution, strategies.get(0), epsilon);
        sLocal=writer.write(subGame, iteration, type, beliefDistribution, strategies.get(1), epsilon);
        uGlobal=writer.write(subGame, iteration, type, beliefDistribution, strategies.get(0), utilities.get(0), epsilon);
        uLocal=writer.write(subGame, iteration, type, beliefDistribution, strategies.get(1), utilities.get(1), epsilon);

        sGlobal = sGlobal.replace("strategy", "strategyGlobal");
        sLocal = sLocal.replace("strategy", "strategyLocal");
        uGlobal = uGlobal.replace("strategy", "strategyGlobal");
        uLocal = uLocal.replace("strategy", "strategyLocal");

        System.out.println(sGlobal.toString());
        System.out.println(sLocal.toString());

        Path output = path.resolve("round"+subGame);
        Path outputStratGlobal = output.resolve(String.format("Global_belief %.3f", beliefDistribution.getMin(0)).toString() +String.format("- %.3f", beliefDistribution.getMax(0)).toString()+String.format("iter%03d.strats", iteration).toString());
        Path outputStratLocal = output.resolve(String.format("Local_belief %.3f", beliefDistribution.getMin(0)).toString() +String.format("- %.3f", beliefDistribution.getMax(0)).toString()+String.format("iter%03d.strats", iteration).toString());
        Path outputUtilGlobal = output.resolve("Util "+String.format("Global_belief %.3f", beliefDistribution.getMin(0)).toString() +String.format("- %.3f", beliefDistribution.getMax(0)).toString()+String.format("iter%03d.strats", iteration).toString());
        Path outputUtilLocal = output.resolve("Util "+String.format("Local_belief %.3f", beliefDistribution.getMin(0)).toString() +String.format("- %.3f", beliefDistribution.getMax(0)).toString()+String.format("iter%03d.strats", iteration).toString());

        try {
                        Files.createDirectories(outputStratGlobal.getParent());
            Files.createDirectories(outputStratLocal.getParent());
            Files.createDirectories(outputUtilGlobal.getParent());
            Files.createDirectories(outputUtilLocal.getParent());

                        Files.write(outputStratGlobal, sGlobal.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
            Files.write(outputStratLocal, sLocal.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
            Files.write(outputUtilGlobal, uGlobal.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
            Files.write(outputUtilLocal, uLocal.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
                                    System.out.println("Error creating directories");
        }


    }

    @Override
    public Strategy<Double, Double> afterBR(int subGame, int iteration, Belief<Double> beliefDistribution, Strategy<Double, Double> strategy, double epsilon) {
        UnivariatePWCStrategy s = (UnivariatePWCStrategy) strategy;
                s.makeStrictMonotoneMean();
                        return s;
    }

}
