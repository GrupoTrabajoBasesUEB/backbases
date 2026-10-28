package co.edu.unbosque.backbases.dto;

import co.edu.unbosque.backbases.entity.Marca;

public record MarcaDTO(Integer idMarca, String nombreMarca, String paisOrigen) {

	public static MarcaDTO de(Marca marca) {
		return new MarcaDTO(marca.getIdMarca(), marca.getNombre(), marca.getPaisOrigen());
	}
}
