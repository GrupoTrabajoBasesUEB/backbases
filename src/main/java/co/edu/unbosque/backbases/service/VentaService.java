package co.edu.unbosque.backbases.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.backbases.dto.LineaVendidaDTO;
import co.edu.unbosque.backbases.dto.LineaVentaDTO;
import co.edu.unbosque.backbases.dto.LineaVentaEntradaDTO;
import co.edu.unbosque.backbases.dto.PaginaDTO;
import co.edu.unbosque.backbases.dto.VentaDTO;
import co.edu.unbosque.backbases.dto.VentaEntradaDTO;
import co.edu.unbosque.backbases.entity.Cliente;
import co.edu.unbosque.backbases.entity.ClienteNatural;
import co.edu.unbosque.backbases.entity.DetalleVenta;
import co.edu.unbosque.backbases.entity.Empleado;
import co.edu.unbosque.backbases.entity.MetodoPago;
import co.edu.unbosque.backbases.entity.Producto;
import co.edu.unbosque.backbases.entity.Vendedor;
import co.edu.unbosque.backbases.entity.Venta;
import co.edu.unbosque.backbases.exception.ConflictoException;
import co.edu.unbosque.backbases.exception.RecursoNoEncontradoException;
import co.edu.unbosque.backbases.exception.ReglaNegocioException;
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
import co.edu.unbosque.backbases.repository.VentaRepository.TotalVenta;
import co.edu.unbosque.backbases.security.UsuarioAutenticado;

@Service
public class VentaService {

	public static final ZoneId ZONA = ZoneId.of("America/Bogota");
	private static final int TAMANO_MAXIMO = 100;

	private final VentaRepository ventaRepository;
	private final DetalleVentaRepository detalleRepository;
	private final InventarioRepository inventarioRepository;
	private final ProductoRepository productoRepository;
	private final VendedorRepository vendedorRepository;
	private final ClienteRepository clienteRepository;
	private final ClienteNaturalRepository naturalRepository;
	private final ClienteJuridicoRepository juridicoRepository;
	private final MetodoPagoRepository metodoPagoRepository;

	public VentaService(VentaRepository ventaRepository, DetalleVentaRepository detalleRepository, InventarioRepository inventarioRepository,
			ProductoRepository productoRepository, VendedorRepository vendedorRepository, ClienteRepository clienteRepository,
			ClienteNaturalRepository naturalRepository, ClienteJuridicoRepository juridicoRepository,
			MetodoPagoRepository metodoPagoRepository) {
		this.ventaRepository = ventaRepository;
		this.detalleRepository = detalleRepository;
		this.inventarioRepository = inventarioRepository;
		this.productoRepository = productoRepository;
		this.vendedorRepository = vendedorRepository;
		this.clienteRepository = clienteRepository;
		this.naturalRepository = naturalRepository;
		this.juridicoRepository = juridicoRepository;
		this.metodoPagoRepository = metodoPagoRepository;
	}

