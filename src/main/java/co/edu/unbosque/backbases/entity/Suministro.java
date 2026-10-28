package co.edu.unbosque.backbases.entity;

import java.math.BigDecimal;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;

@Entity
@Table(name = "SUMINISTRO")
public class Suministro {

	@EmbeddedId
	private SuministroId id;

	@MapsId("nit")
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "NIT")
	private Proveedor proveedor;

	@MapsId("idProducto")
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "ID_PRODUCTO")
	private Producto producto;

	@MapsId("idSucursal")
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "ID_SUCURSAL")
	private Sucursal sucursal;

	@Column(name = "PRECIO_SUMINISTRO", nullable = false, precision = 12, scale = 2)
	private BigDecimal precioSuministro;

	@JdbcTypeCode(SqlTypes.SMALLINT)
	@Column(name = "TIEMPO_ENTREGA_DIAS", nullable = false)
	private Integer tiempoEntregaDias;

	protected Suministro() {
	}

	public Suministro(Proveedor proveedor, Producto producto, Sucursal sucursal) {
		this.id = new SuministroId(proveedor.getNit(), producto.getIdProducto(), sucursal.getIdSucursal());
		this.proveedor = proveedor;
		this.producto = producto;
		this.sucursal = sucursal;
	}

	public SuministroId getId() {
		return id;
	}

	public Proveedor getProveedor() {
		return proveedor;
	}

	public Producto getProducto() {
		return producto;
	}

	public Sucursal getSucursal() {
		return sucursal;
	}

	public BigDecimal getPrecioSuministro() {
		return precioSuministro;
	}

	public void setPrecioSuministro(BigDecimal precioSuministro) {
		this.precioSuministro = precioSuministro;
	}

	public Integer getTiempoEntregaDias() {
		return tiempoEntregaDias;
	}

	public void setTiempoEntregaDias(Integer tiempoEntregaDias) {
		this.tiempoEntregaDias = tiempoEntregaDias;
	}
}
