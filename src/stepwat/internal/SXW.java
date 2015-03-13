package stepwat.internal;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import soilwat.Defines;
import soilwat.InputData;
import soilwat.SW_CONTROL;
import soilwat.SW_SOILS.LayersInfo;
import soilwat.Times;
import stepwat.LogFileIn;
import stepwat.input.SXW.SXW_Input;

public class SXW {
	RGroups RGroup;
	Globals globals;
	Environs env;
	SW_CONTROL control;
	InputData swInput;
	private LayersInfo layerInfo;
	
	public boolean debug = false;
	/* these are initialized and maybe populated here but are used
	 * in sxw_resource.c so they aren't declared static.
	 */
	/**
	 *  relative roots X phen in each lyr,grp,pd
	 */
	double[][][] rootsXphen;
	/**
	 * "active" in terms of size and phenology
	 */
	double[][][] roots_active;
	double[][][] roots_active_rel;
	double[][][] roots_active_sum;
	
	//2D Arrays
	/**
	 * rgroup by layer, ie, group-level values
	 */
	double[][] roots_max;
	
	/**
	 * rgroups by period
	 * phenology read from file
	 */
	double[][] phen;
	
	//simple vectors hold the resource information for each group
	//curr/equ gives the available/required ratio
	/**
	 * current resource utilization
	 */
	float[] resource_cur = new float[RGroups.MAX_RGROUPS];
	/**
	 * resource convertible to PR
	 */
	float[] resource_pr = new float[RGroups.MAX_RGROUPS];
	
	/**
	 * 2D vector for the production constants
	 */
	double[] prod_litter;
	double[][] prod_bmass;
	double[][] prod_pctlive;
	/**
	 * ratio of biomass/m2 / transp/m2
	 */
	float bvt;
	
	String[] swOutDefName;
	String MyFileName;
	String[] files;
	
	int[] debugyrs;
	int debugyrs_cnt;
	
	
	/*
	 * Arrays to transp output from soilwat
	 */
	double[][] transpTotal;
	double[][] transpTrees;
	double[][] transpShrubs;
	double[][] transpForbs;
	double[][] transpGrasses;
	/*
	 * SoilWat's MAT and MAP values
	 */
	float temp;
	float ppt;
	// number of transp periods= maxdays, maxweeks, maxmonths
	int NPds;
	// transp. layers taken from SOILWAT
	int NTrLyrs;
	// plant groups taken from STEPPE
	int NGrps;
	// number of soil layers defined
	int NSolLyrs;
	
	//dynamic array(Ilp) of SWC from SOILWAT
	double[][] swc;
	//soilwat's evapotranspiration for the year
	float aet;
	
	//A old option from C
	boolean SXW_BYMAXSIZE;
	
	//debug database crap
	Connection db;
	PreparedStatement st_xphen;
	PreparedStatement st_inputvars;
	PreparedStatement st_inputprod;
	PreparedStatement st_inputsoils;
	PreparedStatement st_outputvars;
	PreparedStatement st_outputrgroup;
	PreparedStatement st_outputprod;
	PreparedStatement st_rootssum;
	PreparedStatement st_rootsrelative;
	PreparedStatement st_outputTransp;
	PreparedStatement st_outputSWCBulk;
	
	private double[] sumMaxBioByType = new double[4];
	private double[] avgMaxBioByGroup = new double[RGroups.MAX_RGROUPS];
	
	public SXW(RGroups grps, Globals globals, Environs env) {
		this.RGroup = grps;
		this.globals = globals;
		this.env = env;
		SXW_BYMAXSIZE = false;
		control = new SW_CONTROL();
	}
	
	public void setInput(SXW_Input sxwInput, Path outputDirectory, stepwat.input.ST.Rgroup grps) throws Exception {
		this.NGrps = globals.grpCount;
		this.NPds = 12;
		this.swInput = sxwInput.swInput;
		
		LogFileIn f = stepwat.LogFileIn.getInstance();
		try {
			control.onSetInput(sxwInput.swInput);
			if(!control.onVerify()) {
				f.LogError(LogFileIn.LogMode.ERROR, "SXW SoilWat : Bad input data " + control.onGetLog().toString());
			}
		} catch (Exception e) {
			f.LogError(LogFileIn.LogMode.ERROR, "SXW : Could not set SW input." + e.toString());
		}
		layerInfo = control.getLayersInfo();
		this.NTrLyrs  = layerInfo.getTrLyrs();
		this.NSolLyrs = layerInfo.n_layers;
		
		make_arrays();
		
		for(stepwat.input.SXW.Roots.GrpLayerInfo gli : sxwInput.roots.listGrps) {
			int rgi = grps.ResourceParams_Name2Index(gli.grpName);
			for(int l=0; l<gli.lyrDist.length; l++) {
				roots_max[rgi][l] = gli.lyrDist[l];
			}
		}
		
		for(stepwat.input.SXW.Phenology.GrpPhenInfo gpi : sxwInput.phen.listGrps) {
			int rgi = grps.ResourceParams_Name2Index(gpi.grpName);
			for(int m=0; m<gpi.monthlyValues.length; m++) {
				phen[rgi][m] = gpi.monthlyValues[m];
			}
		}
		
		for(int m=0; m<sxwInput.prod.litter.length; m++) {
			prod_litter[m] = sxwInput.prod.litter[m];
		}
		
		for(stepwat.input.SXW.Production.GrpProdMonthlyValues bmassValues : sxwInput.prod.bmass) {
			int rgi = grps.ResourceParams_Name2Index(bmassValues.grpName);
			for(int m=0; m<bmassValues.monthlyValues.length; m++) {
				prod_bmass[rgi][m] = bmassValues.monthlyValues[m];
			}
		}
		
		for(stepwat.input.SXW.Production.GrpProdMonthlyValues pctLive : sxwInput.prod.pctlive) {
			int rgi = grps.ResourceParams_Name2Index(pctLive.grpName);
			for(int m=0; m<pctLive.monthlyValues.length; m++) {
				prod_pctlive[rgi][m] = pctLive.monthlyValues[m];
			}
		}
		
		bvt = sxwInput.bvt.biomass/sxwInput.bvt.transpiration;
		
		sxw_input_phen();
		
		//Some pre computed values for transp splitting to groups
		for(int g=0; g<NGrps; g++) {
			int t = RGroup.rgroups.get(g).getVeg_prod_type()-1;
			for(Species s : RGroup.rgroups.get(g).getSpecies()) {
				if(s.isUse_me())
					avgMaxBioByGroup[g] += s.getMature_biomass();
			}
			avgMaxBioByGroup[g] /= RGroup.rgroups.get(g).getMax_spp();
			sumMaxBioByType[t] += avgMaxBioByGroup[g];
		}
		
		if(sxwInput.files.debug) {
			this.debug = true;
			debugConnect(Paths.get(outputDirectory.toString(), sxwInput.deBug.debugFileName+".sqlite3").toFile());
		}
	}
	
