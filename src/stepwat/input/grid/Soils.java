package stepwat.input.grid;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import stepwat.LogFileIn;
import stepwat.input.Input;

public class Soils extends Input {

	public static final int MAX_LAYERS = 25;
	
	public class Grid_Soil_Lyr implements Comparable<Grid_Soil_Lyr> {
		int layer_num;
		int width;
		int depth;
		float bulkd;
		float fieldc;
		float wiltpt;
		float evco;
		float trco_grass;
		float trco_shrub;
		float trco_tree;
		float sand;
		float clay;
		float imperm;
		float soiltemp;
		
		public String toString() {
			return Integer.toString(layer_num)+","+Integer.toString(depth)+","+Float.toString(bulkd)+","+Float.toString(fieldc)+","+Float.toString(wiltpt)+","+Float.toString(evco)+","+
					Float.toString(trco_grass)+","+Float.toString(trco_shrub)+","+Float.toString(trco_tree)+","+Float.toString(sand)+","+Float.toString(clay)+","+Float.toString(imperm)+","+
					Float.toString(soiltemp);
		}
		@Override
		public int compareTo(Grid_Soil_Lyr lyr) {
			return this.layer_num - lyr.layer_num;
		}
	}
	public class Grid_Soil_Lyrs implements Comparable<Grid_Soil_Lyrs>{
		int cell;
		List<Grid_Soil_Lyr> Lyrs = new ArrayList<Soils.Grid_Soil_Lyr>();
		public int getNLyrs() {
			return Lyrs.size();
		}
		public String toString() {
			String lines = "";
			for(int i=0; i<Lyrs.size(); i++) {
				lines += Integer.toString(cell)+","+Integer.toString(0)+","+Integer.toString(0)+","+Lyrs.get(i).toString()+"\n";	
			}
			return lines;
		}
		@Override
		public int compareTo(Grid_Soil_Lyrs lyrs) {
			return this.cell - lyrs.cell;
		}
	}
	List<Grid_Soil_Lyrs> Grid_Soils = new ArrayList<Grid_Soil_Lyrs>();
	
