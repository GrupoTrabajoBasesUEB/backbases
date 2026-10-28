package co.edu.unbosque.backbases.dto;

import java.math.BigDecimal;
import java.util.List;

public record EmpleadoDTO(
		Integer idEmpleado,
		String documento,
		String nombre,
		String apellido,
		String cargo,
		String correo,
		List<String> telefonos,
		Rol rol,
		Integer nivelAcceso,
		BigDecimal comisionVenta,
		Integer idSucursal,
		String nombreSucursal) {
}
