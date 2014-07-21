package stepwat.input.ST;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import stepwat.LogFileIn;
import stepwat.input.Input;

public class Environment extends Input {
	static final String[] Comments = {"# Environment input file for STEPPEWAT\n\n"+
			"# Anything after the first pound sign is a comment\n"+
			"# Blank lines, white space, and comments may be used freely,\n"+
			"# however, the order of input is important\n\n"+
			"# -------------------------------------------------------------\n"+
			"# precipitation: truncated normal distribution.  To force ppt\n"+
			"# to be a single value, set avg, min and max to that value.\n"+
			"#\n"+
			"# avg = (mm)\n"+
			"# std = (mm) standard deviation\n"+
			"# min = truncate to this minimum\n"+
			"# max = truncate to this maximum\n"+
			"# dry = ppt <= this is treated as a dry year (eqn 3)\n"+
			"# wet = ppt >= this is treated as a wet year (eqn 4)\n"+
			"#       anything between 'dry' and 'wet' is considered average\n"+
			"# gsppt = proportion of ppt occuring during growing season\n"+
			"\n"+
			"# avg    std  min max dry wet gsppt",
			"\n\n\n"+
			"# -------------------------------------------------------------\n"+
			"# temperature: truncated normal distribution. To force temp\n"+
			"# to be a single value, set avg and std to that value.\n"+
			"#\n"+
			"# avg = (centigrade)\n"+
			"# std = (centigrade) standard deviation\n"+
			"# max = maximum truncate value\n"+
			"# min = minimum truncate value\n"+
			"\n"+
			"# avg  std  min  max",
			"\n# -------------------------------------------------------------\n"+
			"# disturbance probabilities = probability of occurrance per year\n"+
			"\n"+
			"# to disallow a disturbance, set its probability to 0.0\n"+
			"\n"+
			"# -------------------------------------------------------------\n"+
			"# These are the parameters associated with fecal pats\n"+
			"# use == use (1) / don't use (0) this disturbance\n"+
			"# occur == probability of occurrance\n"+
			"# removal = prob that the pat will decompose or be removed in yr 1.\n"+
			"# slope = slope for eqn 17 for time to recolonization\n"+
			"# intcpt = intercept for eqn 17 for time to recolonization\n"+
			"\n"+
			"# use  occur removal  slope   intcpt",
			"\n# -------------------------------------------------------------\n"+
			"# These are the parameters associated with ant mounds.\n"+
			"\n"+
			"# use == use (1) / don't use (0) this disturbance\n"+
			"# occur = probability of occurrance\n"+
			"# min = minimum number of years before plants can begin\n"+
			"#       reestablishment after the initiation of the ant mound.\n"+
			"# max = maximum number of years to exclude plants due to ant mound.\n"+
			"# The actual number chosen is a uniform random distribution.\n"+
			"\n"+
			"# use  occur  min  max",
			"\n# -------------------------------------------------------------\n"+
			"# These are the parameters associated with animal burrows.\n"+
			"#\n"+
			"#  In the C&L 1990 paper, plants can reestablish beginning\n"+
			"# immediately after this disturbance.  However, you can change\n"+
			"# that here if you like.\n"+
			"\n"+
			"# Immediate reestablishment potential is denoted by min=0 years.\n"+
			"\n"+
			"# use == use (1) / don't use (0) this disturbance\n"+
			"\n"+
			"# use  occur    min",
			"\n#=============================================================\n"+
			"# These are the parameters for the temperature-based growth modifiers\n"+
			"# for warm/cool -season plants. (See eqns 12 and 13)\n"+
			"#\n"+
			"# ctmpa = cool season additive factor\n"+
			"# ctmp1 = cool season slope for linear term\n"+
			"# ctmp2 = cool season slope for quadratic term\n"+
			"# wtmpa = warm season additive factor\n"+
			"# wtmp1 = warm season slope for linear term\n"+
			"# wtmp2 = warm season slope for quadratic term\n"+
			"#  ctmpa   ctmp1    ctmp2    wtmpa    wtmp1    wtmp2"};
	public class Precipitation {
		/***
		 * Units (mm)
		 */
		public float avg;
		/***
		 * Standard Deviation (mm)
		 */
		public float std;
		/***
		 * Truncate to this minimum
		 */
		public int min;
		/***
		 * Truncate to this maximum
		 */
		public int max;
		/***
		 * ppt <= this is treated as a dry year (eqn 3)
		 */
		public int dry;
		/***
		 * ppt >= this is treated as a wet year (eqn 4)
		 * anything between "dry" and "wet" is considered average
		 */
		public int wet;
		/***
		 * proportion of ppt occurring during growing season
		 */
		public float gsppt;
		
