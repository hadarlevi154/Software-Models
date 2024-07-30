package game;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.WindowConstants;

import tau.smlab.syntech.controller.executor.ControllerExecutor;
import tau.smlab.syntech.games.controller.jits.BasicJitController;

/*
 * Manages the simulation - GUI, controller input/output, board (visualization)
 */

public class ControlPanel {
	// board dimensions
	int x;
	int y;
	// board constants
	final int dim = 130;
	static final int y_offset = 30;

	int num_robots;
	int num_targets;
	
	Point guardLeftCorner;

	Point[] robots;
	Point[] fixedObstacles;
	Point[] targets;

	int state = 0;
	int prevState = 0;

	// holds the robots previous position (for use when animating transitions)
	Point[] robots_prev = new Point[num_robots];
	Point guard_ini_prev;

	// Board and GUI elements
	JFrame frame;
	Board board;
	JButton advance_button;
	JButton autorun_button;

	// holds states for the animation
	boolean ready_for_next;
	boolean autorun;


	// The controller and its inputs
	ControllerExecutor executor;
	Map<String, String> inputs = new HashMap<String, String>();

	// The path to the controller files
	String path;
	


	public ControlPanel(int x, int y, int num_robots, int num_targets, String path) {
		this.x = x;
		this.y = y;
		this.num_robots = num_robots;
		this.num_targets = num_targets;
		this.guardLeftCorner = new Point(0,0);
		this.robots = new Point[num_robots];
		this.robots_prev = new Point[num_robots];
		this.fixedObstacles = new Point[6];
		this.targets = new Point[num_targets];
		this.path = path;
	}

	public void init() throws Exception {
		autorun = false;

		for (int i = 0; i < num_robots; i++) {
			robots[i] = new Point();
			robots_prev[i] = new Point();
		}

		// init controller
		executor = new ControllerExecutor(new BasicJitController(), this.path, "EscapingGuard");

		// TODO: initial input values using inputs.put(...)

		// set initial robot locations
		// TODO: you may initial other things in a similar way

		this.fixedObstacles[0] = new Point(2, 2);
		this.fixedObstacles[1] = new Point(2, 3);
		this.fixedObstacles[2] = new Point(2, 4);
		this.fixedObstacles[3] = new Point(2, 5);
		this.fixedObstacles[4] = new Point(6, 2);
		this.fixedObstacles[5] = new Point(6, 3);
		
		//targets locations 
		this.targets[0] = new Point(0, 0);
		this.targets[1] = new Point(0, 7);
		this.targets[2] = new Point(7, 7);
		
		
		this.guard_ini_prev = new Point(0,0);
		inputs.put("movingGuardLeft_X", Integer.toString(this.guardLeftCorner.x));
		inputs.put("movingGuardTop_Y", Integer.toString(this.guardLeftCorner.y));
		inputs.put("robotMovesCounter", Integer.toString(this.state));
	
		executor.initState(inputs);
		
		Map<String, String> sysValues = executor.getCurrOutputs();

		for (int i = 0; i < num_robots; i++) {
			robots_prev[i].setX(Integer.parseInt(sysValues.get("robotX")));
			robots_prev[i].setY(Integer.parseInt(sysValues.get("robotY")));
			robots[i].setX(Integer.parseInt(sysValues.get("robotX")));
			robots[i].setY(Integer.parseInt(sysValues.get("robotY")));
		}
		
		
		
		setUpUI();
	}

