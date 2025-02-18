package kokottetal;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
import domains.Belief;
import strategy.Strategy;
import utility.Utility;
import kokottetal.KokottWriterRound1;
import kokottetal.KokottWriterRound2;

public class CallbackKokottSG0 implements Callback<Double,Double[]> {
	Path path;
	PBEContext context;
	KokottWriterRound1 writer = new KokottWriterRound1();
	
	public CallbackKokottSG0(Path path, PBEContext context) {
		this.path=path;
		this.context=context;
	}

	@Override
	public void afterIteration(int subGame, int iteration, IterationType type, Belief<Double> beliefDistribution, 
			List<Strategy<Double,Double[]>> strategies, List<Utility<Double>> utilities, double epsilon) {
		context.setIteration(iteration);
		String s;
		s=writer.write(subGame, iteration, type, beliefDistribution, strategies, epsilon);
		System.out.println(s.toString());
		
		Path output = path.resolve(String.format("round1 iter%03d.strats", iteration));

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
	public Strategy<Double,Double[]> afterBR(int subGame, int iteration, Belief<Double> beliefDistribution, Strategy<Double,Double[]> strategy, double epsilon) {
		
		PWCStrategy1Dto2D s = (PWCStrategy1Dto2D) strategy;
        TreeMap<Double, Double> splitMap =(TreeMap<Double, Double>) s.getSplitMap();
        SortedMap<Double,Double> soleMap =s.getSoleMap();
        SortedMap<Double, Double> newSplitMap = new TreeMap<>();
        SortedMap<Double, Double> newSoleMap = new TreeMap<>();
                double maxiV = s.getMaxValue();
        
        double previousBid = splitMap.lastEntry().getValue();
        for (Map.Entry<Double, Double> e : splitMap.descendingMap().entrySet()) {
        Double v = e.getKey();
        	        	/*
        	if(Math.abs(previousBid-e.getValue())<0.00005){
        		Double bid = previousBid;
                newSplitMap.put(v, bid);
                previousBid = bid;
            }else {
                Double bid = Math.min(previousBid, e.getValue());
                newSplitMap.put(v, bid);
                previousBid = bid;
            }*/
        	        	Double bid = Math.min(previousBid-1e-4, e.getValue());
        	newSplitMap.put(v, bid);
        	previousBid = bid;
        }
                previousBid = 0.0;
        for (Map.Entry<Double, Double> e : soleMap.entrySet()) {
        	Double v = e.getKey();
            Double bid = Math.max(previousBid, e.getValue());
            newSoleMap.put(v, bid);
            previousBid = bid; 
        }
        double secondLastBid =splitMap.lowerEntry(maxiV).getValue();
        newSplitMap.put(maxiV,secondLastBid);
        return new PWCStrategy1Dto2D(newSoleMap,newSplitMap);
	}
	

}
