package co.edu.unbosque.bogoparche.exception;

import org.springframework.http.HttpStatus;

/**
 * Se lanza cuando falla un servicio externo del que depende la operación, como el envío de
 * correos. Responde 502 (Bad Gateway).
 */
public class ServicioExternoException extends ApiException {
	private static final long serialVersionUID = 1L;

	/**
	 * Crea la excepción.
	 *
	 * @param mensaje mensaje que se envía al cliente
	 */
	public ServicioExternoException(String mensaje) {
		super(HttpStatus.BAD_GATEWAY, mensaje);
	}
}
