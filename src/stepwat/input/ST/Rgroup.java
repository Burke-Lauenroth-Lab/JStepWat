package stepwat.input.ST;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import stepwat.LogFileIn;

public class Rgroup {
	public static final String[] Comments = {"# Rgroup input definition file STEPPEWAT\n"+
			"# resource-group-level information\n"+
			"\n"+
			"# Anything after the first pound sign is a comment\n"+
			"# Blank lines, white space, and comments may be used freely,\n"+
			"# however, the order of input is important\n"+
			"\n"+
			"####################################################\n"+
			"# All-group parameters:\n"+
			"# NGrpEstab = maximum number of resource groups that can\n"+
			"#        establish in a given year\n"+
			"#\n"+
			"# NGrpEstab",
			//break
			"#=============================================================\n"+
			"#                Group definitions\n"+
			"# name = a name to give the group (12 chars)\n"+
			"# space = proportion of the resource space used by the group.\n"+
			"#         this must sum to 1.0\n"+
			"# density = relative number of mature individuals per plot\n"+
			"# maxest = maximum species that can establish in a given year\n"+
			"# slow = slow growth rate, defines when to count stretched years\n"+
			"# stretch  = max yrs resources can be stretched before low-resource\n"+
			"#                    mortality occurs.\n"+
			"# xres   = 1 = use other groups' extra resources, 0 = can't use\n"+
			"# estann = establish annually if 1; if 0, establishment\n"+
			"#          for this group is random from year to year.\n"+
			"# on = switch to turn off this group, ie, don't produce any\n"+
			"#       of the plants in this group.  1=on, 0=off.\n"+
			"# startyr = do not attempt to grow this group until this year.\n"+
			"#        especially useful for invasives.\n"+
			"# killyr = kill all members of this group at end of this year.\n"+
			"#          0 == don't kill\n"+
			"# killfrq = frequency to kill.  if < 1, yearly probability,\n"+
			"#            if > 1 yearly interval, after start year (start).  \n"+
			"#            Overrides killyr.\n"+
			"#            See also disturbances in environmental parameter file.\n"+
			"# extirp = year at which all group members are extirpated\n"+
			"#       (without recovery) from the plot. 0==don't use, >0 == year # \n"+
			"# mort =  switch to allow age-independent/slowgrowth mortality\n"+
			"#         functions. eg, bouteloua and succulents are excluded,\n"+
			"#         but others might be excluded for testing purposes.\n"+
			"#         0 = don't use; 1 = use.\n"+
			"# xgrow = extra growth factor; if > 0, plants can convert 'extra' ppt\n"+
			"#         into ephemeral (this year's) biomass by\n"+
			"#         grams_extra_biomass = mm_extra_ppt * xgrow_factor\n"+
			"# veg_prod_type = 1 for tree, 2 for shrub, & 3 for grass.  Refer to soilwat's VegProd types in VegProd.h.\n"+
			"\n",
			//break
			"#=============================================================\n"+
			"# Parameters for resource availability in wet/dry/normal years\n"+
			"# name = name used in list above.\n"+
			"# nslope = slope of resource availability in normal years (EQN 2)\n"+
			"# nint   = intercept of resource availability in normal years\n"+
			"# wetslope = slope of resource availability in wet years (EQN 4)\n"+
			"# wetint = intercept of resource availability in wet years\n"+
			"# dryslope = slope of res.avail. in dry years (EQN 3)\n"+
			"# dryint   = intercept of res.avail. in dry years\n"+
			"\n",
			//break
			"#=============================================================\n"+
			"# Parameters for wet-year growth modifiers (usu. succulents)\n"+
			"# name = name used in list above.\n"+
			"# gslope = slope for growth reduction in wet years (eqn 10)\n"+
			"# gint   = intercept ''     ''      ''\n"+
			"# mslope = slope for mortality in wet years (eqn 16)\n"+
			"# gint   = intercept ''     ''      ''\n"+
			"\n"
			};
	
