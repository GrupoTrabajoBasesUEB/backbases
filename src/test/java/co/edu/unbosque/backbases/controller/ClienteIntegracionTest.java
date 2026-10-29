package co.edu.unbosque.backbases.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
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
class ClienteIntegracionTest {

	private static final String NATURAL = """
			{"tipo":"NATURAL","numeroDocumento":"1077123456","direccionCliente":"Calle 80 # 10-20, Bogotá",
			 "telefonos":["310 222 3344","601 222 3344"],"correos":["Lucia.Prieto@Correo.demo"],"nombre":"Lucía","apellido":"Prieto"}
			""";

	@Autowired
	private MockMvc mvc;

	@Autowired
	private JdbcTemplate jdbc;

	private String crear(String cuerpo) throws Exception {
		return mvc.perform(post("/api/clientes").contentType(MediaType.APPLICATION_JSON).content(cuerpo))
				.andExpect(status().isCreated())
				.andReturn().getResponse().getContentAsString()
				.replaceAll(".*\"idCliente\":(\\d+).*", "$1");
	}

	@Test
	void buscaPorNombreRazonSocialODocumento() throws Exception {
		mvc.perform(get("/api/clientes").param("q", "villamil"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(1))
				.andExpect(jsonPath("$[0].nombreMostrado").value("Jorge Villamil"))
				.andExpect(jsonPath("$[0].telefonos.length()").value(2));
		mvc.perform(get("/api/clientes").param("q", "altos del rio"))
				.andExpect(jsonPath("$[0].tipo").value("JURIDICO"));
		mvc.perform(get("/api/clientes").param("tipo", "JURIDICO"))
				.andExpect(jsonPath("$.length()").value(4));
	}

	@Test
	void elConsumidorFinalEstaDisponibleYProtegido() throws Exception {
		mvc.perform(get("/api/clientes/consumidor-final"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.consumidorFinal").value(true))
				.andExpect(jsonPath("$.numeroDocumento").value("222222222222"));
		mvc.perform(delete("/api/clientes/7")).andExpect(status().isBadRequest());
	}

	@Test
	void registraUnClienteNaturalConSusContactos() throws Exception {
		String id = crear(NATURAL);

		mvc.perform(get("/api/clientes/" + id))
				.andExpect(jsonPath("$.tipo").value("NATURAL"))
				.andExpect(jsonPath("$.nombreMostrado").value("Lucía Prieto"))
				.andExpect(jsonPath("$.telefonos").value(containsInAnyOrder("310 222 3344", "601 222 3344")))
				.andExpect(jsonPath("$.correos[0]").value("lucia.prieto@correo.demo"));
		Integer telefonos = jdbc.queryForObject("SELECT COUNT(*) FROM TELEFONO_CLIENTE WHERE ID_CLIENTE = ?", Integer.class, Integer.valueOf(id));
		assertThat(telefonos).isEqualTo(2);
		mvc.perform(post("/api/clientes").contentType(MediaType.APPLICATION_JSON).content(NATURAL))
				.andExpect(status().isConflict());
	}

	@Test
	void cambiaDeNaturalAJuridicoSinDejarRastroDelTipoAnterior() throws Exception {
		String id = crear(NATURAL);

		mvc.perform(put("/api/clientes/" + id).contentType(MediaType.APPLICATION_JSON).content("""
				{"tipo":"JURIDICO","numeroDocumento":"901999888-1","telefonos":["601 222 3344"],"correos":[],"razonSocial":"Prieto y Asociados S.A.S."}
				"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.tipo").value("JURIDICO"))
				.andExpect(jsonPath("$.nombre").doesNotExist())
				.andExpect(jsonPath("$.razonSocial").value("Prieto y Asociados S.A.S."))
				.andExpect(jsonPath("$.correos.length()").value(0));
		Integer naturales = jdbc.queryForObject("SELECT COUNT(*) FROM CLIENTE_NATURAL WHERE ID_CLIENTE = ?", Integer.class, Integer.valueOf(id));
		assertThat(naturales).isZero();
	}

	@Test
	void eliminaSoloClientesSinVentas() throws Exception {
		String id = crear(NATURAL);

		mvc.perform(delete("/api/clientes/1")).andExpect(status().isConflict());
		mvc.perform(delete("/api/clientes/" + id)).andExpect(status().isNoContent());
		mvc.perform(get("/api/clientes/" + id)).andExpect(status().isNotFound());
	}

	@Test
	@ComoEmpleado(id = 3, rol = Rol.VENDEDOR, idSucursal = 1)
	void elVendedorRegistraClientesPeroNoLosElimina() throws Exception {
		String id = crear(NATURAL);

		mvc.perform(delete("/api/clientes/" + id)).andExpect(status().isForbidden());
	}
}