	// handle next turn
	void next() throws Exception {
//		shouldGuardMove = !shouldGuardMove;
		ready_for_next = false;

		prevState = state;
		
		if(state == 2) {
			state = 0;
		}
		else {
			state += 1;
		}
		
		advance_button.setText("...");
		for (int i = 0; i < num_robots; i++) {
			robots_prev[i].setX(robots[i].getX());
			robots_prev[i].setY(robots[i].getY());
		}
		
		// TODO: put here your env inputs before you update the state
		
		//meaning the guard can move
		if(prevState == 0) { 
			
			//if the robot is on the right, the guard should move right
			if ((robots[0].getX() > this.guardLeftCorner.getX() + 1 && robots[0].getY() == this.guardLeftCorner.getY()) || (robots[0].getX() > this.guardLeftCorner.getX() + 1 && robots[0].getY() == this.guardLeftCorner.getY() + 1)) {
				this.guardLeftCorner.x += 1;
			}
			
			else if ((robots[0].getX() < this.guardLeftCorner.getX() && robots[0].getY() == this.guardLeftCorner.getY()) || (robots[0].getX() < this.guardLeftCorner.getX() && robots[0].getY() == this.guardLeftCorner.getY() + 1)) {
				this.guardLeftCorner.x -= 1;
			}
			
			//if the robot is up, the guard should move up
			else if ((robots[0].getX() == this.guardLeftCorner.getX() && robots[0].getY() <= this.guardLeftCorner.getY() - 1) || (robots[0].getX() == this.guardLeftCorner.getX() + 1 && robots[0].getY() <= this.guardLeftCorner.getY() - 1)) {
				this.guardLeftCorner.y -= 1;
			}
			
			//if the robot is down, the guard should move down
			else if ((robots[0].getX() == this.guardLeftCorner.getX() && robots[0].getY() >= this.guardLeftCorner.getY() + 1) || (robots[0].getX() == this.guardLeftCorner.getX() + 1 && robots[0].getY() >= this.guardLeftCorner.getY() + 1)) {
				this.guardLeftCorner.y += 1;
			}
			
			// If robot is to the right and up, the guard will diagonally move right and up
			else if (robots[0].getX() > this.guardLeftCorner.getX() + 1 && robots[0].getY() < this.guardLeftCorner.getY()) {
				this.guardLeftCorner.x += 1;
				this.guardLeftCorner.y -= 1;
			}
			
			// If robot is to the left and up, the guard will diagonally move left and up
			else if (robots[0].getX() < this.guardLeftCorner.getX() && robots[0].getY() < this.guardLeftCorner.getY()) {
				this.guardLeftCorner.x -= 1;
				this.guardLeftCorner.y -= 1;
			}
			
			// If robot is to the right and down, the guard will diagonally move right and down
			else if (robots[0].getX() > this.guardLeftCorner.getX() + 1 && robots[0].getY() > this.guardLeftCorner.getY()) {
				this.guardLeftCorner.x += 1;
				this.guardLeftCorner.y += 1;
			}
			
			// If robot is to the left and down, the guard will diagonally move left and down
			else if (robots[0].getX() < this.guardLeftCorner.getX() && robots[0].getY() > this.guardLeftCorner.getY()) {
				this.guardLeftCorner.x -= 1;
				this.guardLeftCorner.y += 1;
				
			}
		}
		
		inputs.put("movingGuardLeft_X", Integer.toString(this.guardLeftCorner.x));
		inputs.put("movingGuardTop_Y", Integer.toString(this.guardLeftCorner.y));
		inputs.put("robotMovesCounter", Integer.toString(this.state));
		
		executor.updateState(inputs);		

		// Receive updated values from the controller
		Map<String, String> sysValues = executor.getCurrOutputs();

		// Update robot locations
		for (int i = 0; i < num_robots; i++) {
			robots[i].setX(Integer.parseInt(sysValues.get("robotX")));
			robots[i].setY(Integer.parseInt(sysValues.get("robotY")));
		}

		// Animate transition
		board.animate();
	}

	void setUpUI() throws Exception {
		advance_button = new JButton();
		autorun_button = new JButton();
		frame = new JFrame();
		frame.add(advance_button);
		frame.add(autorun_button);
		board = new Board(this);
		board.init();
		frame.setTitle("Robots");
		frame.setSize(x * dim + 8 + 150, y * dim + y_offset + 8);
		board.setSize(x * dim, y * dim);
		frame.setLayout(null);
		frame.add(board, BorderLayout.CENTER);

		// Handle presses of the "next step" button
		advance_button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				try {
					if (ready_for_next && !autorun)
						next();
				} catch (Exception e) {
					System.out.println(e);
				}
			}
		});
		advance_button.setBounds(x * dim + 8, 0, 130, 50);
		advance_button.setText("Start");

		// Handle presses of the "autorun/stop autorun" button
		autorun_button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				try {
					if (autorun) {
						autorun = false;
						autorun_button.setText("Auto run");
					} else if (ready_for_next) {
						autorun = true;
						autorun_button.setText("Stop Auto run");
						next();
					}
				} catch (Exception e) {
					System.out.println(e);
				}
			}
		});

		autorun_button.setBounds(x * dim + 8, 50, 130, 50);
		autorun_button.setText("Auto run");
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.setVisible(true);
		board.setVisible(true);
		advance_button.setVisible(true);
		autorun_button.setVisible(true);
		ready_for_next = true;
	}
}
