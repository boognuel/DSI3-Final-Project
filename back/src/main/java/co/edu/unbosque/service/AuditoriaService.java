package co.edu.unbosque.service;

import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import co.edu.unbosque.model.AccionAuditoria;
import co.edu.unbosque.model.AuditoriaLog;
import co.edu.unbosque.model.ResultadoAuditoria;
import co.edu.unbosque.model.User;
import co.edu.unbosque.repository.AuditoriaRepository;
import jakarta.servlet.http.HttpServletRequest;

@Service
public class AuditoriaService {
	private static final Logger log = LoggerFactory.getLogger(AuditoriaService.class);

	@Autowired
	private AuditoriaRepository repo;

	public void registrar(User u, AccionAuditoria accion, String entidad, Long entidadId, String detalle,
			ResultadoAuditoria resultado) {
		guardar(u == null ? null : u.getId(), u == null ? null : u.getUsername(),
				u == null ? null : u.getRole().name(), accion, entidad, entidadId, detalle, resultado);
	}

	public void registrarSinSesion(String username, AccionAuditoria accion, String detalle,
			ResultadoAuditoria resultado) {
		guardar(null, username, null, accion, null, null, detalle, resultado);
	}

	private void guardar(Long usuarioId, String usuarioNombre, String rol, AccionAuditoria accion, String entidad,
			Long entidadId, String detalle, ResultadoAuditoria resultado) {
		try {
			AuditoriaLog l = new AuditoriaLog();
			l.setFecha(LocalDateTime.now());
			l.setUsuarioId(usuarioId);
			l.setUsuarioNombre(recortar(usuarioNombre, 100));
			l.setRolUsuario(rol);
			l.setAccion(accion);
			l.setEntidad(entidad);
			l.setEntidadId(entidadId);
			l.setDetalle(recortar(detalle, 500));
			l.setResultado(resultado);
			RequestAttributes attrs = RequestContextHolder.getRequestAttributes();
			if (attrs instanceof ServletRequestAttributes sra) {
				HttpServletRequest req = sra.getRequest();
				l.setIpOrigen(req.getRemoteAddr());
				l.setUserAgent(recortar(req.getHeader("User-Agent"), 255));
			}
			repo.save(l);
		} catch (Exception e) {
			// Un fallo al auditar no debe romper la operación del usuario
			log.error("No se pudo registrar la auditoría: {}", e.getMessage());
		}
	}

	public Page<AuditoriaLog> buscar(String usuario, AccionAuditoria accion, LocalDateTime desde, LocalDateTime hasta,
			int page, int size) {
		PageRequest pr = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100),
				Sort.by(Sort.Direction.DESC, "fecha"));
		String u = usuario == null ? "" : usuario.trim();
		return accion == null ? repo.buscar(u, desde, hasta, pr) : repo.buscarPorAccion(u, accion, desde, hasta, pr);
	}

	private String recortar(String s, int max) {
		return s == null ? null : (s.length() > max ? s.substring(0, max) : s);
	}
}
