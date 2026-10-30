package co.edu.unbosque.backbases.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
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

import co.edu.unbosque.backbases.dto.ClienteDTO;
import co.edu.unbosque.backbases.dto.ClienteEntradaDTO;
import co.edu.unbosque.backbases.dto.TipoCliente;
import co.edu.unbosque.backbases.service.ClienteService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/clientes")
public class ClienteController {

	private final ClienteService clienteService;

	public ClienteController(ClienteService clienteService) {
		this.clienteService = clienteService;
	}

	@GetMapping
	public List<ClienteDTO> buscar(@RequestParam(required = false) String q, @RequestParam(required = false) TipoCliente tipo) {
		return clienteService.buscar(q, tipo);
	}

	@GetMapping("/{idCliente}")
	public ClienteDTO obtener(@PathVariable Integer idCliente) {
		return clienteService.obtener(idCliente);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public ClienteDTO crear(@Valid @RequestBody ClienteEntradaDTO entrada) {
		return clienteService.crear(entrada);
	}

	@PutMapping("/{idCliente}")
	public ClienteDTO actualizar(@PathVariable Integer idCliente, @Valid @RequestBody ClienteEntradaDTO entrada) {
		return clienteService.actualizar(idCliente, entrada);
	}

	@DeleteMapping("/{idCliente}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@PreAuthorize("hasRole('ADMINISTRADOR')")
	public void eliminar(@PathVariable Integer idCliente) {
		clienteService.eliminar(idCliente);
	}
}
