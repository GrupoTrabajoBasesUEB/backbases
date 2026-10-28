package co.edu.unbosque.backbases.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unbosque.backbases.dto.CategoriaArbolDTO;
import co.edu.unbosque.backbases.dto.CategoriaDTO;
import co.edu.unbosque.backbases.dto.CategoriaEntradaDTO;
import co.edu.unbosque.backbases.service.CategoriaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/categorias")
@Tag(name = "Categorías")
public class CategoriaController {

	private final CategoriaService categoriaService;

	public CategoriaController(CategoriaService categoriaService) {
		this.categoriaService = categoriaService;
	}

	@GetMapping
	@Operation(summary = "Listar categorías, o solo las principales")
	public List<CategoriaDTO> listar(@RequestParam(defaultValue = "false") boolean soloPrincipales) {
		return categoriaService.listar(soloPrincipales);
	}

	@GetMapping("/arbol")
	@Operation(summary = "Categorías principales con sus subcategorías")
	public List<CategoriaArbolDTO> arbol() {
		return categoriaService.arbol();
	}

	@GetMapping("/{idCategoria}")
	@Operation(summary = "Obtener una categoría")
	public CategoriaDTO obtener(@PathVariable Integer idCategoria) {
		return categoriaService.obtener(idCategoria);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@PreAuthorize("hasRole('ADMINISTRADOR')")
	@Operation(summary = "Crear una categoría o subcategoría")
	public CategoriaDTO crear(@Valid @RequestBody CategoriaEntradaDTO entrada) {
		return categoriaService.crear(entrada);
	}

	@PutMapping("/{idCategoria}")
	@PreAuthorize("hasRole('ADMINISTRADOR')")
	@Operation(summary = "Actualizar una categoría")
	public CategoriaDTO actualizar(@PathVariable Integer idCategoria, @Valid @RequestBody CategoriaEntradaDTO entrada) {
		return categoriaService.actualizar(idCategoria, entrada);
	}

	@DeleteMapping("/{idCategoria}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@PreAuthorize("hasRole('ADMINISTRADOR')")
	@Operation(summary = "Eliminar una categoría sin subcategorías ni productos")
	public void eliminar(@PathVariable Integer idCategoria) {
		categoriaService.eliminar(idCategoria);
	}
}
