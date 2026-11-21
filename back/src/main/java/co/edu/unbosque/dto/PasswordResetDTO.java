package co.edu.unbosque.dto;

import lombok.Data;

/**
 * Datos para recuperar la contraseña o reenviar la verificación: /auth/forgot y
 * /auth/resend-verification usan el correo, y /auth/reset usa el token y la nueva contraseña.
 */
@Data
public class PasswordResetDTO {
	private String email;
	private String token;
	private String nuevaPassword;
}
