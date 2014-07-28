package stepwat.internal;

import java.io.IOException;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.nio.file.Path;
import java.nio.file.Paths;

public class MortFlags {
	Globals globals;
	
	private boolean summary,  /* if FALSE, print no mortality output */
    yearly, /* print individual yearly data as well as summary */
    header, /* print a header line of names in each file */
    group,  /* print data summarized by group */
    species; /* print data for species */
	private String sep;
	
	public MortFlags(Globals g) {
		this.globals = g;
	}
	
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

	public void setInput(stepwat.input.ST.MortFlags mortFlags) throws IOException {
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
		
		Path mortavg = Paths.get(globals.prjDir, globals.files[Globals.F_MortAvg]);
		if(java.nio.file.Files.exists(mortavg.getParent())) {
			if(java.nio.file.Files.exists(mortavg))
				java.nio.file.Files.delete(mortavg);
		} else {
			java.nio.file.Files.createDirectories(mortavg.getParent());
			java.nio.file.Files.createFile(mortavg);
		}
		
		Path mortpre = Paths.get(globals.prjDir, globals.files[Globals.F_MortPre]);
		if(java.nio.file.Files.exists(mortpre.getParent())) {
			//Get a list of all matches to remove			
			try (DirectoryStream<Path> stream = java.nio.file.Files.newDirectoryStream(mortpre.getParent(),
					mortpre.getFileName()+"*.{out}")) {
				for (Path entry : stream) {
					java.nio.file.Files.delete(entry);
				}
			} catch (DirectoryIteratorException ex) {
				// I/O error encounted during the iteration, the cause is an
				// IOException
				throw ex.getCause();
			}
		} else {
			java.nio.file.Files.createDirectories(mortpre.getParent());
			//java.nio.file.Files.createFile(bmasspre);
		}
	}
}
