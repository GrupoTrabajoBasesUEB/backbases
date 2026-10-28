package co.edu.unbosque.backbases.dto;

import java.math.BigDecimal;
import java.util.List;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record EmpleadoEntradaDTO(
		@NotBlank(message = "El documento es obligatorio.")
		@Size(max = 20, message = "El documento admite máximo 20 caracteres.")
		String documento,

		@NotBlank(message = "El nombre es obligatorio.")
		@Size(max = 60, message = "El nombre admite máximo 60 caracteres.")
		String nombre,

		@NotBlank(message = "El apellido es obligatorio.")
		@Size(max = 60, message = "El apellido admite máximo 60 caracteres.")
		String apellido,

		@NotBlank(message = "El cargo es obligatorio.")
		@Size(max = 60, message = "El cargo admite máximo 60 caracteres.")
		String cargo,

		@NotBlank(message = "El correo es obligatorio.")
		@Email(message = "El correo no tiene un formato válido.")
		@Size(max = 120, message = "El correo admite máximo 120 caracteres.")
		String correo,

		@NotEmpty(message = "Registre al menos un teléfono.")
		List<@NotBlank(message = "Un teléfono está vacío.") @Size(max = 20, message = "Cada teléfono admite máximo 20 caracteres.") String> telefonos,

		@NotNull(message = "El rol es obligatorio.")
		Rol rol,

		@Size(min = 8, max = 72, message = "La contraseña debe tener entre 8 y 72 caracteres.")
		String contrasena,

		@Min(value = 1, message = "El nivel de acceso va de 1 a 3.")
		@Max(value = 3, message = "El nivel de acceso va de 1 a 3.")
		Integer nivelAcceso,

		@Size(min = 6, max = 72, message = "La clave de seguridad debe tener entre 6 y 72 caracteres.")
		String claveSeguridad,

		@DecimalMin(value = "0", message = "La comisión va de 0 a 1.")
		@DecimalMax(value = "1", message = "La comisión va de 0 a 1.")
		BigDecimal comisionVenta,

		Integer idSucursal) {
}
