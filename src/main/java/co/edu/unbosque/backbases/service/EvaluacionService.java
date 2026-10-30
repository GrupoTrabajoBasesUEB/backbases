package co.edu.unbosque.backbases.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.backbases.dto.EvaluacionDTO;
import co.edu.unbosque.backbases.dto.EvaluacionEntradaDTO;
import co.edu.unbosque.backbases.entity.Administrador;
import co.edu.unbosque.backbases.entity.Evaluacion;
import co.edu.unbosque.backbases.entity.Suministro;
import co.edu.unbosque.backbases.entity.SuministroId;
import co.edu.unbosque.backbases.exception.RecursoNoEncontradoException;
import co.edu.unbosque.backbases.exception.ReglaNegocioException;
import co.edu.unbosque.backbases.exception.SinPermisoException;
import co.edu.unbosque.backbases.repository.AdministradorRepository;
import co.edu.unbosque.backbases.repository.EvaluacionRepository;
import co.edu.unbosque.backbases.repository.SuministroRepository;
import co.edu.unbosque.backbases.security.UsuarioAutenticado;

@Service
public class EvaluacionService {

	private final EvaluacionRepository evaluacionRepository;
	private final SuministroRepository suministroRepository;
	private final AdministradorRepository administradorRepository;

	public EvaluacionService(EvaluacionRepository evaluacionRepository, SuministroRepository suministroRepository,
			AdministradorRepository administradorRepository) {
		this.evaluacionRepository = evaluacionRepository;
		this.suministroRepository = suministroRepository;
		this.administradorRepository = administradorRepository;
	}

	@Transactional(readOnly = true)
	public List<EvaluacionDTO> buscar(String nit, Integer idProducto, Integer idSucursal, Integer idEmpleado, LocalDate desde, LocalDate hasta) {
		if (desde != null && hasta != null && desde.isAfter(hasta)) {
			throw new ReglaNegocioException("La fecha inicial no puede ser posterior a la final.");
		}
		return evaluacionRepository.buscar(nit == null || nit.isBlank() ? null : nit.trim(), idProducto, idSucursal, idEmpleado, desde, hasta)
				.stream().map(EvaluacionDTO::de).toList();
	}

	@Transactional
	public EvaluacionDTO registrar(EvaluacionEntradaDTO entrada, UsuarioAutenticado usuario) {
		Administrador evaluador = administradorRepository.findById(usuario.idEmpleado())
				.orElseThrow(() -> new SinPermisoException("Solo un administrador puede registrar evaluaciones de calidad."));
		Suministro suministro = suministroRepository.findById(new SuministroId(entrada.nit().trim(), entrada.idProducto(), entrada.idSucursal()))
				.orElseThrow(() -> new ReglaNegocioException("Solo se pueden evaluar suministros registrados."));
		LocalDate hoy = LocalDate.now(VentaService.ZONA);
		LocalDate fecha = entrada.fechaEvaluacion() == null ? hoy : entrada.fechaEvaluacion();
		if (fecha.isAfter(hoy)) {
			throw new ReglaNegocioException("La fecha de evaluación no puede estar en el futuro.");
		}
		Evaluacion evaluacion = new Evaluacion();
		evaluacion.setSuministro(suministro);
		evaluacion.setAdministrador(evaluador);
		evaluacion.setFechaEvaluacion(fecha);
		evaluacion.setCalificacion(entrada.calificacion());
		evaluacion.setCumplimientoTiempo(entrada.cumplimientoTiempo());
		evaluacion.setObservaciones(entrada.observaciones() == null || entrada.observaciones().isBlank() ? null : entrada.observaciones().trim());
		Integer id = evaluacionRepository.saveAndFlush(evaluacion).getIdEvaluacion();
		return EvaluacionDTO.de(buscarEvaluacion(id));
	}

	@Transactional
	public void eliminar(Integer idEvaluacion) {
		evaluacionRepository.delete(buscarEvaluacion(idEvaluacion));
	}

	private Evaluacion buscarEvaluacion(Integer idEvaluacion) {
		return evaluacionRepository.buscarCompleta(idEvaluacion)
				.orElseThrow(() -> RecursoNoEncontradoException.de("la evaluación", idEvaluacion));
	}
}
