package co.edu.unbosque.dto;

import java.time.LocalDateTime;
import co.edu.unbosque.model.EstadoInvitacion;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

// El token NO va aquí: solo lo recibe el invitado por correo
@Data
public class InvitacionDTO {
	private Long id;
	@NotNull
	private Long salidaId;
	private String salidaTitulo;
	@NotBlank
	@Email
	private String emailInvitado;
	private EstadoInvitacion estado;
	private LocalDateTime fechaEnvio;
	private LocalDateTime fechaExpiracion;
	private LocalDateTime fechaRespuesta;
}
