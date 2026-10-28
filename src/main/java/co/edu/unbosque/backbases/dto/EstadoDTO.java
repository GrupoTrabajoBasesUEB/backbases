package co.edu.unbosque.backbases.dto;

import java.time.OffsetDateTime;

public record EstadoDTO(String aplicacion, String version, String baseDatos, OffsetDateTime hora) {
}
