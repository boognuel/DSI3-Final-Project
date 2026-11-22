package co.edu.unbosque.bogoparche.dto;

import lombok.Data;

/**
 * Una de las respuestas que genera Groq.
 */
@Data
public class GroqOpcionDTO {
	private GroqMensajeDTO message;
}
