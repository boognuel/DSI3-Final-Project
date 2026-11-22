package co.edu.unbosque.bogoparche.model;

import java.time.LocalDateTime;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Registro de auditoría. Se crea automáticamente en cada acción relevante del sistema y guarda la
 * fecha, quién la hizo, qué hizo, el resultado y desde dónde (IP y navegador).
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
	// Texto y no ENUM de MySQL: así agregar una acción nueva no exige modificar la columna
	@Enumerated(EnumType.STRING)
	@JdbcTypeCode(SqlTypes.VARCHAR)
	@Column(length = 40)
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
