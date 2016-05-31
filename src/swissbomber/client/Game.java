package swissbomber.client;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import swissbomber.Powerup;
import swissbomber.Tile;

public class Game extends JPanel {

	private static final long serialVersionUID = -7101890057819507949L;

	List<Character> characters = new ArrayList<>();
	Controller controller;
	List<Bomb> bombs = new ArrayList<>();
	Tile[][] map = new Tile[15][13];

	private int currentFPS = 0;
	private int targetFPS = 60;
	public static final int TILE_LENGTH = 50;

	public int playerCount;
	public List<Color> colors = new ArrayList<>();
	public List<Point2D> positions = new ArrayList<>();

	Game() throws IOException {
		new Thread(new Runnable() {

			@Override
			public void run() {
				int[] controls = {KeyEvent.VK_W, KeyEvent.VK_S, KeyEvent.VK_A, KeyEvent.VK_D, KeyEvent.VK_SPACE, KeyEvent.VK_SHIFT};
				try {
					Network.startingConfig(Game.this);
				} catch (IOException e) {
					System.exit(0);
				}
				for (int i = 0; i < playerCount; i++) {
					Point2D pt = positions.get(i);
					Character newCharacter = new Character((float) pt.getX(), (float) pt.getY(), colors.get(i));
					characters.add(newCharacter);
				}
				controller = new InputController(characters.get(0), controls);
				addKeyListener((InputController) controller);

				repaint();
				new Thread(loop(Game.this)).start();
				new Thread(networkLoop(Game.this)).start();
			}

		}).start();

		setPreferredSize(new Dimension(map.length * TILE_LENGTH + 200, map[0].length * TILE_LENGTH));
		setFocusable(true);
		requestFocusInWindow();
	}

	public List<Character> getCharacters() {
		return characters;
	}

	public Tile[][] getMap() {
		return map;
	}

	public void setCurrentFPS(int fps) {
		currentFPS = fps;
	}

	public int getTargetFPS() {
		return targetFPS;
	}

	public int getTileLength() {
		return TILE_LENGTH;
	}

	void placeBomb(int x, int y, int charIndex, int power, boolean piercing, boolean remote) {
		map[x][y] = new Bomb(x, y, 1, Color.BLACK, characters.get(charIndex), power, piercing, remote);
		bombs.add((Bomb) map[x][y]);
		for (Character character : characters) {
			if (character.collidesWithTile(x, y)) {
				character.addTempUncollidableTile(map[x][y]);
			}
		}
	}

