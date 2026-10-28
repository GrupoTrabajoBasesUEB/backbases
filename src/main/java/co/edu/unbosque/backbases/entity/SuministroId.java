package co.edu.unbosque.backbases.entity;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class SuministroId implements Serializable {

	private static final long serialVersionUID = 1L;

	@Column(name = "NIT", length = 15)
	private String nit;

	@Column(name = "ID_PRODUCTO")
	private Integer idProducto;

	@Column(name = "ID_SUCURSAL")
	private Integer idSucursal;

	protected SuministroId() {
	}

	public SuministroId(String nit, Integer idProducto, Integer idSucursal) {
		this.nit = nit;
		this.idProducto = idProducto;
		this.idSucursal = idSucursal;
	}

	public String getNit() {
		return nit;
	}

	public Integer getIdProducto() {
		return idProducto;
	}

	public Integer getIdSucursal() {
		return idSucursal;
	}

	@Override
	public boolean equals(Object otro) {
		return otro instanceof SuministroId id && Objects.equals(nit, id.nit) && Objects.equals(idProducto, id.idProducto)
				&& Objects.equals(idSucursal, id.idSucursal);
	}

	@Override
	public int hashCode() {
		return Objects.hash(nit, idProducto, idSucursal);
	}
}
