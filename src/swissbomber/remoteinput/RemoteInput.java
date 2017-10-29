package swissbomber.remoteinput;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class RemoteInput extends JFrame implements KeyListener {

	private static final long serialVersionUID = -3056446470792957545L;

	private static final int[] keyCodes = {KeyEvent.VK_W, KeyEvent.VK_S, KeyEvent.VK_A, KeyEvent.VK_D, KeyEvent.VK_SPACE, KeyEvent.VK_SHIFT};

	private static final JLabel left = new JLabel("\u2190"), right = new JLabel("\u2192"), up = new JLabel("\u2191"), down = new JLabel("\u2193"), bomb = new JLabel("Bomb"), remote = new JLabel("Remote");
	private static final JLabel[] labels = {up, down, left, right, bomb, remote};

	public static void main(final String[] args) throws IOException {
		final RemoteInput window = new RemoteInput();

		final JPanel keys = new JPanel();
		keys.setFocusable(true);
		keys.requestFocus();
		keys.addKeyListener(window);
		window.add(keys);

		final JPanel panel = new JPanel();
		for (final JLabel label : labels) {
			label.setBackground(Color.GRAY);
			label.setOpaque(false);
			panel.add(label);
		}
		window.add(panel, BorderLayout.NORTH);

		window.setSize(640, 480);
		window.setVisible(true);
	}

	private final Socket socket;
	private final OutputStream out;

	public RemoteInput() throws IOException {
		socket = new Socket("127.0.0.1", 11610);
		out = socket.getOutputStream();
	}

	@Override
	public void keyTyped(final KeyEvent e) {}

	@Override
	public void keyPressed(final KeyEvent e) {
		for (int i = 0; i < keyCodes.length; i++) {
			if (e.getKeyCode() == keyCodes[i]) {
				labels[i].setBackground(Color.RED);
				try {
					out.write(i | 0b1000);
				} catch (final IOException e1) {
					e1.printStackTrace();
				}
			}
		}
	}

	@Override
	public void keyReleased(final KeyEvent e) {
		for (int i = 0; i < keyCodes.length; i++) {
			if (e.getKeyCode() == keyCodes[i]) {
				labels[i].setBackground(Color.GRAY);
				try {
					out.write(i);
				} catch (final IOException e1) {
					e1.printStackTrace();
				}
			}
		}
	}

}
