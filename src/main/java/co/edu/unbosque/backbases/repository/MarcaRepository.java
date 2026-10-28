package co.edu.unbosque.backbases.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import co.edu.unbosque.backbases.entity.Marca;

public interface MarcaRepository extends JpaRepository<Marca, Integer> {

	List<Marca> findAllByOrderByNombreAsc();

	boolean existsByNombre(String nombre);

	boolean existsByNombreAndIdMarcaNot(String nombre, Integer idMarca);

	@Query(value = "SELECT COUNT(*) FROM PRODUCTO WHERE ID_MARCA = :idMarca", nativeQuery = true)
	long contarProductos(@Param("idMarca") Integer idMarca);
}