	@Override
	public void paintComponent(Graphics g) {
		Graphics2D gg = ((Graphics2D) g);
		gg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		gg.setColor(Color.WHITE);
		gg.fillRect(0, 0, map.length * TILE_LENGTH, map[0].length * TILE_LENGTH);

		for (int x = 0; x < map.length; x++) {
			for (int y = 0; y < map[x].length; y++) {
				if (map[x][y] != null) {
					gg.setColor(map[x][y].getColor());
					if (map[x][y] instanceof Bomb) {
						gg.drawImage(((Bomb) map[x][y]).getAnimation(), x * TILE_LENGTH, y * TILE_LENGTH, TILE_LENGTH, TILE_LENGTH, null);
					} else if (map[x][y] instanceof Powerup) {
						gg.fillOval(x * TILE_LENGTH + Math.round(TILE_LENGTH * (0.5f - ((Powerup) map[x][y]).radius)), y * TILE_LENGTH + Math.round(TILE_LENGTH * (0.5f - ((Powerup) map[x][y]).radius)), Math.round(((Powerup) map[x][y]).radius * 2 * TILE_LENGTH), Math.round(((Powerup) map[x][y]).radius * 2 * TILE_LENGTH));
					} else {
						gg.fillRect(x * TILE_LENGTH, y * TILE_LENGTH, TILE_LENGTH, TILE_LENGTH);
					}
				}
			}
		}

		for (Bomb bomb : bombs.toArray(new Bomb[bombs.size()])) {
			if (bomb.hasExploded()) {
				BufferedImage img = new BufferedImage(this.getWidth(), this.getHeight(), BufferedImage.TYPE_INT_ARGB_PRE);
				Graphics2D ig = img.createGraphics();
				ig.setColor(new Color(bomb.getColor().getRGB() & 16777215)); // Remove alpha from color
//				ig.setColor(new Color(bomb.getColor().getRed(), bomb.getColor().getGreen(), bomb.getColor().getBlue()));
				ig.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC, bomb.getColor().getAlpha() / 255f));

				ig.fillRect(bomb.getExplosionSize()[2] * TILE_LENGTH, Math.round((bomb.getY() + 0.05f) * TILE_LENGTH), (bomb.getExplosionSize()[3] - bomb.getExplosionSize()[2] + 1) * TILE_LENGTH, Math.round(0.9f * TILE_LENGTH));
				ig.fillRect(Math.round((bomb.getX() + 0.05f) * TILE_LENGTH), bomb.getExplosionSize()[1] * TILE_LENGTH, Math.round(0.9f * TILE_LENGTH), (bomb.getExplosionSize()[0] - bomb.getExplosionSize()[1] + 1) * TILE_LENGTH);

				ig.dispose();
				gg.drawImage(img, 0, 0, null);
			}
		}

		for (Character character : characters) {
			if (!character.isAlive()) continue;
			gg.setColor(character.getColor());
			gg.fillOval(Math.round(character.getX() * TILE_LENGTH - character.getRadius() * TILE_LENGTH), Math.round(character.getY() * TILE_LENGTH - character.getRadius() * TILE_LENGTH), Math.round(character.getRadius() * 2 * TILE_LENGTH), Math.round(character.getRadius() * 2 * TILE_LENGTH));
		}

		gg.setColor(Color.WHITE);
		gg.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 30));
		gg.drawString(Integer.toString(currentFPS), 10, 30);

		gg.setColor(Color.LIGHT_GRAY);
		gg.fillRect(map.length * TILE_LENGTH, 0, 200, map[0].length * TILE_LENGTH);

		for (int i = 0; i < characters.size(); i++) {
			Color c = characters.get(i).getColor();
			if (!characters.get(i).isAlive()) c = c.darker();
			gg.setColor(c);
			gg.fillRect(map.length * TILE_LENGTH, Math.round(map[0].length * TILE_LENGTH / 4f * i), 200, Math.round(map[0].length * TILE_LENGTH / 4f));

			gg.setColor(Color.LIGHT_GRAY);
			gg.fillRect(map.length * TILE_LENGTH + 20, map[0].length * TILE_LENGTH / 4 * i + 20, 40, 60);
			gg.fillRect(map.length * TILE_LENGTH + 20, map[0].length * TILE_LENGTH / 4 * i + 90, 40, 60);
			gg.fillRect(map.length * TILE_LENGTH + 120, map[0].length * TILE_LENGTH / 4 * i + 20, 60, 60);
			gg.fillRect(map.length * TILE_LENGTH + 120, map[0].length * TILE_LENGTH / 4 * i + 90, 60, 60);

			gg.setColor(Color.BLACK);
			gg.drawString(Integer.toString(characters.get(i).getBombPower()), map.length * TILE_LENGTH + 30, map[0].length * TILE_LENGTH / 4 * i + 60);
			gg.drawString(Integer.toString((int) characters.get(i).getSpeed()), map.length * TILE_LENGTH + 30, map[0].length * TILE_LENGTH / 4 * i + 130);
			gg.drawString(characters.get(i).getCurrentBombs() + "/" + characters.get(i).getMaxBombs(), map.length * TILE_LENGTH + 130, map[0].length * TILE_LENGTH / 4 * i + 60);
			if (characters.get(i).hasPiercingBombs()) {
				gg.setColor(Powerup.POWERUPS[3].getColor());
				gg.fillOval(map.length * TILE_LENGTH + 125, map[0].length * TILE_LENGTH / 4 * i + 95, 20, 20);
			}
			if (characters.get(i).hasRemoteBombs()) {
				gg.setColor(Powerup.POWERUPS[4].getColor());
				gg.fillOval(map.length * TILE_LENGTH + 155, map[0].length * TILE_LENGTH / 4 * i + 95, 20, 20);
			}
			if (characters.get(i).canKick()) {
				gg.setColor(Powerup.POWERUPS[5].getColor());
				gg.fillOval(map.length * TILE_LENGTH + 125, map[0].length * TILE_LENGTH / 4 * i + 125, 20, 20);
			}
		}
	}

	private static Runnable networkLoop(Game game) {
		return new Runnable() {

			@Override
			public void run() {
				while (true) {
					try {
						int[] input = Network.read();

						if (input[0] == 0) {
							game.characters.get(input[1]).setPosition(Float.intBitsToFloat(input[2]), Float.intBitsToFloat(input[3]));
						} else if (input[0] == 1) {
							game.map[input[1]][input[2]] = null;
						} else if (input[0] == 2) {
							Tile bomb = game.map[input[2]][input[3]];
							if (bomb instanceof Bomb) {
								((Bomb) bomb).setTimer(input[1]);
							}
						} else if (input[0] == 3) {
							game.map[input[2]][input[3]] = Powerup.POWERUPS[input[1]];
						} else if (input[0] == 4) {
							Character c = game.characters.get(input[1]);
							switch (input[2]) {
								case 0:
									c.bombPower = input[3];
									break;
								case 1:
									c.speed = input[3];
									break;
								case 2:
									c.maxBombs = input[3];
									break;
								case 3:
									c.piercingBombs = input[3] != 0;
									break;
								case 4:
									c.remoteBombs = input[3] != 0;
									break;
							}
						} else if (input[0] == 5) {
							game.characters.get(input[1]).kill();
						} else if (input[0] == 6) {
							game.map[input[3]][input[4]] = new Tile(input[1], new Color(input[2]));
						} else if (input[0] == 7) {
							game.placeBomb(input[5], input[6], input[1], input[2], input[3] != 0, input[4] != 0);
						} else if (input[0] == 8) {
							game.characters.get(input[1]).currentBombs = input[2];
						}
					} catch (IOException e) {
						System.exit(0);
					}
				}
			}
		};
	}

	private static Runnable loop(Game game) {
		return new Runnable() {

			@Override
			public void run() {
				long deltaTime, currentTime, previousTime = System.nanoTime(), deltaSecond, previousSecond = System.nanoTime();
				int fpsCount = 0;

				while (true) {
					currentTime = System.nanoTime();
					deltaTime = currentTime - previousTime;

					if (deltaTime >= 1000000000 / game.targetFPS) {
						previousTime = currentTime;
						fpsCount++;
						game.update(deltaTime);

						currentTime = System.nanoTime();
						deltaSecond = currentTime - previousSecond;

						if (deltaSecond >= 1000000000) {
							game.setCurrentFPS((int) (fpsCount / (deltaSecond / 1000000000)));
							previousSecond = currentTime;
							fpsCount = 0;
						}
					}
				}
			}
		};
	}

	private void update(long deltaTime) {
		controller.step(this, deltaTime);

		for (Bomb bomb : bombs) {
			bomb.step(this, deltaTime);
		}

		repaint();
	}

}
