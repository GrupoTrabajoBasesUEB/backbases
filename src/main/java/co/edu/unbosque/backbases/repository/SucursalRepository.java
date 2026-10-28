package co.edu.unbosque.backbases.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import co.edu.unbosque.backbases.entity.Sucursal;

public interface SucursalRepository extends JpaRepository<Sucursal, Integer> {

	Optional<Sucursal> findByAdministradorIdEmpleado(Integer idEmpleado);
}
