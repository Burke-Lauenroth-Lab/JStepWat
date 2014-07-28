package stepwat.internal;

import java.util.ArrayList;
import java.util.List;

import stepwat.LogFileIn;
import stepwat.LogFileIn.LogMode;
import stepwat.internal.Environs.PPTClass;
import stepwat.internal.Species.DisturbClass;
import stepwat.internal.Species.MortalityType;

public class Mortality {
	
	Globals globals;
	Plot plot;
	Environs env;
	Succulent succulent;
	RGroups rgroups;
	
	//flag : some plant was reduced and PR is affected
	private boolean someKillage;
	
	public Mortality(Globals g, Plot p, Succulent s, Environs e, RGroups rgs) {
		this.globals = g;
		this.plot = p;
		this.env = e;
		this.succulent = s;
		this.rgroups = rgs;
	}
	/**
	 * This routine contains all the references to mortality
	 * that occurs during the growing season.  (See mort_EndOfYear()
	 * for other routines.)  It reduces "plant-space" according to
	 * resource availability, age, growth rate, and disturbance
	 * conditions.  Also, succulents are "mortified" if it's a wet
	 * year.
	 * <br>
	 * An important consideration is the fact that the age of new
	 * plants starts at 1.  Consider that for it to establish, it
	 * has already survived the massive mortality of year 0; this
	 * rate of year 0 mortality, among other factors of survival,
	 * is captured by the probability of establishment (see
	 * rgroup_Establish()).  However, for consistency with other
	 * arrays, the age-related arrays are indexed from 0.  Thus,
	 * age is base1 but the arrays are base0.
	 * <br>
	 * ALGORITHM<br>
	 * The outline of the steps is:<br>
	 * no_resources(group)  - eqns 7, 8, 9<br>
	 * age_independent(spp) - eqn 14<br>
	 * slow_growth(spp) - slow growth rate, constant probability<br>
	 * succulents(sp) - if wet year<br>
	 * Mort based on disturbances.<br>
	 * <br>
	 * More specifically:<br>
	 * Process each group separately.<br>
	 * If resources required for a group are greater than available
	 * (PR > 1) for the maximum years stretching is allowed, kill
	 * plants according the rules for insufficient resources.  If
	 * PR < 1, reset number of stretched years to zero.  This means
	 * that even one year of adequate resources nulls any number of
	 * years of stretched resources.<br>
	 * For each species, if the user requested the mortality functions
	 * to be executed, run the age-independent and slow-growth
	 * routines.<br>
	 * If the current species is a succulent and this is a wet year,
	 * execute the mortality function for succulents.  That is,
	 * reduce each plant by some constant proportion, defined by
	 * eqn 16.  The parameters are defined in the group-parms and
	 * the reduction amount is computed in the Env module based
	 * on precipitation.<br>
	 * execute disturbance effects, if any.
	 * @throws Exception 
	 */
	public boolean mortalityMain() throws Exception {
		
		for(ResourceGroup g : rgroups) {
			if(g.est_spp.size() == 0) continue;
			//annuals get theirs in EndOfYear()
			if(g.getMax_age() == 1) continue;
			
			//mortify plants if low resources for consecutive years
			//increment yrs_neg_pr if pr > 1, else zero it.
			//one good year cancels all previous bad years.
			if(Float.compare(g.pr, 1.0f) > 0) {//GT
				if(++g.yrs_neg_pr >= g.getMax_stretch())
					noResources(g);
			} else {
				g.yrs_neg_pr = 0;
			}
			for(int i=g.est_spp.size()-1; i>=0; i--) {
				Species s = g.est_spp.get(i);
				//Take care of mortality types 1 and 2
				if(g.isUse_mort()) {
					ageIndependent(s);
					slowGrowth(s);
				}
				//now deal with succulents problems
				if(g.isSucculent() && env.wet_dry == PPTClass.Ppt_Wet && globals.random.RandUni() <= succulent.prob_death) {
					succulents(s);
				}
				//finally, implement disturbance mortality
				switch (plot.disturbance) {
				case FecalPat:
					pat(s);
					break;
				case AntMound:
					mound(s);
					break;
				case Burrow:
					burrow(s);
					break;
				default:
					break;
				}
			}//end for each species
		}//end ForEachGroup
		
		return someKillage;
	}
	
