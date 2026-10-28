package co.edu.unbosque.backbases.entity;

import java.util.LinkedHashSet;
import java.util.Set;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

@Entity
@Table(name = "PROVEEDOR")
public class Proveedor {

	@Id
	@Column(name = "NIT", length = 15)
	private String nit;

	@Column(name = "NOMBRE_PROVEEDOR", nullable = false, length = 100)
	private String nombre;

	@Column(name = "DIRECCION_PROVEEDOR", nullable = false, length = 120)
	private String direccion;

	@ElementCollection
	@CollectionTable(name = "TELEFONO_PROVEEDOR", joinColumns = @JoinColumn(name = "NIT"))
	@Column(name = "TELEFONO", length = 20)
	private Set<String> telefonos = new LinkedHashSet<>();

	@ElementCollection
	@CollectionTable(name = "CORREO_PROVEEDOR", joinColumns = @JoinColumn(name = "NIT"))
	@Column(name = "CORREO", length = 120)
	private Set<String> correos = new LinkedHashSet<>();

	public String getNit() {
		return nit;
	}

	public void setNit(String nit) {
		this.nit = nit;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getDireccion() {
		return direccion;
	}

	public void setDireccion(String direccion) {
		this.direccion = direccion;
	}

	public Set<String> getTelefonos() {
		return telefonos;
	}

	public Set<String> getCorreos() {
		return correos;
	}
}
