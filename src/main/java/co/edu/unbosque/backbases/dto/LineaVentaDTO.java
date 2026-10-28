package co.edu.unbosque.backbases.dto;

import java.math.BigDecimal;

import co.edu.unbosque.backbases.entity.DetalleVenta;

public record LineaVentaDTO(Integer idProducto, String codigo, String nombreProducto, Integer cantidad, BigDecimal precioUnitario,
		BigDecimal subtotal) {

	public static LineaVentaDTO de(DetalleVenta d) {
		return new LineaVentaDTO(d.getProducto().getIdProducto(), d.getProducto().getCodigo(), d.getProducto().getNombre(),
				d.getCantidad(), d.getPrecioUnitario(), d.getSubtotal());
	}
}
