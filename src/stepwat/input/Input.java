package stepwat.input;

import java.io.IOException;
import java.nio.file.Path;
/***
 * This Class is an abstract class.
 * It is a base class for all input files.
 * If you have code that would apply to all input file objects, use this.
 * @author Ryan Murphy
 *
 */
public abstract class Input {
	//This is set when the object is set with data
	protected boolean data = false;
	
	public abstract void read(Path file) throws Exception;
	public abstract void write(Path file) throws IOException;
	
	public boolean data() {
		return data;
	}
}
