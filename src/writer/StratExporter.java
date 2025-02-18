package writer;

import java.nio.file.Path;
import java.util.List;

import strategy.Strategy;

public interface StratExporter {
	public void exportStrat(List<Strategy> strategies, String fp, Path path);
}
