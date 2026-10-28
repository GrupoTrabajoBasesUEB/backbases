package co.edu.unbosque.backbases.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record EvaluacionEntradaDTO(
		@NotBlank(message = "El NIT del proveedor es obligatorio.")
		String nit,

		@NotNull(message = "El producto es obligatorio.")
		Integer idProducto,

		@NotNull(message = "La sucursal es obligatoria.")
		Integer idSucursal,

		LocalDate fechaEvaluacion,

		@NotNull(message = "La calificación es obligatoria.")
		@Min(value = 1, message = "La calificación debe estar entre 1 y 5.")
		@Max(value = 5, message = "La calificación debe estar entre 1 y 5.")
		Integer calificacion,

		@NotNull(message = "Indique si la entrega llegó a tiempo.")
		Boolean cumplimientoTiempo,

		@Size(max = 500, message = "Las observaciones admiten máximo 500 caracteres.")
		String observaciones) {
}
