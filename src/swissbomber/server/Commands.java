package swissbomber.server;

import java.io.IOException;
import java.util.Arrays;

import swissbomber.Powerup;
import swissbomber.Tile;

public class Commands {

	private Commands() {}

	/**
	 * <ul>
	 * <b><i>endGame</i></b><br>
	 * <br>
	 * <code>&nbsp;public static void endGame({@link ServerController} sc) throws {@link IOException}</code><br>
	 * <br>
	 * Tell the client that the server has been closed
	 * @param sc - The {@link ServerController} to send to.
	 * @throws IOException If {@link ServerController#write(int, int...)} throws an <code>IOException</code>
	 *         </ul>
	 */
	public static void endGame(ServerController sc) throws IOException {
		sc.write(-1);
	}

	/**
	 * <ul>
	 * <b><i>positionPlayer</i></b><br>
	 * <br>
	 * <code>&nbsp;public static void positionPlayer({@link ServerController} sc, {@link Character} player) throws {@link IOException}</code><br>
	 * <br>
	 * Send the position of the given player to the client
	 * @param sc - The {@link ServerController} to send to.
	 * @param player - The {@link Character} to send information about
	 * @throws IOException If {@link ServerController#write(int, int...)} throws an <code>IOException</code>
	 *         </ul>
	 */
	public static void positionPlayer(ServerController sc, Character player) throws IOException {
		sc.write(0, sc.indexOf(player), Float.floatToIntBits(player.getX()), Float.floatToIntBits(player.getY()));
	}

	/**
	 * <ul>
	 * <b><i>clearTile</i></b><br>
	 * <br>
	 * <code>&nbsp;public static void clearTile({@link ServerController} sc, int x, int y) throws {@link IOException}</code><br>
	 * <br>
	 * Tell the client that the tile at the given coordinates is <code>null</code>
	 * @param sc - The {@link ServerController} to send to.
	 * @param x - The x coordinate of the blank tile
	 * @param y - The y coordinate of the blank tile
	 * @throws IOException If {@link ServerController#write(int, int...)} throws an <code>IOException</code>
	 *         </ul>
	 */
	public static void clearTile(ServerController sc, int x, int y) throws IOException {
		sc.write(1, x, y);
	}

	/**
	 * <ul>
	 * <b><i>setBombTimer</i></b><br>
	 * <br>
	 * <code>&nbsp;public static void setBombTimer({@link ServerController} sc, {@link Bomb} bomb) throws {@link IOException}</code><br>
	 * <br>
	 * Send the timer of the given bomb to the client
	 * @param sc - The {@link ServerController} to send to.
	 * @param bomb - The {@link Bomb} to send information about
	 * @throws IOException If {@link ServerController#write(int, int...)} throws an <code>IOException</code>
	 * @see {@link #setTile(ServerController, int, int)}
	 *      </ul>
	 */
	public static void setBombTimer(ServerController sc, Bomb bomb) throws IOException {
		sc.write(2, (int) (bomb.timer / 1000), bomb.id);
	}

	/**
	 * <ul>
	 * <b><i>setPowerupAtTile</i></b><br>
	 * <br>
	 * <code>&nbsp;public static void setPowerupAtTile({@link ServerController} sc, int x, int y, {@link Powerup} powerup) throws {@link IOException}</code><br>
	 * <br>
	 * Tell the client that there is the given powerup at the given tile
	 * @param sc - The {@link ServerController} to send to.
	 * @param x - The x coordinate of the powerup
	 * @param y - The y coordinate of the powerup
	 * @param powerup - The powerup that is at the given coordinates
	 * @throws IOException If {@link ServerController#write(int, int...)} throws an <code>IOException</code>
	 * @see {@link #setTile(ServerController, int, int)}
	 *      </ul>
	 */
	public static void setPowerupAtTile(ServerController sc, int x, int y) throws IOException {
		sc.write(3, Arrays.asList(Powerup.POWERUPS).indexOf(Game.getMap()[x][y]), x, y);
	}

	/**
	 * <ul>
	 * <b><i>setPowerups</i></b><br>
	 * <br>
	 * <code>&nbsp;public static void setPowerups({@link ServerController} sc, {@link Character} player) throws {@link IOException}</code><br>
	 * <br>
	 * Send the values of the given player's powerups to the client
	 * @param sc - The {@link ServerController} to send to.
	 * @param player - The {@link Character} to send information about
	 * @throws IOException If {@link ServerController#write(int, int...)} throws an <code>IOException</code>
	 *         </ul>
	 */
	public static void setPowerups(ServerController sc, Character player) throws IOException {
		int powerups = 0;
		if (player.hasPiercingBombs()) powerups |= 0b00001;
		if (player.hasRemoteBombs()) powerups |= 0b00010;
		if (player.canKick()) powerups |= 0b00100;
		if (player.isNextDangerous()) powerups |= 0b01000;
		if (player.isNextPowerful()) powerups |= 0b10000;
		sc.write(4, sc.indexOf(player), player.getBombPower(), player.getSpeed(), player.getMaxBombs(), powerups);
	}

