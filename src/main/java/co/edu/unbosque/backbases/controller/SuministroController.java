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

import co.edu.unbosque.backbases.dto.SuministroCondicionesDTO;
import co.edu.unbosque.backbases.dto.SuministroDTO;
import co.edu.unbosque.backbases.dto.SuministroEntradaDTO;
import co.edu.unbosque.backbases.service.SuministroService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/suministros")
@PreAuthorize("hasAuthority('NIVEL_2')")
public class SuministroController {

	private final SuministroService suministroService;

	public SuministroController(SuministroService suministroService) {
		this.suministroService = suministroService;
	}

	@GetMapping
	public List<SuministroDTO> buscar(@RequestParam(required = false) String nit, @RequestParam(required = false) Integer idProducto,
			@RequestParam(required = false) Integer idSucursal) {
		return suministroService.buscar(nit, idProducto, idSucursal);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public SuministroDTO crear(@Valid @RequestBody SuministroEntradaDTO entrada) {
		return suministroService.crear(entrada);
	}

	@PutMapping("/{nit}/{idProducto}/{idSucursal}")
	public SuministroDTO actualizar(@PathVariable String nit, @PathVariable Integer idProducto, @PathVariable Integer idSucursal,
			@Valid @RequestBody SuministroCondicionesDTO condiciones) {
		return suministroService.actualizar(nit, idProducto, idSucursal, condiciones);
	}

	@DeleteMapping("/{nit}/{idProducto}/{idSucursal}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void eliminar(@PathVariable String nit, @PathVariable Integer idProducto, @PathVariable Integer idSucursal) {
		suministroService.eliminar(nit, idProducto, idSucursal);
	}
}
