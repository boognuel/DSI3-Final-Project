package co.edu.unbosque.bogoparche.dto;

import java.math.BigDecimal;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Datos de una categoría de planes.
 */
@Data
public class CategoriaDTO {
	private Long id;
	@NotBlank(message = "El nombre de la categoría es obligatorio")
	@Size(max = 50, message = "El nombre de la categoría admite máximo 50 caracteres")
	private String nombre;
	@Size(max = 255, message = "La descripción admite máximo 255 caracteres")
	private String descripcion;
	@Pattern(regexp = "^$|^[a-z_]+=[a-z_]+(,[a-z_]+=[a-z_]+)*$", message = "Las etiquetas deben tener la forma clave=valor, separadas por comas, por ejemplo amenity=bar")
	private String etiquetaOsm;
	@PositiveOrZero(message = "El precio de referencia no puede ser negativo")
	private BigDecimal precioReferencia;
}
