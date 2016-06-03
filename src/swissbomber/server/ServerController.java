package swissbomber.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ServerController implements Controller {

	final Character character;
	private boolean[] keyPressed = new boolean[6];
	private boolean[] isHeld = new boolean[2];

	@Deprecated
	protected final int[] indices;
	@Deprecated
	protected final int index;

	protected final Socket socket;
	private final ObjectInputStream in;
	private final ObjectOutputStream out;

	public ServerController(Socket socket, Character character, int[] indices) throws IOException {
		this.socket = socket;
		this.in = new ObjectInputStream(this.socket.getInputStream());
		this.out = new ObjectOutputStream(this.socket.getOutputStream());

		this.character = character;

		this.indices = indices;
		int i = -1;
		for (int n = 0; n < indices.length; n++) {
			if (indices[n] == 0) {
				i = n;
				break;
			}
		}
		if (i < 0) throw new IllegalArgumentException();
		this.index = i;
	}

	/**
	 * <ul>
	 * <b><i>indexOf</i></b><br>
	 * <br>
	 * <code>&nbsp;public int indexOf(Character player)</code><br>
	 * <br>
	 * @param player - The Player to get the index of
	 * @return The index of the given player in the corresponding client's player list.
	 * </ul>
	 */
	public int indexOf(Character player) {
		return indices[Game.getCharacters().indexOf(player)];
	}
	
	@Override
	public void step(long deltaTime) {
		if (!character.isAlive()) return;

		if (keyPressed[4] && !isHeld[0] && character.getCurrentBombs() > 0) {
			Bomb bomb = Game.placeBomb((int) character.getX(), (int) character.getY(), character);
			if (bomb != null) {
				character.removeBomb();
				for (Controller c : Game.getControllers()) {
					if (!(c instanceof ServerController)) continue;

					ServerController controller = (ServerController) c;

					try {
						controller.write(7, controller.indices[this.index], bomb.power, bomb.piercing ? 1 : 0, bomb.remote ? 1 : 0, (int) (character.getX()), (int) (character.getY()));
						controller.write(8, controller.indices[this.index], this.character.getCurrentBombs());
					} catch (IOException e) {
						controller.character.kill();
					}
				}
			}

			isHeld[0] = true;
		}

		if (keyPressed[5] && !isHeld[1]) {
			character.detonateRemoteBomb();
			isHeld[1] = true;
		}

		int horizontal = (keyPressed[2] ? -1 : 0) + (keyPressed[3] ? 1 : 0);
		int vertical = (keyPressed[0] ? 1 : 0) + (keyPressed[1] ? -1 : 0);

		if (horizontal == 0 && vertical == 0) {
			return;
		}

		double angle;
		if (horizontal == 1 && vertical == -1) {
			angle = 315;
		} else {
			angle = (horizontal == -1 ? 180 : 0) + (vertical == -1 ? 270 : (vertical == 1 ? 90 : 0));
			if (horizontal != 0 && vertical != 0) {
				angle /= 2;
			}
		}

		float x1 = character.getX(), y1 = character.getY();
		character.move(angle, deltaTime);
		if (Math.abs(character.getX() - x1) + Math.abs(character.getY() - y1) > 0.001) {
			for (Controller c : Game.getControllers()) {
				if (!(c instanceof ServerController) || c == this) continue;

				ServerController controller = (ServerController) c;

				try {
					controller.write(0, controller.indices[this.index], Float.floatToIntBits(character.getX()), Float.floatToIntBits(character.getY()));
				} catch (IOException e) {}
			}
		}
	}

	public void read() throws IOException {
		int key = in.readInt();
		boolean down = in.readBoolean();

		if (key >= 0 && key < keyPressed.length) {
			keyPressed[key] = down;
			if (!down && key >= 4) isHeld[key - 4] = false;
			Log.print("PLAYER" + index, (down ? "Pressed" : "Released") + " key " + key);
		}
	}

	public synchronized void write(int cmd, int... args) throws IOException {
		out.writeInt(cmd);
		for (int arg : args) {
			out.writeInt(arg);
		}
		out.flush();
		String s = cmd + "";
		for (int i : args)
			s += ", " + i;
		Log.print("SERVER -> PLAYER" + index, s);
	}
	
	public synchronized void write(int[] cmd) throws IOException {
		for (int arg : cmd) {
			out.writeInt(arg);
		}
		out.flush();
		String s = "";
		for (int i : cmd)
			s += ", " + i;
		Log.print("SERVER -> PLAYER" + index, s.substring(2));
	}

	public void start() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					write(128);
				} catch (IOException e) {
					character.kill();
					return;
				}

				while (true) {
					try {
						read();
					} catch (IOException e) {
						character.kill();
						break;
					}
				}
			}
		}).start();
	}

}