		public Precipitation(float avg, float std, int min, int max, int dry, int wet, float gsppt) {
			this.avg = avg;
			this.std = std;
			this.min = min;
			this.max = max;
			this.dry = dry;
			this.wet = wet;
			this.gsppt = gsppt;
		}
		
		public void setValues(float avg, float std, int min, int max, int dry, int wet, float gsppt) {
			this.avg = avg;
			this.std = std;
			this.min = min;
			this.max = max;
			this.dry = dry;
			this.wet = wet;
			this.gsppt = gsppt;
		}
		
		public String toString() {
			return String.format(" %-5.1f  %-4.1f  %-3d %-3d %-3d %-3d %-5.3f", avg, std, min, max, dry, wet, gsppt);
			//return " "+String.valueOf(avg)+"\t"+String.valueOf(std)+"\t"+String.valueOf(min)+"\t"+String.valueOf(max)+"\t"+
			//		String.valueOf(dry)+"\t"+String.valueOf(wet)+"\t"+String.valueOf(gsppt);
		}
	}
	
	public class Temperature {
		/***
		 * Average (Centigrade)
		 */
		public float avg;
		/***
		 * Standard Deviation (Centigrade)
		 */
		public float std;
		/***
		 * Truncate to this minimum
		 */
		public float min;
		/***
		 * Truncate to this maximum
		 */
		public float max;
		
		public Temperature(float avg, float std, float min, float max) {
			this.avg = avg;
			this.std = std;
			this.min = min;
			this.max = max;
		}
		
		public void setValues(float avg, float std, float min, float max) {
			this.avg = avg;
			this.std = std;
			this.min = min;
			this.max = max;
		}
		
		public String toString() {
			return String.format("%-5.2f %-4.2f %-3.1f %-3.1f", avg,std,min,max);
			//return " "+String.valueOf(avg)+"\t"+String.valueOf(std)+"\t"+String.valueOf(min)+"\t"+String.valueOf(max);
		}
	}
	
	//Disturbances
	
	/***
	 * These are the parameters associated with fecal pats.
	 * @author Ryan Murphy
	 *
	 */
	public class FecalPats {
		/***
		 * use == use (1) / don't use (0) this disturbance
		 */
		public boolean use;
		/***
		 * occur == probability of occurrence
		 */
		public float occur;
		/***
		 * removal = prob that the pat will decompose or be removed in yr 1.
		 */
		public float removal;
		/***
		 * slope = slope for eqn 17 for time to recolonization
		 */
		public float slope;
		/***
		 * intcpt = intercept for eqn 17 for time to recolonization
		 */
		public float intcpt;
		
		public FecalPats(boolean use, float occur, float removal, float slope, float intcpt) {
			this.use = use;
			this.occur = occur;
			this.removal = removal;
			this.slope = slope;
			this.intcpt = intcpt;
		}
		
		public void setValues(boolean use, float occur, float removal, float slope, float intcpt) {
			this.use = use;
			this.occur = occur;
			this.removal = removal;
			this.slope = slope;
			this.intcpt = intcpt;
		}
		
		public String toString() {
			return String.format("  %-3d  %-5.3f %-7.4f  %-5.4f   %-6.3f", use?1:0,occur,removal,slope,intcpt);
			//return " "+String.valueOf(use?1:0)+"\t"+String.valueOf(occur)+"\t"+String.valueOf(removal)+"\t"+String.valueOf(slope)+"\t"+
			//		String.valueOf(intcpt);
		}
	}
	
	/***
	 * This is the object that holds the parameters associated with ant mounds.
	 * @author Ryan J Murphy
	 *
	 */
	public class AntMounds {
		/***
		 * use (1) / don't use (0) this disturbance
		 */
		public boolean use;
		/***
		 * probability of occurrence
		 */
		public float occur;
		/***
		 * minimum number of years before plants can begin re-establishment after the initiation of the ant mound.
		 */
		public int minyr;
		/***
		 * maximum number of years to exclude plants due to ant mound. The actual number chosen is a uniform random distribution.
		 */
		public int maxyr;
		
		public AntMounds(boolean use, float occur, int minyr, int maxyr) {
			this.use = use;
			this.occur = occur;
			this.minyr = minyr;
			this.maxyr = maxyr;
		}
		
		public void setValues(boolean use, float occur, int minyr, int maxyr) {
			this.use = use;
			this.occur = occur;
			this.minyr = minyr;
			this.maxyr = maxyr;
		}
		
