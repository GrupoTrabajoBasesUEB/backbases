package co.edu.unbosque.backbases.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SucursalEntradaDTO(
		@NotBlank(message = "El nombre de la sucursal es obligatorio.")
		@Size(max = 80, message = "El nombre de la sucursal admite máximo 80 caracteres.")
		String nombreSucursal,

		@NotBlank(message = "La ciudad es obligatoria.")
		@Size(max = 60, message = "La ciudad admite máximo 60 caracteres.")
		String ciudad,

		@NotBlank(message = "La dirección es obligatoria.")
		@Size(max = 120, message = "La dirección admite máximo 120 caracteres.")
		String direccion,

		@NotBlank(message = "El teléfono es obligatorio.")
		@Size(max = 20, message = "El teléfono admite máximo 20 caracteres.")
		String telefono,

		@NotNull(message = "La sucursal necesita un administrador encargado.")
		Integer idAdministrador) {
}