	/**
	 * Perform the sorts of mortality one might expect at the
	 * end of the growing season, like killing annuals and
	 * extra growth.
	 * 
	 * @param globals
	 * @param RGroup
	 * @throws Exception
	 */
	public void endOfYear() throws Exception {
		
		for(ResourceGroup g : rgroups) {
			if(Float.compare(g.getKillfreq(), 0.0f) > 0) {//GT
				if(Float.compare(g.getKillfreq(), 1.0f) < 0) {//LT
					if(globals.random.RandUni() <= g.getKillfreq())
						g.killyr = globals.currYear;
				} else if((globals.currYear - g.getStartyr()) % g.getKillfreq() == 0) {
					g.killyr = globals.currYear;
				}
			}
			
			if(globals.currYear == g.getExtirp()) {
				g.extirpate();
			} else if(globals.currYear == g.killyr) {
				g.kill();
			}
		}
		killExtraGrowth();
		killAnnuals();
	}
	
	private void pat(Species sp) throws Exception {
		
		//Generate kill list, depending on sensitivity
		if(plot.pat_removed) {
			//get list of seedlings and annuals
			for(Indiv p : sp.Indvs) {
				if(p.age == 1 || sp.getDisturbClass() == DisturbClass.VerySensitive) {
					p.kill_Complete();
					this.someKillage = true;
				}
			}
		} else {
			//kill according to disturbance class
			switch(sp.getDisturbClass()) {
			case VerySensitive:
			case Sensitive:
				sp.kill();
				this.someKillage = true;
				break;
			case Insensitive:
			case VeryInsensitive:
				//unaffected
			default:
				break;
			}
		}
	}
	/**
	 * Ant mounds kill all but the hardiest plants. In C&L-90 that would be the succulents.
	 * @param sp
	 * @throws Exception
	 */
	private void mound(Species sp) throws Exception {
		switch(sp.getDisturbClass()) {
		case VerySensitive:
		case Sensitive:
		case Insensitive:
			sp.kill();
			this.someKillage = true;
			break;
		case VeryInsensitive:
			//unaffected
		default:
			break;
		}
	}
	/**
	 * Kills all individuals on the plot if a burrow occurs.
	 * @param sp
	 * @throws Exception
	 */
	private void burrow(Species sp) throws Exception {
		switch(sp.getDisturbClass()) {
		case VerySensitive:
		case Sensitive:
		case Insensitive:
		case VeryInsensitive:
			sp.kill();
			this.someKillage = true;
			break;
			//unaffected
		default:
			break;
		}
	}
	