	public class GroupType implements Comparable<GroupType> {
		/**
		 * a name to give the group
		 */
		public String name;
		/**
		 * proportion of the resource space used by the group. This must sum to 1.0
		 */
		public float space;
		/**
		 * relative number of mature individuals per plot
		 */
		public float density;
		/**
		 * maximum species that can establish in a given year
		 */
		public int maxest;
		/**
		 * slow growth rate, defines when to count stretched years
		 */
		public float slow;
		/**
		 * max yrs resources can be stretched before low-resource mortality occurs.
		 */
		public int stretch;
		/**
		 * 1 = use other groups' extra resources, 0 = can't use - Stored as boolean
		 */
		public boolean xres;
		/**
		 * establish annually if 1; if 0, establishment for this group is random from year to year.
		 */
		public int estann;
		/**
		 * switch to turn off this group, ie, don't produce any of the plants in this group.  1=on, 0=off.
		 */
		public boolean on;
		/**
		 * do not attempt to grow this group until this year. especially useful for invasives.
		 */
		public int startyr;
		/**
		 * kill all members of this group at end of this year. 0 == don't kill
		 */
		public int killyr;
		/**
		 * frequency to kill.  if < 1, yearly probability, if > 1 yearly interval, after start year (start).<br>
		 * Overrides killyr. See also disturbances in environmental parameter file.
		 */
		public float killfreq;
		/**
		 * year at which all group members are extirpated<br>
		 * (without recovery) from the plot. 0==don't use, >0 == year # 
		 */
		public int extirp;
		/**
		 * switch to allow age-independent/slowgrowth mortality<br>
		 * functions. eg, bouteloua and succulents are excluded,<br>
		 * but others might be excluded for testing purposes.<br>
		 * 0 = don't use; 1 = use.<br>
		 */
		public int mort;
		/**
		 * extra growth factor; if > 0, plants can convert "extra" ppt<br>
		 * into ephemeral (this year's) biomass by grams_extra_biomass = mm_extra_ppt * xgrow_factor
		 */
		public float xgrow;
		/**
		 * 1 for tree, 2 for shrub, & 3 for grass.  Refer to soilwat's VegProd types in VegProd.h.
		 */
		public int veg_prod_type;
		
		public GroupType(String name, float space, float density, int maxest, float slow, int stretch, boolean xres, int estann, boolean on,
				int startyr, int killyr, float killfreq, int extirp, int mort, float xgrow, int veg_prod_type) {
			setValues(name, space, density, maxest, slow, stretch, xres, estann, on, startyr, killyr, killfreq, extirp, mort, xgrow, veg_prod_type);
		}
		public void setValues(String name, float space, float density, int maxest, float slow, int stretch, boolean xres, int estann, boolean on,
				int startyr, int killyr, float killfreq, int extirp, int mort, float xgrow, int veg_prod_type) {
			this.name = name;
			this.space = space;
			this.density = density;
			this.maxest = maxest;
			this.slow = slow;
			this.stretch = stretch;
			this.xres = xres;
			this.estann = estann;
			this.on = on;
			this.startyr = startyr;
			this.killyr = killyr;
			this.killfreq = killfreq;
			this.extirp = extirp;
			this.mort = mort;
			this.xgrow = xgrow;
			this.veg_prod_type = veg_prod_type;
		}
		public int nameLength() {
			return name.length();
		}
		public String toString(int width) {
			return String.format("%-"+Integer.toString(width)+"s  %-07.4f  %-07.4f  %-6d %-07.4f  %-7d %-4d  %-6d  %-2d  %-7d %-6d  %-07.4f  %-6d  %-4d %-07.4f %-4d", name, space,density,maxest,slow,stretch,xres?1:0,estann,on?1:0,startyr,killyr,extirp,mort,xgrow,veg_prod_type);
		}
		@Override
		public int compareTo(GroupType o) {
			return this.name.compareTo(o.name);
		}
	}
	
	public class ResourceParameters implements Comparable<ResourceParameters> {
		/**
		 * name used in group type.
		 */
		public String name;
		/**
		 * slope of resource availability in normal years (EQN 2)
		 */
		public float nslope;
		/**
		 * intercept of resource availability in normal years
		 */
		public float nint;
		/**
		 * slope of resource availability in wet years (EQN 4)
		 */
		public float wetslope;
		/**
		 * intercept of resource availability in wet years
		 */
		public float wetint;
		/**
		 * slope of res.avail. in dry years (EQN 3)
		 */
		public float dryslope;
		/**
		 * intercept of res.avail. in dry years
		 */
		public float dryint;
		
