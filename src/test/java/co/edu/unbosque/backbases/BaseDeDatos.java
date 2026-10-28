package co.edu.unbosque.backbases;

import java.nio.file.Files;
import java.nio.file.Path;

public final class BaseDeDatos {

	private BaseDeDatos() {
	}

	public static boolean disponible() {
		return Files.exists(Path.of("application-local.properties")) || System.getenv("DB_URL") != null;
	}
}
