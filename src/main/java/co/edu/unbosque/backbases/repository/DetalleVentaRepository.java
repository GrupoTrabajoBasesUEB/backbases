package co.edu.unbosque.backbases.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import co.edu.unbosque.backbases.entity.DetalleVenta;
import co.edu.unbosque.backbases.entity.DetalleVentaId;

public interface DetalleVentaRepository extends JpaRepository<DetalleVenta, DetalleVentaId> {

	@Query("select d from DetalleVenta d join fetch d.producto p where d.venta.idVenta = :idVenta order by p.nombre")
	List<DetalleVenta> deVenta(@Param("idVenta") Integer idVenta);

	@Query("""
			select d from DetalleVenta d join fetch d.venta v join fetch d.producto
			where (:desde is null or v.fecha >= :desde)
			  and (:hasta is null or v.fecha <= :hasta)
			  and (:idSucursal is null or v.sucursal.idSucursal = :idSucursal)
			  and (:idEmpleado is null or v.vendedor.idEmpleado = :idEmpleado)
			  and (:idCliente is null or v.cliente.idCliente = :idCliente)
			  and (:idMetodoPago is null or v.metodoPago.idMetodoPago = :idMetodoPago)
			order by v.fecha desc, v.hora desc, v.idVenta desc
			""")
	List<DetalleVenta> lineas(@Param("desde") LocalDate desde, @Param("hasta") LocalDate hasta, @Param("idSucursal") Integer idSucursal,
			@Param("idEmpleado") Integer idEmpleado, @Param("idCliente") Integer idCliente, @Param("idMetodoPago") Integer idMetodoPago);
}
