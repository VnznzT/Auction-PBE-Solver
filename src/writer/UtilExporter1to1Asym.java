package writer;

import utility.PWCUtility;
import utility.Utility;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;

public class UtilExporter1to1Asym implements UtilExporter {

	@Override
	public void exportUtil(List<Utility> utilities, String fp, Path path) {
				StringBuilder builder = new StringBuilder();
		
				for(int k=0;k<utilities.size();k++) {
			PWCUtility util = (PWCUtility) utilities.get(k);
			double[] v = util.values;
			for(int l=1;l<v.length-1;l++) {
				builder.append(String.format("%7.6f ", v[l]));
			}
			builder.append("\n");
			double[] u = util.utilities;
			util = (PWCUtility) utilities.get(k);
			u = util.utilities;
			for(int l=1;l<v.length-1;l++) {
				builder.append(String.format("%7.6f ", u[l]));
			}
			builder.append("\n");
		}
		Path exportPath=path.resolve(String.format("%s.utils", fp));
				try {
	        Files.createDirectories(exportPath.getParent());
	        
			Files.write(exportPath, builder.toString().getBytes(), 
				    StandardOpenOption.CREATE, 
				    StandardOpenOption.WRITE, 
				    StandardOpenOption.TRUNCATE_EXISTING);
			} catch(IOException e) {
				System.err.println("An error occurred while writing verifcation utils: " + e.getMessage());
			}
		
	}

}