	private void succulents(Species sp) throws Exception {
		float killamt = succulent.reduction;
		for(Indiv p : sp.Indvs) {
			if(Float.compare(p.relsize, killamt) > 0) {//GT
				p.kill_Partial(MortalityType.Slow, killamt);
			} else {
				p.kill_Complete();
				this.someKillage = true;
			}
		}
	}
	/**
	 * Kill plants based on a probability if the growth rate
	 * is less than the "slow rate" which is defined by the
	 * user in the group-level parameters (max_slow) and in
	 * the species-level parameters (max_rate).  the slow rate
	 * is growthrate <= max_slow * max_rate.
	 * <br>
	 * Increment the counter for number of years of slow growth.
	 * If the number of years of slow growth is greater than
	 * max_slow (defined in species parms), get a random number
	 * and test it against the probability of death.  C&L'90
	 * defines this value as a constant, but it might be better
	 * to define it in the groups or species parameters.
	 * <br>
	 * Of course, annuals aren't subject to this mortality,
	 * nor are new plants.
	 * @param sp
	 * @throws Exception 
	 */
	private void slowGrowth(Species sp) throws Exception {
		//probability of mortality
		float pm = 0.368f;
		
		float slowrate = sp.getRes_grp().getSlowrate() * sp.getMax_rate();
		
		for(int i=sp.Indvs.size()-1; i>=0; i--) {
			Indiv ndv = sp.Indvs.get(i);
			if(ndv.age == 1)
				continue;
			if(ndv.growthrate <= slowrate) {
				ndv.slow_yrs++;
				//add to kill list if pm met
				if(ndv.slow_yrs >= sp.getMax_slow() && globals.random.RandUni() <= pm) {
					ndv.kill_Complete();
					this.someKillage = true;
				}
			} else {
				ndv.slow_yrs = Math.max(ndv.slow_yrs - 1, 0);
			}
		}
	}
	/**
	 * kills possibly all individuals in a species
	 * by the age-independent function (eqn 14) in C&L'90
	 * assuming that AGEMAX was defined.
	 * @param sp
	 * @throws Exception 
	 */
	private void ageIndependent(Species sp) throws Exception {
		//probability of mortality by year n (eqn 14)
		float pn;
		float a;
		
		if(sp.getMax_age() == 0) {
			//TODO: ERROR?
		}
		
		if(sp.getMax_age() == 1)
			return;
		
		for(int i=sp.Indvs.size()-1; i>=0; i--) {
			Indiv ndv = sp.Indvs.get(i);
			a = (float) ndv.age / sp.getMax_age();
			pn = (float) Math.pow((double) sp.getMax_age(), (double) a - 1) - (a*sp.getCohort_surv());
			//kill if pn met
			if(globals.random.RandUni() <= pn) {
				ndv.kill_Complete();
				this.someKillage = true;
			}
		}	
	}
	/**
	 * use EQN 7, 8, 9 prior to growing
	 * Lack of resources require a reduction in "plant-space"
	 * (ie, individuals or portions thereof) such that
	 * plant-space balances resource-space.  In reality, this
	 * would happen gradually over the season, but in this
	 * yearly-time-step model it has to be done either before
	 * or after the growth routine.  C&L 1990 (p241) note that
	 * growth rates are also reduced--this is done in the main
	 * growth loop by setting gmod = 1/PR.<br>
	 * Note that the group's PR MUST BE > 1.0 BY THIS TIME.
	 * Normally this is tested for in mort_Main().<br>
	 * This routine also calls _stretched_clonal() to kill
	 * additional amounts of clonal plants (if any), which also
	 * happens due to insufficient resources.
	 * @throws Exception 
	 */
	private void noResources(ResourceGroup rg) throws Exception {
		int i, nk, n;
		List<Indiv> indv_list;
		
		//Sorted List of Indv in this group
		indv_list = rg.getIndivs(true, true);
		n = indv_list.size();
		//kill until nk reached (EQN 7)
		nk = (int) ((n * (1.0f - 1.0f / rg.pr)) + .5f);
		for(i=0; i<nk; i++) {
			indv_list.get(i).kill_Complete();
			this.someKillage = true;
		}
		
		/*
		 * Check to see if this group's resources have been stretched, and
		 * commit mortality of clonal plants which get additional killamt if
		 * they are not killed in the preceeding loop. Pick up next largest
		 * individual from where we left off in previous loop by reusing i
		 * without resetting it to 0. i comes out of the loop pointing to the
		 * next living plant. If for some reason all plants were killed,
		 * _stretched_clonal bails before doing anything.
		 */
		stretchedClonal(rg, i, n-1, indv_list);
	}
	
