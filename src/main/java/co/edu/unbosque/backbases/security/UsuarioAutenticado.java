package co.edu.unbosque.backbases.security;

import java.util.ArrayList;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import co.edu.unbosque.backbases.dto.Rol;

public record UsuarioAutenticado(Integer idEmpleado, String correo, Rol rol, Integer idSucursal, Integer nivelAcceso) {

	public boolean esAdministrador() {
		return rol == Rol.ADMINISTRADOR;
	}

	public List<GrantedAuthority> permisos() {
		List<GrantedAuthority> permisos = new ArrayList<>();
		permisos.add(new SimpleGrantedAuthority("ROLE_" + rol.name()));
		if (esAdministrador() && nivelAcceso != null) {
			for (int nivel = 1; nivel <= nivelAcceso; nivel++) {
				permisos.add(new SimpleGrantedAuthority("NIVEL_" + nivel));
			}
		}
		return permisos;
	}
}
