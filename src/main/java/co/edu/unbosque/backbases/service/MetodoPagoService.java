package co.edu.unbosque.backbases.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.backbases.dto.MetodoPagoDTO;
import co.edu.unbosque.backbases.dto.MetodoPagoEntradaDTO;
import co.edu.unbosque.backbases.entity.MetodoPago;
import co.edu.unbosque.backbases.exception.ConflictoException;
import co.edu.unbosque.backbases.exception.RecursoNoEncontradoException;
import co.edu.unbosque.backbases.repository.MetodoPagoRepository;

@Service
public class MetodoPagoService {

	private final MetodoPagoRepository metodoPagoRepository;

	public MetodoPagoService(MetodoPagoRepository metodoPagoRepository) {
		this.metodoPagoRepository = metodoPagoRepository;
	}

	@Transactional(readOnly = true)
	public List<MetodoPagoDTO> listar() {
		return metodoPagoRepository.findAllByOrderByTipoAsc().stream().map(MetodoPagoDTO::de).toList();
	}

	@Transactional
	public MetodoPagoDTO crear(MetodoPagoEntradaDTO entrada) {
		String tipo = entrada.tipo().trim();
		if (metodoPagoRepository.existsByTipo(tipo)) {
			throw yaExiste(tipo);
		}
		MetodoPago metodo = new MetodoPago();
		metodo.setTipo(tipo);
		return MetodoPagoDTO.de(metodoPagoRepository.save(metodo));
	}

	@Transactional
	public MetodoPagoDTO actualizar(Integer idMetodoPago, MetodoPagoEntradaDTO entrada) {
		MetodoPago metodo = buscar(idMetodoPago);
		String tipo = entrada.tipo().trim();
		if (metodoPagoRepository.existsByTipoAndIdMetodoPagoNot(tipo, idMetodoPago)) {
			throw yaExiste(tipo);
		}
		metodo.setTipo(tipo);
		return MetodoPagoDTO.de(metodo);
	}

	@Transactional
	public void eliminar(Integer idMetodoPago) {
		MetodoPago metodo = buscar(idMetodoPago);
		ConflictoException.siEstaEnUso("el método de pago", Map.of("ventas", metodoPagoRepository.contarVentas(idMetodoPago)));
		metodoPagoRepository.delete(metodo);
	}

	private MetodoPago buscar(Integer idMetodoPago) {
		return metodoPagoRepository.findById(idMetodoPago)
				.orElseThrow(() -> RecursoNoEncontradoException.de("el método de pago", idMetodoPago));
	}

	private static ConflictoException yaExiste(String tipo) {
		return new ConflictoException("El método de pago \"" + tipo + "\" ya existe.");
	}
}
