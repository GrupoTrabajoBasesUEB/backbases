package co.edu.unbosque.backbases.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unbosque.backbases.dto.EstadoDTO;
import co.edu.unbosque.backbases.service.EstadoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/estado")
@Tag(name = "Estado", description = "Salud del servicio")
public class EstadoController {

	private final EstadoService estadoService;

	public EstadoController(EstadoService estadoService) {
		this.estadoService = estadoService;
	}

	@GetMapping
	@SecurityRequirements
	@Operation(summary = "Estado de la API y de la conexión a la base de datos")
	public EstadoDTO estado() {
		return estadoService.consultar();
	}
}
