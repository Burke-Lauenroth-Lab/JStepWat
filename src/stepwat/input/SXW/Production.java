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

public class Production extends Input {

	public class GrpProdMonthlyValues {
		public String grpName;
		public float[] monthlyValues = new float[12];
		
		public GrpProdMonthlyValues(String name) {
			this.grpName = name;
		}
		
		public String toString() {
			String line = "";
			
			return line;
		}
	}
	
	public float[] litter = new float[12];
	public List<GrpProdMonthlyValues> bmass = new ArrayList<GrpProdMonthlyValues>();
	public List<GrpProdMonthlyValues> pctlive = new ArrayList<GrpProdMonthlyValues>();
	
	@Override
	public void read(Path file) throws Exception {
		List<String> lines = java.nio.file.Files.readAllLines(file, StandardCharsets.UTF_8);
		//LogFileIn f = stepwat.LogFileIn.getInstance();
		int nFileItemsRead = 0;
		int group = 0;
		for (String line : lines) {
			// Skip Comments and empty lines
			if (!line.matches("^\\s*#.*") && !line.matches("^[\\s]*$")) {
				line = line.trim();
				String[] values = line.split("#")[0].split("[ \t]+");// Remove comment  after data
				if(values[0].compareToIgnoreCase("[end]") == 0) {//Check for next group of values
					group++;
					nFileItemsRead = 0;
					continue;
				}
				switch (group) {
				case 0:
					readLitter(values, nFileItemsRead);
					break;
				case 1:
					readBmass(values);
					break;
				case 2:
					readPCTLive(values);
					break;
				default:
					
					break;
				}
				nFileItemsRead++;
			}
		}
		
		this.data = true;
	}
	
	private void readLitter(String[] values, int nFileItemsRead) throws Exception {
		LogFileIn f = stepwat.LogFileIn.getInstance();
		try {
			litter[nFileItemsRead] = Float.valueOf(values[0]);
		} catch(NumberFormatException e) {
			f.LogError(LogFileIn.LogMode.ERROR, "sxwprod.in read litter: Could not convert litter bmass distribution value.");
		}
	}
	
	private void readBmass(String[] values) throws Exception {
		LogFileIn f = stepwat.LogFileIn.getInstance();
		String name = values[0];				
		GrpProdMonthlyValues newInfo = new GrpProdMonthlyValues(name);
		
		try {
			for(int i=0; i<12; i++) {
				newInfo.monthlyValues[i] = Float.valueOf(values[i+1]);
			}
		} catch(NumberFormatException e) {
			f.LogError(LogFileIn.LogMode.ERROR, "sxwprod.in read bmass: Could not convert monthly values." + e.toString());
		}
		
		bmass.add(newInfo);
	}
	
	private void readPCTLive(String[] values) throws Exception {
		LogFileIn f = stepwat.LogFileIn.getInstance();
		String name = values[0];				
		GrpProdMonthlyValues newInfo = new GrpProdMonthlyValues(name);
		
		try {
			for(int i=0; i<12; i++) {
				newInfo.monthlyValues[i] = Float.valueOf(values[i+1]);
			}
		} catch(NumberFormatException e) {
			f.LogError(LogFileIn.LogMode.ERROR, "sxwprod.in read pctlive: Could not convert monthly values." + e.toString());
		}
		
		pctlive.add(newInfo);
	}

