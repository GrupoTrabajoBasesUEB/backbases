package co.edu.unbosque.backbases.dto;

import co.edu.unbosque.backbases.entity.MetodoPago;

public record MetodoPagoDTO(Integer idMetodoPago, String tipo) {

	public static MetodoPagoDTO de(MetodoPago metodo) {
		return new MetodoPagoDTO(metodo.getIdMetodoPago(), metodo.getTipo());
	}
}
