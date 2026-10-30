package co.edu.unbosque.backbases.service;

import java.nio.charset.StandardCharsets;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriUtils;

import co.edu.unbosque.backbases.entity.Administrador;
import co.edu.unbosque.backbases.exception.ClaveIncorrectaException;
import co.edu.unbosque.backbases.exception.ClaveRequeridaException;
import co.edu.unbosque.backbases.repository.AdministradorRepository;

@Service
public class ClaveSeguridadService {

	private final AdministradorRepository administradorRepository;
	private final PasswordEncoder passwordEncoder;

	public ClaveSeguridadService(AdministradorRepository administradorRepository, PasswordEncoder passwordEncoder) {
		this.administradorRepository = administradorRepository;
		this.passwordEncoder = passwordEncoder;
	}

	@Transactional(readOnly = true)
	public void verificar(Integer idAdministrador, String claveCodificada) {
		if (claveCodificada == null || claveCodificada.isBlank()) {
			throw new ClaveRequeridaException();
		}
		String clave;
		try {
			clave = UriUtils.decode(claveCodificada, StandardCharsets.UTF_8);
		} catch (IllegalArgumentException e) {
			throw new ClaveIncorrectaException();
		}
		String hash = administradorRepository.findById(idAdministrador)
				.map(Administrador::getClaveSeguridad)
				.orElseThrow(ClaveIncorrectaException::new);
		if (!passwordEncoder.matches(clave, hash)) {
			throw new ClaveIncorrectaException();
		}
	}
}
