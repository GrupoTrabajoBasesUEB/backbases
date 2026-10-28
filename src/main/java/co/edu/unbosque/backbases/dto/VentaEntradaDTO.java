package co.edu.unbosque.backbases.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record VentaEntradaDTO(
		Integer idCliente,

		@NotNull(message = "El método de pago es obligatorio.")
		Integer idMetodoPago,

		@NotEmpty(message = "La venta necesita al menos un producto.")
		@Size(max = 50, message = "Una venta admite máximo 50 líneas.")
		List<@Valid LineaVentaEntradaDTO> lineas) {
}