		public ResourceParameters(String name, float nslope, float nint, float wetslope, float wetint, float dryslope, float dryint) {
			setValues(name, nslope, nint, wetslope, wetint, dryslope, dryint);
		}
		public void setValues(String name, float nslope, float nint, float wetslope, float wetint, float dryslope, float dryint) {
			this.name = name;
			this.nslope = nslope;
			this.nint = nint;
			this.wetslope = wetslope;
			this.wetint = wetint;
			this.dryslope = dryslope;
			this.dryint = dryint;
		}
		public String toString(int width) {
			return String.format("%-"+Integer.toString(width)+"s  %-07.5f  %-07.5f  %-07.5f  %-07.5f  %-07.5f  %-07.5f", name, nslope, nint,wetslope,wetint,dryslope,dryint);
		}
		@Override
		public int compareTo(ResourceParameters o) {
			return this.name.compareTo(o.name);
		}
	}
	
	public class SucculentParams implements Comparable<SucculentParams> {
		/**
		 * name used in group definition.
		 */
		public String name;
		/**
		 * slope for growth reduction in wet years (eqn 10)
		 */
		public float gslope;
		/**
		 * intercept for growth reduction in wet years (eqn 10)
		 */
		public float gint;
		/**
		 * slope for mortality in wet years (eqn 16)
		 */
		public float mslope;
		/**
		 * interceptfor mortality in wet years (eqn 16)
		 */
		public float mint;
		
		public SucculentParams(String name, float gslope, float gint, float mslope, float mint) {
			setValues(name, gslope, gint, mslope, mint);
		}
		public void setValues(String name, float gslope, float gint, float mslope, float mint) {
			this.name = name;
			this.gslope = gslope;
			this.gint = gint;
			this.mslope = mslope;
			this.mint = mint;
		}
		public String toString(int width) {
			return String.format("%-"+Integer.toString(width)+"s  %-07.4f  %-07.4f  %-07.4f  %-07.4f", name, gslope, gint,mslope,mint);
		}
		@Override
		public int compareTo(SucculentParams o) {
			return this.name.compareTo(o.name);
		}
	}
	
	/**
	 * maximum number of resource groups that can establish in a given year
	 */
	public int nGrpEstab;
	public List<GroupType> groups;
	public List<ResourceParameters> resourceParameters;
	public List<SucculentParams> succulentParams;
	
	public void read(Path RgroupInPath) throws Exception {
		List<String> lines = java.nio.file.Files.readAllLines(RgroupInPath, StandardCharsets.UTF_8);
		LogFileIn f = stepwat.LogFileIn.getInstance();
		int nFileItemsRead = 0;
		int nGroupNumber = 1;
		
		groups = new ArrayList<GroupType>();
		resourceParameters = new ArrayList<ResourceParameters>();
		succulentParams = new ArrayList<SucculentParams>();
		
		for (String line : lines) {
			// Skip Comments and empty lines
			if (!line.matches("^\\s*#.*") && !line.matches("^[\\s]*$")) {
				line = line.trim();
				String[] values = line.split("#")[0].split("[ \t]+");// Remove comment  after data
				switch (nFileItemsRead) {
				case 0:
					if(values.length != 1)
						f.LogError(LogFileIn.LogMode.ERROR, "rgroup.in read : nGrpEstab Expected 1 value.");
					try {
						nGrpEstab = Integer.parseInt(values[0]);
					} catch(NumberFormatException e) {
						f.LogError(LogFileIn.LogMode.ERROR, "rgroup.in read : Could not convert NGrpEstab to number.");
					}
					break;
				default:
					if(values[0] == "[end]") {
						nGroupNumber++;
					} else {
						readGroup(nGroupNumber, values, f);
					}
					break;
				}
				nFileItemsRead++;
			}
		}
		sort();
	}
	
	public void sort() {
		Collections.sort(groups);
		Collections.sort(resourceParameters);
		Collections.sort(succulentParams);
	}
	
