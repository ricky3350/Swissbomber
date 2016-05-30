package swissbomber.server;

import java.awt.Color;

import swissbomber.Tile;

public class FuturePowerup extends Tile {

	public final Character character;

	public FuturePowerup(Character character) {
		super(0, Color.CYAN);

		this.character = character;
	}

}
