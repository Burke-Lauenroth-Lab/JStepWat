package stepwat.input.grid;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import stepwat.LogFileIn;

public class Disturbances {

	public class Disturb_row {
		int cell;
		boolean fecal_pat_use;
		boolean ant_mound_use;
		boolean animal_burrows_use;
		int kill_yr;
		int kill_frq;
		int extirp;
		
		public String toString() {
			return Integer.toString(cell)+","+String.valueOf(fecal_pat_use?1:0)+","+String.valueOf(ant_mound_use?1:0)+","+String.valueOf(animal_burrows_use?1:0)+","+Integer.toString(kill_yr)+","+Integer.toString(kill_frq)+","+Integer.toString(extirp);
		}
	}	
	List<Disturb_row> Grid_Disturb = new ArrayList<Disturbances.Disturb_row>();
	
	public void read(String DisturbancesFile) throws Exception {
		List<String> lines = java.nio.file.Files.readAllLines(Paths.get(DisturbancesFile), StandardCharsets.UTF_8);
		LogFileIn f = stepwat.LogFileIn.getInstance();
		int nFileItemsRead = 0;
		for (String line : lines) {
			// Skip Comments and empty lines
			if (!line.matches("^\\s*#.*") && !line.matches("^[\\s]*$")) {
				line = line.trim();
				String[] values = line.split(",");//split at commas
				switch (nFileItemsRead) {
				case 0:
					if(values.length != 7)
						f.LogError(LogFileIn.LogMode.ERROR, "grid_disturbances.csv onRead : Illegal Formatted Row : Expected 7 Values read "+String.valueOf(values.length));
					if(values[0] != "cell")
						f.LogError(LogFileIn.LogMode.ERROR, "grid_disturbances.csv onRead : Header Column Illegal Header : Expected 'cell' read "+values[0]);
					if(values[1] != "fecal_pat_use")
						f.LogError(LogFileIn.LogMode.ERROR, "grid_disturbances.csv onRead : Header Column Illegal Header : Expected 'fecal_pat_use' read "+values[1]);
					if(values[2] != "ant_mound_use")
						f.LogError(LogFileIn.LogMode.ERROR, "grid_disturbances.csv onRead : Header Column Illegal Header : Expected 'ant_mound_use' read "+values[2]);
					if(values[3] != "animal_burrows_use")
						f.LogError(LogFileIn.LogMode.ERROR, "grid_disturbances.csv onRead : Header Column Illegal Header : Expected 'animal_burrows_use' read "+values[3]);
					if(values[4] != "kill_yr")
						f.LogError(LogFileIn.LogMode.ERROR, "grid_disturbances.csv onRead : Header Column Illegal Header : Expected 'kill_yr' read "+values[4]);
					if(values[5] != "killfrq")
						f.LogError(LogFileIn.LogMode.ERROR, "grid_disturbances.csv onRead : Header Column Illegal Header : Expected 'killfrq' read "+values[5]);
					if(values[6] != "extirp")
						f.LogError(LogFileIn.LogMode.ERROR, "grid_disturbances.csv onRead : Header Column Illegal Header : Expected 'extirp' read "+values[6]);
					break;
				default:
					int cell=0;
					if(values.length != 7)
						f.LogError(LogFileIn.LogMode.ERROR, "grid_disturbances.csv onRead : Illegal Formatted Row : Expected 7 Values read "+String.valueOf(values.length));
					try {
						Disturb_row row = new Disturb_row();
						row.cell = cell = Integer.parseInt(values[0]);
						row.fecal_pat_use = Integer.parseInt(values[1])>0 ? true : false;
						row.ant_mound_use = Integer.parseInt(values[2])>0 ? true : false;
						row.animal_burrows_use = Integer.parseInt(values[3])>0 ? true : false;
						row.kill_yr = Integer.parseInt(values[4]);
						row.kill_frq = Integer.parseInt(values[5]);
						row.extirp = Integer.parseInt(values[6]);
						Grid_Disturb.add(row);
					} catch(NumberFormatException e) {
						f.LogError(LogFileIn.LogMode.ERROR, "grid_disturbances.csv onRead : Header Column Illegal Header : Could not convert string to integer. In row "+ Integer.toString(cell)+"." + e.getMessage());
					}
					break;
				}
			}
		}
	}
	public void write(Path DisturbancesFile) throws IOException {
		List<String> lines = new ArrayList<String>();
		
		lines.add("cell,fecal_pat_use,ant_mound_use,animal_burrows_use,kill_yr,killfrq,extirp");
		for(int i=0; i<Grid_Disturb.size(); i++) {
			lines.add(Grid_Disturb.get(i).toString());
		}
		Files.write(DisturbancesFile, lines, StandardCharsets.UTF_8);
	}
}
