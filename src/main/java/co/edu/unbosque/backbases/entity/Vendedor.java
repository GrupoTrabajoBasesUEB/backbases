package co.edu.unbosque.backbases.entity;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "VENDEDOR")
public class Vendedor {

	@Id
	@Column(name = "ID_EMPLEADO")
	private Integer idEmpleado;

	@MapsId
	@OneToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "ID_EMPLEADO")
	private Empleado empleado;

	@Column(name = "COMISION_VENTA", nullable = false, precision = 4, scale = 3)
	private BigDecimal comisionVenta;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "ID_SUCURSAL", nullable = false)
	private Sucursal sucursal;

	public Integer getIdEmpleado() {
		return idEmpleado;
	}

	public Empleado getEmpleado() {
		return empleado;
	}

	public void setEmpleado(Empleado empleado) {
		this.empleado = empleado;
	}

	public BigDecimal getComisionVenta() {
		return comisionVenta;
	}

	public void setComisionVenta(BigDecimal comisionVenta) {
		this.comisionVenta = comisionVenta;
	}

	public Sucursal getSucursal() {
		return sucursal;
	}

	public void setSucursal(Sucursal sucursal) {
		this.sucursal = sucursal;
	}
}
