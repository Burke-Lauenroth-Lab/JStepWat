package stepwat;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import stepwat.LogFileIn.LogMode;
import stepwat.input.ST.ST_Input;
import stepwat.internal.*;

public class Control {
	private boolean beenhere = false;
	private boolean logged; /* indicator that err file was written to */

	private RGroups rGroups;

	private Mortality mort;
	private Succulent Succulent;
	private Environs Env;
	private Plot Plot;
	private Globals globals;
	private BmassFlags BmassFlags;
	private MortFlags MortFlags;
	private InitParams initParams;
	private Stats stats;
	private Output output;

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

	public Control() {
		initParams = new InitParams();
		Path currentRelativePath = Paths.get("");
		initParams.workingDirectory = currentRelativePath.toAbsolutePath()
				.toString();
		initParams.filesInRelativePath = "files.in";
		initParams.EchoInits = false;
		initParams.QuietMode = false;
		initParams.UseGrid = false;
		initParams.UseSoilwat = false;

		init();

	}

	public Control(String[] args) throws Exception {
		initParams = new InitParams();
		init_args(args, initParams);

		init();
		
		ST_Input input = new ST_Input(this.initParams.workingDirectory, this.initParams.filesInRelativePath);
		input.readInputData();
		input.verify();
		setInput(input);
	}

	public Control(InitParams initParams) throws Exception {
		this.initParams = new InitParams();
		this.initParams.workingDirectory = initParams.workingDirectory;
		this.initParams.filesInRelativePath = initParams.filesInRelativePath;
		this.initParams.EchoInits = initParams.EchoInits;
		this.initParams.QuietMode = initParams.QuietMode;
		this.initParams.UseGrid = initParams.UseGrid;
		this.initParams.UseSoilwat = initParams.UseSoilwat;

		init();
		
		ST_Input input = new ST_Input(this.initParams.workingDirectory, this.initParams.filesInRelativePath);
		input.readInputData();
		input.verify();
		setInput(input);
	}
	
	public Control(ST_Input input) throws Exception {
		input.verify();
		
		initParams = new InitParams();
		initParams.workingDirectory = input.prjDir;
		initParams.filesInRelativePath = input.filesInName;
		initParams.EchoInits = false;
		initParams.QuietMode = false;
		initParams.UseGrid = false;
		initParams.UseSoilwat = false;
		
		init();
		setInput(input);
	}

	private void init() {
		// Initialize all internal objects and give references
		globals = new Globals();
		globals.prjDir = initParams.workingDirectory;
		Plot = new Plot();
		Succulent = new Succulent();
		BmassFlags = new BmassFlags(globals);
		MortFlags = new MortFlags(globals);
		Env = new Environs(globals, Plot, Succulent);
		rGroups = new RGroups(globals, Plot, Succulent, Env, MortFlags);
		mort = new Mortality(globals, Plot, Succulent, Env, rGroups);

		
		stats = new Stats(globals, Plot, Env, rGroups, BmassFlags, MortFlags);
		output = new Output(globals, Plot, Env, rGroups, BmassFlags, MortFlags);
	}

