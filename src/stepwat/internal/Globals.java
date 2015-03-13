package stepwat.internal;

import java.nio.file.Path;

import stepwat.input.ST.Environment;
import stepwat.input.ST.Files;
import stepwat.input.ST.Model;
import stepwat.input.ST.Plot;
import stepwat.input.ST.Rgroup;

public class Globals {
	public static final int NFILES=14;
	
	public static final int F_First=0;
	public static final int F_Log=1;
	public static final int F_Model=2;
	public static final int F_Env=3;
	public static final int F_Plot=4;
	public static final int F_RGroup=5;
	public static final int F_Species=6;
	public static final int F_BMassFlag=7;
	public static final int F_BMassPre=8;
	public static final int F_BMassAvg=9;
	public static final int F_MortFlag=10;
	public static final int F_MortPre=11;
	public static final int F_MortAvg=12;
	public static final int F_SXW=13;
	public static final int F_EXE=14;
	
	public static final int Intcpt=0;
	public static final int Slope=1;
	public static final int P0=0;
	public static final int P1=1;
	public static final int P2=2;
	public static final int P3=3;
	public static final int P4=4;
	
	public static final int NoSeason=-1;
	public static final int CoolSeason=0;
	public static final int WarmSeason=1;
	
	public class PPT {
		protected float avg, std;
		protected int min, max, dry, wet;
	}

	public class Temperature {
		protected float avg, std, min, max;
	}

	public class Fecalpats {
		protected boolean use;
		protected float occur, removal; /* 1 elem. for slope and intercept */
		protected float[] recol;
		
		protected Fecalpats() {
			recol = new float[2];
		}
	}

	public class AntMounds {
		protected boolean use;
		protected float occur;
		protected int minyr, maxyr;
	}

	public class Burrows {
		protected boolean use;
		protected float occur;
		protected int minyr;
	}
	
	public class OutFiles {
		protected Path year;
		protected Path sumry;
		protected int suffixwidth;
		protected String headerLine;
		
		public Path getYear() {
			return year;
		}
		public void setYear(Path year) {
			this.year = year;
		}
		public Path getSumry() {
			return sumry;
		}
		public void setSumry(Path sumry) {
			this.sumry = sumry;
		}
		public int getSuffixwidth() {
			return suffixwidth;
		}
		public void setSuffixwidth(int suffixwidth) {
			this.suffixwidth = suffixwidth;
		}
		public String getHeaderLine() {
			return headerLine;
		}
		public void setHeaderLine(String headerLine) {
			this.headerLine = headerLine;
		}
	}

	public Rand random = new Rand();
	public PPT ppt = new PPT();
	public Temperature temp = new Temperature();
	public Fecalpats pat = new Fecalpats();
	public AntMounds mound = new AntMounds();
	public Burrows burrow = new Burrows();
	public OutFiles bmass = new OutFiles(), mort = new OutFiles();
	
	/**
	 * Use SoilWat
	 */
	protected boolean UseSoilwat;
	/**
	 * Use Grid option
	 */
	protected boolean UseGrid;
	/**
	 * Use Seed Dispersal
	 */
	public boolean UseSeedDispersal;
	/**
	 * Use Progress Bar
	 */
	public boolean UseProgressBar;
	/**
	 * The directory to project folder
	 */
	public String prjDir;
	/**
	 * Where all the files.in paths are found.
	 * Access with static final ints with name of
	 * the file name you want.
	 */
	protected String[] files = new String[NFILES];
	/**
	 * size of plot in square meters
	 */
	protected float plotsize;
	/**
	 * proportion of ppt during growing season
	 */
	protected float gsppt_prop;
	/**
	 * three parms for Warm/Cool growth mod
	 */
	protected float[][] tempparm;
	/**
	 * number of years to run the model
	 */
	protected int runModelYears;
	/**
	 * oldest plant; same as runModelYears for now
	 */
	protected int Max_Age;
	/**
	 * run model this many times for statistics
	 */
	protected int currYear, runModelIterations;
	/**
	 * number of groups defined
	 */
	protected int currIter, grpCount;
	/**
	 * number of species defined
	 */
	protected int sppCount;
	/**
	 * max species groups that can successfully
	 * establish in a year
	 */
	protected int grpMaxEstab;
	/**
	 * number of cells to use in Grid, only applicable if grid function is being used
	 */
	protected int nCells;
	/**
	 * random seed from input file
	 */
	protected int randseed;
	
