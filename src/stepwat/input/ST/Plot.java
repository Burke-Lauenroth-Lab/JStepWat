package stepwat.input.ST;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import stepwat.LogFileIn;
import stepwat.input.Input;

public class Plot extends Input {
	public static final String Comment = "# Plot input definition file STEPPEWAT\n"+
			"\n"+
			"# Anything after the first pound sign is a comment\n"+
			"# Blank lines, white space, and comments may be used freely,\n"+
			"# however, the order of input is important\n"+
			"\n"+
			"####################################################\n"+
			"# plotsize = area of a plot in square meters.  Everything\n"+
			"#        revolves around the plotsize, which, in C&L 1990,\n"+
			"#        is .125, same as an individual of blue grama.\n"+
			"#\n"+
			"#Future columns may include the following: \n"+
			"#   Rows=number of rows in a landscape matrix\n"+
			"#   Cols=number of cols in a landscape matrix\n"+
			"#   Vertices=number of vertices in plot boundary. Plots can be\n"+
			"#        a tesselation of regular polygons.\n"+
			"#\n"+
			"\n"+
			"# plotsize";
	
	public float plotsize;
	
	public void read(Path PlotInPath) throws Exception {
		List<String> lines = java.nio.file.Files.readAllLines(PlotInPath, StandardCharsets.UTF_8);
		LogFileIn f = stepwat.LogFileIn.getInstance();
		int nFileItemsRead = 0;
		for (String line : lines) {
			// Skip Comments and empty lines
			if (!line.matches("^\\s*#.*") && !line.matches("^[\\s]*$")) {
				line = line.trim();
				String[] values = line.split("#")[0].split("[ \t]+");// Remove comment  after data
				switch (nFileItemsRead) {
				case 0:
					if(values.length != 1)
						f.LogError(LogFileIn.LogMode.ERROR, "Plot.in read : Expected 1 value.");
					try {
						plotsize = Float.parseFloat(values[0]);
					} catch(NumberFormatException e) {
						f.LogError(LogFileIn.LogMode.ERROR, "Plot.in read : Could not convert to number.");
					}
					break;
				default:
					f.LogError(LogFileIn.LogMode.ERROR, "Plot.in : unkown line.");
					break;
				}
				nFileItemsRead++;
			}
		}
		this.data = true;
	}
	
	public void write(Path PlotInPath) throws IOException {
		List<String> lines = new ArrayList<String>();
		lines.add(Comment);
		lines.add(Float.toString(plotsize));
		java.nio.file.Files.write(PlotInPath, lines, StandardCharsets.UTF_8);
	}
}
