package co.edu.unbosque.model;

import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Registro de auditoría. Se crea automáticamente en cada acción relevante del sistema y guarda la
 * fecha, quién la hizo, qué hizo, el resultado y desde dónde (IP, navegador y coordenadas).
 */
@Entity
@Table(name = "auditoria_log", indexes = { @Index(columnList = "fecha"), @Index(columnList = "accion") })
@Getter
@Setter
@NoArgsConstructor
public class AuditoriaLog {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private LocalDateTime fecha;
	// Sin llave foránea: el registro debe sobrevivir aunque se borre el usuario
	private Long usuarioId;
	private String usuarioNombre;
	private String rolUsuario;
	@Enumerated(EnumType.STRING)
	private AccionAuditoria accion;
	private String entidad;
	private Long entidadId;
	@Column(length = 500)
	private String detalle;
	private String ipOrigen;
	private String userAgent;
	@Enumerated(EnumType.STRING)
	private ResultadoAuditoria resultado;
}
