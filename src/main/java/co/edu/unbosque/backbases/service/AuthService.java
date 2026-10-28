package co.edu.unbosque.backbases.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.backbases.dto.EmpleadoDTO;
import co.edu.unbosque.backbases.dto.LoginDTO;
import co.edu.unbosque.backbases.dto.SesionDTO;
import co.edu.unbosque.backbases.exception.CredencialesInvalidasException;
import co.edu.unbosque.backbases.security.JwtService;
import co.edu.unbosque.backbases.security.UsuarioAutenticado;

@Service
public class AuthService {

	private final AuthenticationManager authenticationManager;
	private final EmpleadoService empleadoService;
	private final JwtService jwtService;

	public AuthService(AuthenticationManager authenticationManager, EmpleadoService empleadoService, JwtService jwtService) {
		this.authenticationManager = authenticationManager;
		this.empleadoService = empleadoService;
		this.jwtService = jwtService;
	}

	@Transactional(readOnly = true)
	public SesionDTO iniciarSesion(LoginDTO login) {
		String correo = login.correo().trim().toLowerCase();
		try {
			authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(correo, login.contrasena()));
		} catch (AuthenticationException e) {
			throw new CredencialesInvalidasException();
		}
		EmpleadoDTO empleado = empleadoService.obtenerPorCorreo(correo);
		if (empleado.rol() == null) {
			throw new CredencialesInvalidasException();
		}
		UsuarioAutenticado usuario = new UsuarioAutenticado(empleado.idEmpleado(), empleado.correo(), empleado.rol(), empleado.idSucursal());
		return SesionDTO.bearer(jwtService.generar(usuario), jwtService.getVigenciaMs(), empleado);
	}

	@Transactional(readOnly = true)
	public EmpleadoDTO perfil(UsuarioAutenticado usuario) {
		return empleadoService.obtener(usuario.idEmpleado());
	}
}
