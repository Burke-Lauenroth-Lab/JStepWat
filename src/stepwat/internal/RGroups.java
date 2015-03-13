package stepwat.internal;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import soilwat.Defines;
import stepwat.LogFileIn;
import stepwat.LogFileIn.LogMode;
import stepwat.input.ST.Species.AnnualsParams;
import stepwat.input.ST.Species.SeedDispersalParam;
import stepwat.input.ST.Species.SpeciesProbParam;

public class RGroups implements Iterable<ResourceGroup> {
	public static final int MAX_RGROUPS=10;
	
	Globals globals;
	Plot plot;
	Succulent succulent;
	Environs env;
	MortFlags mortFlags;
	
	List<ResourceGroup> rgroups;
	private List<Species> speciesList;
	
	public RGroups(Globals g, Plot p, Succulent s, Environs e, MortFlags m) {
		this.globals = g;
		this.env = e;
		this.plot = p;
		this.succulent = s;
		this.mortFlags = m;
	}
	
	public void setInputs(stepwat.input.ST.Rgroup rGroup, stepwat.input.ST.Species species, boolean mortFlagsSpecies, boolean mortFlagsGroup) throws Exception {
		LogFileIn f = stepwat.LogFileIn.getInstance();
		
		rgroups = new ArrayList<ResourceGroup>(rGroup.groups.size());
		for(int i=0; i<rGroup.groups.size(); i++) {
			ResourceGroup rg = new ResourceGroup(globals, plot, env);
			int idIndex = -1;
			String name = "";
			for(int j=0; j<rGroup.groups.size(); j++) {
				if(rGroup.groups.get(j).id == i) {
					idIndex = rGroup.groups.get(j).id;
					name = rGroup.groups.get(j).name;
				}
			}
			int rpi = rGroup.ResourceParams_Name2Index(name);
			int spi = rGroup.SucculentParams_Name2Index(name);
			boolean isSucculent;
			if(spi == -1) {
				isSucculent = false;
			} else {
				isSucculent = true;
				succulent.setInput(rGroup.succulentParams.get(spi));
			}
			rg.setInput(rGroup.groups.get(idIndex), rGroup.resourceParameters.get(rpi), isSucculent);
			rgroups.add(rg);
		}
		
		speciesList = new ArrayList<Species>(species.speciesParams.size());
		for(int i=0; i<species.speciesParams.size(); i++) {
			Species sp = new Species(globals);
			int rgi = species.speciesParams.get(i).rg-1;
			int ai = species.Annuals_Name2Index(species.speciesParams.get(i).name);
			AnnualsParams aparams = ai==-1?null:species.annualsParams.get(ai);
			
			SpeciesProbParam sprobparam = species.speciesProbParam.get(species.SpeciesProb_Name2Index(species.speciesParams.get(i).name));
			SeedDispersalParam seedparam = species.seedDispersalParam.get(species.SeedDispersal_Name2Index(species.speciesParams.get(i).name));
			sp.setInput(species.speciesParams.get(i), rgroups.get(rgi), aparams, sprobparam, seedparam, i);
			speciesList.add(sp);
		}
		
		/*
		 * count and link species to their groups.
		 * print a message if more specified than available
		 */
		
		for(int rg=0; rg<rgroups.size(); rg++) {
			int cnt = 0;
			ResourceGroup g = rgroups.get(rg);
			for(int sp=0; sp<speciesList.size(); sp++) {
				if(this.speciesList.get(sp).getRes_grp().getGrp_num() == rg) {
					g.getSpecies().add(speciesList.get(sp));
					cnt++;
				}
			}
			g.setMax_spp(cnt);
			if(cnt < g.getMax_spp_estab()) {
				g.setMax_spp_estab(cnt);
				f.LogError(LogMode.NOTE, "Max_Spp_Estab > Number of Spp for "+g.getName()+".\nContinuing");
			}
		}
		/*
		 * determine max age for the species and
		 * keep track for group's max age
		 * and compute max g/m^2 for this group
		 * also, compute max_bmass for the group
		 */
		for(int rg=0; rg<rgroups.size(); rg++) {
			ResourceGroup g = rgroups.get(rg);
			int maxage=0;
			int minage=30000;
			g.setMax_bmass(0);
			for(Species s : g.getSpecies()) {
				if(s.getMax_age()==0)
					s.setMax_age(globals.getRunModelYears());
				/* maxage shouldn't be set to extirp due to age-independent mortality.
				 extirp happens due to the flag, not age;
				 but I hesitate to remove the code, so I'll comment it out for now.
				 if(g.getExtirp() != 0 && g.getExtirp() < s.getMax_age())
					s.setMax_age(g.getExtirp());
				 */
				maxage = Math.max(s.getMax_age(), maxage);
				minage = Math.min(s.getMax_age(), minage);
				g.setMax_bmass(g.getMax_bmass()+s.getMature_biomass());
			}
			if(minage == 1 && maxage != 1) {
				f.LogError(LogMode.FATAL, rgroups.get(rg).getName()+": Can't mix annuals and perennials within a group\n"+
							"Refer to the groups.in and species.in files\n");
			}
			g.setMax_age(maxage);
		}
		/*
		 * check out the definitions for SppMaxAge and GrpMaxAge
		 * they're used here for some hoped-for sense of readability
		 */
		if(mortFlagsSpecies) {
			for(int sp=0; sp<speciesList.size(); sp++) {
				if(speciesList.get(sp).isUse_me()) {
					speciesList.get(sp).setKills(new int[speciesList.get(sp).getMax_age()]);
				} else {
					speciesList.get(sp).setKills(null);
				}
			}
		} else {
			for(int sp=0; sp<speciesList.size(); sp++) {
				speciesList.get(sp).setKills(null);
			}
		}
		
		if(mortFlagsGroup) {
			for(int rg=0; rg<rgroups.size(); rg++) {
				if(rgroups.get(rg).isUse_me()) {
					for(Species sp : rgroups.get(rg).getSpecies()) {
						int max_age = sp.getMax_age()>0 ? Math.max(sp.getMax_age(), rgroups.get(rg).getMax_age()) : globals.getMax_Age();
						rgroups.get(rg).setMax_age(max_age);
					}
					rgroups.get(rg).setKills(new int[rgroups.get(rg).getMax_age()]);
				} else {
					rgroups.get(rg).setKills(null);
				}
			}
		} else {
			for(int rg=0; rg<rgroups.size(); rg++) {
				rgroups.get(rg).setKills(null);
			}
		}
	}
	
