package co.edu.unbosque.dto;

import java.math.BigDecimal;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Datos de una zona y su rango de gasto por persona.
 */
@Data
public class ZonaDTO {
	private Long id;
	@NotBlank(message = "El nombre de la zona es obligatorio")
	@Size(max = 80, message = "El nombre de la zona admite máximo 80 caracteres")
	private String nombre;
	@Size(max = 255, message = "La descripción admite máximo 255 caracteres")
	private String descripcion;
	@NotNull(message = "El gasto mínimo es obligatorio")
	@PositiveOrZero(message = "El gasto mínimo no puede ser negativo")
	private BigDecimal gastoMinimo;
	@NotNull(message = "El gasto máximo es obligatorio")
	@PositiveOrZero(message = "El gasto máximo no puede ser negativo")
	private BigDecimal gastoMaximo;
}
