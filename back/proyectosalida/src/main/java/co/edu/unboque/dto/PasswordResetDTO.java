package co.edu.unboque.dto;

import lombok.Data;

// Sirve para /auth/forgot y /auth/resend-verification (email) y /auth/reset (token + nuevaPassword)
@Data
public class PasswordResetDTO {
	private String email;
	private String token;
	private String nuevaPassword;
}
