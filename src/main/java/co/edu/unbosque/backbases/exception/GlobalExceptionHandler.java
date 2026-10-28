package co.edu.unbosque.backbases.exception;

import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import co.edu.unbosque.backbases.dto.ErrorDTO;

@RestControllerAdvice
public class GlobalExceptionHandler {

	private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	@ExceptionHandler(ApiException.class)
	public ResponseEntity<ErrorDTO> api(ApiException e) {
		return responder(e.getEstado(), e.getCodigo(), e.getMessage());
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorDTO> validacion(MethodArgumentNotValidException e) {
		Map<String, String> campos = new LinkedHashMap<>();
		e.getBindingResult().getFieldErrors().forEach(f -> campos.putIfAbsent(f.getField(), f.getDefaultMessage()));
		return ResponseEntity.badRequest()
				.body(ErrorDTO.conCampos(400, "VALIDACION", "Hay datos inválidos o incompletos.", campos));
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ErrorDTO> cuerpoIlegible(HttpMessageNotReadableException e) {
		return responder(HttpStatus.BAD_REQUEST, "CUERPO_INVALIDO", "El cuerpo de la petición no es un JSON válido o tiene tipos incorrectos.");
	}

	@ExceptionHandler({ MethodArgumentTypeMismatchException.class, MissingServletRequestParameterException.class })
	public ResponseEntity<ErrorDTO> parametro(Exception e) {
		return responder(HttpStatus.BAD_REQUEST, "PARAMETRO_INVALIDO", "Un parámetro de la petición falta o tiene un valor inválido.");
	}

	@ExceptionHandler(DataIntegrityViolationException.class)
	public ResponseEntity<ErrorDTO> integridad(DataIntegrityViolationException e) {
		log.warn("Restricción de la base de datos: {}", e.getMostSpecificCause().getMessage());
		return responder(HttpStatus.CONFLICT, "INTEGRIDAD", "La operación viola una restricción de la base de datos.");
	}

	@ExceptionHandler(AccessDeniedException.class)
	public ResponseEntity<ErrorDTO> sinPermiso(AccessDeniedException e) {
		return responder(HttpStatus.FORBIDDEN, "SIN_PERMISO", "No tiene permiso para esta operación.");
	}

	@ExceptionHandler(AuthenticationException.class)
	public ResponseEntity<ErrorDTO> noAutenticado(AuthenticationException e) {
		return responder(HttpStatus.UNAUTHORIZED, "NO_AUTENTICADO", "Debe iniciar sesión.");
	}

	@ExceptionHandler(NoResourceFoundException.class)
	public ResponseEntity<ErrorDTO> rutaInexistente(NoResourceFoundException e) {
		return responder(HttpStatus.NOT_FOUND, "RUTA_INEXISTENTE", "La ruta pedida no existe.");
	}

	@ExceptionHandler(HttpRequestMethodNotSupportedException.class)
	public ResponseEntity<ErrorDTO> metodo(HttpRequestMethodNotSupportedException e) {
		return responder(HttpStatus.METHOD_NOT_ALLOWED, "METODO_NO_PERMITIDO", "La ruta no admite el método " + e.getMethod() + ".");
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorDTO> inesperado(Exception e) {
		log.error("Error no controlado", e);
		return responder(HttpStatus.INTERNAL_SERVER_ERROR, "ERROR_INTERNO", "Ocurrió un error inesperado. Intente de nuevo.");
	}

	private static ResponseEntity<ErrorDTO> responder(HttpStatus estado, String codigo, String mensaje) {
		return ResponseEntity.status(estado).body(ErrorDTO.de(estado.value(), codigo, mensaje));
	}
}
