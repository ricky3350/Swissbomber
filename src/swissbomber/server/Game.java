package swissbomber.server;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import swissbomber.Powerup;
import swissbomber.Tile;

public class Game {

	private static List<Character> characters = new ArrayList<>();
	private static List<Controller> controllers = new ArrayList<>();
	private static List<Bomb> bombs = new ArrayList<>();
	private static Tile[][] map;

	private static int playerCount;

	
	private static final int TARGET_FPS = 60;

	private Game() {}

	/**
	 * <ul>
	 * <b><i>init</i></b><br>
	 * <br>
	 * <code>&nbsp;public static void init(Tile[][] map, int playerCount)</code><br>
	 * <br>
	 * Starts the game with the given map once the given number of players connect
	 * @param map - The game map
	 * @param playerCount - The number of players to wait for before starting the game
	 *        </ul>
	 */
	public static void init(Tile[][] map, int playerCount) {
		Game.map = map;
		Game.playerCount = playerCount;

		Color[] colors = {Color.BLUE, Color.GREEN, Color.ORANGE, Color.MAGENTA};
		float[][] positions = {{1.5f, 1.5f}, {13.5f, 11.5f}, {13.5f, 1.5f}, {1.5f, 11.5f}};
//		for (int i = 0; i < playerCount; i++) {
//			try {
//				Character character = new Character(positions[i][0], positions[i][1], colors[i]);
//
//				ServerController controller = Network.getNewPlayer(character, i, playerCount);
//
//				Commands.sendVersion(controller);
//				Commands.sendNumberOfPlayers(controller);
//				for (int n = 0; n < playerCount; n++) {
//					Commands.colorPlayer(controller, n, colors[n]);
//					controller.write(0, controller.indices[n], Float.floatToIntBits(positions[n][0]), Float.floatToIntBits(positions[n][1]));
//				}
//				for (int x = 0; x < map.length; x++) {
//					for (int y = 0; y < map[x].length; y++) {
//						if (map[x][y] == null) {
//							controller.write(1, x, y);
//						} else {
//							controller.write(6, map[x][y].getArmor(), map[x][y].getColor().getRGB(), x, y);
//						}
//					}
//				}
//
//				controllers.add(controller);
//				characters.add(character);
//			} catch (IOException e) {
//				i--;
//			}
//		}

		for (int i = 0; i < playerCount; i++) {
			characters.add(new Character(positions[i][0], positions[i][1], colors[i]));
		}

		while (controllers.size() < characters.size()) {
			try {
				final int i = controllers.size();
				ServerController sc = Network.getNewPlayer(characters.get(i), i);

				Commands.sendVersion(sc);
				Commands.sendNumberOfPlayers(sc);

				for (Character c : characters) {
					Commands.colorPlayer(sc, c);
					Commands.positionPlayer(sc, c);
				}

				for (int x = 0; x < Game.map.length; x++) {
					for (int y = 0; y < Game.map[x].length; y++) {
						if (map[x][y] == null) {
							Commands.clearTile(sc, x, y);
						} else {
							Commands.setSimpleTile(sc, x, y);
						}
					}
				}
			} catch (IOException e) {}
		}

		for (Controller c : controllers) {
			if (!(c instanceof ServerController)) continue;
			((ServerController) c).start();
		}

		new Thread(GAME_LOOP).start();
		Network.NETWORK_LOOP.start();
	}

	public static List<Character> getCharacters() {
		return characters;
	}

	public static List<Controller> getControllers() {
		return controllers;
	}

	public static Tile[][] getMap() {
		return map;
	}
	
	public static List<Bomb> getBombs() {
		return bombs;
	}

	public static int getPlayerCount() {
		return playerCount;
	}

	/**
	 * <ul>
	 * <b><i>collides</i></b><br>
	 * <br>
	 * <code>&nbsp;public static boolean collides(float rectX, float rectY, float width, float height, float squareX, float squareY, float radius)</code><br>
	 * <br>
	 * Tests whether or not a rectangle centered at <code>rectX</code> and <code>rectY</code> with the given <code>width</code> and <code>height</code> intersects a square with the given <code>radius</code> centered at <code>squareX</code> and <code>squareY</code>
	 * @param rectX - The x coordinate of the center of the rectangle
	 * @param rectY - The y coordinate of the center of the rectangle
	 * @param width - The width of the rectangle
	 * @param height - The height of the rectangle
	 * @param squareX - The x coordinate of the center of the square
	 * @param squareY - The y coordinate of the center of the square
	 * @param radius - The minimum radius of the square (length of a side / 2) 
	 * @return <code>true</code> if the rectange intersects the square, <code>false</code> otherwise.
	 * </ul>
	 */
	public static boolean collides(float rectX, float rectY, float width, float height, float squareX, float squareY, float radius) {
		if (width <= 0 || height <= 0) return false; 
		
		float dx = Math.abs(squareX - rectX);
		float dy = Math.abs(squareY - rectY);

		return width / 2 + radius - dx > 0.001 && height / 2 + radius - dy > 0.001;
	}
	
