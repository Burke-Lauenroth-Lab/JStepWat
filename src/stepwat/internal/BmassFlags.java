package stepwat.internal;

import java.io.IOException;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.nio.file.Path;
import java.nio.file.Paths;

public class BmassFlags {
	
	Globals globals;
	
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
	
	public BmassFlags(Globals g) {
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

	public void setInput(stepwat.input.ST.BmassFlags bmassFlags) throws IOException {
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
		
		Path bmassavg = Paths.get(globals.prjDir, globals.files[Globals.F_BMassAvg]);
		if(java.nio.file.Files.exists(bmassavg.getParent())) {
			if(java.nio.file.Files.exists(bmassavg))
				java.nio.file.Files.delete(bmassavg);
		} else {
			java.nio.file.Files.createDirectories(bmassavg.getParent());
			java.nio.file.Files.createFile(bmassavg);
		}
		
		Path bmasspre = Paths.get(globals.prjDir, globals.files[Globals.F_BMassPre]);
		if(java.nio.file.Files.exists(bmasspre.getParent())) {
			//Get a list of all matches to remove			
			try (DirectoryStream<Path> stream = java.nio.file.Files.newDirectoryStream(bmasspre.getParent(),
					bmasspre.getFileName()+"*.{out}")) {
				for (Path entry : stream) {
					java.nio.file.Files.delete(entry);
				}
			} catch (DirectoryIteratorException ex) {
				// I/O error encounted during the iteration, the cause is an
				// IOException
				throw ex.getCause();
			}
		} else {
			java.nio.file.Files.createDirectories(bmasspre.getParent());
			//java.nio.file.Files.createFile(bmasspre);
		}
	}
}
