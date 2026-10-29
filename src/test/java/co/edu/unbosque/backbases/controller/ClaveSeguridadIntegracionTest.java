package co.edu.unbosque.backbases.controller;

import static co.edu.unbosque.backbases.security.ClaveSeguridadInterceptor.ENCABEZADO;
import static co.edu.unbosque.backbases.security.ComoEmpleado.CLAVE_ADMINISTRADOR;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriUtils;

import co.edu.unbosque.backbases.dto.Rol;
import co.edu.unbosque.backbases.security.ComoEmpleado;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ComoEmpleado(id = 1)
@EnabledIf("co.edu.unbosque.backbases.BaseDeDatos#disponible")
class ClaveSeguridadIntegracionTest {

	@Autowired
	private MockMvc mvc;

	@Autowired
	private JdbcTemplate jdbc;

	@Autowired
	private PasswordEncoder passwordEncoder;

	private static ResultMatcher sinErrorDeClave() {
		return resultado -> assertThat(resultado.getResponse().getContentAsString()).doesNotContain("CLAVE_");
	}

	private int evaluaciones(int idEvaluacion) {
		return jdbc.queryForObject("SELECT COUNT(*) FROM EVALUACION WHERE ID_EVALUACION = ?", Integer.class, idEvaluacion);
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"/api/marcas/1",
			"/api/categorias/1",
			"/api/metodos-pago/1",
			"/api/productos/1",
			"/api/sucursales/1",
			"/api/inventario/1/1",
			"/api/empleados/3",
			"/api/clientes/1",
			"/api/proveedores/900215334-1",
			"/api/suministros/900215334-1/1/1",
			"/api/evaluaciones/1" })
	void todaEliminacionPideLaClaveDelAdministrador(String ruta) throws Exception {
		mvc.perform(delete(ruta))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.codigo").value("CLAVE_REQUERIDA"))
				.andExpect(jsonPath("$.mensaje").value("Escriba su clave de seguridad para eliminar."));
		mvc.perform(delete(ruta).header(ENCABEZADO, "   "))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.codigo").value("CLAVE_REQUERIDA"));
		mvc.perform(delete(ruta).header(ENCABEZADO, "no-es-la-clave"))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.codigo").value("CLAVE_INCORRECTA"))
				.andExpect(jsonPath("$.mensaje").value("La clave de seguridad no es correcta."));
		mvc.perform(delete(ruta).header(ENCABEZADO, CLAVE_ADMINISTRADOR)).andExpect(sinErrorDeClave());
	}

	@Test
	void laClaveIncorrectaNoEliminaNada() throws Exception {
		mvc.perform(delete("/api/evaluaciones/1").header(ENCABEZADO, "demo-compras")).andExpect(status().isForbidden());
		assertThat(evaluaciones(1)).isEqualTo(1);

		mvc.perform(delete("/api/evaluaciones/1").header(ENCABEZADO, CLAVE_ADMINISTRADOR)).andExpect(status().isNoContent());
		mvc.perform(get("/api/evaluaciones")).andExpect(jsonPath("$[?(@.idEvaluacion == 1)]").isEmpty());
	}

	@Test
	void laClaveSeEnviaCodificadaYAdmiteTildes() throws Exception {
		jdbc.update("UPDATE ADMINISTRADOR SET CLAVE_SEGURIDAD = ? WHERE ID_EMPLEADO = 1", passwordEncoder.encode("ñandú 2026+"));

		mvc.perform(delete("/api/evaluaciones/1").header(ENCABEZADO, UriUtils.encode("ñandú 2026+", StandardCharsets.UTF_8)))
				.andExpect(status().isNoContent());
		mvc.perform(delete("/api/evaluaciones/2").header(ENCABEZADO, "%E0%A4%A"))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.codigo").value("CLAVE_INCORRECTA"));
		assertThat(evaluaciones(2)).isEqualTo(1);
	}

	@Test
	@ComoEmpleado(id = 3, rol = Rol.VENDEDOR, idSucursal = 1)
	void elVendedorNoEliminaAunqueEnvieUnaClave() throws Exception {
		mvc.perform(delete("/api/evaluaciones/1").header(ENCABEZADO, CLAVE_ADMINISTRADOR))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.codigo").value("SIN_PERMISO"));
		assertThat(evaluaciones(1)).isEqualTo(1);
	}
}
