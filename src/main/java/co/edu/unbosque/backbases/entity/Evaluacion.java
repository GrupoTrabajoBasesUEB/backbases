package co.edu.unbosque.backbases.entity;

import java.time.LocalDate;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinColumns;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "EVALUACION")
public class Evaluacion {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ID_EVALUACION")
	private Integer idEvaluacion;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumns({
			@JoinColumn(name = "NIT", referencedColumnName = "NIT"),
			@JoinColumn(name = "ID_PRODUCTO", referencedColumnName = "ID_PRODUCTO"),
			@JoinColumn(name = "ID_SUCURSAL", referencedColumnName = "ID_SUCURSAL") })
	private Suministro suministro;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "ID_EMPLEADO", nullable = false)
	private Administrador administrador;

	@Column(name = "FECHA_EVALUACION", nullable = false)
	private LocalDate fechaEvaluacion;

	@JdbcTypeCode(SqlTypes.TINYINT)
	@Column(name = "CALIFICACION", nullable = false)
	private Integer calificacion;

	@Column(name = "CUMPLIMIENTO_TIEMPO", nullable = false)
	private Boolean cumplimientoTiempo;

	@Column(name = "OBSERVACIONES", length = 500)
	private String observaciones;

	public Integer getIdEvaluacion() {
		return idEvaluacion;
	}

	public Suministro getSuministro() {
		return suministro;
	}

	public void setSuministro(Suministro suministro) {
		this.suministro = suministro;
	}

	public Administrador getAdministrador() {
		return administrador;
	}

	public void setAdministrador(Administrador administrador) {
		this.administrador = administrador;
	}

	public LocalDate getFechaEvaluacion() {
		return fechaEvaluacion;
	}

	public void setFechaEvaluacion(LocalDate fechaEvaluacion) {
		this.fechaEvaluacion = fechaEvaluacion;
	}

	public Integer getCalificacion() {
		return calificacion;
	}

	public void setCalificacion(Integer calificacion) {
		this.calificacion = calificacion;
	}

	public Boolean getCumplimientoTiempo() {
		return cumplimientoTiempo;
	}

	public void setCumplimientoTiempo(Boolean cumplimientoTiempo) {
		this.cumplimientoTiempo = cumplimientoTiempo;
	}

	public String getObservaciones() {
		return observaciones;
	}

	public void setObservaciones(String observaciones) {
		this.observaciones = observaciones;
	}
}
