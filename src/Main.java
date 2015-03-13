import stepwat.Control;

public class Main {
	public static void main(String[] args) {
		try {
			Control stepwat = new Control(args);
			stepwat.run();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
