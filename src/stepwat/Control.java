package stepwat;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import stepwat.LogFileIn.LogMode;
import stepwat.input.ST.Rgroup;
import stepwat.input.ST.ST_Input;
import stepwat.input.ST.Species.AnnualsParams;
import stepwat.input.ST.Species.SeedDispersalParam;
import stepwat.input.ST.Species.SpeciesProbParam;
import stepwat.internal.*;

public class Control {
	private String errstr;
	private String inbuf;

	private boolean logged; /* indicator that err file was written to */

	private List<Species> Species;
	private List<ResourceGroup> RGroup;
	private Succulent Succulent;
	private Environs Env;
	private Plot Plot;
	private Globals Globals;
	private BmassFlags BmassFlags;
	private MortFlags MortFlags;
	private InitParams initParams;
	
	public class InitParams {
		/**
		 * Project Working Directory<br>
		 * If not set, will default to programs launch directory.
		 */
		public String workingDirectory = "";
		/**
		 * Relative Path from PrjDir to files.in file.
		 */
		public String filesInRelativePath = "";
		/**
		 * quiet mode, don't print message to check logfile.
		 */
		public boolean QuietMode = false;
		/**
		 * use SOILWAT model for resource partitioning.
		 */
		public boolean UseSoilwat = false;
		/**
		 * echo initialization results to logfile
		 */
		public boolean EchoInits = false;
		/**
		 * use gridded mode
		 */
		public boolean UseGrid = false; 
	}
	
	private boolean UseSeedDispersal;
	private boolean UseProgressBar;
	
	private boolean beenhere = false;
	
	public Control() {
		initParams = new InitParams();
		Path currentRelativePath = Paths.get("");
		initParams.workingDirectory = currentRelativePath.toAbsolutePath().toString();
		initParams.filesInRelativePath = "files.in";
		initParams.EchoInits = false;
		initParams.QuietMode = false;
		initParams.UseGrid = false;
		initParams.UseSoilwat = false;
	}
	
	public Control(String[] args) {
		initParams = new InitParams();
		init_args(args, initParams);
	}
	
	public Control(InitParams initParams) {
		initParams = new InitParams();
		this.initParams.workingDirectory = initParams.workingDirectory;
		this.initParams.filesInRelativePath = initParams.filesInRelativePath;
		this.initParams.EchoInits = initParams.EchoInits;
		this.initParams.QuietMode = initParams.QuietMode;
		this.initParams.UseGrid = initParams.UseGrid;
		this.initParams.UseSoilwat = initParams.UseSoilwat;
	}
	
