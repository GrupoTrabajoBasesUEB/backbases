package co.edu.unbosque.backbases.controller;

import static co.edu.unbosque.backbases.security.ClaveSeguridadInterceptor.ENCABEZADO;
import static co.edu.unbosque.backbases.security.ComoEmpleado.CLAVE_ADMINISTRADOR;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;
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
class CatalogosIntegracionTest {

	@Autowired
	private MockMvc mvc;

	@Test
	void listaLasMarcasOrdenadasPorNombre() throws Exception {
		mvc.perform(get("/api/marcas"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(greaterThanOrEqualTo(7)))
				.andExpect(jsonPath("$[*].nombreMarca").value(hasItem("Stanley")));
	}

	@Test
	void laBaseIgnoraMayusculasYTildesAlBuscarRepetidos() throws Exception {
		mvc.perform(post("/api/marcas").contentType(MediaType.APPLICATION_JSON)
				.content("{\"nombreMarca\":\"stanley\",\"paisOrigen\":\"Estados Unidos\"}"))
				.andExpect(status().isConflict());
		mvc.perform(post("/api/categorias").contentType(MediaType.APPLICATION_JSON)
				.content("{\"nombre\":\"plomeria\"}"))
				.andExpect(status().isConflict());
	}

	@Test
	void creaActualizaYEliminaUnaMarcaNueva() throws Exception {
		String respuesta = mvc.perform(post("/api/marcas").contentType(MediaType.APPLICATION_JSON)
				.content("{\"nombreMarca\":\"Milwaukee\",\"paisOrigen\":\"Estados Unidos\"}"))
				.andExpect(status().isCreated())
				.andReturn().getResponse().getContentAsString();
		String id = respuesta.replaceAll(".*\"idMarca\":(\\d+).*", "$1");

		mvc.perform(put("/api/marcas/" + id).contentType(MediaType.APPLICATION_JSON)
				.content("{\"nombreMarca\":\"Milwaukee\",\"paisOrigen\":\"Estados Unidos (Wisconsin)\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.paisOrigen").value("Estados Unidos (Wisconsin)"));
		mvc.perform(delete("/api/marcas/" + id).header(ENCABEZADO, CLAVE_ADMINISTRADOR)).andExpect(status().isNoContent());
		mvc.perform(get("/api/marcas/" + id)).andExpect(status().isNotFound());
	}

	@Test
	void noEliminaLoQueEstaEnUso() throws Exception {
		mvc.perform(delete("/api/marcas/1").header(ENCABEZADO, CLAVE_ADMINISTRADOR))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.mensaje").value("No se puede eliminar la marca: la usan 3 productos."));
		mvc.perform(delete("/api/categorias/1").header(ENCABEZADO, CLAVE_ADMINISTRADOR))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.mensaje").value("No se puede eliminar la categoría: la usan 3 subcategorías."));
		mvc.perform(delete("/api/metodos-pago/1").header(ENCABEZADO, CLAVE_ADMINISTRADOR)).andExpect(status().isConflict());
	}

	@Test
	void elArbolTieneLasSeisPrincipalesConSusSubcategorias() throws Exception {
		mvc.perform(get("/api/categorias/arbol"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(6))
				.andExpect(jsonPath("$[?(@.nombre == 'Herramientas manuales')].subcategorias.length()").value(3));
		mvc.perform(get("/api/categorias").param("soloPrincipales", "true"))
				.andExpect(jsonPath("$.length()").value(6));
	}

	@Test
	void unaSubcategoriaNoPuedeTenerSubcategorias() throws Exception {
		mvc.perform(post("/api/categorias").contentType(MediaType.APPLICATION_JSON)
				.content("{\"nombre\":\"Mazos de goma\",\"idCategoriaPrincipal\":2}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.codigo").value("REGLA_NEGOCIO"));
	}
}
