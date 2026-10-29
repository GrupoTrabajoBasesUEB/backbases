package co.edu.unbosque.backbases.controller;

import static co.edu.unbosque.backbases.security.ClaveSeguridadInterceptor.ENCABEZADO;
import static co.edu.unbosque.backbases.security.ComoEmpleado.CLAVE_ADMINISTRADOR;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.Map;

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
import co.edu.unbosque.backbases.service.VentaService;
import co.edu.unbosque.backbases.security.ComoEmpleado;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ComoEmpleado(id = 1)
@EnabledIf("co.edu.unbosque.backbases.BaseDeDatos#disponible")
class ProveedoresIntegracionTest {

	private static final String NUEVO = """
			{"nit":"901777666-4","nombreProveedor":"Ferreinsumos del Valle S.A.S.","direccionProveedor":"Calle 5 # 38-20, Cali",
			 "telefonos":["602 555 0100","315 555 0101"],"correos":["Ventas@Ferreinsumos.demo"]}
			""";

	@Autowired
	private MockMvc mvc;

	@Autowired
	private JdbcTemplate jdbc;

	@AfterAll
	void reiniciarConsecutivo() {
		jdbc.execute("ALTER TABLE EVALUACION AUTO_INCREMENT = 1");
	}

	@Test
	void proveedoresConSusTelefonosYCorreos() throws Exception {
		mvc.perform(get("/api/proveedores"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(10));
		mvc.perform(get("/api/proveedores/900215334-1"))
				.andExpect(jsonPath("$.telefonos.length()").value(2))
				.andExpect(jsonPath("$.correos").value(containsInAnyOrder("pedidos@ferreandina.demo", "cartera@ferreandina.demo")));
		mvc.perform(get("/api/proveedores").param("q", "herramientas del caribe"))
				.andExpect(jsonPath("$.length()").value(1))
				.andExpect(jsonPath("$[0].nit").value("830104552-8"));
	}

	@Test
	void registraEditaYEliminaUnProveedor() throws Exception {
		mvc.perform(post("/api/proveedores").contentType(MediaType.APPLICATION_JSON).content(NUEVO))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.correos[0]").value("ventas@ferreinsumos.demo"));
		mvc.perform(post("/api/proveedores").contentType(MediaType.APPLICATION_JSON).content(NUEVO))
				.andExpect(status().isConflict());
		mvc.perform(put("/api/proveedores/901777666-4").contentType(MediaType.APPLICATION_JSON)
				.content(NUEVO.replace("Cali", "Palmira")))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.direccionProveedor").value("Calle 5 # 38-20, Palmira"));
		mvc.perform(delete("/api/proveedores/901777666-4").header(ENCABEZADO, CLAVE_ADMINISTRADOR)).andExpect(status().isNoContent());
		mvc.perform(delete("/api/proveedores/900215334-1").header(ENCABEZADO, CLAVE_ADMINISTRADOR)).andExpect(status().isConflict());
	}

	@Test
	void elNitDebeTenerFormatoValido() throws Exception {
		mvc.perform(post("/api/proveedores").contentType(MediaType.APPLICATION_JSON).content(NUEVO.replace("901777666-4", "ABC-1")))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.campos.nit").exists());
	}

	@Test
	void elResumenDeEvaluacionesCoincideConLaBase() throws Exception {
		Map<String, Object> evaluado = jdbc.queryForMap("""
				SELECT NIT, ID_PRODUCTO, ID_SUCURSAL, COUNT(*) N FROM EVALUACION
				GROUP BY NIT, ID_PRODUCTO, ID_SUCURSAL ORDER BY N DESC, NIT LIMIT 1
				""");
		String ruta = "/api/suministros/" + evaluado.get("NIT") + "/" + evaluado.get("ID_PRODUCTO") + "/" + evaluado.get("ID_SUCURSAL");

		mvc.perform(get(ruta))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.cantidadEvaluaciones").value(((Number) evaluado.get("N")).intValue()))
				.andExpect(jsonPath("$.promedioCalificacion").isNumber());
		mvc.perform(delete(ruta).header(ENCABEZADO, CLAVE_ADMINISTRADOR)).andExpect(status().isConflict());
		mvc.perform(get("/api/suministros").param("idSucursal", "3"))
				.andExpect(jsonPath("$[*].nombreSucursal").value(everyItem(is("Sede Laureles"))));
	}

	@Test
	void creaYActualizaUnSuministro() throws Exception {
		mvc.perform(post("/api/proveedores").contentType(MediaType.APPLICATION_JSON).content(NUEVO)).andExpect(status().isCreated());

		mvc.perform(post("/api/suministros").contentType(MediaType.APPLICATION_JSON)
				.content("{\"nit\":\"901777666-4\",\"idProducto\":12,\"idSucursal\":3,\"precioSuministro\":51000,\"tiempoEntregaDias\":5}"))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.cantidadEvaluaciones").value(0));
		mvc.perform(post("/api/suministros").contentType(MediaType.APPLICATION_JSON)
				.content("{\"nit\":\"901777666-4\",\"idProducto\":12,\"idSucursal\":3,\"precioSuministro\":51000,\"tiempoEntregaDias\":5}"))
				.andExpect(status().isConflict());
		mvc.perform(put("/api/suministros/901777666-4/12/3").contentType(MediaType.APPLICATION_JSON)
				.content("{\"precioSuministro\":49500,\"tiempoEntregaDias\":4}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.tiempoEntregaDias").value(4));
		mvc.perform(delete("/api/suministros/901777666-4/12/3").header(ENCABEZADO, CLAVE_ADMINISTRADOR)).andExpect(status().isNoContent());
	}

	@Test
	void elAdministradorEvaluaUnSuministroYQuedaComoEvaluador() throws Exception {
		mvc.perform(post("/api/evaluaciones").contentType(MediaType.APPLICATION_JSON).content("""
				{"nit":"900215334-1","idProducto":1,"idSucursal":1,"calificacion":5,"cumplimientoTiempo":true,"observaciones":"Llegó completo."}
				"""))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.nombreEvaluador").value("Laura Gómez"))
				.andExpect(jsonPath("$.fechaEvaluacion").value(LocalDate.now(VentaService.ZONA).toString()))
				.andExpect(jsonPath("$.nombreProducto").value("Martillo de uña 16 oz"));
		mvc.perform(post("/api/evaluaciones").contentType(MediaType.APPLICATION_JSON)
				.content("{\"nit\":\"900215334-1\",\"idProducto\":6,\"idSucursal\":1,\"calificacion\":3,\"cumplimientoTiempo\":false}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.mensaje").value("Solo se pueden evaluar suministros registrados."));
		mvc.perform(post("/api/evaluaciones").contentType(MediaType.APPLICATION_JSON)
				.content("{\"nit\":\"900215334-1\",\"idProducto\":1,\"idSucursal\":1,\"calificacion\":6,\"cumplimientoTiempo\":true}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.campos.calificacion").value("La calificación debe estar entre 1 y 5."));
		mvc.perform(get("/api/evaluaciones").param("idEmpleado", "1"))
				.andExpect(jsonPath("$[*].nombreEvaluador").value(everyItem(is("Laura Gómez"))));
	}

	@Test
	@ComoEmpleado(id = 3, rol = Rol.VENDEDOR, idSucursal = 1)
	void elVendedorNoGestionaProveedores() throws Exception {
		mvc.perform(get("/api/proveedores")).andExpect(status().isForbidden());
		mvc.perform(get("/api/suministros")).andExpect(status().isForbidden());
		mvc.perform(get("/api/evaluaciones")).andExpect(status().isForbidden());
	}
}
