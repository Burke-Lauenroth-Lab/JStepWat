package stepwat.internal;

import java.util.LinkedList;

public class Species {
	
	public enum MortalityType {
		Slow, NoResources, Intrinsic, Disturbance, LastMort;
	}
	
	public enum TempClass {
		NoSeason, CoolSeason, WarmSeason;
	}
	
	public enum DisturbClass {
		VerySensitive, Sensitive, Insensitive, VeryInsensitive;
	}
	
	public class Indiv {
		int age, mm_extra_res,
			slow_yrs,  /* number of years this individual has slow growth */
			myspecies,
			yrs_neg_pr; /* num yrs pr > 1 (stretched resources) */
		MortalityType killedby;
		boolean killed;
		float relsize,      /* relative to full-sized individual -- 0-1.0) */
	    	grp_res_prop,  /* prop'l contribution this indiv makes to group relsize */
	    	res_required, /* min_res_req * relsize */
	    	res_avail,    /* resource * min_res_req */
	    	res_extra,    /* resource applied to superficial growth */
	    	pr,           /* ratio of resources required to amt available */
	    	growthrate,   /* actual growth rate*/
	    	prob_veggrow; /* set when killed; 0 if not clonal*/
	}
	
	/**** Quantities that can change during model runs *****/
	int est_count; /* number of individuals established (growing)*/
	int kills[]; /* Array of # indivs killed by age. index=age. */
	int estabs; /* number of individuals established in iter */
	float relsize; /* size of all indivs' relsize (>= 0) */
	float seedprod[]; /* annuals: array of previous years' seed production (size = viable_yrs)*/
	float extragrowth; /* amt of superfluous growth from extra resources */
	float received_prob; /* the chance that this species received seeds this year... only applicable if using seed dispersal and gridded option */
	LinkedList<Indiv> IndvHead; /* facility for linked list 8/3/01; top of list */
	boolean allow_growth, /* whether to allow growth this year... only applicable if using seed dispersal and gridded option */
		sd_sgerm; /* whether seeds where produced/received and germinated this year... only applicable if using seed dispersal and gridded option */
	
	/**** Quantities that DO NOT change during model runs *****/
	String name;
	int max_age,         /* max age of mature plant, also flag for annual */
		viable_yrs,      /* annuals: max years of viability of seeds */
		max_seed_estab,  /* max seedlings that can estab in 1 yr*/
		max_vegunits,    /* max vegetative regrowth units (eg tillers)*/
		max_slow,        /* years slow growth allowed before mortality */
		sp_num,          /* index number of this species */
		res_grp;         /* this sp. belongs to this res_grp*/
	float max_rate,      /* intrin_rate * proportion*/
      intrin_rate,
      relseedlingsize,
      seedling_biomass,
      mature_biomass,    /* biomass of mature_size individual */
      seedling_estab_prob_old,  /* supports Extirpate() and Kill() */
      seedling_estab_prob,
      ann_mort_prob,
      cohort_surv,
      exp_decay,        /* annuals: exponent for viability decay function */
      prob_veggrow[],  /* 1 value for each mortality type, if clonal*/
      sd_Param1,	  /* for seed dispersal */
      sd_PPTdry,
      sd_PPTwet,
      sd_Pmin,
      sd_Pmax,
      sd_H,
      sd_VT;
	TempClass tempclass;
	DisturbClass disturbClass;
	boolean isclonal,
    	use_temp_response,
    	use_me,           /* do not establish if this is false */
    	use_dispersal;	 /* whether to use seed dispersal... only applicable if using gridded option */
}
