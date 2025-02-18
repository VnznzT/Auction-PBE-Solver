package Analysis;

import java.nio.file.Path;

import ClusterRuns.Statistics;

public interface Analysis {

	public Statistics analyse(Path pathStrategies);
	
}
