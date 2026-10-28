package co.edu.unbosque.backbases.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import co.edu.unbosque.backbases.dto.MarcaDTO;
import co.edu.unbosque.backbases.dto.MarcaEntradaDTO;
import co.edu.unbosque.backbases.entity.Marca;
import co.edu.unbosque.backbases.exception.ConflictoException;
import co.edu.unbosque.backbases.repository.MarcaRepository;

@ExtendWith(MockitoExtension.class)
class MarcaServiceTest {

	@Mock
	private MarcaRepository marcaRepository;

	@InjectMocks
	private MarcaService marcaService;

	private static Marca marca(int id, String nombre) {
		Marca m = new Marca();
		m.setIdMarca(id);
		m.setNombre(nombre);
		m.setPaisOrigen("Colombia");
		return m;
	}

	@Test
	void creaConLosTextosSinEspaciosSobrantes() {
		when(marcaRepository.save(any(Marca.class))).thenAnswer(i -> i.getArgument(0));

		MarcaDTO creada = marcaService.crear(new MarcaEntradaDTO("  Makita ", " Japón "));

		assertThat(creada.nombreMarca()).isEqualTo("Makita");
		assertThat(creada.paisOrigen()).isEqualTo("Japón");
	}

	@Test
	void noCreaUnaMarcaRepetida() {
		when(marcaRepository.existsByNombre("Bosch")).thenReturn(true);

		assertThatThrownBy(() -> marcaService.crear(new MarcaEntradaDTO("Bosch", "Alemania")))
				.isInstanceOf(ConflictoException.class)
				.hasMessage("La marca \"Bosch\" ya está registrada.");
		verify(marcaRepository, never()).save(any());
	}

	@Test
	void alActualizarNoChocaConsigoMisma() {
		Marca bosch = marca(3, "Bosch");
		when(marcaRepository.findById(3)).thenReturn(Optional.of(bosch));
		when(marcaRepository.existsByNombreAndIdMarcaNot("Bosch", 3)).thenReturn(false);

		MarcaDTO actualizada = marcaService.actualizar(3, new MarcaEntradaDTO("Bosch", "Alemania"));

		assertThat(actualizada.paisOrigen()).isEqualTo("Alemania");
	}

	@Test
	void noEliminaUnaMarcaConProductos() {
		when(marcaRepository.findById(1)).thenReturn(Optional.of(marca(1, "Stanley")));
		when(marcaRepository.contarProductos(1)).thenReturn(4L);

		assertThatThrownBy(() -> marcaService.eliminar(1))
				.isInstanceOf(ConflictoException.class)
				.hasMessage("No se puede eliminar la marca: la usan 4 productos.");
		verify(marcaRepository, never()).delete(any());
	}

	@Test
	void eliminaUnaMarcaSinProductos() {
		Marca libre = marca(8, "Makita");
		when(marcaRepository.findById(8)).thenReturn(Optional.of(libre));
		when(marcaRepository.contarProductos(8)).thenReturn(0L);

		marcaService.eliminar(8);

		verify(marcaRepository).delete(libre);
	}
}
