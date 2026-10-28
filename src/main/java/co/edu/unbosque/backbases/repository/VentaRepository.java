package co.edu.unbosque.backbases.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import co.edu.unbosque.backbases.entity.Venta;

public interface VentaRepository extends JpaRepository<Venta, Integer> {

	interface TotalVenta {
		Integer getIdVenta();

		BigDecimal getMontoTotal();
	}

	String FILTROS = """
			where (:desde is null or v.fecha >= :desde)
			  and (:hasta is null or v.fecha <= :hasta)
			  and (:idSucursal is null or v.sucursal.idSucursal = :idSucursal)
			  and (:idEmpleado is null or v.vendedor.idEmpleado = :idEmpleado)
			  and (:idCliente is null or v.cliente.idCliente = :idCliente)
			  and (:idMetodoPago is null or v.metodoPago.idMetodoPago = :idMetodoPago)
			  and (:factura is null or v.numeroFactura like concat('%', :factura, '%'))
			""";

	@Query(value = """
			select v from Venta v join fetch v.cliente join fetch v.vendedor ve join fetch ve.empleado
			join fetch v.sucursal join fetch v.metodoPago
			""" + FILTROS + " order by v.fecha desc, v.hora desc, v.idVenta desc",
			countQuery = "select count(v) from Venta v " + FILTROS)
	Page<Venta> buscar(@Param("desde") LocalDate desde, @Param("hasta") LocalDate hasta, @Param("idSucursal") Integer idSucursal,
			@Param("idEmpleado") Integer idEmpleado, @Param("idCliente") Integer idCliente, @Param("idMetodoPago") Integer idMetodoPago,
			@Param("factura") String factura, Pageable pagina);

	@Query("""
			select v from Venta v join fetch v.cliente join fetch v.vendedor ve join fetch ve.empleado
			join fetch v.sucursal join fetch v.metodoPago where v.idVenta = :idVenta
			""")
	Optional<Venta> buscarCompleta(@Param("idVenta") Integer idVenta);

	@Query(value = "SELECT ID_VENTA AS idVenta, MONTO_TOTAL AS montoTotal FROM V_VENTA_TOTAL WHERE ID_VENTA IN (:ids)", nativeQuery = true)
	List<TotalVenta> totales(@Param("ids") Collection<Integer> ids);
}
