package co.edu.unbosque.backbases.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record VentaDTO(
		Integer idVenta,
		String numeroFactura,
		LocalDate fecha,
		LocalTime hora,
		Integer idCliente,
		String nombreCliente,
		String documentoCliente,
		Integer idEmpleado,
		String nombreVendedor,
		Integer idSucursal,
		String nombreSucursal,
		Integer idMetodoPago,
		String tipoMetodoPago,
		BigDecimal montoTotal,
		List<LineaVentaDTO> lineas) {
}