		public String toString() {
			return String.format("  %-3d  %-6.5f %-3d  %-3d", use?1:0, occur, minyr,maxyr);
			//return " "+String.valueOf(use)+"\t"+String.valueOf(occur)+"\t"+String.valueOf(minyr)+"\t"+String.valueOf(maxyr);
		}
	}
	
	/***
	 * This is the object that holds the parameters associated with ant burrows.<br>
	 * In the C&L 1990 paper, plants can reestablish beginning<br>
	 * immediately after this disturbance.  However, you can change<br>
	 * that here if you like.<br>
	 * @author Ryan J Murphy
	 *
	 */
	public class AntBurrows {
		/***
		 * use (1) / don't use (0) this disturbance
		 */
		public boolean use;
		/***
		 * probability of occurrence
		 */
		public float occur;
		/***
		 * Immediate re-establishment potential is denoted by min=0 years.
		 */
		public int minyr;
		
		public AntBurrows(boolean use, float occur, int minyr) {
			this.use = use;
			this.occur = occur;
			this.minyr = minyr;
		}
		
		public void setValues(boolean use, float occur, int minyr) {
			this.use = use;
			this.occur = occur;
			this.minyr = minyr;
		}
		
		public String toString() {
			return String.format("  %-3d %8.7f %-3d", use?1:0,occur,minyr);
			//return " "+String.valueOf(use)+"\t"+String.valueOf(occur)+"\t"+String.valueOf(minyr);
		}
	}
	
	/***
	 * This object holds the parameters for the temperature-based growth modifiers<br>
	 * for warm/cool -season plants. (See eqns 12 and 13)<br>
	 * 
	 * @author Ryan J Murphy
	 *
	 */
	public class GrowthModifiers {
		/**
		 * cool season additive factor
		 */
		public float ctmpa;
		/**
		 * cool season slope for linear term
		 */
		public float ctmp1;
		/**
		 * cool season slope for quadratic term
		 */
		public float ctmp2;
		/**
		 * warm season additive factor
		 */
		public float wtmpa;
		/**
		 * warm season slope for linear term
		 */
		public float wtmp1;
		/**
		 * warm season slope for quadratic term
		 */
		public float wtmp2;

		public GrowthModifiers(float ctmpa, float ctmp1, float ctmp2, float wtmpa, float wtmp1, float wtmp2) {
			this.ctmpa = ctmpa;
			this.ctmp1 = ctmp1;
			this.ctmp2 = ctmp2;
			this.wtmpa = wtmpa;
			this.wtmp1 = wtmp1;
			this.wtmp2 = wtmp2;
		}
		
		public void setValues(float ctmpa, float ctmp1, float ctmp2, float wtmpa, float wtmp1, float wtmp2) {
			this.ctmpa = ctmpa;
			this.ctmp1 = ctmp1;
			this.ctmp2 = ctmp2;
			this.wtmpa = wtmpa;
			this.wtmp1 = wtmp1;
			this.wtmp2 = wtmp2;
		}
		
		public String toString() {
			return String.format("  %-7.5f %-7.5f %-7.5f %-7.5f %-7.5f %-7.5f", ctmpa,ctmp1,ctmp2,wtmpa,wtmp1,wtmp2);
			//return " "+String.valueOf(ctmpa)+"\t"+String.valueOf(ctmp1)+"\t"+String.valueOf(ctmp2)+"\t"+String.valueOf(wtmpa)+"\t"+
			//		String.valueOf(wtmp1)+"\t"+String.valueOf(wtmp2);
		}
	}
	
	public Precipitation precip;
	public Temperature temp;
	public FecalPats fecalPats;
	public AntMounds antMounds;
	public AntBurrows antBurrows;
	public GrowthModifiers growthModifiers;
	
