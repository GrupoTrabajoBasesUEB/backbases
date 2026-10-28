package co.edu.unbosque.backbases.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.OffsetDateTime;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import co.edu.unbosque.backbases.dto.EstadoDTO;
import co.edu.unbosque.backbases.security.PruebaWebSegura;
import co.edu.unbosque.backbases.service.EstadoService;

@WebMvcTest(EstadoController.class)
@PruebaWebSegura
class EstadoControllerTest {

	@Autowired
	private MockMvc mvc;

	@MockitoBean
	private EstadoService estadoService;

	@Test
	void estadoEsPublicoYDiceSiLaBaseResponde() throws Exception {
		when(estadoService.consultar()).thenReturn(new EstadoDTO("backbases", "0.0.1", "DISPONIBLE", OffsetDateTime.now()));

		mvc.perform(get("/api/estado"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.aplicacion").value("backbases"))
				.andExpect(jsonPath("$.baseDatos").value("DISPONIBLE"));
	}

	@Test
	void rutaProtegidaSinTokenDevuelve401ConErrorDTO() throws Exception {
		mvc.perform(get("/api/marcas"))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.estado").value(401))
				.andExpect(jsonPath("$.codigo").value("NO_AUTENTICADO"))
				.andExpect(jsonPath("$.mensaje").isNotEmpty());
	}

	@Test
	void elFrontDeDesarrolloPasaCors() throws Exception {
		mvc.perform(options("/api/estado")
				.header("Origin", "http://localhost:4200")
				.header("Access-Control-Request-Method", "GET"))
				.andExpect(status().isOk())
				.andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:4200"));
	}

	@Test
	void unOrigenDesconocidoNoPasaCors() throws Exception {
		mvc.perform(options("/api/estado")
				.header("Origin", "https://otro-sitio.example")
				.header("Access-Control-Request-Method", "GET"))
				.andExpect(status().isForbidden());
	}
}