	public void checkSpecies() throws Exception {
		LogFileIn f = stepwat.LogFileIn.getInstance();
		int runyrs = globals.runModelYears;
		int cnt = 0;
		int maxage = 0;
		int minage = 0;
		boolean tripped = false;
		for(ResourceGroup g : this) {
			cnt = 0;
			for(Species sp : speciesList) {
				if(sp.getRes_grp().getGrp_num() == g.getGrp_num()) {
					cnt++;
				}
			}
			g.setMax_spp(cnt);
			if(cnt < g.getMax_spp_estab()) {
				tripped = true;
				g.setMax_spp_estab(cnt);
				f.LogError(LogMode.NOTE, "Max_Spp_Estab > Number of Spp for "+g.getName());
			}
		}
		if(tripped)
			f.LogError(LogMode.NOTE, "Continuing");
		
		//determine max age for the species and
		//keep track for group's max age
		//and compute max g/m^2 for this group
		//10-Apr-03 - also, compute max_bmass for the group.
		for(ResourceGroup g : this) {
			maxage = 0;
			minage = 30000;
			g.setMax_bmass(0.0f);
			for(Species s : g.getSpecies()) {
				if(s.getMax_age() == 0)
					s.setMax_age(runyrs+1);
				//maxage shouldn't be set to extirp due to age-independent mortality.
				//extirp happens due to the flag, not age;
				//but I hesitate to remove the code, so I'll comment it out for now.
				//if ( g->extirp && g->extirp < s->max_age)
				//   s->max_age = g->extirp;
				maxage = Math.max(s.getMax_age(), maxage);
				minage = Math.min(s.getMax_age(), minage);
				g.setMax_bmass(g.getMax_bmass() + s.getMature_biomass());
			}
			if(minage == 1 && maxage !=1) {
				f.LogError(LogMode.FATAL, g.getName()+": Can't mix annuals and perennials within a group\n"
						+"Refer to the groups.in and species.in files\n");
			}
			g.setMax_age(maxage);
		}
		//check out the definitions for SppMaxAge and GrpMaxAge
		//they're used here for some hoped-for sense of readability
		if(mortFlags.isSpecies()) {
			//size each species kills list, age ==0 ok
			for(Species s : speciesList) {
				if(s.isUse_me()) {
					s.kills = new int[s.getMax_age()];
				} else {
					s.kills = null;
				}
			}
		} else {
			for(Species s : speciesList)
				s.kills = null;
		}
		
		if(mortFlags.isGroup()) {
			for(ResourceGroup rg : this) {
				if(rg.isUse_me()) {
					//find max age of the group and size the kills array
					for(Species s : rg.getSpecies()) {
						rg.setMax_age( (s.getMax_age() != 0) ? Math.max(s.getMax_age(), rg.getMax_age()) : globals.getMax_Age() );
					}
					rg.kills = new int[rg.getMax_age()];
				} else {
					rg.kills = null;
				}
			}
		} else {
			for(ResourceGroup rg : this) {
				rg.kills = null;
			}
		}
	}
	
