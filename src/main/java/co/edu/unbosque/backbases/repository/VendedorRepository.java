package co.edu.unbosque.backbases.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import co.edu.unbosque.backbases.entity.Vendedor;

public interface VendedorRepository extends JpaRepository<Vendedor, Integer> {

	@Query("select v from Vendedor v join fetch v.sucursal where v.idEmpleado = :idEmpleado")
	Optional<Vendedor> buscarConSucursal(@Param("idEmpleado") Integer idEmpleado);

	@Query("select v from Vendedor v join fetch v.sucursal")
	List<Vendedor> listarConSucursal();
}
