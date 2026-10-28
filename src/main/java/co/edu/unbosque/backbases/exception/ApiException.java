package co.edu.unbosque.backbases.exception;

import org.springframework.http.HttpStatus;

public abstract class ApiException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	private final HttpStatus estado;
	private final String codigo;

	protected ApiException(HttpStatus estado, String codigo, String mensaje) {
		super(mensaje);
		this.estado = estado;
		this.codigo = codigo;
	}

	public HttpStatus getEstado() {
		return estado;
	}

	public String getCodigo() {
		return codigo;
	}
}
