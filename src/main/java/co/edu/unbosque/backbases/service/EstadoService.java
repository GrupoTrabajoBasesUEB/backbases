package co.edu.unbosque.backbases.service;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.OffsetDateTime;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import co.edu.unbosque.backbases.dto.EstadoDTO;

@Service
public class EstadoService {

	private static final Logger log = LoggerFactory.getLogger(EstadoService.class);
	private static final int SEGUNDOS_ESPERA = 3;

	private final DataSource dataSource;
	private final String aplicacion;
	private final String version;

	public EstadoService(DataSource dataSource, @Value("${spring.application.name}") String aplicacion,
			@Value("${app.version}") String version) {
		this.dataSource = dataSource;
		this.aplicacion = aplicacion;
		this.version = version;
	}

	public EstadoDTO consultar() {
		return new EstadoDTO(aplicacion, version, baseDisponible() ? "DISPONIBLE" : "NO_DISPONIBLE", OffsetDateTime.now());
	}

	private boolean baseDisponible() {
		try (Connection conexion = dataSource.getConnection()) {
			return conexion.isValid(SEGUNDOS_ESPERA);
		} catch (SQLException e) {
			log.warn("La base de datos no responde: {}", e.getMessage());
			return false;
		}
	}
}
