package co.edu.unbosque.bogoparche.exception;

import org.springframework.http.HttpStatus;

/**
 * Se lanza cuando el recurso solicitado no existe. Responde 404 (Not Found).
 */
public class RecursoNoEncontradoException extends ApiException {
	private static final long serialVersionUID = 1L;

	/**
	 * Crea la excepción.
	 *
	 * @param mensaje mensaje que se envía al cliente
	 */
	public RecursoNoEncontradoException(String mensaje) {
		super(HttpStatus.NOT_FOUND, mensaje);
	}
}
