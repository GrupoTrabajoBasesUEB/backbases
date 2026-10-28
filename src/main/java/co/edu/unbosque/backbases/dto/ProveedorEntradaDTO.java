package co.edu.unbosque.backbases.dto;

import java.util.List;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ProveedorEntradaDTO(
		@NotBlank(message = "El NIT es obligatorio.")
		@Pattern(regexp = "\\d{6,12}(-\\d)?", message = "El NIT debe tener entre 6 y 12 dígitos y, opcionalmente, el dígito de verificación (900215334-1).")
		String nit,

		@NotBlank(message = "El nombre del proveedor es obligatorio.")
		@Size(max = 100, message = "El nombre del proveedor admite máximo 100 caracteres.")
		String nombreProveedor,

		@NotBlank(message = "La dirección es obligatoria.")
		@Size(max = 120, message = "La dirección admite máximo 120 caracteres.")
		String direccionProveedor,

		@NotEmpty(message = "Registre al menos un teléfono.")
		List<@NotBlank(message = "Un teléfono está vacío.") @Size(max = 20, message = "Cada teléfono admite máximo 20 caracteres.") String> telefonos,

		@NotEmpty(message = "Registre al menos un correo.")
		List<@NotBlank(message = "Un correo está vacío.") @Email(message = "Un correo no tiene un formato válido.") @Size(max = 120, message = "Cada correo admite máximo 120 caracteres.") String> correos) {
}
