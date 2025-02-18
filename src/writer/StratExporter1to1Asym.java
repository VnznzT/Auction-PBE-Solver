package writer;

import strategy.Strategy;
import strategy.UnivariatePWCStrategy;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;

public class StratExporter1to1Asym implements StratExporter {

	@Override
	public void exportStrat(List<Strategy> strategies, String fp, Path path) {
		StringBuilder builder = new StringBuilder();

		
				for(int k=0;k<strategies.size();k++) {
			UnivariatePWCStrategy strat = (UnivariatePWCStrategy) strategies.get(k);
			double[] v = strat.values;
			double[] b = strat.bids;
			for(int l=1;l<v.length-1;l++) {
				builder.append(String.format("%7.6f ", v[l]));
			}
			builder.append("\n");
			for(int l=1;l<v.length-1;l++) {
				builder.append(String.format("%7.6f ", b[l]));
			}
			builder.append("\n");
		}
		Path exportPath=path.resolve(String.format("%s.strats", fp));
				try {
	        Files.createDirectories(exportPath.getParent());
	        
			Files.write(exportPath, builder.toString().getBytes(), 
				    StandardOpenOption.CREATE, 
				    StandardOpenOption.WRITE, 
				    StandardOpenOption.TRUNCATE_EXISTING);
			} catch(IOException e) {
				System.err.println("An error occurred while writing verifcation strats: " + e.getMessage());
			}
		
	}

}
