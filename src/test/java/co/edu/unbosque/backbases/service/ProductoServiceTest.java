package co.edu.unbosque.backbases.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import co.edu.unbosque.backbases.dto.ProductoDTO;
import co.edu.unbosque.backbases.dto.ProductoEntradaDTO;
import co.edu.unbosque.backbases.entity.Categoria;
import co.edu.unbosque.backbases.entity.Marca;
import co.edu.unbosque.backbases.entity.Producto;
import co.edu.unbosque.backbases.exception.ConflictoException;
import co.edu.unbosque.backbases.exception.RecursoNoEncontradoException;
import co.edu.unbosque.backbases.exception.ReglaNegocioException;
import co.edu.unbosque.backbases.repository.CategoriaRepository;
import co.edu.unbosque.backbases.repository.MarcaRepository;
import co.edu.unbosque.backbases.repository.ProductoRepository;

@ExtendWith(MockitoExtension.class)
class ProductoServiceTest {

	@Mock
	private ProductoRepository productoRepository;

	@Mock
	private CategoriaRepository categoriaRepository;

	@Mock
	private MarcaRepository marcaRepository;

	@InjectMocks
	private ProductoService productoService;

	private static ProductoEntradaDTO entrada(String costo, String precio) {
		return new ProductoEntradaDTO(" HM-0099 ", "Mazo de goma", " ", new BigDecimal(costo), new BigDecimal(precio), 2, 1);
	}

	@Test
	void elPrecioNoPuedeSerMenorQueElCosto() {
		assertThatThrownBy(() -> productoService.crear(entrada("30000", "29999.99")))
				.isInstanceOf(ReglaNegocioException.class)
				.hasMessage("El precio base de venta no puede ser menor que el costo de compra.");
		verify(productoRepository, never()).save(any());
	}

	@Test
	void elCodigoNoSeRepite() {
		when(productoRepository.existsByCodigo("HM-0099")).thenReturn(true);

		assertThatThrownBy(() -> productoService.crear(entrada("10000", "15000")))
				.isInstanceOf(ConflictoException.class)
				.hasMessage("El código HM-0099 ya pertenece a otro producto.");
	}

	@Test
	void laCategoriaDebeExistir() {
		when(categoriaRepository.findById(2)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> productoService.crear(entrada("10000", "15000")))
				.isInstanceOf(RecursoNoEncontradoException.class)
				.hasMessage("No existe la categoría con id 2.");
	}

	@Test
	void unProductoNuevoEmpiezaSinStock() {
		Categoria martillos = new Categoria();
		martillos.setIdCategoria(2);
		martillos.setNombre("Martillos");
		Marca stanley = new Marca();
		stanley.setIdMarca(1);
		stanley.setNombre("Stanley");
		when(categoriaRepository.findById(2)).thenReturn(Optional.of(martillos));
		when(marcaRepository.findById(1)).thenReturn(Optional.of(stanley));
		when(productoRepository.save(any(Producto.class))).thenAnswer(i -> i.getArgument(0));

		ProductoDTO creado = productoService.crear(entrada("10000", "15000"));

		assertThat(creado.codigo()).isEqualTo("HM-0099");
		assertThat(creado.descripcion()).isNull();
		assertThat(creado.stock()).isZero();
		assertThat(creado.nombreCategoria()).isEqualTo("Martillos");
	}
}
