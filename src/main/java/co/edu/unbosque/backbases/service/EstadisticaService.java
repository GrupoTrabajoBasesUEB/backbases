package co.edu.unbosque.backbases.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.backbases.dto.EstadisticasDTO.Agrupado;
import co.edu.unbosque.backbases.dto.EstadisticasDTO.Comision;
import co.edu.unbosque.backbases.dto.EstadisticasDTO.Comparativo;
import co.edu.unbosque.backbases.dto.EstadisticasDTO.ProductoVendido;
import co.edu.unbosque.backbases.dto.EstadisticasDTO.SucursalDetalle;
import co.edu.unbosque.backbases.dto.EstadisticasDTO.UnidadesInventario;
import co.edu.unbosque.backbases.dto.EstadisticasDTO.VentasDia;
import co.edu.unbosque.backbases.dto.EstadisticasDTO.VentasProducto;
import co.edu.unbosque.backbases.exception.RecursoNoEncontradoException;
import co.edu.unbosque.backbases.exception.ReglaNegocioException;
import co.edu.unbosque.backbases.exception.SinPermisoException;
import co.edu.unbosque.backbases.repository.EstadisticaRepository;
import co.edu.unbosque.backbases.repository.EstadisticaRepository.Filtro;
import co.edu.unbosque.backbases.repository.ProductoRepository;
import co.edu.unbosque.backbases.security.UsuarioAutenticado;

@Service
@Transactional(readOnly = true)
public class EstadisticaService {

	public static final int DIAS_POR_DEFECTO = 30;
	public static final int DIAS_MAXIMOS = 366;
	public static final int UMBRAL_STOCK_BAJO = 5;

	private final EstadisticaRepository estadisticaRepository;
	private final ProductoRepository productoRepository;

	public EstadisticaService(EstadisticaRepository estadisticaRepository, ProductoRepository productoRepository) {
		this.estadisticaRepository = estadisticaRepository;
		this.productoRepository = productoRepository;
	}

	public Filtro filtro(LocalDate desde, LocalDate hasta, Integer idSucursal, Integer idEmpleado, UsuarioAutenticado usuario) {
		LocalDate fin = hasta == null ? LocalDate.now(VentaService.ZONA) : hasta;
		LocalDate inicio = desde == null ? fin.minusDays(DIAS_POR_DEFECTO - 1L) : desde;
		if (inicio.isAfter(fin)) {
			throw new ReglaNegocioException("La fecha inicial no puede ser posterior a la final.");
		}
		if (ChronoUnit.DAYS.between(inicio, fin) + 1 > DIAS_MAXIMOS) {
			throw new ReglaNegocioException("El periodo no puede superar " + DIAS_MAXIMOS + " días.");
		}
		return new Filtro(inicio, fin, sucursalPermitida(idSucursal, usuario), idEmpleado);
	}

	public Comparativo resumen(Filtro filtro) {
		long dias = ChronoUnit.DAYS.between(filtro.desde(), filtro.hasta()) + 1;
		Filtro anterior = new Filtro(filtro.desde().minusDays(dias), filtro.desde().minusDays(1), filtro.idSucursal(), filtro.idEmpleado());
		return new Comparativo(filtro.desde(), filtro.hasta(), estadisticaRepository.resumen(filtro), anterior.desde(), anterior.hasta(),
				estadisticaRepository.resumen(anterior));
	}

	public List<VentasDia> ventasPorDia(Filtro filtro) {
		return estadisticaRepository.ventasPorDia(filtro);
	}

	public List<Agrupado> porMetodoPago(Filtro filtro) {
		return estadisticaRepository.porMetodoPago(filtro);
	}

	public List<ProductoVendido> rendimientoPorProducto(Filtro filtro) {
		return estadisticaRepository.rendimientoPorProducto(filtro);
	}

	public VentasProducto ventasDeProducto(Integer idProducto, Filtro filtro) {
		if (!productoRepository.existsById(idProducto)) {
			throw RecursoNoEncontradoException.de("el producto", idProducto);
		}
		List<VentasDia> porDia = estadisticaRepository.ventasDeProductoPorDia(filtro, idProducto);
		long unidades = porDia.stream().mapToLong(VentasDia::cantidad).sum();
		BigDecimal total = porDia.stream().map(VentasDia::total).reduce(BigDecimal.ZERO, BigDecimal::add);
		return new VentasProducto(idProducto, unidades, total, porDia);
	}

	public List<UnidadesInventario> unidadesPorInventario(Filtro filtro) {
		return estadisticaRepository.unidadesPorInventario(filtro);
	}

	public List<SucursalDetalle> sucursalesDetalle(Filtro filtro) {
		return estadisticaRepository.sucursalesDetalle(filtro, UMBRAL_STOCK_BAJO);
	}

	public List<Comision> comisiones(Filtro filtro, UsuarioAutenticado usuario) {
		if (!usuario.esAdministrador()) {
			if (filtro.idEmpleado() != null && !filtro.idEmpleado().equals(usuario.idEmpleado())) {
				throw new SinPermisoException("Solo puede consultar su propia comisión.");
			}
			filtro = new Filtro(filtro.desde(), filtro.hasta(), filtro.idSucursal(), usuario.idEmpleado());
		}
		return estadisticaRepository.comisiones(filtro);
	}

	private static Integer sucursalPermitida(Integer idSucursal, UsuarioAutenticado usuario) {
		if (usuario.esAdministrador()) {
			return idSucursal;
		}
		if (idSucursal != null && !idSucursal.equals(usuario.idSucursal())) {
			throw new SinPermisoException("Solo puede consultar las estadísticas de su sucursal.");
		}
		return usuario.idSucursal();
	}
}