	private void setInput(ST_Input in) throws Exception {
		LogFileIn f = stepwat.LogFileIn.getInstance();
		
		Globals.setInput(initParams.filesInRelativePath, in.files, in.model, in.environment, in.plot, in.rGroup);
		BmassFlags.setInput(in.bmassFlags);
		MortFlags.setInput(in.mortFlags);
		
		RGroup = new ArrayList<ResourceGroup>(in.rGroup.groups.size());
		Globals.setGrpCount(in.rGroup.groups.size());
		for(int i=0; i<RGroup.size(); i++) {
			ResourceGroup rg = new ResourceGroup();
			int idIndex = -1;
			String name = "";
			for(int j=0; j<RGroup.size(); j++) {
				if(in.rGroup.groups.get(j).id == i) {
					idIndex = in.rGroup.groups.get(j).id;
					name = in.rGroup.groups.get(j).name;
				}
			}
			int rpi = in.rGroup.ResourceParams_Name2Index(name);
			int spi = in.rGroup.SucculentParams_Name2Index(name);
			boolean succulent;
			if(spi == -1) {
				succulent = false;
			} else {
				succulent = true;
				Succulent.setInput(in.rGroup.succulentParams.get(spi));
			}
			rg.setInput(in.rGroup.groups.get(idIndex), in.rGroup.resourceParameters.get(rpi), succulent);
			RGroup.add(i, rg);
		}
		
		Species = new ArrayList<Species>(in.species.speciesParams.size());
		Globals.setSppCount(in.species.speciesParams.size());
		for(int i=0; i<Species.size(); i++) {
			Species sp = new Species();
			int rgi = in.species.speciesParams.get(i).rg-1;
			int ai = in.species.Annuals_Name2Index(in.species.speciesParams.get(i).name);
			AnnualsParams aparams = ai==-1?null:in.species.annualsParams.get(ai);
			
			SpeciesProbParam sprobparam = in.species.speciesProbParam.get(in.species.SpeciesProb_Name2Index(in.species.speciesParams.get(i).name));
			SeedDispersalParam seedparam = in.species.seedDispersalParam.get(in.species.SeedDispersal_Name2Index(in.species.speciesParams.get(i).name));
			sp.setInput(in.species.speciesParams.get(i), RGroup.get(rgi), aparams, sprobparam, seedparam, i);
			Species.add(sp);
		}
		
		/*
		 * count and link species to their groups.
		 * print a message if more specified than available
		 */
		int cnt = 0;
		for(int rg=0; rg<Globals.getGrpCount(); rg++) {
			ResourceGroup g = RGroup.get(rg);
			for(int sp=0; sp<Globals.getSppCount(); sp++) {
				if(this.Species.get(sp).getRes_grp().getGrp_num() == rg) {
					g.getSpecies().add(Species.get(sp));
				}
			}
			g.setMax_spp(cnt);
			if(cnt < g.getMax_spp_estab()) {
				g.setMax_spp_estab(cnt);
				f.LogError(LogMode.NOTE, "Max_Spp_Estab > Number of Spp for "+g.getName()+".\nContinuing");
			}
		}
		/*
		 * determine max age for the species and
		 * keep track for group's max age
		 * and compute max g/m^2 for this group
		 * also, compute max_bmass for the group
		 */
		for(int rg=0; rg<Globals.getGrpCount(); rg++) {
			ResourceGroup g = RGroup.get(rg);
			int maxage=0;
			int minage=30000;
			g.setMax_bmass(0);
			for(Species s : g.getSpecies()) {
				if(s.getMax_age()==0)
					s.setMax_age(Globals.getRunModelYears());
				/* maxage shouldn't be set to extirp due to age-independent mortality.
				 extirp happens due to the flag, not age;
				 but I hesitate to remove the code, so I'll comment it out for now.
				 if(g.getExtirp() != 0 && g.getExtirp() < s.getMax_age())
					s.setMax_age(g.getExtirp());
				 */
				maxage = Math.max(s.getMax_age(), maxage);
				minage = Math.min(s.getMax_age(), minage);
				g.setMax_bmass(g.getMax_bmass()+s.getMature_biomass());
			}
			if(minage == 1 && maxage != 1) {
				f.LogError(LogMode.FATAL, RGroup.get(rg).getName()+": Can't mix annuals and perennials within a group\n"+
							"Refer to the groups.in and species.in files\n");
			}
			g.setMax_age(maxage);
		}
		/*
		 * check out the definitions for SppMaxAge and GrpMaxAge
		 * they're used here for some hoped-for sense of readability
		 */
		if(MortFlags.isSpecies()) {
			for(int sp=0; sp<Globals.getSppCount(); sp++) {
				if(Species.get(sp).isUse_me()) {
					Species.get(sp).setKills(new int[Species.get(sp).getMax_age()]);
				} else {
					Species.get(sp).setKills(null);
				}
			}
		} else {
			for(int sp=0; sp<Globals.getSppCount(); sp++) {
				Species.get(sp).setKills(null);
			}
		}
		
		if(MortFlags.isGroup()) {
			for(int rg=0; rg<Globals.getGrpCount(); rg++) {
				if(RGroup.get(rg).isUse_me()) {
					for(Species species : RGroup.get(rg).getSpecies()) {
						int max_age = species.getMax_age()>0 ? Math.max(species.getMax_age(), RGroup.get(rg).getMax_age()) : Globals.getMax_Age();
						RGroup.get(rg).setMax_age(max_age);
					}
					RGroup.get(rg).setKills(new int[RGroup.get(rg).getMax_age()]);
				} else {
					RGroup.get(rg).setKills(null);
				}
			}
		} else {
			for(int rg=0; rg<Globals.getGrpCount(); rg++) {
				RGroup.get(rg).setKills(null);
			}
		}
		
		/*
		 * Writes to Bmass Globals.header
		 */
		if(BmassFlags.isHeader()) {
			List<String> headerLines = new ArrayList<String>();
			if(BmassFlags.isYr())
				headerLines.add("Year");
			if(BmassFlags.isDist())
				headerLines.add("Disturb");
			if(BmassFlags.isPpt())
				headerLines.add("PPT");
			if(BmassFlags.isPclass())
				headerLines.add("PPTClass");
			if(BmassFlags.isTmp())
				headerLines.add("Temp");
			
			if(BmassFlags.isGrpb()) {
				for(int rg=0; rg<Globals.getGrpCount(); rg++) {
					headerLines.add(RGroup.get(rg).getName());
					if(BmassFlags.isSize()) {
						headerLines.add(RGroup.get(rg).getName()+"_RSize");
					}
					if(BmassFlags.isPr()) {
						headerLines.add(RGroup.get(rg).getName()+"_PR");
					}
				}
			}
			
			if(BmassFlags.isSppb()) {
				for(int sp=0; sp<Globals.getSppCount(); sp++) {
					headerLines.add(Species.get(sp).getName());
					if(BmassFlags.isIndv()) {
						headerLines.add(Species.get(sp).getName()+"_Indv");
					}
				}
			}
			String headerLine = "";
			for(int i=0; i<headerLines.size()-1; i++) {
				headerLine += headerLines.get(i) + BmassFlags.getSep();
			}
			headerLine+=headerLines.get(headerLines.size()-1)+"\n";
			Globals.bmass.setHeaderLine(headerLine);
		}
		
		/*
		 * Writes to Mort Globals.header
		 */
		if(MortFlags.isHeader()) {
			List<String> fields = new ArrayList<String>();
			fields.add("Age");
			if(MortFlags.isGroup()) {
				for(int rg=0; rg<Globals.getGrpCount(); rg++) {
					fields.add(RGroup.get(rg).getName());
				}
			}
			if(MortFlags.isSpecies()) {
				for(int sp=0; sp<Globals.getSppCount(); sp++) {
					fields.add(Species.get(sp).getName());
				}
			}
			String headerLine = "";
			for(int i=0; i<fields.size()-1; i++) {
				headerLine += fields.get(i) + MortFlags.getSep();
			}
			headerLine+=fields.get(fields.size()-1)+"\n";
			Globals.mort.setHeaderLine(headerLine);
		}
		
		/*
		 * Resets the random number seed.  The
		 * seed is set to negative when this routine
		 * is called, so the generator routines
		 * ( eg, RandUni()) can tell that it has
		 * changed.  If called with seed==0,
		 * _randseed is reset from process time.
		 * '% 0xffff' is due to a bug in RandUni()
		 * that conks if seed is too large; should
		 * be removed in the near future.
		 */
		Globals.random.RandSeed(Globals.getRandseed());
	}
	
	
	public void start() throws Exception {

		  int year, iter, incr;
		  boolean killedany;

		  logged = false;

		  //if(this.initParams.UseGrid == true) {
		  	//runGrid();
		  	//return;
		  //}
		  
		  ST_Input input = new ST_Input(this.initParams.workingDirectory, this.initParams.filesInRelativePath);
		  input.readInputData();
		  input.verify();
		  setInput(input);
		  
		  //parm_Initialize(0);

		  //if (this.initParams.UseSoilwat)
		    //SXW_Init(true);

		  incr = (int) ((float)Globals.getRunModelIterations()/10);
		  if (incr == 0) incr = 1;

		  // --- Begin a new iteration ------ 
		  for (iter = 1; iter <= Globals.getRunModelIterations(); iter++) {
			  System.out.println(iter);
		    
		      if (BmassFlags.isYearly() || MortFlags.isYearly())
		        parm_Initialize(iter);

		      Plot_Initialize();
		      Globals.setCurrIter(iter);

		      // ------  Begin running the model ------ 
		      for( year=1; year <= Globals.getRunModelYears(); year++) {

		    	  // printf("Iter=%d, Year=%d\n", iter, year);  
		          Globals.setCurrYear(year);

		          ResourceGroup.establish(Plot, Globals, RGroup);  // excludes annuals 
		          Env.generate(RGroup);
		          rgroup_PartResources();
		          rgroup_Grow();

		          //#ifdef STEPWAT
		          //if (!isnull(SXW.debugfile) ) SXW_PrintDebug();
		          //#endif

		          mort_Main(killedany);
		          rgroup_IncrAges();
		          stat_Collect(year);

		          if (BmassFlags.yearly)	output_Bmass_Yearly(year);
		          mort_EndOfYear();
		      } /* end model run for this year

		      if (BmassFlags.yearly)
		        CloseFile(Globals.bmass.fp_year);
		      if (MortFlags.summary) {
		        stat_Collect_GMort();
		        stat_Collect_SMort();
		      }

		      if (MortFlags.yearly)
		        output_Mort_Yearly();*/

		  } /* end model run for this iteration*/

		 /*------------------------------------------------------
		  if (MortFlags.summary)
		    stat_Output_AllMorts( );
		  if (BmassFlags.summary)
		    stat_Output_AllBmass();

		  System.out.println();
		  return;*/
	}
	
