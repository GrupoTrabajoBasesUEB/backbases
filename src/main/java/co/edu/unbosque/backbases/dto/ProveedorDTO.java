package co.edu.unbosque.backbases.dto;

import java.util.List;

import co.edu.unbosque.backbases.entity.Proveedor;

public record ProveedorDTO(String nit, String nombreProveedor, String direccionProveedor, List<String> telefonos, List<String> correos) {

	public static ProveedorDTO de(Proveedor p) {
		return new ProveedorDTO(p.getNit(), p.getNombre(), p.getDireccion(), List.copyOf(p.getTelefonos()), List.copyOf(p.getCorreos()));
	}
}
