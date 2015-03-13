package stepwat.input.SXW;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import stepwat.LogFileIn;
import stepwat.input.Input;

public class Times extends Input {
	String time = "";
	
	@Override
	public void read(Path file) throws Exception {
		List<String> lines = java.nio.file.Files.readAllLines(file, StandardCharsets.UTF_8);
		LogFileIn f = stepwat.LogFileIn.getInstance();
		int nFileItemsRead = 0;
		for (String line : lines) {
			// Skip Comments and empty lines
			if (!line.matches("^\\s*#.*") && !line.matches("^[\\s]*$")) {
				line = line.trim();
				String[] values = line.split("#")[0].split("[ \t]+");// Remove comment  after data
				switch (nFileItemsRead) {
				case 0:
					if (values.length != 1)
						f.LogError(LogFileIn.LogMode.ERROR, "ST SXW sxwtimes.in : sxwtimes.in time value has a space.");
					time = values[0];
					break;
				default:
					f.LogError(LogFileIn.LogMode.ERROR,
							"SXW sxwtimes.in : unkown line.");
					break;
				}
				nFileItemsRead++;
			}
		}
		this.data = true;
	}

	@Override
	public void write(Path file) throws IOException {
		List<String> lines = new ArrayList<String>();
		lines.add("# sxw times definition file STEPPEWAT - DLM 07-19-12");
		lines.add("");
		lines.add("# can be one of three options (w/out the quotes): 'week', 'month', or 'day'.  Corresponds to the transpiration intervals.");
		lines.add(time);
		java.nio.file.Files.write(file, lines, StandardCharsets.UTF_8);
	}
	
	public boolean verify() {
		if(time.toLowerCase().compareTo("week") == 0)
			return true;
		else if(time.toLowerCase().compareTo("month") == 0)
			return true;
		else if(time.toLowerCase().compareTo("day") == 0)
			return true;
		
		return false;
	}
}
