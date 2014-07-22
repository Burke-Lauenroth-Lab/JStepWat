package stepwat.internal;

import java.util.Random;

public class Rand {
	Random r = new Random();
	
	public void RandSeed(int seed) {
		if(seed != 0) {
			r.setSeed(Math.abs(seed)*-1);
		}
	}
	
	public float RandUni() {
		return r.nextFloat();
	}
	
	public int RandUniRange(int first, int last) {
		int f,l,r;
		if(first == last)
			return first;
		if(first > last) {
			l=first;
			f=last;
		} else {
			f=first;
			l=last;
		}
		r=l-f+1;
		return (int)(this.r.nextFloat() * r + f);
	}
	
	/**
	 * return a random number from normal distribution with mean and stddev
	 * characteristics supplied by the user. This routine is adapted from
	 * FUNCTION GASDEV in Press, et al., 1986, Numerical Recipes, p203, Press Syndicate, NY.
	 * To reset the random number sequence, set _randseed to any negative number
	 * prior to calling any function, that depends on RandUni(). 
	 * @param mean
	 * @param stddev
	 * @return
	 */
	public double randNorm(double mean, double stddev) {
		return mean + r.nextGaussian() * stddev;
	}
}
