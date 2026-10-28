package co.edu.unbosque.backbases.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

import co.edu.unbosque.backbases.entity.DetalleVenta;
import co.edu.unbosque.backbases.entity.Venta;

public record LineaVendidaDTO(
		Integer idVenta,
		String numeroFactura,
		LocalDate fecha,
		LocalTime hora,
		Integer idSucursal,
		Integer idEmpleado,
		Integer idCliente,
		Integer idProducto,
		Integer cantidad,
		BigDecimal precioUnitario) {

	public static LineaVendidaDTO de(DetalleVenta d) {
		Venta v = d.getVenta();
		return new LineaVendidaDTO(v.getIdVenta(), v.getNumeroFactura(), v.getFecha(), v.getHora(), v.getSucursal().getIdSucursal(),
				v.getVendedor().getIdEmpleado(), v.getCliente().getIdCliente(), d.getProducto().getIdProducto(), d.getCantidad(),
				d.getPrecioUnitario());
	}
}
