package co.edu.unbosque.backbases.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record InventarioEntradaDTO(
		@NotNull(message = "La cantidad disponible es obligatoria.")
		@PositiveOrZero(message = "La cantidad disponible no puede ser negativa.")
		Integer cantidadDisponible,

		@NotBlank(message = "La ubicación en bodega es obligatoria.")
		@Size(max = 40, message = "La ubicación en bodega admite máximo 40 caracteres.")
		String ubicacionBodega) {
}
