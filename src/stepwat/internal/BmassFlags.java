package stepwat.internal;

public class BmassFlags {
	private boolean summary,  /* if FALSE, print no biomass output */
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
	private String sep;
	
	public boolean isSummary() {
		return summary;
	}

	public void setSummary(boolean summary) {
		this.summary = summary;
	}

	public boolean isYearly() {
		return yearly;
	}

	public void setYearly(boolean yearly) {
		this.yearly = yearly;
	}

	public boolean isHeader() {
		return header;
	}

	public void setHeader(boolean header) {
		this.header = header;
	}

	public boolean isYr() {
		return yr;
	}

	public void setYr(boolean yr) {
		this.yr = yr;
	}

	public boolean isDist() {
		return dist;
	}

	public void setDist(boolean dist) {
		this.dist = dist;
	}

	public boolean isPpt() {
		return ppt;
	}

	public void setPpt(boolean ppt) {
		this.ppt = ppt;
	}

	public boolean isPclass() {
		return pclass;
	}

	public void setPclass(boolean pclass) {
		this.pclass = pclass;
	}

	public boolean isTmp() {
		return tmp;
	}

	public void setTmp(boolean tmp) {
		this.tmp = tmp;
	}

	public boolean isGrpb() {
		return grpb;
	}

	public void setGrpb(boolean grpb) {
		this.grpb = grpb;
	}

	public boolean isPr() {
		return pr;
	}

	public void setPr(boolean pr) {
		this.pr = pr;
	}

	public boolean isSize() {
		return size;
	}

	public void setSize(boolean size) {
		this.size = size;
	}

	public boolean isSppb() {
		return sppb;
	}

	public void setSppb(boolean sppb) {
		this.sppb = sppb;
	}

	public boolean isIndv() {
		return indv;
	}

	public void setIndv(boolean indv) {
		this.indv = indv;
	}

	public String getSep() {
		return sep;
	}

	public void setSep(String sep) {
		this.sep = sep;
	}

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
