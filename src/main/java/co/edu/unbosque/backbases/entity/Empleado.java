package co.edu.unbosque.backbases.entity;

import java.util.LinkedHashSet;
import java.util.Set;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

@Entity
@Table(name = "EMPLEADO")
public class Empleado {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ID_EMPLEADO")
	private Integer idEmpleado;

	@Column(name = "DOCUMENTO", nullable = false, length = 20)
	private String documento;

	@Column(name = "NOMBRE", nullable = false, length = 60)
	private String nombre;

	@Column(name = "APELLIDO", nullable = false, length = 60)
	private String apellido;

	@Column(name = "CARGO", nullable = false, length = 60)
	private String cargo;

	@Column(name = "CORREO", nullable = false, length = 120)
	private String correo;

	@Column(name = "CONTRASENA", nullable = false, length = 255)
	private String contrasena;

	@ElementCollection
	@CollectionTable(name = "TELEFONO_EMPLEADO", joinColumns = @JoinColumn(name = "ID_EMPLEADO"))
	@Column(name = "TELEFONO", length = 20)
	private Set<String> telefonos = new LinkedHashSet<>();

	public Integer getIdEmpleado() {
		return idEmpleado;
	}

	public void setIdEmpleado(Integer idEmpleado) {
		this.idEmpleado = idEmpleado;
	}

	public String getDocumento() {
		return documento;
	}

	public void setDocumento(String documento) {
		this.documento = documento;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getApellido() {
		return apellido;
	}

	public void setApellido(String apellido) {
		this.apellido = apellido;
	}

	public String getCargo() {
		return cargo;
	}

	public void setCargo(String cargo) {
		this.cargo = cargo;
	}

	public String getCorreo() {
		return correo;
	}

	public void setCorreo(String correo) {
		this.correo = correo;
	}

	public String getContrasena() {
		return contrasena;
	}

	public void setContrasena(String contrasena) {
		this.contrasena = contrasena;
	}

	public Set<String> getTelefonos() {
		return telefonos;
	}

	public void setTelefonos(Set<String> telefonos) {
		this.telefonos = telefonos;
	}
}