	public void reset() throws Exception {
		//this.control.onClear();
		this.control = new SW_CONTROL();
		this.control.onSetInput(swInput);
		this.control.onVerify();
	}
	
	protected void run_soilwat() throws Exception {
		float[] sizes = new float[RGroups.MAX_RGROUPS];
		for(int g=0; g<NGrps; g++) {
			sizes[g] = RGroup.rgroups.get(g).relsize;
		}
		sw_setup(sizes);
		aet=0;
		sw_run();
		transpForbs = control.getMonthlyTranspirationForb(globals.currYear - 1);
		transpGrasses = control.getMonthlyTranspirationGrass(globals.currYear - 1);
		transpShrubs = control.getMonthlyTranspirationShrub(globals.currYear - 1);
		transpTrees = control.getMonthlyTranspirationTree(globals.currYear - 1);
		transpTotal = control.getMonthlyTranspirationTotal(globals.currYear - 1);
		swc = control.getMonthlySWCBulk(globals.currYear - 1);
		aet = (float) control.getYearAET(globals.currYear - 1);
		//now compute resource availability for the given plant sizes
		updateResource();
		//and set environmental variables
		set_environs();
		//get Transp values
		
	}
	
	protected float getPR(int grpIndex) {
		float pr = Defines.isZero(resource_pr[grpIndex]) ? 0 : 1/resource_pr[grpIndex];
		return pr;
	}
	
	protected float getTranspiration(int grpIndex) {
		return resource_cur[grpIndex];
	}
	
	/**
	 * run this after SOILWAT has completed.
	 * need to convert ppt from cm to mm
	 */
	private void set_environs() {
		env.ppt = (int) (control.getYearTotalPPT(globals.currYear - 1)*10 + 0.5);
		env.temp = (float) control.getYearAvgTemp(globals.currYear - 1);
	}
	
	private void sw_setup(float[] sizes) {
		update_transp_coeff(sizes);
		update_productivity();
		
		control.resetProdDaily();
	}
	
	private void sw_run() throws Exception {
		control.setYear(control.getStartYear() + globals.currYear - 1);
		control.SW_CTL_run_current_year();
	}
	
	/***
	 * copy the relative root distribution to soilwat's layers
	 * @param relsize
	 */
	private void update_transp_coeff(float[] relsize) {
		float sum1=0,sum2=0,sum3=0,sum4=0;
		//Tree
		for(int l=0; l<getNTranspLayers(1); l++) {
			double value = 0;
			for(int g=0; g<NGrps; g++) {
				if(RGroup.rgroups.get(g).getVeg_prod_type() == 1) {
					if(getNTranspLayers(1) > 0) {
						value += roots_max[g][l] * relsize[g];
					}
				}
			}
			control.setTranspCoeffTree(l, value);
			sum1+=value;
		}
		//Shrub
		for(int l=0; l<getNTranspLayers(2); l++) {
			double value = 0;
			for(int g=0; g<NGrps; g++) {
				if(RGroup.rgroups.get(g).getVeg_prod_type() == 2) {
					if(getNTranspLayers(2) > 0) {
						value += roots_max[g][l] * relsize[g];
					}
				}
			}
			control.setTranspCoeffShrub(l, value);
			sum2+=value;
		}
		//Grass
		for(int l=0; l<getNTranspLayers(3); l++) {
			double value = 0;
			for(int g=0; g<NGrps; g++) {
				if(RGroup.rgroups.get(g).getVeg_prod_type() == 3) {
					if(getNTranspLayers(3) > 0) {
						value += roots_max[g][l] * relsize[g];
					}
				}
			}
			control.setTranspCoeffGrass(l, value);
			sum3+=value;
		}
		//Forb
		for(int l=0; l<getNTranspLayers(4); l++) {
			double value = 0;
			for(int g=0; g<NGrps; g++) {
				if(RGroup.rgroups.get(g).getVeg_prod_type() == 4) {
					if(getNTranspLayers(4) > 0) {
						value += roots_max[g][l] * relsize[g];
					}
				}
			}
			control.setTranspCoeffForb(l, value);
			sum4+=value;
		}
		//normalize coefficients to 1.0 If sum is 0, then the transp_coeff is also 0.
		for(int l=0; l<getNTranspLayers(1); l++)
			if(!Defines.isZero(sum1)) control.setTranspCoeffTree(l, control.getTranspCoeffTree(l)/sum1);
		for(int l=0; l<getNTranspLayers(2); l++)
			if(!Defines.isZero(sum2)) control.setTranspCoeffShrub(l, control.getTranspCoeffShrub(l)/sum2);
		for(int l=0; l<getNTranspLayers(3); l++)
			if(!Defines.isZero(sum3)) control.setTranspCoeffGrass(l, control.getTranspCoeffGrass(l)/sum3);
		for(int l=0; l<getNTranspLayers(4); l++)
			if(!Defines.isZero(sum4)) control.setTranspCoeffForb(l, control.getTranspCoeffForb(l)/sum4);
	}
	
