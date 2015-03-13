package stepwat.input.SXW;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import soilwat.InputData;
import soilwat.SW_CONTROL;
import soilwat.SW_OUTPUT.OutKey;
import soilwat.SW_OUTPUT.OutPeriod;
import soilwat.SW_OUTPUT.OutSum;
import stepwat.LogFileIn;
import stepwat.input.ST.Rgroup;

public class SXW_Input {
	/**
	 * The project directory path for ST
	 */
	public String prjDir;
	/**
	 * The relative path and name for files.in file.
	 */
	public String filesInName = "files.in";
	
	public BVT bvt;
	public stepwat.input.SXW.Files files;
	public Phenology phen;
	public Production prod;
	public Roots roots;
	//public Times times;
	public DeBug deBug;
	
	//Soilwats input
	public soilwat.InputData swInput;
	//This is used to verify swInput
	private soilwat.SW_CONTROL swControl;
	
	public SXW_Input(String prjDir, String filesIn) {
		this.prjDir = prjDir;
		this.filesInName = filesIn;
		Init();
	}
	
	private void Init() {
		bvt = new BVT();
		files = new stepwat.input.SXW.Files();
		phen = new Phenology();
		prod = new Production();
		roots = new Roots();
		//times = new Times();
		
		deBug = new DeBug();
		
		swControl = new SW_CONTROL();
		swInput = new InputData();
	}
	
	public void readInputData() throws Exception {
		//After files order does not matter. This is because we store all
		//input before we verify any of it. Format though is checked
		LogFileIn f = stepwat.LogFileIn.getInstance();
		if(Files.notExists(Paths.get(prjDir, filesInName))) {
			f.LogError(LogFileIn.LogMode.ERROR, "SXW " + filesInName + " : Directory does not exist.");
		}
		files.read(Paths.get(prjDir, filesInName));
		
		//Now check to see if other files exist
		filesExist();
		
		bvt.read(Paths.get(prjDir, files.BVT));
		phen.read(Paths.get(prjDir, files.Phenology));
		prod.read(Paths.get(prjDir, files.Production));
		roots.read(Paths.get(prjDir, files.Roots));
		//times.read(Paths.get(prjDir, files.Times));
		
		if(files.debug)
			deBug.read(Paths.get(prjDir, files.DeBug));
		
		//Have soilwat read in the soilwat prj files and store in swInput
		swInput.onRead(Paths.get(prjDir, files.SoilWatFiles).toString());
	}
	
	public void filesExist() throws Exception {
		LogFileIn f = stepwat.LogFileIn.getInstance();
		//files.in should have been read or files location have been set
		List<String> messages = new ArrayList<String>();
		if(Files.notExists(Paths.get(prjDir, files.BVT)))
			messages.add("SXW sxwbvt.in : " + files.BVT + " : Directory does not exist.");
		if(Files.notExists(Paths.get(prjDir, files.Phenology)))
			messages.add("SXW sxwphen.in : " + files.Phenology + " : Directory does not exist.");
		if(Files.notExists(Paths.get(prjDir, files.Production)))
			messages.add("SXW sxwprod.in : " + files.Production + " : Directory does not exist.");
		if(Files.notExists(Paths.get(prjDir, files.Roots)))
			messages.add("SXW sxwroots.in : " + files.Roots + " : Directory does not exist.");
		//if(Files.notExists(Paths.get(prjDir, files.Times)))
		//	messages.add("SXW sxwtimes.in : " + files.Times + " : Directory does not exist.");
		if(files.debug)
			if(Files.notExists(Paths.get(prjDir, files.DeBug)))
				messages.add("SXW sxwdebug.in : " + files.Times + " : Directory does not exist.");
		
		if(messages.size() > 0) {
			String message = "";
			for (String s : messages)
				message += s + "\n";
			f.LogError(LogFileIn.LogMode.ERROR, message);
		}
	}
	
