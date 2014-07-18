package stepwat.internal;

import stepwat.input.ST.Rgroup;

public class Succulent {
	float[] growth = new float[2], /* growth modifier eqn parms for succulents (eqn 10)*/
	mort = new float[2];			/* mortality eqn parms for succulents (eqn 16)*/
	float reduction,		/* if not killed, reduce by eqn 10*/
	prob_death;		/* calculated from eqn 16*/
	
	public void setInput(Rgroup.SucculentParams succParam) {
		this.growth[Globals.Slope] = succParam.gslope;
		this.growth[Globals.Intcpt] = succParam.gint;
		this.mort[Globals.Slope] = succParam.mslope;
		this.mort[Globals.Intcpt] = succParam.mint;
	}
}
