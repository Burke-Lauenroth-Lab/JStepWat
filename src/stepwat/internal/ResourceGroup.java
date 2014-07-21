package stepwat.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import stepwat.input.ST.Rgroup;

public class ResourceGroup {

	public static final int MAX_SPP_PER_GRP = 10;
	public static final int Ppt_Wet=0;
	public static final int Ppt_Norm=0;
	public static final int Ppt_Dry=0;
	
	public enum DepthClass {
		DepthNonComp, DepthShallow, DepthMedium, DepthDeep, DepthLast;
	}
	
	public Globals globals;
	
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
	
	public boolean isExtirpated() {
		return extirpated;
	}

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
	
	public void setInput(Rgroup.GroupType group, Rgroup.ResourceParameters rpram, boolean succulent) {
		this.setGrp_num(group.id);
		this.name = new String(name);
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
	 * put all the individuals of the group into a list
	 * and optionally sort the list. If sort==true then
	 * asc if asc==true else desc.
	 * 
	 * @param sort
	 * @param asc
	 * @return
	 */
	public LinkedList<Indiv> RGroup_GetIndivs(boolean sort, boolean asc) {
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
	public void RGroup_Update_Newsize() {
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
			LinkedList<Indiv> indivs = RGroup_GetIndivs(false,false);
			Iterator<Indiv> itr = indivs.iterator();
			while(itr.hasNext()) {
				Indiv ind = itr.next();
				ind.grp_res_prop = ind.relsize / sumsize;
			}
		}
		
		if(est_count < 0) est_count = 0;
		if(Float.compare(relsize, 0)==0) relsize=0;
	}
	
	/**
	 * When a species associated with a resource group dies
	 * out, it is dropped from the group so it will not be
	 * processed unnecessarily.
	 */
	protected void rgroup_DropSpecies(Species s) {
		if(est_spp.contains(s)) {
			est_spp.remove(s);
			est_count = est_spp.size();
		}	
	}
	
	public int getYrs_neg_pr() {
		return yrs_neg_pr;
	}
	public void setYrs_neg_pr(int yrs_neg_pr) {
		this.yrs_neg_pr = yrs_neg_pr;
	}
	public int getEst_count() {
		return est_count;
	}
	public void setEst_count(int est_count) {
		this.est_count = est_count;
	}
	public void setExtirpated(boolean extirpated) {
		this.extirpated = extirpated;
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
}