	public Globals() {
		tempparm = new float[2][3];
	}
	
	public void setInput(String prjDir, String filesIn, Files files, Model model, Environment envir, Plot plot, Rgroup rGroup) {
		//Files Inputs
		this.files[F_First] = filesIn;
		this.files[F_Log] = files.logfile;
		this.files[F_Model] = files.model;
		this.files[F_Env] = files.env;
		this.files[F_Plot] = files.plot;
		this.files[F_RGroup] = files.rgroup;
		this.files[F_Species] = files.species;
		this.files[F_BMassFlag] = files.bmassflags;
		this.files[F_BMassPre] = files.bmasspre;
		this.files[F_BMassAvg] = files.bmassavg;
		this.files[F_MortFlag] = files.mortflags;
		this.files[F_MortPre] = files.mortpre;
		this.files[F_MortAvg] = files.mortavg;
		this.files[F_SXW] = files.sxw;
		//Set Model Inputs
		this.setRunModelYears(model.nYears);
		this.setMax_Age(model.nYears);
		this.setRunModelIterations(model.nIterations);
		this.bmass.suffixwidth = this.mort.suffixwidth = String.valueOf(model.nIterations).length();
		this.setRandseed(model.seed==0?0:-Math.abs(model.seed));
		//Set Envir Inputs
		this.ppt.avg = envir.precip.avg;
		this.ppt.std = envir.precip.std;
		this.ppt.min = envir.precip.min;
		this.ppt.max = envir.precip.max;
		this.ppt.dry = envir.precip.dry;
		this.ppt.wet = envir.precip.wet;
		this.setGsppt_prop(envir.precip.gsppt);
		
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
		this.setPlotsize(plot.plotsize);
		//rGroup
		this.setGrpMaxEstab(rGroup.nGrpEstab);
	}
	
	public boolean useSoilWat() {
		return this.UseSoilwat;
	}
	
	public void setUseSoilWat(boolean use) {
		this.UseSoilwat = use;
	}

	public String[] getFiles() {
		return files;
	}

	public void setFiles(String[] files) {
		this.files = files;
	}

	public float getPlotsize() {
		return plotsize;
	}

	public void setPlotsize(float plotsize) {
		this.plotsize = plotsize;
	}

	public float getGsppt_prop() {
		return gsppt_prop;
	}

	public void setGsppt_prop(float gsppt_prop) {
		this.gsppt_prop = gsppt_prop;
	}

	public int getRunModelYears() {
		return runModelYears;
	}

	public void setRunModelYears(int runModelYears) {
		this.runModelYears = runModelYears;
	}

	public int getMax_Age() {
		return Max_Age;
	}

	public void setMax_Age(int max_Age) {
		Max_Age = max_Age;
	}

	public int getRunModelIterations() {
		return runModelIterations;
	}

	public void setRunModelIterations(int runModelIterations) {
		this.runModelIterations = runModelIterations;
	}

	public int getCurrYear() {
		return currYear;
	}

	public void setCurrYear(int currYear) {
		this.currYear = currYear;
	}

	public int getCurrIter() {
		return currIter;
	}

	public void setCurrIter(int currIter) {
		this.currIter = currIter;
	}

	public int getGrpCount() {
		return grpCount;
	}

	public void setGrpCount(int grpCount) {
		this.grpCount = grpCount;
	}

	public int getSppCount() {
		return sppCount;
	}

	public void setSppCount(int sppCount) {
		this.sppCount = sppCount;
	}

	public int getGrpMaxEstab() {
		return grpMaxEstab;
	}

	public void setGrpMaxEstab(int grpMaxEstab) {
		this.grpMaxEstab = grpMaxEstab;
	}

	public int getnCells() {
		return nCells;
	}

	public void setnCells(int nCells) {
		this.nCells = nCells;
	}

	public int getRandseed() {
		return randseed;
	}

	public void setRandseed(int randseed) {
		this.randseed = randseed;
	}
}
