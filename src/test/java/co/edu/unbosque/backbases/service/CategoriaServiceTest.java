package co.edu.unbosque.backbases.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import co.edu.unbosque.backbases.dto.CategoriaDTO;
import co.edu.unbosque.backbases.dto.CategoriaEntradaDTO;
import co.edu.unbosque.backbases.entity.Categoria;
import co.edu.unbosque.backbases.exception.ConflictoException;
import co.edu.unbosque.backbases.exception.RecursoNoEncontradoException;
import co.edu.unbosque.backbases.exception.ReglaNegocioException;
import co.edu.unbosque.backbases.repository.CategoriaRepository;

@ExtendWith(MockitoExtension.class)
class CategoriaServiceTest {

	@Mock
	private CategoriaRepository categoriaRepository;

	@InjectMocks
	private CategoriaService categoriaService;

	private static Categoria categoria(int id, String nombre, Categoria principal) {
		Categoria c = new Categoria();
		c.setIdCategoria(id);
		c.setNombre(nombre);
		c.setCategoriaPrincipal(principal);
		return c;
	}

	@Test
	void creaUnaSubcategoriaBajoUnaPrincipal() {
		Categoria herramientas = categoria(1, "Herramientas manuales", null);
		when(categoriaRepository.findById(1)).thenReturn(Optional.of(herramientas));
		when(categoriaRepository.save(any(Categoria.class))).thenAnswer(i -> {
			Categoria c = i.getArgument(0);
			c.setIdCategoria(20);
			return c;
		});

		CategoriaDTO creada = categoriaService.crear(new CategoriaEntradaDTO("  Alicates ", " ", 1));

		assertThat(creada.idCategoria()).isEqualTo(20);
		assertThat(creada.nombre()).isEqualTo("Alicates");
		assertThat(creada.descripcion()).isNull();
		assertThat(creada.idCategoriaPrincipal()).isEqualTo(1);
		assertThat(creada.nombreCategoriaPrincipal()).isEqualTo("Herramientas manuales");
	}

	@Test
	void noPermiteColgarDeUnaSubcategoria() {
		Categoria martillos = categoria(2, "Martillos", categoria(1, "Herramientas manuales", null));
		when(categoriaRepository.findById(2)).thenReturn(Optional.of(martillos));

		assertThatThrownBy(() -> categoriaService.crear(new CategoriaEntradaDTO("Mazos", null, 2)))
				.isInstanceOf(ReglaNegocioException.class)
				.hasMessage("Una subcategoría debe colgar de una categoría principal.");
		verify(categoriaRepository, never()).save(any());
	}

	@Test
	void noPuedeSerSuPropiaCategoriaPrincipal() {
		when(categoriaRepository.findById(1)).thenReturn(Optional.of(categoria(1, "Herramientas manuales", null)));

		assertThatThrownBy(() -> categoriaService.actualizar(1, new CategoriaEntradaDTO("Herramientas manuales", null, 1)))
				.isInstanceOf(ReglaNegocioException.class)
				.hasMessage("Una categoría no puede ser su propia categoría principal.");
	}

	@Test
	void unaPrincipalConSubcategoriasNoPuedeVolverseSubcategoria() {
		when(categoriaRepository.findById(1)).thenReturn(Optional.of(categoria(1, "Herramientas manuales", null)));
		when(categoriaRepository.findById(5)).thenReturn(Optional.of(categoria(5, "Herramientas eléctricas", null)));
		when(categoriaRepository.countByCategoriaPrincipalIdCategoria(1)).thenReturn(3L);

		assertThatThrownBy(() -> categoriaService.actualizar(1, new CategoriaEntradaDTO("Herramientas manuales", null, 5)))
				.isInstanceOf(ReglaNegocioException.class)
				.hasMessageContaining("tiene subcategorías");
	}

	@Test
	void noRepiteNombreEnElMismoNivel() {
		when(categoriaRepository.existeNombreEnNivel(anyString(), any(), any())).thenReturn(true);

		assertThatThrownBy(() -> categoriaService.crear(new CategoriaEntradaDTO("Pinturas", null, null)))
				.isInstanceOf(ConflictoException.class)
				.hasMessage("Ya existe la categoría \"Pinturas\" en ese nivel.");
	}

	@Test
	void noEliminaSiTieneSubcategoriasOProductos() {
		when(categoriaRepository.findById(1)).thenReturn(Optional.of(categoria(1, "Herramientas manuales", null)));
		when(categoriaRepository.countByCategoriaPrincipalIdCategoria(1)).thenReturn(3L);
		when(categoriaRepository.contarProductos(1)).thenReturn(0L);

		assertThatThrownBy(() -> categoriaService.eliminar(1))
				.isInstanceOf(ConflictoException.class)
				.hasMessage("No se puede eliminar la categoría: la usan 3 subcategorías.");
		verify(categoriaRepository, never()).delete(any());
	}

	@Test
	void eliminarUnaInexistenteDa404() {
		when(categoriaRepository.findById(99)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> categoriaService.eliminar(99))
				.isInstanceOf(RecursoNoEncontradoException.class)
				.hasMessage("No existe la categoría con id 99.");
	}
}
