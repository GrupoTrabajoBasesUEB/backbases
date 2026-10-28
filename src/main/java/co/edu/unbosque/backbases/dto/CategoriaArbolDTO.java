package co.edu.unbosque.backbases.dto;

import java.util.List;

public record CategoriaArbolDTO(Integer idCategoria, String nombre, String descripcion, List<CategoriaDTO> subcategorias) {
}
