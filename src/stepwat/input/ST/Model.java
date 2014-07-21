package stepwat.input.ST;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import stepwat.LogFileIn;
import stepwat.input.Input;

/**
 * Input-level parameters in STEPPE
 * @author Ryan Murphy
 *
 */
public class Model extends Input {
	/***
	 * niter = number of iterations to run the model.<br>
	 *         I.e., the model runs niter times for<br>
	 *         nyrs years; statistics are collected with<br>
	 *         each year in each iteration as a sample.
	 */
	public int nIterations;
	/***
	 * # nyrs = Number of years to run each iteration of<br>
	 * the model
	 */
	public int nYears;
	/***
	 * seed = random number seed.  Can be set to a
	 * specific number, but setting it to 0
	 * causes it to be reset each iteration
	 * based on the cpu clock (ie, process time).<br>
	 * Note: if niter is > 1, set seed to 0.  In fact,
	 * it doesn't hurt to set it to 0 always.
	 */
	public int seed;
	
	public void read(Path ModelInPath) throws Exception {
		List<String> lines = java.nio.file.Files.readAllLines(ModelInPath, StandardCharsets.UTF_8);
		LogFileIn f = stepwat.LogFileIn.getInstance();
		int nFileItemsRead = 0;
		for (String line : lines) {
			// Skip Comments and empty lines
			if (!line.matches("^\\s*#.*") && !line.matches("^[\\s]*$")) {
				line = line.trim();
				String[] values = line.split("#")[0].split("[ \t]+");// Remove comment  after data
				switch (nFileItemsRead) {
				case 0:
					if(values.length != 3)
						f.LogError(LogFileIn.LogMode.ERROR, "Model.in read : Expected 3 values.");
					try {
						nIterations = Integer.parseInt(values[0]);
						nYears = Integer.parseInt(values[1]);
						seed = Integer.parseInt(values[2]);
					} catch(NumberFormatException e) {
						f.LogError(LogFileIn.LogMode.ERROR, "Model.in read : Could not convert to Integers.");
					}
					break;
				default:
					f.LogError(LogFileIn.LogMode.ERROR, "Model.in : unkown line.");
					break;
				}
				nFileItemsRead++;
			}
		}
		this.data = true;
	}
	
	public void write(Path ModelInPath) throws IOException {
		List<String> lines = new ArrayList<String>();
		lines.add("# Model-level parameters in STEPPE");
		lines.add("");
		lines.add("# Anything after the first pound sign is a comment");
		lines.add("# Blank lines, white space, and comments may be used freely,");
		lines.add("# however, the order of input is important");
		lines.add("");
		lines.add("####################################################");
		lines.add("# niter = number of iterations to run the model.");
		lines.add("#         I.e., the model runs niter times for");
		lines.add("#         nyrs years; statistics are collected with");
		lines.add("#         each year in each iteration as a sample.");
		lines.add("# nyrs = Number of years to run each iteration of");
		lines.add("#        the model");
		lines.add("# seed = random number seed.  Can be set to a");
		lines.add("#        specific number, but setting it to 0");
		lines.add("#        causes it to be reset each iteration");
		lines.add("#        based on the cpu clock (ie, process time).");
		lines.add("# Note: if niter is > 1, set seed to 0.  In fact,");
		lines.add("#       it doesn't hurt to set it to 0 always.");
		lines.add("");
		lines.add("# niter nyrs  seed");
		lines.add("  " + String.valueOf(nIterations)+"\t"+String.valueOf(nYears)+"\t\t"+String.valueOf(seed));
		java.nio.file.Files.write(ModelInPath, lines, StandardCharsets.UTF_8);
	}
}
