package co.edu.unbosque.backbases.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record LineaVentaEntradaDTO(
		@NotNull(message = "Cada línea necesita un producto.")
		Integer idProducto,

		@NotNull(message = "Cada línea necesita una cantidad.")
		@Positive(message = "La cantidad debe ser mayor que cero.")
		@Max(value = 10000, message = "La cantidad no puede superar 10000 unidades por línea.")
		Integer cantidad) {
}