	private void update_productivity() {
		float totbmass = 0;
		float[] bmassg = new float[RGroups.MAX_RGROUPS];
		float[] vegTypeBiomass = new float[4];
		float[] rgroupFractionOfVegTypeBiomass = new float[RGroups.MAX_RGROUPS];
		
		for(int g=0; g<NGrps; g++) {
			bmassg[g] = RGroup.rgroups.get(g).getBiomass() / globals.plotsize;
			totbmass += bmassg[g];
			vegTypeBiomass[RGroup.rgroups.get(g).getVeg_prod_type()-1] += bmassg[g];
		}
		
		for(int g=0; g<NGrps; g++) {
			if(Defines.GT(vegTypeBiomass[RGroup.rgroups.get(g).getVeg_prod_type()-1], 0))
				rgroupFractionOfVegTypeBiomass[g] = bmassg[g]/vegTypeBiomass[RGroup.rgroups.get(g).getVeg_prod_type()-1];
			else
				rgroupFractionOfVegTypeBiomass[g] = 0;
		}
		
		for(int m=0; m<12; m++) {
			double treeBM=0,shrubBM=0,grassBM=0,forbBM=0;
			double treePL=0,shrubPL=0,grassPL=0,forbPL=0;
			
			if(Defines.GT(totbmass, 0)) {
				for(int g=0; g<NGrps; g++) {
					int t = RGroup.rgroups.get(g).getVeg_prod_type();
					switch(t) {//Tree
					case 1:
						treePL += prod_pctlive[g][m] * rgroupFractionOfVegTypeBiomass[g];
						treeBM += prod_bmass[g][m] * bmassg[g];
						break;
					case 2://shrub
						shrubPL += prod_pctlive[g][m] * rgroupFractionOfVegTypeBiomass[g];
						shrubBM += prod_bmass[g][m] * bmassg[g];
						break;
					case 3://grass
						grassPL += prod_pctlive[g][m] * rgroupFractionOfVegTypeBiomass[g];
						grassBM += prod_bmass[g][m] * bmassg[g];
						break;
					case 4:
						forbPL += prod_pctlive[g][m] * rgroupFractionOfVegTypeBiomass[g];
						forbBM += prod_bmass[g][m] * bmassg[g];
						break;
					}
				}
			}
			
			control.setMonthlyLitter(m+1, vegTypeBiomass[2]*prod_litter[m], vegTypeBiomass[1]*prod_litter[m], vegTypeBiomass[0]*prod_litter[m], vegTypeBiomass[3]*prod_litter[m]);
			control.setMonthlyBiomass(m+1, grassBM, shrubBM, treeBM, forbBM);
			control.setMonthlyPCTLive(m+1, grassPL, shrubPL, treePL, forbPL);
		}
		
		if(Defines.GT(totbmass, 0)) {
			control.setVegComposition(vegTypeBiomass[2] / totbmass, vegTypeBiomass[1] / totbmass, vegTypeBiomass[0] / totbmass, vegTypeBiomass[3] / totbmass, 0);
		} else {
			control.setVegComposition(0, 0, 0, 0, 1);
		}
	}
	
	protected int getNTranspLayers(int veg_prod_type) {
		if(veg_prod_type == 1)
			return layerInfo.n_transp_lyrs_tree;
		else if(veg_prod_type == 2)
			return layerInfo.n_transp_lyrs_shrub;
		else if(veg_prod_type == 3)
			return layerInfo.n_transp_lyrs_grass;
		else if(veg_prod_type == 4)
			return layerInfo.n_transp_lyrs_forb;
		return -1;
	}
	
	protected void sxw_input_phen() {
		for(int g=0; g<NGrps; g++) {
			int nLyrs = getNTranspLayers(RGroup.rgroups.get(g).getVeg_prod_type());
			for(int p=0; p<NPds; p++) {
				for(int l=0; l<nLyrs; l++) {
					rootsXphen[g][p][l] = roots_max[g][l] * phen[g][p];
				}
			}
		}
	}
	
	public void initPlot() {
		clearTransp();
		updateResource();
	}
	
	private void clearTransp() {
		if(transpTotal != null) {
			for(int i=0; i<12; i++) {
				for(int j=0; j<NSolLyrs; j++) {
					transpTotal[i][j] = transpForbs[i][j] = transpGrasses[i][j] = transpShrubs[i][j] = transpTrees[i][j] = 0.0;
				}
			}
		}
	}
	
	private void updateResource() {
		float[] sizes = new float[NGrps];
		for(int g=0; g<NGrps; g++) {
			sizes[g] = 0;
			if(RGroup.rgroups.get(g).getMax_age() == 1) {
				for(Species s : RGroup.rgroups.get(g).getSpecies()) {
					sizes[g] += s.getMature_biomass() * .75;
				}
			} else {
				sizes[g] = RGroup.rgroups.get(g).getBiomass();
			}
		}
		
		update_root_tables(sizes);
		if(transpTotal != null)
			transp_contribution_by_group(resource_cur);
		
		for(int g=0; g<NGrps; g++) {
			resource_cur[g] = resource_cur[g] * bvt;
		}
	}
	
