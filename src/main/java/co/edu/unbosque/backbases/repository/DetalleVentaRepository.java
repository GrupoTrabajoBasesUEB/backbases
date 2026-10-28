package co.edu.unbosque.backbases.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import co.edu.unbosque.backbases.entity.DetalleVenta;
import co.edu.unbosque.backbases.entity.DetalleVentaId;

public interface DetalleVentaRepository extends JpaRepository<DetalleVenta, DetalleVentaId> {

	@Query("select d from DetalleVenta d join fetch d.producto p where d.venta.idVenta = :idVenta order by p.nombre")
	List<DetalleVenta> deVenta(@Param("idVenta") Integer idVenta);
}
