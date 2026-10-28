package co.edu.unbosque.backbases.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "CLIENTE_NATURAL")
public class ClienteNatural {

	@Id
	@Column(name = "ID_CLIENTE")
	private Integer idCliente;

	@MapsId
	@OneToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "ID_CLIENTE")
	private Cliente cliente;

	@Column(name = "NOMBRE", nullable = false, length = 60)
	private String nombre;

	@Column(name = "APELLIDO", nullable = false, length = 60)
	private String apellido;

	public Integer getIdCliente() {
		return idCliente;
	}

	public void setCliente(Cliente cliente) {
		this.cliente = cliente;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getApellido() {
		return apellido;
	}

	public void setApellido(String apellido) {
		this.apellido = apellido;
	}
}
