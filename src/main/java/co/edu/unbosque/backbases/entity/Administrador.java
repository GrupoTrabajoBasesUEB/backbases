package co.edu.unbosque.backbases.entity;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "ADMINISTRADOR")
public class Administrador {

	@Id
	@Column(name = "ID_EMPLEADO")
	private Integer idEmpleado;

	@MapsId
	@OneToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "ID_EMPLEADO")
	private Empleado empleado;

	@JdbcTypeCode(SqlTypes.TINYINT)
	@Column(name = "NIVEL_ACCESO", nullable = false)
	private Integer nivelAcceso;

	@Column(name = "CLAVE_SEGURIDAD", nullable = false, length = 255)
	private String claveSeguridad;

	public Integer getIdEmpleado() {
		return idEmpleado;
	}

	public Empleado getEmpleado() {
		return empleado;
	}

	public void setEmpleado(Empleado empleado) {
		this.empleado = empleado;
	}

	public Integer getNivelAcceso() {
		return nivelAcceso;
	}

	public void setNivelAcceso(Integer nivelAcceso) {
		this.nivelAcceso = nivelAcceso;
	}

	public String getClaveSeguridad() {
		return claveSeguridad;
	}

	public void setClaveSeguridad(String claveSeguridad) {
		this.claveSeguridad = claveSeguridad;
	}
}
