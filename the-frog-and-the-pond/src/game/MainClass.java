package game;

public class MainClass {
	public static void main(String args[]) throws Exception {

		int x = 8;
		int y = 8;
		int num_frogs = 1;

		Point[] cars = new Point[16];
		Point[] free_points = new Point[2];

		cars[0] = new Point(2, 0);
		cars[1] = new Point(2, 1);
		cars[2] = new Point(2, 2);
		cars[3] = new Point(2, 3);
		cars[4] = new Point(2, 4);
		cars[5] = new Point(2, 5);
		cars[6] = new Point(2, 6);
		cars[7] = new Point(2, 7);
		cars[8] = new Point(5, 0);
		cars[9] = new Point(5, 1);
		cars[10] = new Point(5, 2);
		cars[11] = new Point(5, 3);
		cars[12] = new Point(5, 4);
		cars[13] = new Point(5, 5);
		cars[14] = new Point(5, 6);
		cars[15] = new Point(5, 7);
		
		//TODO: You should decide how to handle with the logic of the free points
		free_points[0] = new Point(2,4);
		free_points[1] = new Point(5,6);

		Point[] goals = new Point[num_frogs];

		goals[0] = new Point(7, 7);

		ControlPanel cp;
		String path = "out/jit";

		System.out.println("Running the system");
		cp = new ControlPanel(x, y, num_frogs, cars, free_points, goals, path);
		cp.init();

	}
}
