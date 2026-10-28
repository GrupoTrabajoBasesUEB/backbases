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

import co.edu.unbosque.backbases.dto.SucursalEntradaDTO;
import co.edu.unbosque.backbases.entity.Administrador;
import co.edu.unbosque.backbases.entity.Sucursal;
import co.edu.unbosque.backbases.exception.ConflictoException;
import co.edu.unbosque.backbases.exception.ReglaNegocioException;
import co.edu.unbosque.backbases.repository.AdministradorRepository;
import co.edu.unbosque.backbases.repository.SucursalRepository;

@ExtendWith(MockitoExtension.class)
class SucursalServiceTest {

	@Mock
	private SucursalRepository sucursalRepository;

	@Mock
	private AdministradorRepository administradorRepository;

	@InjectMocks
	private SucursalService sucursalService;

	private static final SucursalEntradaDTO SEDE_SUR = new SucursalEntradaDTO("Sede Sur", "Bogotá", "Calle 1 # 2-3", "601 000 0000", 2);

	private static Sucursal sucursal(int id, String nombre) {
		Sucursal s = new Sucursal();
		s.setIdSucursal(id);
		s.setNombre(nombre);
		return s;
	}

	@Test
	void elEncargadoDebeSerAdministrador() {
		when(administradorRepository.findById(2)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> sucursalService.crear(SEDE_SUR))
				.isInstanceOf(ReglaNegocioException.class)
				.hasMessage("El encargado debe ser un administrador registrado.");
	}

	@Test
	void unAdministradorNoDirigeDosSedes() {
		when(administradorRepository.findById(2)).thenReturn(Optional.of(new Administrador()));
		when(sucursalRepository.findByAdministradorIdEmpleado(2)).thenReturn(Optional.of(sucursal(2, "Sede Norte")));

		assertThatThrownBy(() -> sucursalService.crear(SEDE_SUR))
				.isInstanceOf(ConflictoException.class)
				.hasMessage("Ese administrador ya dirige la sucursal \"Sede Norte\".");
		verify(sucursalRepository, never()).save(any());
	}

	@Test
	void noRepiteElNombre() {
		when(sucursalRepository.existsByNombre("Sede Sur")).thenReturn(true);

		assertThatThrownBy(() -> sucursalService.crear(SEDE_SUR)).isInstanceOf(ConflictoException.class);
	}

	@Test
	void noEliminaUnaSedeConMovimiento() {
		Sucursal centro = sucursal(1, "Sede Centro");
		when(sucursalRepository.buscarConEncargado(1)).thenReturn(Optional.of(centro));
		when(sucursalRepository.contarVentas(1)).thenReturn(20L);
		when(sucursalRepository.contarInventario(1)).thenReturn(16L);
		when(sucursalRepository.contarSuministros(1)).thenReturn(0L);
		when(sucursalRepository.contarVendedores(1)).thenReturn(1L);

		assertThatThrownBy(() -> sucursalService.eliminar(1))
				.isInstanceOf(ConflictoException.class)
				.hasMessage("No se puede eliminar la sucursal: la usan 20 ventas, 16 productos en inventario, 1 vendedores.");
		verify(sucursalRepository, never()).delete(any());
	}
}
