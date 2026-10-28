package co.edu.unbosque.backbases.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.condition.EnabledIf;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.backbases.dto.Rol;
import co.edu.unbosque.backbases.security.ComoEmpleado;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ComoEmpleado(id = 3, rol = Rol.VENDEDOR, idSucursal = 1)
@EnabledIf("co.edu.unbosque.backbases.BaseDeDatos#disponible")
class VentaIntegracionTest {

	@Autowired
	private MockMvc mvc;

	@Autowired
	private JdbcTemplate jdbc;

	@AfterAll
	void reiniciarConsecutivo() {
		jdbc.execute("ALTER TABLE VENTA AUTO_INCREMENT = 1");
	}

	private int cantidad(int idSucursal, int idProducto) {
		return jdbc.queryForObject("SELECT CANTIDAD_DISPONIBLE FROM INVENTARIO WHERE ID_SUCURSAL = ? AND ID_PRODUCTO = ?",
				Integer.class, idSucursal, idProducto);
	}

	@Test
	void registraLaVentaDescuentaElStockYNumeraLaFactura() throws Exception {
		int martillos = cantidad(1, 1);
		int codos = cantidad(1, 10);

		String respuesta = mvc.perform(post("/api/ventas").contentType(MediaType.APPLICATION_JSON).content("""
				{"idCliente":1,"idMetodoPago":2,"lineas":[{"idProducto":1,"cantidad":1},{"idProducto":10,"cantidad":3}]}
				"""))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.numeroFactura").value(startsWith("FV-")))
				.andExpect(jsonPath("$.nombreSucursal").value("Sede Centro"))
				.andExpect(jsonPath("$.nombreVendedor").value("Camila Rojas"))
				.andExpect(jsonPath("$.nombreCliente").value("Carlos Medina"))
				.andExpect(jsonPath("$.montoTotal").value(42900 + 3 * 1200))
				.andExpect(jsonPath("$.lineas.length()").value(2))
				.andReturn().getResponse().getContentAsString();

		assertThat(cantidad(1, 1)).isEqualTo(martillos - 1);
		assertThat(cantidad(1, 10)).isEqualTo(codos - 3);
		String id = respuesta.replaceAll(".*\"idVenta\":(\\d+).*", "$1");
		assertThat(respuesta).contains("\"numeroFactura\":\"FV-" + String.format("%06d", Integer.valueOf(id)) + "\"");
		mvc.perform(get("/api/ventas/" + id))
				.andExpect(jsonPath("$.montoTotal").value(46500.0))
				.andExpect(jsonPath("$.lineas[*].precioUnitario").exists());
	}

	@Test
	void sinClienteLaVentaEsAConsumidorFinal() throws Exception {
		mvc.perform(post("/api/ventas").contentType(MediaType.APPLICATION_JSON)
				.content("{\"idMetodoPago\":1,\"lineas\":[{\"idProducto\":10,\"cantidad\":2}]}"))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.documentoCliente").value("222222222222"))
				.andExpect(jsonPath("$.nombreCliente").value("Consumidor final"));
	}

	@Test
	void sinStockDevuelve409ConElDetalle() throws Exception {
		int disponibles = cantidad(1, 7);

		mvc.perform(post("/api/ventas").contentType(MediaType.APPLICATION_JSON)
				.content("{\"idMetodoPago\":1,\"lineas\":[{\"idProducto\":7,\"cantidad\":" + (disponibles + 1) + "}]}"))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.mensaje").value("Stock insuficiente de HE-0101 (Taladro atornillador inalámbrico 20 V): hay "
						+ disponibles + " y se piden " + (disponibles + 1) + "."));
	}

	@Test
	void datosInvalidosNoLleganALaBase() throws Exception {
		mvc.perform(post("/api/ventas").contentType(MediaType.APPLICATION_JSON).content("{\"lineas\":[]}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.campos.idMetodoPago").exists())
				.andExpect(jsonPath("$.campos.lineas").value("La venta necesita al menos un producto."));
		mvc.perform(post("/api/ventas").contentType(MediaType.APPLICATION_JSON)
				.content("{\"idMetodoPago\":1,\"lineas\":[{\"idProducto\":1,\"cantidad\":0}]}"))
				.andExpect(status().isBadRequest());
	}

	@Test
	void elVendedorSoloVeLasVentasDeSuSede() throws Exception {
		mvc.perform(get("/api/ventas").param("tamano", "100"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.contenido[*].idSucursal").value(everyItem(is(1))));
		mvc.perform(get("/api/ventas").param("idSucursal", "2")).andExpect(status().isForbidden());
		Integer ventaDeOtraSede = jdbc.queryForObject("SELECT MIN(ID_VENTA) FROM VENTA WHERE ID_SUCURSAL = 3", Integer.class);
		mvc.perform(get("/api/ventas/" + ventaDeOtraSede)).andExpect(status().isForbidden());
	}

	@Test
	@ComoEmpleado(id = 1)
	void elAdministradorFiltraYPaginaPeroNoVende() throws Exception {
		Integer efectivo = jdbc.queryForObject("SELECT COUNT(*) FROM VENTA WHERE ID_METODO_PAGO = 1", Integer.class);

		mvc.perform(get("/api/ventas").param("idMetodoPago", "1").param("tamano", "5").param("pagina", "0"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.totalElementos").value(efectivo))
				.andExpect(jsonPath("$.contenido.length()").value(Math.min(5, efectivo)))
				.andExpect(jsonPath("$.contenido[*].tipoMetodoPago").value(everyItem(is("Efectivo"))));
		mvc.perform(get("/api/ventas").param("desde", "2026-12-31").param("hasta", "2026-01-01"))
				.andExpect(status().isBadRequest());
		mvc.perform(post("/api/ventas").contentType(MediaType.APPLICATION_JSON)
				.content("{\"idMetodoPago\":1,\"lineas\":[{\"idProducto\":1,\"cantidad\":1}]}"))
				.andExpect(status().isForbidden());
	}
}
