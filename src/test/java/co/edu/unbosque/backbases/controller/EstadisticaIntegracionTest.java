package co.edu.unbosque.backbases.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import com.jayway.jsonpath.JsonPath;

import co.edu.unbosque.backbases.dto.Rol;
import co.edu.unbosque.backbases.security.ComoEmpleado;
import co.edu.unbosque.backbases.service.VentaService;

@SpringBootTest
@AutoConfigureMockMvc
@ComoEmpleado(id = 1)
@EnabledIf("co.edu.unbosque.backbases.BaseDeDatos#disponible")
class EstadisticaIntegracionTest {

	@Autowired
	private MockMvc mvc;

	@Autowired
	private JdbcTemplate jdbc;

	private LocalDate desde;
	private LocalDate hasta;

	@BeforeEach
	void periodoConTodasLasVentas() {
		hasta = LocalDate.now(VentaService.ZONA);
		desde = jdbc.queryForObject("SELECT MIN(FECHA) FROM VENTA", LocalDate.class);
	}

	private MockHttpServletRequestBuilder consulta(String ruta) {
		return get("/api/estadisticas/" + ruta).param("desde", desde.toString()).param("hasta", hasta.toString());
	}

	private String json(MockHttpServletRequestBuilder peticion) throws Exception {
		return mvc.perform(peticion).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
	}

	private static BigDecimal suma(String json, String ruta) {
		List<Number> valores = JsonPath.read(json, ruta);
		return valores.stream().map(n -> new BigDecimal(n.toString())).reduce(BigDecimal.ZERO, BigDecimal::add);
	}

	private int filas(String tabla) {
		return jdbc.queryForObject("SELECT COUNT(*) FROM " + tabla, Integer.class);
	}

	private BigDecimal totalVendido() {
		return jdbc.queryForObject("SELECT SUM(MONTO_TOTAL) FROM V_VENTA_TOTAL", BigDecimal.class);
	}

	@Test
	void elResumenCoincideConLaVistaYConDetalleVenta() throws Exception {
		Integer ventas = jdbc.queryForObject("SELECT COUNT(*) FROM VENTA", Integer.class);
		Integer unidades = jdbc.queryForObject("SELECT SUM(CANTIDAD) FROM DETALLE_VENTA", Integer.class);

		String json = json(consulta("resumen"));

		assertThat(new BigDecimal(JsonPath.read(json, "$.actual.total").toString())).isEqualByComparingTo(totalVendido());
		assertThat((Integer) JsonPath.read(json, "$.actual.cantidad")).isEqualTo(ventas);
		assertThat((Integer) JsonPath.read(json, "$.actual.unidades")).isEqualTo(unidades);
		long dias = ChronoUnit.DAYS.between(desde, hasta) + 1;
		assertThat((String) JsonPath.read(json, "$.hastaAnterior")).isEqualTo(desde.minusDays(1).toString());
		assertThat((String) JsonPath.read(json, "$.desdeAnterior")).isEqualTo(desde.minusDays(dias).toString());
	}

	@Test
	void ventasPorDiaIncluyeLosDiasSinVentas() throws Exception {
		desde = desde.minusDays(2);
		String json = json(consulta("ventas-por-dia"));

		List<String> fechas = JsonPath.read(json, "$[*].fecha");
		assertThat(fechas).hasSize((int) ChronoUnit.DAYS.between(desde, hasta) + 1);
		assertThat(fechas.getFirst()).isEqualTo(desde.toString());
		assertThat(suma(json, "$[*].total")).isEqualByComparingTo(totalVendido());
		List<Integer> cantidades = JsonPath.read(json, "$[*].cantidad");
		assertThat(cantidades).contains(0);
	}

	@Test
	void lasAgrupacionesSumanElTotal() throws Exception {
		String sucursales = json(consulta("sucursales"));
		String metodos = json(consulta("por-metodo-pago"));
		String vendedores = json(consulta("comisiones"));

		assertThat(JsonPath.<List<?>>read(sucursales, "$")).hasSize(filas("SUCURSAL"));
		assertThat(JsonPath.<List<?>>read(metodos, "$")).hasSize(filas("METODO_PAGO"));
		assertThat(JsonPath.<List<?>>read(vendedores, "$")).hasSize(filas("VENDEDOR"));
		assertThat(suma(sucursales, "$[*].total")).isEqualByComparingTo(totalVendido());
		assertThat(suma(metodos, "$[*].total")).isEqualByComparingTo(totalVendido());
		assertThat(suma(vendedores, "$[*].total")).isEqualByComparingTo(totalVendido());
	}

