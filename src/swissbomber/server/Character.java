package swissbomber.server;

import java.awt.Color;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

import swissbomber.Powerup;
import swissbomber.Tile;

public class Character {

	private boolean alive = true;
	private float positionX, positionY;
	private Color color;

	private int bombPower = 1;
	private int speed = 5; // Tiles per second
	private int maxBombs = 1, currentBombs = maxBombs;
	private boolean piercingBombs = false;
	private boolean remoteBombs = false;
	private List<Bomb> activeRemoteBombs = new ArrayList<Bomb>();
	private boolean kicks = false, nextDangerous = false, nextPowerful = false;;

	private float radius = 0.4f;

	private List<Bomb> tempUncollidableBombs = new ArrayList<>();

	Character(float positionX, float positionY, Color color) {
		this.positionX = positionX;
		this.positionY = positionY;
		this.color = color;
	}

	public boolean isAlive() {
		return alive;
	}

	public void kill() {
		boolean wasAlive = alive;
		alive = false;
		Game.tryEndGame();
		if (wasAlive) Network.notifyDead(this);
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

	public void removeBomb() {
		currentBombs = Math.max(0, currentBombs - 1);
	}

	public void addBomb() {
		currentBombs = Math.min(maxBombs, currentBombs + 1);
	}

	public int getSpeed() {
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

	public boolean isNextDangerous() {
		return nextDangerous;
	}

	public boolean isNextPowerful() {
		return nextPowerful;
	}

	public float getRadius() {
		return radius;
	}

	public void addTempUncollidableBomb(Bomb bomb) {
		tempUncollidableBombs.add(bomb);
	}

	private static final int[][] COLLIDABLE_TILES = {{0, 0}, {-1, -1}, {-1, 0}, {-1, 1}, {0, -1}, {0, 1}, {1, -1}, {1, 0}, {1, 1}};
	
	public void move(double angle, long deltaTime) {
		if (!alive) return;

		double distance = speed / 1000000000D * deltaTime;
		positionX += Math.cos(Math.toRadians(angle)) * distance;
		positionY -= Math.sin(Math.toRadians(angle)) * distance;

		Game.getBombs().stream().filter(b -> !tempUncollidableBombs.contains(b) && collidesWithBomb(b)).forEach(bomb -> {
			if (kicks) {
				// TODO kick
			}
		});
		
		for (Bomb bomb : tempUncollidableBombs.toArray(new Bomb[tempUncollidableBombs.size()])) {
			if (!collidesWithBomb(bomb)) {
				tempUncollidableBombs.remove(bomb);
			}
		}
		
		for (int[] collidableTile : COLLIDABLE_TILES) {
			Tile tile = Game.getMap()[(int) (positionX + collidableTile[0])][(int) (positionY + collidableTile[1])];

			float distanceX = Math.abs(positionX - ((int) (positionX + collidableTile[0]) + 0.5f));
			float distanceY = Math.abs(positionY - ((int) (positionY + collidableTile[1]) + 0.5f));

			if (tile instanceof Powerup) {
				if (collidesWithPowerup((int) (positionX + collidableTile[0]), (int) (positionY + collidableTile[1]), (Powerup) tile)) {
					activatePowerup((Powerup) tile);
					Game.getMap()[(int) (positionX + collidableTile[0])][(int) (positionY + collidableTile[1])] = null;
				}
				continue;
			}

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

			float dx = distanceX - 0.5f;
			float dy = distanceY - 0.5f;
			if (dx * dx + dy * dy <= radius * radius) {
				if (distanceY > distanceX)
					positionY = (float) (Math.floor(positionY) + 0.5f + collidableTile[1] * (0.5f - radius));
				else
					positionX = (float) (Math.floor(positionX) + 0.5f + collidableTile[0] * (0.5f - radius));
			}
		}
	}

	private float properPosition(float position, float tile) {
		return tile + (position > tile ? 1 : -1) * (radius + 0.5f);
	}

	public boolean collidesWithTile(int x, int y) {
		return Game.collides(x + 0.5F, y + 0.5F, 1, 1, this.positionX, this.positionY, this.radius);
	}

	public boolean collidesWithBomb(Bomb bomb) {
		return Game.collides(bomb.x, bomb.y, 1, 1, this.positionX, this.positionY, this.radius);
	}

	public boolean collidesWithPowerup(int x, int y, Powerup powerup) {
		return Math.hypot(positionX - (x + 0.5f), positionY - (y + 0.5f)) <= radius + powerup.radius;
	}

	private ArrayDeque<Powerup> nextPowerups = new ArrayDeque<>();

	public Powerup nextPowerup() {
		if (nextPowerups.isEmpty()) {
			nextPowerups.addAll(Powerup.randomPowerupOrder());
		}
		return nextPowerups.pop();
	}

	public void activatePowerup(Powerup powerup) {
		switch (powerup.effect) {
			case "power+":
				if (bombPower < 9) bombPower++;
				break;
			case "speed+":
				if (speed < 9) speed++;
				break;
			case "bombs+":
				if (maxBombs < 9) {
					currentBombs++;
					maxBombs++;
				}
				break;
			case "pierce":
				piercingBombs = true;
				break;
			case "remote":
				remoteBombs = true;
				break;
			case "kick":
				kicks = true;
				break;
			case "nextDangerous":
				nextDangerous = true;
				break;
			case "nextPowerful":
				nextPowerful = true;
				break;
			default:
				Log.print("ERROR", "Undefined powerup \"" + powerup.effect + "\"");
				break;
		}
	}
}
