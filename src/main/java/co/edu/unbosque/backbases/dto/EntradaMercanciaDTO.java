package co.edu.unbosque.backbases.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record EntradaMercanciaDTO(
		@NotNull(message = "Las unidades recibidas son obligatorias.")
		@Positive(message = "Las unidades recibidas deben ser mayores que cero.")
		@Max(value = 100000, message = "Las unidades recibidas no pueden superar 100000 por entrada.")
		Integer unidades,

		@Size(max = 40, message = "La ubicación en bodega admite máximo 40 caracteres.")
		String ubicacionBodega) {
}
