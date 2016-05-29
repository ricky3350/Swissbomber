package swissbomber.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class Network {

	protected static ServerSocket socket;
	protected static ObjectInputStream in;
	protected static ObjectOutputStream out;

	private Network() {}

	public static void openServer() throws IOException {
		Path path = Paths.get("settings/server/ip.cgf");

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

	public ServerController getNewPlayer(Character ch) throws IOException {
		try {
			ServerController ret = new ServerController(socket.accept(), ch);
			Log.print("Accepted player at " + ret.socket.getInetAddress().getHostAddress());
			return ret;
		} catch (IOException e) {
			Log.print("WARN", "Could not create player controller.");
			throw e;
		}
	}

}
