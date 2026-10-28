package co.edu.unbosque.backbases.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import co.edu.unbosque.backbases.entity.ClienteNatural;

public interface ClienteNaturalRepository extends JpaRepository<ClienteNatural, Integer> {

	List<ClienteNatural> findByIdClienteIn(Collection<Integer> ids);
}
