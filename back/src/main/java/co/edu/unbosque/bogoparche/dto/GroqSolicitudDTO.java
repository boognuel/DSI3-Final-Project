package co.edu.unbosque.bogoparche.dto;

import java.util.List;
import lombok.Data;

/**
 * Petición a Groq con el modelo, la temperatura y los mensajes.
 */
@Data
public class GroqSolicitudDTO {
	private String model;
	private double temperature;
	private List<GroqMensajeDTO> messages;
}