	private void setInput(ST_Input in) throws Exception {

		globals.setInput(initParams.workingDirectory,
				initParams.filesInRelativePath, in.files, in.model,
				in.environment, in.plot, in.rGroup);
		globals.setGrpCount(in.rGroup.groups.size());
		globals.setSppCount(in.species.speciesParams.size());

		BmassFlags.setInput(in.bmassFlags);
		MortFlags.setInput(in.mortFlags);

		rGroups.setInputs(in.rGroup, in.species, in.mortFlags.species,
				in.mortFlags.group);
		/*
		 * Writes to Bmass Globals.header
		 */
		if (BmassFlags.isHeader()) {
			List<String> headerLines = new ArrayList<String>();
			if (BmassFlags.isYr())
				headerLines.add("Year");
			if (BmassFlags.isDist())
				headerLines.add("Disturb");
			if (BmassFlags.isPpt())
				headerLines.add("PPT");
			if (BmassFlags.isPclass())
				headerLines.add("PPTClass");
			if (BmassFlags.isTmp())
				headerLines.add("Temp");

			if (BmassFlags.isGrpb()) {
				for (int rg = 0; rg < globals.getGrpCount(); rg++) {
					headerLines.add(rGroups.get(rg).getName());
					if (BmassFlags.isSize()) {
						headerLines.add(rGroups.get(rg).getName() + "_RSize");
					}
					if (BmassFlags.isPr()) {
						headerLines.add(rGroups.get(rg).getName() + "_PR");
					}
				}
			}

			if (BmassFlags.isSppb()) {
				for (int sp = 0; sp < globals.getSppCount(); sp++) {
					headerLines.add(rGroups.getAllSpecies().get(sp).getName());
					if (BmassFlags.isIndv()) {
						headerLines.add(rGroups.getAllSpecies().get(sp)
								.getName()
								+ "_Indv");
					}
				}
			}
			String headerLine = "";
			for (int i = 0; i < headerLines.size() - 1; i++) {
				headerLine += headerLines.get(i) + BmassFlags.getSep();
			}
			headerLine += headerLines.get(headerLines.size() - 1) + "\n";
			globals.bmass.setHeaderLine(headerLine);
		}

		/*
		 * Writes to Mort Globals.header
		 */
		if (MortFlags.isHeader()) {
			List<String> fields = new ArrayList<String>();
			fields.add("Age");
			if (MortFlags.isGroup()) {
				for (int rg = 0; rg < globals.getGrpCount(); rg++) {
					fields.add(rGroups.get(rg).getName());
				}
			}
			if (MortFlags.isSpecies()) {
				for (int sp = 0; sp < globals.getSppCount(); sp++) {
					fields.add(rGroups.getAllSpecies().get(sp).getName());
				}
			}
			String headerLine = "";
			for (int i = 0; i < fields.size() - 1; i++) {
				headerLine += fields.get(i) + MortFlags.getSep();
			}
			headerLine += fields.get(fields.size() - 1) + "\n";
			globals.mort.setHeaderLine(headerLine);
		}

		/*
		 * Resets the random number seed. The seed is set to negative when this
		 * routine is called, so the generator routines ( eg, RandUni()) can
		 * tell that it has changed. If called with seed==0, _randseed is reset
		 * from process time. '% 0xffff' is due to a bug in RandUni() that conks
		 * if seed is too large; should be removed in the near future.
		 */
		globals.random.RandSeed(globals.getRandseed());
	}

	public void run() throws Exception {

		int year, iter, incr;
		boolean killedany;

		logged = false;

		// if(this.initParams.UseGrid == true) {
		// runGrid();
		// return;
		// }

		parm_Initialize(0);

		// if (this.initParams.UseSoilwat)
		// SXW_Init(true);

		incr = (int) ((float) globals.getRunModelIterations() / 10);
		if (incr == 0)
			incr = 1;

		// --- Begin a new iteration ------
		for (iter = 1; iter <= globals.getRunModelIterations(); iter++) {
			System.out.println(iter);

			if (BmassFlags.isYearly() || MortFlags.isYearly())
				parm_Initialize(iter);

			Plot_Initialize();
			globals.setCurrIter(iter);

			// ------ Begin running the model ------
			for (year = 1; year <= globals.getRunModelYears(); year++) {

				// printf("Iter=%d, Year=%d\n", iter, year);
				globals.setCurrYear(year);

				rGroups.establish(); // excludes annuals
				Env.generate(rGroups);
				rGroups.partResources();
				rGroups.grow();

				// #ifdef STEPWAT
				// if (!isnull(SXW.debugfile) ) SXW_PrintDebug();
				// #endif

				killedany = mort.mortalityMain();
				rGroups.incrAges();
				stats.collect(year);

				if (BmassFlags.isYearly())
					output.bmassYearly(year);
				mort.endOfYear();
			} // end model run for this year

			if(BmassFlags.isYearly())
				globals.bmass.setYear(null);
			if (MortFlags.isSummary()) {
				stats.collectGMort();
				stats.collectSMort();
			}

			if (MortFlags.isYearly())
				output.mortYearly();

		} /* end model run for this iteration */

		if (MortFlags.isSummary())
			stats.outputAllMorts();
		if (BmassFlags.isSummary())
			stats.outputAllBmass();

		System.out.println();
		return;
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
		 //valid options
		@SuppressWarnings("unused")
		String str, opts[] = { "-d", "-f", "-q", "-s", "-e", "-p", "-g" };
		int valopts[] = { 1, 1, 0, -1, 0, 0, 0 }; /* indicates options with values */
		/* 0=none, 1=required, -1=optional */
		int i, /* looper through all cmdline arguments */
		a, /* current valid argument-value position */
		op, /* position number of found option */
		nopts = opts.length;
		boolean lastop_noval = false;

		/* Defaults */
		initParams.UseSoilwat = initParams.QuietMode = initParams.EchoInits = globals.UseSeedDispersal = false;
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
				} else if (!args[a + 1].startsWith("-")) { /*
															 * space betw
															 * opt-value
															 */
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
				// Path path = Paths.get(str);
				// if (Files.notExists(path)) {
				// System.err.println("Invalid project directory " + str);
				// }
				break;
			case 1:
				// parm_SetFirstName(str);
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
				globals.UseProgressBar = true;
				break;
			case 6:
				initParams.UseGrid = true;
				break; /* -g */

			default:
				System.err
						.println("Programmer: bad option in main:init_args:switch");
			}

