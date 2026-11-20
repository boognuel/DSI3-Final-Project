package co.edu.unbosque.model;

import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Token de un solo uso que se envía por correo para verificar la cuenta o cambiar la contraseña.
 */
@Entity
@Table(name = "token_usuario")
@Getter
@Setter
@NoArgsConstructor
public class TokenUsuario {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@Column(unique = true, nullable = false, length = 64)
	private String token;
	@Enumerated(EnumType.STRING)
	private TipoToken tipo;
	@ManyToOne(optional = false)
	@JoinColumn(name = "usuario_id")
	private User usuario;
	private LocalDateTime fechaExpiracion;
	private boolean usado;
}
