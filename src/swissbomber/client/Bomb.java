package swissbomber.client;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import swissbomber.Tile;

public class Bomb extends Tile {

	private static BufferedImage[] animations = new BufferedImage[100];

	public final long TIMER_START;

	private int x, y;
	private Character owner;
	private long timer;
	private boolean hasExploded = false;

	@SuppressWarnings("unused")
	private int power;
	@SuppressWarnings("unused")
	private boolean piercing;
	
	private boolean remote, remoteActivated = false;

	private int[] explosionSize = new int[4]; // Extends up, down, left, right

	static void loadAnimations() {
		for (int i = 1; i <= 100; i++) {
			try {
				animations[i - 1] = new BufferedImage(Game.TILE_LENGTH, Game.TILE_LENGTH, BufferedImage.TYPE_INT_ARGB_PRE);
				Graphics g = animations[i - 1].getGraphics();
				g.drawImage(ImageIO.read(new File("bomb/" + String.format("%04d", i) + ".png")).getScaledInstance(Game.TILE_LENGTH, Game.TILE_LENGTH, BufferedImage.SCALE_SMOOTH), 0, 0, null);
				g.dispose();
			} catch (IOException | NullPointerException e) {
				System.err.println("Error: Failed loading bomb animation image " + i);
				e.printStackTrace();
			}
		}
	}

	public BufferedImage getAnimation() {
		int i = Math.round(100f * timer / TIMER_START);
		if (i <= 0) return animations[99];
		return animations[100 - i];
	}

	Bomb(int x, int y, int armor, Color color, Character owner, int power, boolean piercing, boolean remote) {
		super(armor, color);

		this.x = x;
		this.y = y;
		this.owner = owner;
		this.power = power;
		this.piercing = piercing;
		this.remote = remote;

		TIMER_START = remote ? 750000000L : 3000000000L;
		timer = TIMER_START;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public Color getColor() {
		if (!hasExploded)
			return Color.BLACK;// new Color((int) Math.round((1 - timer / (double)TIMER_START) * 100), 0, 0);
		else
			return new Color((int) Math.round((1 - timer / -1000000000d) * 200), 0, 0, (int) Math.round((1 - timer / -1000000000d) * 255));
	}

	public boolean hasExploded() {
		return hasExploded;
	}

	public int[] getExplosionSize() {
		return explosionSize;
	}

	public void step(Game game, long deltaTime) {
		if (!remote || remoteActivated) timer -= deltaTime;
	}
	
	public void setTimer(int timer) {
		this.timer = timer * 1000;
	}

	public void explode(Game game) {
		if (remote && !remoteActivated) owner.detonateRemoteBomb(this);
		step(game, timer);
	}

	public void detonate() {
		remoteActivated = true;
	}
}
