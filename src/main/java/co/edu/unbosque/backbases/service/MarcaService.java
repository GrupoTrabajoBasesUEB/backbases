package co.edu.unbosque.backbases.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.backbases.dto.MarcaDTO;
import co.edu.unbosque.backbases.dto.MarcaEntradaDTO;
import co.edu.unbosque.backbases.entity.Marca;
import co.edu.unbosque.backbases.exception.ConflictoException;
import co.edu.unbosque.backbases.exception.RecursoNoEncontradoException;
import co.edu.unbosque.backbases.repository.MarcaRepository;

@Service
public class MarcaService {

	private final MarcaRepository marcaRepository;

	public MarcaService(MarcaRepository marcaRepository) {
		this.marcaRepository = marcaRepository;
	}

	@Transactional(readOnly = true)
	public List<MarcaDTO> listar() {
		return marcaRepository.findAllByOrderByNombreAsc().stream().map(MarcaDTO::de).toList();
	}

	@Transactional
	public MarcaDTO crear(MarcaEntradaDTO entrada) {
		String nombre = entrada.nombreMarca().trim();
		if (marcaRepository.existsByNombre(nombre)) {
			throw yaRegistrada(nombre);
		}
		Marca marca = new Marca();
		copiar(entrada, marca);
		return MarcaDTO.de(marcaRepository.save(marca));
	}

	@Transactional
	public MarcaDTO actualizar(Integer idMarca, MarcaEntradaDTO entrada) {
		Marca marca = buscar(idMarca);
		String nombre = entrada.nombreMarca().trim();
		if (marcaRepository.existsByNombreAndIdMarcaNot(nombre, idMarca)) {
			throw yaRegistrada(nombre);
		}
		copiar(entrada, marca);
		return MarcaDTO.de(marca);
	}

	@Transactional
	public void eliminar(Integer idMarca) {
		Marca marca = buscar(idMarca);
		ConflictoException.siEstaEnUso("la marca", Map.of("productos", marcaRepository.contarProductos(idMarca)));
		marcaRepository.delete(marca);
	}

	private Marca buscar(Integer idMarca) {
		return marcaRepository.findById(idMarca).orElseThrow(() -> RecursoNoEncontradoException.de("la marca", idMarca));
	}

	private static void copiar(MarcaEntradaDTO entrada, Marca marca) {
		marca.setNombre(entrada.nombreMarca().trim());
		marca.setPaisOrigen(entrada.paisOrigen().trim());
	}

	private static ConflictoException yaRegistrada(String nombre) {
		return new ConflictoException("La marca \"" + nombre + "\" ya está registrada.");
	}
}
