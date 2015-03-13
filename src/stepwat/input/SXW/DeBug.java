package stepwat.input.SXW;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import stepwat.LogFileIn;
import stepwat.input.Input;

public class DeBug extends Input {
	public String debugFileName = "";
	public List<Integer> years = new ArrayList<Integer>();
	
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
						f.LogError(LogFileIn.LogMode.ERROR, "ST SXW debug.in : debug output file name has space.");
					debugFileName = values[0];
					break;
				default:
					for(int i=0; i<values.length; i++) {
						try {
							years.add(new Integer(Integer.valueOf(values[i])));
						} catch(NumberFormatException e) {
							f.LogError(LogFileIn.LogMode.ERROR, "ST SXW debug.in : Year could not be converted."+e.toString());
						}
					}
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
		lines.add("#sxwdebug.in, for debugging STEPPEWAT... this file is completely optional - DLM: 08-01-12");
		lines.add("#must specify if you want to use via something like './stepwat -f files.in -ssxwdebug.in -q -e'.  There can't be a space between the -s and the name of the debug inputfile for some reason...");
		lines.add("");
		lines.add(this.debugFileName + "\t#name of file to write debug info to");
		for(int i=0; i<years.size(); i++) {
			lines.add(Integer.toString(years.get(i).intValue()));
		}
		java.nio.file.Files.write(file, lines, StandardCharsets.UTF_8);
	}
}
