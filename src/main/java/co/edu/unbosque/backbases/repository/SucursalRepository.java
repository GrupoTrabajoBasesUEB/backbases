package co.edu.unbosque.backbases.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import co.edu.unbosque.backbases.entity.Sucursal;

public interface SucursalRepository extends JpaRepository<Sucursal, Integer> {

	Optional<Sucursal> findByAdministradorIdEmpleado(Integer idEmpleado);

	@Query("select s from Sucursal s join fetch s.administrador a join fetch a.empleado order by s.nombre")
	List<Sucursal> listarConEncargado();

	@Query("select s from Sucursal s join fetch s.administrador a join fetch a.empleado where s.idSucursal = :idSucursal")
	Optional<Sucursal> buscarConEncargado(@Param("idSucursal") Integer idSucursal);

	boolean existsByNombre(String nombre);

	boolean existsByNombreAndIdSucursalNot(String nombre, Integer idSucursal);

	@Query(value = "SELECT COUNT(*) FROM VENTA WHERE ID_SUCURSAL = :idSucursal", nativeQuery = true)
	long contarVentas(@Param("idSucursal") Integer idSucursal);

	@Query(value = "SELECT COUNT(*) FROM INVENTARIO WHERE ID_SUCURSAL = :idSucursal", nativeQuery = true)
	long contarInventario(@Param("idSucursal") Integer idSucursal);

	@Query(value = "SELECT COUNT(*) FROM SUMINISTRO WHERE ID_SUCURSAL = :idSucursal", nativeQuery = true)
	long contarSuministros(@Param("idSucursal") Integer idSucursal);

	@Query(value = "SELECT COUNT(*) FROM VENDEDOR WHERE ID_SUCURSAL = :idSucursal", nativeQuery = true)
	long contarVendedores(@Param("idSucursal") Integer idSucursal);
}
