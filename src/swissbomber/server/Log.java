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

	/**
	 * <ul>
	 * <b><i>initFile</i></b><br>
	 * <br>
	 * <code>&nbsp;public static void initFile({@link Path} path) throws IOException</code><br>
	 * <br>
	 * Opens the file at the given path for logging. Only one file can be open for logging at a time, so this method tries to close the previous file if it exists.
	 * @param path - The path to open.
	 * @throws IOException If there was an IOException while opening the file.
	 *         </ul>
	 */
	public static void initFile(Path path) throws IOException {
		try {
			if (file != null) file.close();
		} catch (Exception e) {}
		
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