	/**
	 * Partition resources for this year among the resource
	 * groups.  The allocation happens in three steps: basic
	 * group allocation (which is done here), partitioning of
	 * extra resources (in _res_part_extra()), and further
	 * partitioning to individuals in the group (_ResPartIndiv()).<br>
	 * See COMMENT 1. at the end of this file for the algorithm.<br>
	 * For future documenting reference, note that the one
	 * requirement regarding slope/intercept is that the
	 * Globals.ppt.avg * slope + intercept == 1.0 for all
	 * groups.
	 * @throws Exception 
	 */
	public void partResources(SXW sxw) throws Exception {
		/** amt of "resource" == 1 when ppt is avg */
		float resource;
		/** pooled extra resource up to 1.0 */
		float xtra_base = 0.0f;
		/** pooled resource > 1.0 */
		float xtra_obase = 0.0f;
		/** total res. contrib to base, all groups */
		float[] size_base = new float[MAX_RGROUPS];
		/** total res. contrib. if xtra_obase */
		float[] size_obase = new float[MAX_RGROUPS];

		boolean noplants = true;
		/** Monikers for _res_part_extra() */
		final boolean do_base = false, do_extra = true,
		/** Monikers for pass 1 & 2 _add_annuals() */
		add_seeds = true, no_seeds = false;
		
		/* ----- distribute basic (minimum) resources */
		for (ResourceGroup g : rgroups) {
			if (g.getMax_age() == 1)
				g.relsize = g.add_annuals(1.0f, no_seeds);

			if (!globals.UseSoilwat) {
				resource = g.ppt2resource(env.ppt);
				g.res_required = g.relsize / g.getMax_density();
				g.res_avail = Math.min(1.0f, Math.min(g.res_required, resource));
				xtra_base += Math.max(0., Math.min(1., resource) - g.res_avail) * g.getMin_res_req();
				xtra_obase += Math.max(0., resource - 1.) * g.getMin_res_req();

				size_base[g.getGrp_num()] = g.relsize * g.getMin_res_req();
				size_obase[g.getGrp_num()] = (g.isUse_extra_res()) ? size_base[g.getGrp_num()] : 0.0f;
			} else {
				g.res_required = g.getBiomass();
				g.res_avail = sxw.getTranspiration(g.getGrp_num());
				
				//PR limit can be really high see mort no_resource I limit to the groups estab indiv because that is what we can kill.
				//This was at 10 but ten might be to low or high in some cases.
				if(!Defines.isZero(g.res_avail) && g.res_required/g.res_avail > g.estabs) {
					g.res_required = g.estabs;
					g.res_avail = 1;
				}
				//Check
				if(Defines.isZero(g.res_avail) && g.res_required > 0) {
					g.res_required = g.estabs;
					g.res_avail = 1;
				}
				
				//Annuals seem to have a artificial limit of 20. We do Annuals here differently.
				if(g.getMax_age() == 1) {
					g.res_required = g.relsize * g.est_annuals_bio(1.0f);
					g.res_avail = sxw.getTranspiration(g.getGrp_num());
					if(!Defines.isZero(g.res_avail) && g.res_required / g.res_avail > 20) {
						g.res_required = 20;
						g.res_avail = 1;
					}
					if(Defines.isZero(g.res_avail) && g.res_required > 0) {
						g.res_required = 20;
						g.res_avail = 1;
					}
				}
			}
			if(Float.compare(g.relsize, 0.0f) > 0)
				noplants = false;
		    
		}
		if(noplants)
			return;
		
		if(!globals.UseSoilwat) {
			res_part_extra(do_base, xtra_base, size_base);
			res_part_extra(do_extra, xtra_obase, size_obase);
		}
		//reset annuals' "true" relative size here
		for (ResourceGroup g : rgroups) {
			g.pr = Float.compare(g.res_avail, 0.0f) == 0 ? 0.0f : g.res_required / g.res_avail;
			if(g.getMax_age() == 1) {
				g.relsize = g.add_annuals(g.pr, add_seeds);
			}
		}
		
		for (ResourceGroup g : rgroups) {
			g.resPartIndiv();
		}
	}
	
