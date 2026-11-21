package co.edu.unbosque.bogoparche.dto;

import java.time.LocalDateTime;
import co.edu.unbosque.bogoparche.model.AccionAuditoria;
import co.edu.unbosque.bogoparche.model.ResultadoAuditoria;
import lombok.Data;

/**
 * Datos de un registro de auditoría que se envían al administrador.
 */
@Data
public class AuditoriaDTO {
	private Long id;
	private LocalDateTime fecha;
	private Long usuarioId;
	private String usuarioNombre;
	private String rolUsuario;
	private AccionAuditoria accion;
	private String entidad;
	private Long entidadId;
	private String detalle;
	private String ipOrigen;
	private String userAgent;
	private Double latitud;
	private Double longitud;
	private ResultadoAuditoria resultado;
}
