package stepwat.input.ST;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ST_Input {
	
	/**
	 * The project directory path for ST
	 */
	public String prjDir;
	/**
	 * The relative path and name for files.in file.
	 */
	public String filesInName = "files.in";
	//These are the objects for the input files
	//They handle reading/writing functions
	public BmassFlags bmassFlags;
	public Environment environment;
	public Files files;
	public Model model;
	public MortFlags mortFlags;
	public Plot plot;
	public Rgroup rGroup;
	public Species species;
	
	public ST_Input() {
		Path currentRelativePath = Paths.get("");
		//This should get the applications launch location
		this.prjDir = currentRelativePath.toAbsolutePath().toString();
		Init();
	}
	public ST_Input(String prjDir) {
		this.prjDir = prjDir;
		Init();
	}
	public ST_Input(String prjDir, String filesIn) {
		this.prjDir = prjDir;
		this.filesInName = filesIn;
		Init();
	}
	private void Init() {
		bmassFlags = new BmassFlags();
		environment = new Environment();
		files = new Files();
		model = new Model();
		mortFlags = new MortFlags();
		plot = new Plot();
		rGroup = new Rgroup();
		species = new Species();
	}
	
	public void readInputData() throws Exception {
		//After files order does not matter. This is because we store all
		//input before we verify any of it. Format though is checked
		files.read(Paths.get(prjDir, filesInName));
		model.read(Paths.get(prjDir, files.model));
		environment.read(Paths.get(prjDir, files.env));
		plot.read(Paths.get(prjDir, files.plot));
		bmassFlags.read(Paths.get(prjDir, files.bmassflags));
		mortFlags.read(Paths.get(prjDir, files.mortflags));
		rGroup.read(Paths.get(prjDir, files.rgroup));
		species.read(Paths.get(prjDir, files.species));
	}
	
	public void writeInputData() throws IOException {
		files.write(Paths.get(prjDir, filesInName));
		model.write(Paths.get(prjDir, files.model));
		environment.write(Paths.get(prjDir, files.env));
		plot.write(Paths.get(prjDir, files.plot));
		bmassFlags.write(Paths.get(prjDir, files.bmassflags));
		mortFlags.write(Paths.get(prjDir, files.mortflags));
		rGroup.write(Paths.get(prjDir, files.rgroup));
		species.write(Paths.get(prjDir, files.species));
	}
	
	public void setPrjDir(String Path) {
		this.prjDir = Path;
	}
	public void setFilesIn(String Path) {
		this.filesInName = Path;
	}
}
