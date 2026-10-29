package co.edu.unbosque.backbases.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
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

import co.edu.unbosque.backbases.dto.LineaVentaEntradaDTO;
import co.edu.unbosque.backbases.dto.Rol;
import co.edu.unbosque.backbases.dto.VentaEntradaDTO;
import co.edu.unbosque.backbases.exception.ConflictoException;
import co.edu.unbosque.backbases.security.UsuarioAutenticado;

@SpringBootTest
@EnabledIf("co.edu.unbosque.backbases.BaseDeDatos#disponible")
class VentaConcurrenteIntegracionTest {

	private static final int SEDE_NORTE = 2;
	private static final int MARTILLO = 1;
	private static final int TALADRO = 7;

	@Autowired
	private VentaService ventaService;

	@Autowired
	private JdbcTemplate jdbc;

	private int ultimaVenta;
	private int martillos;
	private int taladros;

	private int cantidad(int idProducto) {
		return jdbc.queryForObject("SELECT CANTIDAD_DISPONIBLE FROM INVENTARIO WHERE ID_SUCURSAL = ? AND ID_PRODUCTO = ?",
				Integer.class, SEDE_NORTE, idProducto);
	}

	private int ventas() {
		return jdbc.queryForObject("SELECT COUNT(*) FROM VENTA", Integer.class);
	}

	private static UsuarioAutenticado vendedor(int id) {
		return new UsuarioAutenticado(id, "vendedor@x.co", Rol.VENDEDOR, SEDE_NORTE, null);
	}

	@BeforeEach
	void recordarEstado() {
		ultimaVenta = jdbc.queryForObject("SELECT COALESCE(MAX(ID_VENTA), 0) FROM VENTA", Integer.class);
		martillos = cantidad(MARTILLO);
		taladros = cantidad(TALADRO);
	}

	@AfterEach
	void restaurarEstado() {
		jdbc.update("DELETE FROM VENTA WHERE ID_VENTA > ?", ultimaVenta);
		jdbc.update("UPDATE INVENTARIO SET CANTIDAD_DISPONIBLE = ? WHERE ID_SUCURSAL = ? AND ID_PRODUCTO = ?", martillos, SEDE_NORTE, MARTILLO);
		jdbc.update("UPDATE INVENTARIO SET CANTIDAD_DISPONIBLE = ? WHERE ID_SUCURSAL = ? AND ID_PRODUCTO = ?", taladros, SEDE_NORTE, TALADRO);
		jdbc.execute("ALTER TABLE VENTA AUTO_INCREMENT = 1");
	}

	@Test
	void unaVentaQueFallaNoDejaRastro() {
		int ventasAntes = ventas();
		VentaEntradaDTO entrada = new VentaEntradaDTO(null, 1,
				List.of(new LineaVentaEntradaDTO(MARTILLO, 1), new LineaVentaEntradaDTO(TALADRO, taladros + 1)));

		assertThatThrownBy(() -> ventaService.registrar(entrada, vendedor(4))).isInstanceOf(ConflictoException.class);

		assertThat(ventas()).isEqualTo(ventasAntes);
		assertThat(cantidad(MARTILLO)).isEqualTo(martillos);
		assertThat(cantidad(TALADRO)).isEqualTo(taladros);
	}

	@Test
	void tresVendedoresPorLasUltimasUnidadesSoloUnoLasVende() throws Exception {
		jdbc.update("UPDATE INVENTARIO SET CANTIDAD_DISPONIBLE = 3 WHERE ID_SUCURSAL = ? AND ID_PRODUCTO = ?", SEDE_NORTE, MARTILLO);
		VentaEntradaDTO dosMartillos = new VentaEntradaDTO(null, 1, List.of(new LineaVentaEntradaDTO(MARTILLO, 2)));
		ExecutorService hilos = Executors.newFixedThreadPool(3);
		int exitosas = 0;
		int rechazadas = 0;
		try {
			List<Callable<Object>> tareas = new ArrayList<>();
			for (int idVendedor : new int[] { 4, 5, 4 }) {
				tareas.add(() -> ventaService.registrar(dosMartillos, vendedor(idVendedor)));
			}
			for (Future<Object> resultado : hilos.invokeAll(tareas)) {
				try {
					resultado.get();
					exitosas++;
				} catch (ExecutionException e) {
					assertThat(e.getCause()).isInstanceOf(ConflictoException.class);
					rechazadas++;
				}
			}
		} finally {
			hilos.shutdown();
		}

		assertThat(exitosas).isEqualTo(1);
		assertThat(rechazadas).isEqualTo(2);
		assertThat(cantidad(MARTILLO)).isEqualTo(1);
	}
}
