package co.edu.unbosque.backbases.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public record SuministroEntradaDTO(
		@NotBlank(message = "El NIT del proveedor es obligatorio.")
		String nit,

		@NotNull(message = "El producto es obligatorio.")
		Integer idProducto,

		@NotNull(message = "La sucursal es obligatoria.")
		Integer idSucursal,

		@NotNull(message = "El precio de suministro es obligatorio.")
		@Positive(message = "El precio de suministro debe ser mayor que cero.")
		@Digits(integer = 10, fraction = 2, message = "El precio admite hasta 10 enteros y 2 decimales.")
		BigDecimal precioSuministro,

		@NotNull(message = "El tiempo de entrega es obligatorio.")
		@PositiveOrZero(message = "Los días de entrega no pueden ser negativos.")
		@Max(value = 365, message = "El tiempo de entrega no puede superar 365 días.")
		Integer tiempoEntregaDias) {
}