	@Transactional
	public VentaDTO registrar(VentaEntradaDTO entrada, UsuarioAutenticado usuario) {
		Vendedor vendedor = vendedorRepository.buscarConSucursal(usuario.idEmpleado())
				.orElseThrow(() -> new SinPermisoException("Solo un vendedor puede registrar ventas."));
		Cliente cliente = entrada.idCliente() == null
				? clienteRepository.findByNumeroDocumento(Cliente.DOCUMENTO_CONSUMIDOR_FINAL)
						.orElseThrow(() -> new RecursoNoEncontradoException("No está registrado el cliente consumidor final."))
				: clienteRepository.findById(entrada.idCliente())
						.orElseThrow(() -> RecursoNoEncontradoException.de("el cliente", entrada.idCliente()));
		MetodoPago metodo = metodoPagoRepository.findById(entrada.idMetodoPago())
				.orElseThrow(() -> RecursoNoEncontradoException.de("el método de pago", entrada.idMetodoPago()));
		SortedMap<Integer, Integer> cantidades = agrupar(entrada.lineas());
		Map<Integer, Producto> productos = productoRepository.findAllById(cantidades.keySet()).stream()
				.collect(Collectors.toMap(Producto::getIdProducto, Function.identity()));
		cantidades.keySet().stream().filter(id -> !productos.containsKey(id)).findFirst().ifPresent(id -> {
			throw RecursoNoEncontradoException.de("el producto", id);
		});

		ZonedDateTime ahora = ZonedDateTime.now(ZONA).truncatedTo(ChronoUnit.SECONDS);
		Venta venta = new Venta();
		venta.setNumeroFactura("TMP-" + UUID.randomUUID().toString().replace("-", "").substring(0, 16));
		venta.setFecha(ahora.toLocalDate());
		venta.setHora(ahora.toLocalTime());
		venta.setCliente(cliente);
		venta.setVendedor(vendedor);
		venta.setSucursal(vendedor.getSucursal());
		venta.setMetodoPago(metodo);
		venta = ventaRepository.saveAndFlush(venta);

		Integer idSucursal = vendedor.getSucursal().getIdSucursal();
		List<DetalleVenta> detalles = new ArrayList<>();
		for (Map.Entry<Integer, Integer> linea : cantidades.entrySet()) {
			Producto producto = productos.get(linea.getKey());
			int unidades = linea.getValue();
			if (inventarioRepository.descontar(idSucursal, producto.getIdProducto(), unidades) == 0) {
				throw sinStock(producto, unidades, inventarioRepository.cantidad(idSucursal, producto.getIdProducto()).orElse(null),
						vendedor.getSucursal().getNombre());
			}
			detalles.add(new DetalleVenta(venta, producto, unidades, producto.getPrecioBaseVenta()));
		}
		detalleRepository.saveAll(detalles);
		venta.setNumeroFactura(String.format("FV-%06d", venta.getIdVenta()));
		ventaRepository.flush();

		List<LineaVentaDTO> lineas = detalles.stream().map(LineaVentaDTO::de).toList();
		BigDecimal total = lineas.stream().map(LineaVentaDTO::subtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
		return aDTO(venta, nombreCliente(cliente), total, lineas);
	}

	@Transactional(readOnly = true)
	public PaginaDTO<VentaDTO> buscar(LocalDate desde, LocalDate hasta, Integer idSucursal, Integer idEmpleado, Integer idCliente,
			Integer idMetodoPago, String factura, int pagina, int tamano, UsuarioAutenticado usuario) {
		Integer sucursal = sucursalPermitida(idSucursal, usuario);
		if (desde != null && hasta != null && desde.isAfter(hasta)) {
			throw new ReglaNegocioException("La fecha inicial no puede ser posterior a la final.");
		}
		String filtroFactura = factura == null || factura.isBlank() ? null : factura.trim();
		Page<Venta> resultado = ventaRepository.buscar(desde, hasta, sucursal, idEmpleado, idCliente, idMetodoPago, filtroFactura,
				PageRequest.of(Math.max(pagina, 0), Math.clamp(tamano, 1, TAMANO_MAXIMO)));
		Set<Integer> idsVenta = resultado.getContent().stream().map(Venta::getIdVenta).collect(Collectors.toSet());
		Map<Integer, BigDecimal> totales = idsVenta.isEmpty() ? Map.of()
				: ventaRepository.totales(idsVenta).stream().collect(Collectors.toMap(TotalVenta::getIdVenta, TotalVenta::getMontoTotal));
		Map<Integer, String> nombres = nombresClientes(resultado.getContent().stream().map(v -> v.getCliente().getIdCliente()).collect(Collectors.toSet()));
		List<VentaDTO> contenido = resultado.getContent().stream()
				.map(v -> aDTO(v, nombres.getOrDefault(v.getCliente().getIdCliente(), v.getCliente().getNumeroDocumento()),
						totales.getOrDefault(v.getIdVenta(), BigDecimal.ZERO), null))
				.toList();
		return new PaginaDTO<>(contenido, resultado.getNumber(), resultado.getSize(), resultado.getTotalElements(), resultado.getTotalPages());
	}

	@Transactional(readOnly = true)
	public List<LineaVendidaDTO> lineas(LocalDate desde, LocalDate hasta, Integer idSucursal, Integer idEmpleado, Integer idCliente,
			Integer idMetodoPago, UsuarioAutenticado usuario) {
		Integer sucursal = sucursalPermitida(idSucursal, usuario);
		return detalleRepository.lineas(desde, hasta, sucursal, idEmpleado, idCliente, idMetodoPago).stream().map(LineaVendidaDTO::de).toList();
	}

	@Transactional(readOnly = true)
	public VentaDTO obtener(Integer idVenta, UsuarioAutenticado usuario) {
		Venta venta = ventaRepository.buscarCompleta(idVenta).orElseThrow(() -> RecursoNoEncontradoException.de("la venta", idVenta));
		if (!usuario.esAdministrador() && !venta.getSucursal().getIdSucursal().equals(usuario.idSucursal())) {
			throw new SinPermisoException("Solo puede consultar las ventas de su sucursal.");
		}
		List<LineaVentaDTO> lineas = detalleRepository.deVenta(idVenta).stream().map(LineaVentaDTO::de).toList();
		BigDecimal total = ventaRepository.totales(List.of(idVenta)).stream().findFirst().map(TotalVenta::getMontoTotal).orElse(BigDecimal.ZERO);
		return aDTO(venta, nombreCliente(venta.getCliente()), total, lineas);
	}

	private static SortedMap<Integer, Integer> agrupar(List<LineaVentaEntradaDTO> lineas) {
		SortedMap<Integer, Integer> cantidades = new TreeMap<>();
		lineas.forEach(l -> cantidades.merge(l.idProducto(), l.cantidad(), Integer::sum));
		return cantidades;
	}

	private static ConflictoException sinStock(Producto producto, int pedidas, Integer disponibles, String sucursal) {
		if (disponibles == null) {
			return new ConflictoException("La sucursal " + sucursal + " no maneja el producto " + producto.getCodigo() + " ("
					+ producto.getNombre() + ").");
		}
		return new ConflictoException("Stock insuficiente de " + producto.getCodigo() + " (" + producto.getNombre() + "): hay "
				+ disponibles + " y se piden " + pedidas + ".");
	}

	private static Integer sucursalPermitida(Integer idSucursal, UsuarioAutenticado usuario) {
		if (usuario.esAdministrador()) {
			return idSucursal;
		}
		if (idSucursal != null && !idSucursal.equals(usuario.idSucursal())) {
			throw new SinPermisoException("Solo puede consultar las ventas de su sucursal.");
		}
		return usuario.idSucursal();
	}

	private String nombreCliente(Cliente cliente) {
		return nombresClientes(Set.of(cliente.getIdCliente())).getOrDefault(cliente.getIdCliente(), cliente.getNumeroDocumento());
	}

	private Map<Integer, String> nombresClientes(Set<Integer> ids) {
		if (ids.isEmpty()) {
			return Map.of();
		}
		Map<Integer, String> nombres = naturalRepository.findByIdClienteIn(ids).stream()
				.collect(Collectors.toMap(ClienteNatural::getIdCliente, n -> n.getNombre() + " " + n.getApellido()));
		juridicoRepository.findByIdClienteIn(ids).forEach(j -> nombres.put(j.getIdCliente(), j.getRazonSocial()));
		return nombres;
	}

	private static VentaDTO aDTO(Venta v, String nombreCliente, BigDecimal total, List<LineaVentaDTO> lineas) {
		Empleado empleado = v.getVendedor().getEmpleado();
		return new VentaDTO(v.getIdVenta(), v.getNumeroFactura(), v.getFecha(), v.getHora(), v.getCliente().getIdCliente(),
				nombreCliente, v.getCliente().getNumeroDocumento(), empleado.getIdEmpleado(),
				empleado.getNombre() + " " + empleado.getApellido(), v.getSucursal().getIdSucursal(), v.getSucursal().getNombre(),
				v.getMetodoPago().getIdMetodoPago(), v.getMetodoPago().getTipo(), total, lineas);
	}
}
