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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/inventario")
@Tag(name = "Inventario")
public class InventarioController {

	private final InventarioService inventarioService;

	public InventarioController(InventarioService inventarioService) {
		this.inventarioService = inventarioService;
	}

	@GetMapping
	@Operation(summary = "Inventario de todas las sucursales, con búsqueda por código o nombre")
	public List<InventarioDTO> todo(@RequestParam(required = false) String q) {
		return inventarioService.todo(q);
	}

	@PutMapping("/{idSucursal}/{idProducto}")
	@PreAuthorize("hasRole('ADMINISTRADOR')")
	@Operation(summary = "Fijar la cantidad y la ubicación de un producto en una sucursal (crea el registro si no existe)")
	public InventarioDTO guardar(@PathVariable Integer idSucursal, @PathVariable Integer idProducto,
			@Valid @RequestBody InventarioEntradaDTO entrada) {
		return inventarioService.guardar(idSucursal, idProducto, entrada);
	}

	@PostMapping("/{idSucursal}/{idProducto}/entradas")
	@PreAuthorize("hasRole('ADMINISTRADOR')")
	@Operation(summary = "Sumar mercancía recibida a la cantidad vigente, de forma atómica")
	public InventarioDTO registrarEntrada(@PathVariable Integer idSucursal, @PathVariable Integer idProducto,
			@Valid @RequestBody EntradaMercanciaDTO entrada) {
		return inventarioService.registrarEntrada(idSucursal, idProducto, entrada);
	}

	@DeleteMapping("/{idSucursal}/{idProducto}")
	@PreAuthorize("hasRole('ADMINISTRADOR')")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Operation(summary = "Retirar un producto de la sucursal cuando su cantidad es 0")
	public void retirar(@PathVariable Integer idSucursal, @PathVariable Integer idProducto) {
		inventarioService.retirar(idSucursal, idProducto);
	}
}
