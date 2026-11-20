package co.edu.unbosque.controller;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import co.edu.unbosque.dto.AuditoriaDTO;
import co.edu.unbosque.model.AccionAuditoria;
import co.edu.unbosque.model.AuditoriaLog;
import co.edu.unbosque.model.ResultadoAuditoria;
import co.edu.unbosque.model.User;
import co.edu.unbosque.service.AuditoriaService;
import co.edu.unbosque.service.ReporteService;

/**
 * Funciones del administrador: consulta de la auditoría y descarga del reporte. Todos los
 * endpoints requieren el rol ADMIN (ver SecurityConfig).
 */
@RestController
@RequestMapping("/admin")
public class AdminController {
	@Autowired
	private AuditoriaService auditoriaService;
	@Autowired
	private ReporteService reporteService;
	@Autowired
	private ModelMapper modelMapper;

	/**
	 * Consulta la auditoría con filtros y paginación, del registro más reciente al más antiguo.
	 *
	 * @param usuario parte del nombre de usuario
	 * @param accion  acción a filtrar
	 * @param desde   fecha inicial (yyyy-MM-dd); sin ella, desde el inicio del historial
	 * @param hasta   fecha final (yyyy-MM-dd); sin ella, hasta hoy
	 * @param page    número de página, desde 0
	 * @param size    tamaño de página, máximo 100
	 * @param auth    administrador autenticado
	 * @return 200 con los registros y los datos de la paginación
	 */
	@GetMapping("/auditoria")
	public ResponseEntity<?> auditoria(@RequestParam(required = false) String usuario,
			@RequestParam(required = false) AccionAuditoria accion,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
			@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size,
			Authentication auth) {
		User actual = (User) auth.getPrincipal();
		Page<AuditoriaLog> pagina = auditoriaService.buscar(usuario, accion, inicio(desde), fin(hasta), page, size);
		auditoriaService.registrar(actual, AccionAuditoria.CONSULTAR_AUDITORIA, null, null,
				"Consulta de auditoría", ResultadoAuditoria.EXITO);
		List<AuditoriaDTO> contenido = pagina.getContent().stream()
				.map(l -> modelMapper.map(l, AuditoriaDTO.class)).toList();
		Map<String, Object> cuerpo = new LinkedHashMap<>();
		cuerpo.put("content", contenido);
		cuerpo.put("page", pagina.getNumber());
		cuerpo.put("size", pagina.getSize());
		cuerpo.put("totalElements", pagina.getTotalElements());
		cuerpo.put("totalPages", pagina.getTotalPages());
		return ResponseEntity.ok(cuerpo);
	}

	/**
	 * Descarga el reporte en Excel del periodo indicado.
	 *
	 * @param desde fecha inicial (yyyy-MM-dd)
	 * @param hasta fecha final (yyyy-MM-dd)
	 * @param auth  administrador autenticado
	 * @return 200 con el archivo .xlsx, o 500 si no se pudo generar
	 */
	@GetMapping("/reporte")
	public ResponseEntity<?> reporte(
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
			Authentication auth) {
		User actual = (User) auth.getPrincipal();
		try {
			byte[] excel = reporteService.generarExcel(inicio(desde), fin(hasta));
			auditoriaService.registrar(actual, AccionAuditoria.DESCARGAR_REPORTE, null, null, "Reporte Excel",
					ResultadoAuditoria.EXITO);
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.parseMediaType(
					"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
			headers.setContentDisposition(ContentDisposition.attachment().filename("reporte-salidas.xlsx").build());
			return new ResponseEntity<>(excel, headers, HttpStatus.OK);
		} catch (IOException e) {
			auditoriaService.registrar(actual, AccionAuditoria.DESCARGAR_REPORTE, null, null,
					"Error al generar el reporte", ResultadoAuditoria.FALLO);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("No se pudo generar el reporte");
		}
	}

	/**
	 * Convierte la fecha inicial del filtro en el inicio de ese día.
	 *
	 * @param d fecha inicial, o nula
	 * @return inicio del día, o el 1 de enero de 2000 si no hay fecha
	 */
	private LocalDateTime inicio(LocalDate d) {
		return d == null ? LocalDateTime.of(2000, 1, 1, 0, 0) : d.atStartOfDay();
	}

	/**
	 * Convierte la fecha final del filtro en el final de ese día.
	 *
	 * @param d fecha final, o nula
	 * @return final del día, o mañana si no hay fecha
	 */
	private LocalDateTime fin(LocalDate d) {
		return d == null ? LocalDateTime.now().plusDays(1) : d.atTime(LocalTime.MAX);
	}
}
