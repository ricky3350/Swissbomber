package swissbomber.server;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import javax.swing.Timer;

public class Network {

	public static final int VERSION = 0;

	public static final Timer NETWORK_LOOP = new Timer(4096, new ActionListener() {

		@Override
		public void actionPerformed(ActionEvent e) {
			ServerController[] scs = Game.getControllers().stream().filter(c -> c instanceof ServerController).map(c -> (ServerController) c).toArray(size -> new ServerController[size]);
			for (ServerController sc : scs) {
				try {
					for (Character character : Game.getCharacters()) {
						if (character.isAlive()) {
							Commands.positionPlayer(sc, character);
							Commands.setPowerups(sc, character);
						} else {
							Commands.playerDied(sc, character);
						}
					}

					for (int x = 0; x < Game.getMap().length; x++) {
						for (int y = 0; y < Game.getMap()[x].length; y++) {
							Commands.setTile(sc, x, y);
						}
					}
				} catch (IOException exception) {}
			}
		}
	});

	protected static ServerSocket socket;

	private Network() {}

	/**
	 * <ul>
	 * <b><i>openServer</i></b><br>
	 * <br>
	 * <code>&nbsp;public static void openServer() throws IOException</code><br>
	 * <br>
	 * Opens the server at the IP address specified in <code>ip.cfg</code>. If no IP or port are specified, the default address (<code>localhost</code>) and port (<code>11432</code>) are used.
	 * @throws IOException If {@link ServerSocket#bind(java.net.SocketAddress, int)} throws an IOException
	 *         </ul>
	 */
	public static void openServer() throws IOException {
		Files.createDirectories(Paths.get("server/settings"));
		Path path = Paths.get("server/settings/ip.cfg");

		String address = null;
		int port = -1;

		try {
			List<String> lines = Files.readAllLines(path);
			for (String line : lines) {
				if (line.startsWith("address=")) {
					address = line.substring(line.indexOf('=') + 1);
				} else if (line.startsWith("port=")) {
					try {
						port = Integer.parseInt(line.substring(line.indexOf('=') + 1));
					} catch (NumberFormatException e) {}
				}
				if (port > 0 && address == null) break;
			}
		} catch (IOException e) {}

		if (address == null) {
			Log.print("WARN", "'address' not found, using default address 'localhost'.");
			address = "localhost";
		}
		if (port <= 0 || port > 65535) {
			Log.print("WARN", "'port' not found, using default port '11432'.");
			port = 11432;
		}

		try {
			Files.write(path, String.format("address=%s\nport=%d", address, port).getBytes());
		} catch (IOException e) {}

		socket = new ServerSocket();
		try {
			Log.print("Binding to " + address + ":" + port + "...");
			socket.bind(new InetSocketAddress(address, port));
			Log.print("Server bound to " + socket.getInetAddress().getHostAddress() + ":" + port);
		} catch (IOException e) {
			Log.print("ERROR", "Failed to bind to " + address + ":" + port + "! Perhaps another server is already running on that port?");
			throw e;
		}
	}

	/**
	 * <ul>
	 * <b><i>getNewPlayer</i></b><br>
	 * <br>
	 * <code>&nbsp;public static ServerController getNewPlayer({@link Character} ch, int index, int numPlayers) throws IOException</code><br>
	 * <br>
	 * Creates a new {@link ServerController} for a new client, using the given character. {@link ServerController#indices} are assigned based on the given index.
	 * @param ch - The {@link Character} to bind the {@link ServerController} to.
	 * @param index - The index of this player
	 * @return A new {@link ServerController}
	 * @throws IOException if {@link ServerSocket#accept()} throws an IOException
	 *         </ul>
	 */
	public static ServerController getNewPlayer(Character ch, int index) throws IOException {
		try {
			int[] indices = new int[Game.getPlayerCount()];
			for (int n = 0; n < Game.getPlayerCount(); n++)
				indices[n] = n == index ? 0 : (n < index ? n + 1 : n);
			ServerController ret = new ServerController(socket.accept(), ch, indices);
			Log.print("Accepted player at " + ret.socket.getInetAddress().getHostAddress());
			return ret;
		} catch (IOException e) {
			Log.print("ERROR", "Could not create player controller.");
			throw e;
		}
	}

	/**
	 * <ul>
	 * <b><i>notifyDead</i></b><br>
	 * <br>
	 * <code>&nbsp;public static void notifyDead(Character character)</code><br>
	 * <br>
	 * Sends a message to all players that the specified character has died.
	 * @param character - The character that died
	 *        </ul>
	 */
	public static void notifyDead(Character character) {
		int index = Game.getCharacters().indexOf(character);
		if (index < 0) return;

		for (Controller controller : Game.getControllers()) {
			if (!(controller instanceof ServerController)) continue;

			ServerController sc = (ServerController) controller;

			try {
				Commands.playerDied(sc, character);
			} catch (IOException e) {
				if (sc.character.isAlive()) sc.character.kill();
			}
		}
	}

	public static void close() throws IOException {
		socket.close();
	}

}
