package co.edu.unboque.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

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
