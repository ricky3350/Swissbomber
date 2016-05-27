package swissbomber;

import java.awt.Color;

public class Powerup extends Tile {

	public static final Powerup[] POWERUPS = {
			new Powerup(1, Color.RED, 10, "power+"),
			new Powerup(1, Color.GREEN, 10, "speed+"),
			new Powerup(1, Color.BLUE, 10, "bombs+"),
			new Powerup(1, Color.GRAY, 2, "pierce"),
			new Powerup(1, Color.CYAN, 2, "remote"),
			//new Powerup(1, Color.ORANGE, 2, "kick") // Kick still not implemented
			};
	public final int RARITY;
	public final String EFFECT;
	public final float RADIUS;
	
	Powerup(int armor, Color color, int rarity, String effect) {
		super(armor, color);
		
		RARITY = rarity;
		EFFECT = effect;
		RADIUS = 0.3f;
	}
	
	public static int getTotalRarity() {
		int totalRarity = 0;
		
		for (Powerup powerup : POWERUPS) {
			totalRarity += powerup.RARITY;
		}
		
		return totalRarity;
	}

}
