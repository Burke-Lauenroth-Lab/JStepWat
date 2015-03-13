package stepwat.input.grid;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import stepwat.LogFileIn;
import stepwat.input.ST.ST_Input;

public class Grid_Input {
	/**
	 * The project directory path for ST
	 */
	public String prjDir;
	/**
	 * The relative path and name for files.in file.
	 */
	public String filesInName = "files.in";
	
	//Grid input files
	public stepwat.input.grid.Files files;
	public Disturbances disturbances;
	public Setup setup;
	public Soils soils;
	public SeedDispersal seedDispersal;
	public Species species;
	
	//ST input data
	public ST_Input st_input;
	
	public Grid_Input() {
		Path currentRelativePath = Paths.get("");
		//This should get the applications launch location
		this.prjDir = currentRelativePath.toAbsolutePath().toString();
		Init();
	}
	
	public Grid_Input(String prjDir) {
		this.prjDir = prjDir;
		Init();
	}
	
	public Grid_Input(String prjDir, String filesIn) {
		this.prjDir = prjDir;
		this.filesInName = filesIn;
		Init();
	}
	
	private void Init() {
		st_input = new ST_Input();
		files = new stepwat.input.grid.Files();
		disturbances = new Disturbances();
		setup = new Setup();
		soils = new Soils();
		species = new Species();
	}
	
	public void readGridInputs() throws Exception {
		LogFileIn f = stepwat.LogFileIn.getInstance();
		if(Files.notExists(Paths.get(prjDir, filesInName))) {
			f.LogError(LogFileIn.LogMode.ERROR, "Grid " + filesInName + " : Directory does not exist.");
		}
		files.read(Paths.get(prjDir, filesInName));
		
		//Lets check to see if all the files exist.
		filesExist();
		
		//Read in the ST files
		st_input.prjDir = Paths.get(this.prjDir, files.StepWatFilesDir).toString();
		st_input.filesInName = files.ST_Files;
		st_input.readInputData();
		//Finish reading the grid setup files
		setup.read(Paths.get(this.prjDir, files.setup));
		//Some of these files are optional.. only used if their option is set in setup
		//We will read them if they exist though. I do this because I wanted to separate
		//logic from input/output.
		if(Files.exists(Paths.get(prjDir, files.disturbances)))
			disturbances.read(Paths.get(this.prjDir, files.disturbances));
		if(Files.exists(Paths.get(prjDir, files.soils)))
			soils.read(Paths.get(this.prjDir, files.soils));
		if(Files.exists(Paths.get(prjDir, files.seedDispersal)))
			seedDispersal.read(Paths.get(this.prjDir, files.seedDispersal));
		if(Files.exists(Paths.get(prjDir, files.initSpecies)))
			species.read(Paths.get(this.prjDir, files.initSpecies));
	}
	
	public void filesExist() throws Exception {
		LogFileIn f = stepwat.LogFileIn.getInstance();
		//files.in should have been read or files location have been set
		List<String> messages = new ArrayList<String>();
		if(Files.notExists(Paths.get(prjDir, files.StepWatFilesDir)))
			messages.add("GRID StepWat Project Folder : " + files.StepWatFilesDir + " : File does not exist.");
		if(Files.notExists(Paths.get(prjDir, files.StepWatFilesDir, files.ST_Files)))
			messages.add("GRID StepWat files.in : " + files.ST_Files + " : File does not exist.");
		if(Files.notExists(Paths.get(prjDir, files.setup)))
			messages.add("GRID setup.in : " + files.setup + " : File does not exist.");
		if(Files.notExists(Paths.get(prjDir, files.disturbances)))
			f.LogError(LogFileIn.LogMode.WARN, "GRID disturbances.in : " + files.disturbances + " : File does not exist.");
		if(Files.notExists(Paths.get(prjDir, files.soils)))
			f.LogError(LogFileIn.LogMode.WARN, "GRID soils.in : " + files.soils + " : File does not exist.");
		if(Files.notExists(Paths.get(prjDir, files.seedDispersal)))
			f.LogError(LogFileIn.LogMode.WARN, "GRID seedDispersal.in : " + files.seedDispersal + " : File does not exist.");
		if(Files.notExists(Paths.get(prjDir, files.initSpecies)))
			f.LogError(LogFileIn.LogMode.WARN, "GRID initSpecies.in : " + files.initSpecies + " : File does not exist.");
		
		if(messages.size() > 0) {
			String message = "";
			for (String s : messages)
				message += s + "\n";
			f.LogError(LogFileIn.LogMode.ERROR, message);
		}
	}
	
	public void writeGridInputs() throws Exception {
		LogFileIn f = stepwat.LogFileIn.getInstance();
		
		if(files.data())
			files.write(Paths.get(prjDir, filesInName));
		else
			f.LogError(LogFileIn.LogMode.WARN, "GRID writeGridInputs : files.in appears to not have data.");
		
		st_input.writeInputData();
		
		if(setup.data())
			setup.write(Paths.get(this.prjDir, files.setup));
		else
			f.LogError(LogFileIn.LogMode.WARN, "GRID writeGridInputs : files.in appears to not have data.");
		
		if(disturbances.data())
			disturbances.write(Paths.get(this.prjDir, files.disturbances));
		if(soils.data())
			soils.write(Paths.get(this.prjDir, files.soils));
		if(seedDispersal.data())
			seedDispersal.write(Paths.get(this.prjDir, files.seedDispersal));
		if(species.data())
			species.write(Paths.get(this.prjDir, files.initSpecies));
	}
	
	public boolean verify(boolean useSoilWat) throws Exception {
		LogFileIn f = stepwat.LogFileIn.getInstance();
		//List<String> messages = new ArrayList<String>();
		
		if(!st_input.verify()) {
			return false;
		}
		
		if(setup.disturbances && !disturbances.data()) {
			f.LogError(LogFileIn.LogMode.WARN, "GRID verify : grid disturbances is on but disturbances.in has not been read or set.");
			return false;
		}
		if(setup.soils && useSoilWat && !soils.data()) {
			f.LogError(LogFileIn.LogMode.WARN, "GRID verify : grid soils is on and soilwat is on but soils.in has not been read or set.");
			return false;
		}
		if(setup.seedDispersal && !seedDispersal.data()) {
			f.LogError(LogFileIn.LogMode.WARN, "GRID verify : grid seed_dispersal is on but grid_seed_dispersal.in has not been read or set.");
			return false;
		}
		if(setup.seedDispersal && !species.data()) {
			f.LogError(LogFileIn.LogMode.WARN, "GRID verify : grid init_species is on but grid_initSpecies.csv has not been read or set.");
			return false;
		}
		//disturbances checks
		if(disturbances.data()) {
			if(disturbances.Grid_Disturb.size() != setup.gridCells()) {
				f.LogError(LogFileIn.LogMode.WARN, "GRID verify : grid disturbances : Wrong number of cells");
				return false;
			}
		}
		//soils checks
		if(soils.data()) {
			if(soils.Grid_Soils.length != setup.gridCells()) {
				f.LogError(LogFileIn.LogMode.WARN, "GRID verify : grid soils : Wrong number of cells");
				return false;
			}
		}
		//seedDispersal checks
		if(seedDispersal.data()) {
			if(!seedDispersal.verify()) {
				return false;
			}
		}
		//species checks
		if(species.data()) {
			if(species.Grid_Init_Species.size() != setup.gridCells()) {
				f.LogError(LogFileIn.LogMode.WARN, "GRID verify : grid initSpecies : Wrong number of cells");
				return false;
			}
		}
		
		return true;
	}
}
