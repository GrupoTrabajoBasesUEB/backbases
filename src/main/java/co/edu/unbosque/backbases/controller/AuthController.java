package co.edu.unbosque.backbases.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unbosque.backbases.dto.EmpleadoDTO;
import co.edu.unbosque.backbases.dto.LoginDTO;
import co.edu.unbosque.backbases.dto.SesionDTO;
import co.edu.unbosque.backbases.security.UsuarioAutenticado;
import co.edu.unbosque.backbases.service.AuthService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

	private final AuthService authService;

	public AuthController(AuthService authService) {
		this.authService = authService;
	}

	@PostMapping("/login")
	public SesionDTO login(@Valid @RequestBody LoginDTO login) {
		return authService.iniciarSesion(login);
	}

	@GetMapping("/yo")
	public EmpleadoDTO yo(@AuthenticationPrincipal UsuarioAutenticado usuario) {
		return authService.perfil(usuario);
	}
}
