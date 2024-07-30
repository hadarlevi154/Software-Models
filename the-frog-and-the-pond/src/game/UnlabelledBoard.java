package game;

import java.io.File;

import javax.imageio.ImageIO;

@SuppressWarnings("serial")
public class UnlabelledBoard extends Board {
	public UnlabelledBoard(ControlPanel cp) {
		super(cp);
	}

	@Override
	public void init() throws Exception {
		for (int i = 0; i < cp.num_frogs; i++) {
			start_graphics[i] = new Point();
			target_graphics[i] = new Point();
			frog_graphics[i] = new Point();
		}

		car_image = ImageIO.read(new File("img/Obstacle.png"));

		for (int i = 0; i < cp.num_frogs; i++) {
			base_robot_images[i] = ImageIO.read(new File("img/Robot" + String.valueOf(0) + ".png"));
			frog_images[i] = ImageIO.read(new File("img/Robot" + String.valueOf(0) + ".png"));
			goals_images[i] = ImageIO.read(new File("img/Goal" + String.valueOf(0) + ".png"));
		}
	}
}