	public void read(Path EnvirInPath) throws Exception {
		List<String> lines = java.nio.file.Files.readAllLines(EnvirInPath, StandardCharsets.UTF_8);
		LogFileIn f = stepwat.LogFileIn.getInstance();
		int nFileItemsRead = 0;
		for (String line : lines) {
			// Skip Comments and empty lines
			if (!line.matches("^\\s*#.*") && !line.matches("^[\\s]*$")) {
				line = line.trim();
				String[] values = line.split("#")[0].split("[ \t]+");// Remove comment  after data
				switch (nFileItemsRead) {
				case 0:
					if(values.length != 7)
						f.LogError(LogFileIn.LogMode.ERROR, "Environment.in read : Expected 7 values for Precipitation.");
					try {
						float avg = Float.parseFloat(values[0]);
						float std = Float.parseFloat(values[1]);
						int min = Integer.parseInt(values[2]);
						int max = Integer.parseInt(values[3]);
						int dry = Integer.parseInt(values[4]);
						int wet = Integer.parseInt(values[5]);
						float gsppt = Float.parseFloat(values[6]);
						this.precip = new Precipitation(avg, std, min, max, dry, wet, gsppt);
					} catch(NumberFormatException e) {
						f.LogError(LogFileIn.LogMode.ERROR, "Environment.in read : Precip Could not convert to number.");
					}
					break;
				case 1:
					if(values.length != 4)
						f.LogError(LogFileIn.LogMode.ERROR, "Environment.in read : Expected 4 values for Temp.");
					try {
						float avg = Float.parseFloat(values[0]);
						float std = Float.parseFloat(values[1]);
						float min = Float.parseFloat(values[2]);
						float max = Float.parseFloat(values[3]);
						this.temp = new Temperature(avg, std, min, max);
					} catch(NumberFormatException e) {
						f.LogError(LogFileIn.LogMode.ERROR, "Environment.in read : Temp Could not convert to number.");
					}
					break;
				case 2:
					if(values.length != 5)
						f.LogError(LogFileIn.LogMode.ERROR, "Environment.in read : Expected 5 values for FecalPats.");
					try {
						boolean use = Integer.parseInt(values[0])>0 ? true : false;
						float occur = Float.parseFloat(values[1]);
						float removal = Float.parseFloat(values[2]);
						float slope = Float.parseFloat(values[3]);
						float intcpt = Float.parseFloat(values[4]);
						this.fecalPats = new FecalPats(use, occur, removal, slope, intcpt);
					} catch(NumberFormatException e) {
						f.LogError(LogFileIn.LogMode.ERROR, "Environment.in read : Fecal Pats Could not convert to number.");
					}
					break;
				case 3:
					if(values.length != 4)
						f.LogError(LogFileIn.LogMode.ERROR, "Environment.in read : Expected 4 values for Ant Mounds.");
					try {
						boolean use = Integer.parseInt(values[0])>0 ? true : false;
						float occur = Float.parseFloat(values[1]);
						int min = Integer.parseInt(values[2]);
						int max = Integer.parseInt(values[3]);
						this.antMounds = new AntMounds(use, occur, min, max);
					} catch(NumberFormatException e) {
						f.LogError(LogFileIn.LogMode.ERROR, "Environment.in read : Ant Mounds Could not convert to number.");
					}
					break;
				case 4:
					if(values.length != 3)
						f.LogError(LogFileIn.LogMode.ERROR, "Environment.in read : Expected 3 values for Ant Burrows.");
					try {
						boolean use = Integer.parseInt(values[0])>0 ? true : false;
						float occur = Float.parseFloat(values[1]);
						int min = Integer.parseInt(values[2]);
						this.antBurrows = new AntBurrows(use, occur, min);
					} catch(NumberFormatException e) {
						f.LogError(LogFileIn.LogMode.ERROR, "Environment.in read : Ant Burrows Could not convert to number.");
					}
					break;
				case 5:
					if(values.length != 6)
						f.LogError(LogFileIn.LogMode.ERROR, "Environment.in read : Expected 6 values for growth modifiers.");
					try {
						float ctmpa = Float.parseFloat(values[0]);
						float ctmp1 = Float.parseFloat(values[1]);
						float ctmp2 = Float.parseFloat(values[2]);
						float wtmpa = Float.parseFloat(values[3]);
						float wtmp1 = Float.parseFloat(values[4]);
						float wtmp2 = Float.parseFloat(values[5]);
						this.growthModifiers = new GrowthModifiers(ctmpa, ctmp1, ctmp2, wtmpa, wtmp1, wtmp2);
					} catch(NumberFormatException e) {
						f.LogError(LogFileIn.LogMode.ERROR, "Environment.in read : growth modifiers Could not convert to number.");
					}
					break;
				default:
					f.LogError(LogFileIn.LogMode.ERROR, "Environment.in : unkown line.");
					break;
				}
				nFileItemsRead++;
			}
		}
		this.data = true;
	}
	
	public void write(Path EnvirInPath) throws IOException {
		List<String> lines = new ArrayList<String>();
		lines.add(Comments[0]);
		lines.add(this.precip.toString());
		lines.add(Comments[1]);
		lines.add(this.temp.toString());
		lines.add(Comments[2]);
		lines.add(this.fecalPats.toString());
		lines.add(Comments[3]);
		lines.add(this.antMounds.toString());
		lines.add(Comments[4]);
		lines.add(this.antBurrows.toString());
		lines.add(Comments[5]);
		lines.add(this.growthModifiers.toString());
		java.nio.file.Files.write(EnvirInPath, lines, StandardCharsets.UTF_8);
	}	
}
