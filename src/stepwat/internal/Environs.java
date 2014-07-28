package stepwat.internal;

import stepwat.internal.Plot.DisturbEvent;

public class Environs {
	
	public enum PPTClass {
		Ppt_Wet, Ppt_Norm, Ppt_Dry;
	}

	Globals globals;
	Succulent succulent;
	Plot plot;
	
	PPTClass wet_dry;
	/**
	 * precip for the year (mm)
	 */
	protected int ppt;
	/**
	 * precip for the previous (last) year (mm)
	 */
	protected int lyppt;
	/**
	 * precip during growing season (mm)
	 */
	protected int gsppt;
	/**
	 * average daily temp for the year (C)
	 */
	protected float temp;
	/**
	 * amt to reduce growth by temp
	 */
	protected float[] temp_reduction = new float[2];
	/* (eqns 12,13), one for each tempclass */
	
	public Environs(Globals g, Plot p, Succulent s) {
		this.globals = g;
		this.succulent = s;
		this.plot = p;
	}
	/**
	 * Wrapper to generate a new set of environmental factors,
	 * usually for the current year.  Any new environmental
	 * generators should be called from this subroutine.
	 */
	public void generate(RGroups rgroups) {
		if(globals.UseSoilwat) {
			//SXW_Run_SOILWAT();
		} else {
			for(ResourceGroup g : rgroups) {
				g.res_avail = 0.0f;
			}
		}
		
		make_ppt();
		make_temp();
		set_ppt_reduction();
		set_temp_reduction();
		make_disturbance();
	}
	
	/**
	 * take a random number from normal distribution with
	 * mean, stddev that is between min & max from
	 * the Globals.ppt structure.
	 * Also set the growing season precip.
	 */
	private void make_ppt() {
		int r = 0;
		if(!globals.UseSoilwat) {
			while(r < globals.ppt.min || r > globals.ppt.max)
				r = (int) (globals.random.randNorm(globals.ppt.avg, globals.ppt.std) + .5);
			if(ppt > 0) {
				lyppt = ppt;
				ppt = r;
			} else {
				lyppt = ppt = r;
			}
		}
		
		gsppt = (int)(globals.gsppt_prop * ppt);
		
		if(ppt <= globals.ppt.dry)
			wet_dry = PPTClass.Ppt_Dry;
		else if(ppt >= globals.ppt.wet)
			wet_dry = PPTClass.Ppt_Wet;
		else
			wet_dry = PPTClass.Ppt_Norm;
	}
	
	/**
	 * take a random number from normal distribution with
	 * mean, stddev, that is between min & max from
	 * the Globals.temp structure.
	 */
	private void make_temp() {
		float r=0.0f;
		
		if(!globals.UseSoilwat) {
			while(r<globals.temp.min || r > globals.temp.max)
				r = (float) globals.random.randNorm(globals.temp.avg, globals.temp.std);
			temp = r;
		}
	}
	
	/**
	 * 
	 */
	private void set_ppt_reduction() {
		//EQN 10
		succulent.reduction = Math.abs(succulent.growth[Globals.Slope] * gsppt + succulent.growth[Globals.Intcpt]);
		//EQN 16
		succulent.prob_death = (succulent.mort[Globals.Slope] * gsppt + succulent.mort[Globals.Intcpt]) / 100.0f;
	}
	
	/**
	 * This routine implements EQNs 12 and 13
	 */
	private void set_temp_reduction() {
		float[] tp = new float[4];
		int i = 0;
		for(i=Globals.CoolSeason; i<= Globals.WarmSeason; i++) {
			tp[1] = globals.tempparm[i][0];
			tp[2] = globals.tempparm[i][1];
			tp[3] = globals.tempparm[i][2];
			tp[0] = temp + tp[1];
			temp_reduction[i] = tp[2]*tp[0] + tp[3] * (tp[0]*tp[0]);
			temp_reduction[i] = Math.max(0.0f, temp_reduction[i]);
		}
		
		if(globals.UseSoilwat) {
			if(temp < 9.5) {
				temp_reduction[Globals.CoolSeason] = .9f;
				temp_reduction[Globals.WarmSeason] = .6f;
			} else {
				temp_reduction[Globals.CoolSeason] = .6f;
				temp_reduction[Globals.WarmSeason] = .9f;
			}
		}
	}
	
	/**
	 * Generate disturbances, if any, for this year.
	 */
	private void make_disturbance() {
		//probability of colonization if current
		//disturbance is fecalpat
		float pc;
		//new disturbance event generated, if any
		DisturbEvent event;
		
		//Can't have simultaneous disturbances
		if(plot.disturbance != DisturbEvent.NoDisturb) {
			if(plot.disturbance == DisturbEvent.FecalPat) {
				if(plot.pat_removed) {
					plot.disturbed = 0;
					plot.pat_removed = false;
					plot.disturbance = DisturbEvent.NoDisturb;
				} else {
					pc = globals.pat.recol[Globals.Slope] * plot.disturbed + globals.pat.recol[Globals.Intcpt];
					if(globals.random.RandUni() <= pc) {
						plot.pat_removed = true;
						//slight effects for one year
						plot.disturbed = 1;
					} else {
						plot.pat_removed = false;
						plot.disturbed ++;
					}
				}				
			} else {
				plot.disturbed = plot.disturbed != 0 ? plot.disturbed - 1 : 0;
			}
			if(plot.disturbed == 0)
				plot.disturbance = DisturbEvent.NoDisturb;
		}
		
		//if the disturbance was expired above,
		//we can generate a new one immediately
		if(plot.disturbance == DisturbEvent.NoDisturb) {
			//pick some type of disturbance (other than none)
			event = DisturbEvent.values()[globals.random.RandUniRange(1, DisturbEvent.LastDisturb.ordinal() - 1)];
			
			//make sure this is off unless needed
			plot.pat_removed = false;
			if(event == DisturbEvent.FecalPat) {
				if(!globals.pat.use)
					event=DisturbEvent.NoDisturb;
				event = (globals.random.RandUni() <= globals.pat.occur) ? event : DisturbEvent.NoDisturb;
				
				if(event == DisturbEvent.NoDisturb) {
					plot.disturbance = event;
					return;
				}
				plot.pat_removed = (globals.random.RandUni() <= globals.pat.removal) ? true : false;
				plot.disturbed = 0;
			}
			if(event == DisturbEvent.AntMound) {
				
			}
		}
	}
}
