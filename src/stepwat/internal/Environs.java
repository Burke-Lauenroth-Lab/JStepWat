package stepwat.internal;

public class Environs {
	public enum PPTClass {
		Ppt_Wet, Ppt_Norm, Ppt_Dry;
	}

	PPTClass wet_dry;
	int ppt, /* precip for the year (mm) */
	lyppt, /* precip for the previous (last) year (mm) */
	gsppt; /* precip during growing season (mm) */
	float temp, /* average daily temp for the year (C) */
	temp_reduction[]; /* amt to reduce growth by temp */
	/* (eqns 12,13), one for each tempclass */
}
