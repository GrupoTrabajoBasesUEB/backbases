package co.edu.unbosque.backbases.controller;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unbosque.backbases.dto.PaginaDTO;
import co.edu.unbosque.backbases.dto.VentaDTO;
import co.edu.unbosque.backbases.dto.VentaEntradaDTO;
import co.edu.unbosque.backbases.security.UsuarioAutenticado;
import co.edu.unbosque.backbases.service.VentaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/ventas")
@Tag(name = "Ventas")
public class VentaController {

	private final VentaService ventaService;

	public VentaController(VentaService ventaService) {
		this.ventaService = ventaService;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@PreAuthorize("hasRole('VENDEDOR')")
	@Operation(summary = "Registrar una venta en la sucursal del vendedor; sin cliente se registra a consumidor final")
	public VentaDTO registrar(@Valid @RequestBody VentaEntradaDTO entrada, @AuthenticationPrincipal UsuarioAutenticado usuario) {
		return ventaService.registrar(entrada, usuario);
	}

	@GetMapping
	@Operation(summary = "Buscar ventas por fechas, sucursal, vendedor, cliente, método de pago o factura, paginado")
	public PaginaDTO<VentaDTO> buscar(
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
			@RequestParam(required = false) Integer idSucursal,
			@RequestParam(required = false) Integer idEmpleado,
			@RequestParam(required = false) Integer idCliente,
			@RequestParam(required = false) Integer idMetodoPago,
			@RequestParam(required = false) String factura,
			@RequestParam(defaultValue = "0") int pagina,
			@RequestParam(defaultValue = "20") int tamano,
			@AuthenticationPrincipal UsuarioAutenticado usuario) {
		return ventaService.buscar(desde, hasta, idSucursal, idEmpleado, idCliente, idMetodoPago, factura, pagina, tamano, usuario);
	}

	@GetMapping("/{idVenta}")
	@Operation(summary = "Factura con sus líneas y el total calculado por la vista V_VENTA_TOTAL")
	public VentaDTO obtener(@PathVariable Integer idVenta, @AuthenticationPrincipal UsuarioAutenticado usuario) {
		return ventaService.obtener(idVenta, usuario);
	}
}