	/**
	 * This function will only create the Folder Str
	 * within the prj folder.
	 * @throws Exception 
	 */
	public void createPrjFolders() throws Exception {
		LogFileIn f = LogFileIn.getInstance();
		if(Files.notExists(Paths.get(this.prjDir))) {
			try {
				Files.createDirectories(Paths.get(this.prjDir));
			} catch (IOException e) {
				f.LogError(LogFileIn.LogMode.ERROR, this.prjDir + " : Directory does not exist and can not be created."+e.getMessage());
			}
		}
		if(Files.notExists(Paths.get(this.prjDir,this.filesInName))) {
			try {
				Files.createDirectories(Paths.get(this.prjDir,this.filesInName).getParent());
				Files.createFile(Paths.get(this.prjDir,this.filesInName));
			} catch (IOException e) {
				f.LogError(LogFileIn.LogMode.ERROR, this.filesInName + " : Directory does not exist and can not be created."+e.getMessage());
			}
		}
		if(Files.notExists(Paths.get(this.prjDir,this.files.BVT))) {
			try {
				Files.createDirectories(Paths.get(this.prjDir,this.files.BVT).getParent());
				Files.createFile(Paths.get(this.prjDir,this.files.BVT));
			} catch (IOException e) {
				f.LogError(LogFileIn.LogMode.ERROR, this.files.BVT + " : Directory does not exist and can not be created."+e.getMessage());
			}
		}
		if(Files.notExists(Paths.get(this.prjDir,this.files.Phenology))) {
			try {
				Files.createDirectories(Paths.get(this.prjDir,this.files.Phenology).getParent());
				Files.createFile(Paths.get(this.prjDir,this.files.Phenology));
			} catch (IOException e) {
				f.LogError(LogFileIn.LogMode.ERROR, this.files.Phenology + " : Directory does not exist and can not be created."+e.getMessage());
			}
		}
		if(Files.notExists(Paths.get(this.prjDir,this.files.Production))) {
			try {
				Files.createDirectories(Paths.get(this.prjDir,this.files.Production).getParent());
				Files.createFile(Paths.get(this.prjDir,this.files.Production));
			} catch (IOException e) {
				f.LogError(LogFileIn.LogMode.ERROR, this.files.Production + " : Directory does not exist and can not be created."+e.getMessage());
			}
		}
		if(Files.notExists(Paths.get(this.prjDir,this.files.Roots))) {
			try {
				Files.createDirectories(Paths.get(this.prjDir,this.files.Roots).getParent());
				Files.createFile(Paths.get(this.prjDir,this.files.Roots));
			} catch (IOException e) {
				f.LogError(LogFileIn.LogMode.ERROR, this.files.Roots + " : Directory does not exist and can not be created."+e.getMessage());
			}
		}
		if(Files.notExists(Paths.get(this.prjDir,this.files.SoilWatFiles))) {
			try {
				Files.createDirectories(Paths.get(this.prjDir,this.files.SoilWatFiles).getParent());
				Files.createFile(Paths.get(this.prjDir,this.files.SoilWatFiles));
			} catch (IOException e) {
				f.LogError(LogFileIn.LogMode.ERROR, this.files.SoilWatFiles + " : Directory does not exist and can not be created."+e.getMessage());
			}
		}
		if(Files.notExists(Paths.get(this.prjDir,this.files.Times))) {
			try {
				Files.createDirectories(Paths.get(this.prjDir,this.files.Times).getParent());
				Files.createFile(Paths.get(this.prjDir,this.files.Times));
			} catch (IOException e) {
				f.LogError(LogFileIn.LogMode.ERROR, this.files.Times + " : Directory does not exist and can not be created."+e.getMessage());
			}
		}
		if (files.debug) {
			if (Files.notExists(Paths.get(this.prjDir, this.files.DeBug))) {
				try {
					Files.createDirectories(Paths.get(this.prjDir,
							this.files.DeBug).getParent());
					Files.createFile(Paths.get(this.prjDir, this.files.DeBug));
				} catch (IOException e) {
					f.LogError(
							LogFileIn.LogMode.ERROR,
							this.files.DeBug
									+ " : Directory does not exist and can not be created."
									+ e.getMessage());
				}
			}
		}
	}
	