	@Test
	void elRendimientoIncluyeTodosLosProductos() throws Exception {
		String rendimiento = json(consulta("rendimiento-productos"));
		assertThat(JsonPath.<List<?>>read(rendimiento, "$")).hasSize(filas("PRODUCTO"));
		assertThat(suma(rendimiento, "$[*].total")).isEqualByComparingTo(totalVendido());
	}

	@Test
	void ventasDeUnProductoCoincidenConSusLineas() throws Exception {
		Integer unidades = jdbc.queryForObject("SELECT COALESCE(SUM(CANTIDAD), 0) FROM DETALLE_VENTA WHERE ID_PRODUCTO = 10", Integer.class);

		mvc.perform(consulta("productos/10"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.unidades").value(unidades));
		mvc.perform(consulta("productos/999")).andExpect(status().isNotFound());
	}

	@Test
	void comparativoDeSucursalesYComisiones() throws Exception {
		Map<String, Object> centro = jdbc.queryForMap("""
				SELECT SUM(i.CANTIDAD_DISPONIBLE * p.COSTO_COMPRA) AS CAPITAL, SUM(i.CANTIDAD_DISPONIBLE <= 5) AS BAJO
				FROM INVENTARIO i JOIN PRODUCTO p ON p.ID_PRODUCTO = i.ID_PRODUCTO WHERE i.ID_SUCURSAL = 1
				""");
		String sucursales = json(consulta("sucursales").param("idSucursal", "1"));
		assertThat(new BigDecimal(JsonPath.read(sucursales, "$[0].capitalInventario").toString()))
				.isEqualByComparingTo(new BigDecimal(centro.get("CAPITAL").toString()));
		assertThat(((Number) JsonPath.read(sucursales, "$[0].productosStockBajo")).longValue())
				.isEqualTo(((Number) centro.get("BAJO")).longValue());

		String comisiones = json(consulta("comisiones"));
		List<Number> totales = JsonPath.read(comisiones, "$[*].total");
		List<Number> tasas = JsonPath.read(comisiones, "$[*].tasa");
		List<Number> valores = JsonPath.read(comisiones, "$[*].comision");
		assertThat(totales).hasSize(filas("VENDEDOR"));
		for (int i = 0; i < totales.size(); i++) {
			assertThat(new BigDecimal(valores.get(i).toString()))
					.isEqualByComparingTo(new BigDecimal(totales.get(i).toString()).multiply(new BigDecimal(tasas.get(i).toString())).setScale(2, RoundingMode.HALF_UP));
		}
	}

	@Test
	void elPeriodoSeValida() throws Exception {
		mvc.perform(get("/api/estadisticas/resumen").param("desde", "2026-10-10").param("hasta", "2026-10-01"))
				.andExpect(status().isBadRequest());
		mvc.perform(get("/api/estadisticas/resumen").param("desde", "2024-01-01").param("hasta", "2026-01-01"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.mensaje").value("El periodo no puede superar 366 días."));
		mvc.perform(get("/api/estadisticas/ventas-por-dia"))
				.andExpect(jsonPath("$.length()").value(30));
	}

	@Test
	@ComoEmpleado(id = 6, rol = Rol.VENDEDOR, idSucursal = 3)
	void elVendedorSoloVeSuSucursalYSuComision() throws Exception {
		mvc.perform(consulta("sucursales"))
				.andExpect(jsonPath("$.length()").value(1))
				.andExpect(jsonPath("$[0].nombreSucursal").value("Sede Laureles"));
		mvc.perform(consulta("resumen").param("idSucursal", "1")).andExpect(status().isForbidden());
		mvc.perform(consulta("comisiones"))
				.andExpect(jsonPath("$[*].idEmpleado").value(everyItem(is(6))));
		mvc.perform(consulta("comisiones").param("idEmpleado", "3")).andExpect(status().isForbidden());
	}
}
