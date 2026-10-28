package co.edu.unbosque.backbases.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.backbases.dto.EntradaMercanciaDTO;
import co.edu.unbosque.backbases.dto.InventarioDTO;
import co.edu.unbosque.backbases.dto.InventarioEntradaDTO;
import co.edu.unbosque.backbases.entity.Inventario;
import co.edu.unbosque.backbases.entity.InventarioId;
import co.edu.unbosque.backbases.entity.Producto;
import co.edu.unbosque.backbases.entity.Sucursal;
import co.edu.unbosque.backbases.exception.RecursoNoEncontradoException;
import co.edu.unbosque.backbases.exception.ReglaNegocioException;
import co.edu.unbosque.backbases.repository.InventarioRepository;
import co.edu.unbosque.backbases.repository.ProductoRepository;
import co.edu.unbosque.backbases.repository.SucursalRepository;

@Service
public class InventarioService {

	private final InventarioRepository inventarioRepository;
	private final SucursalRepository sucursalRepository;
	private final ProductoRepository productoRepository;

	public InventarioService(InventarioRepository inventarioRepository, SucursalRepository sucursalRepository,
			ProductoRepository productoRepository) {
		this.inventarioRepository = inventarioRepository;
		this.sucursalRepository = sucursalRepository;
		this.productoRepository = productoRepository;
	}

	@Transactional(readOnly = true)
	public List<InventarioDTO> deSucursal(Integer idSucursal, String texto) {
		buscarSucursal(idSucursal);
		String filtro = texto == null || texto.isBlank() ? null : texto.trim();
		return inventarioRepository.deSucursal(idSucursal, filtro).stream().map(InventarioDTO::de).toList();
	}

	@Transactional(readOnly = true)
	public List<InventarioDTO> todo(String texto) {
		String filtro = texto == null || texto.isBlank() ? null : texto.trim();
		return inventarioRepository.todo(filtro).stream().map(InventarioDTO::de).toList();
	}

	@Transactional(readOnly = true)
	public List<InventarioDTO> deProducto(Integer idProducto) {
		buscarProducto(idProducto);
		return inventarioRepository.deProducto(idProducto).stream().map(InventarioDTO::de).toList();
	}

	@Transactional
	public InventarioDTO guardar(Integer idSucursal, Integer idProducto, InventarioEntradaDTO entrada) {
		Inventario registro = inventarioRepository.buscar(new InventarioId(idSucursal, idProducto))
				.orElseGet(() -> new Inventario(buscarSucursal(idSucursal), buscarProducto(idProducto)));
		registro.setCantidadDisponible(entrada.cantidadDisponible());
		registro.setUbicacionBodega(entrada.ubicacionBodega().trim());
		return InventarioDTO.de(inventarioRepository.save(registro));
	}

	@Transactional
	public InventarioDTO registrarEntrada(Integer idSucursal, Integer idProducto, EntradaMercanciaDTO entrada) {
		buscarSucursal(idSucursal);
		buscarProducto(idProducto);
		String ubicacion = entrada.ubicacionBodega() == null || entrada.ubicacionBodega().isBlank() ? null : entrada.ubicacionBodega().trim();
		InventarioId id = new InventarioId(idSucursal, idProducto);
		if (ubicacion == null && !inventarioRepository.existsById(id)) {
			throw new ReglaNegocioException("Es la primera entrada de este producto en la sucursal: indique en qué parte de la bodega queda.");
		}
		inventarioRepository.sumarEntrada(idSucursal, idProducto, entrada.unidades(), ubicacion);
		return InventarioDTO.de(inventarioRepository.buscar(id).orElseThrow());
	}

	@Transactional
	public void retirar(Integer idSucursal, Integer idProducto) {
		Inventario registro = inventarioRepository.buscar(new InventarioId(idSucursal, idProducto))
				.orElseThrow(() -> new RecursoNoEncontradoException("Ese producto no está en el inventario de la sucursal."));
		if (registro.getCantidadDisponible() > 0) {
			throw new ReglaNegocioException("Aún quedan " + registro.getCantidadDisponible()
					+ " unidades en esta sucursal. Lleve la cantidad a 0 antes de retirar el producto.");
		}
		inventarioRepository.delete(registro);
	}

	private Sucursal buscarSucursal(Integer idSucursal) {
		return sucursalRepository.findById(idSucursal).orElseThrow(() -> RecursoNoEncontradoException.de("la sucursal", idSucursal));
	}

	private Producto buscarProducto(Integer idProducto) {
		return productoRepository.findById(idProducto).orElseThrow(() -> RecursoNoEncontradoException.de("el producto", idProducto));
	}
}
