package co.edu.unbosque.backbases.entity;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class DetalleVentaId implements Serializable {

	private static final long serialVersionUID = 1L;

	@Column(name = "ID_VENTA")
	private Integer idVenta;

	@Column(name = "ID_PRODUCTO")
	private Integer idProducto;

	protected DetalleVentaId() {
	}

	public DetalleVentaId(Integer idVenta, Integer idProducto) {
		this.idVenta = idVenta;
		this.idProducto = idProducto;
	}

	public Integer getIdVenta() {
		return idVenta;
	}

	public Integer getIdProducto() {
		return idProducto;
	}

	@Override
	public boolean equals(Object otro) {
		return otro instanceof DetalleVentaId id && Objects.equals(idVenta, id.idVenta) && Objects.equals(idProducto, id.idProducto);
	}

	@Override
	public int hashCode() {
		return Objects.hash(idVenta, idProducto);
	}
}
