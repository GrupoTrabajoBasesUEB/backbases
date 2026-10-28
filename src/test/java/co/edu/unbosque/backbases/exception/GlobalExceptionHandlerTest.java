package co.edu.unbosque.backbases.exception;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unbosque.backbases.security.PruebaWebSegura;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@WebMvcTest(GlobalExceptionHandlerTest.ControladorDePrueba.class)
@PruebaWebSegura
@Import(GlobalExceptionHandlerTest.ControladorDePrueba.class)
@WithMockUser
class GlobalExceptionHandlerTest {

	@RestController
	static class ControladorDePrueba {

		record Cuerpo(@NotBlank(message = "El nombre es obligatorio.") @Size(max = 5, message = "Máximo 5 caracteres.") String nombre) {
		}

		@GetMapping("/api/prueba/no-encontrado")
		void noEncontrado() {
			throw RecursoNoEncontradoException.de("la marca", 7);
		}

		@GetMapping("/api/prueba/conflicto")
		void conflicto() {
			throw new ConflictoException("Ya existe una marca con ese nombre.");
		}

		@GetMapping("/api/prueba/regla")
		void regla() {
			throw new ReglaNegocioException("Una categoría no puede ser su propia categoría principal.");
		}

		@GetMapping("/api/prueba/integridad")
		void integridad() {
			throw new DataIntegrityViolationException("FK");
		}

		@GetMapping("/api/prueba/prohibido")
		void prohibido() {
			throw new AccessDeniedException("no");
		}

		@GetMapping("/api/prueba/inesperado")
		void inesperado() {
			throw new IllegalStateException("fallo interno con detalles que no deben salir");
		}

		@PostMapping("/api/prueba/validacion")
		void validacion(@Valid @RequestBody Cuerpo cuerpo) {
		}
	}

	@Autowired
	private MockMvc mvc;

	@Test
	void noEncontradoDevuelve404() throws Exception {
		mvc.perform(get("/api/prueba/no-encontrado"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.codigo").value("NO_ENCONTRADO"))
				.andExpect(jsonPath("$.mensaje").value("No existe la marca con id 7."));
	}

	@Test
	void conflictoDevuelve409() throws Exception {
		mvc.perform(get("/api/prueba/conflicto"))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.codigo").value("CONFLICTO"));
	}

	@Test
	void reglaDeNegocioDevuelve400() throws Exception {
		mvc.perform(get("/api/prueba/regla"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.codigo").value("REGLA_NEGOCIO"));
	}

	@Test
	void restriccionDeLaBaseDevuelve409() throws Exception {
		mvc.perform(get("/api/prueba/integridad"))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.codigo").value("INTEGRIDAD"));
	}

	@Test
	void sinPermisoDevuelve403() throws Exception {
		mvc.perform(get("/api/prueba/prohibido"))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.codigo").value("SIN_PERMISO"));
	}

	@Test
	void errorInesperadoNoMuestraDetalles() throws Exception {
		mvc.perform(get("/api/prueba/inesperado"))
				.andExpect(status().isInternalServerError())
				.andExpect(jsonPath("$.codigo").value("ERROR_INTERNO"))
				.andExpect(jsonPath("$.mensaje").value("Ocurrió un error inesperado. Intente de nuevo."));
	}

	@Test
	void validacionListaLosCamposConError() throws Exception {
		mvc.perform(post("/api/prueba/validacion").contentType(MediaType.APPLICATION_JSON).content("{\"nombre\":\"\"}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.codigo").value("VALIDACION"))
				.andExpect(jsonPath("$.campos.nombre").value("El nombre es obligatorio."));
	}

	@Test
	void jsonMalFormadoDevuelve400() throws Exception {
		mvc.perform(post("/api/prueba/validacion").contentType(MediaType.APPLICATION_JSON).content("{nombre"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.codigo").value("CUERPO_INVALIDO"));
	}

	@Test
	void rutaInexistenteDevuelve404() throws Exception {
		mvc.perform(get("/api/prueba/no-existe"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.codigo").value("RUTA_INEXISTENTE"));
	}
}
