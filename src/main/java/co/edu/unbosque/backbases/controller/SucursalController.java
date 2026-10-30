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

import co.edu.unbosque.backbases.dto.InventarioDTO;
import co.edu.unbosque.backbases.dto.SucursalDTO;
import co.edu.unbosque.backbases.dto.SucursalEntradaDTO;
import co.edu.unbosque.backbases.service.InventarioService;
import co.edu.unbosque.backbases.service.SucursalService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/sucursales")
public class SucursalController {

	private final SucursalService sucursalService;
	private final InventarioService inventarioService;

	public SucursalController(SucursalService sucursalService, InventarioService inventarioService) {
		this.sucursalService = sucursalService;
		this.inventarioService = inventarioService;
	}

	@GetMapping
	public List<SucursalDTO> listar() {
		return sucursalService.listar();
	}

	@GetMapping("/{idSucursal}/inventario")
	public List<InventarioDTO> inventario(@PathVariable Integer idSucursal, @RequestParam(required = false) String q) {
		return inventarioService.deSucursal(idSucursal, q);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@PreAuthorize("hasAuthority('NIVEL_3')")
	public SucursalDTO crear(@Valid @RequestBody SucursalEntradaDTO entrada) {
		return sucursalService.crear(entrada);
	}

	@PutMapping("/{idSucursal}")
	@PreAuthorize("hasAuthority('NIVEL_3')")
	public SucursalDTO actualizar(@PathVariable Integer idSucursal, @Valid @RequestBody SucursalEntradaDTO entrada) {
		return sucursalService.actualizar(idSucursal, entrada);
	}

	@DeleteMapping("/{idSucursal}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@PreAuthorize("hasAuthority('NIVEL_3')")
	public void eliminar(@PathVariable Integer idSucursal) {
		sucursalService.eliminar(idSucursal);
	}
}
