package stepwat.internal;

public class BmassFlags {
	public boolean summary,  /* if FALSE, print no biomass output */
    yearly, /* print individual yearly runs as well as average */
    header,
    yr,
    dist,
    ppt,
    pclass,
    tmp,
    grpb,
    pr,
    size,
    sppb,
    indv;
	String sep;
	
	public void setInput(stepwat.input.ST.BmassFlags bmassFlags) {
		this.summary = bmassFlags.sumry;
		this.yearly = bmassFlags.yearly;
		this.header = bmassFlags.header;
		this.yr = bmassFlags.yrnum;
		this.dist = bmassFlags.disturb;
		this.ppt = bmassFlags.ppt;
		this.pclass = bmassFlags.pclass;
		this.tmp = bmassFlags.temp;
		this.grpb = bmassFlags.grpBmass;
		this.pr = bmassFlags.grpPR;
		this.size = bmassFlags.grpSize;
		this.sppb = bmassFlags.sppBmass;
		this.indv = bmassFlags.indivs;
		sep = bmassFlags.sep;
		if(bmassFlags.sep.compareTo("t") == 0)
			sep = "\t";
		if(bmassFlags.sep.compareTo("s") == 0)
			sep = " ";
	}
}
