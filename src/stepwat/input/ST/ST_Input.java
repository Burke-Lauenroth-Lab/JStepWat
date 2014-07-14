package stepwat.input.ST;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import stepwat.LogFileIn;

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
	public stepwat.input.ST.Files files;
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
		files = new stepwat.input.ST.Files();
		model = new Model();
		mortFlags = new MortFlags();
		plot = new Plot();
		rGroup = new Rgroup();
		species = new Species();
	}
	
	public void readInputData() throws Exception {
		//After files order does not matter. This is because we store all
		//input before we verify any of it. Format though is checked
		LogFileIn f = stepwat.LogFileIn.getInstance();
		if(Files.notExists(Paths.get(prjDir, filesInName))) {
			f.LogError(LogFileIn.LogMode.ERROR, "ST " + filesInName + " : Directory does not exist.");
		}
		files.read(Paths.get(prjDir, filesInName));
		
		//Now check to see if other files exist
		filesExist();
		
		model.read(Paths.get(prjDir, files.model));
		environment.read(Paths.get(prjDir, files.env));
		plot.read(Paths.get(prjDir, files.plot));
		bmassFlags.read(Paths.get(prjDir, files.bmassflags));
		mortFlags.read(Paths.get(prjDir, files.mortflags));
		rGroup.read(Paths.get(prjDir, files.rgroup));
		species.read(Paths.get(prjDir, files.species));
	}
	
	public void filesExist() throws Exception {
		LogFileIn f = stepwat.LogFileIn.getInstance();
		//files.in should have been read or files location have been set
		List<String> messages = new ArrayList<String>();
		if(Files.notExists(Paths.get(prjDir, files.model)))
			messages.add("ST model.in : " + files.model + " : Directory does not exist.");
		if(Files.notExists(Paths.get(prjDir, files.env)))
			messages.add("ST env.in : " + files.env + " : Directory does not exist.");
		if(Files.notExists(Paths.get(prjDir, files.plot)))
			messages.add("ST plot.in : " + files.plot + " : Directory does not exist.");
		if(Files.notExists(Paths.get(prjDir, files.bmassflags)))
			messages.add("ST bmassflags.in : " + files.bmassflags + " : Directory does not exist.");
		if(Files.notExists(Paths.get(prjDir, files.mortflags)))
			messages.add("ST mortflags.in : " + files.mortflags + " : Directory does not exist.");
		if(Files.notExists(Paths.get(prjDir, files.rgroup)))
			messages.add("ST rgroup.in : " + files.rgroup + " : Directory does not exist.");
		if(Files.notExists(Paths.get(prjDir, files.species)))
			messages.add("ST species.in : " + files.species + " : Directory does not exist.");
		
		if(messages.size() > 0) {
			String message = "";
			for (String s : messages)
				message += s + "\n";
			f.LogError(LogFileIn.LogMode.ERROR, message);
		}
	}
	
	public void writeInputData() throws Exception {
		LogFileIn f = stepwat.LogFileIn.getInstance();
		if(files.data())
			files.write(Paths.get(prjDir, filesInName));
		else
			f.LogError(LogFileIn.LogMode.WARN, "ST writeInputData files.in appears to have no data set.");
		
		if(model.data())
			model.write(Paths.get(prjDir, files.model));
		else
			f.LogError(LogFileIn.LogMode.WARN, "ST writeInputData model.in appears to have no data set.");
		
		if(environment.data())
			environment.write(Paths.get(prjDir, files.env));
		else
			f.LogError(LogFileIn.LogMode.WARN, "ST writeInputData env.in appears to have no data set.");
		
		if(plot.data())
			plot.write(Paths.get(prjDir, files.plot));
		else
			f.LogError(LogFileIn.LogMode.WARN, "ST writeInputData plot.in appears to have no data set.");
		
		if(bmassFlags.data())
			bmassFlags.write(Paths.get(prjDir, files.bmassflags));
		else
			f.LogError(LogFileIn.LogMode.WARN, "ST writeInputData bmassflags.in appears to have no data set.");
		
		if(mortFlags.data())
			mortFlags.write(Paths.get(prjDir, files.mortflags));
		else
			f.LogError(LogFileIn.LogMode.WARN, "ST writeInputData mortflags.in appears to have no data set.");
		
		if(rGroup.data())
			rGroup.write(Paths.get(prjDir, files.rgroup));
		else
			f.LogError(LogFileIn.LogMode.WARN, "ST writeInputData rgroup.in appears to have no data set.");
		
		if(species.data())
			species.write(Paths.get(prjDir, files.species));
		else
			f.LogError(LogFileIn.LogMode.WARN, "ST writeInputData species.in appears to have no data set.");
	}
	
	public void setPrjDir(String Path) {
		this.prjDir = Path;
	}
	public void setFilesIn(String Path) {
		this.filesInName = Path;
	}
	
	
	
	public boolean verify() throws Exception {
		LogFileIn f = stepwat.LogFileIn.getInstance();
		
		if(model.nIterations < 1 || model.nYears < 1) {
			f.LogError(LogFileIn.LogMode.WARN, "ST verify : models iterations or nYears is less then 1.");
			return false;
		}
		
		return true;
	}
}
