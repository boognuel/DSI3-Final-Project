package co.edu.unbosque.bogoparche.service;

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
import co.edu.unbosque.bogoparche.model.AccionAuditoria;
import co.edu.unbosque.bogoparche.model.AuditoriaLog;
import co.edu.unbosque.bogoparche.model.ResultadoAuditoria;
import co.edu.unbosque.bogoparche.model.User;
import co.edu.unbosque.bogoparche.repository.AuditoriaRepository;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Registro y consulta de la auditoría. Además de los datos que recibe, toma de la petición HTTP la
 * IP y el navegador.
 */
@Service
public class AuditoriaService {
	private static final Logger log = LoggerFactory.getLogger(AuditoriaService.class);

	@Autowired
	private AuditoriaRepository repo;

	/**
	 * Registra una acción de un usuario autenticado.
	 *
	 * @param u         usuario que hizo la acción
	 * @param accion    acción realizada
	 * @param entidad   tipo de registro afectado, por ejemplo "Salida"
	 * @param entidadId identificador del registro afectado
	 * @param detalle   descripción adicional
	 * @param resultado resultado de la acción
	 */
	public void registrar(User u, AccionAuditoria accion, String entidad, Long entidadId, String detalle,
			ResultadoAuditoria resultado) {
		guardar(u == null ? null : u.getId(), u == null ? null : u.getUsername(),
				u == null ? null : u.getRole().name(), accion, entidad, entidadId, detalle, resultado);
	}

	/**
	 * Registra una acción hecha sin sesión, como el registro, el login o la respuesta a una
	 * invitación.
	 *
	 * @param username  nombre o correo de quien hizo la acción, si se conoce
	 * @param accion    acción realizada
	 * @param detalle   descripción adicional
	 * @param resultado resultado de la acción
	 */
	public void registrarSinSesion(String username, AccionAuditoria accion, String detalle,
			ResultadoAuditoria resultado) {
		guardar(null, username, null, accion, null, null, detalle, resultado);
	}

	/**
	 * Guarda un registro con los datos de la petición actual: IP y navegador. Un fallo al guardar se registra en el log y no interrumpe la operación.
	 *
	 * @param usuarioId     identificador del usuario, o nulo
	 * @param usuarioNombre nombre de usuario, o nulo
	 * @param rol           rol del usuario, o nulo
	 * @param accion        acción realizada
	 * @param entidad       tipo de entidad afectada, o nulo
	 * @param entidadId     identificador de la entidad, o nulo
	 * @param detalle       descripción adicional, o nulo
	 * @param resultado     resultado de la acción
	 */
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

	/**
	 * Busca registros de auditoría, del más reciente al más antiguo.
	 *
	 * @param usuario parte del nombre de usuario; vacío o nulo para todos
	 * @param accion  acción a filtrar; nula para todas
	 * @param desde   fecha inicial
	 * @param hasta   fecha final
	 * @param page    número de página, desde 0
	 * @param size    tamaño de página, entre 1 y 100
	 * @return página de registros
	 */
	public Page<AuditoriaLog> buscar(String usuario, AccionAuditoria accion, LocalDateTime desde, LocalDateTime hasta,
			int page, int size) {
		PageRequest pr = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100),
				Sort.by(Sort.Direction.DESC, "fecha"));
		String u = usuario == null ? "" : usuario.trim();
		return accion == null ? repo.buscar(u, desde, hasta, pr) : repo.buscarPorAccion(u, accion, desde, hasta, pr);
	}

	/**
	 * Recorta un texto al largo máximo de su columna.
	 *
	 * @param s   texto, o nulo
	 * @param max largo máximo
	 * @return el texto recortado, o nulo
	 */
	private String recortar(String s, int max) {
		return s == null ? null : (s.length() > max ? s.substring(0, max) : s);
	}
}
