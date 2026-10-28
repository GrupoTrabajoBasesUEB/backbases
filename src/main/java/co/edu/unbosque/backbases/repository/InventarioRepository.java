package co.edu.unbosque.backbases.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import co.edu.unbosque.backbases.entity.Inventario;
import co.edu.unbosque.backbases.entity.InventarioId;

public interface InventarioRepository extends JpaRepository<Inventario, InventarioId> {

	@Query("""
			select i from Inventario i join fetch i.producto p join fetch i.sucursal s
			where s.idSucursal = :idSucursal
			  and (:texto is null or p.codigo like concat('%', :texto, '%') or p.nombre like concat('%', :texto, '%'))
			order by p.nombre
			""")
	List<Inventario> deSucursal(@Param("idSucursal") Integer idSucursal, @Param("texto") String texto);

	@Query("select i from Inventario i join fetch i.producto p join fetch i.sucursal s where p.idProducto = :idProducto order by s.nombre")
	List<Inventario> deProducto(@Param("idProducto") Integer idProducto);

	@Query("select i from Inventario i join fetch i.producto join fetch i.sucursal where i.id = :id")
	Optional<Inventario> buscar(@Param("id") InventarioId id);

	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query(value = """
			INSERT INTO INVENTARIO (ID_SUCURSAL, ID_PRODUCTO, CANTIDAD_DISPONIBLE, UBICACION_BODEGA)
			VALUES (:idSucursal, :idProducto, :unidades, COALESCE(:ubicacion, '')) AS nuevo
			ON DUPLICATE KEY UPDATE
			  CANTIDAD_DISPONIBLE = INVENTARIO.CANTIDAD_DISPONIBLE + nuevo.CANTIDAD_DISPONIBLE,
			  UBICACION_BODEGA = COALESCE(:ubicacion, INVENTARIO.UBICACION_BODEGA)
			""", nativeQuery = true)
	int sumarEntrada(@Param("idSucursal") Integer idSucursal, @Param("idProducto") Integer idProducto,
			@Param("unidades") int unidades, @Param("ubicacion") String ubicacion);

	@Modifying(flushAutomatically = true)
	@Query(value = """
			UPDATE INVENTARIO SET CANTIDAD_DISPONIBLE = CANTIDAD_DISPONIBLE - :unidades
			WHERE ID_SUCURSAL = :idSucursal AND ID_PRODUCTO = :idProducto AND CANTIDAD_DISPONIBLE >= :unidades
			""", nativeQuery = true)
	int descontar(@Param("idSucursal") Integer idSucursal, @Param("idProducto") Integer idProducto, @Param("unidades") int unidades);

	@Query(value = "SELECT CANTIDAD_DISPONIBLE FROM INVENTARIO WHERE ID_SUCURSAL = :idSucursal AND ID_PRODUCTO = :idProducto", nativeQuery = true)
	Optional<Integer> cantidad(@Param("idSucursal") Integer idSucursal, @Param("idProducto") Integer idProducto);
}
