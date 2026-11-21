package co.edu.unbosque.bogoparche.model;

import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Invitación a una salida enviada por correo. El token del enlace prueba que quien responde tiene
 * acceso a ese correo.
 */
@Entity
@Table(name = "invitacion")
@Getter
@Setter
@NoArgsConstructor
public class Invitacion {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@ManyToOne(optional = false)
	@JoinColumn(name = "salida_id")
	private Salida salida;
	@Column(nullable = false)
	private String emailInvitado;
	@ManyToOne
	@JoinColumn(name = "usuario_invitado_id")
	private User usuarioInvitado;
	@Column(unique = true, nullable = false, length = 64)
	private String token;
	@Enumerated(EnumType.STRING)
	private EstadoInvitacion estado = EstadoInvitacion.PENDIENTE;
	private LocalDateTime fechaEnvio;
	private LocalDateTime fechaExpiracion;
	private LocalDateTime fechaRespuesta;
}
