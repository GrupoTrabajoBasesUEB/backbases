package co.edu.unbosque.backbases.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record SuministroDTO(
		String nit,
		String nombreProveedor,
		Integer idProducto,
		String codigo,
		String nombreProducto,
		Integer idSucursal,
		String nombreSucursal,
		BigDecimal precioSuministro,
		Integer tiempoEntregaDias,
		long cantidadEvaluaciones,
		BigDecimal promedioCalificacion,
		LocalDate ultimaEvaluacion) {
}
