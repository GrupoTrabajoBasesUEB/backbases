package co.edu.unbosque.backbases.entity;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "PRODUCTO")
public class Producto {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ID_PRODUCTO")
	private Integer idProducto;

	@Column(name = "CODIGO", nullable = false, length = 20)
	private String codigo;

	@Column(name = "NOMBRE_PRODUCTO", nullable = false, length = 100)
	private String nombre;

	@Column(name = "DESCRIPCION", length = 255)
	private String descripcion;

	@Column(name = "COSTO_COMPRA", nullable = false, precision = 12, scale = 2)
	private BigDecimal costoCompra;

	@Column(name = "PRECIO_BASE_VENTA", nullable = false, precision = 12, scale = 2)
	private BigDecimal precioBaseVenta;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "ID_CATEGORIA", nullable = false)
	private Categoria categoria;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "ID_MARCA", nullable = false)
	private Marca marca;

	public Integer getIdProducto() {
		return idProducto;
	}

	public void setIdProducto(Integer idProducto) {
		this.idProducto = idProducto;
	}

	public String getCodigo() {
		return codigo;
	}

	public void setCodigo(String codigo) {
		this.codigo = codigo;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	public BigDecimal getCostoCompra() {
		return costoCompra;
	}

	public void setCostoCompra(BigDecimal costoCompra) {
		this.costoCompra = costoCompra;
	}

	public BigDecimal getPrecioBaseVenta() {
		return precioBaseVenta;
	}

	public void setPrecioBaseVenta(BigDecimal precioBaseVenta) {
		this.precioBaseVenta = precioBaseVenta;
	}

	public Categoria getCategoria() {
		return categoria;
	}

	public void setCategoria(Categoria categoria) {
		this.categoria = categoria;
	}

	public Marca getMarca() {
		return marca;
	}

	public void setMarca(Marca marca) {
		this.marca = marca;
	}
}
