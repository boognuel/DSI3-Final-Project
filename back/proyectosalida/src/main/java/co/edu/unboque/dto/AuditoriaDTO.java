package co.edu.unboque.dto;

import java.time.LocalDateTime;
import co.edu.unboque.model.AccionAuditoria;
import co.edu.unboque.model.ResultadoAuditoria;
import lombok.Data;

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
	private ResultadoAuditoria resultado;
}
