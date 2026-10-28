package co.edu.unbosque.backbases.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import co.edu.unbosque.backbases.entity.Categoria;

public interface CategoriaRepository extends JpaRepository<Categoria, Integer> {

	@Query("select c from Categoria c left join fetch c.categoriaPrincipal order by c.nombre")
	List<Categoria> listarConPrincipal();

	@Query("select c from Categoria c where c.categoriaPrincipal is null order by c.nombre")
	List<Categoria> listarPrincipales();

	@Query("""
			select count(c) > 0 from Categoria c left join c.categoriaPrincipal p
			where c.nombre = :nombre
			  and ((:idPrincipal is null and p is null) or p.idCategoria = :idPrincipal)
			  and (:idExcluir is null or c.idCategoria <> :idExcluir)
			""")
	boolean existeNombreEnNivel(@Param("nombre") String nombre, @Param("idPrincipal") Integer idPrincipal,
			@Param("idExcluir") Integer idExcluir);

	long countByCategoriaPrincipalIdCategoria(Integer idCategoria);

	@Query(value = "SELECT COUNT(*) FROM PRODUCTO WHERE ID_CATEGORIA = :idCategoria", nativeQuery = true)
	long contarProductos(@Param("idCategoria") Integer idCategoria);
}
