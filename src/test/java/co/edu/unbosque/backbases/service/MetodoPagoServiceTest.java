package co.edu.unbosque.backbases.service;

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

import co.edu.unbosque.backbases.dto.MetodoPagoEntradaDTO;
import co.edu.unbosque.backbases.entity.MetodoPago;
import co.edu.unbosque.backbases.exception.ConflictoException;
import co.edu.unbosque.backbases.repository.MetodoPagoRepository;

@ExtendWith(MockitoExtension.class)
class MetodoPagoServiceTest {

	@Mock
	private MetodoPagoRepository metodoPagoRepository;

	@InjectMocks
	private MetodoPagoService metodoPagoService;

	@Test
	void noCreaUnTipoRepetido() {
		when(metodoPagoRepository.existsByTipo("Efectivo")).thenReturn(true);

		assertThatThrownBy(() -> metodoPagoService.crear(new MetodoPagoEntradaDTO(" Efectivo ")))
				.isInstanceOf(ConflictoException.class)
				.hasMessage("El método de pago \"Efectivo\" ya existe.");
	}

	@Test
	void noEliminaUnMetodoConVentas() {
		MetodoPago efectivo = new MetodoPago();
		efectivo.setIdMetodoPago(1);
		efectivo.setTipo("Efectivo");
		when(metodoPagoRepository.findById(1)).thenReturn(Optional.of(efectivo));
		when(metodoPagoRepository.contarVentas(1)).thenReturn(25L);

		assertThatThrownBy(() -> metodoPagoService.eliminar(1))
				.isInstanceOf(ConflictoException.class)
				.hasMessage("No se puede eliminar el método de pago: la usan 25 ventas.");
		verify(metodoPagoRepository, never()).delete(any());
	}
}
