package swissbomber.client;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import swissbomber.Tile;

public class Network {

	public static final long VERSION = 0;

	public static Socket socket;
	protected static ObjectInputStream in;
	protected static ObjectOutputStream out;

	private Network() {}

	/**
	 * <ul>
	 * <b><i>getIPAndConnect</i></b><br>
	 * <br>
	 * <code>&nbsp;public static void getIPAndConnect()</code><br>
	 * <br>
	 * Description
	 * </ul>
	 */
	public static boolean getIPAndConnect() {
		Path path = Paths.get("client/settings/saved-ips.cfg");
		Vector<String> prevAddresses = new Vector<>();
		if (Files.notExists(path, LinkOption.NOFOLLOW_LINKS)) {
			try {
				Files.createDirectories(Paths.get("client/settings"));
				Files.createFile(path);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			try {
				prevAddresses.addAll(Files.readAllLines(path));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));

		JTextField address = new JTextField();
		panel.add(address);
		JLabel label = new JLabel("IP Address");
		label.setAlignmentX(0.5F);
		panel.add(label, 0);

		JList<String> addresses = new JList<>(prevAddresses);
		addresses.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		addresses.setVisibleRowCount(5);
		addresses.addListSelectionListener(new ListSelectionListener() {

			@Override
			public void valueChanged(ListSelectionEvent e) {
				address.setText(addresses.getSelectedValue());
			}
		});
		panel.add(new JScrollPane(addresses), 0);

		String a;
		while (true) {
			if (JOptionPane.showOptionDialog(null, panel, "Connect", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, new String[] {"Connect", "Cancel"}, "Connect") != 0) return false;

			String[] parts = address.getText().split(":");
			int port;
			try {
				port = Integer.parseInt(parts[1]);
			} catch (Exception e) {
				port = 11432;
			}

			try {
				a = String.format("%s:%d", parts[0], port);
				socket = new Socket(parts[0], port);
				out = new ObjectOutputStream(socket.getOutputStream());
				in = new ObjectInputStream(socket.getInputStream());
				break;
			} catch (IOException e) {
				JOptionPane.showMessageDialog(null, "Failed to connect to " + parts[0] + ":" + port, "Connection Failed", JOptionPane.ERROR_MESSAGE);
				address.setText("");
			}
		}

		try {
			prevAddresses.add(a);
			Files.write(path, new TreeSet<>(prevAddresses));
		} catch (IOException e) {}
		return true;
	}

	/**
	 * <ul>
	 * <b><i>startingConfig</i></b><br>
	 * <br>
	 * <code>&nbsp;public static void startingConfig(Game game) throws IOException</code><br>
	 * <br>
	 * Configures the given game with information provided by the server.
	 * @param game - The Game to configure
	 * @throws IOException - If {@link #read()} throws an IOException, if {@link #VERSION} doesn't match the server's version, or if the server closes.
	 *         </ul>
	 */
	public static void startingConfig(Game game) throws IOException {
		int[] input;
		while ((input = read())[0] != 128) {
			switch (input[0]) {
				case 129:
					game.playerCount = input[1];
					break;
				case 130:
					while (game.colors.size() <= input[1])
						game.colors.add(Color.BLACK);
					game.colors.set(input[1], new Color(input[2]));
					break;
				case 131:
					if (input[1] != VERSION) {
						JOptionPane.showMessageDialog(null, "The server that you are trying to connect to is running a different version of the game (server: version " + input[1] + ", you: version " + VERSION + ")", "Incompatible Version", JOptionPane.ERROR_MESSAGE);
						throw new IOException();
					}
					break;
				case 0:
					while (game.positions.size() <= input[1])
						game.positions.add(new Point2D.Float(0, 0));
					game.positions.set(input[1], new Point2D.Float(Float.intBitsToFloat(input[2]), Float.intBitsToFloat(input[3])));
					break;
				case 1:
					game.map[input[1]][input[2]] = null;
					break;
				case 6:
					game.map[input[3]][input[4]] = new Tile(input[1], new Color(input[2]));
					break;
				case -1:
					throw new IOException();
			}
		}
	}

	public static int[] read() throws IOException {
		int cmd = in.readInt();

		List<Integer> ret = new ArrayList<>();
		ret.add(cmd);

		final int i = getNumArgs(cmd);
		for (int n = 0; n < i; n++)
			ret.add(in.readInt());

		return ret.stream().mapToInt(n -> n).toArray();
	}

	public static int getNumArgs(int cmd) {
		switch (cmd) {
			case -1:
			case 128:
				return 0;
			case 5:
			case 129:
			case 131:
				return 1;
			case 1:
			case 130:
				return 2;
			case 0:
			case 2:
			case 3:
			case 4:
				return 3;
			case 6:
				return 4;
			default:
				throw new IllegalArgumentException();
		}
	}

}
