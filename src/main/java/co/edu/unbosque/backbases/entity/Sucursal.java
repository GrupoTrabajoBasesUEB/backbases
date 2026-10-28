package co.edu.unbosque.backbases.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "SUCURSAL")
public class Sucursal {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ID_SUCURSAL")
	private Integer idSucursal;

	@Column(name = "NOMBRE_SUCURSAL", nullable = false, length = 80)
	private String nombre;

	@Column(name = "CIUDAD", nullable = false, length = 60)
	private String ciudad;

	@Column(name = "DIRECCION", nullable = false, length = 120)
	private String direccion;

	@Column(name = "TELEFONO", nullable = false, length = 20)
	private String telefono;

	@OneToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "ID_ADMINISTRADOR", nullable = false, unique = true)
	private Administrador administrador;

	public Integer getIdSucursal() {
		return idSucursal;
	}

	public void setIdSucursal(Integer idSucursal) {
		this.idSucursal = idSucursal;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getCiudad() {
		return ciudad;
	}

	public void setCiudad(String ciudad) {
		this.ciudad = ciudad;
	}

	public String getDireccion() {
		return direccion;
	}

	public void setDireccion(String direccion) {
		this.direccion = direccion;
	}

	public String getTelefono() {
		return telefono;
	}

	public void setTelefono(String telefono) {
		this.telefono = telefono;
	}

	public Administrador getAdministrador() {
		return administrador;
	}

	public void setAdministrador(Administrador administrador) {
		this.administrador = administrador;
	}
}
