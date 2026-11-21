package co.edu.unbosque.bogoparche.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import co.edu.unbosque.bogoparche.model.User;
import lombok.Data;

/**
 * Datos de un usuario. La contraseña solo se recibe al crear o editar y nunca se devuelve.
 */
@Data
public class UserDTO {
	private Long id;
	private String username;
	private String email;
	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY) // nunca se devuelve la contraseña
	private String password;
	private User.Role role;
	private Boolean enabled;
	private Boolean emailVerificado;
}
