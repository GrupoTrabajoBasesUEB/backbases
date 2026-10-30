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

import co.edu.unbosque.backbases.dto.ProductoDTO;
import co.edu.unbosque.backbases.dto.ProductoEntradaDTO;
import co.edu.unbosque.backbases.service.InventarioService;
import co.edu.unbosque.backbases.service.ProductoService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {

	private final ProductoService productoService;
	private final InventarioService inventarioService;

	public ProductoController(ProductoService productoService, InventarioService inventarioService) {
		this.productoService = productoService;
		this.inventarioService = inventarioService;
	}

	@GetMapping
	public List<ProductoDTO> buscar(@RequestParam(required = false) String q, @RequestParam(required = false) Integer idCategoria,
			@RequestParam(required = false) Integer idMarca) {
		return productoService.buscar(q, idCategoria, idMarca);
	}

	@GetMapping("/{idProducto}")
	public ProductoDTO obtener(@PathVariable Integer idProducto) {
		return productoService.obtener(idProducto);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@PreAuthorize("hasRole('ADMINISTRADOR')")
	public ProductoDTO crear(@Valid @RequestBody ProductoEntradaDTO entrada) {
		return productoService.crear(entrada);
	}

	@PutMapping("/{idProducto}")
	@PreAuthorize("hasRole('ADMINISTRADOR')")
	public ProductoDTO actualizar(@PathVariable Integer idProducto, @Valid @RequestBody ProductoEntradaDTO entrada) {
		return productoService.actualizar(idProducto, entrada);
	}

	@DeleteMapping("/{idProducto}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@PreAuthorize("hasRole('ADMINISTRADOR')")
	public void eliminar(@PathVariable Integer idProducto) {
		productoService.eliminar(idProducto);
	}
}
