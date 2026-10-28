package co.edu.unbosque.backbases.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public final class EstadisticasDTO {

	private EstadisticasDTO() {
	}

	public record Resumen(BigDecimal total, long cantidad, long unidades, BigDecimal ticketPromedio) {
	}

	public record Comparativo(LocalDate desde, LocalDate hasta, Resumen actual, LocalDate desdeAnterior, LocalDate hastaAnterior,
			Resumen anterior) {
	}

	public record VentasDia(LocalDate fecha, BigDecimal total, long cantidad) {
	}

	public record Agrupado(Integer id, String nombre, BigDecimal total, long cantidad) {
	}

	public record ProductoVendido(Integer idProducto, String codigo, String nombreProducto, long unidades, BigDecimal total,
			BigDecimal utilidadEstimada) {
	}

	public record VentasProducto(Integer idProducto, long unidades, BigDecimal total, List<VentasDia> porDia) {
	}

	public record UnidadesInventario(Integer idProducto, Integer idSucursal, long unidades) {
	}

	public record SucursalDetalle(Integer idSucursal, String nombreSucursal, String ciudad, long cantidad, BigDecimal total,
			BigDecimal ticketPromedio, BigDecimal utilidadEstimada, long productosStockBajo, BigDecimal capitalInventario) {
	}

	public record Comision(Integer idEmpleado, String nombre, List<String> sucursales, long cantidad, BigDecimal total,
			BigDecimal ticketPromedio, BigDecimal tasa, BigDecimal comision) {
	}

	public record StockBajo(Integer idSucursal, String nombreSucursal, Integer idProducto, String codigo, String nombreProducto,
			int cantidadDisponible, String ubicacionBodega) {
	}
}
