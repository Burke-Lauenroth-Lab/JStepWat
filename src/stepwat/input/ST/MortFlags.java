package stepwat.input.ST;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import stepwat.LogFileIn;
import stepwat.input.Input;

public class MortFlags extends Input {
	public static final String[] Comments = {
			"# Mortflags input definition file STEPPEWAT\n"
					+ "# to control output quantities etc.\n"
					+ "\n"
					+ "# Anything after the first pound sign is a comment\n"
					+ "# Blank lines, white space, and comments may be used freely,\n"
					+ "# however, the order of input is important\n" + "\n",
			"\n\n#===================================================\n"
					+ "# The input line is a set of yes/no flags that define\n"
					+ "# whether that parameter will be output, except for\n"
					+ "# the separator character.  For the flags, 'y' or 'yes'\n"
					+ "# (upper or lower case) indicates true. Anything else\n"
					+ "# (other than nothing) indicates false; ie, there must\n"
					+ "# be at least a place holder.\n"
					+ "#\n"
					+ "# sumry == y if mortality data summary to be output, \n"
					+ "#     == n if mortality data summary not to be output. If 'n' then\n"
					+ "#     the rest of the flags are not processed.\n"
					+ "#      \n"
					+ "# yearly == if 'n', suppress printing yearly results.\n"
					+ "#          important if you've specified many iterations.\n"
					+ "#          if 'y', each output file contains a full run\n"
					+ "#          of the model with the iteration number as part\n"
					+ "#          of the filename.\n"
					+ "# head == output a line of field names as the first line of\n"
					+ "#      the output file.  Affects both yearly and summary output.\n"
					+ "# sep == specify one of the following as a separator\n"
					+ "#       t = tab, s = space, or any single character\n"
					+ "#       such as , | : etc (excluding '#','t', and 's').\n"
					+ "#\n"
					+ "# group == y if group level output desired\n"
					+ "#\n"
					+ "# species == y if species-level output desired\n"
					+ "#\n"
					+ "#===================================================================\n"
					+ "# Suggested future output.  Categories of output are written to\n"
					+ "# separate files. Preceed each entry with a hash mark # to turn it\n"
					+ "# off.  Items in a group without a filename are written as a separate\n"
					+ "# column in the last-specified file.\n"
					+ "#\n"
					+ "# all output is one or two columns preceeded by Iter, Year, Spp\n"
					+ "#\n"
					+ "# Estab(1) - number of indivs of spp established this year\n"
					+ "\n"
					+ "#ESTAB estabs/estabs.out  # number indivs of spp estab this year\n"
					+ "#MortAll   mort/kills.out\n"
					+ "#MortIntrin    mort/intrin.out #\n"
					+ "#MortNoRes     #output goes in intrin.out if none specified here\n"
					+ "#Mort" };
	/**
	 * y if mortality data summary to be output,<br>
	 * n if mortality data summary not to be output. If 'n' then the rest of the flags are not processed.
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
	 * output a line of field names as the first line of<br>
	 * the output file.  Affects both yearly and summary output.
	 */
	public boolean header;
	/**
	 * specify one of the following as a separator<br>
	 * t = tab, s = space, or any single character<br>
	 * such as , | : etc (excluding '#','t', and 's').
	 */
	public String sep;
	/**
	 * y if group level output desired
	 */
	public boolean group;
	/**
	 * y if species-level output desired
	 */
	public boolean species;
	
	public void read(Path MortFlagsFile) throws Exception {
		List<String> lines = java.nio.file.Files.readAllLines(MortFlagsFile, StandardCharsets.UTF_8);
		LogFileIn f = stepwat.LogFileIn.getInstance();
		int nFileItemsRead = 0;

		for (String line : lines) {
			// Skip Comments and empty lines
			if (!line.matches("^\\s*#.*") && !line.matches("^[\\s]*$")) {
				line = line.trim();
				String[] values = line.split("#")[0].split("[ \t]+");
				switch (nFileItemsRead) {
				case 0:
					if(values.length != 6)
						f.LogError(LogFileIn.LogMode.ERROR, "MortFlags.in read : Expected 6 value.");
					sumry = values[0]=="y"?true:false;
					yearly = values[1]=="y"?true:false;
					header = values[2]=="y"?true:false;
					sep = values[3];
					group = values[4]=="y"?true:false;
					species = values[5]=="y"?true:false;
					break;
				default:
					f.LogError(LogFileIn.LogMode.ERROR, "MortFlags.in read : Unkown Line.");
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
		lines.add((sumry?"y":"n")+"\t"+(yearly?"y":"n")+"\t"+(header?"y":"n")+"\t"+sep+"\t"+(group?"y":"n")+"\t"+(species?"y":"n"));
		lines.add(Comments[1]);
		java.nio.file.Files.write(SpeciesInPath, lines, StandardCharsets.UTF_8);
	}
}
