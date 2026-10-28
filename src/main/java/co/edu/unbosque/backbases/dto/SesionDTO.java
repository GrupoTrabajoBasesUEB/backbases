package co.edu.unbosque.backbases.dto;

public record SesionDTO(String token, String tipo, long expiraEnMs, EmpleadoDTO empleado) {

	public static SesionDTO bearer(String token, long expiraEnMs, EmpleadoDTO empleado) {
		return new SesionDTO(token, "Bearer", expiraEnMs, empleado);
	}
}
