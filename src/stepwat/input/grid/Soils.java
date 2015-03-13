package stepwat.input.grid;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import stepwat.LogFileIn;
import stepwat.input.Input;
import soilwat.InputData.SoilsIn;
import soilwat.SW_SOILS.SOILS_INPUT_DATA;
import stepwat.input.SXW.Roots;

public class Soils extends Input {

	public static final int MAX_LAYERS = 25;
	
	SoilsIn[] Grid_Soils = null;
	Roots[] Grid_Roots = null;
	
	
	public void read(Path SoilsFile) throws Exception {
		List<String> lines = java.nio.file.Files.readAllLines(SoilsFile, StandardCharsets.UTF_8);
		LogFileIn f = stepwat.LogFileIn.getInstance();
		Grid_Soils = new SoilsIn[lines.size()-1];
		Grid_Roots = new Roots[lines.size()-1];
		int nFileItemsRead = 0;

		for (String line : lines) {
			// Skip Comments and empty lines
			if (!line.matches("^\\s*#.*") && !line.matches("^[\\s]*$")) {
				line = line.trim();
				String[] values = line.split(",");//split at commas
				switch (nFileItemsRead) {
				case 0:
					break;
				default:
					boolean copy = false;
					int cell=0, copy_which=0;
					String swx_roots = values[4];
					
					SoilsIn soils;
					try {
						cell = Integer.parseInt(values[0]);
						copy = Integer.parseInt(values[1])==0?false:true;
					} catch(NumberFormatException e) {
						f.LogError(LogFileIn.LogMode.ERROR, "grid_soils.csv onRead : cell or copy_option bad : Could not convert string to integer. In row "+ Integer.toString(cell)+"." + e.getMessage());
					}
					try {						
						if(copy) {
							copy_which = Integer.parseInt(values[2]);
							//with a little work you could copy any cell
							if(copy_which < cell && copy_which >= 0) {
								boolean found = false;
								if(Grid_Soils[copy_which] == null)
									f.LogError(LogFileIn.LogMode.ERROR, "grid_soils.csv onRead : Copy Cell : Tried to copy a cell's soils that is not set. Cell "+Integer.toString(cell)+" Copy "+Integer.toString(copy_which));
								if(Grid_Roots[copy_which] == null)
									f.LogError(LogFileIn.LogMode.ERROR, "grid_soils.csv onRead : Copy Cell : Tried to copy a cell's roots that is not set. Cell "+Integer.toString(cell)+" Copy "+Integer.toString(copy_which));
								Grid_Soils[cell] = Grid_Soils[copy_which];//shallow copy
								Grid_Roots[cell] = Grid_Roots[copy_which];//shallow copy
								if(!found)
									f.LogError(LogFileIn.LogMode.ERROR, "grid_soils.csv onRead : Copy Cell : Could find cell to copy ( "+String.valueOf(copy_which) + ").");
							} else {
								f.LogError(LogFileIn.LogMode.ERROR, "grid_soils.csv onRead : Copy Cell : Bad Value");
							}
							//int copyLayer = Integer.parseInt(values[3]);
						} else {
							soils = new SoilsIn(null);
							soils.nLayers = Integer.parseInt(values[3]);
							if(soils.nLayers > MAX_LAYERS)
								f.LogError(LogFileIn.LogMode.ERROR, "grid_soils.csv onRead : Max Layers exceeded.");
							if(values.length < (5+12*soils.nLayers))
								f.LogError(LogFileIn.LogMode.ERROR, "grid_soils.csv onRead : Not enough columns to read all layers. Cell " + Integer.toString(cell));
							for(int i=0; i<soils.nLayers; i++) {
								soils.layers[i].depth = Double.parseDouble(values[5+12*i+0]);
								soils.layers[i].soilMatric_density = Double.parseDouble(values[5+12*i+1]);
								soils.layers[i].fractionVolBulk_gravel = Double.parseDouble(values[5+12*i+2]);
								soils.layers[i].evap_coeff = Double.parseDouble(values[5+12*i+3]);
								soils.layers[i].transp_coeff_grass = Double.parseDouble(values[5+12*i+4]);
								soils.layers[i].transp_coeff_shrub = Double.parseDouble(values[5+12*i+5]);
								soils.layers[i].transp_coeff_tree = Double.parseDouble(values[5+12*i+6]);
								soils.layers[i].transp_coeff_forb = Double.parseDouble(values[5+12*i+7]);
								soils.layers[i].fractionWeightMatric_sand = Double.parseDouble(values[5+12*i+8]);
								soils.layers[i].fractionWeightMatric_clay = Double.parseDouble(values[5+12*i+9]);
								soils.layers[i].impermeability = Double.parseDouble(values[5+12*i+10]);
								soils.layers[i].sTemp = Double.parseDouble(values[5+12*i+11]);
							}
							Roots r = new Roots();
							r.read(Paths.get(SoilsFile.getParent().toString(),swx_roots));
							this.Grid_Roots[cell] = r;
							this.Grid_Soils[cell] = soils;
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
	}
	
	private String header() {
		String line = "";
		String layer = "zz_depth,zz_matricd,zz_gravel_content,zz_evco,zz_trco_grass,zz_trco_shrub,zz_trco_tree,zz_trco_forb,zz_sand,z_clay,zz_imperm,zz_soiltemp";
		List<Integer> lengths = new ArrayList<Integer>();
		for(int i=0; i<Grid_Soils.length; i++) {
			lengths.add(Grid_Soils[i].nLayers);
		}
		int maxLayers = Collections.max(lengths);
		line += "cell,copy_cell,copy_which,layer_num,sxw_roots";
		for(int i=0; i<maxLayers; i++) {
			line += ","+layer.replaceAll("zz", String.valueOf(i+1));
		}
		return line;
	}
	
	private String soilsToString(SoilsIn s) {
		String line = "";
		for (SOILS_INPUT_DATA l : s.layers) {
			line += ","+String.format("%3.0f,%f,%f,%f,%f,%f,%f,%f,%f,%f,%f,%f", l.depth,l.soilMatric_density,l.fractionVolBulk_gravel,l.evap_coeff,l.transp_coeff_grass,l.transp_coeff_shrub,l.transp_coeff_tree,l.transp_coeff_forb,l.fractionWeightMatric_sand,l.fractionWeightMatric_clay,l.impermeability,l.sTemp);
		}
		return line;
	}
	
	public void write(Path SoilsFile) throws IOException {
		List<String> lines = new ArrayList<String>();
		
		lines.add(header());
		Set<SoilsIn> hsoil = new HashSet<SoilsIn>(Grid_Soils.length);
		for(int i=0; i<Grid_Soils.length; i++) {
			String line = String.valueOf(i);
			if(hsoil.contains(Grid_Soils[i])) {
				line += ",1,"+String.valueOf(java.util.Arrays.asList(Grid_Soils).indexOf(Grid_Soils[i]))+",0";
			} else {
				line += ",0,0,"+String.valueOf(Grid_Soils[i].nLayers)+","+Grid_Roots[i].fileName+","+soilsToString(Grid_Soils[i]);
				hsoil.add(Grid_Soils[i]);
			}
			lines.add(line);
		}
		Files.write(SoilsFile, lines, StandardCharsets.UTF_8);
	}
}
