package stepwat.input.ST;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import stepwat.LogFileIn;
import stepwat.input.Input;

public class Species extends Input {
	public static final String[] Comments = {
			"# Species input definition file STEPPEWAT\n"
					+ "\n"
					+ "# Anything after the first pound sign is a comment\n"
					+ "# Blank lines, white space, and comments may be used freely,\n"
					+ "# however, the order of input is important\n"
					+ "\n"
					+ "# name = 4 char genus/species name\n"
					+ "# rg = number of resource group to which this species belongs,\n"
					+ "#      based on the order they were entered in the rgroups input file,\n"
					+ "#      which is read before this file.  Note that a group cannot\n"
					+ "#      contain a mix of annuals and perennials, although there can \n"
					+ "#      be more than one group for annuals, for example, one for\n"
					+ "#      invasives and one for natives.\n"
					+ "# age = maximum age in years, 0 = no limit\n"
					+ "#       because a group can be only annuals or only perennials,\n"
					+ "#       a 1 here implies this species belongs to an annual group.\n"
					+ "# irate = intrinsic growth rate (column 8, Table 2, C&L 1990)\n"
					+ "#         for annuals, this is the proportion of maximum biomass\n"
					+ "#         attainable with average resource availability, that is\n"
					+ "#         reported biomass = maxbio * irate / PR\n"
					+ "# ratep = prop of intrisic growth rate to use as maximum growth rate\n"
					+ "#         not used for annuals.\n"
					+ "# slow  = number of years of slow growth before mortality begins\n"
					+ "#         not used for annuals.\n"
					+ "# disturb  = disturbance class:\n"
					+ "#         1 = very sensitive to fecal pats and ant mounds\n"
					+ "#         2 = sensitive to pats, very sensitive to mounds\n"
					+ "#         3 = insensitive to pats, sensitive to mounds\n"
					+ "#         4 = insensitive to pats and mounds\n"
					+ "# pestab = seedling establishment probability for a given year\n"
					+ "#          for annuals, this is the probability that propagules\n"
					+ "#          will reach the plot in a given year.  Can be 1.0 for \n"
					+ "#          native species, or < 1 for invasives.\n"
					+ "# eind  = maximum number of individuals that can establish in a year\n"
					+ "#         for annuals, max number of viable seeds that will grow to maturity\n"
					+ "#         on the plot. ie, fecundity.\n"
					+ "# minbio = biomass of an established seedling in grams\n"
					+ "#         meaningless for annuals.\n"
					+ "# maxbio = biomass of a mature individual in grams\n"
					+ "#         for annuals, see also irate definition.\n"
					+ "# clonal = can reproduce vegetatively (yes/no)\n"
					+ "#         placeholder required but ignored for annuals.\n"
					+ "# vegindv = number of seedling-sized units to add if vegprop occurs\n"
					+ "# tclass = temperature class (1=C4, 2=C3, 0=not applicable)\n"
					+ "# cosurv = proportion of a cohort surviving to maturity (0.0-1.0)\n"
					+ "#         meaningless for annuals.\n"
					+ "# onoff = turn on/off this species from the model. 1=use, 0=don't use.\n"
					+ "#      see also the same flag in group parameters file.\n"
					+ "# dispersal = turn on/off seed dispersal for this species. 1=use, 0=don't use.\n"
					+ "# 	dispersal is only applicable when using gridded option.\n"
					+ "#\n" + "#\n" + "#\n",
			"# ===============================================================================\n"
					+ "# Additional parameters for annuals-only establishment.\n"
					+ "#\n"
					+ "# name = name as found in previous table.  must be exactly the same.\n"
					+ "# viable = max years seed viability.  sets seedprod array size.\n"
					+ "# xdecay = decay exponent.  indivs established = SUM(1/seed_age^xdecay)\n"
					+ "#",
			"# ===============================================================================\n"
					+ "# Species-specific probabilities of vegetative propagation for\n"
					+ "# various mortality types.  In C&L 1990, the probabilities were the same\n"
					+ "# for all clonal species, however, the program design makes it easy to provide\n"
					+ "# species-level control.  If the species is not clonal, the numbers are ignored,\n"
					+ "# which makes it easy to just cut and paste.\n"
					+ "\n"
					+ "# name = 4 char name exactly as above (although order is not important)\n"
					+ "# vprop1 = probability if insufficient resources\n"
					+ "# vprop2 = probability if slow growth\n"
					+ "# vprop3 = probability if intrinsic mortality\n"
					+ "# vprop4 = probability if disturbance\n" + "#",
			"# ===============================================================================\n"
					+ "# Species-specific input for seed dispersal.\n"
					+ "# Seed dispersal based on Coffin & Lauenroth 1989 paper\n"
					+ "# NOTE: Seed dispersal requires running with gridded option.\n"
					+ "#\n"
					+ "# name = 4 char name exactly as above (although order is not important)\n"
					+ "# dispersal = flag to turn seed dispersal on/off for this species(0 is off, 1 is on)\n"
					+ "# param1 = % of maximum biomass required for an individual to produce seeds\n"
					+ "# PPTdry = ppt of a dry year (in mm)\n"
					+ "# PPTwet = ppt of a wet year (in mm)\n"
					+ "# Pmin = probability of producing seeds in a dry year\n"
					+ "# Pmax = probability of producing seeds in a wet year\n"
					+ "# H = the average release height of the inflorescences (cm)\n"
					+ "# VT = the average sinking velocity of the seeds (cm/sec)\n"
					+ "#" };
	public class SpeciesParams implements Comparable<SpeciesParams> {
		/**
		 * genus/species name
		 */
		public String name;
		/**
		 * number of resource group to which this species belongs,<br>
		 * based on the order they were entered in the rgroups input file,<br>
		 * which is read before this file.  Note that a group cannot <br>
		 * contain a mix of annuals and perennials, although there can<br>
		 * be more than one group for annuals, for example, one for<br>
		 * invasives and one for natives.
		 */
		public int rg;
		/**
		 * maximum age in years, 0 = no limit<br>
		 * because a group can be only annuals or only perennials,<br>
		 * a 1 here implies this species belongs to an annual group.
		 */
		public int age;
		/**
		 * intrinsic growth rate (column 8, Table 2, C&L 1990)<br>
		 * for annuals, this is the proportion of maximum biomass<br>
		 * attainable with average resource availability, that is<br>
		 * reported biomass = maxbio * irate / PR
		 */
		public float irate;
		/**
		 * prop of intrisic growth rate to use as maximum growth rate<br>
		 * not used for annuals.
		 */
		public float ratep;
		/**
		 * number of years of slow growth before mortality begins not used for annuals.
		 */
		public int slow;
		/**
		 * disturbance class:<br>
		 * 1 = very sensitive to fecal pats and ant mounds<br>
		 * 2 = sensitive to pats, very sensitive to mounds<br>
		 * 3 = insensitive to pats, sensitive to mounds<br>
		 * 4 = insensitive to pats and mounds<br>
		 */
		public int disturb;
		/**
		 * seedling establishment probability for a given year<br>
		 * for annuals, this is the probability that propagules<br>
		 * will reach the plot in a given year.  Can be 1.0 for<br>
		 * native species, or < 1 for invasives.
		 */
		public float pestab;
		/**
		 * maximum number of individuals that can establish in a year<br>
		 * for annuals, max number of viable seeds that will grow to maturity<br>
		 * on the plot. ie, fecundity.<br>
		 */
		public int eind;
		/**
		 * biomass of an established seedling in grams<br>
		 * meaningless for annuals.
		 */
		public float minbio;
		/**
		 * biomass of a mature individual in grams<br>
		 * for annuals, see also irate definition.
		 */
		public float maxbio;
		/**
		 * can reproduce vegetatively (yes/no)<br>
		 * placeholder required but ignored for annuals.
		 */
		public boolean clonal;
		/**
		 * number of seedling-sized units to add if vegprop occurs
		 */
		public int vegindv;
		/**
		 * temperature class (1=C4, 2=C3, 0=not applicable)
		 */
		public int tclass;
		/**
		 * proportion of a cohort surviving to maturity (0.0-1.0)<br>
		 * meaningless for annuals.
		 */
		public float cosurv;
		/**
		 * turn on/off this species from the model. 1=use, 0=don't use.<br>
		 * see also the same flag in group parameters file.<br>
		 */
		public boolean onoff;
		/**
		 * turn on/off seed dispersal for this species. 1=use, 0=don't use.<br>
		 * dispersal is only applicable when using gridded option.
		 */
		public boolean dispersal;
		
