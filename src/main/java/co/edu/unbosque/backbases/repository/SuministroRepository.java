package co.edu.unbosque.backbases.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import co.edu.unbosque.backbases.entity.Suministro;
import co.edu.unbosque.backbases.entity.SuministroId;

public interface SuministroRepository extends JpaRepository<Suministro, SuministroId> {

	interface ResumenEvaluaciones {
		String getNit();

		Integer getIdProducto();

		Integer getIdSucursal();

		Long getCantidad();

		BigDecimal getPromedio();

		LocalDate getUltima();
	}

	@Query("""
			select s from Suministro s join fetch s.proveedor pr join fetch s.producto p join fetch s.sucursal su
			where (:nit is null or pr.nit = :nit)
			  and (:idProducto is null or p.idProducto = :idProducto)
			  and (:idSucursal is null or su.idSucursal = :idSucursal)
			order by pr.nombre, p.nombre, su.nombre
			""")
	List<Suministro> buscar(@Param("nit") String nit, @Param("idProducto") Integer idProducto, @Param("idSucursal") Integer idSucursal);

	@Query("select s from Suministro s join fetch s.proveedor join fetch s.producto join fetch s.sucursal where s.id = :id")
	Optional<Suministro> buscarCompleto(@Param("id") SuministroId id);

	@Query(value = """
			SELECT NIT AS nit, ID_PRODUCTO AS idProducto, ID_SUCURSAL AS idSucursal, COUNT(*) AS cantidad,
			       ROUND(AVG(CALIFICACION), 2) AS promedio, MAX(FECHA_EVALUACION) AS ultima
			FROM EVALUACION
			GROUP BY NIT, ID_PRODUCTO, ID_SUCURSAL
			""", nativeQuery = true)
	List<ResumenEvaluaciones> resumenEvaluaciones();

	@Query(value = "SELECT COUNT(*) FROM EVALUACION WHERE NIT = :nit AND ID_PRODUCTO = :idProducto AND ID_SUCURSAL = :idSucursal", nativeQuery = true)
	long contarEvaluaciones(@Param("nit") String nit, @Param("idProducto") Integer idProducto, @Param("idSucursal") Integer idSucursal);
}
