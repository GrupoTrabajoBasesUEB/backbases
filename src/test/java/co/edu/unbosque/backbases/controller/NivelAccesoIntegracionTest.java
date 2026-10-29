package co.edu.unbosque.backbases.controller;

import static co.edu.unbosque.backbases.security.ClaveSeguridadInterceptor.ENCABEZADO;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.backbases.dto.Rol;
import co.edu.unbosque.backbases.security.ComoEmpleado;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@EnabledIf("co.edu.unbosque.backbases.BaseDeDatos#disponible")
class NivelAccesoIntegracionTest {

	private static final String MARCA = "{\"nombreMarca\":\"Milwaukee\",\"paisOrigen\":\"Estados Unidos\"}";
	private static final String SEDE_NORTE = """
			{"nombreSucursal":"Sede Norte","ciudad":"Bogotá","direccion":"Autopista Norte # 145-30","telefono":"601 615 2290","idAdministrador":2}
			""";
	private static final String PROVEEDOR = """
			{"nit":"901777666-4","nombreProveedor":"Ferreinsumos del Valle S.A.S.","direccionProveedor":"Calle 5 # 38-20, Cali",
			 "telefonos":["602 555 0100"],"correos":["ventas@ferreinsumos.demo"]}
			""";

	@Autowired
	private MockMvc mvc;

	private void permitido(MockHttpServletRequestBuilder peticion) throws Exception {
		mvc.perform(peticion).andExpect(result -> {
			int estado = result.getResponse().getStatus();
			if (estado == 401 || estado == 403) {
				throw new AssertionError("Se esperaba acceso y se obtuvo " + estado + ": " + result.getResponse().getContentAsString());
			}
		});
	}

	private void rechazado(MockHttpServletRequestBuilder peticion) throws Exception {
		mvc.perform(peticion).andExpect(status().isForbidden()).andExpect(jsonPath("$.codigo").value("SIN_PERMISO"));
	}

	private static MockHttpServletRequestBuilder json(MockHttpServletRequestBuilder peticion, String cuerpo) {
		return peticion.contentType(MediaType.APPLICATION_JSON).content(cuerpo);
	}

	@ParameterizedTest
	@ValueSource(strings = { "/api/marcas", "/api/categorias", "/api/metodos-pago", "/api/sucursales", "/api/empleados",
			"/api/sucursales/1/inventario", "/api/productos", "/api/inventario", "/api/clientes", "/api/ventas", "/api/ventas/lineas",
			"/api/estadisticas/resumen", "/api/estadisticas/sucursales", "/api/estadisticas/rendimiento-productos" })
	@ComoEmpleado(id = 10, nivel = 1)
	void elNivel1LeeSuAreaYLasListasDeApoyo(String ruta) throws Exception {
		permitido(get(ruta));
	}

	@ParameterizedTest
	@ValueSource(strings = { "/api/proveedores", "/api/proveedores/900215334-1", "/api/suministros", "/api/evaluaciones",
			"/api/estadisticas/comisiones" })
	@ComoEmpleado(id = 10, nivel = 1)
	void elNivel1NoConsultaAreasSuperiores(String ruta) throws Exception {
		rechazado(get(ruta));
	}

	@Test
	@ComoEmpleado(id = 10, nivel = 1)
	void elNivel1AdministraInventarioPeroNoCatalogosNiProveedores() throws Exception {
		permitido(json(post("/api/inventario/4/1/entradas"), "{\"unidades\":2}"));
		rechazado(json(post("/api/marcas"), MARCA));
		rechazado(json(put("/api/sucursales/2"), SEDE_NORTE));
		rechazado(json(post("/api/proveedores"), PROVEEDOR));
		rechazado(delete("/api/evaluaciones/1").header(ENCABEZADO, "demo-kennedy"));
	}

	@ParameterizedTest
	@ValueSource(strings = { "/api/proveedores", "/api/suministros", "/api/evaluaciones" })
	@ComoEmpleado(id = 2, nivel = 2)
	void elNivel2ConsultaProveedoresSuministrosYEvaluaciones(String ruta) throws Exception {
		permitido(get(ruta));
	}

	@Test
	@ComoEmpleado(id = 2, nivel = 2)
	void elNivel2NoAdministraEmpleadosSucursalesNiCatalogos() throws Exception {
		rechazado(json(post("/api/marcas"), MARCA));
		rechazado(json(put("/api/sucursales/2"), SEDE_NORTE));
		rechazado(delete("/api/empleados/4").header(ENCABEZADO, "demo-compras"));
		rechazado(get("/api/estadisticas/comisiones"));
		mvc.perform(delete("/api/evaluaciones/1").header(ENCABEZADO, "demo-compras")).andExpect(status().isNoContent());
	}

	@Test
	@ComoEmpleado(id = 1, nivel = 3)
	void elNivel3AdministraTodo() throws Exception {
		mvc.perform(json(post("/api/marcas"), MARCA)).andExpect(status().isCreated());
		mvc.perform(json(put("/api/sucursales/2"), SEDE_NORTE)).andExpect(status().isOk());
		permitido(get("/api/estadisticas/comisiones"));
		permitido(get("/api/proveedores"));
	}

	@Test
	@ComoEmpleado(id = 3, rol = Rol.VENDEDOR, idSucursal = 1)
	void elVendedorVeSuComisionPeroNoLasAreasDeAdministrador() throws Exception {
		permitido(get("/api/estadisticas/comisiones"));
		rechazado(get("/api/proveedores"));
		rechazado(json(post("/api/marcas"), MARCA));
	}
}
