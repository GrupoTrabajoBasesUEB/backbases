package co.edu.unbosque.backbases.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@EnabledIf("co.edu.unbosque.backbases.BaseDeDatos#disponible")
class AuthIntegracionTest {

	private static final String CONTRASENA_DEMO = "Ferreteria2026";

	@Autowired
	private MockMvc mvc;

	private String token(String correo) throws Exception {
		String respuesta = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
				.content("{\"correo\":\"" + correo + "\",\"contrasena\":\"" + CONTRASENA_DEMO + "\"}"))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();
		return respuesta.replaceAll(".*\"token\":\"([^\"]+)\".*", "$1");
	}

	@Test
	void elAdministradorIniciaSesionYRecibeSuPerfil() throws Exception {
		mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
				.content("{\"correo\":\"Laura.Gomez@ferreteria.demo\",\"contrasena\":\"" + CONTRASENA_DEMO + "\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.tipo").value("Bearer"))
				.andExpect(jsonPath("$.token").isNotEmpty())
				.andExpect(jsonPath("$.empleado.rol").value("ADMINISTRADOR"))
				.andExpect(jsonPath("$.empleado.nivelAcceso").value(3))
				.andExpect(jsonPath("$.empleado.idSucursal").doesNotExist());
	}

	@Test
	void elVendedorRecibeSuSucursal() throws Exception {
		mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
				.content("{\"correo\":\"camila.rojas@ferreteria.demo\",\"contrasena\":\"" + CONTRASENA_DEMO + "\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.empleado.rol").value("VENDEDOR"))
				.andExpect(jsonPath("$.empleado.idSucursal").value(1))
				.andExpect(jsonPath("$.empleado.nombreSucursal").value("Sede Centro"));
	}

	@Test
	void contrasenaIncorrectaOCorreoInexistenteDan401SinDistinguir() throws Exception {
		mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
				.content("{\"correo\":\"laura.gomez@ferreteria.demo\",\"contrasena\":\"otra-cosa\"}"))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.codigo").value("CREDENCIALES_INVALIDAS"));
		mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
				.content("{\"correo\":\"nadie@ferreteria.demo\",\"contrasena\":\"otra-cosa\"}"))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.mensaje").value("Correo o contraseña incorrectos."));
	}

	@Test
	void conElTokenSeConsultaElPerfil() throws Exception {
		mvc.perform(get("/api/auth/yo").header("Authorization", "Bearer " + token("andres.pardo@ferreteria.demo")))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.nombre").value("Andrés"))
				.andExpect(jsonPath("$.contrasena").doesNotExist());
	}

	@Test
	void unTokenInventadoNoEntra() throws Exception {
		mvc.perform(get("/api/auth/yo").header("Authorization", "Bearer abc.def.ghi"))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.codigo").value("NO_AUTENTICADO"));
	}

	@Test
	void losPermisosDependenDelRolDelToken() throws Exception {
		String vendedor = "Bearer " + token("julian.herrera@ferreteria.demo");
		String administrador = "Bearer " + token("laura.gomez@ferreteria.demo");

		mvc.perform(get("/api/marcas").header("Authorization", vendedor)).andExpect(status().isOk());
		mvc.perform(get("/api/empleados").header("Authorization", vendedor))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.codigo").value("SIN_PERMISO"));
		mvc.perform(get("/api/empleados").header("Authorization", administrador))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(7));
		mvc.perform(get("/api/empleados").param("rol", "VENDEDOR").header("Authorization", administrador))
				.andExpect(jsonPath("$.length()").value(4));
	}
}