	@Override
	public void write(Path file) throws IOException {
		List<String> lines = new ArrayList<String>();
		int maxStringLength = getMaxStringLength()+3;
		lines.add("# sxw prod definition file STEPPEWAT\n");
		lines.add("#Production Coefficients for STEPWAT\n");
		lines.add("# These values are used to convert yearly biomass");
		lines.add("# from STEPPE to monthly production values for SOILWAT\n");
		lines.add("# BMASS - convert STEPPE's total biomass values (g/m^2) to");
		lines.add("#         aboveground biomass (g/m^2). Needn't sum to 1.");
		lines.add("#         This is a percent of the total yearly biomass.");
		lines.add("#         (STEPPE's yearly vals are converted to monthly).");
		lines.add("# LITTER - use the maximum value from the converted biomass");
		lines.add("#          to multiply by this number to compute a litter");
		lines.add("#          value in g/m^2.  Needn't sum to 1.0.\n");
		lines.add("# BMASS  LITTER");
		lines.add(String.format("%-5.3f", litter[0]) + "\t# January");
		lines.add(String.format("%-5.3f", litter[1]) + "\t# February");
		lines.add(String.format("%-5.3f", litter[2]) + "\t# March");
		lines.add(String.format("%-5.3f", litter[3]) + "\t# April");
		lines.add(String.format("%-5.3f", litter[4]) + "\t# May");
		lines.add(String.format("%-5.3f", litter[5]) + "\t# June");
		lines.add(String.format("%-5.3f", litter[6]) + "\t# July");
		lines.add(String.format("%-5.3f", litter[7]) + "\t# August");
		lines.add(String.format("%-5.3f", litter[8]) + "\t# September");
		lines.add(String.format("%-5.3f", litter[9]) + "\t# October");
		lines.add(String.format("%-5.3f", litter[10]) + "\t# November");
		lines.add(String.format("%-5.3f", litter[11]) + "\t# December");
		lines.add("");
		lines.add("[end]  # section end");
		lines.add("");
		lines.add("# BMASS");
		lines.add("# This is the percent of yearly biomass that each month has");
		lines.add("# Example of how Grass (bouteloua,p.graminoids,a.gram/shrub) biomass is calculated");
		lines.add("# Biomass (150,50,10) for the RGroups above");
		lines.add("# bouteloua    90  	90	 90   102  114  132  150  132  114   108  102  96");
		lines.add("# p.graminoids 30   30   30   34   38   44   50   44   38    36   34   32");
		lines.add("#a.gram/shrub  6    6     6   6.8  7.6  8.8  10   8.8  7.6   7.2  6.8  6.4");
		lines.add("#-------------------------------------------------------------------------");
		lines.add("#Grass Biomass 126 126 126  142.8 159.6 184.8 210 184  159.6 151.2 142.8 134.4");
		lines.add("#The above value would be converted to above ground biomass by sxwprod.in Biomass column");
		lines.add("# Group" + getSpacing(maxStringLength-7) + "Jan   Feb   Mar   Apr   May   Jun   Jul   Aug   Sep    Oct   Nov   Dec");
		for(GrpProdMonthlyValues gli : bmass) {
			String line = gli.grpName + getSpacing(maxStringLength-gli.grpName.length());
			for(int i=0; i<gli.monthlyValues.length; i++) {
				line += String.format("%-4.3f", gli.monthlyValues[i])+"  ";
			}
			lines.add(line);
		}
		
		lines.add("");
		lines.add("[end]  # section end");
		lines.add("");
		
		
		lines.add("# PCTLive");
		lines.add("# Proportion of Biomass that is actually living (0-1) for each month for each RGroup");
		lines.add("# This table is used to calculate the %Live for SoilWat. The %Live for each VegType group is the weighted average of the Groups in the vegtype by");
		lines.add("# their fraction of biomass. For example bouteloua, p.graminoids, and a.gram/shrub are in the same group. If they had biomass 150,50,10 respectively.");
		lines.add("# July the Grass %Live value would be .40 * (150/210) + .40 * (50/210) + .40 * (10/210) = .40");
		lines.add("# ");
		lines.add("# First column is the group name as defined in the ");
		lines.add("# group parameters file for STEPPE.  The name must be");
		lines.add("# spelled exactly the same.");
		lines.add("#");
		lines.add("# Group" + getSpacing(maxStringLength-7) + "Jan   Feb   Mar   Apr   May   Jun   Jul   Aug   Sep    Oct   Nov   Dec");
		for(GrpProdMonthlyValues gli : pctlive) {
			String line = gli.grpName + getSpacing(maxStringLength-gli.grpName.length());
			for(int i=0; i<gli.monthlyValues.length; i++) {
				line += String.format("%-4.3f", gli.monthlyValues[i])+"  ";
			}
			lines.add(line);
		}
		
		lines.add("");
		lines.add("[end]  # section end");
		
		java.nio.file.Files.write(file, lines, StandardCharsets.UTF_8);
	}
	
	private int getMaxStringLength() {
		List<Integer> lengths = new ArrayList<Integer>();
		for(GrpProdMonthlyValues gli : bmass) {
			lengths.add(gli.grpName.length());
		}
		for(GrpProdMonthlyValues gli : pctlive) {
			lengths.add(gli.grpName.length());
		}
		return Collections.max(lengths);
	}
	
	public boolean verify(Rgroup grps) throws Exception {
		LogFileIn f = stepwat.LogFileIn.getInstance();
		for(GrpProdMonthlyValues gli : bmass) {
			if(grps.ResourceParams_Name2Index(gli.grpName) == -1) {
				f.LogError(LogFileIn.LogMode.NOTE, "sxwprod.in verify bmass values : " + gli.grpName + " not found in Resource Groups.");
				return false;
			}
		}
		if(grps.groups.size() != bmass.size()) {
			f.LogError(LogFileIn.LogMode.NOTE, "sxwroots.in verify bmass values: To few or too many resource groups.");
			return false;
		}
		
		for(GrpProdMonthlyValues gli : pctlive) {
			if(grps.ResourceParams_Name2Index(gli.grpName) == -1) {
				f.LogError(LogFileIn.LogMode.NOTE, "sxwprod.in verify pctlive values : " + gli.grpName + " not found in Resource Groups.");
				return false;
			}
		}
		if(grps.groups.size() != pctlive.size()) {
			f.LogError(LogFileIn.LogMode.NOTE, "sxwroots.in verify pctlive values: To few or too many resource groups.");
			return false;
		}
		
		return true;
	}
	
	private String getSpacing(int length) {
		return new String(new char[length]).replace("\0", " ");
	}
}
