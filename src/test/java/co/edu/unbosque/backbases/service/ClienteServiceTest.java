package co.edu.unbosque.backbases.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import co.edu.unbosque.backbases.dto.ClienteEntradaDTO;
import co.edu.unbosque.backbases.dto.TipoCliente;
import co.edu.unbosque.backbases.entity.Cliente;
import co.edu.unbosque.backbases.exception.ConflictoException;
import co.edu.unbosque.backbases.exception.ReglaNegocioException;
import co.edu.unbosque.backbases.repository.ClienteJuridicoRepository;
import co.edu.unbosque.backbases.repository.ClienteNaturalRepository;
import co.edu.unbosque.backbases.repository.ClienteRepository;

@ExtendWith(MockitoExtension.class)
class ClienteServiceTest {

	@Mock
	private ClienteRepository clienteRepository;

	@Mock
	private ClienteNaturalRepository naturalRepository;

	@Mock
	private ClienteJuridicoRepository juridicoRepository;

	@InjectMocks
	private ClienteService clienteService;

	private static ClienteEntradaDTO natural(String documento, String apellido) {
		return new ClienteEntradaDTO(TipoCliente.NATURAL, documento, null, List.of("300 000 0000"), List.of(), "Ana", apellido, null);
	}

	private static Cliente cliente(int id, String documento) {
		Cliente c = new Cliente();
		c.setIdCliente(id);
		c.setNumeroDocumento(documento);
		return c;
	}

	@Test
	void elDocumentoDelConsumidorFinalEstaReservado() {
		assertThatThrownBy(() -> clienteService.crear(natural(" 222222222222 ", "Ruiz")))
				.isInstanceOf(ReglaNegocioException.class)
				.hasMessageContaining("reservado para el consumidor final");
		verify(clienteRepository, never()).save(any());
	}

	@Test
	void unClienteNaturalNecesitaApellido() {
		assertThatThrownBy(() -> clienteService.crear(natural("1010", "  ")))
				.isInstanceOf(ReglaNegocioException.class)
				.hasMessage("Un cliente natural necesita nombre y apellido.");
	}

	@Test
	void unClienteJuridicoNecesitaRazonSocial() {
		ClienteEntradaDTO juridico = new ClienteEntradaDTO(TipoCliente.JURIDICO, "900111222-3", null, null, null, null, null, null);

		assertThatThrownBy(() -> clienteService.crear(juridico))
				.isInstanceOf(ReglaNegocioException.class)
				.hasMessage("Un cliente jurídico necesita razón social.");
	}

	@Test
	void elDocumentoNoSeRepite() {
		when(clienteRepository.existsByNumeroDocumento("79845123")).thenReturn(true);

		assertThatThrownBy(() -> clienteService.crear(natural("79845123", "Ruiz")))
				.isInstanceOf(ConflictoException.class)
				.hasMessage("Ya existe un cliente con el documento 79845123.");
	}

	@Test
	void elConsumidorFinalNoSeModificaNiSeElimina() {
		when(clienteRepository.findWithContactoByIdCliente(7)).thenReturn(Optional.of(cliente(7, Cliente.DOCUMENTO_CONSUMIDOR_FINAL)));

		assertThatThrownBy(() -> clienteService.actualizar(7, natural("1010", "Ruiz")))
				.isInstanceOf(ReglaNegocioException.class)
				.hasMessage("El consumidor final es un registro del sistema y no se puede modificar.");
		assertThatThrownBy(() -> clienteService.eliminar(7)).isInstanceOf(ReglaNegocioException.class);
		verify(clienteRepository, never()).delete(any());
	}

	@Test
	void noEliminaUnClienteConVentas() {
		when(clienteRepository.findWithContactoByIdCliente(1)).thenReturn(Optional.of(cliente(1, "79845123")));
		when(clienteRepository.contarVentas(1)).thenReturn(6L);

		assertThatThrownBy(() -> clienteService.eliminar(1))
				.isInstanceOf(ConflictoException.class)
				.hasMessage("No se puede eliminar el cliente: la usan 6 ventas.");
	}
}
