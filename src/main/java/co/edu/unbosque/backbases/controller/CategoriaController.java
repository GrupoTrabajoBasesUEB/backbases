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

import co.edu.unbosque.backbases.dto.CategoriaDTO;
import co.edu.unbosque.backbases.dto.CategoriaEntradaDTO;
import co.edu.unbosque.backbases.service.CategoriaService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/categorias")
public class CategoriaController {

	private final CategoriaService categoriaService;

	public CategoriaController(CategoriaService categoriaService) {
		this.categoriaService = categoriaService;
	}

	@GetMapping
	public List<CategoriaDTO> listar(@RequestParam(defaultValue = "false") boolean soloPrincipales) {
		return categoriaService.listar(soloPrincipales);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@PreAuthorize("hasAuthority('NIVEL_3')")
	public CategoriaDTO crear(@Valid @RequestBody CategoriaEntradaDTO entrada) {
		return categoriaService.crear(entrada);
	}

	@PutMapping("/{idCategoria}")
	@PreAuthorize("hasAuthority('NIVEL_3')")
	public CategoriaDTO actualizar(@PathVariable Integer idCategoria, @Valid @RequestBody CategoriaEntradaDTO entrada) {
		return categoriaService.actualizar(idCategoria, entrada);
	}

	@DeleteMapping("/{idCategoria}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@PreAuthorize("hasAuthority('NIVEL_3')")
	public void eliminar(@PathVariable Integer idCategoria) {
		categoriaService.eliminar(idCategoria);
	}
}
