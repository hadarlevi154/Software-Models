package game;

public class MainClass {
	public static void main(String args[]) throws Exception {

		// TODO: we provide here some optional code for generating **some** of the
		// initial env elements, please modify and complete according to your needs
		// You may create more elements and pass them to the control panel.

		int x = 8;
		int y = 8;
		int num_robots = 1;
		int num_targets = 3;
	
		ControlPanel cp;
		String path = "out/jit";

		System.out.println("Running the system");
		cp = new ControlPanel(x, y, num_robots, num_targets, path);
		cp.init();

	}
}