	private void readGroup(int group, String[] values, LogFileIn f) throws Exception {
		switch(group) {
		case 2:
			if(values.length != 16)
				f.LogError(LogFileIn.LogMode.ERROR, "rgroup.in read : Group definitions Expected 16 value.");
			try {
				String name = values[0];
				float space = Float.parseFloat(values[1]);
				float density = Float.parseFloat(values[2]);
				int maxest = Integer.parseInt(values[3]);
				float slow = Float.parseFloat(values[4]);
				int stretch = Integer.parseInt(values[5]);
				boolean xres = Integer.parseInt(values[6])>0 ? true : false;
				int estann = Integer.parseInt(values[7]);
				boolean on = Integer.parseInt(values[8])>0 ? true : false;
				int startyr = Integer.parseInt(values[9]);
				int killyr = Integer.parseInt(values[10]);
				float killfreq = Float.parseFloat(values[11]);
				int extirp = Integer.parseInt(values[12]);
				int mort = Integer.parseInt(values[13]);
				float xgrow = Float.parseFloat(values[14]);
				int veg_prod_type = Integer.parseInt(values[15]);
				groups.add(new GroupType(name, space, density, maxest, slow, stretch, xres, estann, on, startyr, killyr, killfreq, extirp, mort, xgrow, veg_prod_type));
			} catch(NumberFormatException e) {
				f.LogError(LogFileIn.LogMode.ERROR, "rgroup.in read : Could not convert group values to number.");
			}
			break;
		case 3:
			if(values.length != 7)
				f.LogError(LogFileIn.LogMode.ERROR, "rgroup.in read : Resource Parameters Expected 7 value.");
			try {
				String name = values[0];
				float nslope = Float.valueOf(values[1]);
				float nint = Float.valueOf(values[2]);
				float wetslope = Float.valueOf(values[3]);
				float wetint = Float.valueOf(values[4]);
				float dryslope = Float.valueOf(values[5]);
				float dryint = Float.valueOf(values[6]);
				resourceParameters.add(new ResourceParameters(name, nslope, nint, wetslope, wetint, dryslope, dryint));
			} catch(NumberFormatException e) {
				f.LogError(LogFileIn.LogMode.ERROR, "rgroup.in read : Could not convert Resource Parameters values to number.");
			}
			break;
		case 4:
			if(values.length != 5)
				f.LogError(LogFileIn.LogMode.ERROR, "rgroup.in read : Succulents Parameters Expected 5 value.");
			try {
				String name = values[0];
				float gslope = Float.valueOf(values[1]);
				float gint = Float.valueOf(values[2]);
				float mslope = Float.valueOf(values[3]);
				float mint = Float.valueOf(values[4]);
				succulentParams.add(new SucculentParams(name, gslope, gint, mslope, mint));
			} catch(NumberFormatException e) {
				f.LogError(LogFileIn.LogMode.ERROR, "rgroup.in read : Could not convert Succulents Parameters values to number.");
			}
			break;
		case 5:
			break;
		default:
			f.LogError(LogFileIn.LogMode.ERROR, "rgroup.in read : Unkown Section.");
		}
	}
	
	private int maxNameLength() {
		List<Integer> nameLengths = new ArrayList<Integer>();
		for(int i=0; i<groups.size(); i++) {
			nameLengths.add(groups.get(i).nameLength());
		}
		int max = Collections.max(nameLengths);
		if(max <= 6)
			return 7;
		else
			return max;
	}
	
	public void write(Path RgroupInPath) throws IOException {
		List<String> lines = new ArrayList<String>();
		lines.add(Comments[0]);
		lines.add(String.valueOf(nGrpEstab));
		lines.add("[end]  # section end\n");
		lines.add(Comments[1]);
		int width = maxNameLength();
		lines.add("# name"+new String(new char[width-6]).replace("\0", " ")+"space   density maxest slow    stretch xres  estann  on  startyr killyr  killfrq  extirp  mort  xgrow  veg_prod_type");
		for (GroupType group : groups) {
			lines.add(group.toString(width));
		}
		lines.add("");
		lines.add("[end]  # section end\n");
		lines.add(Comments[2]);
		lines.add("# name"+new String(new char[width-6]).replace("\0", " ")+"nslope   nint     wetslope wetint   dryslope dryint");
		for(ResourceParameters rparm : resourceParameters) {
			lines.add(rparm.toString(width));
		}
		lines.add("");
		lines.add("[end]  # section end\n");
		lines.add(Comments[3]);
		lines.add("# name"+new String(new char[width-6]).replace("\0", " ")+"gslope  gint  mslope  gint");
		for(SucculentParams sparm : succulentParams) {
			lines.add(sparm.toString());
		}
		lines.add("");
		lines.add("[end]  # section end\n");
		java.nio.file.Files.write(RgroupInPath, lines, StandardCharsets.UTF_8);
	}
}
