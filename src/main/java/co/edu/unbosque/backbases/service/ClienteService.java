package co.edu.unbosque.backbases.service;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.backbases.dto.ClienteDTO;
import co.edu.unbosque.backbases.dto.ClienteEntradaDTO;
import co.edu.unbosque.backbases.dto.TipoCliente;
import co.edu.unbosque.backbases.entity.Cliente;
import co.edu.unbosque.backbases.entity.ClienteJuridico;
import co.edu.unbosque.backbases.entity.ClienteNatural;
import co.edu.unbosque.backbases.exception.ConflictoException;
import co.edu.unbosque.backbases.exception.RecursoNoEncontradoException;
import co.edu.unbosque.backbases.exception.ReglaNegocioException;
import co.edu.unbosque.backbases.repository.ClienteJuridicoRepository;
import co.edu.unbosque.backbases.repository.ClienteNaturalRepository;
import co.edu.unbosque.backbases.repository.ClienteRepository;

@Service
public class ClienteService {

	private final ClienteRepository clienteRepository;
	private final ClienteNaturalRepository naturalRepository;
	private final ClienteJuridicoRepository juridicoRepository;

	public ClienteService(ClienteRepository clienteRepository, ClienteNaturalRepository naturalRepository,
			ClienteJuridicoRepository juridicoRepository) {
		this.clienteRepository = clienteRepository;
		this.naturalRepository = naturalRepository;
		this.juridicoRepository = juridicoRepository;
	}

	@Transactional(readOnly = true)
	public List<ClienteDTO> buscar(String texto, TipoCliente tipo) {
		String filtro = texto == null || texto.isBlank() ? null : texto.trim();
		List<Cliente> clientes = clienteRepository.buscar(filtro, tipo == null ? null : tipo.name());
		Set<Integer> ids = clientes.stream().map(Cliente::getIdCliente).collect(Collectors.toSet());
		Map<Integer, ClienteNatural> naturales = naturalRepository.findByIdClienteIn(ids).stream()
				.collect(Collectors.toMap(ClienteNatural::getIdCliente, Function.identity()));
		Map<Integer, ClienteJuridico> juridicos = juridicoRepository.findByIdClienteIn(ids).stream()
				.collect(Collectors.toMap(ClienteJuridico::getIdCliente, Function.identity()));
		return clientes.stream()
				.map(c -> aDTO(c, naturales.get(c.getIdCliente()), juridicos.get(c.getIdCliente())))
				.sorted(Comparator.comparing(ClienteDTO::nombreMostrado, String.CASE_INSENSITIVE_ORDER))
				.toList();
	}

	@Transactional(readOnly = true)
	public ClienteDTO obtener(Integer idCliente) {
		return aDTO(buscarCliente(idCliente));
	}

	@Transactional
	public ClienteDTO crear(ClienteEntradaDTO entrada) {
		String documento = validarDocumento(entrada, null);
		validarDatosDelTipo(entrada);
		Cliente cliente = new Cliente();
		cliente.setNumeroDocumento(documento);
		copiarContacto(entrada, cliente);
		cliente = clienteRepository.save(cliente);
		return guardarSubtipo(cliente, entrada);
	}

	@Transactional
	public ClienteDTO actualizar(Integer idCliente, ClienteEntradaDTO entrada) {
		Cliente cliente = buscarCliente(idCliente);
		protegerConsumidorFinal(cliente);
		String documento = validarDocumento(entrada, idCliente);
		validarDatosDelTipo(entrada);
		cliente.setNumeroDocumento(documento);
		copiarContacto(entrada, cliente);
		if (entrada.tipo() == TipoCliente.NATURAL) {
			juridicoRepository.findById(idCliente).ifPresent(juridicoRepository::delete);
		} else {
			naturalRepository.findById(idCliente).ifPresent(naturalRepository::delete);
		}
		juridicoRepository.flush();
		naturalRepository.flush();
		return guardarSubtipo(cliente, entrada);
	}

	@Transactional
	public void eliminar(Integer idCliente) {
		Cliente cliente = buscarCliente(idCliente);
		protegerConsumidorFinal(cliente);
		ConflictoException.siEstaEnUso("el cliente", Map.of("ventas", clienteRepository.contarVentas(idCliente)));
		naturalRepository.findById(idCliente).ifPresent(naturalRepository::delete);
		juridicoRepository.findById(idCliente).ifPresent(juridicoRepository::delete);
		clienteRepository.delete(cliente);
	}

