package co.edu.unbosque.backbases.security;

import org.springframework.http.HttpMethod;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import co.edu.unbosque.backbases.dto.Rol;
import co.edu.unbosque.backbases.exception.ClaveIncorrectaException;
import co.edu.unbosque.backbases.service.ClaveSeguridadService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class ClaveSeguridadInterceptor implements HandlerInterceptor {

	public static final String ENCABEZADO = "X-Clave-Seguridad";
	private static final String ROL_ADMINISTRADOR = "ROLE_" + Rol.ADMINISTRADOR.name();

	private final ClaveSeguridadService claveSeguridadService;

	public ClaveSeguridadInterceptor(ClaveSeguridadService claveSeguridadService) {
		this.claveSeguridadService = claveSeguridadService;
	}

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
		if (!HttpMethod.DELETE.matches(request.getMethod())) {
			return true;
		}
		Authentication autenticacion = SecurityContextHolder.getContext().getAuthentication();
		if (autenticacion == null || autenticacion.getAuthorities().stream().noneMatch(a -> ROL_ADMINISTRADOR.equals(a.getAuthority()))) {
			return true;
		}
		if (!(autenticacion.getPrincipal() instanceof UsuarioAutenticado usuario)) {
			throw new ClaveIncorrectaException();
		}
		claveSeguridadService.verificar(usuario.idEmpleado(), request.getHeader(ENCABEZADO));
		return true;
	}
}
