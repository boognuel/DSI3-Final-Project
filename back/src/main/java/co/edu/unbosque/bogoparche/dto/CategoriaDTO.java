package co.edu.unbosque.bogoparche.dto;

import jakarta.validation.constraints.NotBlank;
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
}
