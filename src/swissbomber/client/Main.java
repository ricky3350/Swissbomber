package swissbomber.client;

import java.io.IOException;

public class Main {

	public static void main(String[] args) {
		if (!Network.getIPAndConnect()) return;

		Bomb.loadAnimations();
		try {
			new Window(new Game());
		} catch (IOException e) {}
	}

}
