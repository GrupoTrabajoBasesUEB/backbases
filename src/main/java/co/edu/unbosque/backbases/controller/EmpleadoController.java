package co.edu.unbosque.backbases.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unbosque.backbases.dto.EmpleadoDTO;
import co.edu.unbosque.backbases.dto.EmpleadoEntradaDTO;
import co.edu.unbosque.backbases.dto.Rol;
import co.edu.unbosque.backbases.security.UsuarioAutenticado;
import co.edu.unbosque.backbases.service.EmpleadoService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/empleados")
public class EmpleadoController {

	private final EmpleadoService empleadoService;

	public EmpleadoController(EmpleadoService empleadoService) {
		this.empleadoService = empleadoService;
	}

	@GetMapping
	public List<EmpleadoDTO> listar(@RequestParam(required = false) Rol rol) {
		return empleadoService.listar(rol);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@PreAuthorize("hasAuthority('NIVEL_3')")
	public EmpleadoDTO crear(@Valid @RequestBody EmpleadoEntradaDTO entrada) {
		return empleadoService.crear(entrada);
	}

	@PutMapping("/{idEmpleado}")
	@PreAuthorize("hasAuthority('NIVEL_3')")
	public EmpleadoDTO actualizar(@PathVariable Integer idEmpleado, @Valid @RequestBody EmpleadoEntradaDTO entrada) {
		return empleadoService.actualizar(idEmpleado, entrada);
	}

	@DeleteMapping("/{idEmpleado}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@PreAuthorize("hasAuthority('NIVEL_3')")
	public void eliminar(@PathVariable Integer idEmpleado, @AuthenticationPrincipal UsuarioAutenticado usuario) {
		empleadoService.eliminar(idEmpleado, usuario.idEmpleado());
	}
}
