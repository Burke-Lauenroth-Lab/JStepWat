package stepwat.internal;

public class Globals {
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

	public PPT ppt;
	public Temperature temp;
	public Fecalpats pat;
	public AntMounds mound;
	public Burrows burrow;

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
}
