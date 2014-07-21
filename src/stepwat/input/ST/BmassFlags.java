package stepwat.input.ST;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import stepwat.LogFileIn;
import stepwat.input.Input;

public class BmassFlags extends Input {
	public static final String[] Comments = {
			"# Bmassflags input definition file STEPPEWAT\n"
					+ "# to help control the output quantities etc.\n"
					+ "\n"
					+ "# Anything after the first pound sign is a comment\n"
					+ "# Blank lines, white space, and comments may be used freely,\n"
					+ "# however, the order of input is important\n" + "\n"
					+ "####################################################\n"
					+ "#\n" + "#",
			"#===================================================\n"
					+ "# The input line is a set of yes/no flags that define\n"
					+ "# whether that parameter will be output, except for\n"
					+ "# the separator character.  For the flags, 'y' or 'yes'\n"
					+ "# (upper or lower case) indicates true. Anything else\n"
					+ "# (other than nothing) indicates false; ie, there must\n"
					+ "# be at least a place holder.\n"
					+ "#\n"
					+ "# Sumry == y if biomass to be output, ==n if biomass not to be output\n"
					+ "# Yearly == if 'n', suppress printing yearly results.\n"
					+ "#          important if you've specified many iterations.\n"
					+ "#          if 'y', each output file contains a full run\n"
					+ "#          of the model with the iteration number as part\n"
					+ "#          of the filename.\n"
					+ "# Header == output a line of field names\n"
					+ "# Sep == specify one of the following as a separator\n"
					+ "#       t = tab, s = space, or any single character\n"
					+ "#       such as , | : etc (excluding '#','t', and 's').\n"
					+ "# YrNum == year\n"
					+ "# Disturb == yearly: current disturbance for this year\n"
					+ "#         summary:  number of years with disturbances,\n"
					+ "#                 over all runs in this year\n"
					+ "# PPT ==  yearly: precip for this year\n"
					+ "#         summary:  average ppt for this year over all runs\n"
					+ "# PClass = yearly: precip class (wet/dry/normal) for this year\n"
					+ "#          summary:  'NA', field is output because the same\n"
					+ "#                  header line is used for yearlies and stats\n"
					+ "# Temp == yearly: temperature for this year\n"
					+ "#        summary:  average temp over all runs for this year\n"
					+ "# GrpBmass == biomass for each resource group number, one\n"
					+ "#         field for each group.  The resource group\n"
					+ "#         number is defined by it's order of appearance\n"
					+ "#         in the group parameter definition file. If\n"
					+ "#         'head' is to be printed, the name of the\n"
					+ "#         resource group is output as the field name.\n"
					+ "#         For summary, this is the average value for a given\n"
					+ "#         year over all runs.\n"
					+ "# GrpPR == proportion of resources available to the group.\n"
					+ "#       Each group's pr value is reported immediately\n"
					+ "#       to the right of the group biomass.\n"
					+ "#       summary:  average for the given year.  Can only be\n"
					+ "#       output if GrpBmass is specified.\n"
					+ "# GrpSize == relative size ('equivalent mature individuals')\n"
					+ "#       of the group.  Each group's relative size is reported\n"
					+ "#       immediately to the right of the group biomass, unless\n"
					+ "#       pr (above) is turned on.\n"
					+ "#       summary:  average for the given year.  Can only be\n"
					+ "#       output if GrpBmass is specified.\n"
					+ "# SppBmass == net biomass for each species for the year.\n"
					+ "#         If 'head' is true, species names are output as\n"
					+ "#         the field names.\n"
					+ "#         summary: average value for all runs.\n"
					+ "# Indivs == yearly: net number of individuals of this species\n"
					+ "#                 this year.\n"
					+ "#         summary: average number of individuals for this year.\n"
					+ "#                Can only be output if SppBmass specified."};
	/**
	 * y if biomass to be output, ==n if biomass not to be output
	 */
	public boolean sumry;
	/**
	 * if 'n', suppress printing yearly results.<br>
	 * important if you've specified many iterations.<br>
	 * if 'y', each output file contains a full run<br>
	 * of the model with the iteration number as part<br>
	 * of the filename.
	 */
	public boolean yearly;
	/**
	 * output a line of field names
	 */
	public boolean header;
	/**
	 * specify one of the following as a separator<br>
	 * t = tab, s = space, or any single character<br>
	 * such as , | : etc (excluding '#','t', and 's').<br>
	 */
	public String sep;
	/**
	 * year
	 */
	public boolean yrnum;
	/**
	 * yearly: current disturbance for this year<br>
	 * summary:  number of years with disturbances<br>
	 * over all runs in this year
	 */
	public boolean disturb;
	/**
	 * yearly: precip for this year<br>
	 * summary:  average ppt for this year over all runs
	 */
	public boolean ppt;
	/**
	 * yearly: precip class (wet/dry/normal) for this year<br>
	 * summary:  'NA', field is output because the same<br>
	 * header line is used for yearlies and stats
	 */
	public boolean pclass;
	/**
	 * yearly: temperature for this year<br>
	 * summary:  average temp over all runs for this year
	 */
	public boolean temp;
	/**
	 * biomass for each resource group number, one<br>
	 * field for each group.  The resource group<br>
	 * number is defined by it's order of appearance<br>
	 * in the group parameter definition file. If<br>
	 * 'head' is to be printed, the name of the<br>
	 * resource group is output as the field name.<br>
	 * For summary, this is the average value for a given<br>
	 * year over all runs.
	 */
	public boolean grpBmass;
	/**
	 * proportion of resources available to the group.<br>
	 * Each group's pr value is reported immediately<br>
	 * to the right of the group biomass.<br>
	 * summary:  average for the given year. Can only be<br>
	 * output if GrpBmass is specified.
	 */
	public boolean grpPR;
	/**
	 * relative size ('equivalent mature individuals')<br>
	 * of the group.  Each group's relative size is reported<br>
	 * immediately to the right of the group biomass, unless<br>
	 * pr (above) is turned on.<br>
	 * summary:  average for the given year.  Can only be<br>
	 * output if GrpBmass is specified.<br>
	 */
	public boolean grpSize;
	/**
	 * net biomass for each species for the year.<br>
	 * If 'head' is true, species names are output as<br>
	 * the field names.<br>
	 * summary: average value for all runs.
	 */
	public boolean sppBmass;
	/**
	 * yearly: net number of individuals of this species this year.<br>
	 * summary: average number of individuals for this year can only be output if SppBmass specified.
	 */
	public boolean indivs;
	