	public void start(ST_Input input) throws Exception {
		input.verify();
		setInput(input);
	}
	static void usage() {
		final String s = "STEPPE plant community dynamics (SGS-LTER Jan-04).\n"
				+ "Usage: steppe [-d startdir] [-f files.in] [-q] [-s] [-e] [-g]\n"
				+ "  -d : supply working directory (default=.)\n"
				+ "  -f : supply list of input files (default=files.in)\n"
				+ "  -q : quiet mode, don't print message to check logfile.\n"
				+ "  -s : use SOILWAT model for resource partitioning.\n"
				+ "  -e : echo initialization results to logfile\n"
				+ "  -g : use gridded mode\n";
		System.out.println(s);
	}
	
	private void init_args(String[] args, InitParams initParams) {
		/*
		 * to add an option: - include it in opts[] - set a flag in valopts
		 * indicating no value (0), value required (1), or value optional (-1),
		 * - then tell us what to do in the switch statement
		 * 
		 * 3/1/03 - cwb - Current options are -d=chg to work dir <opt=dir_name>
		 * -f=chg deflt first file <opt=file.in> -q=quiet, noprint
		 * "Check logfile" at end of program -s=soilwat model-derived resource
		 * optional parm debugfile for pgmr testing; see code. -e=echo init
		 * values to logfile. 1/8/04 - cwb - Added -p option to help the GUI
		 * with a progress bar. This is another "secret" option, insofar as the
		 * command-line user doesn't need it. The option directs the program to
		 * write progress info (iter) to stdout. Without the option, progress
		 * info (dots) is written to stderr.
		 */
		String str, opts[] = { "-d", "-f", "-q", "-s", "-e", "-p", "-g" }; /*
																			 * valid
																			 * options
																			 */
		int valopts[] = { 1, 1, 0, -1, 0, 0, 0 }; /* indicates options with values */
		/* 0=none, 1=required, -1=optional */
		int i, /* looper through all cmdline arguments */
		a, /* current valid argument-value position */
		op, /* position number of found option */
		nopts = opts.length;
		boolean lastop_noval = false;

		/* Defaults */
		// parm_SetFirstName("files.in");
		initParams.UseSoilwat = initParams.QuietMode = initParams.EchoInits = UseSeedDispersal = false;
		// SXW.debugfile = null;
		// progfp = stderr;

		a = 1;
		for (i = 1; i <= nopts; i++) {
			if (a >= args.length)
				break;

			/* figure out which option by its position 0-(nopts-1) */
			for (op = 0; op < nopts; op++) {
				if (opts[op].compareTo(args[a]) == 0)
					break; /* found it, move on */
			}
			if (op == nopts) {
				System.out.println("Invalid option " + args[a]);
				usage();
				// exit
			}
			if (a == args.length - 1 && args[a].length() == 2)
				lastop_noval = true;

			/* extract value part of option-value pair */
			if (valopts[op] != 0) {
				if (lastop_noval && valopts[op] < 0) {
					/* break out, optional value not available */
					/* avoid checking past end of array */
				} else if (lastop_noval && valopts[op] > 0) {
					System.out.println("Incomplete option " + opts[op]);
					usage();
					// exit(-1);

				} else if (args[a].length() == 2 && valopts[op] < 0) {
					/* break out, optional value not available */
				} else if (args[a].length() > 2) { /* no space betw opt-value */
					str = args[a].substring(2);
				} else if (!args[a + 1].startsWith("-")) { /* space betw opt-value */
					str = args[++a];
				} else if (0 < valopts[op]) { /* required opt-val not found */
					System.out.println("Incomplete option " + opts[op]);
					usage();
					// exit(-1);
				} /* opt-val not required */
			}

			/* set indicators/variables based on results */
			switch (op) {
			case 0: /* -d */
				//Path path = Paths.get(str);
				//if (Files.notExists(path)) {
				//	System.err.println("Invalid project directory " + str);
				//}
				break;
			case 1:
				//parm_SetFirstName(str);
				break; /* -f */

			case 2:
				initParams.QuietMode = true;
				break; /* -q */

			case 3:
				initParams.UseSoilwat = true; /* -s */
				// SXW.debugfile = (char *) Str_Dup(str);
				break;

			case 4:
				initParams.EchoInits = true;
				break; /* -e */

			case 5: // progfp = stdout; /* -p */
				UseProgressBar = true;
				break;

			case 6:
				initParams.UseGrid = true;
				break; /* -g */

			default:
				System.err.println("Programmer: bad option in main:init_args:switch");
			}

			a++; /* move to next valid option-value position */

		} /* end for(i) */

	}
	
