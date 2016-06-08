package swissbomber.server;

import swissbomber.Powerup;
import swissbomber.Tile;

public class Bomb {

	public final long TIMER_START;

	public final int id;
	private static int LAST_ID = 0;

	float x, y; // TODO
	Character owner;
	long timer;
	boolean hasExploded = false;

	int power;
	boolean piercing, remote, remoteActivated = false, powerful, dangerous, sliding;
	private int slideDirection;

	int[] explosionSize = new int[4]; // Extends up, down, left, right

	Bomb(int x, int y, Character owner, int power, boolean piercing, boolean remote) {
		this.id = LAST_ID++;

		this.x = x;
		this.y = y;
		this.owner = owner;
		this.power = power;
		this.piercing = piercing;
		this.remote = remote;

		TIMER_START = remote ? 750000000L : 3000000000L;
		timer = TIMER_START;
	}

	public float getX() {
		return x;
	}

	public float getY() {
		return y;
	}

	public boolean hasExploded() {
		return hasExploded;
	}

	public int[] getExplosionSize() {
		return explosionSize;
	}

	public boolean step(long deltaTime) {
		if (!remote || remoteActivated) timer -= deltaTime;
		if (timer <= -1000000000) {
			return true;
		} else if (timer <= 0 && !hasExploded) {
			int x = Math.round(this.x - 0.5F);
			int y = Math.round(this.y - 0.5F);

			explosionSize[0] = y;
			explosionSize[1] = y;
			explosionSize[2] = x;
			explosionSize[3] = x;

			destroy(x, y);
			int[][] explodeDirections = {{0, 1}, {1, 0}, {0, -1}, {-1, 0}};
			for (int d = 0; d < explodeDirections.length; d++) {
				int destroyX = x, destroyY = y;
				for (int i = 0; i < power; i++) {
					destroyX += explodeDirections[d][0];
					destroyY += explodeDirections[d][1];

					int next = destroy(destroyX, destroyY);
					if (next != 1) {
						if (destroyY > explosionSize[0]) explosionSize[0] = destroyY;
						if (destroyY < explosionSize[1]) explosionSize[1] = destroyY;
						if (destroyX < explosionSize[2]) explosionSize[2] = destroyX;
						if (destroyX > explosionSize[3]) explosionSize[3] = destroyX;
						if (next == 0 && !piercing) break;
					} else {
						break;
					}
				}
			}

			float rect1X = (explosionSize[2] + explosionSize[3] + 1) * 0.5F;
			float rect1Y = y + 0.5F;
			float width1 = explosionSize[3] + explosionSize[2];
			float rect2X = x + 0.5F;
			float rect2Y = (explosionSize[0] + explosionSize[1] + 1) * 0.5F;
			float height2 = explosionSize[3] + explosionSize[2];

			for (Character character : Game.getCharacters()) {
				if (Game.collides(rect1X, rect1Y, width1, 1, character.getX(), character.getY(), character.getRadius()) || Game.collides(rect2X, rect2Y, 1, height2, character.getX(), character.getY(), character.getRadius())) {
					Log.print(owner.getColor().getRGB() + " killed " + character.getColor().getRGB());
					Log.print("Victim (" + character.getX() + ", " + character.getY() + ")");
					character.kill();
				}
			}

			for (Bomb bomb : Game.getBombs()) {
				if (Game.collides(rect1X, rect1Y, width1, 1, bomb.getX(), bomb.getY(), 0.5F) || Game.collides(rect2X, rect2Y, 1, height2, bomb.getX(), bomb.getY(), 0.5F)) bomb.explode();
			}

			owner.addBomb();
			hasExploded = true;
		}
		return false;
	}

	private int destroy(int x, int y) {
		Tile tile = Game.getMap()[x][y];
		if (tile != null) {
			if (tile instanceof Powerup) {
				Game.getMap()[x][y] = null;
			} else {
				if (power >= tile.getArmor() && tile.getArmor() > 0) { // TODO: Better armor mechanics
					Game.getMap()[x][y] = new FuturePowerup(this.owner);
					return 0; // Tile hit and destroyed
				}
				if (tile.getArmor() == 0 && piercing) return 0; // Do not stop at (but also do not destroy) temporary indestructibles if the bomb is piercing
				return 1; // Tile hit and not destroyed
			}
		}

		return -1; // No tiles hit
	}

	public void explode() {
		if (remote && !remoteActivated) owner.detonateRemoteBomb(this);
		step(timer);
	}

	public void detonate() {
		remoteActivated = true;
	}
}
