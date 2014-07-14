package stepwat.input.grid;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import stepwat.LogFileIn;
import stepwat.input.Input;

public class SeedDispersal extends Input {
	// CONSTANTS
	// from the max distance function: MAXD = (H * VW) / VT. MAXD refers to the
	// maximum distance that a seed can be dispersed to. If you're MAXD ends up
	// being really small, then you can get kinda weird results, so be careful.
	/**
	 * the average release height of the inflorescences (30 cm in the paper)
	 */
	public float H;
	/**
	 * the mean horizontal wind speed (500 cm/sec in the paper)
	 */
	public float VW;
	/**
	 * the average sinking velocity of the seeds (100 cm/sec in the paper)
	 */
	public float VT;

	// SEED DISPERSAL OUTPUTS
	/**
	 * output seed dispersal (ie. the total probability that a cell received <br>
	 * seeds for each cell for each year for each species)? 1 means yes, 0 means<br>
	 * no
	 */
	public boolean outputSeedDispersal;
	/**
	 * output header (ie. the names of all the species)? 1 means yes, 0 means<br>
	 * no. Suggested: yes, as it's hard to figure out what all the numbers mean<br>
	 * elsewise.
	 */
	public boolean outputHeader;
	/**
	 * output separator... specify one of the following as a separator: t = tab,<br>
	 * s = space, or any single character such as , | : etc (excluding '#','t',<br>
	 * and 's'). Suggested: tab.
	 */
	public String sep;

	// SPINUP AND SEED AVAILABILITY OPTIONS
	/**
	 * option 1) number of years N at the beginning of the simulation during<br>
	 * which seeds are available for germination. Input: N years.
	 */
	public int nYearsSeedsAvailable;
	/**
	 * option 1a) all cells and species. 1 means yes, 0 means no.
	 */
	public boolean option1a;
	/**
	 * option 1b) input for each cell: seeds of which species are available<br>
	 * during those first N years [names & order of all species in species.in].<br>
	 * 1 means yes, 0 means no.
	 */
	public boolean option1b;
	// option 2) run one site without dispersal (ie, conditions of 1-site
	// StepWat, ie, seeds available in every cell for every year) for inputted #
	// of years x replications (eg, 500 years times 10 replications) as spinup
	/**
	 * option 2a) use this spinup as init condition in all cells. 1 means yes, 0<br>
	 * means no.
	 */
	public boolean option2a;
	/**
	 * option 2b) input for each cell whether or not to use this spinup as<br>
	 * inits. 1 means yes, 0 means no.
	 */
	public boolean option2b;