	static Bomb placeBomb(int x, int y, Character owner) {
//		if (map[x][y] == null) {
//			map[x][y] = new Bomb(x, y, 1, Color.BLACK, owner, owner.getBombPower(), owner.hasPiercingBombs(), owner.hasRemoteBombs());
//			bombs.add((Bomb) map[x][y]);
//			if (owner.hasRemoteBombs()) owner.addRemoteBomb((Bomb) map[x][y]);
//			for (Character character : characters) {
//				if (character.collidesWithTile(x, y)) {
//					character.addTempUncollidableTile(map[x][y]);
//				}
//			}
//			return (Bomb) map[x][y];
//		} TODO
		return null;
	}

	private static boolean gameOver = false;

	private static final Runnable GAME_LOOP = new Runnable() {

		@Override
		public void run() {
			long deltaTime, currentTime, previousTime = System.nanoTime(), deltaSecond, previousSecond = System.nanoTime();

			while (!gameOver) {
				currentTime = System.nanoTime();
				deltaTime = currentTime - previousTime;

				if (deltaTime >= 1000000000 / Game.TARGET_FPS) {
					previousTime = currentTime;
					Game.update(deltaTime);

					currentTime = System.nanoTime();
					deltaSecond = currentTime - previousSecond;

					if (deltaSecond >= 1000000000) previousSecond = currentTime;
				}
			}
		}
	};

	private static synchronized void update(long deltaTime) {
		Tile[][] oldMap = new Tile[map.length][map[0].length];
		for (int i = 0; i < map.length; i++) {
			oldMap[i] = map[i].clone();
		}

		for (Controller controller : controllers) {
			controller.step(deltaTime);
		}

		for (int i = 0; i < bombs.size(); i++) {
			if (bombs.get(i).step(deltaTime)) {
				bombs.remove(bombs.get(i));
				i--;
			}
		}

		final List<Powerup> powerups = Arrays.asList(Powerup.POWERUPS);
		Set<int[]> commands = new HashSet<>();
		for (int x = 0; x < map.length; x++) {
			for (int y = 0; y < map[x].length; y++) {
				if (map[x][y] == Tile.ASH) {
					map[x][y] = null;
				} else if (map[x][y] instanceof FuturePowerup) {
					Powerup next = ((FuturePowerup) map[x][y]).character.nextPowerup();
					map[x][y] = next == Powerup.NULL ? null : next;
				}

				if (map[x][y] != oldMap[x][y]) {
					if (map[x][y] == null) {
						commands.add(new int[] {1, x, y});
					} else if (map[x][y] instanceof Powerup) {
						commands.add(new int[] {3, powerups.indexOf(map[x][y]), x, y});
//					} else if (map[x][y] instanceof Bomb) { TODO
//						Bomb bomb = (Bomb) map[x][y];
//						commands.add(new int[] {7, characters.indexOf(bomb.owner), bomb.power, bomb.piercing ? 1 : 0, bomb.remote ? 1 : 0, x, y});
//						commands.add(new int[] {2, (int) (bomb.timer / 1000), x, y});
					} else {
						commands.add(new int[] {6, map[x][y].getArmor(), map[x][y].getColor().getRGB(), x, y});
					}
				}
			}
		}

		for (Controller c : controllers) {
			if (!(c instanceof ServerController)) continue;

			ServerController controller = (ServerController) c;
			try {
				for (int[] command : commands) {
					controller.write(command);
				}
			} catch (IOException e) {
				controller.character.kill();
			}
		}
	}

	public static void tryEndGame() {
		if (characters.stream().filter(c -> c.isAlive()).count() <= 1) {
			gameOver = true;
			Network.NETWORK_LOOP.stop();
			for (Controller controller : controllers) {
				if (!(controller instanceof ServerController)) continue;
				try {
					((ServerController) controller).write(-1);
				} catch (IOException e) {}
				try {
					((ServerController) controller).socket.close();
				} catch (IOException e) {}
			}

			try {
				Network.close();
			} catch (IOException e) {}
		}
	}

}
