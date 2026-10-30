package co.edu.unbosque.backbases.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unbosque.backbases.dto.EvaluacionDTO;
import co.edu.unbosque.backbases.dto.EvaluacionEntradaDTO;
import co.edu.unbosque.backbases.security.UsuarioAutenticado;
import co.edu.unbosque.backbases.service.EvaluacionService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/evaluaciones")
@PreAuthorize("hasAuthority('NIVEL_2')")
public class EvaluacionController {

	private final EvaluacionService evaluacionService;

	public EvaluacionController(EvaluacionService evaluacionService) {
		this.evaluacionService = evaluacionService;
	}

	@GetMapping
	public List<EvaluacionDTO> buscar(@RequestParam(required = false) String nit, @RequestParam(required = false) Integer idProducto,
			@RequestParam(required = false) Integer idSucursal, @RequestParam(required = false) Integer idEmpleado,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
		return evaluacionService.buscar(nit, idProducto, idSucursal, idEmpleado, desde, hasta);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public EvaluacionDTO registrar(@Valid @RequestBody EvaluacionEntradaDTO entrada, @AuthenticationPrincipal UsuarioAutenticado usuario) {
		return evaluacionService.registrar(entrada, usuario);
	}

	@DeleteMapping("/{idEvaluacion}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void eliminar(@PathVariable Integer idEvaluacion) {
		evaluacionService.eliminar(idEvaluacion);
	}
}
