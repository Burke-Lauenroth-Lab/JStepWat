package stepwat.internal;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class Output {
	
	Globals globals;
	Plot plot;
	Environs env;
	RGroups rgroups;
	BmassFlags bmassFlags;
	MortFlags mortFlags;
	
	public Output(Globals g, Plot p, Environs e, RGroups rgs, BmassFlags b, MortFlags m) {
		this.globals = g;
		this.plot = p;
		this.env = e;
		this.rgroups = rgs;
		this.bmassFlags = b;
		this.mortFlags = m;
	}
	
	public void bmassYearly(int year) throws IOException {
		//note that year is only printed, not used as index, so
		//we don't need to decrement to make base0
		List<String> fields = new ArrayList<String>();
		
		if(!bmassFlags.isYearly())
			return;
		if(bmassFlags.isYr()) {
			if(globals.UseSoilwat) {
				//TODO: finish this
				//fields.add(model.year);
			} else {
				fields.add(String.valueOf(year));
			}
		}
		if(bmassFlags.isDist()) {
			switch(plot.disturbance) {
			case NoDisturb : 
				fields.add("None");
				break;
			case FecalPat :
				fields.add("Pat");
				break;
			case AntMound :
				fields.add("Mound");
				break;
			case Burrow :
				fields.add("Burrow");
				break;
			default :
				fields.add("Unkown");
				break;
			}
		}
		if(bmassFlags.isPpt()) {
			fields.add(String.valueOf(env.ppt));
		}
		if(bmassFlags.isPclass()) {
			switch(env.wet_dry) {
			case Ppt_Norm : 
				fields.add("Normal");
				break;
			case Ppt_Wet :
				fields.add("Wet");
				break;
			case Ppt_Dry :
				fields.add("Dry");
				break;
			default :
				fields.add("Unkown");
				break;
			}
		}
		if(bmassFlags.isTmp()) {
			fields.add(String.format("%05.1f", env.temp));
		}
		if(bmassFlags.isGrpb()) {
			for(ResourceGroup rg : rgroups) {
				fields.add(String.format("%f", rg.getBiomass()));
				if(bmassFlags.isSize())
					fields.add(String.format("%f", rg.relsize));
				if(bmassFlags.isPr())
					fields.add(String.format("%f", rg.pr));
			}
		}
		if(bmassFlags.isSppb()) {
			for(Species sp : rgroups.getAllSpecies()) {
				fields.add(String.format("%f", sp.getBiomass()));
				if(bmassFlags.isIndv())
					fields.add(String.format("%d",sp.getEst_count()));
			}
		}
		
		PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(globals.bmass.year.toFile(), true)));
		
		StringBuilder builder = new StringBuilder();
		for(String s : fields) {
			builder.append(s+bmassFlags.getSep());
		}
		String outLine = builder.toString();
		//remove the last sep from the end and print
		if(fields.size() != 0)
			out.println(outLine.substring(0, outLine.length()-1));
		
		out.close();
	}
	
	public void mortYearly() throws IOException {
		if(!mortFlags.isYearly())
			return;
		
		String lines = "";
		
		List<String> fields = new ArrayList<String>();
		//header line already printed
		//print a line of establishments
		fields.add("(Estabs)");
		if(mortFlags.isGroup()) {
			for(ResourceGroup rg : rgroups) {
				fields.add(String.format("%d", rg.estabs));
			}
		}
		if(mortFlags.isSpecies()) {
			for(Species sp : rgroups.getAllSpecies()) {
				fields.add(String.format("%d", sp.estabs));
			}
		}
		StringBuilder builder = new StringBuilder();
		for(String s : fields) {
			builder.append(s+mortFlags.getSep());
		}
		lines = builder.toString();
		lines = lines.substring(0, lines.length()-1);
		lines += "\n";
		
		//now get kill data
		fields.clear();
		for(int age=0; age<globals.getMax_Age(); age++) {
			fields.add(String.format("%d", age+1));
			if(mortFlags.isGroup()) {
				for(ResourceGroup rg : rgroups) {
					if(age < rg.getMax_age())
						fields.add(String.format("%d", rg.kills[age]));
					else
						fields.add("");
				}
			}
			if(mortFlags.isSpecies()) {
				for(Species sp : rgroups.getAllSpecies()) {
					if(age < sp.getMax_age())
						fields.add(String.format("%d", sp.kills[age]));
					else
						fields.add("");
				}
			}
			builder = new StringBuilder();
			for(String s : fields) {
				builder.append(s+mortFlags.getSep());
			}
			lines += builder.toString();
			lines = lines.substring(0, lines.length()-1);
			lines += "\n";
			fields.clear();
		}
		
		PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(globals.mort.year.toFile(), true)));
		out.print(lines);
		out.close();
		globals.mort.year = null;
	}
}
