package co.edu.unbosque.backbases.service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.backbases.dto.SuministroCondicionesDTO;
import co.edu.unbosque.backbases.dto.SuministroDTO;
import co.edu.unbosque.backbases.dto.SuministroEntradaDTO;
import co.edu.unbosque.backbases.entity.Producto;
import co.edu.unbosque.backbases.entity.Proveedor;
import co.edu.unbosque.backbases.entity.Sucursal;
import co.edu.unbosque.backbases.entity.Suministro;
import co.edu.unbosque.backbases.entity.SuministroId;
import co.edu.unbosque.backbases.exception.ConflictoException;
import co.edu.unbosque.backbases.exception.RecursoNoEncontradoException;
import co.edu.unbosque.backbases.repository.ProductoRepository;
import co.edu.unbosque.backbases.repository.ProveedorRepository;
import co.edu.unbosque.backbases.repository.SucursalRepository;
import co.edu.unbosque.backbases.repository.SuministroRepository;
import co.edu.unbosque.backbases.repository.SuministroRepository.ResumenEvaluaciones;

@Service
public class SuministroService {

	private final SuministroRepository suministroRepository;
	private final ProveedorRepository proveedorRepository;
	private final ProductoRepository productoRepository;
	private final SucursalRepository sucursalRepository;

	public SuministroService(SuministroRepository suministroRepository, ProveedorRepository proveedorRepository,
			ProductoRepository productoRepository, SucursalRepository sucursalRepository) {
		this.suministroRepository = suministroRepository;
		this.proveedorRepository = proveedorRepository;
		this.productoRepository = productoRepository;
		this.sucursalRepository = sucursalRepository;
	}

	@Transactional(readOnly = true)
	public List<SuministroDTO> buscar(String nit, Integer idProducto, Integer idSucursal) {
		Map<SuministroId, ResumenEvaluaciones> resumen = suministroRepository.resumenEvaluaciones().stream()
				.collect(Collectors.toMap(r -> new SuministroId(r.getNit(), r.getIdProducto(), r.getIdSucursal()), Function.identity()));
		return suministroRepository.buscar(nit == null || nit.isBlank() ? null : nit.trim(), idProducto, idSucursal).stream()
				.map(s -> aDTO(s, resumen.get(s.getId())))
				.toList();
	}

	@Transactional
	public SuministroDTO crear(SuministroEntradaDTO entrada) {
		Proveedor proveedor = proveedorRepository.findById(entrada.nit().trim())
				.orElseThrow(() -> new RecursoNoEncontradoException("No existe el proveedor con NIT " + entrada.nit() + "."));
		Producto producto = productoRepository.findById(entrada.idProducto())
				.orElseThrow(() -> RecursoNoEncontradoException.de("el producto", entrada.idProducto()));
		Sucursal sucursal = sucursalRepository.findById(entrada.idSucursal())
				.orElseThrow(() -> RecursoNoEncontradoException.de("la sucursal", entrada.idSucursal()));
		Suministro suministro = new Suministro(proveedor, producto, sucursal);
		if (suministroRepository.existsById(suministro.getId())) {
			throw new ConflictoException("Este proveedor ya suministra ese producto a esa sucursal. Edite el registro existente.");
		}
		suministro.setPrecioSuministro(entrada.precioSuministro());
		suministro.setTiempoEntregaDias(entrada.tiempoEntregaDias());
		return aDTO(suministroRepository.save(suministro), null);
	}

	@Transactional
	public SuministroDTO actualizar(String nit, Integer idProducto, Integer idSucursal, SuministroCondicionesDTO condiciones) {
		Suministro suministro = buscarSuministro(new SuministroId(nit, idProducto, idSucursal));
		suministro.setPrecioSuministro(condiciones.precioSuministro());
		suministro.setTiempoEntregaDias(condiciones.tiempoEntregaDias());
		return conResumen(suministro);
	}

	@Transactional
	public void eliminar(String nit, Integer idProducto, Integer idSucursal) {
		Suministro suministro = buscarSuministro(new SuministroId(nit, idProducto, idSucursal));
		ConflictoException.siEstaEnUso("el suministro",
				Map.of("evaluaciones de calidad", suministroRepository.contarEvaluaciones(nit, idProducto, idSucursal)));
		suministroRepository.delete(suministro);
	}

	Suministro buscarSuministro(SuministroId id) {
		return suministroRepository.buscarCompleto(id)
				.orElseThrow(() -> new RecursoNoEncontradoException("Ese proveedor no suministra ese producto a esa sucursal."));
	}

	private SuministroDTO conResumen(Suministro suministro) {
		ResumenEvaluaciones resumen = suministroRepository.resumenEvaluaciones().stream()
				.filter(r -> suministro.getId().equals(new SuministroId(r.getNit(), r.getIdProducto(), r.getIdSucursal())))
				.findFirst().orElse(null);
		return aDTO(suministro, resumen);
	}

	private static SuministroDTO aDTO(Suministro s, ResumenEvaluaciones resumen) {
		return new SuministroDTO(s.getProveedor().getNit(), s.getProveedor().getNombre(), s.getProducto().getIdProducto(),
				s.getProducto().getCodigo(), s.getProducto().getNombre(), s.getSucursal().getIdSucursal(), s.getSucursal().getNombre(),
				s.getPrecioSuministro(), s.getTiempoEntregaDias(),
				resumen == null ? 0 : resumen.getCantidad(),
				resumen == null ? null : resumen.getPromedio(),
				resumen == null ? null : resumen.getUltima());
	}
}
