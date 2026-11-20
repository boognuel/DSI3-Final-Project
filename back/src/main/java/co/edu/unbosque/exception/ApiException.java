package co.edu.unbosque.exception;

import org.springframework.http.HttpStatus;

/**
 * Excepción base de la aplicación. Cada subclase indica el código HTTP con el que
 * {@link GlobalExceptionHandler} responde al cliente, junto con el mensaje de la excepción.
 */
public abstract class ApiException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	/** Código HTTP de la respuesta. */
	private final HttpStatus estado;

	/**
	 * Crea la excepción.
	 *
	 * @param estado  código HTTP de la respuesta
	 * @param mensaje mensaje que se envía al cliente
	 */
	protected ApiException(HttpStatus estado, String mensaje) {
		super(mensaje);
		this.estado = estado;
	}

	/**
	 * Devuelve el código HTTP de la respuesta.
	 *
	 * @return código HTTP
	 */
	public HttpStatus getEstado() {
		return estado;
	}
}
