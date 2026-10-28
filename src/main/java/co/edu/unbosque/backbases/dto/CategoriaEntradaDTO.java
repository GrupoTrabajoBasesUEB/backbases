package co.edu.unbosque.backbases.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoriaEntradaDTO(
		@NotBlank(message = "El nombre de la categoría es obligatorio.")
		@Size(max = 60, message = "El nombre de la categoría admite máximo 60 caracteres.")
		String nombre,

		@Size(max = 255, message = "La descripción admite máximo 255 caracteres.")
		String descripcion,

		Integer idCategoriaPrincipal) {
}
