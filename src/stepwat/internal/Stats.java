package stepwat.internal;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import stepwat.LogFileIn;
import stepwat.LogFileIn.LogMode;

public class Stats {
	
	Globals globals;
	Plot plot;
	Environs env;
	RGroups rgroups;
	BmassFlags bmassFlags;
	MortFlags mortFlags;
	
	public class Accumulators {
		double sum, sum_sq;
		int nobs;
	}
	
	public class Stat {
		//Names in RGroup and Species
		String name;
		List<Accumulators> s;
		
		public Stat(int size) {
			s = new ArrayList<Accumulators>(size);
			for(int i=0; i<size; i++)
				s.add(new Accumulators());
		}
	}
	
	private Stat dist, ppt, temp;
	private List<Stat> grp = new ArrayList<Stats.Stat>();
	private List<Stat> gsize = new ArrayList<Stats.Stat>();
	private List<Stat> gpr = new ArrayList<Stats.Stat>();
	private List<Stat> gmort = new ArrayList<Stats.Stat>();
	private List<Stat> gestab = new ArrayList<Stats.Stat>();
	private List<Stat> spp = new ArrayList<Stats.Stat>();
	private List<Stat> indv = new ArrayList<Stats.Stat>();
	private List<Stat> smort = new ArrayList<Stats.Stat>();
	private List<Stat> sestab = new ArrayList<Stats.Stat>();
	private List<Stat> sreceived = new ArrayList<Stats.Stat>();
	
	boolean firsttime = true;
	
	public Stats(Globals g, Plot p, Environs e, RGroups rgs, BmassFlags b, MortFlags m) {
		this.globals = g;
		this.plot = p;
		this.env = e;
		this.rgroups = rgs;
		this.bmassFlags = b;
		this.mortFlags = m;
	}
	
	private void collectAdd(Accumulators p, double v) {
		p.sum += v;
		p.sum_sq += v * v;
		p.nobs ++;
	}
	
	public void collect(int year) throws Exception {
		// fill data structures with samples to be
		// computed later in Stat_Output().
		// enter with year base1, but subtract 1 for index (base0)
		LogFileIn f = stepwat.LogFileIn.getInstance();

		double bmass;

		if (firsttime) {
			firsttime = false;
			init();
		}

		year--;
		if (bmassFlags.isDist() && plot.disturbed != 0)
			dist.s.get(year).nobs++;
		if (bmassFlags.isPpt())
			collectAdd(ppt.s.get(year), env.ppt);

		if (bmassFlags.isTmp())
			collectAdd(temp.s.get(year), env.temp);

		if (bmassFlags.isGrpb()) {
			for (ResourceGroup rg : rgroups) {
				bmass = (double) rg.getBiomass();
				if (Double.compare(bmass, 0.0) < 0) {// LT
					f.LogError(LogMode.WARN, "Grp " + rg.getName()
							+ " biomass(" + String.valueOf(bmass)
							+ ") < 0 in Stat collect");
					bmass = 0.0;
				}
				collectAdd(grp.get(rg.getGrp_num()).s.get(year), bmass);

				if (bmassFlags.isSize())
					collectAdd(gsize.get(rg.getGrp_num()).s.get(year),
							rg.relsize);
				if (bmassFlags.isPr())
					collectAdd(gpr.get(rg.getGrp_num()).s.get(year), rg.pr);
			}
		}

		if (bmassFlags.isSppb()) {
			for (Species sp : rgroups.getAllSpecies()) {
				bmass = (double) sp.getBiomass();
				if (Double.compare(bmass, 0.0) < 0) {// LT
					f.LogError(LogMode.WARN, "Spp " + sp.getName()
							+ " biomass(" + String.valueOf(bmass)
							+ ") < 0 in Stat collect");
					bmass = 0.0;
				}
				collectAdd(spp.get(sp.getSp_num()).s.get(year), bmass);

				if (bmassFlags.isIndv())
					collectAdd(indv.get(sp.getSp_num()).s.get(year),
							(double) sp.getEst_count());
			}
		}

		if (globals.UseSeedDispersal && globals.UseGrid) {
			for (Species sp : rgroups.getAllSpecies())
				collectAdd(sreceived.get(sp.getSp_num()).s.get(year),
						(double) sp.received_prob);
		}
		
	}
	
