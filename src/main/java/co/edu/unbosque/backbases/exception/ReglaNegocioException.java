package co.edu.unbosque.backbases.exception;

import org.springframework.http.HttpStatus;

public class ReglaNegocioException extends ApiException {

	private static final long serialVersionUID = 1L;

	public ReglaNegocioException(String mensaje) {
		super(HttpStatus.BAD_REQUEST, "REGLA_NEGOCIO", mensaje);
	}
}
