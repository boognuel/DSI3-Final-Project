package co.edu.unbosque.exception;

import org.springframework.http.HttpStatus;

/**
 * Se lanza cuando un enlace o token venció o ya fue usado, como los de verificación, recuperación
 * de contraseña o invitación. Responde 410 (Gone).
 */
public class RecursoExpiradoException extends ApiException {
	private static final long serialVersionUID = 1L;

	/**
	 * Crea la excepción.
	 *
	 * @param mensaje mensaje que se envía al cliente
	 */
	public RecursoExpiradoException(String mensaje) {
		super(HttpStatus.GONE, mensaje);
	}
}
