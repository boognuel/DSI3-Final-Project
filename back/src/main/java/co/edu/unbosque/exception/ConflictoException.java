package co.edu.unbosque.exception;

import org.springframework.http.HttpStatus;

/**
 * Se lanza cuando la operación choca con el estado actual de los datos, por ejemplo un nombre de
 * usuario o un correo ya registrado. Responde 409 (Conflict).
 */
public class ConflictoException extends ApiException {
	private static final long serialVersionUID = 1L;

	/**
	 * Crea la excepción.
	 *
	 * @param mensaje mensaje que se envía al cliente
	 */
	public ConflictoException(String mensaje) {
		super(HttpStatus.CONFLICT, mensaje);
	}
}