		public SpeciesParams(String name, int rg, int age, float irate, float ratep, int slow, int disturb, float pestab, int eind, float minbio, float maxbio, boolean clonal, int vegindv, int tclass, float cosurv, boolean onoff, boolean dispersal) {
			setValues(name, rg, age, irate, ratep, slow, disturb, pestab, eind, minbio, maxbio, clonal, vegindv, tclass, cosurv, onoff, dispersal);
		}
		public void setValues(String name, int rg, int age, float irate, float ratep, int slow, int disturb, float pestab, int eind, float minbio, float maxbio, boolean clonal, int vegindv, int tclass, float cosurv, boolean onoff, boolean dispersal) {
			this.name = name;
			this.rg = rg;
			this.age = age;
			this.irate = irate;
			this.ratep = ratep;
			this.slow = slow;
			this.disturb = disturb;
			this.pestab = pestab;
			this.eind = eind;
			this.minbio = minbio;
			this.maxbio = maxbio;
			this.clonal = clonal;
			this.vegindv = vegindv;
			this.tclass = tclass;
			this.cosurv = cosurv;
			this.onoff = onoff;
			this.dispersal = dispersal;
		}
		public String toString() {
			return String.format(" %-9s %-2d  %-3d  %-07.5f  %-05.3f %-4d %-7d %-07.5f  %-4d %-05.3f   %-6.5f  %-6s %-7d %-6d  %-06.4f  %-5d", name, rg, age, irate, ratep, slow, disturb, pestab, eind, minbio, maxbio, clonal?"y":"n", vegindv, tclass, cosurv, onoff?1:0, dispersal?1:0);
		}
		@Override
		public int compareTo(SpeciesParams o) {
			return this.name.compareTo(o.name);
		}
		
	}
	
