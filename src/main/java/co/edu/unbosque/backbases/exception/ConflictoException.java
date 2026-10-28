package co.edu.unbosque.backbases.exception;

import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;

public class ConflictoException extends ApiException {

	private static final long serialVersionUID = 1L;

	public ConflictoException(String mensaje) {
		super(HttpStatus.CONFLICT, "CONFLICTO", mensaje);
	}

	public static void siEstaEnUso(String entidad, Map<String, Long> referencias) {
		String detalle = referencias.entrySet().stream()
				.filter(r -> r.getValue() > 0)
				.map(r -> r.getValue() + " " + r.getKey())
				.collect(Collectors.joining(", "));
		if (!detalle.isEmpty()) {
			throw new ConflictoException("No se puede eliminar " + entidad + ": la usan " + detalle + ".");
		}
	}
}
