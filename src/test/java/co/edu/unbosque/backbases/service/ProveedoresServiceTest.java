package co.edu.unbosque.backbases.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import co.edu.unbosque.backbases.dto.EvaluacionEntradaDTO;
import co.edu.unbosque.backbases.dto.ProveedorEntradaDTO;
import co.edu.unbosque.backbases.dto.Rol;
import co.edu.unbosque.backbases.dto.SuministroEntradaDTO;
import co.edu.unbosque.backbases.entity.Administrador;
import co.edu.unbosque.backbases.entity.Producto;
import co.edu.unbosque.backbases.entity.Proveedor;
import co.edu.unbosque.backbases.entity.Sucursal;
import co.edu.unbosque.backbases.entity.Suministro;
import co.edu.unbosque.backbases.entity.SuministroId;
import co.edu.unbosque.backbases.exception.ConflictoException;
import co.edu.unbosque.backbases.exception.ReglaNegocioException;
import co.edu.unbosque.backbases.exception.SinPermisoException;
import co.edu.unbosque.backbases.repository.AdministradorRepository;
import co.edu.unbosque.backbases.repository.EvaluacionRepository;
import co.edu.unbosque.backbases.repository.ProductoRepository;
import co.edu.unbosque.backbases.repository.ProveedorRepository;
import co.edu.unbosque.backbases.repository.SucursalRepository;
import co.edu.unbosque.backbases.repository.SuministroRepository;
import co.edu.unbosque.backbases.security.UsuarioAutenticado;

class ProveedoresServiceTest {

	private final ProveedorRepository proveedorRepository = mock(ProveedorRepository.class);
	private final SuministroRepository suministroRepository = mock(SuministroRepository.class);
	private final ProductoRepository productoRepository = mock(ProductoRepository.class);
	private final SucursalRepository sucursalRepository = mock(SucursalRepository.class);
	private final EvaluacionRepository evaluacionRepository = mock(EvaluacionRepository.class);
	private final AdministradorRepository administradorRepository = mock(AdministradorRepository.class);

	private final ProveedorService proveedorService = new ProveedorService(proveedorRepository);
	private final SuministroService suministroService = new SuministroService(suministroRepository, proveedorRepository, productoRepository,
			sucursalRepository);
	private final EvaluacionService evaluacionService = new EvaluacionService(evaluacionRepository, suministroRepository, administradorRepository);

	private static final UsuarioAutenticado LAURA = new UsuarioAutenticado(1, "laura@x.co", Rol.ADMINISTRADOR, null);

	private static Proveedor proveedor(String nit) {
		Proveedor p = new Proveedor();
		p.setNit(nit);
		p.setNombre("Proveedor " + nit);
		return p;
	}

	private static EvaluacionEntradaDTO evaluacion(LocalDate fecha) {
		return new EvaluacionEntradaDTO("900215334-1", 1, 1, fecha, 4, true, null);
	}

	@Test
	void elNitDelProveedorNoSeRepiteNiCambia() {
		ProveedorEntradaDTO andina = new ProveedorEntradaDTO("900215334-1", "Andina", "Calle 1", List.of("601"), List.of("a@b.co"));
		when(proveedorRepository.existsById("900215334-1")).thenReturn(true);
		when(proveedorRepository.findWithContactoByNit("900215334-1")).thenReturn(Optional.of(proveedor("900215334-1")));

		assertThatThrownBy(() -> proveedorService.crear(andina))
				.isInstanceOf(ConflictoException.class)
				.hasMessage("Ya hay un proveedor con el NIT 900215334-1.");
		assertThatThrownBy(() -> proveedorService.actualizar("900215334-1",
				new ProveedorEntradaDTO("900215334-9", "Andina", "Calle 1", List.of("601"), List.of("a@b.co"))))
				.isInstanceOf(ReglaNegocioException.class)
				.hasMessage("El NIT es la llave del proveedor y no se puede cambiar.");
	}

	@Test
	void noEliminaUnProveedorConSuministros() {
		when(proveedorRepository.findWithContactoByNit("900215334-1")).thenReturn(Optional.of(proveedor("900215334-1")));
		when(proveedorRepository.contarSuministros("900215334-1")).thenReturn(16L);

		assertThatThrownBy(() -> proveedorService.eliminar("900215334-1"))
				.isInstanceOf(ConflictoException.class)
				.hasMessage("No se puede eliminar el proveedor: la usan 16 suministros.");
		verify(proveedorRepository, never()).delete(any());
	}

	@Test
	void laMismaCombinacionDeLaTernariaNoSeRepite() {
		Producto producto = new Producto();
		producto.setIdProducto(1);
		Sucursal sucursal = new Sucursal();
		sucursal.setIdSucursal(1);
		when(proveedorRepository.findById("900215334-1")).thenReturn(Optional.of(proveedor("900215334-1")));
		when(productoRepository.findById(1)).thenReturn(Optional.of(producto));
		when(sucursalRepository.findById(1)).thenReturn(Optional.of(sucursal));
		when(suministroRepository.existsById(new SuministroId("900215334-1", 1, 1))).thenReturn(true);

		assertThatThrownBy(() -> suministroService.crear(new SuministroEntradaDTO("900215334-1", 1, 1, new BigDecimal("28000"), 3)))
				.isInstanceOf(ConflictoException.class)
				.hasMessageStartingWith("Este proveedor ya suministra ese producto a esa sucursal.");
		verify(suministroRepository, never()).save(any());
	}

	@Test
	void soloSeEvaluanSuministrosRegistrados() {
		when(administradorRepository.findById(1)).thenReturn(Optional.of(new Administrador()));
		when(suministroRepository.findById(new SuministroId("900215334-1", 1, 1))).thenReturn(Optional.empty());

		assertThatThrownBy(() -> evaluacionService.registrar(evaluacion(null), LAURA))
				.isInstanceOf(ReglaNegocioException.class)
				.hasMessage("Solo se pueden evaluar suministros registrados.");
	}

	@Test
	void unaEvaluacionNoPuedeTenerFechaFutura() {
		when(administradorRepository.findById(1)).thenReturn(Optional.of(new Administrador()));
		when(suministroRepository.findById(new SuministroId("900215334-1", 1, 1))).thenReturn(Optional.of(mock(Suministro.class)));

		assertThatThrownBy(() -> evaluacionService.registrar(evaluacion(LocalDate.now(VentaService.ZONA).plusDays(1)), LAURA))
				.isInstanceOf(ReglaNegocioException.class)
				.hasMessage("La fecha de evaluación no puede estar en el futuro.");
		verify(evaluacionRepository, never()).saveAndFlush(any());
	}

	@Test
	void soloUnAdministradorEvalua() {
		UsuarioAutenticado camila = new UsuarioAutenticado(3, "camila@x.co", Rol.VENDEDOR, 1);
		when(administradorRepository.findById(3)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> evaluacionService.registrar(evaluacion(null), camila)).isInstanceOf(SinPermisoException.class);
	}
}
