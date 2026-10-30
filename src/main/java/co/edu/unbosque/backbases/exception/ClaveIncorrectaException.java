package co.edu.unbosque.backbases.exception;

import org.springframework.http.HttpStatus;

public class ClaveIncorrectaException extends ApiException {

	private static final long serialVersionUID = 1L;

	public ClaveIncorrectaException() {
		super(HttpStatus.FORBIDDEN, "CLAVE_INCORRECTA", "La clave de seguridad no es correcta.");
	}
}
