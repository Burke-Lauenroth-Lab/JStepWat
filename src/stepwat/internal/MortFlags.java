package stepwat.internal;

public class MortFlags {
	public boolean summary,  /* if FALSE, print no mortality output */
    yearly, /* print individual yearly data as well as summary */
    header, /* print a header line of names in each file */
    group,  /* print data summarized by group */
    species; /* print data for species */
	public String sep;
	
	public void setInput(stepwat.input.ST.MortFlags mortFlags) {
		this.summary = mortFlags.sumry;
		this.yearly = mortFlags.yearly;
		this.header = mortFlags.header;
		this.group = mortFlags.group;
		this.species = mortFlags.species;
		sep = mortFlags.sep;
		if(mortFlags.sep.compareTo("t") == 0)
			sep = "\t";
		if(mortFlags.sep.compareTo("s") == 0)
			sep = " ";
	}
}
