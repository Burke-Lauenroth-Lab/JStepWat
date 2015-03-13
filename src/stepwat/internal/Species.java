package stepwat.internal;

import java.util.Iterator;
import java.util.LinkedList;

import soilwat.Defines;
import stepwat.LogFileIn;
import stepwat.LogFileIn.LogMode;

public class Species {
	
	Globals globals;
	
	public enum MortalityType {
		Slow, NoResources, Intrinsic, Disturbance, LastMort;
	}
	
	public enum TempClass {
		NoSeason, CoolSeason, WarmSeason;
	}
	
	public enum DisturbClass {
		VerySensitive, Sensitive, Insensitive, VeryInsensitive;
	}
	
	// Quantities that can change during model runs //
	/**
	 * number of individuals established (growing)<br>
	 * <b>Use Indvs.size()</b>
	 * @deprecated
	 */
	@SuppressWarnings("unused")
	private int est_count;
	/**
	 * Array of # indivs killed by age. index=age
	 */
	protected int[] kills;
	/**
	 * number of individuals established in iter
	 */
	protected int estabs;
	/**
	 * size of all indivs' relsize (>= 0)
	 */
	protected float relsize=0;
	/**
	 * annuals: array of previous years' seed production (size = viable_yrs)
	 */
	protected float[] seedprod;
	/**
	 * amt of superfluous growth from extra resources
	 */
	protected float extragrowth=0;
	/**
	 * the chance that this species received seeds this year... only applicable if using seed dispersal and gridded option
	 */
	protected float received_prob;
	/**
	 * facility for linked list for indvs
	 */
	protected LinkedList<Indiv> Indvs = new LinkedList<Indiv>();
	/**
	 * whether to allow growth this year... only applicable if using seed dispersal and gridded option
	 */
	protected boolean allow_growth;
	/**
	 * whether seeds where produced/received and germinated this year... only applicable if using seed dispersal and gridded option
	 */
	protected boolean sd_sgerm;
	
	// Quantities that DO NOT change during model runs
	/**
	 * Name of this Species
	 */
	private String name;
	/**
	 * max age of mature plant, also flag for annual
	 */
	private int max_age;
	/**
	 * annuals: max years of viability of seeds
	 */
	private int viable_yrs;
	/**
	 * max seedlings that can estab in 1 yr
	 */
	private int max_seed_estab;
	/**
	 * max vegetative regrowth units (eg tillers)
	 */
	private int max_vegunits;
	/**
	 * years slow growth allowed before mortality
	 */
	private int max_slow;
	/**
	 * index number of this species
	 */
	private int sp_num;
	/**
	 * this sp. belongs to this res_grp
	 */
	private ResourceGroup res_grp;
	/**
	 * intrin_rate * proportion
	 */
	private float max_rate;
	private float intrin_rate;
	private float relseedlingsize;
	private float seedling_biomass;
	/**
	 * biomass of mature_size individual
	 */
	private float mature_biomass;
	/**
	 * supports Extirpate() and Kill()
	 */
	private float seedling_estab_prob_old;
	private float seedling_estab_prob;
	private float ann_mort_prob;
	private float cohort_surv;
	/**
	 * annuals: exponent for viability decay function
	 */
	private float exp_decay;
	/**
	 * 1 value for each mortality type, if clonal
	 */
	private float[] prob_veggrow = new float[4];
	/**
	 * for seed dispersal
	 */
	private float sd_Param1;
	/**
	 * for seed dispersal
	 */
	private float sd_PPTdry;
	/**
	 * for seed dispersal
	 */
	private float sd_PPTwet;
	/**
	 * for seed dispersal
	 */
	private float sd_Pmin;
	/**
	 * for seed dispersal
	 */
	private float sd_Pmax;
	/**
	 * for seed dispersal
	 */
	private float sd_H;
	/**
	 * for seed dispersal
	 */
	private float sd_VT;
	private TempClass tempclass;
	private DisturbClass disturbClass;
	private boolean isclonal;
	private boolean use_temp_response;
	/**
	 * do not establish if this is false
	 */
	private boolean use_me;
	/**
	 * whether to use seed dispersal... only applicable if using gridded option
	 */
	private boolean use_dispersal;
	
	public Species(Globals g) {
		this.globals = g;
	}
	
