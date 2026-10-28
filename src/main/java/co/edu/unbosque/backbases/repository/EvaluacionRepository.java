package co.edu.unbosque.backbases.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import co.edu.unbosque.backbases.entity.Evaluacion;

public interface EvaluacionRepository extends JpaRepository<Evaluacion, Integer> {

	String CONSULTA = """
			select e from Evaluacion e join fetch e.suministro s join fetch s.proveedor pr join fetch s.producto p
			join fetch s.sucursal su join fetch e.administrador a join fetch a.empleado em
			""";

	@Query(CONSULTA + """
			where (:nit is null or pr.nit = :nit)
			  and (:idProducto is null or p.idProducto = :idProducto)
			  and (:idSucursal is null or su.idSucursal = :idSucursal)
			  and (:idEmpleado is null or a.idEmpleado = :idEmpleado)
			  and (:desde is null or e.fechaEvaluacion >= :desde)
			  and (:hasta is null or e.fechaEvaluacion <= :hasta)
			order by e.fechaEvaluacion desc, e.idEvaluacion desc
			""")
	List<Evaluacion> buscar(@Param("nit") String nit, @Param("idProducto") Integer idProducto, @Param("idSucursal") Integer idSucursal,
			@Param("idEmpleado") Integer idEmpleado, @Param("desde") LocalDate desde, @Param("hasta") LocalDate hasta);

	@Query(CONSULTA + " where e.idEvaluacion = :idEvaluacion")
	Optional<Evaluacion> buscarCompleta(@Param("idEvaluacion") Integer idEvaluacion);
}
