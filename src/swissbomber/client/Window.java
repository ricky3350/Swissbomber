package swissbomber.client;

import javax.swing.JFrame;

public class Window extends JFrame {

	private static final long serialVersionUID = 5857900679549302482L;

	Window(Game game) {
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setTitle("Swissbomber");

		this.add(game);

		this.setIgnoreRepaint(true);
		this.setResizable(false);
		this.pack();
		this.setVisible(true);
	}

}
