package co.edu.unbosque.backbases.dto;

import java.math.BigDecimal;

import co.edu.unbosque.backbases.entity.Producto;

public record ProductoDTO(
		Integer idProducto,
		String codigo,
		String nombreProducto,
		String descripcion,
		BigDecimal costoCompra,
		BigDecimal precioBaseVenta,
		Integer idCategoria,
		String nombreCategoria,
		Integer idMarca,
		String nombreMarca,
		long stock) {

	public static ProductoDTO de(Producto p, long stock) {
		return new ProductoDTO(p.getIdProducto(), p.getCodigo(), p.getNombre(), p.getDescripcion(), p.getCostoCompra(),
				p.getPrecioBaseVenta(), p.getCategoria().getIdCategoria(), p.getCategoria().getNombre(),
				p.getMarca().getIdMarca(), p.getMarca().getNombre(), stock);
	}
}
