package stepwat.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import soilwat.Defines;
import stepwat.LogFileIn;
import stepwat.LogFileIn.LogMode;
import stepwat.input.ST.Rgroup;
import stepwat.internal.Environs.PPTClass;

public class ResourceGroup {

	public static final int MAX_SPP_PER_GRP = 10;
	public static final int Ppt_Wet=0;
	public static final int Ppt_Norm=1;
	public static final int Ppt_Dry=2;
	
	public enum DepthClass {
		DepthNonComp, DepthShallow, DepthMedium, DepthDeep, DepthLast;
	}
	
	public Globals globals;
	public Plot plot;
	public Environs Env;
	
	/* Quantities that can change during model runs */
	/**
	 * indivs in group killed. index by age killed
	 */
	protected int[] kills;
	/**
	 * total indivs in group established during iter
	 */
	protected int estabs;
	/**
	 * kill the group in this year; if 0, don't kill, but see killfreq
	 */
	protected int killyr;
	/**
	 * counter for consecutive years low resources
	 */
	protected int yrs_neg_pr;
	/**
	 * extra resource converted back to mm
	 */
	protected int mm_extra_res;
	/**
	 * resource required for current size
	 */
	protected float res_required;
	/**
	 *  resource available from environment X competition
	 */
	protected float res_avail;
	/**
	 * if requested, resource above 1.0 when PR < 1.0
	 */
	protected float res_extra;
	/**
	 * resources required / resources available
	 */
	protected float pr;
	/**
	 * size of all species' indivs' relsizes scaled to 1.0
	 */
	protected float relsize;
	/**
	 * number of species actually established in group
	 * @deprecated
	 */
	protected int est_count;
	/**
	 * list of spp actually estab in grp
	 */
	protected List<Species> est_spp = new ArrayList<Species>();
	/**
	 * group extirpated, no more regen
	 */
	protected boolean extirpated;
	/**
	 * annuals: comb. of startyr, etc means we can regen this year
	 */
	protected boolean regen_ok;

	/* Quantities that DO NOT change during model runs */
	
	/**
	 * num yrs resources can be stretched w/o killing
	 */
	private int max_stretch;
	/**
	 * max # species that can add new plants per year
	 */
	private int max_spp_estab;
	/**
	 * number of species in the group
	 */
	private int max_spp;
	/**
	 * longest lifespan in group. used to malloc kills[]
	 */
	private int max_age;
	/**
	 * don't start trying to grow until this year
	 */
	private int startyr;
	/**
	 * kill group at this frequency: <1=prob, >1=# years
	 */
	private int killfreq;
	/**
	 * year in which group is extirpated (0==ignore)
	 */
	private int extirp;
	/**
	 * index number of this group
	 */
	private int grp_num;
	/**
	 * type of VegProd. 1 for tree, 2 for shrub, 3 for grass
	 */
	private int veg_prod_type;
	/**
	 * list of spp belonging to this grp
	 */
	private List<Species> species = new LinkedList<Species>();
	/**
	 * input from table
	 */
	private float min_res_req;
	/**
	 * number of mature plants per plot allowed
	 */
	private float max_density;
	/**
	 * convert density and plotsize to max plants/m^2
	 */
	private float max_per_sqm;
	/**
	 * sum of mature biomass for all species in group
	 */
	private float max_bmass;
	/**
	 * ephemeral growth = mm extra ppt * xgrow
	 */
	private float xgrow;
	/**
	 * user-defined growthrate that triggers mortality
	 */
	private float slowrate;
	/**
	 * res. space eqn: slope for wet/dry/norm yrs
	 */
	private float[] ppt_slope = new float[3];
	/**
	 * res. space eqn: intercept for wet/dry/norm yrs
	 */
	private float[] ppt_intcpt = new float[3];
	/**
	 * responds to other groups' unused resources
	 */
	private boolean succulent, use_extra_res;
	/**
	 * establish no species of this group if false
	 */
	private boolean use_me;
	/**
	 * use age-independent+slowgrowth mortality?
	 */
	private boolean use_mort;
	/**
	 * establish this group every year if true
	 */
	private boolean est_annually;
	/**
	 * rooting depth class
	 */
	private DepthClass depth;
	/**
	 * Group name
	 */
	private String name;
	