	private void init() {
		if(bmassFlags.isDist())
			dist = new Stat(globals.runModelYears);
		if(bmassFlags.isPpt())
			ppt = new Stat(globals.runModelYears);
		if(bmassFlags.isTmp())
			temp = new Stat(globals.runModelYears);
		if(bmassFlags.isGrpb()) {
			grp = new ArrayList<Stats.Stat>(globals.grpCount);
			for(int i=0; i<globals.grpCount; i++)
				grp.add(new Stat(globals.runModelYears));
			if(bmassFlags.isSize()) {
				gsize = new ArrayList<Stats.Stat>(globals.grpCount);
				for(int i=0; i<globals.grpCount; i++)
					gsize.add(new Stat(globals.runModelYears));
			}
			if(bmassFlags.isPr()) {
				gpr = new ArrayList<Stats.Stat>();
				for(int i=0; i<globals.grpCount; i++)
					gpr.add(new Stat(globals.runModelYears));
			}
		}
		if(mortFlags.isGroup()) {
			gestab = new ArrayList<Stats.Stat>(globals.grpCount);
			for(int i=0; i<globals.grpCount; i++)
				gestab.add(new Stat(1));
			gmort = new ArrayList<Stats.Stat>(globals.grpCount);
			for(ResourceGroup rg : rgroups)
				gmort.add(new Stat(rg.getMax_age()));
		}
		if(bmassFlags.isSppb()) {
			spp = new ArrayList<Stats.Stat>(globals.sppCount);
			for(int i=0; i<globals.sppCount; i++)
				spp.add(new Stat(globals.runModelYears));
			if(bmassFlags.isIndv()) {
				indv = new ArrayList<Stats.Stat>(globals.sppCount);
				for(int i=0; i<globals.sppCount; i++)
					indv.add(new Stat(globals.runModelYears));
			}
		}
		if(mortFlags.isSpecies()) {
			sestab = new ArrayList<Stats.Stat>(globals.sppCount);
			for(int i=0; i<globals.sppCount; i++)
				sestab.add(new Stat(1));
			smort = new ArrayList<Stats.Stat>(globals.sppCount);
			for(Species sp : rgroups.getAllSpecies())
				smort.add(new Stat(sp.getMax_age()));
		}
		if(globals.UseSeedDispersal && globals.UseGrid) {
			
		}
		//apoint names of columns
		if (bmassFlags.isGrpb()) {
			for(ResourceGroup rg : rgroups)
				grp.get(rg.getGrp_num()).name = rg.getName();
		}
		if (mortFlags.isGroup()) {
			for(ResourceGroup rg : rgroups)
				gmort.get(rg.getGrp_num()).name = rg.getName();
		}
		if (bmassFlags.isSppb()) {
			for(Species sp : rgroups.getAllSpecies())
				spp.get(sp.getSp_num()).name = sp.getName();
		}
		if (mortFlags.isSpecies()) {
			for(Species sp : rgroups.getAllSpecies())
				smort.get(sp.getSp_num()).name = sp.getName();
		}
	}
	
