package co.edu.unbosque.backbases.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import co.edu.unbosque.backbases.entity.Producto;

public interface ProductoRepository extends JpaRepository<Producto, Integer> {

	interface StockProducto {
		Integer getIdProducto();

		Long getStock();
	}

	@Query("""
			select p from Producto p join fetch p.categoria c left join c.categoriaPrincipal cp join fetch p.marca m
			where (:texto is null or p.codigo like concat('%', :texto, '%') or p.nombre like concat('%', :texto, '%'))
			  and (:idCategoria is null or c.idCategoria = :idCategoria or cp.idCategoria = :idCategoria)
			  and (:idMarca is null or m.idMarca = :idMarca)
			order by p.nombre
			""")
	List<Producto> buscar(@Param("texto") String texto, @Param("idCategoria") Integer idCategoria, @Param("idMarca") Integer idMarca);

	@Query("select p from Producto p join fetch p.categoria join fetch p.marca where p.idProducto = :idProducto")
	Optional<Producto> buscarCompleto(@Param("idProducto") Integer idProducto);

	boolean existsByCodigo(String codigo);

	boolean existsByCodigoAndIdProductoNot(String codigo, Integer idProducto);

	@Query(value = "SELECT ID_PRODUCTO AS idProducto, CAST(STOCK AS SIGNED) AS stock FROM V_PRODUCTO_STOCK", nativeQuery = true)
	List<StockProducto> stockDeTodos();

	@Query(value = "SELECT CAST(STOCK AS SIGNED) FROM V_PRODUCTO_STOCK WHERE ID_PRODUCTO = :idProducto", nativeQuery = true)
	Long stockDe(@Param("idProducto") Integer idProducto);

	@Query(value = "SELECT COUNT(*) FROM DETALLE_VENTA WHERE ID_PRODUCTO = :idProducto", nativeQuery = true)
	long contarLineasVenta(@Param("idProducto") Integer idProducto);

	@Query(value = "SELECT COUNT(*) FROM INVENTARIO WHERE ID_PRODUCTO = :idProducto", nativeQuery = true)
	long contarInventario(@Param("idProducto") Integer idProducto);

	@Query(value = "SELECT COUNT(*) FROM SUMINISTRO WHERE ID_PRODUCTO = :idProducto", nativeQuery = true)
	long contarSuministros(@Param("idProducto") Integer idProducto);
}