	/***
	 * sizes is a simple array that contains the groups'
	 * actual biomass in grams in group order.
	 * @param sizes
	 */
	private void update_root_tables(float[] sizes) {
		for(int t=0; t<4; t++)
			for(int p=0; p<NPds; p++)
				for(int l=0; l<NTrLyrs; l++)
					roots_active_sum[t][p][l] = 0;
		for(int g=0; g<NGrps; g++) {
			int t = RGroup.rgroups.get(g).getVeg_prod_type()-1;
			int nLyrs = getNTranspLayers(t+1);
			for(int p=0; p<NPds; p++) {
				for(int l=0; l<nLyrs; l++) {
					double x = rootsXphen[g][p][l] * sizes[g];
					roots_active[g][p][l] = x;
					roots_active_sum[t][p][l] += x;
				}
			}
		}
		
		for(int g=0; g<NGrps; g++) {
			int t = RGroup.rgroups.get(g).getVeg_prod_type()-1;
			int nLyrs = getNTranspLayers(t+1);
			for(int p=0; p<NPds; p++) {
				for(int l=0; l<nLyrs; l++) {
					roots_active_rel[g][p][l] = Defines.isZero(roots_active_sum[t][p][l])?0.:roots_active[g][p][l]/roots_active_sum[t][p][l];
				}
			}
		}
	}
	
	/***
	 * use_by_group is the vector to be used in the resource
	 *        availability calculation, ie, the output.
	 * must call _update_root_tables() before this.
	 * compute each group's contribution to the
	 * transpiration values retrieved from SOILWAT based
	 * on its relative size, its root distribution, and
	 * its phenology (activity).
	 * @param use_by_group
	 */
	private void transp_contribution_by_group(float[] use_by_group) {
		float sumTranspTotal = 0, sumUsedByGroup =0;
		for(int g=0; g<NGrps; g++) {
			use_by_group[g] = 0;
			int t = RGroup.rgroups.get(g).getVeg_prod_type()-1;
			int nLyrs = getNTranspLayers(t+1);
			double fracGroupsMaxBioFromType = avgMaxBioByGroup[g]/sumMaxBioByType[t];
			double[][] transp;
			switch (t) {
			case 0:
				transp = transpTrees;
				break;
			case 1:
				transp = transpShrubs;
				break;
			case 2:
				transp = transpGrasses;
				break;
			case 3:
				transp = transpForbs;
				break;
			default:
				transp = transpTotal;
				break;
			}
			for(int p=0; p<NPds; p++) {
				for(int l=0;l<nLyrs;l++) {
					use_by_group[g] += (float) roots_active_rel[g][p][l] * fracGroupsMaxBioFromType * transp[p][l];
				}
			}
			sumUsedByGroup += use_by_group[g];
		}
		
		for(int p=0; p<NPds; p++) {
			for(int t=0; t<NSolLyrs; t++) {
				sumTranspTotal += transpTotal[p][t];
			}
		}
		if(!Defines.isZero(sumTranspTotal)) {
			for(int g=0; g<NGrps; g++) {
				if(Defines.isZero(sumUsedByGroup)) {
					use_by_group[g] = 1/NGrps;
					sumUsedByGroup = 1;
				}
					
				use_by_group[g] = (use_by_group[g] / sumUsedByGroup) * sumTranspTotal;
				if(Float.isNaN(use_by_group[g]))
					System.out.print("prblem");
			}
		}
	}
	
	private void make_arrays() {
		roots_max = new double[NGrps][NTrLyrs];
		rootsXphen = new double[NGrps][NPds][NTrLyrs];
		roots_active = new double[NGrps][NPds][NTrLyrs];
		roots_active_rel = new double[NGrps][NPds][NTrLyrs];
		//4 - grass forb tree shrub
		roots_active_sum = new double[4][NPds][NTrLyrs];
		
		phen = new double[NGrps][12];
		
		prod_litter = new double[12];
		prod_bmass = new double[NGrps][12];
		prod_pctlive = new double[NGrps][12];
		
		//We don't need any space for transp because we can just get a pointer to the data
		//Same with swc
	}
	
	public void writeDebug(boolean cleanup) {
		if(cleanup) {
			dbClose();
		} else {
			insertInputVars();
			insertInputProd();
			insertInputSoils();
			insertOutputVars();
			insertOutputRGroupInfo();
			insertOutputProd();
			insertRootsSum();
			insertRootsRelative();
			insertTranspiration();
			insertSWCBulk();
		}
	}
	
