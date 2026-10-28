package co.edu.unbosque.backbases.dto;

import java.time.OffsetDateTime;
import java.util.Map;

public record ErrorDTO(int estado, String codigo, String mensaje, Map<String, String> campos, OffsetDateTime fecha) {

	public static ErrorDTO de(int estado, String codigo, String mensaje) {
		return new ErrorDTO(estado, codigo, mensaje, Map.of(), OffsetDateTime.now());
	}

	public static ErrorDTO conCampos(int estado, String codigo, String mensaje, Map<String, String> campos) {
		return new ErrorDTO(estado, codigo, mensaje, campos, OffsetDateTime.now());
	}
}