	public void setInput(stepwat.input.ST.Species.SpeciesParams species, ResourceGroup group,
			stepwat.input.ST.Species.AnnualsParams aparams, stepwat.input.ST.Species.SpeciesProbParam sprobparam,
			stepwat.input.ST.Species.SeedDispersalParam seedparam, int index) {
		this.setName(species.name);
		this.setSp_num(index);
		this.setRes_grp(group);
		this.setMax_age(species.age);
		this.setIntrin_rate(species.irate);
		this.setMax_rate(species.irate * species.ratep);
		this.setMax_slow(species.slow);
		switch(species.disturb) {
		case 1:
			this.setDisturbClass(DisturbClass.VerySensitive);
			break;
		case 2:
			this.setDisturbClass(DisturbClass.Sensitive);
			break;
		case 3:
			this.setDisturbClass(DisturbClass.Insensitive);
			break;
		case 4:
			this.setDisturbClass(DisturbClass.VerySensitive);
			break;
		}
		this.setSeedling_estab_prob(species.pestab);
		this.setSeedling_estab_prob_old(species.pestab);
		this.setMax_seed_estab(species.eind);
		this.setSeedling_biomass(species.minbio);
		this.setMature_biomass(species.maxbio);
		this.setTempclass((species.tclass==1)?TempClass.WarmSeason:(species.tclass==2)? TempClass.CoolSeason : TempClass.NoSeason);
		this.setRelseedlingsize(species.minbio / species.maxbio);
		this.setIsclonal(species.clonal);
		this.setMax_vegunits((species.clonal) ? species.vegindv : 0);
		this.setUse_me((group.isUse_me())?species.onoff:false);
		this.received_prob = 0;
		this.setCohort_surv(species.cosurv);
		
		if(aparams != null) {
			this.setViable_yrs(aparams.viable);
			this.setExp_decay(aparams.xdecay);
			this.seedprod = new float[aparams.viable];
		}
		
		this.prob_veggrow[MortalityType.NoResources.ordinal()] = sprobparam.vprop1;
		this.prob_veggrow[MortalityType.Slow.ordinal()] = sprobparam.vprop2;
		this.prob_veggrow[MortalityType.Intrinsic.ordinal()] = sprobparam.vprop3;
		this.prob_veggrow[MortalityType.Disturbance.ordinal()] = sprobparam.vprop4;

		this.setUse_dispersal(seedparam.dispersal);
		this.allow_growth = true;
		this.sd_sgerm = false;
		this.setSd_Param1(seedparam.param1);
		this.setSd_PPTdry(seedparam.pptDry);
		this.setSd_PPTwet(seedparam.pptWet);
		this.setSd_Pmin(seedparam.pmin);
		this.setSd_Pmax(seedparam.pmax);
		this.setSd_H(seedparam.h);
		this.setSd_VT(seedparam.vt);
		
	}
	/**
	 * 
	 * @return 0 or more seedlings that establish
	 */
	public int numEstablish() {
		//special conditions if we're using the grid and seed dispersal options
		if(globals.UseGrid && globals.UseSeedDispersal) {
			if (sd_sgerm) {
				if (max_seed_estab <= 1) {
					return 1;
				} else {
					return globals.random.RandUniRange(1, max_seed_estab);
				}
			} else {
				return 0;
			}
		}
		//float biomass = relsize * mature_biomass;
		if(res_grp.isEst_annually() || Defines.LT(globals.random.RandUni(), seedling_estab_prob) || sd_sgerm) {
			if(max_seed_estab <= 1)
				return 1;
			else
				return globals.random.RandUniRange(1, max_seed_estab);
		} else {
			return 0;
		}
	}
	
	/**
	 * relsize * mature_biomass
	 * @return
	 */
	public float getBiomass() {
		return this.relsize * this.mature_biomass;
	}
	
	public void addIndiv(int new_indivs) throws Exception {
		if(0 == new_indivs) return;
		
		float newsize = 0;
		for(int i=1; i<=new_indivs; i++) {
			new Indiv(globals, this);
			newsize += relseedlingsize;
		}
		
		res_grp.addSpecies(this);
		update_Newsize(newsize);
	}
	
