package co.edu.unbosque.bogoparche.dto;

import java.util.List;
import lombok.Data;

/**
 * Respuesta de Overpass con los elementos que cumplen la consulta.
 */
@Data
public class OverpassRespuestaDTO {
	private List<OverpassElementoDTO> elements;
}