	public void read(Path SoilsFile) throws Exception {
		List<String> lines = java.nio.file.Files.readAllLines(SoilsFile, StandardCharsets.UTF_8);
		LogFileIn f = stepwat.LogFileIn.getInstance();
		int nFileItemsRead = 0;
		int depthMin = 0;
		int lastCell = -1;
		for (String line : lines) {
			// Skip Comments and empty lines
			if (!line.matches("^\\s*#.*") && !line.matches("^[\\s]*$")) {
				line = line.trim();
				String[] values = line.split(",");//split at commas
				switch (nFileItemsRead) {
				case 0:
					//"cell","copy_cell","copy_which","layer_num","depth","bulkd","fieldc","wiltpt","evco","trco_grass","trco_shrub","trco_tree","sand","clay","imperm","soiltemp"
					if(values.length != 16)
						f.LogError(LogFileIn.LogMode.ERROR, "grid_soils.csv onRead : Illegal Formatted Row : Expected 16 Values read "+String.valueOf(values.length));
					if(values[0] != "cell")
						f.LogError(LogFileIn.LogMode.ERROR, "grid_soils.csv onRead : Header Column Illegal Header : Expected 'cell' read "+values[0]);
					if(values[1] != "copy_cell")
						f.LogError(LogFileIn.LogMode.ERROR, "grid_soils.csv onRead : Header Column Illegal Header : Expected 'copy_cell' read "+values[1]);
					if(values[2] != "copy_which")
						f.LogError(LogFileIn.LogMode.ERROR, "grid_soils.csv onRead : Header Column Illegal Header : Expected 'copy_which' read "+values[2]);
					if(values[3] != "layer_num")
						f.LogError(LogFileIn.LogMode.ERROR, "grid_soils.csv onRead : Header Column Illegal Header : Expected 'layer_num' read "+values[3]);
					if(values[4] != "depth")
						f.LogError(LogFileIn.LogMode.ERROR, "grid_soils.csv onRead : Header Column Illegal Header : Expected 'depth' read "+values[4]);
					if(values[5] != "bulkd")
						f.LogError(LogFileIn.LogMode.ERROR, "grid_soils.csv onRead : Header Column Illegal Header : Expected 'bulkd' read "+values[5]);
					if(values[6] != "fieldc")
						f.LogError(LogFileIn.LogMode.ERROR, "grid_soils.csv onRead : Header Column Illegal Header : Expected 'fieldc' read "+values[6]);
					if(values[7] != "wiltpt")
						f.LogError(LogFileIn.LogMode.ERROR, "grid_soils.csv onRead : Header Column Illegal Header : Expected 'wiltpt' read "+values[7]);
					if(values[8] != "evco")
						f.LogError(LogFileIn.LogMode.ERROR, "grid_soils.csv onRead : Header Column Illegal Header : Expected 'evco' read "+values[8]);
					if(values[9] != "trco_grass")
						f.LogError(LogFileIn.LogMode.ERROR, "grid_soils.csv onRead : Header Column Illegal Header : Expected 'trco_grass' read "+values[9]);
					if(values[10] != "trco_shrub")
						f.LogError(LogFileIn.LogMode.ERROR, "grid_soils.csv onRead : Header Column Illegal Header : Expected 'trco_shrub' read "+values[10]);
					if(values[11] != "trco_tree")
						f.LogError(LogFileIn.LogMode.ERROR, "grid_soils.csv onRead : Header Column Illegal Header : Expected 'trco_tree' read "+values[11]);
					if(values[12] != "sand")
						f.LogError(LogFileIn.LogMode.ERROR, "grid_soils.csv onRead : Header Column Illegal Header : Expected 'sand' read "+values[12]);
					if(values[13] != "clay")
						f.LogError(LogFileIn.LogMode.ERROR, "grid_soils.csv onRead : Header Column Illegal Header : Expected 'clay' read "+values[13]);
					if(values[14] != "imperm")
						f.LogError(LogFileIn.LogMode.ERROR, "grid_soils.csv onRead : Header Column Illegal Header : Expected 'imperm' read "+values[14]);
					if(values[15] != "soiltemp")
						f.LogError(LogFileIn.LogMode.ERROR, "grid_soils.csv onRead : Header Column Illegal Header : Expected 'soiltemp' read "+values[15]);
					break;
				default:
					int cell=0, copy=0;
					Grid_Soil_Lyrs lyrs;
					try {
						cell = Integer.parseInt(values[0]);
						copy = Integer.parseInt(values[1]);
					} catch(NumberFormatException e) {
						f.LogError(LogFileIn.LogMode.ERROR, "grid_soils.csv onRead : cell or copy_option bad : Could not convert string to integer. In row "+ Integer.toString(cell)+"." + e.getMessage());
					}
					if(values.length != 16 && copy!=1)
						f.LogError(LogFileIn.LogMode.ERROR, "grid_soils.csv onRead : Illegal Formatted Row : Expected 16 Values read "+String.valueOf(values.length));
					try {
						if(cell != lastCell) {
							lyrs = new Grid_Soil_Lyrs();
							lyrs.cell = cell;
							Grid_Soils.add(lyrs);
						} else {
							lyrs = Grid_Soils.get(Grid_Soils.size()-1);
						}
						if(copy==1) {
							int copyCell = Integer.parseInt(values[2]);
							//with a little work you could copy any cell
							if(copyCell < cell && copyCell >= 0) {
								boolean found = false;
								for(int i=0; i<Grid_Soils.size(); i++) {
									if(Grid_Soils.get(i).cell == copyCell) {
										Grid_Soil_Lyrs temp = new Grid_Soil_Lyrs();
										temp.cell = cell;
										temp.Lyrs = Grid_Soils.get(i).Lyrs;
										found = true;
									}
								}
								if(!found)
									f.LogError(LogFileIn.LogMode.ERROR, "grid_soils.csv onRead : Copy Cell : Could find cell to copy ( "+String.valueOf(copyCell) + ").");
							} else {
								f.LogError(LogFileIn.LogMode.ERROR, "grid_soils.csv onRead : Copy Cell : Bad Value");
							}
							//int copyLayer = Integer.parseInt(values[3]);
						} else {
							Grid_Soil_Lyr row = new Grid_Soil_Lyr();
							row.layer_num = Integer.parseInt(values[3]);
							if(row.layer_num > MAX_LAYERS)
								f.LogError(LogFileIn.LogMode.ERROR, "grid_soils.csv onRead : Max Layers exceeded.");
							row.depth = Integer.parseInt(values[4]);
							row.bulkd = Float.parseFloat(values[5]);
							row.fieldc = Float.parseFloat(values[6]);
							row.wiltpt = Float.parseFloat(values[7]);
							row.evco = Float.parseFloat(values[8]);
							row.trco_grass = Float.parseFloat(values[9]);
							row.trco_shrub = Float.parseFloat(values[10]);
							row.trco_tree = Float.parseFloat(values[11]);
							row.sand = Float.parseFloat(values[12]);
							row.clay = Float.parseFloat(values[13]);
							row.imperm = Float.parseFloat(values[14]);
							row.soiltemp = Float.parseFloat(values[15]);
							lyrs.Lyrs.add(row);
						}
					} catch(NumberFormatException e) {
						f.LogError(LogFileIn.LogMode.ERROR, "grid_soils.csv onRead : Number format wrong : Could not convert string to number. In row "+ Integer.toString(cell)+"." + e.getMessage());
					}
					break;
				}
				nFileItemsRead++;
			}
			this.data = true;
		}
		//Now sort the cells
		Collections.sort(Grid_Soils);
		//Now sort the layers in each cell and calc width
		for(int i=0; i<Grid_Soils.size(); i++) {
			Collections.sort(Grid_Soils.get(i).Lyrs);
			depthMin = 0;
			for(int j=0; j<Grid_Soils.get(i).Lyrs.size(); j++) {
				Grid_Soils.get(i).Lyrs.get(j).width = Grid_Soils.get(i).Lyrs.get(j).depth-depthMin;
				depthMin = Grid_Soils.get(i).Lyrs.get(j).depth;
			}
		}
	}
	
	public void write(Path SoilsFile) throws IOException {
		List<String> lines = new ArrayList<String>();
		
		lines.add("cell,copy_cell,copy_which,layer_num,depth,bulkd,fieldc,wiltpt,evco,trco_grass,trco_shrub,trco_tree,sand,clay,imperm,soiltemp");
		for(int i=0; i<Grid_Soils.size(); i++) {
			lines.add(Grid_Soils.get(i).toString());
		}
		Files.write(SoilsFile, lines, StandardCharsets.UTF_8);
	}
}