			a++; /* move to next valid option-value position */

		} /* end for(i) */

	}

	void parm_Initialize(int iter) throws Exception {
		LogFileIn f = stepwat.LogFileIn.getInstance();
		String filename;
		if (beenhere) {
			if (BmassFlags.isYearly()) {
				filename = String.format("%s%0"+globals.bmass.getSuffixwidth()+"d.out", globals.getFiles()[Globals.F_BMassPre], iter);
				if(globals.bmass.getYear() != null) {
					f.LogError(LogMode.FATAL, "Programmer error: Globals.bmass.fp_year not null in parm_Initialize()");
				}
				globals.bmass.setYear(Paths.get(initParams.workingDirectory, filename));
				
				PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(globals.bmass.getYear().toFile(), false)));
				out.println(globals.bmass.getHeaderLine()+"\n");
				out.close();
			}
			if (MortFlags.isYearly()) {
				filename = String.format("%s%0"+globals.mort.getSuffixwidth()+"d.out", globals.getFiles()[Globals.F_MortPre], iter);
				if(globals.mort.getYear() != null) {
					f.LogError(LogMode.FATAL, "Programmer error: Globals.mort.fp_year not null in parm_Initialize()");
				}
				globals.mort.setYear(Paths.get(initParams.workingDirectory, filename));
				
				PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(globals.mort.getYear().toFile(), false)));
				out.println(globals.mort.getHeaderLine()+"\n");
				out.close();
			}
		} else {
			rGroups.checkSpecies();
			this.beenhere = true;
		}
	}

	private void Plot_Initialize() throws Exception {
		LogFileIn f = stepwat.LogFileIn.getInstance();
		/*
		 * Clear remaining individuals and resource counters
		 */
		for (Species sp : rGroups.getAllSpecies()) {
			if (!sp.isUse_me())
				continue;

			// reset extirpated RGroups' species, if any
			if (sp.getRes_grp().isExtirpated()) {
				sp.setSeedling_estab_prob(sp.getSeedling_estab_prob_old());
			}

			// clear estab and kills information
			if (sp.getKills() != null) {
				for (int i = 0; i < sp.getKills().length; i++) {
					sp.getKills()[i] = 0;
				}
			}

			// Kill all individuals of each species.
			// This should zero everything necessary (inc. estab&kilz)
			sp.kill();

			// TODO: programmer alert: INVESTIGATE WHY THIS OCCURS
			if (Float.compare(sp.getRelsize(), 0) != 0) {
				f.LogError(
						LogMode.NOTE,
						sp.getName() + " relsize "
								+ String.valueOf(sp.getRelsize())
								+ " forced in Plot_Initialize to 0.");
				sp.setRelsize(0);
			}
			if (sp.getEst_count() != 0) {
				f.LogError(
						LogMode.NOTE,
						sp.getName() + " est_count "
								+ String.valueOf(sp.getEst_count())
								+ " forced in Plot_Initialize to 0.");
				sp.clearEstIndv();
			}
		}

		for (ResourceGroup rg : rGroups) {
			if (!rg.isUse_me())
				continue;

			// Clearing kills-accounting for survival data
			if (rg.getKills() != null) {
				for (int i = 0; i < rg.getKills().length; i++)
					rg.getKills()[i] = 0;
			}
			// THIS NEVER SEEMS TO OCCUR
			if (rg.getEst_count() != 0) {
				f.LogError(
						LogMode.NOTE,
						rg.getName() + " est_count "
								+ String.valueOf(rg.getEst_count())
								+ " forced in Plot_Initialize to 0.");
				rg.clearEstSpecies();
			}
			rg.setYrs_neg_pr(0);
			rg.setExtirpated(false);
		}
		//TODO : finish
		// if(this.initParams.UseSoilwat)
		// SXW_InitPlot();
	}

}
