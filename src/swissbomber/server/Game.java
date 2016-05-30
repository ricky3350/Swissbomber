package swissbomber.server;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import swissbomber.Tile;

public class Game {

	List<Character> characters = new ArrayList<>();
	List<Controller> controllers = new ArrayList<>();
	List<Bomb> bombs = new ArrayList<>();
	private Tile[][] map;

	private int targetFPS = 60;

	Game(Tile[][] map, int playerCount) {
		this.map = map;
		Color[] colors = {Color.BLUE, Color.GREEN, Color.ORANGE, Color.MAGENTA};
		float[][] positions = {{1.5f, 1.5f}, {13.5f, 11.5f}, {13.5f, 1.5f}, {1.5f, 11.5f}};
		for (int i = 0; i < playerCount; i++) {
			try {
				Character character = new Character(positions[i][0], positions[i][1], colors[i]);

				ServerController controller = Network.getNewPlayer(character, i, playerCount);

				controller.write(131, Network.VERSION);
				controller.write(129, playerCount);
				for (int n = 0; n < playerCount; n++) {
					controller.write(130, controller.indices[n], colors[n].getRGB());
					controller.write(0, controller.indices[n], Float.floatToIntBits(positions[n][0]), Float.floatToIntBits(positions[n][1]));
				}
				for (int x = 0; x < map.length; x++) {
					for (int y = 0; y < map[x].length; y++) {
						if (map[x][y] == null) {
							controller.write(1, x, y);
						} else {
							controller.write(6, map[x][y].getArmor(), map[x][y].getColor().getRGB(), x, y);
						}
					}
				}

				controllers.add(controller);
				characters.add(character);
			} catch (IOException e) {
				i--;
			}
		}

		for (Controller c : controllers) {
			if (!(c instanceof ServerController)) continue;
			((ServerController) c).start();
		}
		
		new Thread(loop(this)).start();
	}

	public List<Character> getCharacters() {
		return characters;
	}

	public List<Controller> getControllers() {
		return controllers;
	}

	public Tile[][] getMap() {
		return map;
	}

	boolean placeBomb(int x, int y, Character owner) {
		if (map[x][y] == null) {
			map[x][y] = new Bomb(x, y, 1, Color.BLACK, owner, owner.getBombPower(), owner.hasPiercingBombs(), owner.hasRemoteBombs());
			bombs.add((Bomb) map[x][y]);
			if (owner.hasRemoteBombs()) owner.addRemoteBomb((Bomb) map[x][y]);
			for (Character character : characters) {
				if (character.collidesWithTile(x, y)) {
					character.addTempUncollidableTile(map[x][y]);
				}
			}
			return true;
		}
		return false;
	}

	private Runnable loop(Game game) {
		return new Runnable() {

			@Override
			public void run() {
				long deltaTime, currentTime, previousTime = System.nanoTime(), deltaSecond, previousSecond = System.nanoTime();

				while (true) {
					currentTime = System.nanoTime();
					deltaTime = currentTime - previousTime;

					if (deltaTime >= 1000000000 / game.targetFPS) {
						previousTime = currentTime;
						game.update(deltaTime);

						currentTime = System.nanoTime();
						deltaSecond = currentTime - previousSecond;

						if (deltaSecond >= 1000000000) previousSecond = currentTime;
					}
				}
			}
		};
	}

	private void update(long deltaTime) {
		for (Controller controller : controllers) {
			controller.step(this, deltaTime);
		}

		for (int i = 0; i < bombs.size(); i++) {
			if (bombs.get(i).step(this, deltaTime)) {
				bombs.remove(bombs.get(i));
				i--;
			}
		}
	}

}