	public class AnnualsParams implements Comparable<AnnualsParams> {
		/**
		 * name as found in SpeciesParams.  must be exactly the same.
		 */
		public String name;
		/**
		 * max years seed viability.  sets seedprod array size.
		 */
		public int viable;
		/**
		 * decay exponent.  indivs established = SUM(1/seed_age^xdecay)
		 */
		public float xdecay;
		
		public AnnualsParams(String name, int viable, float xdecay) {
			setValues(name, viable, xdecay);
		}
		public void setValues(String name, int viable, float xdecay) {
			this.name = name;
			this.viable = viable;
			this.xdecay = xdecay;
		}
		public String toString() {
			return String.format(" %-9s %-6d %-06.3f",name,viable,xdecay);
		}
		@Override
		public int compareTo(AnnualsParams o) {
			return this.name.compareTo(o.name);
		}
		
	}
	
	public class SpeciesProbParam implements Comparable<SpeciesProbParam> {
		/**
		 * name exactly from SpeciesParam (although order is not important)
		 */
		public String name;
		/**
		 * probability if insufficient resources
		 */
		public float vprop1;
		/**
		 * probability if slow growth
		 */
		public float vprop2;
		/**
		 * probability if intrinsic mortality
		 */
		public float vprop3;
		/**
		 * probability if disturbance
		 */
		public float vprop4;
		
		public SpeciesProbParam(String name, float vprop1, float vprop2, float vprop3, float vprop4) {
			setValues(name, vprop1, vprop2, vprop3, vprop4);
		}
		
		public void setValues(String name, float vprop1, float vprop2, float vprop3, float vprop4) {
			this.name = name;
			this.vprop1 = vprop1;
			this.vprop2 = vprop2;
			this.vprop3 = vprop3;
			this.vprop4 = vprop4;
		}
		
		public String toString() {
			return String.format(" %-9s %-06.4f %-06.4f %-06.4f %-06.4f",name,vprop1,vprop2,vprop3,vprop4);
		}
		
		@Override
		public int compareTo(SpeciesProbParam o) {
			return this.name.compareTo(o.name);
		}
	}
	
