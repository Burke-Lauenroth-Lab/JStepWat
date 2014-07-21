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
}
