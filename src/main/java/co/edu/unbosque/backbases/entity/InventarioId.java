package co.edu.unbosque.backbases.entity;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class InventarioId implements Serializable {

	private static final long serialVersionUID = 1L;

	@Column(name = "ID_SUCURSAL")
	private Integer idSucursal;

	@Column(name = "ID_PRODUCTO")
	private Integer idProducto;

	protected InventarioId() {
	}

	public InventarioId(Integer idSucursal, Integer idProducto) {
		this.idSucursal = idSucursal;
		this.idProducto = idProducto;
	}

	public Integer getIdSucursal() {
		return idSucursal;
	}

	public Integer getIdProducto() {
		return idProducto;
	}

	@Override
	public boolean equals(Object otro) {
		return otro instanceof InventarioId id && Objects.equals(idSucursal, id.idSucursal) && Objects.equals(idProducto, id.idProducto);
	}

	@Override
	public int hashCode() {
		return Objects.hash(idSucursal, idProducto);
	}
}
