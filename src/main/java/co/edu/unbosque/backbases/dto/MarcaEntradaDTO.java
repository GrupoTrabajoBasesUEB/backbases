package co.edu.unbosque.backbases.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MarcaEntradaDTO(
		@NotBlank(message = "El nombre de la marca es obligatorio.")
		@Size(max = 60, message = "El nombre de la marca admite máximo 60 caracteres.")
		String nombreMarca,

		@NotBlank(message = "El país de origen es obligatorio.")
		@Size(max = 60, message = "El país de origen admite máximo 60 caracteres.")
		String paisOrigen) {
}
