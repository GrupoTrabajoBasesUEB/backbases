package co.edu.unbosque.backbases.service;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.backbases.dto.ProveedorDTO;
import co.edu.unbosque.backbases.dto.ProveedorEntradaDTO;
import co.edu.unbosque.backbases.entity.Proveedor;
import co.edu.unbosque.backbases.exception.ConflictoException;
import co.edu.unbosque.backbases.exception.RecursoNoEncontradoException;
import co.edu.unbosque.backbases.exception.ReglaNegocioException;
import co.edu.unbosque.backbases.repository.ProveedorRepository;

@Service
public class ProveedorService {

	private final ProveedorRepository proveedorRepository;

	public ProveedorService(ProveedorRepository proveedorRepository) {
		this.proveedorRepository = proveedorRepository;
	}

	@Transactional(readOnly = true)
	public List<ProveedorDTO> buscar(String texto) {
		return proveedorRepository.buscar(texto == null || texto.isBlank() ? null : texto.trim()).stream().map(ProveedorDTO::de).toList();
	}

	@Transactional(readOnly = true)
	public ProveedorDTO obtener(String nit) {
		return ProveedorDTO.de(buscarProveedor(nit));
	}

	@Transactional
	public ProveedorDTO crear(ProveedorEntradaDTO entrada) {
		String nit = entrada.nit().trim();
		if (proveedorRepository.existsById(nit)) {
			throw new ConflictoException("Ya hay un proveedor con el NIT " + nit + ".");
		}
		Proveedor proveedor = new Proveedor();
		proveedor.setNit(nit);
		copiar(entrada, proveedor);
		return ProveedorDTO.de(proveedorRepository.save(proveedor));
	}

	@Transactional
	public ProveedorDTO actualizar(String nit, ProveedorEntradaDTO entrada) {
		Proveedor proveedor = buscarProveedor(nit);
		if (!proveedor.getNit().equals(entrada.nit().trim())) {
			throw new ReglaNegocioException("El NIT es la llave del proveedor y no se puede cambiar.");
		}
		copiar(entrada, proveedor);
		return ProveedorDTO.de(proveedor);
	}

	@Transactional
	public void eliminar(String nit) {
		Proveedor proveedor = buscarProveedor(nit);
		ConflictoException.siEstaEnUso("el proveedor", Map.of("suministros", proveedorRepository.contarSuministros(proveedor.getNit())));
		proveedorRepository.delete(proveedor);
	}

	private static void copiar(ProveedorEntradaDTO entrada, Proveedor proveedor) {
		proveedor.setNombre(entrada.nombreProveedor().trim());
		proveedor.setDireccion(entrada.direccionProveedor().trim());
		reemplazar(proveedor.getTelefonos(), entrada.telefonos(), false);
		reemplazar(proveedor.getCorreos(), entrada.correos(), true);
	}

	private static void reemplazar(Set<String> actuales, List<String> nuevos, boolean minusculas) {
		actuales.clear();
		actuales.addAll(nuevos.stream().map(String::trim).map(v -> minusculas ? v.toLowerCase() : v)
				.collect(Collectors.toCollection(LinkedHashSet::new)));
	}

	private Proveedor buscarProveedor(String nit) {
		return proveedorRepository.findWithContactoByNit(nit.trim())
				.orElseThrow(() -> new RecursoNoEncontradoException("No existe el proveedor con NIT " + nit + "."));
	}
}
