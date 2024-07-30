package game;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.swing.JButton;
import javax.swing.JFrame;

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
	int num_obstacles;
	Point[] robots;
	Point[] obstacles;
	Point[] goals;
	boolean[] isTargetBlocked;
	int state = 0;
	boolean engine_problem;
	boolean A_is_blocked, B_is_blocked, C_is_blocked;
	Random rand_block = new Random();
	Random rand_position = new Random();
	int position_type, moving_obstacle_x, current_moving_obstacle_x, updated_moving_obstacle_x;

	// holds the robots previous position (for use when animating transitions)
	Point[] robots_prev = new Point[num_robots];
		
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
	

	public ControlPanel(int x, int y, int num_robots, int num_targets, Point[] goals, Point[] obstacles,
			String path) {
		this.x = x;
		this.y = y;
		this.num_robots = num_robots;
		this.num_targets = num_targets;
		this.num_obstacles = obstacles.length;
		this.robots = new Point[num_robots];
		this.robots_prev = new Point[num_robots];
		this.obstacles = obstacles;
		this.goals = goals;
		this.engine_problem = false;
		this.path = path;
		this.isTargetBlocked = new boolean[num_targets];
		this.moving_obstacle_x = 0;
		
	}
	
	//Make sure that the robot doesn't exceed the grid boundries
		public static int check_boundries(int pos) {
			if (pos < 0) {
				return 0;
			}
			if (pos > 4) {
				return 4;
			}
			return pos;
		}

	public void init() throws Exception {
		autorun = false;

		for (int i = 0; i < num_robots; i++) {
			robots[i] = new Point();
			robots_prev[i] = new Point();
		}
		
		
		// init controller
		executor = new ControllerExecutor(new BasicJitController(), this.path, "Patrolling");
		//TODO: initial input values using input.put(...)
		
		this.moving_obstacle_x = 0;
		
		for (int i = 0; i < num_targets; i++) {
			isTargetBlocked[i] = rand_block.nextBoolean();
				
		}
		

		inputs.put("A_X", Integer.toString(goals[0].getX()));
        inputs.put("A_Y", Integer.toString(goals[0].getY()));
        inputs.put("B_X", Integer.toString(goals[1].getX()));
        inputs.put("B_Y", Integer.toString(goals[1].getY()));
        inputs.put("C_X", Integer.toString(goals[2].getX()));
        inputs.put("C_Y", Integer.toString(goals[2].getY()));
        inputs.put("movingObstacle_X", Integer.toString(moving_obstacle_x));
        inputs.put("BlockedA", Boolean.toString(isTargetBlocked[0]));
		inputs.put("BlockedB", Boolean.toString(isTargetBlocked[1]));
		inputs.put("BlockedC", Boolean.toString(isTargetBlocked[2]));
        
		executor.initState(inputs);

		Map<String, String> sysValues = executor.getCurrOutputs();
		
		// set initial robot locations
		//TODO: you may initial other things in a similar way
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
		ready_for_next = false;
		state += 1;
		advance_button.setText("...");
		for (int i = 0; i < num_robots; i++) {
			robots_prev[i].setX(robots[i].getX());
			robots_prev[i].setY(robots[i].getY());
		}
		
		//TODO: put here your ramdom env inputs
		for (int i = 0; i < num_targets; i++) {
			
			if (robots[0].getX() == goals[i].getX() && robots[0].getY() == goals[i].getY()) {
				isTargetBlocked[i] = false;
			}
			else {
				isTargetBlocked[i] = rand_block.nextBoolean();
			}	
		}
		
		inputs.put("BlockedA", Boolean.toString(isTargetBlocked[0]));
		inputs.put("BlockedB", Boolean.toString(isTargetBlocked[1]));
		inputs.put("BlockedC", Boolean.toString(isTargetBlocked[2]));		

		//choose randomly the next position
		position_type = rand_position.nextInt(3) - 1; 
		current_moving_obstacle_x = Integer.parseInt(inputs.get("movingObstacle_X"));
		//check that the moving obstacle doesn't exceed the grid boundries
		updated_moving_obstacle_x = check_boundries(current_moving_obstacle_x + position_type);
		this.moving_obstacle_x = updated_moving_obstacle_x;
		inputs.put("movingObstacle_X", Integer.toString(updated_moving_obstacle_x));
		
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
		frame.setVisible(true);
		board.setVisible(true);
		advance_button.setVisible(true);
		autorun_button.setVisible(true);
		ready_for_next = true;
	}

}
