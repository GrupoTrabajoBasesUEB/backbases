package co.edu.unbosque.backbases.controller;

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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.backbases.security.ComoEmpleado;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ComoEmpleado(id = 1)
@EnabledIf("co.edu.unbosque.backbases.BaseDeDatos#disponible")
class EmpleadoIntegracionTest {

	@Autowired
	private MockMvc mvc;

	private static String vendedor(String documento, String correo) {
		return """
				{"documento":"%s","nombre":"Valentina","apellido":"Mora","cargo":"Asesora de ventas","correo":"%s",
				 "telefonos":["300 111 2233"],"rol":"VENDEDOR","contrasena":"Clave12345","idSucursal":2}
				""".formatted(documento, correo);
	}

	private String crear(String cuerpo) throws Exception {
		String respuesta = mvc.perform(post("/api/empleados").contentType(MediaType.APPLICATION_JSON).content(cuerpo))
				.andExpect(status().isCreated())
				.andReturn().getResponse().getContentAsString();
		return respuesta.replaceAll(".*\"idEmpleado\":(\\d+).*", "$1");
	}

	@Test
	void creaUnVendedorConComisionPorDefecto() throws Exception {
		mvc.perform(post("/api/empleados").contentType(MediaType.APPLICATION_JSON).content(vendedor("1099888777", "valentina.mora@ferreteria.demo")))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.rol").value("VENDEDOR"))
				.andExpect(jsonPath("$.comisionVenta").value(0.1))
				.andExpect(jsonPath("$.nombreSucursal").value("Sede Norte"))
				.andExpect(jsonPath("$.telefonos[0]").value("300 111 2233"));
	}

	@Test
	void creaUnAdministrador() throws Exception {
		mvc.perform(post("/api/empleados").contentType(MediaType.APPLICATION_JSON).content("""
				{"documento":"1099888778","nombre":"Hernán","apellido":"Díaz","cargo":"Auditor","correo":"hernan.diaz@ferreteria.demo",
				 "telefonos":["300 111 4455"],"rol":"ADMINISTRADOR","contrasena":"Clave12345","nivelAcceso":1,"claveSeguridad":"auditoria"}
				"""))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.rol").value("ADMINISTRADOR"))
				.andExpect(jsonPath("$.nivelAcceso").value(1));
	}

	@Test
	void unAdministradorSinClaveDeSeguridadNoSeCrea() throws Exception {
		mvc.perform(post("/api/empleados").contentType(MediaType.APPLICATION_JSON).content("""
				{"documento":"1099888779","nombre":"Hernán","apellido":"Díaz","cargo":"Auditor","correo":"h.diaz@ferreteria.demo",
				 "telefonos":["300 111 4455"],"rol":"ADMINISTRADOR","contrasena":"Clave12345","nivelAcceso":1}
				"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.codigo").value("REGLA_NEGOCIO"));
	}

	@Test
	void noRepiteCorreoNiDocumento() throws Exception {
		mvc.perform(post("/api/empleados").contentType(MediaType.APPLICATION_JSON).content(vendedor("1099888777", "CAMILA.ROJAS@ferreteria.demo")))
				.andExpect(status().isConflict());
		mvc.perform(post("/api/empleados").contentType(MediaType.APPLICATION_JSON).content(vendedor("52814736", "otro@ferreteria.demo")))
				.andExpect(status().isConflict());
	}

	@Test
	void elRolNoCambiaYLaContrasenaSoloCambiaSiSeEnvia() throws Exception {
		String id = crear(vendedor("1099888777", "valentina.mora@ferreteria.demo"));

		mvc.perform(put("/api/empleados/" + id).contentType(MediaType.APPLICATION_JSON)
				.content(vendedor("1099888777", "valentina.mora@ferreteria.demo").replace("\"VENDEDOR\"", "\"ADMINISTRADOR\"")))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.mensaje").value("El rol de un empleado no se puede cambiar."));
		mvc.perform(put("/api/empleados/" + id).contentType(MediaType.APPLICATION_JSON)
				.content(vendedor("1099888777", "valentina.mora@ferreteria.demo").replace(",\"contrasena\":\"Clave12345\"", "")
						.replace("\"idSucursal\":2", "\"idSucursal\":3")))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.nombreSucursal").value("Sede Laureles"));
	}

	@Test
	void noEliminaAQuienTieneHistorialNiASiMismo() throws Exception {
		mvc.perform(delete("/api/empleados/3"))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.mensaje").value(org.hamcrest.Matchers.startsWith("No se puede eliminar el empleado: la usan")));
		mvc.perform(delete("/api/empleados/1"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.mensaje").value("No puede eliminar su propio usuario."));
	}

	@Test
	void eliminaUnEmpleadoSinHistorial() throws Exception {
		String id = crear(vendedor("1099888777", "valentina.mora@ferreteria.demo"));

		mvc.perform(delete("/api/empleados/" + id)).andExpect(status().isNoContent());
		mvc.perform(get("/api/empleados/" + id)).andExpect(status().isNotFound());
	}

	@Test
	void datosInvalidosListanCadaCampo() throws Exception {
		mvc.perform(post("/api/empleados").contentType(MediaType.APPLICATION_JSON)
				.content("{\"correo\":\"no-es-correo\",\"telefonos\":[],\"contrasena\":\"corta\"}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.campos.correo").value("El correo no tiene un formato válido."))
				.andExpect(jsonPath("$.campos.telefonos").value("Registre al menos un teléfono."))
				.andExpect(jsonPath("$.campos.contrasena").value("La contraseña debe tener entre 8 y 72 caracteres."))
				.andExpect(jsonPath("$.campos.rol").value("El rol es obligatorio."));
	}
}
