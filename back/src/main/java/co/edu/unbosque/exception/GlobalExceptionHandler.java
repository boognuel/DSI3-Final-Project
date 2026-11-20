package co.edu.unbosque.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Convierte las excepciones de la aplicación en respuestas HTTP. El cuerpo es el mensaje en texto
 * plano, que es el formato que muestra el frontend.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

	/**
	 * Responde con el código HTTP y el mensaje de la excepción.
	 *
	 * @param e excepción lanzada por un servicio o controlador
	 * @return respuesta con el código de la excepción y su mensaje
	 */
	@ExceptionHandler(ApiException.class)
	public ResponseEntity<String> manejarApiException(ApiException e) {
		return ResponseEntity.status(e.getEstado()).body(e.getMessage());
	}

	/**
	 * Responde 400 cuando falla la validación de un DTO anotado con {@code @Valid}, con el mensaje
	 * del primer campo que no cumple.
	 *
	 * @param e excepción de validación
	 * @return respuesta 400 con el mensaje del campo
	 */
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<String> manejarValidacion(MethodArgumentNotValidException e) {
		FieldError campo = e.getBindingResult().getFieldError();
		String mensaje = campo != null ? campo.getDefaultMessage() : "Revisa los datos ingresados";
		return ResponseEntity.badRequest().body(mensaje);
	}
}
