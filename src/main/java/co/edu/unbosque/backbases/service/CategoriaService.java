package co.edu.unbosque.backbases.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.backbases.dto.CategoriaDTO;
import co.edu.unbosque.backbases.dto.CategoriaEntradaDTO;
import co.edu.unbosque.backbases.entity.Categoria;
import co.edu.unbosque.backbases.exception.ConflictoException;
import co.edu.unbosque.backbases.exception.RecursoNoEncontradoException;
import co.edu.unbosque.backbases.exception.ReglaNegocioException;
import co.edu.unbosque.backbases.repository.CategoriaRepository;

@Service
public class CategoriaService {

	private final CategoriaRepository categoriaRepository;

	public CategoriaService(CategoriaRepository categoriaRepository) {
		this.categoriaRepository = categoriaRepository;
	}

	@Transactional(readOnly = true)
	public List<CategoriaDTO> listar(boolean soloPrincipales) {
		List<Categoria> categorias = soloPrincipales ? categoriaRepository.listarPrincipales() : categoriaRepository.listarConPrincipal();
		return categorias.stream().map(CategoriaDTO::de).toList();
	}

	@Transactional
	public CategoriaDTO crear(CategoriaEntradaDTO entrada) {
		Categoria categoria = new Categoria();
		aplicar(entrada, categoria);
		return CategoriaDTO.de(categoriaRepository.save(categoria));
	}

	@Transactional
	public CategoriaDTO actualizar(Integer idCategoria, CategoriaEntradaDTO entrada) {
		Categoria categoria = buscar(idCategoria);
		aplicar(entrada, categoria);
		return CategoriaDTO.de(categoria);
	}

	@Transactional
	public void eliminar(Integer idCategoria) {
		Categoria categoria = buscar(idCategoria);
		Map<String, Long> referencias = new LinkedHashMap<>();
		referencias.put("subcategorías", categoriaRepository.countByCategoriaPrincipalIdCategoria(idCategoria));
		referencias.put("productos", categoriaRepository.contarProductos(idCategoria));
		ConflictoException.siEstaEnUso("la categoría", referencias);
		categoriaRepository.delete(categoria);
	}

	private void aplicar(CategoriaEntradaDTO entrada, Categoria categoria) {
		String nombre = entrada.nombre().trim();
		Integer idPrincipal = entrada.idCategoriaPrincipal();
		Integer idPropio = categoria.getIdCategoria();
		Categoria principal = null;
		if (idPrincipal != null) {
			if (Objects.equals(idPrincipal, idPropio)) {
				throw new ReglaNegocioException("Una categoría no puede ser su propia categoría principal.");
			}
			principal = buscar(idPrincipal);
			if (principal.getCategoriaPrincipal() != null) {
				throw new ReglaNegocioException("Una subcategoría debe colgar de una categoría principal.");
			}
			if (idPropio != null && categoriaRepository.countByCategoriaPrincipalIdCategoria(idPropio) > 0) {
				throw new ReglaNegocioException("Esta categoría tiene subcategorías, así que no puede volverse subcategoría.");
			}
		}
		if (categoriaRepository.existeNombreEnNivel(nombre, idPrincipal, idPropio)) {
			throw new ConflictoException("Ya existe la categoría \"" + nombre + "\" en ese nivel.");
		}
		categoria.setNombre(nombre);
		categoria.setDescripcion(entrada.descripcion() == null || entrada.descripcion().isBlank() ? null : entrada.descripcion().trim());
		categoria.setCategoriaPrincipal(principal);
	}

	private Categoria buscar(Integer idCategoria) {
		return categoriaRepository.findById(idCategoria)
				.orElseThrow(() -> RecursoNoEncontradoException.de("la categoría", idCategoria));
	}
}
