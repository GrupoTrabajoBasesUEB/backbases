package co.edu.unbosque.backbases;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@EnabledIf("hayBaseDeDatos")
class BackbasesApplicationTests {

	static boolean hayBaseDeDatos() {
		return Files.exists(Path.of("application-local.properties")) || System.getenv("DB_URL") != null;
	}

	@Test
	void contextLoads() {
	}
}
