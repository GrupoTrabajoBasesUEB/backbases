package co.edu.unbosque.backbases.service;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.backbases.dto.EmpleadoDTO;
import co.edu.unbosque.backbases.dto.EmpleadoEntradaDTO;
import co.edu.unbosque.backbases.dto.Rol;
import co.edu.unbosque.backbases.entity.Administrador;
import co.edu.unbosque.backbases.entity.Empleado;
import co.edu.unbosque.backbases.entity.Sucursal;
import co.edu.unbosque.backbases.entity.Vendedor;
import co.edu.unbosque.backbases.exception.ConflictoException;
import co.edu.unbosque.backbases.exception.RecursoNoEncontradoException;
import co.edu.unbosque.backbases.exception.ReglaNegocioException;
import co.edu.unbosque.backbases.repository.AdministradorRepository;
import co.edu.unbosque.backbases.repository.EmpleadoRepository;
import co.edu.unbosque.backbases.repository.SucursalRepository;
import co.edu.unbosque.backbases.repository.VendedorRepository;

@Service
public class EmpleadoService {

	private static final BigDecimal COMISION_POR_DEFECTO = new BigDecimal("0.100");

	private final EmpleadoRepository empleadoRepository;
	private final AdministradorRepository administradorRepository;
	private final VendedorRepository vendedorRepository;
	private final SucursalRepository sucursalRepository;
	private final PasswordEncoder passwordEncoder;

	public EmpleadoService(EmpleadoRepository empleadoRepository, AdministradorRepository administradorRepository,
			VendedorRepository vendedorRepository, SucursalRepository sucursalRepository, PasswordEncoder passwordEncoder) {
		this.empleadoRepository = empleadoRepository;
		this.administradorRepository = administradorRepository;
		this.vendedorRepository = vendedorRepository;
		this.sucursalRepository = sucursalRepository;
		this.passwordEncoder = passwordEncoder;
	}

	@Transactional(readOnly = true)
	public List<EmpleadoDTO> listar(Rol rol) {
		Map<Integer, Administrador> administradores = administradorRepository.findAll().stream()
				.collect(Collectors.toMap(Administrador::getIdEmpleado, Function.identity()));
		Map<Integer, Vendedor> vendedores = vendedorRepository.listarConSucursal().stream()
				.collect(Collectors.toMap(Vendedor::getIdEmpleado, Function.identity()));
		return empleadoRepository.listarConTelefonos().stream()
				.map(e -> aDTO(e, administradores.get(e.getIdEmpleado()), vendedores.get(e.getIdEmpleado())))
				.filter(e -> rol == null || e.rol() == rol)
				.toList();
	}

	@Transactional(readOnly = true)
	public EmpleadoDTO obtener(Integer idEmpleado) {
		return aDTO(buscar(idEmpleado));
	}

	@Transactional(readOnly = true)
	public EmpleadoDTO obtenerPorCorreo(String correo) {
		return aDTO(empleadoRepository.findByCorreo(correo)
				.orElseThrow(() -> new RecursoNoEncontradoException("No existe un empleado con ese correo.")));
	}

	@Transactional
	public EmpleadoDTO crear(EmpleadoEntradaDTO entrada) {
		validarUnicos(entrada, null);
		if (entrada.contrasena() == null) {
			throw new ReglaNegocioException("La contraseña es obligatoria al crear un empleado.");
		}
		Empleado empleado = new Empleado();
		copiarDatos(entrada, empleado);
		empleado.setContrasena(passwordEncoder.encode(entrada.contrasena()));
		empleado = empleadoRepository.save(empleado);

		if (entrada.rol() == Rol.ADMINISTRADOR) {
			if (entrada.nivelAcceso() == null || entrada.claveSeguridad() == null) {
				throw new ReglaNegocioException("Un administrador necesita nivel de acceso y clave de seguridad.");
			}
			Administrador administrador = new Administrador();
			administrador.setEmpleado(empleado);
			administrador.setNivelAcceso(entrada.nivelAcceso());
			administrador.setClaveSeguridad(passwordEncoder.encode(entrada.claveSeguridad()));
			return aDTO(empleado, administradorRepository.save(administrador), null);
		}
		if (entrada.idSucursal() == null) {
			throw new ReglaNegocioException("Un vendedor debe estar asignado a una sucursal.");
		}
		Vendedor vendedor = new Vendedor();
		vendedor.setEmpleado(empleado);
		vendedor.setSucursal(buscarSucursal(entrada.idSucursal()));
		vendedor.setComisionVenta(entrada.comisionVenta() == null ? COMISION_POR_DEFECTO : entrada.comisionVenta());
		return aDTO(empleado, null, vendedorRepository.save(vendedor));
	}

