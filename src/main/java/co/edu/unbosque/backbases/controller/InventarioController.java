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

import co.edu.unbosque.backbases.dto.EntradaMercanciaDTO;
import co.edu.unbosque.backbases.dto.InventarioDTO;
import co.edu.unbosque.backbases.dto.InventarioEntradaDTO;
import co.edu.unbosque.backbases.service.InventarioService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/inventario")
public class InventarioController {

	private final InventarioService inventarioService;

	public InventarioController(InventarioService inventarioService) {
		this.inventarioService = inventarioService;
	}

	@GetMapping
	public List<InventarioDTO> todo(@RequestParam(required = false) String q) {
		return inventarioService.todo(q);
	}

	@PutMapping("/{idSucursal}/{idProducto}")
	@PreAuthorize("hasRole('ADMINISTRADOR')")
	public InventarioDTO guardar(@PathVariable Integer idSucursal, @PathVariable Integer idProducto,
			@Valid @RequestBody InventarioEntradaDTO entrada) {
		return inventarioService.guardar(idSucursal, idProducto, entrada);
	}

	@PostMapping("/{idSucursal}/{idProducto}/entradas")
	@PreAuthorize("hasRole('ADMINISTRADOR')")
	public InventarioDTO registrarEntrada(@PathVariable Integer idSucursal, @PathVariable Integer idProducto,
			@Valid @RequestBody EntradaMercanciaDTO entrada) {
		return inventarioService.registrarEntrada(idSucursal, idProducto, entrada);
	}

	@DeleteMapping("/{idSucursal}/{idProducto}")
	@PreAuthorize("hasRole('ADMINISTRADOR')")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void retirar(@PathVariable Integer idSucursal, @PathVariable Integer idProducto) {
		inventarioService.retirar(idSucursal, idProducto);
	}
}
