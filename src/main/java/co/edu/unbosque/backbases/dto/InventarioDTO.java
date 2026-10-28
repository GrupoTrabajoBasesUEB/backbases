package co.edu.unbosque.backbases.dto;

import co.edu.unbosque.backbases.entity.Inventario;

public record InventarioDTO(
		Integer idSucursal,
		String nombreSucursal,
		Integer idProducto,
		String codigo,
		String nombreProducto,
		Integer cantidadDisponible,
		String ubicacionBodega) {

	public static InventarioDTO de(Inventario i) {
		return new InventarioDTO(i.getSucursal().getIdSucursal(), i.getSucursal().getNombre(), i.getProducto().getIdProducto(),
				i.getProducto().getCodigo(), i.getProducto().getNombre(), i.getCantidadDisponible(), i.getUbicacionBodega());
	}
}