	/**
	 * Determines if some groups have unused resources and
	 * redistributes them to other groups that can use them
	 * (if any).
	 *  See COMMENT 2 at the end of this file for the algorithm.
	 */
	private void res_part_extra(boolean isextra, float extra, float[] size) {
		//group's prop'l contrib to the total requirements
		//placeholder for 1 if by-mm or min_res_req otherwise
		float req_prop, sum_size=0.0f, space;
		
		for(ResourceGroup g : rgroups) {
			sum_size += size[g.getGrp_num()];
		}
		
		for(ResourceGroup g : rgroups) {
			if(Float.compare(g.relsize, 0.0f) == 0)
				continue;
			if(isextra && !g.isUse_extra_res())
				continue;
			
			space = (globals.UseSoilwat) ? 1.0f : g.getMin_res_req();
			req_prop = size[g.getGrp_num()] / sum_size;
			
			if(isextra && g.isUse_extra_res() && Float.compare(g.getXgrow(), 0.0f) > 0) {
				g.res_extra = req_prop * extra / space;
			} else {
				g.res_avail += req_prop * extra / space;
			}
		}
	}
	
	/**
	 * Main loop to grow all the plants.
	 * @throws Exception 
	 */
	public void grow() throws Exception {
		for(ResourceGroup g : rgroups) {
			g.grow();
		}
	}
	
	/**
	 * Determines which and how many species can establish
	 * in a given year.
	 * <br>
	 * For each species in each group, check that a uniform
	 * random number between 0 and 1 is less than the species'
	 * establishment probability.<br>
	 * a) If so, return a random number of individuals,
	 * up to the maximum allowed to establish for the
	 * species.  This is the number of individuals in
	 * this species that will establish this year.
	 * b) If not, continue with the next species.
	 * @throws Exception 
	 */
	public void establish() throws Exception {
		//Cannot establish if plot is still in disturbed state
		if(plot.disturbed > 0) {
			for(ResourceGroup g : rgroups)
				g.regen_ok = false;
			return;
		}
		
		for(ResourceGroup g : rgroups)
			g.establish();
		
	}
	
	/**
	 * Increment ages of individuals in a resource group.
	 * @return
	 * @throws Exception 
	 */
	public void incrAges() throws Exception {
		for(ResourceGroup g : rgroups) {
			g.incrAges();
		}
	}
	/**
	 * returns all species
	 * @return
	 */
	public List<Species> getAllSpecies() {
		return speciesList;
	}
	
	public int size() {
		return rgroups.size();
	}
	
	public void add(ResourceGroup rg) {
		this.rgroups.add(rg);
	}
	
	public ResourceGroup get(int index) {
		return this.rgroups.get(index);
	}

	@Override
	public Iterator<ResourceGroup> iterator() {
		// TODO Auto-generated method stub
		return rgroups.iterator();
	}
}
