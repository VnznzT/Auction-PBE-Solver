package writer;

import java.nio.file.Path;
import java.util.List;

import utility.Utility;

public interface UtilExporter {
	public void exportUtil(List<Utility> utilities, String fp, Path path);
}
