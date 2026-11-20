package co.edu.unbosque.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Hyperlink;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import co.edu.unbosque.model.AuditoriaLog;
import co.edu.unbosque.model.EstadoInvitacion;
import co.edu.unbosque.model.Salida;
import co.edu.unbosque.model.User;
import co.edu.unbosque.repository.AuditoriaRepository;
import co.edu.unbosque.repository.InvitacionRepository;
import co.edu.unbosque.repository.SalidaRepository;
import co.edu.unbosque.repository.UserRepository;
import co.edu.unbosque.util.MapaUtil;

/**
 * Genera el reporte en Excel que descarga el administrador.
 */
@Service
public class ReporteService {
	private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

	@Autowired
	private AuditoriaRepository auditoriaRepo;
	@Autowired
	private SalidaRepository salidaRepo;
	@Autowired
	private InvitacionRepository invitacionRepo;
	@Autowired
	private UserRepository userRepo;

	/**
	 * Genera el Excel con cuatro hojas: Resumen (acciones por tipo en el periodo), Usuarios (todos
	 * los usuarios), Auditoría (registros del periodo, con su ubicación en Google Maps) y Salidas.
	 *
	 * @param desde inicio del periodo de la auditoría
	 * @param hasta fin del periodo de la auditoría
	 * @return contenido del archivo .xlsx
	 * @throws IOException si no se puede escribir el archivo
	 */
	public byte[] generarExcel(LocalDateTime desde, LocalDateTime hasta) throws IOException {
		try (XSSFWorkbook wb = new XSSFWorkbook();
				ByteArrayOutputStream out = new ByteArrayOutputStream()) {
			Sheet resumen = wb.createSheet("Resumen");
			fila(resumen, 0, "Periodo", desde.format(FORMATO_FECHA) + " a " + hasta.format(FORMATO_FECHA));
			fila(resumen, 2, "Acción", "Total");
			int n = 3;
			for (Object[] f : auditoriaRepo.contarPorAccion(desde, hasta)) {
				fila(resumen, n++, f[0], f[1]);
			}

			Sheet usuarios = wb.createSheet("Usuarios");
			fila(usuarios, 0, "Id", "Usuario", "Correo", "Rol", "Correo verificado", "Fecha de registro");
			n = 1;
			for (User u : userRepo.findAllByOrderByIdAsc()) {
				fila(usuarios, n++, u.getId(), u.getUsername(), u.getEmail(), u.getRole(),
						u.isEmailVerificado() ? "Sí" : "No", u.getFechaCreacion());
			}

			Sheet auditoria = wb.createSheet("Auditoría");
			fila(auditoria, 0, "Fecha", "Usuario", "Rol", "Acción", "Entidad", "Entidad id", "Resultado", "IP",
					"Latitud", "Longitud", "Ubicación", "Detalle");
			n = 1;
			for (AuditoriaLog a : auditoriaRepo.findByFechaBetweenOrderByFechaAsc(desde, hasta)) {
				Row r = fila(auditoria, n++, a.getFecha(), a.getUsuarioNombre(), a.getRolUsuario(), a.getAccion(),
						a.getEntidad(), a.getEntidadId(), a.getResultado(), a.getIpOrigen(), a.getLatitud(),
						a.getLongitud(), null, a.getDetalle());
				enlazarMapa(wb, r.getCell(10), MapaUtil.enlaceGoogleMaps(a.getLatitud(), a.getLongitud()));
			}

			Sheet salidas = wb.createSheet("Salidas");
			fila(salidas, 0, "Id", "Título", "Organizador", "Presupuesto", "Estado", "Fecha", "Invitaciones aceptadas");
			n = 1;
			for (Salida s : salidaRepo.findAll()) {
				fila(salidas, n++, s.getId(), s.getTitulo(), s.getOrganizador().getUsername(), s.getPresupuesto(),
						s.getEstado(), s.getFechaHora(),
						invitacionRepo.countBySalidaIdAndEstado(s.getId(), EstadoInvitacion.ACEPTADA));
			}
			wb.write(out);
			return out.toByteArray();
		}
	}

	/**
	 * Escribe una fila. Los números quedan como números, las fechas con el formato aaaa-mm-dd hh:mm y
	 * lo demás como texto.
	 *
	 * @param hoja    hoja del libro
	 * @param n       número de la fila, desde 0
	 * @param valores valores de las celdas, en orden
	 * @return la fila creada
	 */
	private Row fila(Sheet hoja, int n, Object... valores) {
		Row row = hoja.createRow(n);
		for (int i = 0; i < valores.length; i++) {
			Cell c = row.createCell(i);
			Object v = valores[i];
			if (v instanceof Number num) {
				c.setCellValue(num.doubleValue());
			} else if (v instanceof LocalDateTime fecha) {
				c.setCellValue(fecha.format(FORMATO_FECHA));
			} else {
				c.setCellValue(v == null ? "" : v.toString());
			}
		}
		return row;
	}

	/**
	 * Pone en la celda un enlace a Google Maps con el texto "Ver en Google Maps".
	 *
	 * @param wb     libro de Excel
	 * @param celda  celda donde va el enlace
	 * @param enlace URL del punto, o nula si el registro no tiene ubicación
	 */
	private void enlazarMapa(XSSFWorkbook wb, Cell celda, String enlace) {
		if (enlace == null) {
			return;
		}
		Hyperlink link = wb.getCreationHelper().createHyperlink(HyperlinkType.URL);
		link.setAddress(enlace);
		celda.setHyperlink(link);
		celda.setCellValue("Ver en Google Maps");
	}
}
