package co.edu.unbosque.backbases.dto;

import java.util.List;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ClienteEntradaDTO(
		@NotNull(message = "El tipo de cliente es obligatorio.")
		TipoCliente tipo,

		@NotBlank(message = "El número de documento es obligatorio.")
		@Size(max = 20, message = "El documento admite máximo 20 caracteres.")
		String numeroDocumento,

		@Size(max = 120, message = "La dirección admite máximo 120 caracteres.")
		String direccionCliente,

		List<@NotBlank(message = "Un teléfono está vacío.") @Size(max = 20, message = "Cada teléfono admite máximo 20 caracteres.") String> telefonos,

		List<@NotBlank(message = "Un correo está vacío.") @Email(message = "Un correo no tiene un formato válido.") @Size(max = 120, message = "Cada correo admite máximo 120 caracteres.") String> correos,

		@Size(max = 60, message = "El nombre admite máximo 60 caracteres.")
		String nombre,

		@Size(max = 60, message = "El apellido admite máximo 60 caracteres.")
		String apellido,

		@Size(max = 120, message = "La razón social admite máximo 120 caracteres.")
		String razonSocial) {
}
