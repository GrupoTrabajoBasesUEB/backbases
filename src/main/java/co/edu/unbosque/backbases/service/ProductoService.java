package co.edu.unbosque.backbases.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.backbases.dto.ProductoDTO;
import co.edu.unbosque.backbases.dto.ProductoEntradaDTO;
import co.edu.unbosque.backbases.entity.Producto;
import co.edu.unbosque.backbases.exception.ConflictoException;
import co.edu.unbosque.backbases.exception.RecursoNoEncontradoException;
import co.edu.unbosque.backbases.exception.ReglaNegocioException;
import co.edu.unbosque.backbases.repository.CategoriaRepository;
import co.edu.unbosque.backbases.repository.MarcaRepository;
import co.edu.unbosque.backbases.repository.ProductoRepository;
import co.edu.unbosque.backbases.repository.ProductoRepository.StockProducto;

@Service
public class ProductoService {

	private final ProductoRepository productoRepository;
	private final CategoriaRepository categoriaRepository;
	private final MarcaRepository marcaRepository;

	public ProductoService(ProductoRepository productoRepository, CategoriaRepository categoriaRepository, MarcaRepository marcaRepository) {
		this.productoRepository = productoRepository;
		this.categoriaRepository = categoriaRepository;
		this.marcaRepository = marcaRepository;
	}

	@Transactional(readOnly = true)
	public List<ProductoDTO> buscar(String texto, Integer idCategoria, Integer idMarca) {
		String filtro = texto == null || texto.isBlank() ? null : texto.trim();
		Map<Integer, Long> stock = productoRepository.stockDeTodos().stream()
				.collect(Collectors.toMap(StockProducto::getIdProducto, StockProducto::getStock));
		return productoRepository.buscar(filtro, idCategoria, idMarca).stream()
				.map(p -> ProductoDTO.de(p, stock.getOrDefault(p.getIdProducto(), 0L)))
				.toList();
	}

	@Transactional(readOnly = true)
	public ProductoDTO obtener(Integer idProducto) {
		return conStock(buscarProducto(idProducto));
	}

	@Transactional
	public ProductoDTO crear(ProductoEntradaDTO entrada) {
		String codigo = entrada.codigo().trim();
		if (productoRepository.existsByCodigo(codigo)) {
			throw codigoRepetido(codigo);
		}
		Producto producto = new Producto();
		aplicar(entrada, producto);
		return ProductoDTO.de(productoRepository.save(producto), 0);
	}

	@Transactional
	public ProductoDTO actualizar(Integer idProducto, ProductoEntradaDTO entrada) {
		Producto producto = buscarProducto(idProducto);
		String codigo = entrada.codigo().trim();
		if (productoRepository.existsByCodigoAndIdProductoNot(codigo, idProducto)) {
			throw codigoRepetido(codigo);
		}
		aplicar(entrada, producto);
		return conStock(producto);
	}

	@Transactional
	public void eliminar(Integer idProducto) {
		Producto producto = buscarProducto(idProducto);
		Map<String, Long> referencias = new LinkedHashMap<>();
		referencias.put("líneas de venta", productoRepository.contarLineasVenta(idProducto));
		referencias.put("registros de inventario", productoRepository.contarInventario(idProducto));
		referencias.put("suministros", productoRepository.contarSuministros(idProducto));
		ConflictoException.siEstaEnUso("el producto", referencias);
		productoRepository.delete(producto);
	}

	Producto buscarProducto(Integer idProducto) {
		return productoRepository.buscarCompleto(idProducto)
				.orElseThrow(() -> RecursoNoEncontradoException.de("el producto", idProducto));
	}

	private ProductoDTO conStock(Producto producto) {
		Long stock = productoRepository.stockDe(producto.getIdProducto());
		return ProductoDTO.de(producto, stock == null ? 0 : stock);
	}

	private void aplicar(ProductoEntradaDTO entrada, Producto producto) {
		if (entrada.precioBaseVenta().compareTo(entrada.costoCompra()) < 0) {
			throw new ReglaNegocioException("El precio base de venta no puede ser menor que el costo de compra.");
		}
		producto.setCodigo(entrada.codigo().trim());
		producto.setNombre(entrada.nombreProducto().trim());
		producto.setDescripcion(entrada.descripcion() == null || entrada.descripcion().isBlank() ? null : entrada.descripcion().trim());
		producto.setCostoCompra(entrada.costoCompra());
		producto.setPrecioBaseVenta(entrada.precioBaseVenta());
		producto.setCategoria(categoriaRepository.findById(entrada.idCategoria())
				.orElseThrow(() -> RecursoNoEncontradoException.de("la categoría", entrada.idCategoria())));
		producto.setMarca(marcaRepository.findById(entrada.idMarca())
				.orElseThrow(() -> RecursoNoEncontradoException.de("la marca", entrada.idMarca())));
	}

	private static ConflictoException codigoRepetido(String codigo) {
		return new ConflictoException("El código " + codigo + " ya pertenece a otro producto.");
	}
}
