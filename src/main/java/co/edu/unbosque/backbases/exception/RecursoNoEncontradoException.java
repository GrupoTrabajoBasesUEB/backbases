package co.edu.unbosque.backbases.exception;

import org.springframework.http.HttpStatus;

public class RecursoNoEncontradoException extends ApiException {

	private static final long serialVersionUID = 1L;

	public RecursoNoEncontradoException(String mensaje) {
		super(HttpStatus.NOT_FOUND, "NO_ENCONTRADO", mensaje);
	}

	public static RecursoNoEncontradoException de(String recurso, Object id) {
		return new RecursoNoEncontradoException("No existe " + recurso + " con id " + id + ".");
	}
}
