package swissbomber.client;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class Character {

	private boolean alive = true;
	private float positionX, positionY;
	private Color color;

	int bombPower = 1;
	float speed = 5; // Tiles per second
	int maxBombs = 1, currentBombs = maxBombs;
	boolean piercingBombs = false;
	boolean remoteBombs = false;
	private List<Bomb> activeRemoteBombs = new ArrayList<>();
	boolean kicks = false;

	private float radius = 0.4f;

	private List<Bomb> tempUncollidableBombs = new ArrayList<>();

	public Character(float positionX, float positionY, Color color) {
		this.positionX = positionX;
		this.positionY = positionY;
		this.color = color;
	}

	public boolean isAlive() {
		return alive;
	}

	public void kill() {
		alive = false;
	}

	public float getX() {
		return positionX;
	}

	public float getY() {
		return positionY;
	}

	public Color getColor() {
		return color;
	}

	public int getBombPower() {
		return bombPower;
	}

	public int getCurrentBombs() {
		return currentBombs;
	}

	public int getMaxBombs() {
		return maxBombs;
	}

	public float getSpeed() {
		return speed;
	}

	public boolean hasPiercingBombs() {
		return piercingBombs;
	}

	public boolean hasRemoteBombs() {
		return remoteBombs;
	}

	public void addRemoteBomb(Bomb bomb) {
		activeRemoteBombs.add(bomb);
	}

	public boolean detonateRemoteBomb() {
		if (activeRemoteBombs.size() <= 0) return false;
		activeRemoteBombs.remove(0).detonate();
		return true;
	}

	public boolean detonateRemoteBomb(Bomb bomb) {
		if (!activeRemoteBombs.contains(bomb)) return false;
		activeRemoteBombs.remove(activeRemoteBombs.indexOf(bomb)).detonate();
		return true;
	}

	public boolean canKick() {
		return kicks;
	}

	public float getRadius() {
		return radius;
	}

	public void addTempUncollidableBomb(Bomb bomb) {
		tempUncollidableBombs.add(bomb);
	}

	public void move(Game game, double angle, long deltaTime) {
		if (!alive) return;

		double distance = speed / 1000000000 * deltaTime;
		positionX += Math.cos(Math.toRadians(angle)) * distance;
		positionY -= Math.sin(Math.toRadians(angle)) * distance;

		int[][] collidableTiles = {{0, 0}, {-1, -1}, {-1, 0}, {-1, 1}, {0, -1}, {0, 1}, {1, -1}, {1, 0}, {1, 1}};

		collidableTiles: for (int[] collidableTile : collidableTiles) {
			if (game.getMap()[(int) (positionX + collidableTile[0])][(int) (positionY + collidableTile[1])] != null) {
				for (Bomb bomb : tempUncollidableBombs) {
					if (!collidesWithBomb(bomb)) tempUncollidableBombs.remove(bomb);
					continue collidableTiles;
				}

				float distanceX = Math.abs(positionX - ((int) (positionX + collidableTile[0]) + 0.5f));
				float distanceY = Math.abs(positionY - ((int) (positionY + collidableTile[1]) + 0.5f));

				if (distanceX > 0.5f + radius) continue;
				if (distanceY > 0.5f + radius) continue;

				if (distanceY <= 0.5f && distanceX >= distanceY) {
					positionX = properPosition(positionX, (float) (Math.floor(positionX) + 0.5f + collidableTile[0]));
					continue;
				}
				if (distanceX <= 0.5f && distanceX <= distanceY) {
					positionY = properPosition(positionY, (float) (Math.floor(positionY) + 0.5f + collidableTile[1]));
					continue;
				}

				if (Math.pow(distanceX - 0.5f, 2) + Math.pow(distanceY - 0.5f, 2) <= Math.pow(radius, 2)) {
					if (distanceY > distanceX)
						positionY = (float) (Math.floor(positionY) + 0.5f + collidableTile[1] * (0.5f - radius));
					else
						positionX = (float) (Math.floor(positionX) + 0.5f + collidableTile[0] * (0.5f - radius));
				}
			}
		}
	}

	public void setPosition(float x, float y) {
		this.positionX = x;
		this.positionY = y;
	}

	private float properPosition(float position, float tile) {
		return tile + (position > tile ? 1 : -1) * (radius + 0.5f);
	}

	public boolean collidesWithTile(int x, int y) {
		return collides(x, y, 0.5F);
	}

	/**
	 * <ul>
	 * <b><i>collides</i></b><br>
	 * <br>
	 * <code>&nbsp;private boolean collides(float x, float y, float radius)</code><br>
	 * <br>
	 * Tests whether or not this character collides with a square at the given coordinates
	 * @param x - The x coordinate of the center of the square
	 * @param y - The y coordinate of the center of the square
	 * @param radius - The smallest distance from the center of the square to the edge
	 * @return Whether or not this <code>Character</code> collides with the square
	 *         </ul>
	 */
	private boolean collides(float x, float y, float radius) {
		float distanceX = Math.abs(positionX - (x + radius));
		float distanceY = Math.abs(positionY - (y + radius));

		if (distanceX - radius - this.radius >= -0.001) return false;
		if (distanceY - radius - this.radius >= -0.001) return false;

		if (distanceX <= radius) return true;
		if (distanceY <= radius) return true;

		float dx = distanceX - radius;
		float dy = distanceY - radius;
		return dx * dx + dy * dy <= this.radius * this.radius;
	}

	public boolean collidesWithBomb(Bomb bomb) {
		return collides(bomb.getX(), bomb.getY(), 0.5F);
	}

}
