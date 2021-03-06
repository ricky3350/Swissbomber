package swissbomber;

import java.awt.Color;

public class Tile {

	public static final Tile ASH = new Tile(0, null); // Remains of a destroyed tile, gets destroyed before being painted (used to prevent piercing when a bomb triggers another bomb)

	private int armor;
	private Color color;

	public Tile(int armor, Color color) {
		this.armor = armor;
		this.color = color;
	}

	public int getArmor() {
		return armor;
	}

	public Color getColor() {
		return color;
	}
}