	/**
	 * This is the point at which any changes in the individuals,<br>
	 * whether by growth or mortality, is reflected in the overall<br>
	 * relative size at the Species and RGroup levels.
	 * 
	 * @param newsize
	 * @throws Exception 
	 */
	public void update_Newsize(float newsize) throws Exception {
		LogFileIn f = stepwat.LogFileIn.getInstance();
		
		//if this cond. true, we're off a bit from zeroing. fix it
		if (Indvs.size() == 1 && newsize < -this.relsize) {
			newsize = -this.relsize;
		}

		this.relsize += newsize;
		if (Defines.isZero(relsize) || Math.abs(relsize) < 1E-5)
			relsize = 0;
		
		if(this.relsize < 0.0) {
			f.LogError(LogMode.WARN, "Species_Update_Newsize: " + this.res_grp.getName() + " : " + name
					+ " relsize < 0.0 (=" + String.valueOf(relsize)
					+ ")  year=" + String.valueOf(globals.getCurrYear())
					+ ", iter=" + String.valueOf(globals.getCurrIter()));
			this.relsize = 0;
		}
		if(this.relsize > 100.0) {
			f.LogError(LogMode.NOTE, "Species_Update_Newsize: " + name
					+ " relsize very large (=" + String.valueOf(relsize)
					+ ") year=" + String.valueOf(globals.getCurrYear())
					+ ", iter=" + String.valueOf(globals.getCurrIter()));
		}
		
		this.res_grp.update_Newsize();
		
	}
	
	/**
	 * Kill all established individuals in a species.
	 * @throws Exception
	 */
	public void kill() throws Exception {
		if(this.max_age ==1) {
			update_Newsize(-relsize);
		} else {
			Iterator<Indiv> iter = Indvs.iterator();
			while(iter.hasNext()) {
				Indiv p = iter.next();
				p.kill_Complete(null, iter);
			}
		}
		res_grp.dropSpecies(null, this);
	}
	
	/**
	 * accumulate frequencies of kills by age for survivorship
	 * 'kills' is a pointer to a dynamically sized array created
	 * in params_check_species().
	 * @param age
	 */
	public void update_Kills(int age) {
		if(this.kills != null) {
			age--;
			//a quick check to keep from writing off the end of the kills arrays (which was happening in really obscure cases)...TODO: see why?
			if(age >= max_age)
				return;
			kills[age]++;
			res_grp.kills[age]++;
		}
	}
	
	/**
	 * accumulate number of indivs established by species
	 * for fecundity rates.
	 * @param num
	 */
	public void updateEstabs(int num) {
		estabs += num;
		res_grp.estabs += num;
	}
	
	public int getEst_count() {
		return this.Indvs.size();
	}

	public void clearEstIndv() {
		this.Indvs.clear();
	}

	public float getRelsize() {
		return relsize;
	}
	
	public void setRelsize(float relsize) {
		this.relsize = relsize;
	}
	
	public int[] getKills() {
		return kills;
	}
	
