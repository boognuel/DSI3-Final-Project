package co.edu.unbosque.bogoparche.exception;

/**
 * Excepción que se lanza cuando una contraseña no cumple la política de seguridad: mínimo 8
 * caracteres, con al menos una minúscula, una mayúscula, un número y un símbolo. Responde 400
 * (Bad Request).
 */
public class PasswordNotValidException extends DatosInvalidosException {
	private static final long serialVersionUID = -1290628531913444044L;

	/**
	 * Crea la excepción con el mensaje que describe los requisitos de la contraseña.
	 */
	public PasswordNotValidException() {
		super("La contraseña no cumple con el estándar (mínimo 8 caracteres, al menos una letra minúscula, "
				+ "al menos una letra mayúscula, al menos un número y al menos un símbolo).");
	}
}
