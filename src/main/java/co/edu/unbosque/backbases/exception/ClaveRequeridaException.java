package co.edu.unbosque.backbases.exception;

import org.springframework.http.HttpStatus;

public class ClaveRequeridaException extends ApiException {

	private static final long serialVersionUID = 1L;

	public ClaveRequeridaException() {
		super(HttpStatus.BAD_REQUEST, "CLAVE_REQUERIDA", "Escriba su clave de seguridad para eliminar.");
	}
}
