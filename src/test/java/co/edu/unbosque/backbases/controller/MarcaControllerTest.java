package co.edu.unbosque.backbases.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import co.edu.unbosque.backbases.configuration.CorsConfig;
import co.edu.unbosque.backbases.dto.MarcaDTO;
import co.edu.unbosque.backbases.exception.ConflictoException;
import co.edu.unbosque.backbases.security.SecurityConfig;
import co.edu.unbosque.backbases.service.MarcaService;

@WebMvcTest(MarcaController.class)
@Import({ SecurityConfig.class, CorsConfig.class })
@TestPropertySource(properties = "app.cors.allowed-origins=http://localhost:4200")
class MarcaControllerTest {

	@Autowired
	private MockMvc mvc;

	@MockitoBean
	private MarcaService marcaService;

	@Test
	void sinSesionDevuelve401() throws Exception {
		mvc.perform(get("/api/marcas")).andExpect(status().isUnauthorized());
	}

	@Test
	@WithMockUser(roles = "VENDEDOR")
	void elVendedorPuedeListar() throws Exception {
		when(marcaService.listar()).thenReturn(List.of(new MarcaDTO(1, "Stanley", "Estados Unidos")));

		mvc.perform(get("/api/marcas"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].idMarca").value(1))
				.andExpect(jsonPath("$[0].nombreMarca").value("Stanley"));
	}

	@Test
	@WithMockUser(roles = "VENDEDOR")
	void elVendedorNoPuedeCrear() throws Exception {
		mvc.perform(post("/api/marcas").contentType(MediaType.APPLICATION_JSON)
				.content("{\"nombreMarca\":\"Makita\",\"paisOrigen\":\"Japón\"}"))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.codigo").value("SIN_PERMISO"));
		verify(marcaService, never()).crear(any());
	}

	@Test
	@WithMockUser(roles = "ADMINISTRADOR")
	void elAdministradorCreaYRecibe201() throws Exception {
		when(marcaService.crear(any())).thenReturn(new MarcaDTO(8, "Makita", "Japón"));

		mvc.perform(post("/api/marcas").contentType(MediaType.APPLICATION_JSON)
				.content("{\"nombreMarca\":\"Makita\",\"paisOrigen\":\"Japón\"}"))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.idMarca").value(8));
	}

	@Test
	@WithMockUser(roles = "ADMINISTRADOR")
	void datosIncompletosDevuelven400ConLosCampos() throws Exception {
		mvc.perform(post("/api/marcas").contentType(MediaType.APPLICATION_JSON).content("{\"nombreMarca\":\"\"}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.campos.nombreMarca").value("El nombre de la marca es obligatorio."))
				.andExpect(jsonPath("$.campos.paisOrigen").value("El país de origen es obligatorio."));
	}

	@Test
	@WithMockUser(roles = "ADMINISTRADOR")
	void eliminarEnUsoDevuelve409() throws Exception {
		doThrow(new ConflictoException("No se puede eliminar la marca: la usan 4 productos."))
				.when(marcaService).eliminar(1);

		mvc.perform(delete("/api/marcas/1"))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.mensaje").value("No se puede eliminar la marca: la usan 4 productos."));
	}

	@Test
	@WithMockUser(roles = "ADMINISTRADOR")
	void eliminarDevuelve204() throws Exception {
		mvc.perform(delete("/api/marcas/8")).andExpect(status().isNoContent());
		verify(marcaService).eliminar(8);
	}
}
