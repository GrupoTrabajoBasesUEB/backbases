package co.edu.unbosque.backbases.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import co.edu.unbosque.backbases.entity.MetodoPago;

public interface MetodoPagoRepository extends JpaRepository<MetodoPago, Integer> {

	List<MetodoPago> findAllByOrderByTipoAsc();

	boolean existsByTipo(String tipo);

	boolean existsByTipoAndIdMetodoPagoNot(String tipo, Integer idMetodoPago);

	@Query(value = "SELECT COUNT(*) FROM VENTA WHERE ID_METODO_PAGO = :idMetodoPago", nativeQuery = true)
	long contarVentas(@Param("idMetodoPago") Integer idMetodoPago);
}
