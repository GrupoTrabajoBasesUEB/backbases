package co.edu.unbosque.backbases.entity;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;

@Entity
@Table(name = "DETALLE_VENTA")
public class DetalleVenta {

	@EmbeddedId
	private DetalleVentaId id;

	@MapsId("idVenta")
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "ID_VENTA")
	private Venta venta;

	@MapsId("idProducto")
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "ID_PRODUCTO")
	private Producto producto;

	@Column(name = "CANTIDAD", nullable = false)
	private Integer cantidad;

	@Column(name = "PRECIO_UNITARIO", nullable = false, precision = 12, scale = 2)
	private BigDecimal precioUnitario;

	protected DetalleVenta() {
	}

	public DetalleVenta(Venta venta, Producto producto, int cantidad, BigDecimal precioUnitario) {
		this.id = new DetalleVentaId(venta.getIdVenta(), producto.getIdProducto());
		this.venta = venta;
		this.producto = producto;
		this.cantidad = cantidad;
		this.precioUnitario = precioUnitario;
	}

	public DetalleVentaId getId() {
		return id;
	}

	public Venta getVenta() {
		return venta;
	}

	public Producto getProducto() {
		return producto;
	}

	public Integer getCantidad() {
		return cantidad;
	}

	public BigDecimal getPrecioUnitario() {
		return precioUnitario;
	}

	public BigDecimal getSubtotal() {
		return precioUnitario.multiply(BigDecimal.valueOf(cantidad));
	}
}
