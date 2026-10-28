package co.edu.unbosque.backbases.exception;

import org.springframework.http.HttpStatus;

public class ConflictoException extends ApiException {

	private static final long serialVersionUID = 1L;

	public ConflictoException(String mensaje) {
		super(HttpStatus.CONFLICT, "CONFLICTO", mensaje);
	}
}
