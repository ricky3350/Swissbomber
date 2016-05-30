package swissbomber.server;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Log {

	protected static final DateFormat date = new SimpleDateFormat("[HH:mm:ss.SSS]");

	private Log() {}

	private static PrintStream file;

	public static void initFile(Path path) throws IOException {
		file = new PrintStream(new FileOutputStream(path.toFile()), true);
	}

	public static void print(Object obj) {
		print(obj.toString());
	}

	public static void print(String s) {
		print("INFO", s);
	}

	public static void print(String sender, Object obj) {
		print(obj.toString());
	}

	public static void print(String sender, String text) {
		String str = date.format(new Date()) + " [" + sender + "] " + text;
		System.out.println(str);
		if (file != null) file.println(str);
	}

	public static void printf(String sender, String format, Object... args) {
		print(sender, String.format(format, args));
	}

}
