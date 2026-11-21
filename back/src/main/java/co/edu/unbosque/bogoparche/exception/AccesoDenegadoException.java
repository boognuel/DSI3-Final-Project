package co.edu.unbosque.bogoparche.exception;

import org.springframework.http.HttpStatus;

/**
 * Se lanza cuando el usuario autenticado no tiene permiso sobre el recurso, por ejemplo al editar
 * una salida que no organiza. Responde 403 (Forbidden).
 */
public class AccesoDenegadoException extends ApiException {
	private static final long serialVersionUID = 1L;

	/**
	 * Crea la excepción.
	 *
	 * @param mensaje mensaje que se envía al cliente
	 */
	public AccesoDenegadoException(String mensaje) {
		super(HttpStatus.FORBIDDEN, mensaje);
	}
}
