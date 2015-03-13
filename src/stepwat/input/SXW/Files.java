package stepwat.input.SXW;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import stepwat.LogFileIn;
import stepwat.input.Input;

public class Files extends Input {
	String Times;
	String Roots;
	String Phenology;
	String BVT;
	String Production;
	String SoilWatFiles;
	public boolean debug = false;
	String DeBug;
	
	@Override
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
				//case 0:
				//	if (values.length != 1)
				//		f.LogError(LogFileIn.LogMode.ERROR, "ST SXW sxw.in : sxwtimes.in name has a space.");
				//	Times = values[0];
				//	break;
				case 0:
					if (values.length != 1)
						f.LogError(LogFileIn.LogMode.ERROR, "ST SXW sxw.in : sxwroots.in name has a space.");
					Roots = values[0];
					break;
				case 1:
					if (values.length != 1)
						f.LogError(LogFileIn.LogMode.ERROR, "ST SXW sxw.in : sxwphen.in name has a space.");
					Phenology = values[0];
					break;
				case 2:
					if (values.length != 1)
						f.LogError(LogFileIn.LogMode.ERROR, "ST SXW sxw.in : sxwbvt.in name has a space.");
					BVT = values[0];
					break;
				case 3:
					if (values.length != 1)
						f.LogError(LogFileIn.LogMode.ERROR, "ST SXW sxw.in : sxwprod.in name has a space.");
					Production = values[0];
					break;
				case 4:
					if (values.length != 1)
						f.LogError(LogFileIn.LogMode.ERROR, "ST SXW sxw.in : files.in name has a space.");
					SoilWatFiles = values[0];
					break;
				case 5:
					if (values.length != 1)
						f.LogError(LogFileIn.LogMode.ERROR, "ST SXW sxw.in : debug.in name has a space.");
					DeBug = values[0];
					debug = true;
					break;
				default:
					f.LogError(LogFileIn.LogMode.ERROR,
							"SXW sxw.in : unkown line.");
					break;
				}
				nFileItemsRead++;
			}
		}
		this.data = true;
	}
	
	@Override
	public void write(Path gridFilesIn) throws IOException {
		List<String> lines = new ArrayList<String>();
		int maxStringLength = getMaxStringLength()+4;
		lines.add("# sxw input definition file STEPPEWAT - DLM 07-19-12");
		lines.add("# 6 files total... apparently it can also include the directory");
		lines.add("");
		lines.add(Times + getSpacing(maxStringLength-Times.length()) + "#times");
		lines.add(Roots + getSpacing(maxStringLength-Roots.length()) + "#roots");
		lines.add(Phenology + getSpacing(maxStringLength-Phenology.length()) + "#phen");
		lines.add(BVT + getSpacing(maxStringLength-BVT.length()) + "#bvt");
		lines.add(Production + getSpacing(maxStringLength-Production.length()) + "#prod");
		lines.add(SoilWatFiles + getSpacing(maxStringLength-SoilWatFiles.length()) + "#soilwat files.in");
		if(debug)
			lines.add(DeBug + getSpacing(maxStringLength-DeBug.length()) + "#debug.in Leave blank for no debugging files");
		java.nio.file.Files.write(gridFilesIn, lines, StandardCharsets.UTF_8);
	}
	
	private int getMaxStringLength() {
		List<Integer> lengths = new ArrayList<Integer>();
		lengths.add(Times.length());
		lengths.add(Roots.length());
		lengths.add(Phenology.length());
		lengths.add(BVT.length());
		lengths.add(Production.length());
		lengths.add(SoilWatFiles.length());
		if(debug)
			lengths.add(DeBug.length());
		return Collections.max(lengths);
	}
	
	private String getSpacing(int length) {
		return new String(new char[length]).replace("\0", " ");
	}
}