	public ResourceGroup(Globals g, Plot p, Environs e) {
		this.globals = g;
		this.plot = p;
		this.Env = e;
	}
	
	/**
	 * Takes input object and copies values to proper internal structure
	 * 
	 * @param group
	 * @param rpram
	 * @param succulent
	 */
	public void setInput(Rgroup.GroupType group, Rgroup.ResourceParameters rpram, boolean succulent) {
		this.setGrp_num(group.id);
		this.name = new String(group.name);
		this.setMax_stretch(group.stretch);
		this.setMax_spp_estab(group.maxest);
		this.setMax_density(group.density);
		this.setMax_per_sqm(group.density / globals.getPlotsize());
		this.setSlowrate(group.slow);
		this.setMin_res_req(group.space);
		this.setEst_annually(group.estann);
		this.setStartyr(group.startyr);
		this.killyr = group.killyr;
		this.setKillfreq(group.killfreq);
		this.setUse_extra_res(group.xres);
		this.setExtirp(group.extirp);
		this.setXgrow(group.xgrow);
		this.setUse_me(group.on);
		this.setUse_mort(group.mort);
		this.setVeg_prod_type(group.veg_prod_type);

		this.extirpated = false;
		
		this.ppt_slope[Ppt_Norm] = rpram.nslope;
		this.ppt_intcpt[Ppt_Norm] = rpram.nint;
		this.ppt_slope[Ppt_Wet] = rpram.wetslope;
		this.ppt_intcpt[Ppt_Wet] = rpram.wetint;
		this.ppt_slope[Ppt_Dry] = rpram.dryslope;
		this.ppt_intcpt[Ppt_Dry] = rpram.dryint;
		
		//only pass succ param if it is the succ group
		//there should be only one succ param
		if(succulent) {
			this.setSucculent(true);
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
	protected void establish() throws Exception {
		int num_est = 0;

		if (!use_me)
			return;

		regen_ok = true;
		if (globals.currYear < startyr) {
			regen_ok = false;
		} else if (max_age == 1) {
			// see similar logic in mort_EndOfYear() for perennials
			if (Defines.GT(killfreq, 0.0f)) {// GT
				if (Defines.LT(killfreq, 1.0f)) {// LT
					if (globals.random.RandUni() <= killfreq)
						regen_ok = false;
				} else if ((globals.currYear - startyr) % killfreq == 0) {
					regen_ok = false;
				}
			}
		} else {
			for (Species sp : species) {
				if (!sp.isUse_me())
					continue;
				if (!sp.allow_growth)
					continue;

				num_est = sp.numEstablish();
				if (num_est != 0) {
					sp.addIndiv(num_est);
					sp.updateEstabs(num_est);
				}
			}
		}
	}
	
	/**
	 * Main loop to grow all the plants.
	 * @throws Exception 
	 */
	public void grow() throws Exception {
		final float OPT_SLOPE = 0.5f;
		/** growth of one individual */
		float growth1;
		/** sum of growth for a species' indivs */
		float sppgrowth = 0;
		/** rate of growth for an individual */
		float rate1;
		/** growth factor modifier */
		float tgmod=0, gmod;
		
		
		if (max_age == 1)
			return; // annuals already taken care of
		if (est_spp.size() == 0)
			return; // Nothing to grow?
		if (isSucculent() && Env.wet_dry == PPTClass.Ppt_Wet)
			return; // can't grow succulents if a wet year, so skip this group

		// for each non-annual species
		// grow individuals and increment size
		// all groups are either all annual or all perennial
		for (Species s : est_spp) {
			sppgrowth = 0;
			if (!s.allow_growth)
				continue;

			// Modify growth rate by temperature
			// calculated in Env_Generate()
			if (s.getTempclass() != Species.TempClass.NoSeason) {
				tgmod = Env.temp_reduction[s.getTempclass().ordinal()-1];
			}

			// now grow the individual plants of current species
			for (Indiv ndv : s.Indvs) {
				// modify growth rate based on resource availability
				// deleted EQN 5 because it's wrong. gmod==.05 is too low

				gmod = 1.0f - OPT_SLOPE * Math.min(1.0f, ndv.pr);
				if (Defines.GT(ndv.pr, 1.0f))
					gmod /= ndv.pr;

				// TODO: tgmod is not initialized in the C code.
				gmod *= tgmod;

				if (ndv.killed && globals.random.RandUni() < ndv.prob_veggrow) {
					// if indiv appears killed it was reduced due to low
					// resources last year. it can veg. prop. this year
					// but couldn't last year.
					growth1 = s.getRelseedlingsize() * globals.random.RandUniRange(1, s.getMax_vegunits());
					rate1 = growth1 / ndv.relsize;
					ndv.killed = false;
				} else {
					// normal growth: modifier times optimal growth rate (EQN 1)
					rate1 = gmod * s.getIntrin_rate() * (1.0f - ndv.relsize);
					growth1 = rate1 * ndv.relsize;
				}
				ndv.relsize += growth1;
				ndv.growthrate = rate1;
				sppgrowth += growth1;
			}// end of indivs
			s.update_Newsize(sppgrowth);
		}// end of species
		extra_growth();
	}
	
	/**
	 * if add_seeds==FALSE, don't add seeds to the seedbank or
	 * add species, but do calculations required to return a
	 * temporary group relative size for resource allocation.
	 * if add_seeds==TRUE, we should have done the above and now
	 * we really want to add to the biomass and seedbank.<br>
	 * <br>
	 * check regen_ok flag.  if true, apply establishment and
	 * add to seedbank.  Otherwise, add 0 to seedbank and skip
	 * adding plants this year.  We also check probability of establishment to
	 * account for introduction of propagules, in which case
	 * we can't add to the seedbank twice.<br>
	 * <br>
	 * reset group size to account for additions, or 0 if none.
	 * @param g_pr
	 * @param add_seeds
	 * @return
	 * @throws Exception 
	 */
	protected float add_annuals(float g_pr, boolean add_seeds) throws Exception {
		final float PR_0_est = 20.0f;
		float x;
		/* number of estabs predicted by seedbank */
		float estabs;
		/* inverse of PR as the limit of estabs */
		float pr_inv;
		float newsize;
		float sumsize;
		boolean forced;
		
		if(max_age != 1) {
			//error?
		}
		
		if(!use_me) {
			return 0.0f;
		}
		
		sumsize = 0.0f;
		
		for(Species s : species) {
			newsize = 0.0f;
			forced = false;
			
			if(!add_seeds && globals.random.RandUni() <= s.getSeedling_estab_prob()) {
				add_annual_seedprod(s, regen_ok?g_pr:-1.0f);
				forced = true;
			}
			
			x = regen_ok ? get_annual_maxestab(s) : 0;
			if(Defines.GT(x, 0)) {
				estabs = (x - (x / PR_0_est * g_pr)) * (float)Math.exp((double)-g_pr);
				pr_inv = 1.0f / g_pr;
				newsize = Math.min(pr_inv, estabs);
				if(add_seeds) {
					addSpecies(s);
					s.update_Newsize(newsize);
				}
			}
			
			if(add_seeds && !forced)
				add_annual_seedprod(s, Defines.isZero(x) ? -1.0f : g_pr);
			
			sumsize += newsize;
		}
		
		return (sumsize / (float)max_spp);
	}
	
	protected float est_annuals_bio(float g_pr) {
		float x;
		/* inverse of PR as the limit of estabs */
		float newsize;
		float sumsize;
		
		if(max_age != 1) {
			//error?
		}
		
		if(!use_me) {
			return 0.0f;
		}
		
		sumsize = 0.0f;
		
		for(Species s : species) {
			newsize = 0.0f;
			
			x = regen_ok ? get_annual_maxestab(s) : 0;
			if(Defines.GT(x, 0)) {
				newsize = s.getMature_biomass();
			}
			
			sumsize += newsize;
		}
		return sumsize;
	}
	
	/**
	 * 
	 * @return
	 */
	private float get_annual_maxestab(Species sp) {
		float sum = 0;
		
		for(int i=1; i<=sp.getViable_yrs(); i++) {
			sum += sp.seedprod[i-1] / Math.pow(i, sp.getExp_decay());
		}
		
		return sum;
	}
	
	/**
	 * negative pr means add 0 seeds to seedbank
	 * @param pr
	 * @return
	 */
	private void add_annual_seedprod(Species sp, float pr) {
		int i=0;
		for(i=sp.getViable_yrs() - 1; i>0; i--) {
			sp.seedprod[i] = sp.seedprod[i-1];
		}
		
		sp.seedprod[i] = Float.compare(pr, 0.0f) < 0 ? 0.0f : sp.getMax_seed_estab() * (float)Math.exp((double)-pr);
	}
	
	/**
	 * Converts ppt in mm to a resource index for a group
	 * pointed to by g. Represents EQNs 2,3,4
	 * @return
	 */
	protected float ppt2resource(float ppt) {
		return (ppt * ppt_slope[Env.wet_dry.ordinal()] + ppt_intcpt[Env.wet_dry.ordinal()]);
	}
	
	/**
	 * The PR value used in the growth loop is now at the
	 * individual level rather than the group level, so the
	 * resource availability for the group is divided between
	 * individuals of the group, largest to smallest.  Species
	 * distinctions within the group are ignored.
	 * 
	 * The partitioning of resources to individuals is done
	 * proportionally based on size so that individuals can
	 * have their own PR value.  The reasoning is that larger
	 * individuals should get more resources than their
	 * less-developed competitors.
	 */
	public void resPartIndiv() {
		Indiv ndv;
		//temporary multiplier
		float x;
		//remainder of resource after allocating to an indiv
		float base_rem;
		
		//annuals don't have indivs
		if(max_age == 1)
			return;
		if(est_spp.size() == 0)
			return;
		
		//allocate the temporary group-oriented arrays
		List<Indiv> indivs = getIndivs(true, false);
		
		//assign indivs' availability, not including extra
		//resource <= 1.0 is for basic growth, >= extra
		//amount of extra, if any, is kept in g->res_extra
		//base_rem = fmin(1, g->res_avail);
		base_rem = res_avail;
		for(int n=0; n<indivs.size(); n++) {
			ndv = indivs.get(n);
			
			ndv.res_required = (ndv.relsize / max_spp) / max_density;
			if(Float.compare(pr, 1.0f) > 0) {
				ndv.res_avail = Math.min(ndv.res_required, base_rem);
				base_rem = Math.max(base_rem - ndv.res_avail, 0.0f);
			} else {
				ndv.res_avail = ndv.grp_res_prop * ndv.res_avail;
			}
		}
		
		base_rem += Math.min(0.0f, res_avail - 1.0f);
		
		//compute PR, but on the way, assign extra resource
		for(int n=0; n<indivs.size(); n++) {
			ndv = indivs.get(n);
			if(use_extra_res) {
				//polish off any remaining resource not allocated to extra
				ndv.res_avail += Float.compare(base_rem, 0.0f) == 0 ? 0.0f : ndv.grp_res_prop * base_rem;
				//extra resource gets assigned here if applicable
				if(Float.compare(res_extra, 0.0f) > 0) {
					x = 1.0f - ndv.relsize;
					ndv.res_extra = (1.0f - x) * ndv.grp_res_prop * res_extra;
					ndv.res_avail += x * ndv.grp_res_prop * res_extra;
				}
			}
			
			//at last!  compute the PR value, or dflt to 100
			ndv.pr = Float.compare(ndv.res_avail, 0.0f) > 0 ? ndv.res_required / ndv.res_avail : 100.0f;
		}
	}
	
	/**
	 * put all the individuals of the group into a list
	 * and optionally sort the list. If sort==true then
	 * asc if asc==true else desc.
	 * 
	 * @param sort
	 * @param asc
	 * @return
	 */
	public LinkedList<Indiv> getIndivs(boolean sort, boolean asc) {
		int i=0;
		LinkedList<Indiv> nlist = new LinkedList<Indiv>();
		
		for (Species species : est_spp) {
			i+=species.Indvs.size();
			nlist.addAll(species.Indvs);
		}

		if(i>0 && sort) {
			if(asc)
				Collections.sort(nlist, new Indiv.SortSizeAsc());
			else
				Collections.sort(nlist, new Indiv.SortSizeDesc());
		}
		
		return nlist;
	}
	
	/**
	 * Relative size for a group is 1.0 if all the group's
	 * species are established and size 1.0;
	 */
	public void update_Newsize() {
		float sumsize = 0;
		
		/*
		 * first get group's relative size adjusted for num indivs
		 * ie, groupsize=1 when 1 indiv of each species is present
		 * ie each indiv is an equivalent contributor, not based on biomass
		 */
		for (Species species : est_spp) {
			sumsize += species.getRelsize();
		}
		this.relsize = sumsize / this.max_spp;
		
		if(this.max_age != 1) {
			//compute the contribution of each indiv to the group's size
			LinkedList<Indiv> indivs = getIndivs(false,false);
			Iterator<Indiv> itr = indivs.iterator();
			while(itr.hasNext()) {
				Indiv ind = itr.next();
				ind.grp_res_prop = ind.relsize / sumsize;
			}
		}
		
		if(Defines.isZero(relsize)) relsize=0;
	}
	
	/**
	 * When a species associated with a resource group dies
	 * out, it is dropped from the group so it will not be
	 * processed unnecessarily.
	 */
	protected void dropSpecies(Iterator<Species> iter, Species s) {
		//if(est_spp.contains(s)) {
			//est_spp.remove(s);
		if(iter != null)
			iter.remove();
		else
			if(est_spp.contains(s))
				est_spp.remove(s);
		//est_count = est_spp.size();
		//}	
	}
	
	/**
	 * When a species associated with a resource group becomes
	 * established, it is added to the list of species and
	 * otherwise linked to the group.
	 * @param sp
	 */
	public void addSpecies(Species sp) {
		if(!est_spp.contains(sp)) {
			est_spp.add(sp);
		}
	}
	
	
	
	/**
	 * When there are resources beyond the minimum necessary
	 * for "optimal" growth, ie, use of eqn 5, the extra
	 * resources are converted to superfluous growth that
	 * only counts for the current year and is removed at
	 * the beginning of the next year.
	 * @throws Exception 
	 */
	protected void extra_growth() throws Exception {
		float extra, indivpergram;
		
		if(max_age == 1) return;
		if(Defines.isZero(xgrow)) return;
		if(!use_extra_res) return;
		
		for(Species s : est_spp) {
			indivpergram = 1.0f/s.getMature_biomass();
			for(Indiv ndv : s.Indvs) {
				extra = ndv.res_extra * min_res_req * Env.ppt * xgrow;
				s.extragrowth += extra * indivpergram;
			}
			s.update_Newsize(s.extragrowth);
		}
	}
	
	/**
	 * Increment ages of individuals in a resource group.
	 * @return
	 * @throws Exception 
	 */
	public void incrAges() throws Exception {
		LogFileIn f = stepwat.LogFileIn.getInstance();

		if (max_age == 1)
			return;
		for (Species s : est_spp) {
			for (Indiv ndv : s.Indvs) {
				ndv.age++;
				if (ndv.age > s.getMax_age()) {
					f.LogError(
							LogMode.WARN,
							s.getName() + " grown older than max_age ("
									+ String.valueOf(ndv.age) + " > "
									+ String.valueOf(s.getMax_age()) + " Iter="
									+ String.valueOf(globals.currIter)
									+ ", Year="
									+ String.valueOf(globals.currYear));
				}
			}
		}
	}
	
	/**
	 * Convert relative size to biomass for a resource group.
	 * @return
	 */
	public float getBiomass() {
		float biomass = 0.0f;
		if(est_spp.size() == 0) return 0.0f;
		for(Species s : est_spp) {
			biomass += s.relsize * s.getMature_biomass();
		}
		return biomass;
	}
	
	/**
	 * Kill a group catastrophically, meaning, kill all
	 * individuals, remove their biomass (relsize), and
	 * don't let them regenerate ever again.
	 * @return
	 * @throws Exception 
	 */
	public void extirpate() throws Exception {
		for(Species s : species) {
			s.kill();
			s.setSeedling_estab_prob(0.0f);
		}
		this.extirpated = true;
	}
	
	/**
	 * Kill all individuals of all species in the group, but allow them to regenerate.
	 * @return
	 * @throws Exception 
	 */
	public void kill() throws Exception {
		for(Species s : est_spp) {
			s.kill();
		}
	}
	
	public int getYrs_neg_pr() {
		return yrs_neg_pr;
	}
	public void setYrs_neg_pr(int yrs_neg_pr) {
		this.yrs_neg_pr = yrs_neg_pr;
	}
	public int getEst_count() {
		return est_spp.size();
	}
	public void clearEstSpecies() {
		this.est_spp.clear();
	}
	public void setExtirpated(boolean extirpated) {
		this.extirpated = extirpated;
	}
	
	public boolean isExtirpated() {
		return extirpated;
	}
	
	public int[] getKills() {
		return kills;
	}

	public void setKills(int[] kills) {
		this.kills = kills;
	}
	
	public String getName() {
		return this.name;
	}

	public int getMax_stretch() {
		return max_stretch;
	}

	public void setMax_stretch(int max_stretch) {
		this.max_stretch = max_stretch;
	}

	public int getMax_spp_estab() {
		return max_spp_estab;
	}

	public void setMax_spp_estab(int max_spp_estab) {
		this.max_spp_estab = max_spp_estab;
	}

	public int getMax_spp() {
		return max_spp;
	}

	public void setMax_spp(int max_spp) {
		this.max_spp = max_spp;
	}

	public int getMax_age() {
		return max_age;
	}

	public void setMax_age(int max_age) {
		this.max_age = max_age;
	}

	public int getStartyr() {
		return startyr;
	}

	public void setStartyr(int startyr) {
		this.startyr = startyr;
	}

	public int getKillfreq() {
		return killfreq;
	}

	public void setKillfreq(int killfreq) {
		this.killfreq = killfreq;
	}

	public int getExtirp() {
		return extirp;
	}

	public void setExtirp(int extirp) {
		this.extirp = extirp;
	}

	public int getGrp_num() {
		return grp_num;
	}

	public void setGrp_num(int grp_num) {
		this.grp_num = grp_num;
	}

	public int getVeg_prod_type() {
		return veg_prod_type;
	}

	public void setVeg_prod_type(int veg_prod_type) {
		this.veg_prod_type = veg_prod_type;
	}

	public List<stepwat.internal.Species> getSpecies() {
		return species;
	}

	public void setSpecies(List<stepwat.internal.Species> species) {
		this.species = species;
	}

	public float getMin_res_req() {
		return min_res_req;
	}

	public void setMin_res_req(float min_res_req) {
		this.min_res_req = min_res_req;
	}

	public float getMax_density() {
		return max_density;
	}

	public void setMax_density(float max_density) {
		this.max_density = max_density;
	}

	public float getMax_per_sqm() {
		return max_per_sqm;
	}

	public void setMax_per_sqm(float max_per_sqm) {
		this.max_per_sqm = max_per_sqm;
	}

	public float getMax_bmass() {
		return max_bmass;
	}

	public void setMax_bmass(float max_bmass) {
		this.max_bmass = max_bmass;
	}

	public float getXgrow() {
		return xgrow;
	}

	public void setXgrow(float xgrow) {
		this.xgrow = xgrow;
	}

	public float getSlowrate() {
		return slowrate;
	}

	public void setSlowrate(float slowrate) {
		this.slowrate = slowrate;
	}

	public boolean isSucculent() {
		return succulent;
	}

	public void setSucculent(boolean succulent) {
		this.succulent = succulent;
	}

	public boolean isUse_extra_res() {
		return use_extra_res;
	}

	public void setUse_extra_res(boolean use_extra_res) {
		this.use_extra_res = use_extra_res;
	}

	public boolean isUse_me() {
		return use_me;
	}

	public void setUse_me(boolean use_me) {
		this.use_me = use_me;
	}

	public boolean isUse_mort() {
		return use_mort;
	}

	public void setUse_mort(boolean use_mort) {
		this.use_mort = use_mort;
	}

	public boolean isEst_annually() {
		return est_annually;
	}

	public void setEst_annually(boolean est_annually) {
		this.est_annually = est_annually;
	}

	public DepthClass getDepth() {
		return depth;
	}

	public void setDepth(DepthClass depth) {
		this.depth = depth;
	}
	
	public String toString() {
		return this.name;
	}
}

/* COMMENT 1 - Algorithm for rgroup_PartResources() */
/*
 * Assign minimum resources to each group based on relative
 * size (which can be greater than 1.0).  Resource is computed
 * by scaling the PPT according to the slope and intercept
 * parameters in the groups input file.  Average PPT should
 * yield a resource value of 1.0.  Any resource above 1.0 is
 * committed to extra resource and is allocated in
 * _PartResExtra().
 *
 * Resource required is equal to the relative size of the group;
 * resource available is equivalent to the relative size truncated
 * at 1.0, ie, this is how a group becomes resource limited.
 * For example, a group of 1 three-quarter-sized individual
 * requires min_res_req times .75 times the resource (scaled PPT);
 * for an average PPT year this produces a resource availability
 * of  0.75.  The minimum availability needed for maintenance is
 * equivalent to the relative size <= 1.0.  If the size is < 1,
 * the plant is too small to use all of the potentially available
 * resource, so the difference is added to the pool of extra
 * resource.  If the size is > 1.0, then the plant is too large
 * to use the minimum available. On the other hand, if this is a
 * below average PPT year and the size is 1.0 (or more) the group
 * only gets min_res_req times resource < 1.0 , so groups with
 * size > space * resource will have stretched resources.
 *
 * However if this is an above average PPT year, the groups start
 * out with no more than min_res_req amount of the resource even
 * if the requirements (and size) are higher.  In any case,
 * _PartResExtra() will determine if there is enough extra
 * from other smaller groups and or above average PPT to make up
 * the difference.
 */



/*  COMMENT 3 - Algorithm for rgroup_ResPartIndiv()
 *  2-Mar-03
 *
 Originally (before 2-Mar-03), the premise for the entire scheme
 of growth modification dependent upon the ratio of required to
 available resources was probably faulty in that availability was
 limited by the size of the indiv (or group).  In retrospect,
 this doesn't make sense.  Rather the availability is defined by
 the amount of resource, irrespective of whether plants are
 available to use it, ie, min(resource, 1.0). Generally this can
 be interpreted as "free range" and any able-bodied plant is
 welcome to take what it can.  For individuals within a group
 though, it's necessary to define availability as the total
 available resource assigned to indivs based on their
 proportional size.  All the available resource is "credited" to
 the individuals based on their size (a competitive feature).

 Whereas in the past, size-limited available resource was partitioned
 according to a "cup"-based method (meaning each individual was
 like a cup of relsize filled in decreasing order until resource
 was depleted), now that isn't necessary because, and this is
 very important, small groups (< resource) have ample resource
 to meet each indiv's minimum need so all indivs get enough.

 However, for large (>= resource) groups, indivs still get available
 resource by the cup method.  For example, if this is a dry year
 with full requirements, the larger indivs get as much as they
 need (relsize amt) until the resource runs out, after which the
 smallest indivs are considered to be in severe resource deprivation
 for that year.

 For groups that are allowed to use extra resource, the pooled
 extra resource is allocated proportionally.  If a plant is
 allowed to exhibit extra (superfluous yearly) growth, the amount
 assigned is directly related to the size of the plant, ie,
 smaller plants get more of the extra resource applied to
 persistent growth (relsize) whereas larger plants get more applied
 to extra (leafy) growth.

 This general approach should work equally well whether the resource
 comes from SOILWAT or the MAP equation because it's based on the
 group PR value which is determined before this routine.  Note,
 though, that the implication is that both methods of resource
 creation imply that availability is not tied to relsize.

 */
