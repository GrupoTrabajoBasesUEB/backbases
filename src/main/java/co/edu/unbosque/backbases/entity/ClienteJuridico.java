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
@Table(name = "CLIENTE_JURIDICO")
public class ClienteJuridico {

	@Id
	@Column(name = "ID_CLIENTE")
	private Integer idCliente;

	@MapsId
	@OneToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "ID_CLIENTE")
	private Cliente cliente;

	@Column(name = "RAZON_SOCIAL", nullable = false, length = 120)
	private String razonSocial;

	public Integer getIdCliente() {
		return idCliente;
	}

	public void setCliente(Cliente cliente) {
		this.cliente = cliente;
	}

	public String getRazonSocial() {
		return razonSocial;
	}

	public void setRazonSocial(String razonSocial) {
		this.razonSocial = razonSocial;
	}
}
