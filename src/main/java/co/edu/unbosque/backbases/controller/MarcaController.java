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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unbosque.backbases.dto.MarcaDTO;
import co.edu.unbosque.backbases.dto.MarcaEntradaDTO;
import co.edu.unbosque.backbases.service.MarcaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/marcas")
@Tag(name = "Marcas")
public class MarcaController {

	private final MarcaService marcaService;

	public MarcaController(MarcaService marcaService) {
		this.marcaService = marcaService;
	}

	@GetMapping
	@Operation(summary = "Listar marcas")
	public List<MarcaDTO> listar() {
		return marcaService.listar();
	}

	@GetMapping("/{idMarca}")
	@Operation(summary = "Obtener una marca")
	public MarcaDTO obtener(@PathVariable Integer idMarca) {
		return marcaService.obtener(idMarca);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@PreAuthorize("hasRole('ADMINISTRADOR')")
	@Operation(summary = "Crear una marca")
	public MarcaDTO crear(@Valid @RequestBody MarcaEntradaDTO entrada) {
		return marcaService.crear(entrada);
	}

	@PutMapping("/{idMarca}")
	@PreAuthorize("hasRole('ADMINISTRADOR')")
	@Operation(summary = "Actualizar una marca")
	public MarcaDTO actualizar(@PathVariable Integer idMarca, @Valid @RequestBody MarcaEntradaDTO entrada) {
		return marcaService.actualizar(idMarca, entrada);
	}

	@DeleteMapping("/{idMarca}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@PreAuthorize("hasRole('ADMINISTRADOR')")
	@Operation(summary = "Eliminar una marca que no tenga productos")
	public void eliminar(@PathVariable Integer idMarca) {
		marcaService.eliminar(idMarca);
	}
}
