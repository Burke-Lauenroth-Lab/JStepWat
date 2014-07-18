package stepwat.internal;

import java.nio.file.Path;

import stepwat.input.ST.Environment;
import stepwat.input.ST.Model;
import stepwat.input.ST.Plot;
import stepwat.input.ST.Rgroup;

public class Globals {
	
	public static final int Intcpt=0;
	public static final int Slope=1;
	public static final int P0=0;
	public static final int P1=0;
	public static final int P2=0;
	public static final int P3=0;
	public static final int P4=0;
	
	public static final int NoSeason=-1;
	public static final int CoolSeason=0;
	public static final int WarmSeason=0;
	
	public class PPT {
		public float avg, std;
		public int min, max, dry, wet;
	}

	public class Temperature {
		public float avg, std, min, max;
	}

	public class Fecalpats {
		public boolean use;
		public float occur, removal, recol[]; /* 1 elem. for slope and intercept */

		public Fecalpats() {
			recol = new float[2];
		}
	}

	public class AntMounds {
		public boolean use;
		public float occur;
		public int minyr, maxyr;
	}

	public class Burrows {
		public boolean use;
		public float occur;
		public int minyr;
	}
	
	public class OutFiles {
		Path year;
		Path sumry;
		int suffixwidth;
		String headerLine;
	}

	public PPT ppt = new PPT();
	public Temperature temp = new Temperature();
	public Fecalpats pat = new Fecalpats();
	public AntMounds mound = new AntMounds();
	public Burrows burrow = new Burrows();
	public OutFiles bmass = new OutFiles(), mort = new OutFiles();

	public float plotsize, /* size of plot in square meters */
	gsppt_prop, /* proportion of ppt during growing season */
	tempparm[][]; /* three parms for Warm/Cool growth mod */
	public int runModelYears, /* number of years to run the model */
	Max_Age, /* oldest plant; same as runModelYears for now */
	currYear, runModelIterations, /* run model this many times for statistics */
	currIter, grpCount, /* number of groups defined */
	sppCount, /* number of species defined */
	grpMaxEstab, /* max species groups that can successfully */
	/* establish in a year */
	nCells; /*
			 * number of cells to use in Grid, only applicable if grid function
			 * is being used
			 */
	public int randseed; /* random seed from input file */
	
	public Globals() {
		tempparm = new float[2][3];
	}
	
	public void setInput(Model model, Environment envir, Plot plot, Rgroup rGroup) {
		//Set Model Inputs
		this.Max_Age = this.runModelYears = model.nYears;
		this.runModelIterations = model.nIterations;
		this.bmass.suffixwidth = this.mort.suffixwidth = String.valueOf(model.nIterations).length();
		this.randseed = model.seed==0?0:-Math.abs(model.seed);
		//Set Envir Inputs
		this.ppt.avg = envir.precip.avg;
		this.ppt.std = envir.precip.std;
		this.ppt.min = envir.precip.min;
		this.ppt.max = envir.precip.max;
		this.ppt.dry = envir.precip.dry;
		this.ppt.wet = envir.precip.wet;
		this.gsppt_prop = envir.precip.gsppt;
		
		this.temp.avg = envir.temp.avg;
		this.temp.std = envir.temp.std;
		this.temp.min = envir.temp.min;
		this.temp.max = envir.temp.max;
		
		this.pat.occur = envir.fecalPats.occur;
		this.pat.removal = envir.fecalPats.removal;
		this.pat.recol[Slope] = envir.fecalPats.slope;
		this.pat.recol[Intcpt] = envir.fecalPats.intcpt;
		
		this.mound.occur = envir.antMounds.occur;
		this.mound.minyr = envir.antMounds.minyr;
		this.mound.maxyr = envir.antMounds.maxyr;
		
		this.burrow.occur = envir.antBurrows.occur;
		this.burrow.minyr = envir.antBurrows.minyr;
		
		this.tempparm[CoolSeason][0] = envir.growthModifiers.ctmpa;
		this.tempparm[CoolSeason][1] = envir.growthModifiers.ctmp1;
		this.tempparm[CoolSeason][2] = envir.growthModifiers.ctmp2;
		this.tempparm[WarmSeason][0] = envir.growthModifiers.wtmpa;
		this.tempparm[WarmSeason][1] = envir.growthModifiers.wtmp1;
		this.tempparm[WarmSeason][2] = envir.growthModifiers.wtmp2;
		
		this.pat.use = this.pat.use;
		this.mound.use = this.mound.use;
		this.burrow.use = this.burrow.use;
		//Set plot Input
		this.plotsize = plot.plotsize;
		//rGroup
		this.grpMaxEstab = rGroup.nGrpEstab;
	}
}
