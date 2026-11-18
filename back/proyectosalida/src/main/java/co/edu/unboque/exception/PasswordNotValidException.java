package co.edu.unboque.exception;

public class PasswordNotValidException extends Exception {
	private static final long serialVersionUID = -1290628531913444044L;

	public PasswordNotValidException() {
		super("La contraseña no cumple con el estándar (mínimo 8 caracteres, al menos una letra minúscula, "
				+ "al menos una letra mayúscula, al menos un número y al menos un símbolo).");
	}
}
