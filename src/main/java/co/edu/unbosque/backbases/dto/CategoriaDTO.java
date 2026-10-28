package co.edu.unbosque.backbases.dto;

import co.edu.unbosque.backbases.entity.Categoria;

public record CategoriaDTO(Integer idCategoria, String nombre, String descripcion, Integer idCategoriaPrincipal,
		String nombreCategoriaPrincipal) {

	public static CategoriaDTO de(Categoria categoria) {
		Categoria principal = categoria.getCategoriaPrincipal();
		return new CategoriaDTO(categoria.getIdCategoria(), categoria.getNombre(), categoria.getDescripcion(),
				principal == null ? null : principal.getIdCategoria(),
				principal == null ? null : principal.getNombre());
	}
}
