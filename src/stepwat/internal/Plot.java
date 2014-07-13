package stepwat.internal;

public class Plot {
	public enum DisturbEvent {
		NoDisturb, FecalPat, AntMound, Burrow, LastDisturb;
	}

	DisturbEvent disturbance;
	boolean pat_removed; 	/* fecalpats can be removed which only */
							/* kills seedlings and not other plants */
	int disturbed; 	/* years remaining before recolonization */
					/* (ie, new establishments) can begin again, */
					/* or, if disturbance is fecalpat, number of */
					/* years it has been ongoing. Set to 0 */
					/* when the disturbance effect is expired. */
}
