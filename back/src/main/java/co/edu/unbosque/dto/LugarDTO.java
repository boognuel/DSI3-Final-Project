package co.edu.unbosque.dto;

import java.math.BigDecimal;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Datos de un lugar del catálogo. Al crear o editar se envían los identificadores de la categoría
 * y la zona; en las respuestas también llegan sus nombres.
 */
@Data
public class LugarDTO {
	private Long id;
	@NotBlank(message = "El nombre del lugar es obligatorio")
	@Size(max = 255, message = "El nombre del lugar admite máximo 255 caracteres")
	private String nombre;
	@Size(max = 500, message = "La descripción admite máximo 500 caracteres")
	private String descripcion;
	@NotNull(message = "La categoría es obligatoria")
	private Long categoriaId;
	private String categoriaNombre;
	@NotNull(message = "La zona es obligatoria")
	private Long zonaId;
	private String zonaNombre;
	@NotBlank(message = "La dirección es obligatoria")
	@Size(max = 255, message = "La dirección admite máximo 255 caracteres")
	private String direccion;
	@NotNull(message = "La latitud es obligatoria")
	private Double latitud;
	@NotNull(message = "La longitud es obligatoria")
	private Double longitud;
	@NotNull(message = "El costo estimado por persona es obligatorio")
	@PositiveOrZero(message = "El costo estimado no puede ser negativo")
	private BigDecimal costoEstimado;
}
