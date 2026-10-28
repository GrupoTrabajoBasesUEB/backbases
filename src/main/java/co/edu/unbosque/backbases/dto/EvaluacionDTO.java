package co.edu.unbosque.backbases.dto;

import java.time.LocalDate;

import co.edu.unbosque.backbases.entity.Empleado;
import co.edu.unbosque.backbases.entity.Evaluacion;
import co.edu.unbosque.backbases.entity.Suministro;

public record EvaluacionDTO(
		Integer idEvaluacion,
		String nit,
		String nombreProveedor,
		Integer idProducto,
		String codigo,
		String nombreProducto,
		Integer idSucursal,
		String nombreSucursal,
		Integer idEmpleado,
		String nombreEvaluador,
		LocalDate fechaEvaluacion,
		Integer calificacion,
		Boolean cumplimientoTiempo,
		String observaciones) {

	public static EvaluacionDTO de(Evaluacion e) {
		Suministro s = e.getSuministro();
		Empleado evaluador = e.getAdministrador().getEmpleado();
		return new EvaluacionDTO(e.getIdEvaluacion(), s.getProveedor().getNit(), s.getProveedor().getNombre(),
				s.getProducto().getIdProducto(), s.getProducto().getCodigo(), s.getProducto().getNombre(),
				s.getSucursal().getIdSucursal(), s.getSucursal().getNombre(), evaluador.getIdEmpleado(),
				evaluador.getNombre() + " " + evaluador.getApellido(), e.getFechaEvaluacion(), e.getCalificacion(),
				e.getCumplimientoTiempo(), e.getObservaciones());
	}
}
