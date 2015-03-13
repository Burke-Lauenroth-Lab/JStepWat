package stepwat.input.SXW;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import stepwat.LogFileIn;
import stepwat.input.Input;
import stepwat.input.ST.Rgroup;

public class Phenology extends Input {
	public List<GrpPhenInfo> listGrps = new ArrayList<GrpPhenInfo>();
	
	public class GrpPhenInfo {
		public String grpName;
		public float[] monthlyValues = new float[12];
		
		public GrpPhenInfo(String name) {
			this.grpName = name;
		}
	}

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
				default:
					String name = values[0];				
					GrpPhenInfo newInfo = new GrpPhenInfo(name);
					
					try {
						for(int i=0; i<12; i++) {
							newInfo.monthlyValues[i] = Float.valueOf(values[i+1]);
						}
					} catch(NumberFormatException e) {
						f.LogError(LogFileIn.LogMode.ERROR, "sxwphen.in read : Could not convert monthly values." + e.toString());
					}
					
					listGrps.add(newInfo);
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
		int maxStringLength = getMaxStringLength()+3;
		lines.add("# phen definition file STEPPEWAT\n");
		lines.add("# Monthly phenological activity values for each resource");
		lines.add("# group in the STEPWAT model in percent.  Activity is defined as the");
		lines.add("# growth activity for that month as a proportion of the total.");
		lines.add("# For example, if half of a plant group's yearly growth occurs");
		lines.add("# in one month, that month's value for that group would be 0.50.");
		lines.add("# Metabolic activity doesn't count");
		lines.add("#");
		lines.add("# First column is the group name as defined in the");
		lines.add("# group parameters file for STEPPE.  The name must be");
		lines.add("# spelled exactly the same.");
		lines.add("# Remaining columns are monthly activity values.");
		lines.add("# All the activity values for a group must sum to 100");
		
		lines.add("# Group" + getSpacing(maxStringLength-7) + "Jan   Feb   Mar   Apr   May   Jun   Jul   Aug   Sep    Oct   Nov   Dec");
		for(GrpPhenInfo gli : listGrps) {
			String line = gli.grpName + getSpacing(maxStringLength-gli.grpName.length());
			for(int i=0; i<gli.monthlyValues.length; i++) {
				line += String.format("%-4.3f", gli.monthlyValues[i])+"  ";
			}
			lines.add(line);
		}
		java.nio.file.Files.write(file, lines, StandardCharsets.UTF_8);
	}
	
	public boolean verify(Rgroup rgroups) throws Exception {
		LogFileIn f = stepwat.LogFileIn.getInstance();
		for(GrpPhenInfo gpi : listGrps) {
			if(rgroups.ResourceParams_Name2Index(gpi.grpName) == -1) {
				f.LogError(LogFileIn.LogMode.NOTE, "sxwphen.in verify : " + gpi.grpName + " not found in Resource Groups.");
				return false;
			}
		}
		if(rgroups.groups.size() != listGrps.size()) {
			f.LogError(LogFileIn.LogMode.NOTE, "sxwphen.in verify : to few or too many groups.");
		}
		
		return true;
	}
	
	private int getMaxStringLength() {
		List<Integer> lengths = new ArrayList<Integer>();
		for(GrpPhenInfo gli : listGrps) {
			lengths.add(gli.grpName.length());
		}
		return Collections.max(lengths);
	}
	
	private String getSpacing(int length) {
		return new String(new char[length]).replace("\0", " ");
	}
}
