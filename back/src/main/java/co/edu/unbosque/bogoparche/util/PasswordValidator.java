package co.edu.unbosque.bogoparche.util;

import java.util.regex.Pattern;
import co.edu.unbosque.bogoparche.exception.PasswordNotValidException;

/**
 * Utilidad para validar que una contraseña cumpla la política de seguridad de la aplicación.
 */
public class PasswordValidator {

	/** Mínimo 8 caracteres, con al menos una minúscula, una mayúscula, un número y un símbolo. */
	private static final Pattern PATRON = Pattern
			.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,}$");

	/**
	 * Valida la contraseña recibida.
	 *
	 * @param password contraseña en texto plano
	 * @throws PasswordNotValidException si la contraseña es nula o no cumple la política
	 */
	public static void validar(String password) throws PasswordNotValidException {
		if (password == null || !PATRON.matcher(password).matches()) {
			throw new PasswordNotValidException();
		}
	}
}
