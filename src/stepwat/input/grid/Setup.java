package stepwat.input.grid;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import stepwat.LogFileIn;

public class Setup {
	int rows;
	int columns;
	boolean disturbances;
	boolean soils;
	boolean seedDispersal;
	
	public void read(String GridSetup) throws Exception {
		List<String> lines = java.nio.file.Files.readAllLines(
				Paths.get(GridSetup), StandardCharsets.UTF_8);
		LogFileIn f = stepwat.LogFileIn.getInstance();
		int nFileItemsRead = 0;
		for (String line : lines) {
			// Skip Comments and empty lines
			if (!line.matches("^\\s*#.*") && !line.matches("^[\\s]*$")) {
				line = line.trim();
				String[] values = line.split("#")[0].split("[ \t]+");// Remove comment  after data
				switch (nFileItemsRead) {
				case 0:
					if(values.length != 2)
						f.LogError(LogFileIn.LogMode.ERROR, "grid_setup.in onRead : rows cols : Expected 2 Values read "+String.valueOf(values.length));
					try {
						rows = Integer.parseInt(values[0]);
						columns = Integer.parseInt(values[1]);
					} catch(NumberFormatException e) {
						f.LogError(LogFileIn.LogMode.ERROR, "grid_setup.in onRead : rows cols: Could not convert string to two integers. " + e.getMessage());
					}
					break;
				case 1:
					if (values.length != 1)
						f.LogError(LogFileIn.LogMode.ERROR,
								"grid_setup.in : use disturbances : one integer required.");
					try {
						disturbances = Integer.parseInt(values[0])>0 ? true : false;
					} catch(NumberFormatException e) {
						f.LogError(LogFileIn.LogMode.ERROR, "grid_setup.in : use disturbances : Could not determine the boolean value." + e.getMessage());
					}
					break;
				case 2:
					if (values.length != 1)
						f.LogError(LogFileIn.LogMode.ERROR,
								"grid_setup.in : use soils : one integer required.");
					try {
						soils = Integer.parseInt(values[0])>0 ? true : false;
					} catch(NumberFormatException e) {
						f.LogError(LogFileIn.LogMode.ERROR, "grid_setup.in : use soils : Could not determine the boolean value." + e.getMessage());
					}
					break;
				case 3:
					if (values.length != 1)
						f.LogError(LogFileIn.LogMode.ERROR,
								"grid_setup.in : seed dispersal : one int required");
					try {
						seedDispersal = Integer.parseInt(values[0])>0 ? true : false;
					} catch(NumberFormatException e) {
						f.LogError(LogFileIn.LogMode.ERROR, "grid_setup.in : use seed dispersal : Could not determine the boolean value." + e.getMessage());
					}
					break;
				default:
					f.LogError(LogFileIn.LogMode.ERROR,
							"grid_setup.in : unkown line.");
					break;
				}
			}
		}
	}
	public void write(Path gridSetupIn) throws IOException {
		List<String> lines = new ArrayList<String>();
		int maxStringLength = getMaxStringLength()+4;
		
		lines.add("# Grid setup for STEPWAT grid version - DLM 6-11-13");
		lines.add("");
		lines.add(Integer.toString(rows) + " " + Integer.toString(columns) + getSpacing(maxStringLength-(Integer.toString(rows).length() + Integer.toString(columns).length() + 1)) + "# folder containing stepwat setup files");
		lines.add(String.valueOf(disturbances?1:0) + getSpacing(maxStringLength-1) + "# name of logfile (can also be stdout)");
		lines.add(String.valueOf(soils?1:0) + getSpacing(maxStringLength-1) + "# name of grid setup file");
		lines.add(String.valueOf(seedDispersal?1:0) + getSpacing(maxStringLength-1) + "# name of grid disturbances input file");
		Files.write(gridSetupIn, lines, StandardCharsets.UTF_8);
	}
	private int getMaxStringLength() {
		List<Integer> lengths = new ArrayList<Integer>();
		lengths.add(Integer.toString(rows).length() + Integer.toString(columns).length() + 1);
		lengths.add(1);
		return Collections.max(lengths);
	}
	private String getSpacing(int length) {
		return new String(new char[length]).replace("\0", " ");
	}
}
