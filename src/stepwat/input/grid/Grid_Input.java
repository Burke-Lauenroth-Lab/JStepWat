package stepwat.input.grid;

import java.nio.file.Path;
import java.nio.file.Paths;

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
	public Files files;
	public Disturbances disturbances;
	public Grid_Input grid_Input;
	public Setup setup;
	public Soils soils;
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
		files = new Files();
		disturbances = new Disturbances();
		grid_Input = new Grid_Input();
		setup = new Setup();
		soils = new Soils();
		species = new Species();
	}
	
	public void readGridInputs() {
		files.read(Paths.get(prjDir, filesInName));
		
	}
	
	public void writeGridInputs() {
		
	}
}
