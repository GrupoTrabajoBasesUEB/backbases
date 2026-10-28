package co.edu.unbosque.backbases.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;

@Entity
@Table(name = "INVENTARIO")
public class Inventario {

	@EmbeddedId
	private InventarioId id;

	@MapsId("idSucursal")
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "ID_SUCURSAL")
	private Sucursal sucursal;

	@MapsId("idProducto")
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "ID_PRODUCTO")
	private Producto producto;

	@Column(name = "CANTIDAD_DISPONIBLE", nullable = false)
	private Integer cantidadDisponible;

	@Column(name = "UBICACION_BODEGA", nullable = false, length = 40)
	private String ubicacionBodega;

	protected Inventario() {
	}

	public Inventario(Sucursal sucursal, Producto producto) {
		this.id = new InventarioId(sucursal.getIdSucursal(), producto.getIdProducto());
		this.sucursal = sucursal;
		this.producto = producto;
	}

	public InventarioId getId() {
		return id;
	}

	public Sucursal getSucursal() {
		return sucursal;
	}

	public Producto getProducto() {
		return producto;
	}

	public Integer getCantidadDisponible() {
		return cantidadDisponible;
	}

	public void setCantidadDisponible(Integer cantidadDisponible) {
		this.cantidadDisponible = cantidadDisponible;
	}

	public String getUbicacionBodega() {
		return ubicacionBodega;
	}

	public void setUbicacionBodega(String ubicacionBodega) {
		this.ubicacionBodega = ubicacionBodega;
	}
}
