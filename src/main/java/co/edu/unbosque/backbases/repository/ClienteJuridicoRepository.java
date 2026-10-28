package co.edu.unbosque.backbases.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import co.edu.unbosque.backbases.entity.ClienteJuridico;

public interface ClienteJuridicoRepository extends JpaRepository<ClienteJuridico, Integer> {

	List<ClienteJuridico> findByIdClienteIn(Collection<Integer> ids);
}
