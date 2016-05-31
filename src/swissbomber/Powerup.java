package swissbomber;

import java.awt.Color;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Powerup extends Tile {

	/**
	 * The Powerup representing <code>null</code> values, because {@link ArrayDeque}s do not support null elements
	 */
	public static final Powerup NULL = new Powerup();
	
	public static final Powerup[] POWERUPS = {
			new Powerup(1, Color.RED, 10, "power+"),
			new Powerup(1, Color.GREEN, 10, "speed+"),
			new Powerup(1, Color.BLUE, 10, "bombs+"),
			new Powerup(1, Color.GRAY, 2, "pierce"),
			new Powerup(1, Color.CYAN, 2, "remote"),
			//new Powerup(1, Color.ORANGE, 2, "kick") // Kick still not implemented
	};
	public final int rarity;
	public final String effect;
	public final float radius;

	private static final List<Powerup> relativeFreq = new ArrayList<>();

	private Powerup() {
		super(0, new Color(0));
		
		this.rarity = 0;
		this.effect = null;
		this.radius = 0;
	}
	
	private Powerup(int armor, Color color, int rarity, String effect) {
		super(armor, color);

		this.rarity = rarity;
		this.effect = effect;
		this.radius = 0.3f;
	}

	/**
	 * <ul>
	 * <b><i>randomPowerupOrder</i></b><br>
	 * <br>
	 * <code>&nbsp;public static List&lt;Powerup&gt; randomPowerupOrder()</code><br>
	 * <br>
	 * @return a list of powerups at their relative frequencies (including <code>null</code>s) in a random order.
	 *         </ul>
	 */
	public static List<Powerup> randomPowerupOrder() {
		if (relativeFreq.size() <= 0) {
			int res = POWERUPS[0].rarity;
			for (int i = 1; i < POWERUPS.length; i++) {
				int a = res, b = POWERUPS[i].rarity;
				while (b > 0) {
					int t = b;
					b = a % b;
					a = t;
				}
				res = a;
			}

			for (Powerup p : POWERUPS) {
				for (int i = 0; i < p.rarity / res; i++) {
					relativeFreq.add(p);
				}
			}
			final int n = relativeFreq.size() * 3;
			for (int i = 0; i < n; i++) {
				relativeFreq.add(Powerup.NULL);
			}
		}

		List<Powerup> ret = new ArrayList<>(relativeFreq);
		Collections.shuffle(ret);
		return ret;
	}

	public static int getTotalRarity() {
		int totalRarity = 0;

		for (Powerup powerup : POWERUPS) {
			totalRarity += powerup.rarity;
		}

		return totalRarity;
	}

}
