package co.edu.unbosque.backbases.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import co.edu.unbosque.backbases.dto.EntradaMercanciaDTO;
import co.edu.unbosque.backbases.dto.Rol;
import co.edu.unbosque.backbases.entity.Inventario;
import co.edu.unbosque.backbases.entity.InventarioId;
import co.edu.unbosque.backbases.entity.Producto;
import co.edu.unbosque.backbases.entity.Sucursal;
import co.edu.unbosque.backbases.exception.ReglaNegocioException;
import co.edu.unbosque.backbases.exception.SinPermisoException;
import co.edu.unbosque.backbases.repository.InventarioRepository;
import co.edu.unbosque.backbases.repository.ProductoRepository;
import co.edu.unbosque.backbases.repository.SucursalRepository;
import co.edu.unbosque.backbases.security.UsuarioAutenticado;

@ExtendWith(MockitoExtension.class)
class InventarioServiceTest {

	@Mock
	private InventarioRepository inventarioRepository;

	@Mock
	private SucursalRepository sucursalRepository;

	@Mock
	private ProductoRepository productoRepository;

	@InjectMocks
	private InventarioService inventarioService;

	private static Sucursal sucursal(int id) {
		Sucursal s = new Sucursal();
		s.setIdSucursal(id);
		return s;
	}

	private static Producto producto(int id) {
		Producto p = new Producto();
		p.setIdProducto(id);
		return p;
	}

	@Test
	void elVendedorSoloVeElInventarioDeSuSede() {
		UsuarioAutenticado camila = new UsuarioAutenticado(3, "camila@x.co", Rol.VENDEDOR, 1);

		assertThatThrownBy(() -> inventarioService.deSucursal(2, null, camila))
				.isInstanceOf(SinPermisoException.class)
				.hasMessage("Solo puede consultar el inventario de su sucursal.");
	}

	@Test
	void laPrimeraEntradaExigeUbicacion() {
		when(sucursalRepository.findById(1)).thenReturn(Optional.of(sucursal(1)));
		when(productoRepository.findById(5)).thenReturn(Optional.of(producto(5)));
		when(inventarioRepository.existsById(new InventarioId(1, 5))).thenReturn(false);

		assertThatThrownBy(() -> inventarioService.registrarEntrada(1, 5, new EntradaMercanciaDTO(10, "  ")))
				.isInstanceOf(ReglaNegocioException.class)
				.hasMessageContaining("indique en qué parte de la bodega queda");
		verify(inventarioRepository, never()).sumarEntrada(anyInt(), anyInt(), anyInt(), any());
	}

	@Test
	void noRetiraUnProductoQueTodaviaTieneUnidades() {
		Inventario registro = new Inventario(sucursal(1), producto(5));
		registro.setCantidadDisponible(4);
		when(inventarioRepository.buscar(new InventarioId(1, 5))).thenReturn(Optional.of(registro));

		assertThatThrownBy(() -> inventarioService.retirar(1, 5))
				.isInstanceOf(ReglaNegocioException.class)
				.hasMessageStartingWith("Aún quedan 4 unidades");
		verify(inventarioRepository, never()).delete(any());
	}
}
