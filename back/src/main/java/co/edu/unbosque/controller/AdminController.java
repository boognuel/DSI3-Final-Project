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

// Solo ADMIN (ver SecurityConfig)
@RestController
@RequestMapping("/admin")
public class AdminController {
	@Autowired
	private AuditoriaService auditoriaService;
	@Autowired
	private ReporteService reporteService;
	@Autowired
	private ModelMapper modelMapper;

	// Fechas en formato yyyy-MM-dd. Sin fechas, devuelve todo el historial.
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

	private LocalDateTime inicio(LocalDate d) {
		return d == null ? LocalDateTime.of(2000, 1, 1, 0, 0) : d.atStartOfDay();
	}

	private LocalDateTime fin(LocalDate d) {
		return d == null ? LocalDateTime.now().plusDays(1) : d.atTime(LocalTime.MAX);
	}
}
