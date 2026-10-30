package co.edu.unbosque.backbases.security;

import java.io.IOException;

import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private static final String PREFIJO = "Bearer ";

	private final JwtService jwtService;

	public JwtAuthenticationFilter(JwtService jwtService) {
		this.jwtService = jwtService;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain cadena)
			throws ServletException, IOException {
		String cabecera = request.getHeader(HttpHeaders.AUTHORIZATION);
		if (cabecera != null && cabecera.startsWith(PREFIJO)) {
			jwtService.leer(cabecera.substring(PREFIJO.length()).trim()).ifPresent(usuario -> {
				var autenticacion = new UsernamePasswordAuthenticationToken(usuario, null, usuario.permisos());
				SecurityContextHolder.getContext().setAuthentication(autenticacion);
			});
		}
		cadena.doFilter(request, response);
	}
}