	private void debugConnect(File dbfile) {
		if(dbfile.exists())
			dbfile.delete();
		try {
			Class.forName("org.sqlite.JDBC");
		} catch (ClassNotFoundException e1) {
			e1.printStackTrace();
		}
		
		try {
			db = DriverManager.getConnection("jdbc:sqlite:"+dbfile.toString());
			Statement statement = db.createStatement();
			statement.setQueryTimeout(30);
			
			statement.executeUpdate("PRAGMA synchronous = OFF");
			statement.executeUpdate("PRAGMA journal_mode = MEMORY");
			
			statement.executeUpdate("CREATE TABLE info(StartYear INT, Years INT, Iterations INT, RGroups INT, TranspirationLayers INT, SoilLayers INT, PlotSize REAL, BVT REAL);");
			statement.executeUpdate("CREATE TABLE RGroups(ID INT PRIMARY KEY NOT NULL, NAME TEXT NOT NULL, VegProdType INT NOT NULL);");
			statement.executeUpdate("CREATE TABLE sxwphen(RGroupID INT NOT NULL, Month INT NOT NULL, GrowthPCT REAL NOT NULL, PRIMARY KEY(RGroupID, Month));");
			statement.executeUpdate("CREATE TABLE sxwprod(RGroupID INT NOT NULL, Month INT NOT NULL, BMASS REAL, LITTER REAL, PCTLIVE REAL, PRIMARY KEY(RGroupID, Month));");
			statement.executeUpdate("CREATE TABLE sxwRootsXphen(RGroupID INT NOT NULL, Layer INT NOT NULL, January REAL, February REAL, March REAL, April REAL, May REAL, June REAL, July REAL, August REAL, September REAL, October REAL, November REAL, December REAL, PRIMARY KEY(RGroupID, Layer));");
			statement.executeUpdate("CREATE TABLE sxwRootsSum(YEAR INT NOT NULL, Iteration INT NOT NULL, Layer INT NOT NULL, VegProdType INT NOT NULL, January REAL, February REAL, March REAL, April REAL, May REAL, June REAL, July REAL, August REAL, September REAL, October REAL, November REAL, December REAL, PRIMARY KEY(Year, Iteration, Layer, VegProdType));");
			statement.executeUpdate("CREATE TABLE sxwRootsRelative(YEAR INT NOT NULL, Iteration INT NOT NULL, Layer INT NOT NULL, RGroupID INT NOT NULL, January REAL, February REAL, March REAL, April REAL, May REAL, June REAL, July REAL, August REAL, September REAL, October REAL, November REAL, December REAL, PRIMARY KEY(Year, Iteration, Layer, RGroupID));");
			statement.executeUpdate("CREATE TABLE sxwInputVars(Year INT NOT NULL, Iteration INT NOT NULL, FracGrass REAL, FracShrub REAL, FracTree REAL, FracForb REAL, FracBareGround REAL, PRIMARY KEY(Year, Iteration));");
			statement.executeUpdate("CREATE TABLE sxwInputProd(Year INT NOT NULL, Iteration INT NOT NULL, VegProdType INT NOT NULL, Month INT NOT NULL, Litter REAL, Biomass REAL, PLive REAL, LAI_conv REAL, PRIMARY KEY(Year, Iteration, VegProdType, Month));");
			statement.executeUpdate("CREATE TABLE sxwInputSoils(Year INT NOT NULL, Iteration INT NOT NULL, Layer INT NOT NULL, Tree_trco REAL, Shrub_trco REAL, Grass_trco REAL, Forb_trco REAL, PRIMARY KEY(Year, Iteration, Layer));");
			statement.executeUpdate("CREATE TABLE sxwOutputVars(Year INT NOT NULL, Iteration INT NOT NULL, MAP_mm INT, MAT_C REAL, AET_cm REAL, AT_cm REAL, TotalRelsize REAL, TotalPR REAL, TotalTransp REAL, PRIMARY KEY(Year, Iteration));");
			statement.executeUpdate("CREATE TABLE sxwOutputRgroup(YEAR INT NOT NULL, Iteration INT NOT NULL, RGroupID INT NOT NULL, Biomass REAL, Realsize REAL, PR REAL, Transpiration REAL, PRIMARY KEY(Year, Iteration, RGroupID));");
			statement.executeUpdate("CREATE TABLE sxwOutputProd(YEAR INT NOT NULL, Iteration INT NOT NULL, Month INT NOT NULL, BMass REAL, PctLive REAL, LAIlive REAL, VegCov REAL, TotAGB REAL, PRIMARY KEY(Year, Iteration, Month));");
			statement.executeUpdate("CREATE TABLE sxwOutputTranspiration(YEAR INT NOT NULL, Iteration INT NOT NULL, Layer INT NOT NULL, VegProdType INT NOT NULL, January REAL, February REAL, March REAL, April REAL, May REAL, June REAL, July REAL, August REAL, September REAL, October REAL, November REAL, December REAL, PRIMARY KEY(Year, Iteration, Layer, VegProdType));");
			statement.executeUpdate("CREATE TABLE sxwOutputSWCBulk(YEAR INT NOT NULL, Iteration INT NOT NULL, Layer INT NOT NULL, January REAL, February REAL, March REAL, April REAL, May REAL, June REAL, July REAL, August REAL, September REAL, October REAL, November REAL, December REAL, PRIMARY KEY(Year, Iteration, Layer));");
			
			PreparedStatement st_rgroups = db.prepareStatement("INSERT INTO RGroups (ID, NAME, VegProdType) VALUES (?, ?, ?);");
			
			for(int g=0; g<NGrps; g++) {
				st_rgroups.setInt(1, g+1);
				st_rgroups.setString(2, RGroup.rgroups.get(g).getName());
				st_rgroups.setInt(3, RGroup.rgroups.get(g).getVeg_prod_type());
				st_rgroups.addBatch();
			}
			db.setAutoCommit(false);
			st_rgroups.executeBatch();
			db.setAutoCommit(true);

			PreparedStatement st_phen = db.prepareStatement("INSERT INTO sxwphen (RGroupID, Month, GrowthPCT) VALUES (?, ?, ?);");
			for(int g=0; g<NGrps; g++) {
				for(int m=0; m<12; m++) {
					st_phen.setInt(1, g+1);
					st_phen.setInt(2, m+1);
					st_phen.setDouble(3, phen[g][m]);
					st_phen.addBatch();
				}
			}
			db.setAutoCommit(false);
			st_phen.executeBatch();
			db.setAutoCommit(true);
			
			PreparedStatement st_prod = db.prepareStatement("INSERT INTO sxwprod (RGroupID, Month, BMASS, LITTER, PCTLIVE) VALUES (?, ?, ?, ?, ?);");
			for(int g=0; g<NGrps; g++) {
				for(int m=0; m<12; m++) {
					st_prod.setInt(1, g+1);
					st_prod.setInt(2, m+1);
					st_prod.setDouble(3, prod_bmass[g][m]);
					st_prod.setDouble(4, prod_litter[m]);
					st_prod.setDouble(5, prod_pctlive[g][m]);
					st_prod.addBatch();
				}
			}
			db.setAutoCommit(false);
			st_prod.executeBatch();
			db.setAutoCommit(true);
			
			PreparedStatement st_info = db.prepareStatement("INSERT INTO info (StartYear, Years, Iterations, RGroups, TranspirationLayers, SoilLayers, PlotSize, BVT) VALUES (?, ?, ?, ?, ?, ?, ?, ?);");
			st_info.setInt(1, control.getStartYear());
			st_info.setInt(2, globals.runModelYears);
			st_info.setInt(3, globals.runModelIterations);
			st_info.setInt(4, globals.grpCount);
			st_info.setInt(5, NTrLyrs);
			st_info.setInt(6, NSolLyrs);
			st_info.setFloat(7, globals.plotsize);
			st_info.setFloat(8, bvt);
			st_info.addBatch();
			db.setAutoCommit(false);
			st_info.executeBatch();
			db.setAutoCommit(true);
			
			st_xphen = db.prepareStatement("INSERT INTO sxwRootsXphen (RGroupID,Layer,January,February,March,April,May,June,July,August,September,October,November,December) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);");
			st_inputvars = db.prepareStatement("INSERT INTO sxwInputVars (Year,Iteration,FracGrass,FracShrub,FracTree,FracForb,FracBareGround) VALUES (@Year,@Iteration,@FracGrass,@FracShrub,@FracTree,@FracForb,@FracBareGround);");
			st_inputprod = db.prepareStatement("INSERT INTO sxwInputProd (Year,Iteration,VegProdType,Month,Litter,Biomass,PLive,LAI_conv) VALUES (@Year,@Iteration,@VegProdType,@Month,@Litter,@Biomass,@PLive,@LAI_conv);");
			st_inputsoils = db.prepareStatement("INSERT INTO sxwInputSoils (Year,Iteration,Layer,Tree_trco,Shrub_trco,Grass_trco,Forb_trco) VALUES (@Year,@Iteration,@Layer,@Tree_trco,@Shrub_trco,@Grass_trco,@Forb_trco);");
			st_outputvars = db.prepareStatement("INSERT INTO sxwOutputVars (Year,Iteration,MAP_mm,MAT_C,AET_cm,AT_cm,TotalRelsize,TotalPR,TotalTransp) VALUES (@Year,@Iteration,@MAP_mm,@MAT_C,@AET_cm,@AT_cm,@TotalRelsize,@TotalPR,@TotalTransp);");
			st_outputrgroup = db.prepareStatement("INSERT INTO sxwOutputRgroup (Year,Iteration,RGroupID,Biomass,Realsize,PR,Transpiration) VALUES (@Year,@Iteration,@RGroupID,@Biomass,@Realsize,@PR,@Transpiration);");
			st_outputprod = db.prepareStatement("INSERT INTO sxwOutputProd (Year,Iteration,Month,BMass,PctLive,LAIlive,VegCov,TotAGB) VALUES (@Year,@Iteration,@Month,@BMass,@PctLive,@LAIlive,@VegCov,@TotAGB);");
			st_rootssum = db.prepareStatement("INSERT INTO sxwRootsSum (Year,Iteration,Layer,VegProdType,January,February,March,April,May,June,July,August,September,October,November,December) VALUES (@Year,@Iteration,@Layer,@VegProdType,@January,@February,@March,@April,@May,@June,@July,@August,@September,@October,@November,@December);");
			st_rootsrelative = db.prepareStatement("INSERT INTO sxwRootsRelative (Year,Iteration,Layer,RGroupID,January,February,March,April,May,June,July,August,September,October,November,December) VALUES (@Year,@Iteration,@Layer,@RGroupID,@January,@February,@March,@April,@May,@June,@July,@August,@September,@October,@November,@December);");
			st_outputTransp = db.prepareStatement("INSERT INTO sxwOutputTranspiration (Year,Iteration,Layer,VegProdType,January,February,March,April,May,June,July,August,September,October,November,December) VALUES (@Year,@Iteration,@Layer,@VegProdType,@January,@February,@March,@April,@May,@June,@July,@August,@September,@October,@November,@December);");
			st_outputSWCBulk = db.prepareStatement("INSERT INTO sxwOutputSWCBulk (Year,Iteration,Layer,January,February,March,April,May,June,July,August,September,October,November,December) VALUES (@Year,@Iteration,@Layer,@January,@February,@March,@April,@May,@June,@July,@August,@September,@October,@November,@December);");
			
			insertRootsXphen();
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	private void dbClose() {
		try {
			st_xphen.close();
			st_inputvars.close();
			st_inputprod.close();
			st_inputsoils.close();
			st_outputvars.close();
			st_outputrgroup.close();
			st_outputprod.close();
			st_rootssum.close();
			st_rootsrelative.close();
			st_outputTransp.close();
			st_outputSWCBulk.close();
			db.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	private void insertRootsXphen() {
		try {
			db.setAutoCommit(false);
			for (int g = 0; g < NGrps; g++) {
				int nLyrs = getNTranspLayers(RGroup.rgroups.get(g).getVeg_prod_type());
				for (int l = 0; l < nLyrs; l++) {
					st_xphen.setInt(1, g + 1);
					st_xphen.setInt(2, l + 1);
					st_xphen.setDouble(3, rootsXphen[g][0][l]);
					st_xphen.setDouble(4, rootsXphen[g][1][l]);
					st_xphen.setDouble(5, rootsXphen[g][2][l]);
					st_xphen.setDouble(6, rootsXphen[g][3][l]);
					st_xphen.setDouble(7, rootsXphen[g][4][l]);
					st_xphen.setDouble(8, rootsXphen[g][5][l]);
					st_xphen.setDouble(9, rootsXphen[g][6][l]);
					st_xphen.setDouble(10, rootsXphen[g][7][l]);
					st_xphen.setDouble(11, rootsXphen[g][8][l]);
					st_xphen.setDouble(12, rootsXphen[g][9][l]);
					st_xphen.setDouble(13, rootsXphen[g][10][l]);
					st_xphen.setDouble(14, rootsXphen[g][11][l]);
					st_xphen.addBatch();
				}
			}
			st_xphen.executeBatch();
			db.setAutoCommit(true);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	private void insertInputVars() {
		try {
			db.setAutoCommit(false);
			st_inputvars.setInt(1, control.getYear());
			st_inputvars.setInt(2, globals.currIter);
			st_inputvars.setDouble(3, control.getGrassFrac());
			st_inputvars.setDouble(4, control.getShrubFrac());
			st_inputvars.setDouble(5, control.getTreeFrac());
			st_inputvars.setDouble(6, control.getForbFrac());
			st_inputvars.setDouble(7, control.getBareGroundFrac());
			st_inputvars.addBatch();
			st_inputvars.executeBatch();
			db.setAutoCommit(true);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	private void insertInputProd() {
		try {
			db.setAutoCommit(false);
			for (int t = 1; t < 5; t++) {
				double[][] values = null;
				switch(t) {
				case 1:
					values = control.getMonthlyProductionTree();
					break;
				case 2:
					values = control.getMonthlyProductionShrub();
					break;
				case 3:
					values = control.getMonthlyProductionGrass();
					break;
				case 4:
					values = control.getMonthlyProductionForb();
					break;
				}
				 
				for (int p = 0; p < 12; p++) {
					st_inputprod.setInt(1, control.getYear());
					st_inputprod.setInt(2, globals.currIter);
					st_inputprod.setInt(3, t);
					st_inputprod.setInt(4, p+1);
					st_inputprod.setDouble(5, values[p][0]);
					st_inputprod.setDouble(6, values[p][1]);
					st_inputprod.setDouble(7, values[p][2]);
					st_inputprod.setDouble(8, values[p][3]);
					st_inputprod.addBatch();
				}
			}
			st_inputprod.executeBatch();
			db.setAutoCommit(true);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	private void insertInputSoils() {
		try {
			db.setAutoCommit(false);
			for (int l = 0; l < NSolLyrs; l++) {
				st_inputsoils.setInt(1, control.getYear());
				st_inputsoils.setInt(2, globals.currIter);
				st_inputsoils.setInt(3, l + 1);
				st_inputsoils.setDouble(4, control.getTranspCoeffTree(l));
				st_inputsoils.setDouble(5, control.getTranspCoeffShrub(l));
				st_inputsoils.setDouble(6, control.getTranspCoeffGrass(l));
				st_inputsoils.setDouble(7, control.getTranspCoeffForb(l));
				st_inputsoils.addBatch();
			}
			st_inputsoils.executeBatch();
			db.setAutoCommit(true);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	private void insertOutputVars() {
		double sum = 0;
		double sum1 = 0;
		double sum2 = 0;
		double sum3 = 0;
		
		for (int p = 0; p < 12; p++) {
			for (int t = 0; t < NSolLyrs; t++)
				sum += transpTotal[p][t];
		}
		
		for (int g = 0; g < NGrps; g++) {
			sum1 += RGroup.rgroups.get(g).relsize;
			sum2 += RGroup.rgroups.get(g).pr;
			sum3 += resource_cur[g];
		}
		try {
			db.setAutoCommit(false);
			st_outputvars.setInt(1, control.getYear());
			st_outputvars.setInt(2, globals.currIter);
			st_outputvars.setInt(3, env.ppt);
			st_outputvars.setFloat(4, env.temp);
			st_outputvars.setFloat(5, aet);
			st_outputvars.setDouble(6, sum);
			st_outputvars.setDouble(7, sum1);
			st_outputvars.setDouble(8, sum2);
			st_outputvars.setDouble(9, sum3);
			st_outputvars.addBatch();
			st_outputvars.executeBatch();
			db.setAutoCommit(true);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	private void insertOutputRGroupInfo() {
		try {
			db.setAutoCommit(false);
			for (int g = 0; g < NGrps; g++) {
				st_outputrgroup.setInt(1, control.getYear());
				st_outputrgroup.setInt(2, globals.currIter);
				st_outputrgroup.setInt(3, RGroup.rgroups.get(g).getGrp_num()+1);
				st_outputrgroup.setFloat(4, RGroup.rgroups.get(g).getBiomass());
				st_outputrgroup.setFloat(5, RGroup.rgroups.get(g).relsize);
				st_outputrgroup.setFloat(6, RGroup.rgroups.get(g).pr);
				st_outputrgroup.setFloat(7, resource_cur[g]);
				st_outputrgroup.addBatch();
			}
			st_outputrgroup.executeBatch();
			db.setAutoCommit(true);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	private void insertOutputProd() {
		try {
			db.setAutoCommit(false);
			int doy = 1;
			for (int p = 0; p < 12; p++) {
				int days = 31;
				double pct_live = 0, lai_live = 0, vegcov = 0, total_agb = 0, biomass = 0;
				if (p == 3 || p == 5 || p == 8 || p == 10) //all these months have 30 days
					days = 30;
				else if (p == 1) { //February has either 28 or 29 days
					days = 28;
					if (Times.isleapyear(control.getYear()))
						days = 29;
				} // all the other months have 31 days
				
				lai_live = control.getProdDailyLai_live(doy, doy + days);
				vegcov = control.getProdDailyVegCov(doy, doy + days);
				total_agb = control.getProdDailyTotalAgb(doy, doy + days);
				pct_live = control.getProdDailyPCTLive(doy, doy + days);
				biomass = control.getProdDailyBiomass(doy, doy + days);
				
				doy+=days;
				pct_live /= days;
				biomass /= days;
				lai_live /= days; //getting the monthly averages...
				vegcov /= days;
				total_agb /= days;
				
				st_outputprod.setInt(1, control.getYear());
				st_outputprod.setInt(2, globals.currIter);
				st_outputprod.setInt(3, p+1);
				st_outputprod.setDouble(4, biomass);
				st_outputprod.setDouble(5, pct_live);
				st_outputprod.setDouble(6, lai_live);
				st_outputprod.setDouble(7, vegcov);
				st_outputprod.setDouble(8, total_agb);
				st_outputprod.addBatch();
			}
			st_outputprod.executeBatch();
			db.setAutoCommit(true);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	private void insertRootsSum() {
		try {
			db.setAutoCommit(false);
			for(int t=1; t<=4; t++) {
				for(int l=0; l<NTrLyrs; l++) {
					st_rootssum.setInt(1, control.getYear());
					st_rootssum.setInt(2, globals.currIter);
					st_rootssum.setInt(3, l+1);
					st_rootssum.setInt(4, t);
					st_rootssum.setDouble(5, roots_active_sum[t-1][0][l]);
					st_rootssum.setDouble(6, roots_active_sum[t-1][1][l]);
					st_rootssum.setDouble(7, roots_active_sum[t-1][2][l]);
					st_rootssum.setDouble(8, roots_active_sum[t-1][3][l]);
					st_rootssum.setDouble(9, roots_active_sum[t-1][4][l]);
					st_rootssum.setDouble(10, roots_active_sum[t-1][5][l]);
					st_rootssum.setDouble(11, roots_active_sum[t-1][6][l]);
					st_rootssum.setDouble(12, roots_active_sum[t-1][7][l]);
					st_rootssum.setDouble(13, roots_active_sum[t-1][8][l]);
					st_rootssum.setDouble(14, roots_active_sum[t-1][9][l]);
					st_rootssum.setDouble(15, roots_active_sum[t-1][10][l]);
					st_rootssum.setDouble(16, roots_active_sum[t-1][11][l]);
					st_rootssum.addBatch();
				}
			}
			st_rootssum.executeBatch();
			db.setAutoCommit(true);
		} catch (SQLException e) {
			e.printStackTrace();
		}	
	}
	
	private void insertRootsRelative() {
		try {
			db.setAutoCommit(false);
			for(int g=0; g<NGrps; g++) {
				int nLyrs = getNTranspLayers(RGroup.rgroups.get(g).getVeg_prod_type());
				for(int l=0; l<nLyrs; l++) {
					st_rootsrelative.setInt(1, control.getYear());
					st_rootsrelative.setInt(2, globals.currIter);
					st_rootsrelative.setInt(3, l+1);
					st_rootsrelative.setInt(4, g+1);
					st_rootsrelative.setDouble(5, roots_active_rel[g][0][l]);
					st_rootsrelative.setDouble(6, roots_active_rel[g][1][l]);
					st_rootsrelative.setDouble(7, roots_active_rel[g][2][l]);
					st_rootsrelative.setDouble(8, roots_active_rel[g][3][l]);
					st_rootsrelative.setDouble(9, roots_active_rel[g][4][l]);
					st_rootsrelative.setDouble(10, roots_active_rel[g][5][l]);
					st_rootsrelative.setDouble(11, roots_active_rel[g][6][l]);
					st_rootsrelative.setDouble(12, roots_active_rel[g][7][l]);
					st_rootsrelative.setDouble(13, roots_active_rel[g][8][l]);
					st_rootsrelative.setDouble(14, roots_active_rel[g][9][l]);
					st_rootsrelative.setDouble(15, roots_active_rel[g][10][l]);
					st_rootsrelative.setDouble(16, roots_active_rel[g][11][l]);
					st_rootsrelative.addBatch();
				}
			}
			st_rootsrelative.executeBatch();
			db.setAutoCommit(true);
		} catch (SQLException e) {
			e.printStackTrace();
		}	
	}
	
	private void insertTranspiration() {
		try {
			double[][] transp = null;
			db.setAutoCommit(false);
			for(int t=0; t<=4; t++) {
				switch(t) {
				case 0:
					transp = transpTotal;
					break;
				case 1:
					transp = transpTrees;
					break;
				case 2:
					transp = transpShrubs;
					break;
				case 3:
					transp = transpGrasses;
					break;
				case 4:
					transp = transpForbs;
				default:
					break;
				}
				for(int l=0; l<NTrLyrs; l++) {
					st_outputTransp.setInt(1, control.getYear());
					st_outputTransp.setInt(2, globals.currIter);
					st_outputTransp.setInt(3, l+1);
					st_outputTransp.setInt(4, t);
					st_outputTransp.setDouble(5, transp[0][l]);
					st_outputTransp.setDouble(6, transp[1][l]);
					st_outputTransp.setDouble(7, transp[2][l]);
					st_outputTransp.setDouble(8, transp[3][l]);
					st_outputTransp.setDouble(9, transp[4][l]);
					st_outputTransp.setDouble(10, transp[5][l]);
					st_outputTransp.setDouble(11, transp[6][l]);
					st_outputTransp.setDouble(12, transp[7][l]);
					st_outputTransp.setDouble(13, transp[8][l]);
					st_outputTransp.setDouble(14, transp[9][l]);
					st_outputTransp.setDouble(15, transp[10][l]);
					st_outputTransp.setDouble(16, transp[11][l]);
					st_outputTransp.addBatch();
				}
			}
			st_outputTransp.executeBatch();
			db.setAutoCommit(true);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	private void insertSWCBulk() {
		try {
			db.setAutoCommit(false);
			for (int l = 0; l < NSolLyrs; l++) {
				st_outputSWCBulk.setInt(1, control.getYear());
				st_outputSWCBulk.setInt(2, globals.currIter);
				st_outputSWCBulk.setInt(3, l + 1);
				st_outputSWCBulk.setDouble(4, swc[0][l]);
				st_outputSWCBulk.setDouble(5, swc[1][l]);
				st_outputSWCBulk.setDouble(6, swc[2][l]);
				st_outputSWCBulk.setDouble(7, swc[3][l]);
				st_outputSWCBulk.setDouble(8, swc[4][l]);
				st_outputSWCBulk.setDouble(9, swc[5][l]);
				st_outputSWCBulk.setDouble(10, swc[6][l]);
				st_outputSWCBulk.setDouble(11, swc[7][l]);
				st_outputSWCBulk.setDouble(12, swc[8][l]);
				st_outputSWCBulk.setDouble(13, swc[9][l]);
				st_outputSWCBulk.setDouble(14, swc[10][l]);
				st_outputSWCBulk.setDouble(15, swc[11][l]);
				st_outputSWCBulk.addBatch();
			}
			st_outputSWCBulk.executeBatch();
			db.setAutoCommit(true);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
