package game;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;
import javax.swing.JPanel;
import javax.swing.Timer;

@SuppressWarnings("serial")
// Responsible for visualization/animation of transitions
public class Board extends JPanel {

	int animation_steps = 0;
	Timer timer;
	ControlPanel cp;

	// The current, start, and target locations for a single transition
	// - used for animation
	Point[] frog_graphics;
	Point[] start_graphics;
	Point[] target_graphics;

	BufferedImage buffer;
	BufferedImage[] frog_images;
	BufferedImage[] goals_images;
	BufferedImage[] base_robot_images;
	BufferedImage car_image;

	public Board(ControlPanel cp) {
		super();
		this.cp = cp;
		frog_graphics = new Point[cp.num_frogs];
		start_graphics = new Point[cp.num_frogs];
		target_graphics = new Point[cp.num_frogs];
		frog_images = new BufferedImage[cp.num_frogs];
		base_robot_images = new BufferedImage[cp.num_frogs];
		goals_images = new BufferedImage[cp.num_frogs];
	}

	public void init() throws Exception {
		for (int i = 0; i < cp.num_frogs; i++) {
			start_graphics[i] = new Point();
			target_graphics[i] = new Point();
			frog_graphics[i] = new Point();
		}

		car_image = ImageIO.read(new File("img/car.png"));

		// Load images for different elements
		for (int i = 0; i < cp.num_frogs; i++) {
			base_robot_images[i] = ImageIO.read(new File("img/frog.png"));
			frog_images[i] = ImageIO.read(new File("img/frog.png"));
			goals_images[i] = ImageIO.read(new File("img/pond.png"));
		}
	}

	// Animate a transition
	public void animate() throws Exception {
		for (int i = 0; i < cp.num_frogs; i++) {
			frog_graphics[i].setX(cp.frog_prev[i].getX() * cp.dim);
			frog_graphics[i].setY(cp.frog_prev[i].getY() * cp.dim);
			start_graphics[i].setX(cp.frog_prev[i].getX() * cp.dim);
			start_graphics[i].setY(cp.frog_prev[i].getY() * cp.dim);
			target_graphics[i].setX(cp.frog[i].getX() * cp.dim);
			target_graphics[i].setY(cp.frog[i].getY() * cp.dim);
		}
		// Each tick of the timer advances the animation
		timer = new Timer(16, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int num_steps = 20;
				if (animation_steps > num_steps)
				// Animation ended
				{
					timer.stop();
					animation_steps = 0;
					cp.ready_for_next = true;
					if (cp.autorun) {
						try {
							cp.next();
						} catch (Exception ex) {
							System.out.println(ex);
						}
					} else {
						cp.advance_button.setText("Next step");
					}
					return;
				}
				// Update robot location for current animation step
				for (int i = 0; i < cp.num_frogs; i++) {
					frog_graphics[i].setX((int) (start_graphics[i].getX() * (1 - (double) (animation_steps) / num_steps)
							+ target_graphics[i].getX() * ((double) (animation_steps) / num_steps)));
					frog_graphics[i].setY((int) (start_graphics[i].getY() * (1 - (double) (animation_steps) / num_steps)
							+ target_graphics[i].getY() * ((double) (animation_steps) / num_steps)));
				}
				animation_steps++;
				// Redraw
				updateBuffer();
				repaint();
			}
		});

		timer.start();
	}

	@Override
	public void invalidate() {
		buffer = null;
		// updateBuffer();
		super.invalidate();
	}

	// Use buffering for smooth animations
	protected void updateBuffer() {
		if (getWidth() > 0 && getHeight() > 0) {

			if (buffer == null) {

				buffer = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);

			}

			Graphics2D g2d = buffer.createGraphics();
			g2d.clearRect(0, 0, cp.x * cp.dim + 8, cp.y * cp.dim + 8);
			g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION,
					RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
			g2d.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
			g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
			g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
			g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

			int row;
			int col;
			// Draw background
			for (row = 0; row < cp.y; row++) {
				for (col = 0; col < cp.x; col++) {
					if ((row % 2) == (col % 2))
						g2d.setColor(Color.WHITE);
					else
						g2d.setColor(Color.WHITE);

					g2d.fillRect(col * cp.dim, row * cp.dim, cp.dim, cp.dim);
				}
			}

			g2d.setColor(Color.WHITE);
			// g2d.drawImage(robot_image, temp, dim, null);
			// Draw goals
			for (int i = 0; i < cp.num_frogs; i++) {
				g2d.drawImage(goals_images[i], cp.goals[i].getX() * cp.dim, cp.goals[i].getY() * cp.dim, null);
			}
			// Draw frogs
			for (int i = 0; i < cp.num_frogs; i++) {
				g2d.drawImage(frog_images[i], frog_graphics[i].getX(), frog_graphics[i].getY(), null);
			}
			// Draw cars
			for (int i = 0; i < cp.num_cars; i++) {
				if (!cp.is_free_point(cp.cars[i])) {
					g2d.drawImage(car_image, cp.cars[i].getX() * cp.dim, cp.cars[i].getY() * cp.dim, null);
				}
			}
		}

	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2d = (Graphics2D) g;
		if (buffer != null) {
			g2d.drawImage(buffer, 0, 0, this);
		}
	}

}