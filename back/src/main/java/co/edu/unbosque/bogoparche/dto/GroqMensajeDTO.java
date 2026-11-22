package co.edu.unbosque.bogoparche.dto;

import lombok.Data;

/**
 * Mensaje de la conversación con Groq: el rol (system, user o assistant) y el texto.
 */
@Data
public class GroqMensajeDTO {
	private String role;
	private String content;
}
