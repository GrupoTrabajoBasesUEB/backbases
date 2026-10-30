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

import co.edu.unbosque.backbases.dto.ProveedorDTO;
import co.edu.unbosque.backbases.dto.ProveedorEntradaDTO;
import co.edu.unbosque.backbases.service.ProveedorService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/proveedores")
@PreAuthorize("hasAuthority('NIVEL_2')")
public class ProveedorController {

	private final ProveedorService proveedorService;

	public ProveedorController(ProveedorService proveedorService) {
		this.proveedorService = proveedorService;
	}

	@GetMapping
	public List<ProveedorDTO> buscar(@RequestParam(required = false) String q) {
		return proveedorService.buscar(q);
	}

	@GetMapping("/{nit}")
	public ProveedorDTO obtener(@PathVariable String nit) {
		return proveedorService.obtener(nit);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public ProveedorDTO crear(@Valid @RequestBody ProveedorEntradaDTO entrada) {
		return proveedorService.crear(entrada);
	}

	@PutMapping("/{nit}")
	public ProveedorDTO actualizar(@PathVariable String nit, @Valid @RequestBody ProveedorEntradaDTO entrada) {
		return proveedorService.actualizar(nit, entrada);
	}

	@DeleteMapping("/{nit}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void eliminar(@PathVariable String nit) {
		proveedorService.eliminar(nit);
	}
}
