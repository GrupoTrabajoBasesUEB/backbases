package co.edu.unbosque.backbases.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import co.edu.unbosque.backbases.entity.Administrador;

public interface AdministradorRepository extends JpaRepository<Administrador, Integer> {
}
