package co.edu.unbosque.exception;

import org.springframework.http.HttpStatus;

/**
 * Se lanza cuando los datos de la petición están incompletos o no son válidos. Responde 400 (Bad
 * Request).
 */
public class DatosInvalidosException extends ApiException {
	private static final long serialVersionUID = 1L;

	/**
	 * Crea la excepción.
	 *
	 * @param mensaje mensaje que se envía al cliente
	 */
	public DatosInvalidosException(String mensaje) {
		super(HttpStatus.BAD_REQUEST, mensaje);
	}
}
