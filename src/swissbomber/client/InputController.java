package swissbomber.client;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;

public class InputController extends KeyAdapter implements Controller {

	Character character;
	private int[] keyCodes; // upKC, downKC, leftKC, rightKC, bombKC, specialKC;
	private boolean[] keyPressed = new boolean[6];

	public InputController(Character character, int[] keyCodes) {
		this.character = character;
		this.keyCodes = keyCodes;
	}

	@Override
	public void step(Game game, long deltaTime) {
		if (!character.isAlive()) return;

		int horizontal = (keyPressed[2] ? -1 : 0) + (keyPressed[3] ? 1 : 0);
		int vertical = (keyPressed[0] ? 1 : 0) + (keyPressed[1] ? -1 : 0);

		if (horizontal == 0 && vertical == 0) {
			return;
		}

		double angle;
		if (horizontal == 1 && vertical == -1) {
			angle = 315;
		} else {
			angle = (horizontal == -1 ? 180 : 0) + (vertical == -1 ? 270 : (vertical == 1 ? 90 : 0));
			if (horizontal != 0 && vertical != 0) {
				angle /= 2;
			}
		}

		character.move(game, angle, deltaTime);
	}

	@Override
	public void keyPressed(KeyEvent e) {
		for (int i = 0; i < keyCodes.length; i++) {
			if (e.getKeyCode() == keyCodes[i] && !keyPressed[i]) {
				keyPressed[i] = true;
				try {
					Network.writeKey(i, true);
				} catch (IOException exception) {}
			}
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		for (int i = 0; i < keyCodes.length; i++) {
			if (e.getKeyCode() == keyCodes[i] && keyPressed[i]) {
				keyPressed[i] = false;
				try {
					Network.writeKey(i, false);
				} catch (IOException exception) {}
			}
		}
	}

}
