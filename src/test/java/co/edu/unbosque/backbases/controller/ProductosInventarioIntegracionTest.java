package co.edu.unbosque.backbases.controller;

import static co.edu.unbosque.backbases.security.ClaveSeguridadInterceptor.ENCABEZADO;
import static co.edu.unbosque.backbases.security.ComoEmpleado.CLAVE_ADMINISTRADOR;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
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
@ComoEmpleado(id = 1)
@EnabledIf("co.edu.unbosque.backbases.BaseDeDatos#disponible")
class ProductosInventarioIntegracionTest {

	@Autowired
	private MockMvc mvc;

	@Autowired
	private JdbcTemplate jdbc;

	private int cantidad(int idSucursal, int idProducto) {
		return jdbc.queryForObject("SELECT CANTIDAD_DISPONIBLE FROM INVENTARIO WHERE ID_SUCURSAL = ? AND ID_PRODUCTO = ?",
				Integer.class, idSucursal, idProducto);
	}

	@Test
	void elStockDelProductoEsLaSumaDeSuInventario() throws Exception {
		Integer suma = jdbc.queryForObject("SELECT SUM(CANTIDAD_DISPONIBLE) FROM INVENTARIO WHERE ID_PRODUCTO = 1", Integer.class);

		mvc.perform(get("/api/productos/1"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.codigo").value("HM-0001"))
				.andExpect(jsonPath("$.stock").value(suma))
				.andExpect(jsonPath("$.nombreMarca").value("Stanley"));
		mvc.perform(get("/api/productos/1/inventario"))
				.andExpect(jsonPath("$.length()").value(10))
				.andExpect(jsonPath("$[*].nombreSucursal").value(hasItem("Sede Laureles")));
	}

	@Test
	void buscarPorCategoriaPrincipalIncluyeSusSubcategorias() throws Exception {
		mvc.perform(get("/api/productos").param("idCategoria", "1"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(5));
		mvc.perform(get("/api/productos").param("q", "taladro"))
				.andExpect(jsonPath("$.length()").value(3))
				.andExpect(jsonPath("$[*].nombreCategoria").value(everyItem(is("Taladros"))));
	}

	@Test
	void creaUnProductoYLoRetiraCuandoNoTieneMovimientos() throws Exception {
		String respuesta = mvc.perform(post("/api/productos").contentType(MediaType.APPLICATION_JSON).content("""
				{"codigo":"HM-0099","nombreProducto":"Mazo de goma","costoCompra":18000,"precioBaseVenta":27900,"idCategoria":2,"idMarca":2}
				"""))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.stock").value(0))
				.andReturn().getResponse().getContentAsString();
		String id = respuesta.replaceAll(".*\"idProducto\":(\\d+).*", "$1");

		mvc.perform(post("/api/productos").contentType(MediaType.APPLICATION_JSON).content("""
				{"codigo":"hm-0099","nombreProducto":"Otro","costoCompra":1,"precioBaseVenta":2,"idCategoria":2,"idMarca":2}
				"""))
				.andExpect(status().isConflict());
		mvc.perform(delete("/api/productos/" + id).header(ENCABEZADO, CLAVE_ADMINISTRADOR)).andExpect(status().isNoContent());
		mvc.perform(delete("/api/productos/1").header(ENCABEZADO, CLAVE_ADMINISTRADOR)).andExpect(status().isConflict());
	}

	@Test
	void laEntradaSumaYLaPrimeraEntradaCreaElRegistro() throws Exception {
		int antes = cantidad(1, 1);

		mvc.perform(post("/api/inventario/1/1/entradas").contentType(MediaType.APPLICATION_JSON).content("{\"unidades\":7}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.cantidadDisponible").value(antes + 7));

		mvc.perform(post("/api/inventario/2/13/entradas").contentType(MediaType.APPLICATION_JSON).content("{\"unidades\":4}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.codigo").value("REGLA_NEGOCIO"));
		mvc.perform(post("/api/inventario/2/13/entradas").contentType(MediaType.APPLICATION_JSON)
				.content("{\"unidades\":4,\"ubicacionBodega\":\"Pasillo 9, estante A\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.cantidadDisponible").value(4))
				.andExpect(jsonPath("$.ubicacionBodega").value("Pasillo 9, estante A"));
	}

	@Test
	void ajustarYRetirarDelInventario() throws Exception {
		mvc.perform(delete("/api/inventario/1/1").header(ENCABEZADO, CLAVE_ADMINISTRADOR)).andExpect(status().isBadRequest());
		mvc.perform(put("/api/inventario/1/1").contentType(MediaType.APPLICATION_JSON)
				.content("{\"cantidadDisponible\":0,\"ubicacionBodega\":\"Pasillo 1, estante A\"}"))
				.andExpect(status().isOk());
		mvc.perform(put("/api/inventario/1/1").contentType(MediaType.APPLICATION_JSON)
				.content("{\"cantidadDisponible\":-3,\"ubicacionBodega\":\"x\"}"))
				.andExpect(status().isBadRequest());
	}

	@Test
	void unAdministradorNoDirigeDosSedes() throws Exception {
		mvc.perform(put("/api/sucursales/2").contentType(MediaType.APPLICATION_JSON).content("""
				{"nombreSucursal":"Sede Norte","ciudad":"Bogotá","direccion":"Autopista Norte # 145-30","telefono":"601 615 2290","idAdministrador":1}
				"""))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.mensaje").value("Ese administrador ya dirige la sucursal \"Sede Centro\"."));
		mvc.perform(get("/api/sucursales"))
				.andExpect(jsonPath("$.length()").value(10))
				.andExpect(jsonPath("$[?(@.nombreSucursal == 'Sede Laureles')].nombreAdministrador").value(hasItem("Sofía Restrepo")));
	}

	@Test
	@ComoEmpleado(id = 3, rol = Rol.VENDEDOR, idSucursal = 1)
	void elVendedorConsultaElInventarioDeTodasLasSedesPeroNoLoModifica() throws Exception {
		mvc.perform(get("/api/sucursales/2/inventario")).andExpect(status().isOk());
		mvc.perform(get("/api/inventario").param("q", "HM-0001"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(10));
		mvc.perform(put("/api/inventario/1/1").contentType(MediaType.APPLICATION_JSON)
				.content("{\"cantidadDisponible\":1,\"ubicacionBodega\":\"x\"}"))
				.andExpect(status().isForbidden());
		mvc.perform(post("/api/inventario/1/1/entradas").contentType(MediaType.APPLICATION_JSON).content("{\"unidades\":1}"))
				.andExpect(status().isForbidden());
	}
}
