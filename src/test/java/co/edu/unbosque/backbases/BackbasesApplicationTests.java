package co.edu.unbosque.backbases;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@EnabledIf("co.edu.unbosque.backbases.BaseDeDatos#disponible")
class BackbasesApplicationTests {

	@Test
	void contextLoads() {
	}
}
