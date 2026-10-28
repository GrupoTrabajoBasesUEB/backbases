package co.edu.unbosque.backbases.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import co.edu.unbosque.backbases.entity.Proveedor;

public interface ProveedorRepository extends JpaRepository<Proveedor, String> {

	@EntityGraph(attributePaths = { "telefonos", "correos" })
	@Query("""
			select distinct p from Proveedor p
			where :texto is null or p.nit like concat('%', :texto, '%') or p.nombre like concat('%', :texto, '%')
			order by p.nombre
			""")
	List<Proveedor> buscar(@Param("texto") String texto);

	@EntityGraph(attributePaths = { "telefonos", "correos" })
	Optional<Proveedor> findWithContactoByNit(String nit);

	@Query(value = "SELECT COUNT(*) FROM SUMINISTRO WHERE NIT = :nit", nativeQuery = true)
	long contarSuministros(@Param("nit") String nit);
}
