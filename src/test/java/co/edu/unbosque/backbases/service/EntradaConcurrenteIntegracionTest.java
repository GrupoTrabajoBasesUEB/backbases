package co.edu.unbosque.backbases.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import co.edu.unbosque.backbases.dto.EntradaMercanciaDTO;

@SpringBootTest
@EnabledIf("co.edu.unbosque.backbases.BaseDeDatos#disponible")
class EntradaConcurrenteIntegracionTest {

	private static final int SUCURSAL = 3;
	private static final int PRODUCTO = 10;
	private static final int HILOS = 12;

	@Autowired
	private InventarioService inventarioService;

	@Autowired
	private JdbcTemplate jdbc;

	private int cantidadInicial;

	private int cantidad() {
		return jdbc.queryForObject("SELECT CANTIDAD_DISPONIBLE FROM INVENTARIO WHERE ID_SUCURSAL = ? AND ID_PRODUCTO = ?",
				Integer.class, SUCURSAL, PRODUCTO);
	}

	@BeforeEach
	void guardarCantidad() {
		cantidadInicial = cantidad();
	}

	@AfterEach
	void restaurarCantidad() {
		jdbc.update("UPDATE INVENTARIO SET CANTIDAD_DISPONIBLE = ? WHERE ID_SUCURSAL = ? AND ID_PRODUCTO = ?",
				cantidadInicial, SUCURSAL, PRODUCTO);
	}

	@Test
	void entradasSimultaneasNoSePisan() throws Exception {
		ExecutorService hilos = Executors.newFixedThreadPool(HILOS);
		try {
			List<Callable<Object>> tareas = new ArrayList<>();
			for (int i = 0; i < HILOS; i++) {
				tareas.add(() -> inventarioService.registrarEntrada(SUCURSAL, PRODUCTO, new EntradaMercanciaDTO(5, null)));
			}
			for (Future<Object> resultado : hilos.invokeAll(tareas)) {
				resultado.get();
			}
		} finally {
			hilos.shutdown();
		}

		assertThat(cantidad()).isEqualTo(cantidadInicial + HILOS * 5);
	}
}
