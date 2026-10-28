package co.edu.unbosque.backbases.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MetodoPagoEntradaDTO(
		@NotBlank(message = "El tipo de método de pago es obligatorio.")
		@Size(max = 40, message = "El tipo admite máximo 40 caracteres.")
		String tipo) {
}