	public void writeInputData() throws Exception {
		LogFileIn f = stepwat.LogFileIn.getInstance();
		createPrjFolders();
		if(files.data())
			files.write(Paths.get(prjDir, filesInName));
		else
			f.LogError(LogFileIn.LogMode.WARN, "SXW writeInputData sxwfiles.in appears to have no data set.");
		
		if(bvt.data())
			bvt.write(Paths.get(prjDir, files.BVT));
		else
			f.LogError(LogFileIn.LogMode.WARN, "SXW writeInputData sxwbvt.in appears to have no data set.");
		
		if(phen.data())
			phen.write(Paths.get(prjDir, files.Phenology));
		else
			f.LogError(LogFileIn.LogMode.WARN, "SXW writeInputData sxwphen.in appears to have no data set.");
		
		if(prod.data())
			prod.write(Paths.get(prjDir, files.Production));
		else
			f.LogError(LogFileIn.LogMode.WARN, "SXW writeInputData sxwprod.in appears to have no data set.");
		
		if(roots.data())
			roots.write(Paths.get(prjDir, files.Roots));
		else
			f.LogError(LogFileIn.LogMode.WARN, "SXW writeInputData sxwroots.in appears to have no data set.");
		
		//if(times.data())
		//	times.write(Paths.get(prjDir, files.Times));
		//else
		//	f.LogError(LogFileIn.LogMode.WARN, "SXW writeInputData sxwtimes.in appears to have no data set.");
		
		if(files.debug)
			if(deBug.data())
				deBug.write(Paths.get(prjDir, files.DeBug));
			else
				f.LogError(LogFileIn.LogMode.WARN, "SXW writeInputData sxwdegub.in appears to have no data set.");
		
		swInput.onWrite(prjDir);
		//swControl.onSetInput(swInput);
		//swControl.onWriteOutputs(prjDir);
	}
	
	public void setPrjDir(String Path) {
		this.prjDir = Path;
	}
	
	public void setFilesIn(String Path) {
		this.filesInName = Path;
	}
	
	public boolean verify(Rgroup rgroups) throws Exception {
		//LogFileIn f = stepwat.LogFileIn.getInstance();
		
		//if(!times.verify()) {
		//	f.LogError(LogFileIn.LogMode.WARN, "SXW verify : times.in : Value needs to be week, month, or day.");
		//	return false;
		//}
		
		if(!roots.verify(rgroups))
			return false;
		
		if(!prod.verify(rgroups))
			return false;
		
		if(!phen.verify(rgroups))
			return false;
		
		swCheckOut();
		
		swControl.onSetInput(swInput);
		if(!swControl.onVerify())
			return false;
		
		return true;
	}
	