	public class SeedDispersalParam implements Comparable<SeedDispersalParam> {
		/**
		 * name exactly from SpeciesParam (although order is not important)
		 */
		public String name;
		/**
		 * flag to turn seed dispersal on/off for this species(0 is off, 1 is on)
		 */
		public boolean dispersal;
		/**
		 * % of maximum biomass required for an individual to produce seeds
		 */
		public float param1;
		/**
		 * ppt of a dry year (in mm)
		 */
		public float pptDry;
		/**
		 * ppt of a wet year (in mm)
		 */
		public float pptWet;
		/**
		 * probability of producing seeds in a dry year
		 */
		public float pmin;
		/**
		 * probability of producing seeds in a wet year
		 */
		public float pmax;
		/**
		 * the average release height of the inflorescences (cm) 
		 */
		public float h;
		/**
		 * the average sinking velocity of the seeds (cm/sec)
		 */
		public float vt;
		
		public SeedDispersalParam(String name, boolean dispersal, float param1, float pptDry, float pptWet, float pmin, float pmax, float h, float vt) {
			setValues(name, dispersal, param1, pptDry, pptWet, pmin, pmax, h, vt);
		}
		public void setValues(String name, boolean dispersal, float param1, float pptDry, float pptWet, float pmin, float pmax, float h, float vt) {
			this.name = name;
			this.dispersal = dispersal;
			this.param1 = param1;
			this.pptDry = pptDry;
			this.pptWet = pptWet;
			this.pmin = pmin;
			this.pmax = pmax;
			this.h = h;
			this.vt = vt;
		}
		public String toString() {
			return String.format(" %-6s %-9d  %-06.3f  %-06.3f  %-06.3f  %-04.3f %-04.4f   %-04.4f %-04.4f",name,dispersal?1:0,param1,pptDry,pptWet,pmin,pmax,h,vt);
		}
		@Override
		public int compareTo(SeedDispersalParam o) {
			return this.name.compareTo(o.name);
		}
	}
	
	public List<SpeciesParams> speciesParams;
	public List<AnnualsParams> annualsParams;
	public List<SpeciesProbParam> speciesProbParam;
	public List<SeedDispersalParam> seedDispersalParam;
	
	public void read(Path SpeciesInPath) throws Exception {
		List<String> lines = java.nio.file.Files.readAllLines(SpeciesInPath, StandardCharsets.UTF_8);
		LogFileIn f = stepwat.LogFileIn.getInstance();
		//int nFileItemsRead = 0;
		int nGroupNumber = 1;
		
		speciesParams = new ArrayList<SpeciesParams>();
		annualsParams = new ArrayList<AnnualsParams>();
		speciesProbParam = new ArrayList<SpeciesProbParam>();
		seedDispersalParam = new ArrayList<SeedDispersalParam>();
		
		for (String line : lines) {
			// Skip Comments and empty lines
			if (!line.matches("^\\s*#.*") && !line.matches("^[\\s]*$")) {
				line = line.trim();
				String[] values = line.split("#")[0].split("[ \t]+");// Remove comment  after data
			
				if(values[0] == "[end]") {
					nGroupNumber++;
				} else {
					readGroup(nGroupNumber, values, f);
				}
				//nFileItemsRead++;
			}
		}
		this.data = true;
		sort();
	}
	
	public void sort() {
		Collections.sort(speciesParams);
		Collections.sort(annualsParams);
		Collections.sort(speciesProbParam);
		Collections.sort(seedDispersalParam);
	}
	
