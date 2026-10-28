package co.edu.unbosque.backbases.entity;

import java.util.LinkedHashSet;
import java.util.Set;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

@Entity
@Table(name = "CLIENTE")
public class Cliente {

	public static final String DOCUMENTO_CONSUMIDOR_FINAL = "222222222222";

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ID_CLIENTE")
	private Integer idCliente;

	@Column(name = "NUMERO_DOCUMENTO", nullable = false, length = 20)
	private String numeroDocumento;

	@Column(name = "DIRECCION_CLIENTE", length = 120)
	private String direccion;

	@ElementCollection
	@CollectionTable(name = "TELEFONO_CLIENTE", joinColumns = @JoinColumn(name = "ID_CLIENTE"))
	@Column(name = "TELEFONO", length = 20)
	private Set<String> telefonos = new LinkedHashSet<>();

	@ElementCollection
	@CollectionTable(name = "CORREO_CLIENTE", joinColumns = @JoinColumn(name = "ID_CLIENTE"))
	@Column(name = "CORREO", length = 120)
	private Set<String> correos = new LinkedHashSet<>();

	public boolean esConsumidorFinal() {
		return DOCUMENTO_CONSUMIDOR_FINAL.equals(numeroDocumento);
	}

	public Integer getIdCliente() {
		return idCliente;
	}

	public void setIdCliente(Integer idCliente) {
		this.idCliente = idCliente;
	}

	public String getNumeroDocumento() {
		return numeroDocumento;
	}

	public void setNumeroDocumento(String numeroDocumento) {
		this.numeroDocumento = numeroDocumento;
	}

	public String getDireccion() {
		return direccion;
	}

	public void setDireccion(String direccion) {
		this.direccion = direccion;
	}

	public Set<String> getTelefonos() {
		return telefonos;
	}

	public Set<String> getCorreos() {
		return correos;
	}
}
