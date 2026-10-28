package co.edu.unbosque.backbases.exception;

import org.springframework.http.HttpStatus;

public class SinPermisoException extends ApiException {

	private static final long serialVersionUID = 1L;

	public SinPermisoException(String mensaje) {
		super(HttpStatus.FORBIDDEN, "SIN_PERMISO", mensaje);
	}
}
