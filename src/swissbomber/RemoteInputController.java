package swissbomber;

import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class RemoteInputController extends InputController {

	private final ServerSocket socket;
	private Socket clientSocket;
	private BufferedReader in;

	private Thread thread;
	private boolean isRunning;

	public RemoteInputController(final Character character, final ServerSocket socket) {
		super(character, null);

		this.socket = socket;
	}

	@Override
	public void keyPressed(final KeyEvent e) {}

	@Override
	public void keyReleased(final KeyEvent e) {}

	@Override
	public void keyTyped(final KeyEvent e) {}

	public void waitToConnect() throws IOException {
		clientSocket = socket.accept();
		in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
	}

	public void start() {
		thread = new Thread(() -> {
			isRunning = true;
			try {
				int r;
				while (isRunning && (r = in.read()) != -1) {
					final int key = r & 0b111;
					final boolean down = (r & 0b1000) == 0b1000;

					keyPressed[key] = down;

					if (down && key >= 4) isHeld[key - 4] = false;
				}
			} catch (final IOException e) {
				isRunning = false;
			}
		});
		thread.start();
	}

	public void stop() {
		isRunning = false;
		if (thread != null && thread.isAlive()) thread.interrupt();
	}

}