	/**
	 * make sure the outsetup file for soilwat contains only
	 * the given information
	 * Note that there won't actually be any output.  These
	 * keys are required to trigger the correct part of the
	 * output accumulation routines.  Refer to the Output
	 * module of SOILWAT for more.
	 */
	private void swCheckOut() {
		swInput.outputSetupIn.outputs[OutKey.eSW_Temp.idx()].onSet(true, OutSum.eSW_Avg, OutPeriod.SW_YEAR, 1, 366, "temp_air");
		swInput.outputSetupIn.outputs[OutKey.eSW_Precip.idx()].onSet(true, OutSum.eSW_Sum, OutPeriod.SW_YEAR, 1, 366, "precip");
		swInput.outputSetupIn.outputs[OutKey.eSW_SoilInf.idx()].onSet(false, OutSum.eSW_Off, OutPeriod.SW_YEAR, 1, 366, "infiltration");
		swInput.outputSetupIn.outputs[OutKey.eSW_Runoff.idx()].onSet(false, OutSum.eSW_Off, OutPeriod.SW_WEEK, 1, 366, "runoff");
		swInput.outputSetupIn.outputs[OutKey.eSW_VWCBulk.idx()].onSet(false, OutSum.eSW_Off, OutPeriod.SW_MONTH, 1, 366, "vwc_bulk");
		swInput.outputSetupIn.outputs[OutKey.eSW_VWCMatric.idx()].onSet(false, OutSum.eSW_Off, OutPeriod.SW_YEAR, 1, 366, "vwc_matric");
		swInput.outputSetupIn.outputs[OutKey.eSW_SWCBulk.idx()].onSet(true, OutSum.eSW_Avg, OutPeriod.SW_MONTH, 1, 366, "swc_bulk");
		swInput.outputSetupIn.outputs[OutKey.eSW_SWABulk.idx()].onSet(false, OutSum.eSW_Off, OutPeriod.SW_MONTH, 1, 366, "swa_bulk");
		swInput.outputSetupIn.outputs[OutKey.eSW_SWAMatric.idx()].onSet(false, OutSum.eSW_Off, OutPeriod.SW_YEAR, 1, 366, "swa_matric");
		swInput.outputSetupIn.outputs[OutKey.eSW_SWPMatric.idx()].onSet(false, OutSum.eSW_Off, OutPeriod.SW_WEEK, 1, 366, "swp_matric");
		swInput.outputSetupIn.outputs[OutKey.eSW_SurfaceWater.idx()].onSet(false, OutSum.eSW_Off, OutPeriod.SW_DAY, 1, 366, "surface_water");
		//OutPeriod p = OutPeriod.SW_DAY;
		//if(times.time.toLowerCase().compareTo("week") == 0)
			//p = OutPeriod.SW_WEEK;
		//else if(times.time.toLowerCase().compareTo("month") == 0)
			//p = OutPeriod.SW_MONTH;
		//else if(times.time.toLowerCase().compareTo("day") == 0)
			//p = OutPeriod.SW_DAY;
		swInput.outputSetupIn.outputs[OutKey.eSW_Transp.idx()].onSet(true, OutSum.eSW_Sum, OutPeriod.SW_MONTH, 1, 366, "transp");
		swInput.outputSetupIn.outputs[OutKey.eSW_EvapSoil.idx()].onSet(false, OutSum.eSW_Off, OutPeriod.SW_DAY, 1, 366, "evap_soil");
		swInput.outputSetupIn.outputs[OutKey.eSW_EvapSurface.idx()].onSet(false, OutSum.eSW_Off, OutPeriod.SW_WEEK, 1, 366, "evap_surface");
		swInput.outputSetupIn.outputs[OutKey.eSW_Interception.idx()].onSet(false, OutSum.eSW_Off, OutPeriod.SW_MONTH, 1, 366, "interception");
		swInput.outputSetupIn.outputs[OutKey.eSW_LyrDrain.idx()].onSet(false, OutSum.eSW_Off, OutPeriod.SW_DAY, 1, 366, "percolation");
		swInput.outputSetupIn.outputs[OutKey.eSW_HydRed.idx()].onSet(false, OutSum.eSW_Off, OutPeriod.SW_WEEK, 1, 366, "hydred");
		swInput.outputSetupIn.outputs[OutKey.eSW_AET.idx()].onSet(true, OutSum.eSW_Sum, OutPeriod.SW_YEAR, 1, 366, "aet");
		swInput.outputSetupIn.outputs[OutKey.eSW_PET.idx()].onSet(false, OutSum.eSW_Off, OutPeriod.SW_DAY, 1, 366, "pet");
		swInput.outputSetupIn.outputs[OutKey.eSW_WetDays.idx()].onSet(false, OutSum.eSW_Off, OutPeriod.SW_DAY, 1, 366, "wetdays");
		swInput.outputSetupIn.outputs[OutKey.eSW_SnowPack.idx()].onSet(false, OutSum.eSW_Off, OutPeriod.SW_WEEK, 1, 366, "snowpack");
		swInput.outputSetupIn.outputs[OutKey.eSW_DeepSWC.idx()].onSet(false, OutSum.eSW_Off, OutPeriod.SW_MONTH, 1, 366, "deep_drain");
		swInput.outputSetupIn.outputs[OutKey.eSW_SoilTemp.idx()].onSet(false, OutSum.eSW_Off, OutPeriod.SW_MONTH, 1, 366, "temp_soil");
		swInput.outputSetupIn.outputs[OutKey.eSW_Estab.idx()].onSet(false, OutSum.eSW_Off, OutPeriod.SW_YEAR, 1, 366, "estab");
		swInput.outputSetupIn.TimeSteps[OutPeriod.SW_DAY.idx()] = false;
		swInput.outputSetupIn.TimeSteps[OutPeriod.SW_WEEK.idx()] = false;
		swInput.outputSetupIn.TimeSteps[OutPeriod.SW_MONTH.idx()] = false;
		swInput.outputSetupIn.TimeSteps[OutPeriod.SW_YEAR.idx()] = false;
	}
	
}
