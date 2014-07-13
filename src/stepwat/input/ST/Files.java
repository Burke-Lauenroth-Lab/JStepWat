package stepwat.input.ST;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import stepwat.LogFileIn;

public class Files {
	public String logfile;
	public String model;
	public String env;
	public String plot;
	public String rgroup;
	public String species;
	public String bmassflags;
	public String bmasspre;
	public String bmassavg;
	public String mortflags;
	public String mortpre;
	public String mortavg;
	public String sxw;
	
	public void read(Path FilesPath) throws Exception {
		List<String> lines = java.nio.file.Files.readAllLines(FilesPath, StandardCharsets.UTF_8);
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
						f.LogError(LogFileIn.LogMode.ERROR, "ST files.in : logfile.log name has a space.");
					logfile = values[0];
					break;
				case 1:
					if (values.length != 1)
						f.LogError(LogFileIn.LogMode.ERROR, "ST files.in : model.in name has a space.");
					model = values[0];
					break;
				case 2:
					if (values.length != 1)
						f.LogError(LogFileIn.LogMode.ERROR, "ST files.in : env.in name has a space.");
					env = values[0];
					break;
				case 3:
					if (values.length != 1)
						f.LogError(LogFileIn.LogMode.ERROR, "ST files.in : plot.in name has a space.");
					plot = values[0];
					break;
				case 4:
					if (values.length != 1)
						f.LogError(LogFileIn.LogMode.ERROR, "ST files.in : rgroup.in name has a space.");
					rgroup = values[0];
					break;
				case 5:
					if (values.length != 1)
						f.LogError(LogFileIn.LogMode.ERROR, "ST files.in : species.in name has a space.");
					species = values[0];
					break;
				case 6:
					if (values.length != 1)
						f.LogError(LogFileIn.LogMode.ERROR, "ST files.in : bmassflags.in name has a space.");
					bmassflags = values[0];
					break;
				case 7:
					if (values.length != 1)
						f.LogError(LogFileIn.LogMode.ERROR, "ST files.in : bmasspre.in name has a space.");
					bmasspre = values[0];
					break;
				case 8:
					if (values.length != 1)
						f.LogError(LogFileIn.LogMode.ERROR, "ST files.in : bmassavg.in has a space.");
					bmassavg = values[0];
					break;
				case 9:
					if (values.length != 1)
						f.LogError(LogFileIn.LogMode.ERROR, "ST files.in : mortflags.in output filename has a space.");
					mortflags = values[0];
					break;
				case 10:
					if (values.length != 1)
						f.LogError(LogFileIn.LogMode.ERROR, "ST files.in : mortpre.in has a space.");
					mortpre = values[0];
					break;
				case 11:
					if (values.length != 1)
						f.LogError(LogFileIn.LogMode.ERROR, "ST files.in : mortavg.in has a space.");
					mortavg = values[0];
					break;
				case 12:
					if (values.length != 1)
						f.LogError(LogFileIn.LogMode.ERROR, "ST files.in : sxw.in has a space.");
					sxw = values[0];
					break;
				default:
					f.LogError(LogFileIn.LogMode.ERROR,
							"Grid files.in : unkown line.");
					break;
				}
				nFileItemsRead++;
			}
		}
	}
	public void write(Path gridFilesIn) throws IOException {
		List<String> lines = new ArrayList<String>();
		int maxStringLength = getMaxStringLength()+4;
		lines.add("# List of input files for STEPPEWAT - DLM 07-16-12");
		lines.add("");
		lines.add(logfile + getSpacing(maxStringLength-logfile.length()) + "#the logfile... can be stdout to print out to the terminal");
		lines.add("");
		lines.add(model + getSpacing(maxStringLength-model.length()) + "#for the model");
		lines.add(env + getSpacing(maxStringLength-env.length()) + "#for the environment");
		lines.add(plot + getSpacing(maxStringLength-plot.length()) + "#for the plot");
		lines.add(rgroup + getSpacing(maxStringLength-rgroup.length()) + "#for the rgroup (resource-group-level information)");
		lines.add(species + getSpacing(maxStringLength-species.length()) + "#for the species information");
		lines.add("");
		lines.add(bmassflags + getSpacing(maxStringLength-bmassflags.length()) + "#biomass flags... (for setting up the output I think)");
		lines.add(bmasspre + getSpacing(maxStringLength-bmasspre.length()) + "#output");
		lines.add(bmassavg + getSpacing(maxStringLength-bmassavg.length()) + "#output");
		lines.add("");
		lines.add(mortflags + getSpacing(maxStringLength-mortflags.length()) + "#mort flags... (for setting up the output I think)");
		lines.add(bmasspre + getSpacing(maxStringLength-bmasspre.length()) + "#output");
		lines.add(bmassavg + getSpacing(maxStringLength-bmassavg.length()) + "#output");
		lines.add("");
		lines.add(sxw + getSpacing(maxStringLength-sxw.length()) + "#");
		java.nio.file.Files.write(gridFilesIn, lines, StandardCharsets.UTF_8);
	}
	private int getMaxStringLength() {		
		List<Integer> lengths = new ArrayList<Integer>();
		lengths.add(logfile.length());
		lengths.add(model.length());
		lengths.add(env.length());
		lengths.add(plot.length());
		lengths.add(rgroup.length());
		lengths.add(species.length());
		lengths.add(bmassflags.length());
		lengths.add(bmasspre.length());
		lengths.add(bmassavg.length());
		lengths.add(mortflags.length());
		lengths.add(mortpre.length());
		lengths.add(mortavg.length());
		lengths.add(sxw.length());
		return Collections.max(lengths);
	}
	private String getSpacing(int length) {
		return new String(new char[length]).replace("\0", " ");
	}
}
