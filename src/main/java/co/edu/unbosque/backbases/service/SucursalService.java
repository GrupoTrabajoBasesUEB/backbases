package co.edu.unbosque.backbases.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.backbases.dto.SucursalDTO;
import co.edu.unbosque.backbases.dto.SucursalEntradaDTO;
import co.edu.unbosque.backbases.entity.Administrador;
import co.edu.unbosque.backbases.entity.Sucursal;
import co.edu.unbosque.backbases.exception.ConflictoException;
import co.edu.unbosque.backbases.exception.RecursoNoEncontradoException;
import co.edu.unbosque.backbases.exception.ReglaNegocioException;
import co.edu.unbosque.backbases.repository.AdministradorRepository;
import co.edu.unbosque.backbases.repository.SucursalRepository;

@Service
public class SucursalService {

	private final SucursalRepository sucursalRepository;
	private final AdministradorRepository administradorRepository;

	public SucursalService(SucursalRepository sucursalRepository, AdministradorRepository administradorRepository) {
		this.sucursalRepository = sucursalRepository;
		this.administradorRepository = administradorRepository;
	}

	@Transactional(readOnly = true)
	public List<SucursalDTO> listar() {
		return sucursalRepository.listarConEncargado().stream().map(SucursalDTO::de).toList();
	}

	@Transactional(readOnly = true)
	public SucursalDTO obtener(Integer idSucursal) {
		return SucursalDTO.de(buscar(idSucursal));
	}

	@Transactional
	public SucursalDTO crear(SucursalEntradaDTO entrada) {
		String nombre = entrada.nombreSucursal().trim();
		if (sucursalRepository.existsByNombre(nombre)) {
			throw yaExiste(nombre);
		}
		Sucursal sucursal = new Sucursal();
		aplicar(entrada, sucursal);
		return SucursalDTO.de(sucursalRepository.save(sucursal));
	}

	@Transactional
	public SucursalDTO actualizar(Integer idSucursal, SucursalEntradaDTO entrada) {
		Sucursal sucursal = buscar(idSucursal);
		String nombre = entrada.nombreSucursal().trim();
		if (sucursalRepository.existsByNombreAndIdSucursalNot(nombre, idSucursal)) {
			throw yaExiste(nombre);
		}
		aplicar(entrada, sucursal);
		return SucursalDTO.de(sucursal);
	}

	@Transactional
	public void eliminar(Integer idSucursal) {
		Sucursal sucursal = buscar(idSucursal);
		Map<String, Long> referencias = new LinkedHashMap<>();
		referencias.put("ventas", sucursalRepository.contarVentas(idSucursal));
		referencias.put("productos en inventario", sucursalRepository.contarInventario(idSucursal));
		referencias.put("suministros", sucursalRepository.contarSuministros(idSucursal));
		referencias.put("vendedores", sucursalRepository.contarVendedores(idSucursal));
		ConflictoException.siEstaEnUso("la sucursal", referencias);
		sucursalRepository.delete(sucursal);
	}

	private void aplicar(SucursalEntradaDTO entrada, Sucursal sucursal) {
		Administrador encargado = administradorRepository.findById(entrada.idAdministrador())
				.orElseThrow(() -> new ReglaNegocioException("El encargado debe ser un administrador registrado."));
		Optional<Sucursal> dirigida = sucursalRepository.findByAdministradorIdEmpleado(entrada.idAdministrador());
		if (dirigida.isPresent() && !dirigida.get().getIdSucursal().equals(sucursal.getIdSucursal())) {
			throw new ConflictoException("Ese administrador ya dirige la sucursal \"" + dirigida.get().getNombre() + "\".");
		}
		sucursal.setNombre(entrada.nombreSucursal().trim());
		sucursal.setCiudad(entrada.ciudad().trim());
		sucursal.setDireccion(entrada.direccion().trim());
		sucursal.setTelefono(entrada.telefono().trim());
		sucursal.setAdministrador(encargado);
	}

	private Sucursal buscar(Integer idSucursal) {
		return sucursalRepository.buscarConEncargado(idSucursal)
				.orElseThrow(() -> RecursoNoEncontradoException.de("la sucursal", idSucursal));
	}

	private static ConflictoException yaExiste(String nombre) {
		return new ConflictoException("Ya existe una sucursal llamada \"" + nombre + "\".");
	}
}
