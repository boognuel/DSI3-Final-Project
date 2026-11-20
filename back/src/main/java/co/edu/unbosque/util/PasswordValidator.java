package co.edu.unbosque.util;

import java.util.regex.Pattern;
import co.edu.unbosque.exception.PasswordNotValidException;

public class PasswordValidator {
	private static final Pattern PATRON = Pattern
			.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,}$");

	public static void validar(String password) throws PasswordNotValidException {
		if (password == null || !PATRON.matcher(password).matches()) {
			throw new PasswordNotValidException();
		}
	}
}