	/**
	 * accumulated for the entire model run within
	 * Species_Update_Kills(), then collected
	 * here to compare among iterations.
	 */
	public void collectGMort() {
		for(ResourceGroup rg : rgroups) {
			if(!rg.isUse_me())
				continue;
			collectAdd(gestab.get(rg.getGrp_num()).s.get(0), rg.estabs);
			for(int age=0; age<rg.getMax_age(); age++) {
				collectAdd(gmort.get(rg.getGrp_num()).s.get(age), (double) rg.kills[age]);
			}
		}
	}
	/**
	 * accumulated for the entire model run within
	 * Species_Update_Kills(), then collected
	 * here to compare among iterations.
	 */
	public void collectSMort() {
		for(Species sp : rgroups.getAllSpecies()) {
			if(!sp.isUse_me())
				continue;
			collectAdd(sestab.get(sp.getSp_num()).s.get(0), sp.estabs);
			for(int age=0; age<sp.getMax_age(); age++) {
				collectAdd(smort.get(sp.getSp_num()).s.get(age), (double) sp.kills[age]);
			}
		}
	}
	private String fieldsToLine(List<String> fields, boolean lineEnding) {
		String line = "";
		StringBuilder builder = new StringBuilder();
		for(String s : fields) {
			builder.append(s+mortFlags.getSep());
		}
		line = builder.toString();
		line = line.substring(0, line.length()-1);
		if(lineEnding)
			line += "\n";
		return line;
	}
	private String getMortFirstLine() {
		String line = "";
		List<String> fields = new ArrayList<String>();
		
		fields.add("Age");
		if(mortFlags.isGroup()) {
			for(ResourceGroup rg : rgroups) {
				fields.add(rg.getName());
			}
		}
		if(mortFlags.isSpecies()) {
			for(Species sp : rgroups.getAllSpecies()) {
				fields.add(sp.getName());
			}
		}
		line += fieldsToLine(fields, true);
		return line;
	}
	private float getAvg(Accumulators p) {
		if(p.nobs == 0)
			return 0.0f;
		return (float) (p.sum / (double) p.nobs);
	}
	private float getStd(Accumulators p) {
		double s;
		
		if(p.nobs <= 1)
			return 0.0f;
		
		s = (p.nobs * p.sum_sq) - (p.sum * p.sum);
		s /= p.nobs * (p.nobs - 1);
		
		return (float) Math.sqrt(s);
	}
	/**
	 * @throws IOException 
	 * 
	 */
	public void outputYrMorts() throws IOException {
		if(!mortFlags.isYearly())
			return;
		
		String lines = "";
		List<String> fields = new ArrayList<String>();
		
		lines += getMortFirstLine();
		
		fields.add("Estabs");
		if(mortFlags.isGroup()) {
			for(ResourceGroup rg : rgroups) {
				fields.add(String.format("%d", rg.estabs));
			}
		}
		if(mortFlags.isSpecies()) {
			for(Species sp : rgroups.getAllSpecies()) {
				fields.add(String.format("%d", sp.estabs));
			}
		}
		lines += fieldsToLine(fields, true);
		fields.clear();
		
		//print one line of kill frequencies per age
		for(int age=0; age < globals.Max_Age; age++) {
			fields.add(String.format("%d", age+1));
			if(mortFlags.isGroup()) {
				for(ResourceGroup rg : rgroups) {
					if(age < rg.getMax_age()) {
						fields.add(String.format("%d", rg.kills[age]));
					} else {
						fields.add("");
					}
				}
			}
			if(mortFlags.isSpecies()) {
				for(Species sp : rgroups.getAllSpecies()) {
					if(age < sp.getMax_age()) {
						fields.add(String.format("%d", sp.kills[age]));
					} else {
						fields.add("");
					}
				}
			}
			lines += fieldsToLine(fields, true);
			fields.clear();
		}
		PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(globals.mort.year.toFile(), true)));
		out.println(lines);
		out.close();
		globals.mort.year = null;
	}
	
	public void outputAllMorts() throws IOException {
		if(!mortFlags.isSummary())
			return;
		
		String lines = "";
		List<String> fields = new ArrayList<String>();
		
		lines += getMortFirstLine();
		//print one line of establishments
		fields.add("Estabs");
		if(mortFlags.isGroup()) {
			for(ResourceGroup rg : rgroups) {
				fields.add(String.format("%5.1f", getAvg(gestab.get(rg.getGrp_num()).s.get(0))));
			}
		}
		if(mortFlags.isSpecies()) {
			for(Species sp : rgroups.getAllSpecies()) {
				fields.add(String.format("%5.1f", getAvg(sestab.get(sp.getSp_num()).s.get(0))));
			}
		}
		lines += fieldsToLine(fields, true);
		fields.clear();
		
		//print one line of kill frequencies per age

		for (int age = 0; age < globals.Max_Age; age++) {
			fields.add(String.format("%d", age + 1));
			if (mortFlags.isGroup()) {
				for (ResourceGroup rg : rgroups) {
					fields.add(String.format("%5.1f", (age < rg.getMax_age()) ? getAvg(gmort.get(rg.getGrp_num()).s.get(age)) : 0.0f));
				}
			}
			if (mortFlags.isSpecies()) {
				for (Species sp : rgroups.getAllSpecies()) {
					fields.add(String.format("%5.1f", (age < sp.getMax_age()) ? getAvg(smort.get(sp.getSp_num()).s.get(age)) : 0.0f));
				}
			}
			lines += fieldsToLine(fields, true);
			fields.clear();
		}
		
		PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(Paths.get(globals.prjDir, globals.files[Globals.F_MortAvg]).toFile(), true)));
		out.print(lines);
		out.close();
	}
	
	public void outputAllBmass() throws IOException {
		if(!bmassFlags.isSummary()) {
			return;
		}
		List<String> fields = new ArrayList<String>();
		String lines = "";
		
		if(bmassFlags.isHeader()) {
			lines += makeHeader();
		}
		
		for(int yr=1; yr <= globals.runModelYears; yr++) {
			if(bmassFlags.isYr())
				fields.add(String.format("%d", yr));
			if(bmassFlags.isDist())
				fields.add(String.format("%d", dist.s.get(yr-1).nobs));
			if(bmassFlags.isPpt()) {
				fields.add(String.format("%f", getAvg( ppt.s.get(yr-1) )));
				fields.add(String.format("%f", getStd( ppt.s.get(yr-1) )));
			}
			if(bmassFlags.isPclass())
				fields.add(String.format("NA"));
			if(bmassFlags.isTmp()) {
				fields.add(String.format("%f", getAvg( temp.s.get(yr-1) )));
				fields.add(String.format("%f", getStd( temp.s.get(yr-1) )));
			}
			if(bmassFlags.isGrpb()) {
				for (ResourceGroup rg : rgroups) {
					fields.add(String.format("%f", getAvg(grp.get(rg.getGrp_num()).s.get(yr-1))));
					if(bmassFlags.isSize())
						fields.add(String.format("%f", getAvg(gsize.get(rg.getGrp_num()).s.get(yr-1))));
					if(bmassFlags.isPr()) {
						fields.add(String.format("%f", getAvg(gpr.get(rg.getGrp_num()).s.get(yr-1))));
						fields.add(String.format("%f", getStd(gpr.get(rg.getGrp_num()).s.get(yr-1))));
					}
				}
			}
			if(bmassFlags.isSppb()) {
				for(Species sp : rgroups.getAllSpecies()) {
					fields.add(String.format("%f", getAvg(spp.get(sp.getSp_num()).s.get(yr-1))));
					if(bmassFlags.isIndv()) 
						fields.add(String.format("%f", getAvg(indv.get(sp.getSp_num()).s.get(yr-1))));
				}
			}
			lines += fieldsToLine(fields, true);
			fields.clear();
		}
		
		PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(Paths.get(globals.prjDir, globals.files[Globals.F_BMassAvg]).toFile(), true)));
		out.println(lines);
		out.close();
	}
	
	//public void outputSeedDispersal()
	
	private String makeHeader() {
		List<String> fields = new ArrayList<String>();
		
		//set up header
		if(bmassFlags.isYr())
			fields.add("Year");
		if(bmassFlags.isDist())
			fields.add("Disturbs");
		if(bmassFlags.isPpt()) {
			fields.add("PPT");
			fields.add("StdDev");
		}
		if(bmassFlags.isPclass())
			fields.add("PPTClass");
		if(bmassFlags.isTmp()) {
			fields.add("Temp");
			fields.add("StdDev");
		}
		if(bmassFlags.isGrpb()) {
			for (ResourceGroup rg : rgroups) {
				fields.add(rg.getName());
				if(bmassFlags.isSize()) {
					fields.add(rg.getName() + "_RSize");
				}
				if(bmassFlags.isPr()) {
					fields.add(rg.getName() + "_PR");
					fields.add(rg.getName() + "_PRstd");
				}
			}
		}
		if(bmassFlags.isSppb()) {
			for(Species sp : rgroups.getAllSpecies()) {
				fields.add(sp.getName());
				if(bmassFlags.isIndv())
					fields.add(sp.getName() + "_Indivs");
			}
		}
		
		return fieldsToLine(fields, true);
	}
}
