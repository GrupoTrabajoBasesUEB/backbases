package co.edu.unbosque.backbases.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import co.edu.unbosque.backbases.dto.LineaVentaEntradaDTO;
import co.edu.unbosque.backbases.dto.Rol;
import co.edu.unbosque.backbases.dto.VentaEntradaDTO;
import co.edu.unbosque.backbases.entity.Cliente;
import co.edu.unbosque.backbases.entity.Empleado;
import co.edu.unbosque.backbases.entity.MetodoPago;
import co.edu.unbosque.backbases.entity.Producto;
import co.edu.unbosque.backbases.entity.Sucursal;
import co.edu.unbosque.backbases.entity.Vendedor;
import co.edu.unbosque.backbases.entity.Venta;
import co.edu.unbosque.backbases.exception.ConflictoException;
import co.edu.unbosque.backbases.exception.SinPermisoException;
import co.edu.unbosque.backbases.repository.ClienteJuridicoRepository;
import co.edu.unbosque.backbases.repository.ClienteNaturalRepository;
import co.edu.unbosque.backbases.repository.ClienteRepository;
import co.edu.unbosque.backbases.repository.DetalleVentaRepository;
import co.edu.unbosque.backbases.repository.InventarioRepository;
import co.edu.unbosque.backbases.repository.MetodoPagoRepository;
import co.edu.unbosque.backbases.repository.ProductoRepository;
import co.edu.unbosque.backbases.repository.VendedorRepository;
import co.edu.unbosque.backbases.repository.VentaRepository;
import co.edu.unbosque.backbases.security.UsuarioAutenticado;

@ExtendWith(MockitoExtension.class)
class VentaServiceTest {

	@Mock
	private VentaRepository ventaRepository;
	@Mock
	private DetalleVentaRepository detalleRepository;
	@Mock
	private InventarioRepository inventarioRepository;
	@Mock
	private ProductoRepository productoRepository;
	@Mock
	private VendedorRepository vendedorRepository;
	@Mock
	private ClienteRepository clienteRepository;
	@Mock
	private ClienteNaturalRepository naturalRepository;
	@Mock
	private ClienteJuridicoRepository juridicoRepository;
	@Mock
	private MetodoPagoRepository metodoPagoRepository;

	@InjectMocks
	private VentaService ventaService;

	private static final UsuarioAutenticado CAMILA = new UsuarioAutenticado(3, "camila@x.co", Rol.VENDEDOR, 1);

	private void prepararVenta(Producto... productos) {
		Sucursal centro = new Sucursal();
		centro.setIdSucursal(1);
		centro.setNombre("Sede Centro");
		Empleado camila = new Empleado();
		camila.setIdEmpleado(3);
		camila.setNombre("Camila");
		camila.setApellido("Rojas");
		Vendedor vendedor = new Vendedor();
		vendedor.setEmpleado(camila);
		vendedor.setSucursal(centro);
		Cliente consumidorFinal = new Cliente();
		consumidorFinal.setIdCliente(7);
		consumidorFinal.setNumeroDocumento(Cliente.DOCUMENTO_CONSUMIDOR_FINAL);
		when(vendedorRepository.buscarConSucursal(3)).thenReturn(Optional.of(vendedor));
		when(clienteRepository.findByNumeroDocumento(Cliente.DOCUMENTO_CONSUMIDOR_FINAL)).thenReturn(Optional.of(consumidorFinal));
		when(metodoPagoRepository.findById(1)).thenReturn(Optional.of(new MetodoPago()));
		when(productoRepository.findAllById(any())).thenReturn(List.of(productos));
		when(ventaRepository.saveAndFlush(any(Venta.class))).thenAnswer(i -> i.getArgument(0));
	}

	private static Producto producto(int id, String codigo) {
		Producto p = new Producto();
		p.setIdProducto(id);
		p.setCodigo(codigo);
		p.setNombre("Producto " + codigo);
		p.setPrecioBaseVenta(new BigDecimal("1000"));
		return p;
	}

	@Test
	void soloUnVendedorRegistraVentas() {
		UsuarioAutenticado laura = new UsuarioAutenticado(1, "laura@x.co", Rol.ADMINISTRADOR, null);
		when(vendedorRepository.buscarConSucursal(1)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> ventaService.registrar(new VentaEntradaDTO(null, 1, List.of(new LineaVentaEntradaDTO(1, 1))), laura))
				.isInstanceOf(SinPermisoException.class);
	}

	@Test
	void sinStockSuficienteExplicaCuantoHay() {
		prepararVenta(producto(1, "HM-0001"));
		when(inventarioRepository.descontar(1, 1, 5)).thenReturn(0);
		when(inventarioRepository.cantidad(1, 1)).thenReturn(Optional.of(2));

		assertThatThrownBy(() -> ventaService.registrar(new VentaEntradaDTO(null, 1, List.of(new LineaVentaEntradaDTO(1, 5))), CAMILA))
				.isInstanceOf(ConflictoException.class)
				.hasMessage("Stock insuficiente de HM-0001 (Producto HM-0001): hay 2 y se piden 5.");
		verify(detalleRepository, never()).saveAll(any());
	}

	@Test
	void unProductoQueLaSedeNoManejaNoSeVende() {
		prepararVenta(producto(13, "PI-0301"));
		when(inventarioRepository.descontar(1, 13, 1)).thenReturn(0);
		when(inventarioRepository.cantidad(1, 13)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> ventaService.registrar(new VentaEntradaDTO(null, 1, List.of(new LineaVentaEntradaDTO(13, 1))), CAMILA))
				.isInstanceOf(ConflictoException.class)
				.hasMessage("La sucursal Sede Centro no maneja el producto PI-0301 (Producto PI-0301).");
	}

	@Test
	void lineasRepetidasDelMismoProductoSeSumanEnUnSoloDescuento() {
		prepararVenta(producto(1, "HM-0001"));
		when(inventarioRepository.descontar(1, 1, 5)).thenReturn(1);

		ventaService.registrar(new VentaEntradaDTO(null, 1, List.of(new LineaVentaEntradaDTO(1, 2), new LineaVentaEntradaDTO(1, 3))), CAMILA);

		verify(inventarioRepository).descontar(1, 1, 5);
		verify(inventarioRepository, never()).descontar(eq(1), eq(1), eq(2));
	}

	@Test
	void elVendedorNoConsultaVentasDeOtraSede() {
		assertThatThrownBy(() -> ventaService.buscar(null, null, 2, null, null, null, null, 0, 20, CAMILA))
				.isInstanceOf(SinPermisoException.class);
		verify(ventaRepository, never()).buscar(any(), any(), any(), any(), any(), any(), any(), any());
		verify(inventarioRepository, never()).descontar(anyInt(), anyInt(), anyInt());
	}
}