	public void read(Path SeedDispersalPath) throws Exception {
		List<String> lines = java.nio.file.Files.readAllLines(
				SeedDispersalPath, StandardCharsets.UTF_8);
		LogFileIn f = stepwat.LogFileIn.getInstance();
		int nFileItemsRead = 0;
		for (String line : lines) {
			// Skip Comments and empty lines
			if (!line.matches("^\\s*#.*") && !line.matches("^[\\s]*$")) {
				line = line.trim();
				String[] values = line.split("#")[0].split("[ \t]+");// Remove comment after data
				switch (nFileItemsRead) {
				case 0:
					if (values.length != 1)
						f.LogError(LogFileIn.LogMode.ERROR, "GRID seed_dispersal.in : VW only one value required.");
					try {
						this.VW = Float.parseFloat(values[0]);
					} catch (NumberFormatException e) {
						f.LogError(LogFileIn.LogMode.ERROR, "GRID seed_dispersal.in read : VH Value : Could not convert string to float. " + e.getMessage());
					}
					break;
				case 1:
					if (values.length != 1)
						f.LogError(LogFileIn.LogMode.ERROR, "GRID seed_dispersal.in : output seed dispersal only requires a 0 or 1.");
					try {
						this.outputSeedDispersal = Integer.parseInt(values[0]) > 0 ? true : false;
					} catch (NumberFormatException e) {
						f.LogError(LogFileIn.LogMode.ERROR, "GRID seed_dispersal.in read : output seed dispersal Value : Could not convert string to integer. " + e.getMessage());
					}
					break;
				case 2:
					if (values.length != 1)
						f.LogError(LogFileIn.LogMode.ERROR, "GRID seed_dispersal.in : output header only requires a 0 or 1.");
					try {
						this.outputHeader = Integer.parseInt(values[0]) > 0 ? true : false;
					} catch (NumberFormatException e) {
						f.LogError(LogFileIn.LogMode.ERROR, "GRID seed_dispersal.in Read : output header Value : Could not convert string to integer. " + e.getMessage());
					}
					break;
				case 3:
					if (values.length != 1)
						f.LogError(LogFileIn.LogMode.ERROR, "GRID seed_dispersal.in : sep requires t or s or single charactor");
					this.sep = values[0];
					break;
				case 4:
					if (values.length != 1)
						f.LogError(LogFileIn.LogMode.ERROR, "GRID seed_dispersal.in : nYearsSeedsAvailable needs one integer.");
					try {
						this.nYearsSeedsAvailable = Integer.parseInt(values[0]);
					} catch (NumberFormatException e) {
						f.LogError(LogFileIn.LogMode.ERROR,"GRID seed_dispersal.in Read : nYearsSeedsAvailable Value : Could not convert string to integer. "+ e.getMessage());
					}
					break;
				case 5:
					if (values.length != 1)
						f.LogError(LogFileIn.LogMode.ERROR, "GRID seed_dispersal.in : species.in name has a space.");
					try {
						this.option1a = Integer.parseInt(values[0]) > 0 ? true : false;
					} catch (NumberFormatException e) {
						f.LogError(LogFileIn.LogMode.ERROR,"GRID seed_dispersal.in Read : option1a Value : Could not convert string to integer. " + e.getMessage());
					}
					break;
				case 6:
					if (values.length != 1)
						f.LogError(LogFileIn.LogMode.ERROR, "GRID seed_dispersal.in : bmassflags.in name has a space.");
					try {
						this.option1b = Integer.parseInt(values[0]) > 0 ? true:false;
					} catch (NumberFormatException e) {
						f.LogError(LogFileIn.LogMode.ERROR,"GRID seed_dispersal.in Read : option1b Value : Could not convert string to integer. " + e.getMessage());
					}
					break;
				case 7:
					if (values.length != 1)
						f.LogError(LogFileIn.LogMode.ERROR,"GRID seed_dispersal.in : bmasspre.in name has a space.");
					try {
						this.option2a = Integer.parseInt(values[0]) > 0?true:false;
					} catch (NumberFormatException e) {
						f.LogError(LogFileIn.LogMode.ERROR,"GRID seed_dispersal.in Read : option2a Value : Could not convert string to integer. " + e.getMessage());
					}
					break;
				case 8:
					if (values.length != 1)
						f.LogError(LogFileIn.LogMode.ERROR,"GRID seed_dispersal.in : bmassavg.in has a space.");
					try {
						this.option2b = Integer.parseInt(values[0]) > 0?true:false;
					} catch (NumberFormatException e) {
						f.LogError(LogFileIn.LogMode.ERROR,"GRID seed_dispersal.in Read : option2b Value : Could not convert string to integer. " + e.getMessage());
					}
					break;
				default:
					f.LogError(LogFileIn.LogMode.ERROR,"Grid seed_dispersal.in : unkown line.");
					break;
				}
				nFileItemsRead++;
			}
		}
		this.data = true;
	}

	public void write(Path SeedDispersalPath) throws IOException {
		List<String> lines = new ArrayList<String>();
		lines.add("# Seed dispersal setup for STEPWAT grid version - DLM 07-29-13.  This file contains constants that are used in seed dispersal.  The rest of the seed dispersal inputs are set up on a species basis in the species input file.");
		lines.add("");
		lines.add("#CONSTANTS from the max distance function: MAXD = (H * VW) / VT.  MAXD refers to the maximum distance that a seed can be dispersed to.  If you're MAXD ends up being really small, then you can get kinda weird results, so be careful.");
		lines.add("#30.0		# H - the average release height of the inflorescences (30 cm in the paper)");
		lines.add(String.format("%-06.4f", this.VW)+"\t\t# VW - the mean horizontal wind speed (500 cm/sec in the paper)");
		lines.add("#100.0		# VT - the average sinking velocity of the seeds (100 cm/sec in the paper)");
		lines.add("\n#SEED DISPERSAL OUTPUTS");
		lines.add(String.valueOf(this.nYearsSeedsAvailable)+"\t\t# output seed dispersal (ie. the total probability that a cell received seeds for each cell for each year for each species)?  1 means yes, 0 means no");
		lines.add(String.valueOf(this.option1a?1:0)+"\t\t# output header (ie. the names of all the species)?  1 means yes, 0 means no.  Suggested: yes, as it's hard to figure out what all the numbers mean elsewise.");
		lines.add(String.valueOf(this.option1b?1:0)+"\t\t# option 1b) input for each cell: seeds of which species are available during those first N years [names & order of all species in species.in].  1 means yes, 0 means no.");
		lines.add(String.valueOf(this.option2a?1:0)+"\t\t# option 2a) use this spinup as init condition in all cells.  1 means yes, 0 means no.");
		lines.add(String.valueOf(this.option2b?1:0)+"\t\t# option 2b) input for each cell whether or not to use this spinup as inits.  1 means yes, 0 means no.VGH");
		Files.write(SeedDispersalPath, lines, StandardCharsets.UTF_8);
	}
	
	public boolean verify() throws Exception {
		LogFileIn f = stepwat.LogFileIn.getInstance();
		if( (option1a && (option1b || option2a || option2b)) || (option2a && (option1a || option1b || option2b)) ) {
			f.LogError(LogFileIn.LogMode.WARN,"Grid seed_dispersal.in : unkown line.");
			return false;
		}
		return true;
	}
}
