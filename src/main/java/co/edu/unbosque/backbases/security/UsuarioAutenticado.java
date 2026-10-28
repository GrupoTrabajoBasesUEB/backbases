package co.edu.unbosque.backbases.security;

import co.edu.unbosque.backbases.dto.Rol;

public record UsuarioAutenticado(Integer idEmpleado, String correo, Rol rol, Integer idSucursal) {

	public boolean esAdministrador() {
		return rol == Rol.ADMINISTRADOR;
	}
}