	ClienteDTO aDTO(Cliente cliente) {
		return aDTO(cliente, naturalRepository.findById(cliente.getIdCliente()).orElse(null),
				juridicoRepository.findById(cliente.getIdCliente()).orElse(null));
	}

	private ClienteDTO guardarSubtipo(Cliente cliente, ClienteEntradaDTO entrada) {
		if (entrada.tipo() == TipoCliente.NATURAL) {
			ClienteNatural natural = naturalRepository.findById(cliente.getIdCliente()).orElseGet(ClienteNatural::new);
			natural.setCliente(cliente);
			natural.setNombre(entrada.nombre().trim());
			natural.setApellido(entrada.apellido().trim());
			return aDTO(cliente, naturalRepository.save(natural), null);
		}
		ClienteJuridico juridico = juridicoRepository.findById(cliente.getIdCliente()).orElseGet(ClienteJuridico::new);
		juridico.setCliente(cliente);
		juridico.setRazonSocial(entrada.razonSocial().trim());
		return aDTO(cliente, null, juridicoRepository.save(juridico));
	}

	private static ClienteDTO aDTO(Cliente c, ClienteNatural natural, ClienteJuridico juridico) {
		TipoCliente tipo = natural != null ? TipoCliente.NATURAL : juridico != null ? TipoCliente.JURIDICO : null;
		String nombreMostrado = natural != null ? natural.getNombre() + " " + natural.getApellido()
				: juridico != null ? juridico.getRazonSocial() : c.getNumeroDocumento();
		return new ClienteDTO(c.getIdCliente(), tipo, c.getNumeroDocumento(), c.getDireccion(),
				List.copyOf(c.getTelefonos()), List.copyOf(c.getCorreos()),
				natural == null ? null : natural.getNombre(),
				natural == null ? null : natural.getApellido(),
				juridico == null ? null : juridico.getRazonSocial(),
				nombreMostrado, c.esConsumidorFinal());
	}

	private String validarDocumento(ClienteEntradaDTO entrada, Integer idCliente) {
		String documento = entrada.numeroDocumento().trim();
		if (Cliente.DOCUMENTO_CONSUMIDOR_FINAL.equals(documento)) {
			throw new ReglaNegocioException("El documento " + Cliente.DOCUMENTO_CONSUMIDOR_FINAL
					+ " está reservado para el consumidor final. Si el cliente no quiere dar sus datos, registre la venta a consumidor final.");
		}
		boolean repetido = idCliente == null ? clienteRepository.existsByNumeroDocumento(documento)
				: clienteRepository.existsByNumeroDocumentoAndIdClienteNot(documento, idCliente);
		if (repetido) {
			throw new ConflictoException("Ya existe un cliente con el documento " + documento + ".");
		}
		return documento;
	}

	private static void validarDatosDelTipo(ClienteEntradaDTO entrada) {
		if (entrada.tipo() == TipoCliente.NATURAL && (vacio(entrada.nombre()) || vacio(entrada.apellido()))) {
			throw new ReglaNegocioException("Un cliente natural necesita nombre y apellido.");
		}
		if (entrada.tipo() == TipoCliente.JURIDICO && vacio(entrada.razonSocial())) {
			throw new ReglaNegocioException("Un cliente jurídico necesita razón social.");
		}
	}

	private static void copiarContacto(ClienteEntradaDTO entrada, Cliente cliente) {
		cliente.setDireccion(vacio(entrada.direccionCliente()) ? null : entrada.direccionCliente().trim());
		reemplazar(cliente.getTelefonos(), entrada.telefonos(), false);
		reemplazar(cliente.getCorreos(), entrada.correos(), true);
	}

	private static void reemplazar(Set<String> actuales, List<String> nuevos, boolean minusculas) {
		actuales.clear();
		if (nuevos != null) {
			actuales.addAll(nuevos.stream().map(String::trim).map(v -> minusculas ? v.toLowerCase() : v)
					.collect(Collectors.toCollection(LinkedHashSet::new)));
		}
	}

	private void protegerConsumidorFinal(Cliente cliente) {
		if (cliente.esConsumidorFinal()) {
			throw new ReglaNegocioException("El consumidor final es un registro del sistema y no se puede modificar.");
		}
	}

	private Cliente buscarCliente(Integer idCliente) {
		return clienteRepository.findWithContactoByIdCliente(idCliente)
				.orElseThrow(() -> RecursoNoEncontradoException.de("el cliente", idCliente));
	}

	private static boolean vacio(String texto) {
		return texto == null || texto.isBlank();
	}
}
