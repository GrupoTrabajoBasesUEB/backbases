package co.edu.unbosque.backbases.security;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.backbases.entity.Empleado;
import co.edu.unbosque.backbases.repository.EmpleadoRepository;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

	private final EmpleadoRepository empleadoRepository;

	public UserDetailsServiceImpl(EmpleadoRepository empleadoRepository) {
		this.empleadoRepository = empleadoRepository;
	}

	@Override
	@Transactional(readOnly = true)
	public UserDetails loadUserByUsername(String correo) {
		Empleado empleado = empleadoRepository.findByCorreo(correo == null ? "" : correo.trim())
				.orElseThrow(() -> new UsernameNotFoundException("No existe un empleado con ese correo."));
		return User.withUsername(empleado.getCorreo()).password(empleado.getContrasena()).authorities(new String[0]).build();
	}
}