	/**
	 * <ul>
	 * <b><i>playerDied</i></b><br>
	 * <br>
	 * <code>&nbsp;public static void playerDied({@link ServerController} sc, {@link Character} player) throws {@link IOException}</code><br>
	 * <br>
	 * Tell the client that the given player is dead
	 * @param sc - The {@link ServerController} to send to.
	 * @param player - The {@link Character} that is dead
	 * @throws IOException If {@link ServerController#write(int, int...)} throws an <code>IOException</code>
	 *         </ul>
	 */
	public static void playerDied(ServerController sc, Character player) throws IOException {
		sc.write(5, sc.indexOf(player));
	}

	/**
	 * <ul>
	 * <b><i>setSimpleTile</i></b><br>
	 * <br>
	 * <code>&nbsp;public static void setSimpleTile({@link ServerController} sc, int x, int y) throws {@link IOException}</code><br>
	 * <br>
	 * Tell the client that the tile at the given coordinates has a specific armor and color, and no other attributes. The armor and color are obtained from {@link Game#getMap()}
	 * @param sc - The {@link ServerController} to send to.
	 * @param x - The x coordinate of the tile
	 * @param y - The y coordinate of the tile
	 * @throws IOException If {@link ServerController#write(int, int...)} throws an <code>IOException</code>
	 * @throws ArrayIndexOutOfBoundsException If the given coordinate is out of bounds
	 * @see {@link #setTile(ServerController, int, int)}
	 *      </ul>
	 */
	public static void setSimpleTile(ServerController sc, int x, int y) throws IOException {
		Tile tile = Game.getMap()[x][y];
		sc.write(6, tile.getArmor(), tile.getColor().getRGB(), x, y);
	}

	/**
	 * <ul>
	 * <b><i>setTile</i></b><br>
	 * <br>
	 * <code>&nbsp;public static void setTile({@link ServerController} sc, int x, int y) throws {@link IOException}</code><br>
	 * <br>
	 * Convenience method - calls {@link #clearTile(ServerController, int, int)}, {@link #setPowerupAtTile(ServerController, int, int)}, or {@link #setSimpleTile(ServerController, int, int)} based on the tile.
	 * @param sc - The {@link ServerController} to send to.
	 * @param x - The x coordinate of the tile
	 * @param y - The y coordinate of the tile
	 * @throws IOException If {@link ServerController#write(int, int...)} throws an <code>IOException</code>
	 * @throws ArrayIndexOutOfBoundsException If the given coordinate is out of bounds
	 * @see {@link #clearTile(ServerController, int, int)}<br>
	 *      {@link #setSimpleTile(ServerController, int, int)}<br>
	 *      {@link #setPowerupAtTile(ServerController, int, int)}<br>
	 *      {@link Game#getMap()}
	 *      </ul>
	 */
	public static void setTile(ServerController sc, int x, int y) throws IOException {
		Tile tile = Game.getMap()[x][y];

		if (tile == null) {
			clearTile(sc, x, y);
		} else if (tile instanceof Powerup) {
			setPowerupAtTile(sc, x, y);
		} else {
			setSimpleTile(sc, x, y);
		}
	}

	/**
	 * <ul>
	 * <b><i>placeBomb</i></b><br>
	 * <br>
	 * <code>&nbsp;public static void placeBomb({@link ServerController} sc, {@link Bomb} bomb) throws {@link IOException}</code><br>
	 * <br>
	 * Tell the client that the given bomb was placed. This sends <i>only</i> the bomb's owner, power, position, and id, and whether or not it is remote, piercing, dangerous, or powerful
	 * @param sc - The {@link ServerController} to send to.
	 * @param bomb - The {@link Bomb} to send info about
	 * @throws IOException If {@link ServerController#write(int, int...)} throws an <code>IOException</code>
	 *         </ul>
	 */
	public static void placeBomb(ServerController sc, Bomb bomb) throws IOException {
		int powerups = 0;
		if (bomb.piercing) powerups |= 0b0001;
		if (bomb.remote) powerups |= 0b0010;
		if (bomb.dangerous) powerups |= 0b0100;
		if (bomb.powerful) powerups |= 0b1000;
		sc.write(sc.indexOf(bomb.owner), bomb.power, powerups, Float.floatToIntBits(bomb.x), Float.floatToIntBits(bomb.y));
	}

	/**
	 * <ul>
	 * <b><i>setRemainingBombs</i></b><br>
	 * <br>
	 * <code>&nbsp;public static void setRemainingBombs({@link ServerController} sc, {@link Character} player) throws {@link IOException}</code><br>
	 * <br>
	 * Send the number of bombs the given player has (can still place) to the client
	 * @param sc - The {@link ServerController} to send to.
	 * @param player - The {@link Character} to send information about
	 * @throws IOException If {@link ServerController#write(int, int...)} throws an <code>IOException</code>
	 *         </ul>
	 */
	public static void setRemainingBombs(ServerController sc, Character player) throws IOException {
		sc.write(8, sc.indexOf(player), player.getCurrentBombs());
	}