	public void read(Path BmassFlagsFile) throws Exception {
		List<String> lines = java.nio.file.Files.readAllLines(BmassFlagsFile, StandardCharsets.UTF_8);
		LogFileIn f = stepwat.LogFileIn.getInstance();
		int nFileItemsRead = 0;

		for (String line : lines) {
			// Skip Comments and empty lines
			if (!line.matches("^\\s*#.*") && !line.matches("^[\\s]*$")) {
				line = line.trim();
				String[] values = line.split("#")[0].split("[ \t]+");
				switch (nFileItemsRead) {
				case 0:
					if(values.length != 14)
						f.LogError(LogFileIn.LogMode.ERROR, "BmassFlags.in read : Expected 14 value.");
					sumry = values[0].compareToIgnoreCase("y")==0?true:false;
					yearly = values[1].compareToIgnoreCase("y")==0?true:false;
					header = values[2].compareToIgnoreCase("y")==0?true:false;
					sep = values[3];
					yrnum = values[4].compareToIgnoreCase("y")==0?true:false;
					disturb = values[5].compareToIgnoreCase("y")==0?true:false;
					ppt = values[6].compareToIgnoreCase("y")==0?true:false;
					pclass = values[7].compareToIgnoreCase("y")==0?true:false;
					temp = values[8].compareToIgnoreCase("y")==0?true:false;
					grpBmass = values[9].compareToIgnoreCase("y")==0?true:false;
					grpPR = values[10].compareToIgnoreCase("y")==0?true:false;
					grpSize = values[11].compareToIgnoreCase("y")==0?true:false;
					sppBmass = values[12].compareToIgnoreCase("y")==0?true:false;
					indivs = values[13].compareToIgnoreCase("y")==0?true:false;
					break;
				default:
					f.LogError(LogFileIn.LogMode.ERROR, "BmassFlags.in read : Unkown Line.");
					break;
				}
				nFileItemsRead++;
			}
		}
		this.data = true;
	}
	
	public void write(Path SpeciesInPath) throws IOException {
		List<String> lines = new ArrayList<String>();
		lines.add(Comments[0]);
		lines.add("# Sumry Yearly Header Sep YrNum Disturb PPT PClass Temp GrpBmass GrpPR GrpSize SppBmass Indivs");
		lines.add("  "+ds(sumry?"y":"n",5)+ds(yearly?"y":"n",6)+ds(header?"y":"n",6)+ds(sep,3)+ds(yrnum?"y":"n",5)+ds(disturb?"y":"n",7)+ds(ppt?"y":"n",3)
				+ds(pclass?"y":"n",6)+ds(temp?"y":"n",4)+ds(grpBmass?"y":"n",8)+ds(grpPR?"y":"n",5)+ds(grpSize?"y":"n",7)+ds(sppBmass?"y":"n",8)+ds(indivs?"y":"n",6));
		lines.add(Comments[1]);
		java.nio.file.Files.write(SpeciesInPath, lines, StandardCharsets.UTF_8);
	}
	private String ds(String t, int width) {
		return String.format("%-"+String.valueOf(width)+"s", t)+" ";
	}
}
