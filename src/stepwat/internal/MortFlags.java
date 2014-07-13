package stepwat.internal;

public class MortFlags {
	public boolean summary,  /* if FALSE, print no mortality output */
    yearly, /* print individual yearly data as well as summary */
    header, /* print a header line of names in each file */
    group,  /* print data summarized by group */
    species; /* print data for species */
	public String sep;
}