	private void stretchedClonal(ResourceGroup rg, int start, int last, List<Indiv> nlist) throws Exception {
		LogFileIn f = stepwat.LogFileIn.getInstance();
		int i,y, //number of years of stretched resources
		nk; //number of clonal plants to kill if pm met
		float pm; //probablity of mortality (eqn 8)
		
		//these are used if reducing proportionally (pm not met)
		float total_size=0.0f, indiv_size, total_reduction, indiv_reduction;
		
		//list of clonals
		List<Indiv> clist = new ArrayList<Indiv>();
		
		//get a list of remaining clonal plants. Still in order of size
		for(Indiv ndv : nlist) {
			if(ndv.myspecies.isIsclonal())
				clist.add(ndv);
		}
		if(clist.isEmpty())
			return;
		
		y = rg.yrs_neg_pr;
		
		if(y>=rg.getMax_stretch()) {
			pm = 0.04f * y * y; //EQN 8
			
			if(globals.random.RandUni() <= pm) {
				/* kill on quota basis*/
				/* must be more than 10 plants for any to survive ?*/
				/* if so, then use ceil(), otherwise, use floor()*/
				nk = (int) Math.floor(((double) (clist.size()) * 0.9f));
				
				//kill until we reach quota or number of plants
				nk = Math.min(nk, clist.size());
				for(i=0; i<nk; i++) {
					clist.get(i).kill_Complete();
					if(i==0)
						this.someKillage = true;
				}
			} else {//reduce inverse-proportionally
				total_reduction = 1.0f / rg.pr;
				//Making sure PR will always be > 1 here
				if(total_reduction > 1.0f)
					f.LogError(LogMode.FATAL, "PR too large in Mort: StretchClonal\n");
				//sum up relsizes for total size
				for(i=0; i<clist.size(); i++) {
					total_size += clist.get(i).relsize;
				}
				/*
				 * the 0.8 is a "magic" correction to prevent thereduction from
				 * being too large; the assumptionis that the plants are hardy
				 * enough to survivewithout dying beyond what is necessary to
				 * makeresources required exactly <= availability.
				 */
				total_reduction *= 0.8f;
				for(i=0; i<clist.size(); i++) {
					indiv_size = clist.get(i).relsize / total_size;
					indiv_reduction = indiv_size * total_reduction;
					//always succeeds if magic number < 1.0
					clist.get(i).kill_Partial(MortalityType.NoResources, indiv_reduction);
				}
				if(clist.size() > 0)
					this.someKillage = true;
			}
		}
	}
	/**
	 * Loop through all species and kill the annuals.  This
	 * routine should be called at the end of the year after
	 * all growth happens and statistics are calculated and
	 * we don't need to know about the annuals any more.
	 * <br>
	 * The assumption, of course, is that all of the annual
	 * species that are established are indeed one year old.
	 * See the discussion at the top of this file and in
	 * indiv_create() for more details.
	 * @throws Exception 
	 */
	private void killAnnuals() throws Exception {
		for(ResourceGroup rg : rgroups) {
			if(rg.getMax_age() == 1) {
				for(int i=rg.est_spp.size()-1; i>=0; i--) {
					rg.est_spp.get(i).kill();
				}
			}
		}
	}
	/**
	 * Remove any extra growth accumulated during the growing
	 * season.  This should be done after all the statistics
	 * are accumulated for the year.
	 * <br>
	 * Note that extra growth is not stored by the individuals
	 * but only by Species and RGroup.
	 * <br>
	 * This should probably be split into separate functions
	 * so the actual killing of extra growth is one function
	 * that can be called from anywhere, and the looping is
	 * in another function with another name.
	 * That way, individual functions can kill specific
	 * extra growth without affecting others.
	 * @throws Exception 
	 */
	private void killExtraGrowth() throws Exception {
		for(ResourceGroup rg : rgroups) {
			if(!rg.isUse_extra_res())
				continue;
			for(Species sp : rg.est_spp) {
				if(Float.compare(sp.extragrowth, 0.0f) == 0)//ZRO
					continue;
				sp.update_Newsize(-sp.extragrowth);
				sp.extragrowth = 0.0f;
			}
		}
	}
}
