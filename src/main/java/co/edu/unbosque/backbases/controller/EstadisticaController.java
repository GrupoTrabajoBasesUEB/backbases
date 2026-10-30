package co.edu.unbosque.backbases.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unbosque.backbases.dto.EstadisticasDTO.Agrupado;
import co.edu.unbosque.backbases.dto.EstadisticasDTO.Comision;
import co.edu.unbosque.backbases.dto.EstadisticasDTO.Comparativo;
import co.edu.unbosque.backbases.dto.EstadisticasDTO.ProductoVendido;
import co.edu.unbosque.backbases.dto.EstadisticasDTO.SucursalDetalle;
import co.edu.unbosque.backbases.dto.EstadisticasDTO.UnidadesInventario;
import co.edu.unbosque.backbases.dto.EstadisticasDTO.VentasDia;
import co.edu.unbosque.backbases.dto.EstadisticasDTO.VentasProducto;
import co.edu.unbosque.backbases.repository.EstadisticaRepository.Filtro;
import co.edu.unbosque.backbases.security.UsuarioAutenticado;
import co.edu.unbosque.backbases.service.EstadisticaService;

@RestController
@RequestMapping("/api/estadisticas")
public class EstadisticaController {

	private final EstadisticaService estadisticaService;

	public EstadisticaController(EstadisticaService estadisticaService) {
		this.estadisticaService = estadisticaService;
	}

	private Filtro filtro(LocalDate desde, LocalDate hasta, Integer idSucursal, Integer idEmpleado, UsuarioAutenticado usuario) {
		return estadisticaService.filtro(desde, hasta, idSucursal, idEmpleado, usuario);
	}

	@GetMapping("/resumen")
	public Comparativo resumen(
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
			@RequestParam(required = false) Integer idSucursal, @RequestParam(required = false) Integer idEmpleado,
			@AuthenticationPrincipal UsuarioAutenticado usuario) {
		return estadisticaService.resumen(filtro(desde, hasta, idSucursal, idEmpleado, usuario));
	}

	@GetMapping("/ventas-por-dia")
	public List<VentasDia> ventasPorDia(
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
			@RequestParam(required = false) Integer idSucursal, @RequestParam(required = false) Integer idEmpleado,
			@AuthenticationPrincipal UsuarioAutenticado usuario) {
		return estadisticaService.ventasPorDia(filtro(desde, hasta, idSucursal, idEmpleado, usuario));
	}

	@GetMapping("/por-metodo-pago")
	public List<Agrupado> porMetodoPago(
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
			@RequestParam(required = false) Integer idSucursal, @RequestParam(required = false) Integer idEmpleado,
			@AuthenticationPrincipal UsuarioAutenticado usuario) {
		return estadisticaService.porMetodoPago(filtro(desde, hasta, idSucursal, idEmpleado, usuario));
	}

	@GetMapping("/rendimiento-productos")
	public List<ProductoVendido> rendimientoPorProducto(
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
			@RequestParam(required = false) Integer idSucursal, @RequestParam(required = false) Integer idEmpleado,
			@AuthenticationPrincipal UsuarioAutenticado usuario) {
		return estadisticaService.rendimientoPorProducto(filtro(desde, hasta, idSucursal, idEmpleado, usuario));
	}

	@GetMapping("/productos/{idProducto}")
	public VentasProducto ventasDeProducto(@PathVariable Integer idProducto,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
			@RequestParam(required = false) Integer idSucursal, @AuthenticationPrincipal UsuarioAutenticado usuario) {
		return estadisticaService.ventasDeProducto(idProducto, filtro(desde, hasta, idSucursal, null, usuario));
	}

	@GetMapping("/unidades-por-inventario")
	public List<UnidadesInventario> unidadesPorInventario(
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
			@RequestParam(required = false) Integer idSucursal, @AuthenticationPrincipal UsuarioAutenticado usuario) {
		return estadisticaService.unidadesPorInventario(filtro(desde, hasta, idSucursal, null, usuario));
	}

	@GetMapping("/sucursales")
	public List<SucursalDetalle> sucursalesDetalle(
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
			@RequestParam(required = false) Integer idSucursal, @AuthenticationPrincipal UsuarioAutenticado usuario) {
		return estadisticaService.sucursalesDetalle(filtro(desde, hasta, idSucursal, null, usuario));
	}

	@GetMapping("/comisiones")
	@PreAuthorize("hasRole('VENDEDOR') or hasAuthority('NIVEL_3')")
	public List<Comision> comisiones(
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
			@RequestParam(required = false) Integer idSucursal, @RequestParam(required = false) Integer idEmpleado,
			@AuthenticationPrincipal UsuarioAutenticado usuario) {
		return estadisticaService.comisiones(filtro(desde, hasta, idSucursal, idEmpleado, usuario), usuario);
	}
}
