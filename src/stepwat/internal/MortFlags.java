package stepwat.internal;

public class MortFlags {
	private boolean summary,  /* if FALSE, print no mortality output */
    yearly, /* print individual yearly data as well as summary */
    header, /* print a header line of names in each file */
    group,  /* print data summarized by group */
    species; /* print data for species */
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

	public boolean isGroup() {
		return group;
	}

	public void setGroup(boolean group) {
		this.group = group;
	}

	public boolean isSpecies() {
		return species;
	}

	public void setSpecies(boolean species) {
		this.species = species;
	}

	public String getSep() {
		return sep;
	}

	public void setSep(String sep) {
		this.sep = sep;
	}

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
		if(!(this.summary || this.yearly)) {
			this.header = this.group = this.species = false;
		}
	}
}