	/**
	 * <ul>
	 * <b><i>setExplosionSize</i></b><br>
	 * <br>
	 * <code>&nbsp;public static void setExplosionSize({@link ServerController} sc, {@link Bomb} bomb) throws {@link IOException}</code><br>
	 * <br>
	 * Send the {@linkplain Bomb#getExplosionSize() explosion size} of the given bomb to the client
	 * @param sc - The {@link ServerController} to send to.
	 * @param bomb - The {@link Bomb} to send information about
	 * @throws IOException If {@link ServerController#write(int, int...)} throws an <code>IOException</code>
	 *         </ul>
	 */
	public static void setExplosionSize(ServerController sc, Bomb bomb) throws IOException {
		int[] send = new int[6];
		send[0] = 9;
		send[5] = bomb.id;
		System.arraycopy(bomb.explosionSize, 0, send, 1, 4);
		sc.write(send);
	}

	/**
	 * <ul>
	 * <b><i>sendBomb</i></b><br>
	 * <br>
	 * <code>&nbsp;public static void sendBomb(ServerController sc, Bomb bomb) throws IOException</code><br>
	 * <br>
	 * Send all of the potentially important information about the given bomb to the client
	 * @param sc - The {@link ServerController} to send to.
	 * @param bomb - The {@link Bomb} to send info about
	 * @throws IOException If {@link ServerController#write(int, int...)} throws an <code>IOException</code>
	 *         </ul>
	 */
	public static void sendBomb(ServerController sc, Bomb bomb) throws IOException {
		int powerups = 0;
		if (bomb.piercing) powerups |= 0b0001;
		if (bomb.remote) powerups |= 0b0010;
		if (bomb.dangerous) powerups |= 0b0100;
		if (bomb.powerful) powerups |= 0b1000;
		sc.write(10, sc.indexOf(bomb.owner), bomb.power, powerups, bomb.explosionSize[0], bomb.explosionSize[1], bomb.explosionSize[2], bomb.explosionSize[3], Float.floatToIntBits(bomb.x), Float.floatToIntBits(bomb.y), bomb.id);
	}

	/**
	 * <ul>
	 * <b><i>positionBomb</i></b><br>
	 * <br>
	 * <code>&nbsp;public static void positionBomb(ServerController sc, Bomb bomb) throws IOException</code><br>
	 * <br>
	 * Send the given bomb's location to the client
	 * @param sc - The {@link ServerController} to send to.
	 * @param bomb - The {@link Bomb} to send info about
	 * @throws IOException If {@link ServerController#write(int, int...)} throws an <code>IOException</code>
	 *         </ul>
	 */
	public static void positionBomb(ServerController sc, Bomb bomb) throws IOException {
		sc.write(11, Float.floatToIntBits(bomb.x), Float.floatToIntBits(bomb.y), bomb.id);
	}

	/**
	 * <ul>
	 * <b><i>startGame</i></b><br>
	 * <br>
	 * <code>&nbsp;public static void startGame(ServerController sc) throws IOException</code><br>
	 * <br>
	 * Tell the client that the game has started
	 * @param sc - The {@link ServerController} to send to.
	 * @throws IOException If {@link ServerController#write(int, int...)} throws an <code>IOException</code>
	 *         </ul>
	 */
	public static void startGame(ServerController sc) throws IOException {
		sc.write(128);
	}

	/**
	 * <ul>
	 * <b><i>sendNumberOfPlayers</i></b><br>
	 * <br>
	 * <code>&nbsp;public static void sendNumberOfPlayers({@link ServerController} sc) throws {@link IOException}</code><br>
	 * <br>
	 * Send the number of players in the game to the client
	 * @param sc - The {@link ServerController} to send to.
	 * @throws IOException If {@link ServerController#write(int, int...)} throws an <code>IOException</code>
	 *         </ul>
	 */
	public static void sendNumberOfPlayers(ServerController sc) throws IOException {
		sc.write(129, Game.getCharacters().size());
	}

	/**
	 * <ul>
	 * <b><i>colorPlayer</i></b><br>
	 * <br>
	 * <code>&nbsp;public static void colorPlayer({@link ServerController} sc, {@link Character} player) throws {@link IOException}</code><br>
	 * <br>
	 * Send the color of the given player to the client
	 * @param sc - The {@link ServerController} to send to.
	 * @param player - The {@link Character} to send information about
	 * @throws IOException If {@link ServerController#write(int, int...)} throws an <code>IOException</code>
	 *         </ul>
	 */
	public static void colorPlayer(ServerController sc, Character player) throws IOException {
		sc.write(130, sc.indexOf(player), player.getColor().getRGB());
	}

	/**
	 * <ul>
	 * <b><i>sendVersion</i></b><br>
	 * <br>
	 * <code>&nbsp;public static void sendVersion(ServerController sc) throws IOException</code><br>
	 * <br>
	 * Send {@link Network#VERSION} to the client to ensure that the client is parsing the correct information. It is up to the client whether or not the close the connection.
	 * @param sc - The {@link ServerController} to send to.
	 * @throws IOException If {@link ServerController#write(int, int...)} throws an <code>IOException</code>
	 *         </ul>
	 */
	public static void sendVersion(ServerController sc) throws IOException {
		sc.write(131, Network.VERSION);
	}

}