	public void setKills(int[] kills) {
		this.kills = kills;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getMax_age() {
		return max_age;
	}

	public void setMax_age(int max_age) {
		this.max_age = max_age;
	}

	public int getViable_yrs() {
		return viable_yrs;
	}

	public void setViable_yrs(int viable_yrs) {
		this.viable_yrs = viable_yrs;
	}

	public int getMax_seed_estab() {
		return max_seed_estab;
	}

	public void setMax_seed_estab(int max_seed_estab) {
		this.max_seed_estab = max_seed_estab;
	}

	public int getMax_vegunits() {
		return max_vegunits;
	}

	public void setMax_vegunits(int max_vegunits) {
		this.max_vegunits = max_vegunits;
	}

	public int getMax_slow() {
		return max_slow;
	}

	public void setMax_slow(int max_slow) {
		this.max_slow = max_slow;
	}

	public int getSp_num() {
		return sp_num;
	}

	public void setSp_num(int sp_num) {
		this.sp_num = sp_num;
	}

	public ResourceGroup getRes_grp() {
		return res_grp;
	}

	public void setRes_grp(ResourceGroup res_grp) {
		this.res_grp = res_grp;
	}

	public float getMax_rate() {
		return max_rate;
	}

	public void setMax_rate(float max_rate) {
		this.max_rate = max_rate;
	}

	public float getIntrin_rate() {
		return intrin_rate;
	}

	public void setIntrin_rate(float intrin_rate) {
		this.intrin_rate = intrin_rate;
	}

	public float getRelseedlingsize() {
		return relseedlingsize;
	}

	public void setRelseedlingsize(float relseedlingsize) {
		this.relseedlingsize = relseedlingsize;
	}

	public float getSeedling_biomass() {
		return seedling_biomass;
	}

	public void setSeedling_biomass(float seedling_biomass) {
		this.seedling_biomass = seedling_biomass;
	}

	public float getMature_biomass() {
		return mature_biomass;
	}

	public void setMature_biomass(float mature_biomass) {
		this.mature_biomass = mature_biomass;
	}

	public float getSeedling_estab_prob_old() {
		return seedling_estab_prob_old;
	}

	public void setSeedling_estab_prob_old(float seedling_estab_prob_old) {
		this.seedling_estab_prob_old = seedling_estab_prob_old;
	}

	public float getSeedling_estab_prob() {
		return seedling_estab_prob;
	}

	public void setSeedling_estab_prob(float seedling_estab_prob) {
		this.seedling_estab_prob = seedling_estab_prob;
	}

	public float getAnn_mort_prob() {
		return ann_mort_prob;
	}

	public void setAnn_mort_prob(float ann_mort_prob) {
		this.ann_mort_prob = ann_mort_prob;
	}

	public float getCohort_surv() {
		return cohort_surv;
	}

	public void setCohort_surv(float cohort_surv) {
		this.cohort_surv = cohort_surv;
	}

	public float getExp_decay() {
		return exp_decay;
	}

	public void setExp_decay(float exp_decay) {
		this.exp_decay = exp_decay;
	}

	public float[] getProb_veggrow() {
		return prob_veggrow;
	}

	public void setProb_veggrow(float[] prob_veggrow) {
		this.prob_veggrow = prob_veggrow;
	}

	public float getSd_Param1() {
		return sd_Param1;
	}

	public void setSd_Param1(float sd_Param1) {
		this.sd_Param1 = sd_Param1;
	}

	public float getSd_PPTdry() {
		return sd_PPTdry;
	}

	public void setSd_PPTdry(float sd_PPTdry) {
		this.sd_PPTdry = sd_PPTdry;
	}

	public float getSd_PPTwet() {
		return sd_PPTwet;
	}

	public void setSd_PPTwet(float sd_PPTwet) {
		this.sd_PPTwet = sd_PPTwet;
	}

	public float getSd_Pmin() {
		return sd_Pmin;
	}

	public void setSd_Pmin(float sd_Pmin) {
		this.sd_Pmin = sd_Pmin;
	}

	public float getSd_Pmax() {
		return sd_Pmax;
	}

	public void setSd_Pmax(float sd_Pmax) {
		this.sd_Pmax = sd_Pmax;
	}

	public float getSd_H() {
		return sd_H;
	}

	public void setSd_H(float sd_H) {
		this.sd_H = sd_H;
	}

	public float getSd_VT() {
		return sd_VT;
	}

	public void setSd_VT(float sd_VT) {
		this.sd_VT = sd_VT;
	}

	public DisturbClass getDisturbClass() {
		return disturbClass;
	}

	public void setDisturbClass(DisturbClass disturbClass) {
		this.disturbClass = disturbClass;
	}

	public TempClass getTempclass() {
		return tempclass;
	}

	public void setTempclass(TempClass tempclass) {
		this.tempclass = tempclass;
	}

	public boolean isIsclonal() {
		return isclonal;
	}

	public void setIsclonal(boolean isclonal) {
		this.isclonal = isclonal;
	}

	public boolean isUse_temp_response() {
		return use_temp_response;
	}

	public void setUse_temp_response(boolean use_temp_response) {
		this.use_temp_response = use_temp_response;
	}

	public boolean isUse_dispersal() {
		return use_dispersal;
	}

	public void setUse_dispersal(boolean use_dispersal) {
		this.use_dispersal = use_dispersal;
	}

	public boolean isUse_me() {
		return use_me;
	}

	public void setUse_me(boolean use_me) {
		this.use_me = use_me;
	}
	
	public String toString() {
		return this.getRes_grp().getName()+":"+this.name+":"+String.valueOf(this.relsize);
	}
}
