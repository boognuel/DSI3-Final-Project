package co.edu.unbosque.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Datos para crear una cuenta: nombre de usuario, correo y contraseña.
 */
@Data
public class RegistroDTO {
	@NotBlank
	@Size(min = 3, max = 50)
	private String username;
	@NotBlank
	@Email
	@Size(max = 150)
	private String email;
	@NotBlank
	private String password;
}