	@Transactional
	public EmpleadoDTO actualizar(Integer idEmpleado, EmpleadoEntradaDTO entrada) {
		Empleado empleado = buscar(idEmpleado);
		Administrador administrador = administradorRepository.findById(idEmpleado).orElse(null);
		Vendedor vendedor = administrador == null ? vendedorRepository.buscarConSucursal(idEmpleado).orElse(null) : null;
		Rol rolActual = administrador != null ? Rol.ADMINISTRADOR : Rol.VENDEDOR;
		if (entrada.rol() != rolActual) {
			throw new ReglaNegocioException("El rol de un empleado no se puede cambiar.");
		}
		validarUnicos(entrada, idEmpleado);
		copiarDatos(entrada, empleado);
		if (entrada.contrasena() != null) {
			empleado.setContrasena(passwordEncoder.encode(entrada.contrasena()));
		}
		if (administrador != null) {
			if (entrada.nivelAcceso() != null) {
				administrador.setNivelAcceso(entrada.nivelAcceso());
			}
			if (entrada.claveSeguridad() != null) {
				administrador.setClaveSeguridad(passwordEncoder.encode(entrada.claveSeguridad()));
			}
		} else if (vendedor != null) {
			if (entrada.idSucursal() != null) {
				vendedor.setSucursal(buscarSucursal(entrada.idSucursal()));
			}
			if (entrada.comisionVenta() != null) {
				vendedor.setComisionVenta(entrada.comisionVenta());
			}
		}
		return aDTO(empleado, administrador, vendedor);
	}

	@Transactional
	public void eliminar(Integer idEmpleado, Integer idEmpleadoActual) {
		Empleado empleado = buscar(idEmpleado);
		if (idEmpleado.equals(idEmpleadoActual)) {
			throw new ReglaNegocioException("No puede eliminar su propio usuario.");
		}
		Map<String, Long> referencias = new LinkedHashMap<>();
		referencias.put("ventas", empleadoRepository.contarVentas(idEmpleado));
		referencias.put("evaluaciones", empleadoRepository.contarEvaluaciones(idEmpleado));
		referencias.put("sucursales que dirige", sucursalRepository.findByAdministradorIdEmpleado(idEmpleado).isPresent() ? 1L : 0L);
		ConflictoException.siEstaEnUso("el empleado", referencias);
		administradorRepository.findById(idEmpleado).ifPresent(administradorRepository::delete);
		vendedorRepository.findById(idEmpleado).ifPresent(vendedorRepository::delete);
		empleadoRepository.delete(empleado);
	}

	EmpleadoDTO aDTO(Empleado empleado) {
		Administrador administrador = administradorRepository.findById(empleado.getIdEmpleado()).orElse(null);
		Vendedor vendedor = administrador == null ? vendedorRepository.buscarConSucursal(empleado.getIdEmpleado()).orElse(null) : null;
		return aDTO(empleado, administrador, vendedor);
	}

	private static EmpleadoDTO aDTO(Empleado e, Administrador administrador, Vendedor vendedor) {
		Sucursal sucursal = vendedor == null ? null : vendedor.getSucursal();
		return new EmpleadoDTO(e.getIdEmpleado(), e.getDocumento(), e.getNombre(), e.getApellido(), e.getCargo(), e.getCorreo(),
				List.copyOf(e.getTelefonos()),
				administrador != null ? Rol.ADMINISTRADOR : vendedor != null ? Rol.VENDEDOR : null,
				administrador == null ? null : administrador.getNivelAcceso(),
				vendedor == null ? null : vendedor.getComisionVenta(),
				sucursal == null ? null : sucursal.getIdSucursal(),
				sucursal == null ? null : sucursal.getNombre());
	}

	private void validarUnicos(EmpleadoEntradaDTO entrada, Integer idEmpleado) {
		String documento = entrada.documento().trim();
		String correo = entrada.correo().trim().toLowerCase();
		boolean documentoRepetido = idEmpleado == null ? empleadoRepository.existsByDocumento(documento)
				: empleadoRepository.existsByDocumentoAndIdEmpleadoNot(documento, idEmpleado);
		if (documentoRepetido) {
			throw new ConflictoException("Ya hay un empleado con el documento " + documento + ".");
		}
		boolean correoRepetido = idEmpleado == null ? empleadoRepository.existsByCorreo(correo)
				: empleadoRepository.existsByCorreoAndIdEmpleadoNot(correo, idEmpleado);
		if (correoRepetido) {
			throw new ConflictoException("Ya hay un empleado con el correo " + correo + ".");
		}
	}

	private static void copiarDatos(EmpleadoEntradaDTO entrada, Empleado empleado) {
		empleado.setDocumento(entrada.documento().trim());
		empleado.setNombre(entrada.nombre().trim());
		empleado.setApellido(entrada.apellido().trim());
		empleado.setCargo(entrada.cargo().trim());
		empleado.setCorreo(entrada.correo().trim().toLowerCase());
		empleado.getTelefonos().clear();
		empleado.getTelefonos().addAll(entrada.telefonos().stream().map(String::trim).collect(Collectors.toCollection(LinkedHashSet::new)));
	}

	private Empleado buscar(Integer idEmpleado) {
		return empleadoRepository.findWithTelefonosByIdEmpleado(idEmpleado)
				.orElseThrow(() -> RecursoNoEncontradoException.de("el empleado", idEmpleado));
	}

	private Sucursal buscarSucursal(Integer idSucursal) {
		return sucursalRepository.findById(idSucursal).orElseThrow(() -> RecursoNoEncontradoException.de("la sucursal", idSucursal));
	}
}
