package stepwat.input.SXW;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import stepwat.LogFileIn;
import stepwat.input.Input;
import stepwat.input.ST.Rgroup;

public class Roots extends Input {
	
	public String fileName = "";
	public List<GrpLayerInfo> listGrps = new ArrayList<Roots.GrpLayerInfo>();
	
	public class GrpLayerInfo {
		public String grpName;
		public float[] lyrDist;
		
		public GrpLayerInfo(String name, int lyrs) {
			this.grpName = name;
			lyrDist = new float[lyrs];
		}
		
		public float sumLayer() {
			float sum=0;
			for(int i=0; i<lyrDist.length; i++) {
				sum += lyrDist[i];
			}
			return sum;
		}
		
		public boolean sumToOne() {
			boolean correct = false;
			float sum = sumLayer();
			if(Float.compare(sum, 0.0f) < 0)
				correct = false;
			if(Float.compare(sum, 0.0f) > 0) {
				if(Float.compare(sum, 1.0f) == 0) {
					correct = true;
				} else {
					for(int i=0; i<lyrDist.length; i++) {
						lyrDist[i] /= sum;
					}
				}
			}
			
			return correct;
		}
	}

	@Override
	public void read(Path file) throws Exception {
		List<String> lines = java.nio.file.Files.readAllLines(file, StandardCharsets.UTF_8);
		fileName = file.getFileName().toString();
		LogFileIn f = stepwat.LogFileIn.getInstance();
		int nFileItemsRead = 0;
		for (String line : lines) {
			// Skip Comments and empty lines
			if (!line.matches("^\\s*#.*") && !line.matches("^[\\s]*$")) {
				line = line.trim();
				String[] values = line.split("#")[0].split("[ \t]+");// Remove comment  after data
				switch (nFileItemsRead) {
				default:
					String name = values[0];				
					int layers = values.length-1;
					GrpLayerInfo newLayer = new GrpLayerInfo(name, layers);
					
					try {
						for(int i=0; i<layers; i++) {
							newLayer.lyrDist[i] = Float.valueOf(values[i+1]);
						}
					} catch(NumberFormatException e) {
						f.LogError(LogFileIn.LogMode.ERROR, "sxwroots.in read : Could not convert layer distribution value.");
					}
					
					listGrps.add(newLayer);
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
		int maxStringLength = getMaxStringLength()+3;
		lines.add("# Rooting distributions for STEPWAT");
		lines.add("# Distributions must sum to one for each group");
		lines.add("# Be sure these data are consistent with the SOILWAT");
		lines.add("# layer definitions for transpiration (i.e. number of layers");
		lines.add("# that roots occupy must match the number of soil layers where");
		lines.add("# transpiration occurs in soils_v23.in)");
		lines.add("");
		lines.add("#GrpName" + getSpacing(maxStringLength-8) + getLayers());
		for(GrpLayerInfo gli : listGrps) {
			String line = gli.grpName + getSpacing(maxStringLength-gli.grpName.length());
			for(int i=0; i<gli.lyrDist.length; i++) {
				line += String.format("%-4.3f", gli.lyrDist[i])+"  ";
			}
			lines.add(line);
		}
		java.nio.file.Files.write(file, lines, StandardCharsets.UTF_8);
	}
	
	public boolean verify(Rgroup grps) throws Exception {
		LogFileIn f = stepwat.LogFileIn.getInstance();
		for(GrpLayerInfo gli : listGrps) {
			if(grps.ResourceParams_Name2Index(gli.grpName) == -1) {
				f.LogError(LogFileIn.LogMode.NOTE, "sxwroots.in verify : " + gli.grpName + " not found in Resource Groups.");
				return false;
			}
			if(gli.sumToOne() != true) {
				f.LogError(LogFileIn.LogMode.NOTE, "sxwroots.in verify : " + gli.grpName + " root distribution could not normalize to 1.");
				return false;
			}
		}
		if(grps.groups.size() != listGrps.size()) {
			f.LogError(LogFileIn.LogMode.NOTE, "sxwroots.in verify : To few or too many resource groups.");
			return false;
		}
		
		return true;
	}
	
	private int getMaxLayers() {
		List<Integer> lengths = new ArrayList<Integer>();
		for(GrpLayerInfo gli : listGrps) {
			lengths.add(gli.lyrDist.length);
		}
		return Collections.max(lengths);
	}
	
	private int getMaxStringLength() {
		List<Integer> lengths = new ArrayList<Integer>();
		for(GrpLayerInfo gli : listGrps) {
			lengths.add(gli.grpName.length());
		}
		return Collections.max(lengths);
	}
	
	private String getSpacing(int length) {
		return new String(new char[length]).replace("\0", " ");
	}
	
	private String getLayers() {
		String Layers = "";
		for(int i=0; i<getMaxLayers(); i++) {
			Layers += "%L"+String.valueOf(i+1)+"   ";
		}
		return Layers;
	}
}
