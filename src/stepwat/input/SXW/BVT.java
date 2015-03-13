package stepwat.input.SXW;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import stepwat.LogFileIn;
import stepwat.input.Input;

public class BVT extends Input {
	
	public float biomass;
	public float transpiration;
	
	@Override
	public void read(Path file) throws Exception {
		List<String> lines = java.nio.file.Files.readAllLines(file, StandardCharsets.UTF_8);
		LogFileIn f = stepwat.LogFileIn.getInstance();
		int nFileItemsRead = 0;
		for (String line : lines) {
			// Skip Comments and empty lines
			if (!line.matches("^\\s*#.*") && !line.matches("^[\\s]*$")) {
				line = line.trim();
				String[] values = line.split("#")[0].split("[ \t]+");// Remove comment  after data
				switch (nFileItemsRead) {
				case 0:
					if (values.length != 1)
						f.LogError(LogFileIn.LogMode.ERROR, "ST SXW sxwbvt.in : sxwbvt.in biomass needs one value.");
					try {
						biomass = Float.valueOf(values[0]);
					} catch(NumberFormatException e) {
						f.LogError(LogFileIn.LogMode.ERROR, "ST SXW sxwbvt.in : sxwbvt.in biomass had a problem."+e.toString());
					}
					break;
				case 1:
					if (values.length != 1)
						f.LogError(LogFileIn.LogMode.ERROR, "ST SXW sxwbvt.in : sxwbvt.in transpiration needs one value.");
					try {
						transpiration = Float.valueOf(values[0]);
					} catch(NumberFormatException e) {
						f.LogError(LogFileIn.LogMode.ERROR, "ST SXW sxwbvt.in : sxwbvt.in transpiration had a problem."+e.toString());
					}
					break;
				default:
					f.LogError(LogFileIn.LogMode.ERROR,
							"SXW bvt.in : unkown line.");
					break;
				}
				nFileItemsRead++;
			}
		}
		this.data = true;
	}

	@Override
	public void write(Path file) throws IOException {
		List<String> lines = new ArrayList<String>();
		lines.add("# sxw bvt definition file STEPPEWAT - DLM 07-19-12");
		lines.add("");
		lines.add("# read two numbers, biomass (g/m2) and transpiration (cm/m2) for that biomass to construct a simple linear relationship that gives g biomass / cm transp per m2.");
		lines.add(String.format("%5.4f", biomass) + " # biomass (g/m2)");
		lines.add(String.format("%5.4f", transpiration) + " # transpiration (cm/m2)");
		java.nio.file.Files.write(file, lines, StandardCharsets.UTF_8);
	}
	
}
