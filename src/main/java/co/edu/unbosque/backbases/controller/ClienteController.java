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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/clientes")
@Tag(name = "Clientes")
public class ClienteController {

	private final ClienteService clienteService;

	public ClienteController(ClienteService clienteService) {
		this.clienteService = clienteService;
	}

	@GetMapping
	@Operation(summary = "Buscar clientes por documento, nombre o razón social, y por tipo")
	public List<ClienteDTO> buscar(@RequestParam(required = false) String q, @RequestParam(required = false) TipoCliente tipo) {
		return clienteService.buscar(q, tipo);
	}

	@GetMapping("/consumidor-final")
	@Operation(summary = "Cliente del sistema para ventas sin datos del comprador")
	public ClienteDTO consumidorFinal() {
		return clienteService.consumidorFinal();
	}

	@GetMapping("/{idCliente}")
	@Operation(summary = "Obtener un cliente")
	public ClienteDTO obtener(@PathVariable Integer idCliente) {
		return clienteService.obtener(idCliente);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(summary = "Registrar un cliente natural o jurídico con sus teléfonos y correos")
	public ClienteDTO crear(@Valid @RequestBody ClienteEntradaDTO entrada) {
		return clienteService.crear(entrada);
	}

	@PutMapping("/{idCliente}")
	@Operation(summary = "Actualizar un cliente; puede cambiar de natural a jurídico o al revés")
	public ClienteDTO actualizar(@PathVariable Integer idCliente, @Valid @RequestBody ClienteEntradaDTO entrada) {
		return clienteService.actualizar(idCliente, entrada);
	}

	@DeleteMapping("/{idCliente}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@PreAuthorize("hasRole('ADMINISTRADOR')")
	@Operation(summary = "Eliminar un cliente sin ventas")
	public void eliminar(@PathVariable Integer idCliente) {
		clienteService.eliminar(idCliente);
	}
}
