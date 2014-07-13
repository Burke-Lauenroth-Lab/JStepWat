package stepwat;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;


public class LogFileIn {
	private static LogFileIn instance = null;
	private Path pLogFile;
	private boolean bLogged;
	private boolean fileSet = false;
	private boolean fileGood = false;
	private List<String> lines;
	
	private LogFileIn() {
		setbLogged(false);
		lines = new ArrayList<String>();
	}
	
	public boolean isbLogged() {
		return bLogged;
	}

	public void setbLogged(boolean bLogged) {
		this.bLogged = bLogged;
	}

	public static LogFileIn getInstance() {
		if(instance == null) {
			instance = new LogFileIn();
		}
		return instance;
	}
	
	public void onClear() {
		lines.clear();
	}
	
	public enum LogMode {
		NOTE, WARN, ERROR, EXIT, FATAL, MAX_ERROR
	}
	
	public void LogError(LogMode mode, String Message) throws Exception {
		if(pLogFile == null)
			pLogFile= Paths.get("");
		Path path = Paths.get(pLogFile.toString());
		String output = "";
		setbLogged(true);
		
		if(mode == LogMode.NOTE)
			output = "NOTE: ";
		else if(mode == LogMode.WARN)
			output = "WARNING: ";
		else if(mode == LogMode.ERROR)
			output = "ERROR: ";
		
		output += Message+"\n";
		lines.add(output);
		
		try {
			if(this.ready())
				Files.write(path, lines, StandardCharsets.UTF_8);
			else
				System.out.println(lines.get(lines.size()-1)+"\n");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("SYSTEM: Cannot write to File fLogFile in LogError()\n");
			e.printStackTrace();
		}
		
		if(mode == LogMode.ERROR | mode == LogMode.EXIT | mode == LogMode.FATAL) {
			throw new Exception(output);
		}
	}
	public List<String> onGetLog() {
		return this.lines;
	}

	public Path getLogFile() {
		return pLogFile;
	}
	public void setLogFile(Path LogFile) {
		this.fileSet = true;
		this.pLogFile = LogFile;
	}
	public boolean ready() {
		if(this.fileGood)
			return true;
		if(this.fileSet) {
			if(Files.notExists(this.pLogFile.getParent())) {
				System.out.println("LogFileIn: Path to log file does not exist. Path: "+this.pLogFile.getParent().toString()+"\n");
				return false;
			} else {
				this.fileGood = true;
				return true;
			}
		} else {
			return false;
		}
	}
}
