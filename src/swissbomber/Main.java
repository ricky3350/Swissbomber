package swissbomber;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

import javax.swing.JDialog;
import javax.swing.JProgressBar;

public class Main {

	public static void main(final String[] args) {
		final Tile w = new Tile(-1, Color.GRAY);
		final Tile c = new Tile(1, new Color(180, 160, 90));
		final Tile n = null;
		final Tile[][] grid = {
				{w, w, w, w, w, w, w, w, w, w, w, w, w},
				{w, n, n, c, c, c, c, c, c, c, n, n, w},
				{w, n, w, c, w, c, w, c, w, c, w, n, w},
				{w, c, c, c, c, c, c, c, c, c, c, c, w},
				{w, c, w, c, w, c, w, c, w, c, w, c, w},
				{w, c, c, c, c, c, c, c, c, c, c, c, w},
				{w, c, w, c, w, c, w, c, w, c, w, c, w},
				{w, c, c, c, c, c, c, c, c, c, c, c, w},
				{w, c, w, c, w, c, w, c, w, c, w, c, w},
				{w, c, c, c, c, c, c, c, c, c, c, c, w},
				{w, c, w, c, w, c, w, c, w, c, w, c, w},
				{w, c, c, c, c, c, c, c, c, c, c, c, w},
				{w, n, w, c, w, c, w, c, w, c, w, n, w},
				{w, n, n, c, c, c, c, c, c, c, n, n, w},
				{w, w, w, w, w, w, w, w, w, w, w, w, w}
		};

		final JDialog dialog = new JDialog((Window) null, "Waiting for Connection", false);
		dialog.setLayout(new FlowLayout());
		final JProgressBar pb = new JProgressBar();
		pb.setIndeterminate(true);
		pb.setStringPainted(true);
		pb.setString("Connecting...");
		dialog.add(pb);
		dialog.setSize(320, 100);
		dialog.setLocationRelativeTo(null);
		dialog.addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosed(WindowEvent e) {
				System.exit(0);
			}

		});
		dialog.setVisible(true);

		Bomb.loadAnimations();
		try {
			new Window(new Game(grid, 2));
		} catch (final IOException e) {
			e.printStackTrace();
		}
		dialog.removeWindowListener(dialog.getWindowListeners()[0]);
		dialog.setVisible(false);
	}

}