	private void readGroup(int group, String[] values, LogFileIn f) throws Exception {
		switch(group) {
		case 1:
			if(values.length < 16 | values.length > 17)
				f.LogError(LogFileIn.LogMode.ERROR, "species.in read : Species definitions Expected 16 or 17 value.");
			try {
				String name = values[0];
				int rg = Integer.valueOf(values[1]);
				int age = Integer.valueOf(values[2]);
				float irate = Float.valueOf(values[3]);
				float ratep = Float.valueOf(values[4]);
				int slow = Integer.valueOf(values[5]);
				int disturb = Integer.valueOf(values[6]);
				float pestab = Float.valueOf(values[7]);
				int eind = Integer.valueOf(values[8]);
				float minbio = Float.valueOf(values[9]);
				float maxbio = Float.valueOf(values[10]);
				boolean clonal = Integer.valueOf(values[11])>0 ? true : false;
				int vegindv = Integer.valueOf(values[12]);
				int tclass = Integer.valueOf(values[13]);
				float cosurv = Float.valueOf(values[14]);
				boolean onoff = Integer.valueOf(values[15])>0 ? true : false;
				boolean dispersal;
				if(values.length == 17) {
					dispersal = Integer.valueOf(values[16])>0 ? true : false;
				} else {
					dispersal = false;
				}
				speciesParams.add(new SpeciesParams(name, rg, age, irate, ratep, slow, disturb, pestab, eind, minbio, maxbio, clonal, vegindv, tclass, cosurv, onoff, dispersal));
			} catch(NumberFormatException e) {
				f.LogError(LogFileIn.LogMode.ERROR, "species.in read : Could not convert group values to numbers.");
			}
			break;
		case 2:
			if(values.length != 3)
				f.LogError(LogFileIn.LogMode.ERROR, "species.in read : Annual Parameters Expected 3 value.");
			try {
				String name = values[0];
				int viable = Integer.valueOf(values[1]);
				float xdecay = Float.valueOf(values[2]);
				annualsParams.add(new AnnualsParams(name, viable, xdecay));
			} catch(NumberFormatException e) {
				f.LogError(LogFileIn.LogMode.ERROR, "species.in read : Could not convert Annual Parameters values to number.");
			}
			break;
		case 3:
			if(values.length != 5)
				f.LogError(LogFileIn.LogMode.ERROR, "species.in read : Veg Prob Parameters Expected 5 value.");
			try {
				String name = values[0];
				float vprop1 = Float.valueOf(values[1]);
				float vprop2 = Float.valueOf(values[2]);
				float vprop3 = Float.valueOf(values[3]);
				float vprop4 = Float.valueOf(values[4]);
				speciesProbParam.add(new SpeciesProbParam(name, vprop1, vprop2, vprop3, vprop4));
			} catch(NumberFormatException e) {
				f.LogError(LogFileIn.LogMode.ERROR, "species.in read : Could not convert Veg Prob values to number.");
			}
			break;
		case 4:
			if(values.length != 9)
				f.LogError(LogFileIn.LogMode.ERROR, "species.in read : Seed Dispersal Parameters Expected 9 value.");
			try {
				String name = values[0];
				boolean dispersal = Integer.valueOf(values[1])>0?true:false;
				float param1 = Float.valueOf(values[2]);
				float pptDry = Float.valueOf(values[3]);
				float pptWet = Float.valueOf(values[4]);
				float pmin = Float.valueOf(values[5]);
				float pmax = Float.valueOf(values[6]);
				float h = Float.valueOf(values[7]);
				float vt = Float.valueOf(values[8]);
				seedDispersalParam.add(new SeedDispersalParam(name, dispersal, param1, pptDry, pptWet, pmin, pmax, h, vt));
			} catch(NumberFormatException e) {
				f.LogError(LogFileIn.LogMode.ERROR, "species.in read : Could not convert Seed Dispersal values to number.");
			}
			break;
		case 5:
			break;
		default:
			f.LogError(LogFileIn.LogMode.ERROR, "species.in read : Unkown Section.");
		}
	}
	
	public void write(Path SpeciesInPath) throws IOException {
		List<String> lines = new ArrayList<String>();
		lines.add(Comments[0]);
		lines.add("# name     rg  age  irate    ratep slow disturb pestab   eind minbio  maxbio  clonal vegindv tclass  cosurv  onoff dispersal");
		for(SpeciesParams species : speciesParams) {
			lines.add(species.toString());
		}
		lines.add("\n[end]  # section end\n");
		lines.add(Comments[1]);
		lines.add("# name     viable xdecay");
		for (AnnualsParams annuals : annualsParams) {
			lines.add(annuals.toString());
		}
		lines.add("\n[end]  # section end\n");
		lines.add(Comments[2]);
		lines.add("# name     vprop1 vprop2 vprop3 vprop4");
		for(SpeciesProbParam prob : speciesProbParam) {
			lines.add(prob.toString());
		}
		lines.add("\n[end]  # section end\n");
		lines.add(Comments[3]);
		lines.add("# name 	dispersal	param1  PPTdry  PPTwet	Pmin	Pmax	H	 VT");
		for(SeedDispersalParam seed : seedDispersalParam) {
			lines.add(seed.toString());
		}
		lines.add("\n[end]  # section end");
		java.nio.file.Files.write(SpeciesInPath, lines, StandardCharsets.UTF_8);
	}
}
