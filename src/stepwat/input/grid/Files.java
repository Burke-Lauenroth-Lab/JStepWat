package stepwat.input.grid;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import stepwat.LogFileIn;

public class Files {
	/**
	 * folder containing stepwat setup files
	 */
	public String StepWatFilesDir; 
	public String logfile; // name of logfile (can also be stdout)
	public String setup; // name of grid setup file
	public String disturbances; // name of grid disturbances input file
	public String soils; // name of grid soils input file
	public String seedDispersal; // name of grid seed dispersal setup file
	public String initSpecies; // name of grid species input file
	public String ST_Files; // name of stepwat files.in file
	public String bmassavg; // name of the prefix given to the biomass output
							// files
	public String mortavg; // name of the prefix given to the mortuary output
							// files
	public String receiveprob; // name of the prefix given to the seed disperal
								// received probability output files

	public void read(String FilesPath) throws Exception {
		List<String> lines = java.nio.file.Files.readAllLines(
				Paths.get(FilesPath), StandardCharsets.UTF_8);
		LogFileIn f = stepwat.LogFileIn.getInstance();
		int nFileItemsRead = 0;
		for (String line : lines) {
			// Skip Comments and empty lines
			if (!line.matches("^\\s*#.*") && !line.matches("^[\\s]*$")) {
				line = line.trim();
				String[] values = line.split("#")[0].split("[ \t]+");// Remove comment  after data
				switch (nFileItemsRead) {
				case 0:
					StringBuilder dir = new StringBuilder();
					for (String s : values) {
						dir.append(s + " ");
					}
					StepWatFilesDir = dir.toString().trim();
					break;
				case 1:
					if (values.length != 1)
						f.LogError(LogFileIn.LogMode.ERROR,
								"Grid files.in : logfile name has a space.");
					logfile = values[0];
					break;
				case 2:
					if (values.length != 1)
						f.LogError(LogFileIn.LogMode.ERROR,
								"Grid files.in : grid_setup.in name has a space.");
					setup = values[0];
					break;
				case 3:
					if (values.length != 1)
						f.LogError(LogFileIn.LogMode.ERROR,
								"Grid files.in : grid_disturbances.csv name has a space.");
					disturbances = values[0];
					break;
				case 4:
					if (values.length != 1)
						f.LogError(LogFileIn.LogMode.ERROR,
								"Grid files.in : grid_soils.csv name has a space.");
					soils = values[0];
					break;
				case 5:
					if (values.length != 1)
						f.LogError(LogFileIn.LogMode.ERROR,
								"Grid files.in : grid_seed_dispersal.in name has a space.");
					seedDispersal = values[0];
					break;
				case 6:
					if (values.length != 1)
						f.LogError(LogFileIn.LogMode.ERROR,
								"Grid files.in : grid_initSpecies.csv name has a space.");
					initSpecies = values[0];
					break;
				case 7:
					if (values.length != 1)
						f.LogError(LogFileIn.LogMode.ERROR,
								"Grid files.in : stepwat files.in name has a space.");
					ST_Files = values[0];
					break;
				case 8:
					if (values.length != 1)
						f.LogError(LogFileIn.LogMode.ERROR,
								"Grid files.in : biomass output file prefix name has a space.");
					bmassavg = values[0];
					break;
				case 9:
					if (values.length != 1)
						f.LogError(LogFileIn.LogMode.ERROR,
								"Grid files.in : mortuary output file prefix name has a space.");
					mortavg = values[0];
					break;
				case 10:
					if (values.length != 1)
						f.LogError(LogFileIn.LogMode.ERROR,
								"Grid files.in : Seed Disperal Received Probability output file prefix name has a space.");
					receiveprob = values[0];
					break;
				default:
					f.LogError(LogFileIn.LogMode.ERROR,
							"Grid files.in : unkown line.");
					break;
				}
			}
		}
	}
	public void write(Path gridFilesIn) throws IOException {
		List<String> lines = new ArrayList<String>();
		int maxStringLength = getMaxStringLength()+4;
		lines.add("# List of files/folders for STEPWAT grid version - DLM 05-24-13");
		lines.add("");
		lines.add(StepWatFilesDir + getSpacing(maxStringLength-StepWatFilesDir.length()) + "# folder containing stepwat setup files");
		lines.add("");
		lines.add(logfile + getSpacing(maxStringLength-logfile.length()) + "# name of logfile (can also be stdout)");
		lines.add("");
		lines.add(setup + getSpacing(maxStringLength-setup.length()) + "# name of grid setup file");
		lines.add(disturbances + getSpacing(maxStringLength-disturbances.length()) + "# name of grid disturbances input file");
		lines.add(soils + getSpacing(maxStringLength-soils.length()) + "# name of grid soils input file");
		lines.add(seedDispersal + getSpacing(maxStringLength-seedDispersal.length()) + "# name of grid seed dispersal setup file");
		lines.add(initSpecies + getSpacing(maxStringLength-initSpecies.length()) + "# name of grid species input file");
		lines.add("");
		lines.add(ST_Files + getSpacing(maxStringLength-ST_Files.length()) + "# name of stepwat files.in file");
		lines.add("");
		lines.add(bmassavg + getSpacing(maxStringLength-bmassavg.length()) + "# name of the prefix given to the biomass output files");
		lines.add(mortavg + getSpacing(maxStringLength-mortavg.length()) + "# name of the prefix given to the mortuary output files");
		lines.add(receiveprob + getSpacing(maxStringLength-receiveprob.length()) + "# name of the prefix given to the seed disperal received probability output files");
		java.nio.file.Files.write(gridFilesIn, lines, StandardCharsets.UTF_8);
	}
	private int getMaxStringLength() {
		List<Integer> lengths = new ArrayList<Integer>();
		lengths.add(StepWatFilesDir.length());
		lengths.add(logfile.length());
		lengths.add(setup.length());
		lengths.add(soils.length());
		lengths.add(seedDispersal.length());
		lengths.add(initSpecies.length());
		lengths.add(ST_Files.length());
		lengths.add(bmassavg.length());
		lengths.add(mortavg.length());
		lengths.add(receiveprob.length());
		return Collections.max(lengths);
	}
	private String getSpacing(int length) {
		return new String(new char[length]).replace("\0", " ");
	}
}
