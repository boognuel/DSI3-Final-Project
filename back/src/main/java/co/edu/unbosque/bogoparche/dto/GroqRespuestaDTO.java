package co.edu.unbosque.bogoparche.dto;

import java.util.List;
import lombok.Data;

/**
 * Respuesta de Groq con las respuestas generadas.
 */
@Data
public class GroqRespuestaDTO {
	private List<GroqOpcionDTO> choices;
}
