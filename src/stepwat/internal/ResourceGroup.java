package stepwat.internal;

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
	
	/**** Quantities that can change during model runs *****/

	int[] kills; /* indivs in group killed. index by age killed. */
	int estabs, /* total indivs in group established during iter */
	killyr, /* kill the group in this year; if 0, don't kill, but see killfreq */
	yrs_neg_pr, /* counter for consecutive years low resources */
	mm_extra_res; /* extra resource converted back to mm */
	float res_required, /* resource required for current size */
	res_avail, /* resource available from environment X competition */
	res_extra, /* if requested, resource above 1.0 when PR < 1.0 */
	pr, /* resources required / resources available */
	relsize; /* size of all species' indivs' relsizes scaled to 1.0 */
	int est_count; /* number of species actually established in group */
	int[] est_spp = new int[MAX_SPP_PER_GRP]; /* list of spp actually estab in grp */
	boolean extirpated, /* group extirpated, no more regen */
	regen_ok; /* annuals: comb. of startyr, etc means we can regen this year */

	/**** Quantities that DO NOT change during model runs *****/

	int max_stretch, /* num yrs resources can be stretched w/o killing */
	max_spp_estab, /* max # species that can add new plants per year */
	max_spp, /* number of species in the group */
	max_age, /* longest lifespan in group. used to malloc kills[] */
	startyr, /* don't start trying to grow until this year */
	killfreq, /* kill group at this frequency: <1=prob, >1=# years */
	extirp, /* year in which group is extirpated (0==ignore) */
	grp_num, /* index number of this group */
	veg_prod_type; /* type of VegProd. 1 for tree, 2 for shrub, 3 for grass */
	int[] species = new int[MAX_SPP_PER_GRP]; /* list of spp belonging to this grp */
	float min_res_req, /* input from table */
	max_density, /* number of mature plants per plot allowed */
	max_per_sqm, /* convert density and plotsize to max plants/m^2 */
	max_bmass, /* sum of mature biomass for all species in group */
	xgrow, /* ephemeral growth = mm extra ppt * xgrow */
	slowrate; /* user-defined growthrate that triggers mortality */
	float[] ppt_slope = new float[3]; /* res. space eqn: slope for wet/dry/norm yrs */
	float[] ppt_intcpt = new float[3];/* res. space eqn: intercept for "" "" */
	boolean succulent, use_extra_res, /* responds to other groups' unused resources */
	use_me, /* establish no species of this group if false */
	use_mort, /* use age-independent+slowgrowth mortality? */
	est_annually; /* establish this group every year if true */
	DepthClass depth; /* rooting depth class */
	String name;
	
	public void setInput(Rgroup.GroupType group, Rgroup.ResourceParameters rpram, boolean succulent, int groupNumber) {
		/*
		C-str = C-code = Java
		name = name
		space = space
		max_density = density = density
		max_spp_estab = estab =	maxest
		slow = slow
		max_stretch = stretch = stretch
		xres = 	xres
		estann = estann
		turnon = on
		styr = startyr
		killyr = killyr
		killfreq = killfreq
		extirp = extirp
		mort = mort
		xgro = xgrow
		veg_prod_type = veg_prod_type
		
		max_per_sqm = (density = density) / Globals.plotsize
		 */
		this.grp_num = groupNumber;
		this.name = new String(name);
		this.max_stretch = group.stretch;
		this.max_spp_estab = group.maxest;
		this.max_density = group.density;
		this.max_per_sqm = group.density / globals.plotsize;
		this.slowrate = group.slow;
		this.min_res_req = group.space;
		this.est_annually = group.estann;
		this.startyr = group.startyr;
		this.killyr = group.killyr;
		this.killfreq = group.killfreq;
		this.use_extra_res = group.xres;
		this.extirp = group.extirp;
		this.xgrow = group.xgrow;
		this.use_me = group.on;
		this.use_mort = group.mort;
		this.veg_prod_type = group.veg_prod_type;

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
			this.succulent = true;
		}
	}
}