	void parm_Initialize(int iter) {
		/* ====================================================== */

		String filename;

		if (BmassFlags.isYearly()) {
			// System.out.println(Parm_name(F_BMassPre) +
			// Globals.bmass.suffixwidth, iter);
			// if (Globals.bmass.fp_year != NULL) {
			// LogError(logfp, LOGFATAL, "Programmer error: "
			// "Globals.bmass.fp_year not null"
			// " in parm_Initialize()");
			// }
			// Globals.bmass.fp_year = OpenFile(filename, "w");
			// fprintf(Globals.bmass.fp_year, "%s\n",
			// Globals.bmass.header_line);
		}

		if (MortFlags.isYearly()) {
			// sprintf(filename, "%s%0*d.out", Parm_name(F_MortPre),
			// Globals.mort.suffixwidth,
			// iter);
			// if (Globals.mort.fp_year != NULL) {
			// LogError(logfp, LOGFATAL, "Programmer error: "
			// "Globals.mort.fp_year not null"
			// " in parm_Initialize()");
			// }
			// Globals.mort.fp_year = OpenFile(filename, "w");
			// fprintf(Globals.mort.fp_year, "%s\n",
			// Globals.mort.header_line);
		}

	}
	
	private void Plot_Initialize() throws Exception {
		LogFileIn f = stepwat.LogFileIn.getInstance();
		/*
		 * Clear remaining individuals and resource counters
		 */
		for(int sp=0; sp<Globals.getSppCount(); sp++) {
			if(!Species.get(sp).isUse_me())
				continue;
			
			// reset extirpated RGroups' species, if any
			if(Species.get(sp).getRes_grp().isExtirpated()) {
				Species.get(sp).setSeedling_estab_prob(Species.get(sp).getSeedling_estab_prob_old());
			}
			
			//clear estab and kills information
			if(Species.get(sp).getKills() != null) {
				for(int i=0; i<Species.get(sp).getKills().length; i++) {
					Species.get(sp).getKills()[i] = 0;
				}
			}
			
			//Kill all individuals of each species.
			//This should zero everything necessary (inc. estab&kilz)
			Species.get(sp).kill();
			
			//TODO: programmer alert: INVESTIGATE WHY THIS OCCURS
			if(Float.compare(Species.get(sp).getRelsize(), 0) != 0) {
				f.LogError(LogMode.NOTE, Species.get(sp).getName()+" relsize "+String.valueOf(Species.get(sp).getRelsize())+" forced in Plot_Initialize to 0.");
				Species.get(sp).setRelsize(0);
			}
			if(Species.get(sp).getEst_count() != 0) {
				f.LogError(LogMode.NOTE, Species.get(sp).getName()+" est_count "+String.valueOf(Species.get(sp).getEst_count())+" forced in Plot_Initialize to 0.");
				//Species.get(sp).setEst_count(0);
			}
		}
		
		for(int rg=0; rg<Globals.getGrpCount(); rg++) {
			if(!RGroup.get(rg).isUse_me())
				continue;
			
			//Clearing kills-accounting for survival data
			if(RGroup.get(rg).getKills() != null) {
				for(int i=0; i<RGroup.get(rg).getKills().length; i++)
					RGroup.get(rg).getKills()[i] = 0;
			}
			//THIS NEVER SEEMS TO OCCUR
			if(RGroup.get(rg).getEst_count() != 0) {
				f.LogError(LogMode.NOTE, RGroup.get(rg).getName()+" est_count "+String.valueOf(RGroup.get(rg).getEst_count())+" forced in Plot_Initialize to 0.");
				RGroup.get(rg).setEst_count(0);
			}
			RGroup.get(rg).setYrs_neg_pr(0);
			RGroup.get(rg).setEst_count(0);
		}
		//if(this.initParams.UseSoilwat)
			//SXW_InitPlot();
	}
	
}
