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

import co.edu.unbosque.backbases.dto.MetodoPagoDTO;
import co.edu.unbosque.backbases.dto.MetodoPagoEntradaDTO;
import co.edu.unbosque.backbases.service.MetodoPagoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/metodos-pago")
@Tag(name = "Métodos de pago")
public class MetodoPagoController {

	private final MetodoPagoService metodoPagoService;

	public MetodoPagoController(MetodoPagoService metodoPagoService) {
		this.metodoPagoService = metodoPagoService;
	}

	@GetMapping
	@Operation(summary = "Listar métodos de pago")
	public List<MetodoPagoDTO> listar() {
		return metodoPagoService.listar();
	}

	@GetMapping("/{idMetodoPago}")
	@Operation(summary = "Obtener un método de pago")
	public MetodoPagoDTO obtener(@PathVariable Integer idMetodoPago) {
		return metodoPagoService.obtener(idMetodoPago);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@PreAuthorize("hasAuthority('NIVEL_3')")
	@Operation(summary = "Crear un método de pago")
	public MetodoPagoDTO crear(@Valid @RequestBody MetodoPagoEntradaDTO entrada) {
		return metodoPagoService.crear(entrada);
	}

	@PutMapping("/{idMetodoPago}")
	@PreAuthorize("hasAuthority('NIVEL_3')")
	@Operation(summary = "Actualizar un método de pago")
	public MetodoPagoDTO actualizar(@PathVariable Integer idMetodoPago, @Valid @RequestBody MetodoPagoEntradaDTO entrada) {
		return metodoPagoService.actualizar(idMetodoPago, entrada);
	}

	@DeleteMapping("/{idMetodoPago}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@PreAuthorize("hasAuthority('NIVEL_3')")
	@Operation(summary = "Eliminar un método de pago sin ventas")
	public void eliminar(@PathVariable Integer idMetodoPago) {
		metodoPagoService.eliminar(idMetodoPago);
	}
}
