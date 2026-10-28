package co.edu.unbosque.backbases.dto;

import java.util.List;

public record ClienteDTO(
		Integer idCliente,
		TipoCliente tipo,
		String numeroDocumento,
		String direccionCliente,
		List<String> telefonos,
		List<String> correos,
		String nombre,
		String apellido,
		String razonSocial,
		String nombreMostrado,
		boolean consumidorFinal) {
}
