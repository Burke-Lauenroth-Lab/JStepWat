package stepwat.input.grid;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import stepwat.LogFileIn;
import stepwat.input.Input;

public class Species extends Input {

	public class Grid_Init_Species_row implements Comparable<Grid_Init_Species_row>{
		int cell;
		boolean use_SpinUp;
		List<Boolean> species_seed_avail = new ArrayList<Boolean>();
		
		public String toString() {
			String lines = Integer.valueOf(cell)+",0,0,"+String.valueOf(use_SpinUp?1:0)+",";
			for(int i=0; i<species_seed_avail.size(); i++) {
				lines+=String.valueOf(species_seed_avail.get(i)?1:0);
				if(i!=(species_seed_avail.size()-1))
					lines+=",";
			}
			return lines;
		}
		@Override
		public int compareTo(Grid_Init_Species_row initSpecies) {
			return this.cell - initSpecies.cell;
		}
	}
	List<Grid_Init_Species_row> Grid_Init_Species = new ArrayList<Grid_Init_Species_row>();
	Map<String,Integer> speciesIndexMap = new HashMap<String, Integer>();
	
	public void read(Path initSpeciesFile) throws Exception {
		List<String> lines = java.nio.file.Files.readAllLines(initSpeciesFile, StandardCharsets.UTF_8);
		LogFileIn f = stepwat.LogFileIn.getInstance();
		int nFileItemsRead = 0;

		for (String line : lines) {
			// Skip Comments and empty lines
			if (!line.matches("^\\s*#.*") && !line.matches("^[\\s]*$")) {
				line = line.trim();
				String[] values = line.split(",");//split at commas
				switch (nFileItemsRead) {
				case 0:
					//"cell","copy_cell","copy_which","layer_num","depth","bulkd","fieldc","wiltpt","evco","trco_grass","trco_shrub","trco_tree","sand","clay","imperm","soiltemp"
					if(values.length < 5)
						f.LogError(LogFileIn.LogMode.ERROR, "grid_initSpecies.csv onRead : Illegal Formatted Row : Expected at least 5 Values, read "+String.valueOf(values.length));
					if(values[0] != "cell")
						f.LogError(LogFileIn.LogMode.ERROR, "grid_initSpecies.csv onRead : Header Column Illegal Header : Expected 'cell' read "+values[0]);
					if(values[1] != "copy_cell")
						f.LogError(LogFileIn.LogMode.ERROR, "grid_initSpecies.csv onRead : Header Column Illegal Header : Expected 'copy_cell' read "+values[1]);
					if(values[2] != "copy_which")
						f.LogError(LogFileIn.LogMode.ERROR, "grid_initSpecies.csv onRead : Header Column Illegal Header : Expected 'copy_which' read "+values[2]);
					if(values[3] != "use_SpinUp")
						f.LogError(LogFileIn.LogMode.ERROR, "grid_initSpecies.csv onRead : Header Column Illegal Header : Expected 'use_SpinUp' read "+values[3]);
					for(int i=4; i<values.length; i++) {
						speciesIndexMap.put(values[i], i-4);
					}
					break;
				default:
					int cell=0, copy=0;
					try {
						cell = Integer.parseInt(values[0]);
						copy = Integer.parseInt(values[1]);
					} catch(NumberFormatException e) {
						f.LogError(LogFileIn.LogMode.ERROR, "grid_initSpecies.csv onRead : cell or copy_option bad : Could not convert string to integer. In row "+ Integer.toString(cell)+"." + e.getMessage());
					}
					if(values.length < 5 && copy!=1)
						f.LogError(LogFileIn.LogMode.ERROR, "grid_initSpecies.csv onRead : Illegal Formatted Row : Expected at least 5 Values read "+String.valueOf(values.length));
					try {
						if(copy==1) {
							int copyCell = Integer.parseInt(values[2]);
							//with a little work you could copy any cell
							if(copyCell < cell && copyCell >= 0) {
								boolean found = false;
								for(int i=0; i<Grid_Init_Species.size(); i++) {
									if(Grid_Init_Species.get(i).cell == copyCell) {
										Grid_Init_Species_row temp = new Grid_Init_Species_row();
										temp.cell = cell;
										temp.use_SpinUp = Grid_Init_Species.get(i).use_SpinUp;
										temp.species_seed_avail = Grid_Init_Species.get(i).species_seed_avail;
										found = true;
									}
								}
								if(!found)
									f.LogError(LogFileIn.LogMode.ERROR, "grid_initSpecies.csv onRead : Copy Cell : Could find cell to copy ( "+String.valueOf(copyCell) + ").");
							} else {
								f.LogError(LogFileIn.LogMode.ERROR, "grid_initSpecies.csv onRead : Copy Cell : Bad Value");
							}
							//int copyLayer = Integer.parseInt(values[3]);
						} else {
							Grid_Init_Species_row row = new Grid_Init_Species_row();
							row.cell = cell;
							for(int i=4; i<values.length; i++) {
								row.species_seed_avail.add(Integer.parseInt(values[i])>0 ? true : false);
							}
							Grid_Init_Species.add(row);
						}
					} catch(NumberFormatException e) {
						f.LogError(LogFileIn.LogMode.ERROR, "grid_soils.csv onRead : Number format wrong : Could not convert string to number. In row "+ Integer.toString(cell)+"." + e.getMessage());
					}
					break;
				}
				nFileItemsRead++;
			}
		}
		//Now sort the cells
		Collections.sort(Grid_Init_Species);
		this.data = true;
	}
	public void write(Path initSpeciesFile) throws IOException {
		List<String> lines = new ArrayList<String>();
		
		String header = "cell,copy_cell,copy_which,";
		for(Map.Entry<String, Integer> entry : speciesIndexMap.entrySet()) {
			String key = entry.getKey();
			//int value = entry.getValue().intValue();
			header += key+",";
		}
		header = header.substring(0, header.length()-1);
		lines.add(header);
		for(int i=0; i<Grid_Init_Species.size(); i++) {
			lines.add(Grid_Init_Species.get(i).toString());
		}
		Files.write(initSpeciesFile, lines, StandardCharsets.UTF_8);
	}
}
