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
import co.edu.unbosque.backbases.dto.EstadisticasDTO.StockBajo;
import co.edu.unbosque.backbases.dto.EstadisticasDTO.SucursalDetalle;
import co.edu.unbosque.backbases.dto.EstadisticasDTO.UnidadesInventario;
import co.edu.unbosque.backbases.dto.EstadisticasDTO.VentasDia;
import co.edu.unbosque.backbases.dto.EstadisticasDTO.VentasProducto;
import co.edu.unbosque.backbases.repository.EstadisticaRepository.Filtro;
import co.edu.unbosque.backbases.security.UsuarioAutenticado;
import co.edu.unbosque.backbases.service.EstadisticaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/estadisticas")
@Tag(name = "Estadísticas", description = "Periodo por defecto: los últimos 30 días. El vendedor solo ve su sucursal.")
public class EstadisticaController {

	private final EstadisticaService estadisticaService;

	public EstadisticaController(EstadisticaService estadisticaService) {
		this.estadisticaService = estadisticaService;
	}

	private Filtro filtro(LocalDate desde, LocalDate hasta, Integer idSucursal, Integer idEmpleado, UsuarioAutenticado usuario) {
		return estadisticaService.filtro(desde, hasta, idSucursal, idEmpleado, usuario);
	}

	@GetMapping("/resumen")
	@Operation(summary = "Total, número de ventas, unidades y ticket promedio, comparados con el periodo anterior de igual duración")
	public Comparativo resumen(
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
			@RequestParam(required = false) Integer idSucursal, @RequestParam(required = false) Integer idEmpleado,
			@AuthenticationPrincipal UsuarioAutenticado usuario) {
		return estadisticaService.resumen(filtro(desde, hasta, idSucursal, idEmpleado, usuario));
	}

	@GetMapping("/ventas-por-dia")
	@Operation(summary = "Total y número de ventas de cada día del periodo, incluidos los días sin ventas")
	public List<VentasDia> ventasPorDia(
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
			@RequestParam(required = false) Integer idSucursal, @RequestParam(required = false) Integer idEmpleado,
			@AuthenticationPrincipal UsuarioAutenticado usuario) {
		return estadisticaService.ventasPorDia(filtro(desde, hasta, idSucursal, idEmpleado, usuario));
	}

	@GetMapping("/por-sucursal")
	@Operation(summary = "Ventas agrupadas por sucursal")
	public List<Agrupado> porSucursal(
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
			@RequestParam(required = false) Integer idSucursal, @RequestParam(required = false) Integer idEmpleado,
			@AuthenticationPrincipal UsuarioAutenticado usuario) {
		return estadisticaService.porSucursal(filtro(desde, hasta, idSucursal, idEmpleado, usuario));
	}

	@GetMapping("/por-vendedor")
	@Operation(summary = "Ventas agrupadas por vendedor")
	public List<Agrupado> porVendedor(
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
			@RequestParam(required = false) Integer idSucursal, @RequestParam(required = false) Integer idEmpleado,
			@AuthenticationPrincipal UsuarioAutenticado usuario) {
		return estadisticaService.porVendedor(filtro(desde, hasta, idSucursal, idEmpleado, usuario));
	}

	@GetMapping("/por-metodo-pago")
	@Operation(summary = "Ventas agrupadas por método de pago")
	public List<Agrupado> porMetodoPago(
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
			@RequestParam(required = false) Integer idSucursal, @RequestParam(required = false) Integer idEmpleado,
			@AuthenticationPrincipal UsuarioAutenticado usuario) {
		return estadisticaService.porMetodoPago(filtro(desde, hasta, idSucursal, idEmpleado, usuario));
	}

	@GetMapping("/productos-mas-vendidos")
	@Operation(summary = "Productos ordenados por dinero vendido en el periodo")
	public List<ProductoVendido> productosMasVendidos(
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
			@RequestParam(required = false) Integer idSucursal, @RequestParam(required = false) Integer idEmpleado,
			@RequestParam(defaultValue = "10") int limite, @AuthenticationPrincipal UsuarioAutenticado usuario) {
		return estadisticaService.productosMasVendidos(filtro(desde, hasta, idSucursal, idEmpleado, usuario), limite);
	}

	@GetMapping("/rendimiento-productos")
	@Operation(summary = "Todo el catálogo con unidades, ventas y utilidad estimada con el costo actual")
	public List<ProductoVendido> rendimientoPorProducto(
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
			@RequestParam(required = false) Integer idSucursal, @RequestParam(required = false) Integer idEmpleado,
			@AuthenticationPrincipal UsuarioAutenticado usuario) {
		return estadisticaService.rendimientoPorProducto(filtro(desde, hasta, idSucursal, idEmpleado, usuario));
	}

	@GetMapping("/productos/{idProducto}")
	@Operation(summary = "Unidades y dinero vendidos de un producto, día por día")
	public VentasProducto ventasDeProducto(@PathVariable Integer idProducto,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
			@RequestParam(required = false) Integer idSucursal, @AuthenticationPrincipal UsuarioAutenticado usuario) {
		return estadisticaService.ventasDeProducto(idProducto, filtro(desde, hasta, idSucursal, null, usuario));
	}

	@GetMapping("/unidades-por-inventario")
	@Operation(summary = "Unidades vendidas por par producto-sucursal, base del ritmo de ventas y del reabastecimiento")
	public List<UnidadesInventario> unidadesPorInventario(
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
			@RequestParam(required = false) Integer idSucursal, @AuthenticationPrincipal UsuarioAutenticado usuario) {
		return estadisticaService.unidadesPorInventario(filtro(desde, hasta, idSucursal, null, usuario));
	}

	@GetMapping("/sucursales")
	@Operation(summary = "Comparativo de sucursales: ventas, ticket, utilidad, productos en stock bajo y capital en inventario")
	public List<SucursalDetalle> sucursalesDetalle(
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
			@RequestParam(required = false) Integer idSucursal, @AuthenticationPrincipal UsuarioAutenticado usuario) {
		return estadisticaService.sucursalesDetalle(filtro(desde, hasta, idSucursal, null, usuario));
	}

	@GetMapping("/comisiones")
	@PreAuthorize("hasRole('VENDEDOR') or hasAuthority('NIVEL_3')")
	@Operation(summary = "Comisión de cada vendedor en el periodo; el vendedor solo ve la suya")
	public List<Comision> comisiones(
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
			@RequestParam(required = false) Integer idSucursal, @RequestParam(required = false) Integer idEmpleado,
			@AuthenticationPrincipal UsuarioAutenticado usuario) {
		return estadisticaService.comisiones(filtro(desde, hasta, idSucursal, idEmpleado, usuario), usuario);
	}

	@GetMapping("/stock-bajo")
	@Operation(summary = "Productos con cantidad menor o igual al umbral (5 por defecto) en cada sucursal")
	public List<StockBajo> stockBajo(@RequestParam(required = false) Integer idSucursal, @RequestParam(required = false) Integer umbral,
			@AuthenticationPrincipal UsuarioAutenticado usuario) {
		return estadisticaService.stockBajo(idSucursal, umbral, usuario);
	}
}
