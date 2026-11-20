package co.edu.unboque.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import co.edu.unboque.model.User;
import lombok.Data;

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
