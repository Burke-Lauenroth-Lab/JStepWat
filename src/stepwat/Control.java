package stepwat;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

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

	private boolean UseSoilwat; /* not used in every module */
	private boolean UseGrid;
	private boolean UseSeedDispersal;
	private boolean EchoInits;
	private boolean UseProgressBar;
	
	private boolean QuietMode;
	
	private boolean beenhere = false;
	
	public void start(String[] args) {

		  int year, iter, incr;
		  boolean killedany;

		  logged = false;
		  //atexit(check_log);
		  /* provides a way to inform user that something
		   * was logged.  see generic.h */


		  init_args(args);

		  if(UseGrid == true) {
		  	//runGrid();
		  	return;
		  }

		  parm_Initialize(0);

		  if (UseSoilwat)
		    SXW_Init(true);

		  incr = (int) ((float)Globals.runModelIterations/10);
		  if (incr == 0) incr = 1;

		  /* --- Begin a new iteration ------ */
		  for (iter = 1; iter <= Globals.runModelIterations; iter++) {
			  System.out.println(iter);
		    
		      if (BmassFlags.yearly || MortFlags.yearly)
		        parm_Initialize( iter);

		      Plot_Initialize();
		      Globals.currIter = iter;

		      /* ------  Begin running the model ------ */
		      for( year=1; year <= Globals.runModelYears; year++) {

		    	  /* printf("Iter=%d, Year=%d\n", iter, year);  */
		          Globals.currYear = year;

		          rgroup_Establish();  /* excludes annuals */
		          Env_Generate();
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
		      } /* end model run for this year*/

		      if (BmassFlags.yearly)
		        CloseFile(Globals.bmass.fp_year);
		      if (MortFlags.summary) {
		        stat_Collect_GMort();
		        stat_Collect_SMort();
		      }

		      if (MortFlags.yearly)
		        output_Mort_Yearly();

		  } /* end model run for this iteration*/

		 /*------------------------------------------------------*/
		  if (MortFlags.summary)
		    stat_Output_AllMorts( );
		  if (BmassFlags.summary)
		    stat_Output_AllBmass();

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
	
	void init_args(String[] args) {
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
		UseSoilwat = QuietMode = EchoInits = UseSeedDispersal = false;
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
				Path path = Paths.get(str);
				if (Files.notExists(path)) {
					System.err.println("Invalid project directory " + str);
				}
				break;
			case 1:
				parm_SetFirstName(str);
				break; /* -f */

			case 2:
				QuietMode = true;
				break; /* -q */

			case 3:
				UseSoilwat = true; /* -s */
				// SXW.debugfile = (char *) Str_Dup(str);
				break;

			case 4:
				EchoInits = true;
				break; /* -e */

			case 5: // progfp = stdout; /* -p */
				UseProgressBar = true;
				break;

			case 6:
				UseGrid = true;
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

		if (beenhere) {
			if (BmassFlags.yearly) {
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

			if (MortFlags.yearly) {
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

		} else {
			_globals_init();
			_files_init();
			_model_init();
			_env_init();
			_plot_init();
			_bmassflags_init();
			_mortflags_init();
			_rgroup_init();
			_species_init();
			_check_species();

			_bmasshdr_init();
			_morthdr_create();
			RandSeed(Globals.randseed);

			/* _recover_names(); */
			beenhere = true;
		}

	}
}
