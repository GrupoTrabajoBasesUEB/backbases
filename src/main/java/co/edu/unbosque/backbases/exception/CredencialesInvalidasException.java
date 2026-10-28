package co.edu.unbosque.backbases.exception;

import org.springframework.http.HttpStatus;

public class CredencialesInvalidasException extends ApiException {

	private static final long serialVersionUID = 1L;

	public CredencialesInvalidasException() {
		super(HttpStatus.UNAUTHORIZED, "CREDENCIALES_INVALIDAS", "Correo o contraseña incorrectos.");
	}
}
