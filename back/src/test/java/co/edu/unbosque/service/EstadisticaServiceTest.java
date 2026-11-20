package co.edu.unbosque.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import co.edu.unbosque.dto.EstadisticasDTO;
import co.edu.unbosque.dto.EstadisticasDTO.Conteo;
import co.edu.unbosque.model.EstadoInvitacion;
import co.edu.unbosque.repository.InvitacionRepository;
import co.edu.unbosque.repository.LugarRepository;
import co.edu.unbosque.repository.UserRepository;

/**
 * Pruebas de las series del módulo de estadísticas con repositorios simulados.
 */
@ExtendWith(MockitoExtension.class)
class EstadisticaServiceTest {
	@Mock
	private UserRepository userRepo;
	@Mock
	private LugarRepository lugarRepo;
	@Mock
	private InvitacionRepository invitacionRepo;
	@InjectMocks
	private EstadisticaService estadisticaService;

	/**
	 * Los meses quedan como "aaaa-mm", las categorías vacías aparecen con 0 y todos los estados de
	 * invitación aparecen aunque no tengan invitaciones.
	 */
	@Test
	void armaLasSeriesDeLasGraficas() {
		when(userRepo.contarPorMes()).thenReturn(List.of(new Object[] { 2026, 3, 2L }, new Object[] { 2026, 11, 5L }));
		when(lugarRepo.contarPorCategoria())
				.thenReturn(List.of(new Object[] { "bailar", 0L }, new Object[] { "comer", 4L }));
		when(invitacionRepo.contarPorEstado()).thenReturn(List.<Object[]>of(new Object[] { EstadoInvitacion.ACEPTADA, 3L }));

		EstadisticasDTO e = estadisticaService.calcular();
		assertEquals(List.of(new Conteo("2026-03", 2), new Conteo("2026-11", 5)), e.usuariosPorMes());
		assertEquals(List.of(new Conteo("bailar", 0), new Conteo("comer", 4)), e.lugaresPorCategoria());
		assertEquals(List.of(new Conteo("PENDIENTE", 0), new Conteo("ACEPTADA", 3), new Conteo("RECHAZADA", 0),
				new Conteo("EXPIRADA", 0)), e.invitacionesPorEstado());
	}
}
