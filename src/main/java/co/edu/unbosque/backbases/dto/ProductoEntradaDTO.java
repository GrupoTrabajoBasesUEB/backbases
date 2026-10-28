package co.edu.unbosque.backbases.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record ProductoEntradaDTO(
		@NotBlank(message = "El código es obligatorio.")
		@Size(max = 20, message = "El código admite máximo 20 caracteres.")
		String codigo,

		@NotBlank(message = "El nombre del producto es obligatorio.")
		@Size(max = 100, message = "El nombre del producto admite máximo 100 caracteres.")
		String nombreProducto,

		@Size(max = 255, message = "La descripción admite máximo 255 caracteres.")
		String descripcion,

		@NotNull(message = "El costo de compra es obligatorio.")
		@PositiveOrZero(message = "El costo de compra no puede ser negativo.")
		@Digits(integer = 10, fraction = 2, message = "El costo de compra admite hasta 10 enteros y 2 decimales.")
		BigDecimal costoCompra,

		@NotNull(message = "El precio base de venta es obligatorio.")
		@PositiveOrZero(message = "El precio base de venta no puede ser negativo.")
		@Digits(integer = 10, fraction = 2, message = "El precio base de venta admite hasta 10 enteros y 2 decimales.")
		BigDecimal precioBaseVenta,

		@NotNull(message = "La categoría es obligatoria.")
		Integer idCategoria,

		@NotNull(message = "La marca es obligatoria.")
		Integer idMarca) {
}
