package swissbomber.server;

import java.awt.Color;

import swissbomber.Tile;

/**
 * Remains of a destroyed tile. Gets turned into a random power-up before being painted.
 * Used to prevent piercing when a bomb triggers another bomb
 * @see {@link Powerup}
 */
public class FuturePowerup extends Tile {

	public final Character character;

	public FuturePowerup(Character character) {
		super(0, Color.CYAN);

		this.character = character;
	}

}
