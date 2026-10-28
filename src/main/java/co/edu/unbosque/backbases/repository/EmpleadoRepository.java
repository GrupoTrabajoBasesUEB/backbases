package co.edu.unbosque.backbases.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import co.edu.unbosque.backbases.entity.Empleado;

public interface EmpleadoRepository extends JpaRepository<Empleado, Integer> {

	@EntityGraph(attributePaths = "telefonos")
	Optional<Empleado> findByCorreo(String correo);

	@EntityGraph(attributePaths = "telefonos")
	Optional<Empleado> findWithTelefonosByIdEmpleado(Integer idEmpleado);

	@EntityGraph(attributePaths = "telefonos")
	@Query("select e from Empleado e order by e.nombre, e.apellido")
	List<Empleado> listarConTelefonos();

	boolean existsByDocumento(String documento);

	boolean existsByDocumentoAndIdEmpleadoNot(String documento, Integer idEmpleado);

	boolean existsByCorreo(String correo);

	boolean existsByCorreoAndIdEmpleadoNot(String correo, Integer idEmpleado);

	@Query(value = "SELECT COUNT(*) FROM VENTA WHERE ID_EMPLEADO = :idEmpleado", nativeQuery = true)
	long contarVentas(@Param("idEmpleado") Integer idEmpleado);

	@Query(value = "SELECT COUNT(*) FROM EVALUACION WHERE ID_EMPLEADO = :idEmpleado", nativeQuery = true)
	long contarEvaluaciones(@Param("idEmpleado") Integer idEmpleado);
}
