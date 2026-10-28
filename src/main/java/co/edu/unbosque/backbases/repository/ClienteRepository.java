package co.edu.unbosque.backbases.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import co.edu.unbosque.backbases.entity.Cliente;

public interface ClienteRepository extends JpaRepository<Cliente, Integer> {

	@EntityGraph(attributePaths = { "telefonos", "correos" })
	@Query("""
			select distinct c from Cliente c
			left join ClienteNatural n on n.idCliente = c.idCliente
			left join ClienteJuridico j on j.idCliente = c.idCliente
			where (:texto is null
			       or c.numeroDocumento like concat('%', :texto, '%')
			       or concat(n.nombre, ' ', n.apellido) like concat('%', :texto, '%')
			       or j.razonSocial like concat('%', :texto, '%'))
			  and (:tipo is null
			       or (:tipo = 'NATURAL' and n.idCliente is not null)
			       or (:tipo = 'JURIDICO' and j.idCliente is not null))
			""")
	List<Cliente> buscar(@Param("texto") String texto, @Param("tipo") String tipo);

	@EntityGraph(attributePaths = { "telefonos", "correos" })
	Optional<Cliente> findWithContactoByIdCliente(Integer idCliente);

	@EntityGraph(attributePaths = { "telefonos", "correos" })
	Optional<Cliente> findByNumeroDocumento(String numeroDocumento);

	boolean existsByNumeroDocumento(String numeroDocumento);

	boolean existsByNumeroDocumentoAndIdClienteNot(String numeroDocumento, Integer idCliente);

	@Query(value = "SELECT COUNT(*) FROM VENTA WHERE ID_CLIENTE = :idCliente", nativeQuery = true)
	long contarVentas(@Param("idCliente") Integer idCliente);
}
