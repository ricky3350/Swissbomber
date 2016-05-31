package swissbomber.server;

import java.awt.Color;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import swissbomber.Tile;

public class Main {

	private static final DateFormat date = new SimpleDateFormat("yyyyMMdd");

	public static void main(String[] args) {
		String date = Main.date.format(new Date());
		try {
			Files.createDirectories(Paths.get("server/logs"));
			
			int i;
			for (i = 0; Files.exists(Paths.get("server/logs/" + date + i + ".log")); i++);

			Log.initFile(Paths.get("server/logs/" + date + i + ".log"));
		} catch (IOException e) {}

		try {
			Network.openServer();
		} catch (IOException e) {
			return;
		}

		Tile w = new Tile(-1, Color.GRAY);
		Tile c = new Tile(1, new Color(180, 160, 90));
		Tile n = null;
		Tile[][] grid = {
				{w,w,w,w,w,w,w,w,w,w,w,w,w},
				{w,n,n,c,c,c,c,c,c,c,n,n,w},
				{w,n,w,c,w,c,w,c,w,c,w,n,w},
				{w,c,c,c,c,c,c,c,c,c,c,c,w},
				{w,c,w,c,w,c,w,c,w,c,w,c,w},
				{w,c,c,c,c,c,c,c,c,c,c,c,w},
				{w,c,w,c,w,c,w,c,w,c,w,c,w},
				{w,c,c,c,c,c,c,c,c,c,c,c,w},
				{w,c,w,c,w,c,w,c,w,c,w,c,w},
				{w,c,c,c,c,c,c,c,c,c,c,c,w},
				{w,c,w,c,w,c,w,c,w,c,w,c,w},
				{w,c,c,c,c,c,c,c,c,c,c,c,w},
				{w,n,w,c,w,c,w,c,w,c,w,n,w},
				{w,n,n,c,c,c,c,c,c,c,n,n,w},
				{w,w,w,w,w,w,w,w,w,w,w,w,w}
		};

		Game.init(grid, 2);
	}

}
