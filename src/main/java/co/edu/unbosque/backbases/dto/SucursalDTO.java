package co.edu.unbosque.backbases.dto;

import co.edu.unbosque.backbases.entity.Empleado;
import co.edu.unbosque.backbases.entity.Sucursal;

public record SucursalDTO(Integer idSucursal, String nombreSucursal, String ciudad, String direccion, String telefono,
		Integer idAdministrador, String nombreAdministrador) {

	public static SucursalDTO de(Sucursal s) {
		Empleado encargado = s.getAdministrador().getEmpleado();
		return new SucursalDTO(s.getIdSucursal(), s.getNombre(), s.getCiudad(), s.getDireccion(), s.getTelefono(),
				encargado.getIdEmpleado(), encargado.getNombre() + " " + encargado.getApellido());
	}
}
