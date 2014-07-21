package stepwat.internal;

import java.util.Comparator;

import stepwat.LogFileIn;
import stepwat.LogFileIn.LogMode;
import stepwat.internal.Species.MortalityType;

public class Indiv {
	Globals globals;
	
	public static final int MAX_INDIVS_PER_SPP = 100;
	
	/**
	 * number of years this individual has slow growth
	 */
	protected int age, mm_extra_res, slow_yrs;
	/**
	 * This Indiv belongs to this species object
	 */
	protected Species myspecies;
	/**
	 * num yrs pr > 1 (stretched resources)
	 */
	protected int yrs_neg_pr;
	protected MortalityType killedby;
	protected boolean killed;
	/**
	 * relative to full-sized individual -- 0-1.0)
	 */
	protected float relsize;
	/**
	 * prop'l contribution this indiv makes to group relsize
	 */
	protected float grp_res_prop;
	/**
	 * min_res_req * relsize
	 */
	protected float res_required;
	/**
	 * resource * min_res_req
	 */
	protected float res_avail;
	/**
	 * resource applied to superficial growth
	 */
	protected float res_extra;
	/**
	 * ratio of resources required to amt available
	 */
	protected float pr;
	/**
	 * actual growth rate
	 */
	protected float growthrate;
	/**
	 * set when killed; 0 if not clonal
	 */
	protected float prob_veggrow;

	/**
	 * Use this constructor when a new plant is established.
	 * The individuals parameters are initialized. The Species
	 * "object" contains a list of its individuals which is
	 * updated here.  See also Indiv_Kill_Complete() for removing
	 * individuals. <br>
	 * Individuals are created at age == 1; the model assumes
	 * one year has passed for a plant to become established.
	 * The age is incremented at the end of both growth and
	 * mortality for the current year so plants killed in their
	 * first established growing season are 1 year old.  The
	 * massive year 0 mortality one expects in nature is
	 * accounted for by the probability of establishment.
	 * But be aware of the base0 problem with age-related arrays.<br>
	 * See rgroup_Establish() for establishment process.
	 * 
	 * @param species
	 * @throws Exception
	 */
	public Indiv(Species species) throws Exception {
		LogFileIn f = stepwat.LogFileIn.getInstance();
		
		if(species.Indvs.size() == MAX_INDIVS_PER_SPP) {
			f.LogError(LogMode.WARN,
					"Limit reached: " + species.getName() + " is about to get "
							+ String.valueOf(species.Indvs.size() + 1)
							+ " (max=" + String.valueOf(MAX_INDIVS_PER_SPP)
							+ ")\n");
		}
		
		this.myspecies = species;
		this.killed = false;
		this.age = 1;
		this.slow_yrs = 0;
		this.relsize = species.getRelseedlingsize();
		
		// Newer objects are closer to head
		species.Indvs.addFirst(this);
	}
	
	/**
	 * Clonal plants can be partially killed. If so, the type
	 * of mortality must be recorded as it determines when or
	 * if the plant can vegetatively propogate.  Amount of
	 * damage/killage/shrinkage is subtracted from size and
	 * RGroup and Species sizes are updated.
	 * 
	 * @param code - stored in indiv to control vegetative propagation (see rgroup_Grow() for prop., see mort_Main() for veg reduction
	 * @param killamt - relative amount of the plant to be removed from the relative size variable.
	 * @return TRUE if killamt < relsize (and relsize etc is updated)<br>
	 * FALSE otherwise which allows the caller to kill completely and handle the removal properly.
	 * @throws Exception 
	 */
	public boolean kill_Partial(MortalityType code, float killamt) throws Exception {
		boolean result = false;
		
		Species species = this.myspecies;
		if(Float.compare(relsize, killamt) > 0 && species.isIsclonal()) {
			result = true;
			this.killed = true;
			this.relsize -= killamt;
			this.killedby = code;
			this.growthrate = 0;
			this.prob_veggrow = species.getProb_veggrow()[code.ordinal()];
			species.update_Newsize(-killamt);
		}
		return result;
	}
	
	/**
	 * Remove individual and adjust relative sizes of the RGroup and Species
	 * downward by the size of the indiv. Also keep up with survivorship data.
	 * 
	 * @param ndv
	 * @throws Exception
	 */
	public void indiv_Kill_Complete() throws Exception {
		LogFileIn f = stepwat.LogFileIn.getInstance();

		if (age > myspecies.getMax_age()) {
			f.LogError(LogMode.WARN, myspecies.getName()
					+ " dies older than max_age (" + String.valueOf(age)
					+ " > " + String.valueOf(myspecies.getMax_age()) + "). Iter="
					+ String.valueOf(globals.getCurrIter()) + ", Year="
					+ String.valueOf(globals.getCurrYear()));
		}
		myspecies.update_Kills(age);
		myspecies.update_Newsize(-relsize);
		_delete();
	}
	
	/**
	 * Local routine to remove the data object of an individual.
	 * Called from indiv_Kill_Complete()
	 */
	private void _delete() {

		myspecies.Indvs.remove(this);
		int count = myspecies.Indvs.size();
		if ((--count) == 0) {
			myspecies.getRes_grp().rgroup_DropSpecies(myspecies);
		}
	}
	//Custom sort methods for indv
	static class SortSizeAsc implements Comparator<Indiv> {
		@Override
		public int compare(Indiv o1, Indiv o2) {
			int value=0;
			if(o1.relsize > o2.relsize)
				value=1;
			else if(o1.relsize < o2.relsize)
				value = -1;
			else if(Double.compare(o1.relsize, o2.relsize) == 0)
				value = 0;
			return value;
		}
	}
	static class SortSizeDesc implements Comparator<Indiv> {
		@Override
		public int compare(Indiv o1, Indiv o2) {
			int value=0;
			if(o1.relsize > o2.relsize)
				value=-1;
			else if(o1.relsize < o2.relsize)
				value = 1;
			else if(Double.compare(o1.relsize, o2.relsize) == 0)
				value = 0;
			return value;
		}
	}
}
