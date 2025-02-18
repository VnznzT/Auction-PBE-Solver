package Analysis;

import java.nio.file.Path;
import java.util.List;

import strategy.Strategy;

public interface StratsReader {

	List<List<Strategy>> read(Path pathStrategies);

}
