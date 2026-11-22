package co.edu.unbosque.bogoparche.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.time.LocalDateTime;
import java.util.List;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import co.edu.unbosque.bogoparche.model.AccionAuditoria;
import co.edu.unbosque.bogoparche.model.AuditoriaLog;
import co.edu.unbosque.bogoparche.model.User;
import co.edu.unbosque.bogoparche.repository.AuditoriaRepository;
import co.edu.unbosque.bogoparche.repository.InvitacionRepository;
import co.edu.unbosque.bogoparche.repository.SalidaRepository;
import co.edu.unbosque.bogoparche.repository.UserRepository;

/**
 * Pruebas del reporte en Excel: se genera con repositorios simulados y se vuelve a leer.
 */
@ExtendWith(MockitoExtension.class)
class ReporteServiceTest {
	private static final LocalDateTime DESDE = LocalDateTime.of(2026, 11, 1, 0, 0);
	private static final LocalDateTime HASTA = LocalDateTime.of(2026, 11, 30, 23, 59);

	@Mock
	private AuditoriaRepository auditoriaRepo;
	@Mock
	private SalidaRepository salidaRepo;
	@Mock
	private InvitacionRepository invitacionRepo;
	@Mock
	private UserRepository userRepo;
	@InjectMocks
	private ReporteService reporteService;

	/**
	 * El reporte trae la hoja de usuarios, sin contraseñas, y la auditoría con el detalle de cada
	 * registro.
	 *
	 * @throws Exception si no se puede generar o leer el archivo
	 */
	@Test
	void incluyeUsuariosYAuditoria() throws Exception {
		User u = new User();
		u.setId(3L);
		u.setUsername("ana");
		u.setEmail("ana@example.com");
		u.setPassword("hash-secreto");
		u.setEmailVerificado(true);
		u.setFechaCreacion(LocalDateTime.of(2026, 11, 20, 9, 30));
		when(userRepo.findAllByOrderByIdAsc()).thenReturn(List.of(u));
		when(auditoriaRepo.findByFechaBetweenOrderByFechaAsc(DESDE, HASTA)).thenReturn(List.of(registro()));

		try (XSSFWorkbook wb = new XSSFWorkbook(new ByteArrayInputStream(reporteService.generarExcel(DESDE, HASTA)))) {
			Sheet usuarios = wb.getSheet("Usuarios");
			Row fila = usuarios.getRow(1);
			assertEquals("ana", fila.getCell(1).getStringCellValue());
			assertEquals("ana@example.com", fila.getCell(2).getStringCellValue());
			assertEquals("Sí", fila.getCell(4).getStringCellValue());
			assertEquals("2026-11-20 09:30", fila.getCell(5).getStringCellValue());
			assertEquals(6, fila.getLastCellNum());

			Sheet auditoria = wb.getSheet("Auditoría");
			assertEquals("Detalle", auditoria.getRow(0).getCell(8).getStringCellValue());
			assertEquals("ana", auditoria.getRow(1).getCell(1).getStringCellValue());
			assertEquals(9, auditoria.getRow(0).getLastCellNum());
		}
	}

	/**
	 * Crea un registro de auditoría de prueba.
	 *
	 * @return registro
	 */
	private AuditoriaLog registro() {
		AuditoriaLog a = new AuditoriaLog();
		a.setFecha(LocalDateTime.of(2026, 11, 20, 10, 0));
		a.setUsuarioNombre("ana");
		a.setAccion(AccionAuditoria.LOGIN_EXITOSO);
		return a;
	}
}
